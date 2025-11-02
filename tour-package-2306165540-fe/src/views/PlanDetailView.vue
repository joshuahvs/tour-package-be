<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { planApi } from '@/services/plan.service'
import type { PlanDetailData, PlanData, AddOrderedQuantityRequest } from '@/interface/plan.interface'

const route = useRoute()
const router = useRouter()

const planDetail = ref<PlanDetailData | null>(null)
const loading = ref(true)
const error = ref('')
const showAddActivityModal = ref(false)
const loadingActivities = ref(false)
const activityError = ref('')
const allActivities = ref<PlanData[]>([])
const selectedActivityId = ref('')
const selectedActivity = ref<PlanData | null>(null)
const orderedQuantity = ref(0)
const addingActivity = ref(false)
const addActivityError = ref('')

const fetchPlanDetail = async () => {
  try {
    loading.value = true
    const planId = route.params.id as string
    planDetail.value = await planApi.getPlanDetail(planId)
  } catch {
    error.value = 'Failed to load plan details'
  } finally {
    loading.value = false
  }
}

const formatDateTime = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleString('id-ID', { hour: '2-digit', minute: '2-digit', day: 'numeric', month: 'long', year: 'numeric' })
}

const formatCurrency = (amount: number) =>
  new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', minimumFractionDigits: 0 }).format(amount)

const handleBack = () => planDetail.value ? router.push(`/packages/${planDetail.value.packageId}`) : router.push('/packages')
const handleViewPackage = () => planDetail.value && router.push(`/packages/${planDetail.value.packageId}`)
const handleEditPlan = () => planDetail.value && router.push(`/plans/${planDetail.value.id}/edit`)

const openAddActivityModal = async () => {
  showAddActivityModal.value = true
  loadingActivities.value = true
  try {
    if (planDetail.value) allActivities.value = await planApi.getAvailablePlansForActivity(planDetail.value.id)
  } catch (err: any) {
    activityError.value = err.message || 'Failed to load activities'
  } finally {
    loadingActivities.value = false
  }
}

const closeAddActivityModal = () => {
  showAddActivityModal.value = false
  selectedActivityId.value = ''
  selectedActivity.value = null
  orderedQuantity.value = 0
  addActivityError.value = ''
}

const onActivitySelect = () => {
  selectedActivity.value = allActivities.value.find(a => a.id === selectedActivityId.value) || null
  orderedQuantity.value = 0
}

const handleAddActivity = async () => {
  if (!selectedActivityId.value || !orderedQuantity.value || !planDetail.value) return
  try {
    addingActivity.value = true
    const req: AddOrderedQuantityRequest = { activityId: selectedActivityId.value, orderedQuantity: orderedQuantity.value }
    planDetail.value = await planApi.addOrderedQuantity(planDetail.value.id, req)
    closeAddActivityModal()
  } catch (err: any) {
    addActivityError.value = err.message || 'Failed to add activity'
  } finally {
    addingActivity.value = false
  }
}

onMounted(fetchPlanDetail)
</script>

<template>
  <div class="max-w-6xl mx-auto p-6">
    <!-- Back button -->
    <button
      @click="handleBack"
      class="mb-4 inline-flex items-center gap-2 rounded-md bg-gray-100 px-3 py-2 text-gray-700 hover:bg-gray-200 transition"
    >
      ← Back
    </button>

    <h1 class="text-3xl font-bold text-gray-800 mb-6">View Plan</h1>

    <div v-if="loading" class="text-center text-gray-500 py-10">Loading...</div>
    <div v-else-if="error" class="text-center text-red-600 bg-red-100 py-4 rounded-lg">{{ error }}</div>

    <div v-else-if="planDetail" class="space-y-8">
      <!-- Plan Information Card -->
      <div class="bg-white rounded-xl shadow p-6">
        <h2 class="text-xl font-semibold text-indigo-600 mb-4">Plan Information</h2>

        <div class="grid md:grid-cols-2 gap-4 text-gray-700">
          <div><span class="font-medium text-gray-500">Plan Name:</span> {{ planDetail.planName }}</div>
          <div><span class="font-medium text-gray-500">Activity Type:</span> {{ planDetail.activityType }}</div>
          <div>
            <span class="font-medium text-gray-500">Plan Status:</span>
            <span
              :class="{
                'bg-green-100 text-green-700 px-2 py-1 rounded-full text-sm': planDetail.status === 'fulfilled',
                'bg-yellow-100 text-yellow-700 px-2 py-1 rounded-full text-sm': planDetail.status === 'unfulfilled',
                'bg-gray-100 text-gray-700 px-2 py-1 rounded-full text-sm': !['fulfilled','unfulfilled'].includes(planDetail.status)
              }"
            >
              {{ planDetail.status }}
            </span>
          </div>
          <div><span class="font-medium text-gray-500">Total Price:</span> {{ formatCurrency(planDetail.totalPrice) }}</div>
          <div><span class="font-medium text-gray-500">Start Date:</span> {{ formatDateTime(planDetail.startDate) }}</div>
          <div><span class="font-medium text-gray-500">End Date:</span> {{ formatDateTime(planDetail.endDate) }}</div>
          <div><span class="font-medium text-gray-500">Start Location:</span> {{ planDetail.startLocation }}</div>
          <div><span class="font-medium text-gray-500">End Location:</span> {{ planDetail.endLocation }}</div>
          <div>
            <span class="font-medium text-gray-500">Package:</span>
            <router-link :to="`/packages/${planDetail.packageId}`" class="text-indigo-600 hover:underline">
              {{ planDetail.packageName }}
            </router-link>
          </div>
        </div>

        <div class="flex flex-wrap gap-3 mt-6 border-t pt-4">
          <button @click="handleViewPackage" class="flex-1 bg-indigo-600 text-white px-4 py-2 rounded-md hover:bg-indigo-700 transition">
            View Package
          </button>
          <button @click="handleEditPlan" class="flex-1 bg-emerald-600 text-white px-4 py-2 rounded-md hover:bg-emerald-700 transition">
            Edit Plan
          </button>
          <button class="flex-1 bg-red-600 text-white px-4 py-2 rounded-md hover:bg-red-700 transition">
            Delete Plan
          </button>
        </div>
      </div>

      <!-- Ordered Activities -->
      <div class="bg-white rounded-xl shadow p-6">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-xl font-semibold text-indigo-600">Ordered Activities</h2>
          <button
            @click="openAddActivityModal"
            :aria-busy="loadingActivities"
            class="inline-flex items-center gap-2 bg-indigo-600 text-white px-4 py-2 rounded-full hover:bg-indigo-700 transition"
          >
            <svg class="w-5 h-5" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clip-rule="evenodd" />
            </svg>
            Add Activity
          </button>
        </div>

        <div v-if="planDetail.orderedQuantities.length === 0" class="text-center text-gray-500 py-10">
          No activities ordered yet.
        </div>

        <div v-else class="overflow-x-auto">
          <table class="w-full text-sm text-left border border-gray-200">
            <thead class="bg-gray-100 text-gray-700">
              <tr>
                <th class="px-4 py-2">Activity Name</th>
                <th class="px-4 py-2">Activity ID</th>
                <th class="px-4 py-2">Start Date</th>
                <th class="px-4 py-2">End Date</th>
                <th class="px-4 py-2">Price</th>
                <th class="px-4 py-2 text-center">Quota</th>
                <th class="px-4 py-2 text-center">Ordered Quota</th>
                <th class="px-4 py-2">Total</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="activity in planDetail.orderedQuantities" :key="activity.id" class="border-t hover:bg-gray-50">
                <td class="px-4 py-2">{{ activity.activityName }}</td>
                <td class="px-4 py-2">{{ activity.activityId }}</td>
                <td class="px-4 py-2">{{ formatDateTime(activity.startDate) }}</td>
                <td class="px-4 py-2">{{ formatDateTime(activity.endDate) }}</td>
                <td class="px-4 py-2">{{ formatCurrency(activity.price) }}</td>
                <td class="px-4 py-2 text-center">{{ activity.quota }}</td>
                <td class="px-4 py-2 text-center">{{ activity.orderedQuota }}</td>
                <td class="px-4 py-2">{{ formatCurrency(activity.total) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Add Activity Modal -->
    <div
      v-if="showAddActivityModal"
      class="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4"
      @click="closeAddActivityModal"
    >
      <div class="bg-white rounded-lg w-full max-w-lg shadow-lg" @click.stop>
        <div class="flex justify-between items-center border-b p-4">
          <h3 class="font-semibold text-gray-800">Add Activity to Plan</h3>
          <button @click="closeAddActivityModal" class="text-2xl text-gray-500 hover:text-gray-700">×</button>
        </div>

        <div class="p-4 space-y-4">
          <div v-if="loadingActivities" class="text-center text-gray-500">Loading activities...</div>
          <div v-else-if="activityError" class="text-red-600 bg-red-100 rounded-md p-2">{{ activityError }}</div>
          <div v-else>
            <div>
              <label class="block font-medium mb-1 text-gray-700">Activity <span class="text-red-500">*</span></label>
              <select
                v-model="selectedActivityId"
                @change="onActivitySelect"
                class="w-full border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500"
              >
                <option value="">Select an activity</option>
                <option
                  v-for="activity in allActivities"
                  :key="activity.id"
                  :value="activity.id"
                >
                  {{ activity.planName }} - {{ activity.activityType }} ({{ formatCurrency(activity.price) }})
                </option>
              </select>
            </div>

            <div v-if="selectedActivity" class="bg-gray-50 p-3 rounded-md mt-4">
              <h4 class="font-semibold text-gray-700 mb-2">Selected Activity</h4>
              <div class="grid grid-cols-2 gap-2 text-sm text-gray-600">
                <div><strong>Name:</strong> {{ selectedActivity.planName }}</div>
                <div><strong>Type:</strong> {{ selectedActivity.activityType }}</div>
                <div><strong>Price:</strong> {{ formatCurrency(selectedActivity.price) }}</div>
                <div><strong>Start:</strong> {{ formatDateTime(selectedActivity.startDate) }}</div>
                <div><strong>End:</strong> {{ formatDateTime(selectedActivity.endDate) }}</div>
                <div><strong>Location:</strong> {{ selectedActivity.startLocation }} → {{ selectedActivity.endLocation }}</div>
              </div>

              <div class="mt-4">
                <label class="block font-medium mb-1 text-gray-700">Ordered Quantity <span class="text-red-500">*</span></label>
                <input
                  v-model.number="orderedQuantity"
                  type="number"
                  min="1"
                  placeholder="Enter quantity"
                  class="w-full border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500"
                />
              </div>

              <div v-if="orderedQuantity > 0" class="mt-3 text-center bg-green-50 text-green-700 p-2 rounded-md">
                <strong>Total Price:</strong> {{ formatCurrency(selectedActivity.price * orderedQuantity) }}
              </div>
            </div>
          </div>
        </div>

        <div class="border-t p-4 flex justify-end gap-2">
          <button @click="closeAddActivityModal" class="px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300">
            Cancel
          </button>
          <button
            @click="handleAddActivity"
            :disabled="!selectedActivityId || !orderedQuantity || addingActivity"
            class="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 disabled:opacity-50"
          >
            {{ addingActivity ? 'Adding...' : 'Add Activity' }}
          </button>
        </div>

        <div v-if="addActivityError" class="text-red-600 bg-red-100 rounded-md m-4 p-2 text-sm">{{ addActivityError }}</div>
      </div>
    </div>
  </div>
</template>