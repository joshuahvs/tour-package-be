<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { packageApi } from '@/services/package.service'
import type { PackageDetailData } from '@/interfaces/package.interface'

const route = useRoute()
const router = useRouter()

const packageDetail = ref<PackageDetailData | null>(null)
const loading = ref(true)
const error = ref('')
const showDeleteModal = ref(false)
const isDeleting = ref(false)
const showProcessModal = ref(false)
const isProcessing = ref(false)

const packageInfo = computed(() => {
  if (!packageDetail.value) return {}
  return {
    'Package Name': packageDetail.value.packageName,
    'User ID': packageDetail.value.userId,
    'Start Date': formatDate(packageDetail.value.startDate),
    'End Date': formatDate(packageDetail.value.endDate),
    'Quota': packageDetail.value.quota,
    'Total Price': formatCurrency(packageDetail.value.price)
  }
})

const tableHeaders = [
  'Plan Name', 'Activity Type', 'Price', 'Start Date', 'End Date',
  'Start Location', 'End Location', 'Status', 'Activities', 'Actions'
]

const canEdit = computed(() =>
  packageDetail.value?.status === 'PENDING' &&
  packageDetail.value?.plans.length === 0
)

const canProcess = computed(() =>
  packageDetail.value?.status === 'PENDING' &&
  packageDetail.value?.plans.length > 0 &&
  packageDetail.value.plans.every(plan => plan.status === 'FULFILLED')
)

const fetchPackageDetail = async () => {
  try {
    loading.value = true
    const id = route.params.id as string
    packageDetail.value = await packageApi.getPackageDetail(id)
  } catch {
    error.value = 'Failed to load package details'
  } finally {
    loading.value = false
  }
}

const formatDate = (d: string) =>
  new Date(d).toLocaleDateString('id-ID', { day: 'numeric', month: 'long', year: 'numeric' })

const formatDateTime = (d: string) =>
  new Date(d).toLocaleString('id-ID', { day: 'numeric', month: 'long', year: 'numeric', hour: '2-digit', minute: '2-digit' })

const formatCurrency = (a: number) =>
  new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', minimumFractionDigits: 0 }).format(a)

const getStatusClass = (status: string) => {
  const s = status.toLowerCase()
  if (['processed', 'fulfilled'].includes(s)) return 'bg-emerald-100 text-emerald-800'
  if (['pending', 'unfulfilled'].includes(s)) return 'bg-amber-100 text-amber-800'
  return 'bg-gray-200 text-gray-700'
}

const handleEdit = () => {
  if (!canEdit.value) return alert('Can only edit PENDING packages without plans')
  router.push(`/packages/${route.params.id}/edit`)
}

const handleViewPlan = (id: string) => router.push(`/plans/${id}`)
const handleCreatePlan = () => router.push(`/packages/${route.params.id}/plans/create`)

const handleDelete = async () => {
  isDeleting.value = true
  try {
    await packageApi.deletePackage(route.params.id as string)
    alert('Package deleted successfully!')
    router.push('/packages')
  } catch {
    alert('Failed to delete package')
  } finally {
    isDeleting.value = false
    showDeleteModal.value = false
  }
}

const handleProcess = async () => {
  isProcessing.value = true
  try {
    await packageApi.processPackage(route.params.id as string)
    alert('Package processed successfully!')
    await fetchPackageDetail()
  } catch {
    alert('Failed to process package')
  } finally {
    isProcessing.value = false
    showProcessModal.value = false
  }
}

const handleProcessClick = () => {
  if (!canProcess.value) {
    return alert('Cannot process package. Must be PENDING and all plans fulfilled.')
  }
  showProcessModal.value = true
}

onMounted(fetchPackageDetail)
</script>


<template>
  <div class="p-8 max-w-[1400px] mx-auto">
    <div v-if="loading" class="text-center py-12 text-lg">Loading...</div>

    <div v-else-if="error" class="text-center py-12 text-lg text-red-600">
      {{ error }}
    </div>

    <div v-else-if="packageDetail" class="flex flex-col gap-6">
      <!-- Package Header -->
      <h1 class="text-3xl font-bold text-gray-800 mb-4">
        {{ packageDetail.packageName }}
      </h1>

      <!-- Action Buttons -->
      <div class="flex flex-wrap gap-3 mb-2">
        <button
          class="px-4 py-2 rounded-md font-medium text-white transition 
                 disabled:opacity-60 disabled:cursor-not-allowed 
                 bg-indigo-500 hover:bg-indigo-600"
          @click="handleEdit"
          :disabled="!canEdit"
          :title="!canEdit ? 'Can only edit PENDING packages without plans' : 'Edit this package'"
        >
          Edit Package
        </button>

        <button
          class="px-4 py-2 rounded-md font-medium text-white bg-red-500 hover:bg-red-600 transition"
          @click="showDeleteModal = true"
        >
          Delete Package
        </button>

        <button
          class="px-4 py-2 rounded-md font-medium text-white transition
                 bg-emerald-500 hover:bg-emerald-600
                 disabled:bg-gray-400 disabled:cursor-not-allowed"
          @click="handleProcessClick"
          :disabled="!canProcess"
        >
          Process Package
        </button>
      </div>

      <!-- Package Information Card -->
      <div class="bg-white rounded-lg shadow">
        <h2 class="bg-gradient-to-r from-indigo-500 to-violet-500 text-white px-6 py-4 text-lg font-semibold">
          Package Information
        </h2>
        <div class="grid md:grid-cols-2 gap-6 p-6">
          <div v-for="(value, label) in packageInfo" :key="label" class="flex flex-col gap-1">
            <label class="font-semibold text-gray-500 text-sm">{{ label }}:</label>
            <span class="text-gray-800 text-base">{{ value }}</span>
          </div>
          <div class="flex flex-col gap-1">
            <label class="font-semibold text-gray-500 text-sm">Status:</label>
            <span
              :class="[
                'inline-block px-3 py-1 rounded-full text-sm font-medium',
                getStatusClass(packageDetail.status)
              ]"
            >
              {{ packageDetail.status }}
            </span>
          </div>
        </div>
      </div>

      <!-- Plans for Package Card -->
      <div class="bg-white rounded-lg shadow overflow-hidden">
        <div
          class="bg-gradient-to-r from-indigo-500 to-violet-500 text-white px-6 py-4 flex justify-between items-center"
        >
          <h2 class="text-lg font-semibold">Plans for Package</h2>
          <button
            v-if="packageDetail.status === 'PENDING'"
            class="bg-emerald-500 hover:bg-emerald-600 px-4 py-2 text-sm rounded-md font-medium transition"
            @click="handleCreatePlan"
          >
            Create New Plan
          </button>
        </div>

        <div class="overflow-x-auto">
          <table class="w-full border-collapse">
            <thead class="bg-gray-50 border-b border-gray-200">
              <tr>
                <th v-for="header in tableHeaders" :key="header" class="px-4 py-3 text-left font-semibold text-gray-700 text-sm">
                  {{ header }}
                </th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="plan in packageDetail.plans"
                :key="plan.id"
                class="border-b border-gray-200 hover:bg-gray-50"
              >
                <td class="px-4 py-3 text-gray-800 text-sm">{{ plan.planName }}</td>
                <td class="px-4 py-3 text-gray-800 text-sm">{{ plan.activityType }}</td>
                <td class="px-4 py-3 text-gray-800 text-sm">{{ formatCurrency(plan.price) }}</td>
                <td class="px-4 py-3 text-gray-800 text-sm">{{ formatDateTime(plan.startDate) }}</td>
                <td class="px-4 py-3 text-gray-800 text-sm">{{ formatDateTime(plan.endDate) }}</td>
                <td class="px-4 py-3 text-gray-800 text-sm">{{ plan.startLocation }}</td>
                <td class="px-4 py-3 text-gray-800 text-sm">{{ plan.endLocation }}</td>
                <td class="px-4 py-3">
                  <span
                    :class="[
                      'inline-block px-3 py-1 rounded-full text-sm font-medium',
                      getStatusClass(plan.status)
                    ]"
                  >
                    {{ plan.status }}
                  </span>
                </td>
                <td class="px-4 py-3 text-center text-gray-800 text-sm">
                  {{ plan.activitiesCount }}
                </td>
                <td class="px-4 py-3">
                  <button
                    class="bg-indigo-500 hover:bg-indigo-600 text-white text-sm px-3 py-1 rounded-md transition"
                    @click="handleViewPlan(plan.id)"
                  >
                    View
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <div
      v-if="showDeleteModal"
      class="fixed inset-0 bg-black/60 flex items-center justify-center z-[1000] p-4"
      @click="showDeleteModal = false"
    >
      <div class="bg-gray-800 rounded-xl p-6 max-w-md w-full text-gray-100" @click.stop>
        <h3 class="text-xl font-semibold mb-4">Delete Package</h3>
        <p class="mb-6 text-gray-300">
          Are you sure you want to delete this package? This action cannot be undone and will also delete all associated plans.
        </p>
        <div class="flex gap-3 justify-center">
          <button
            class="bg-cyan-500 hover:bg-cyan-600 text-white px-4 py-2 rounded-md min-w-[80px]"
            @click="handleDelete"
            :disabled="isDeleting"
          >
            {{ isDeleting ? 'Deleting...' : 'OK' }}
          </button>
          <button
            class="bg-gray-600 hover:bg-gray-700 text-white px-4 py-2 rounded-md min-w-[80px]"
            @click="showDeleteModal = false"
            :disabled="isDeleting"
          >
            Cancel
          </button>
        </div>
      </div>
    </div>

    <!-- Process Confirmation Modal -->
    <div
      v-if="showProcessModal"
      class="fixed inset-0 bg-black/60 flex items-center justify-center z-[1000] p-4"
      @click="showProcessModal = false"
    >
      <div class="bg-gray-800 rounded-xl p-6 max-w-md w-full text-gray-100" @click.stop>
        <h3 class="text-xl font-semibold mb-4">Process Package</h3>
        <p class="mb-4 text-gray-300">
          Are you sure you want to process this package? This will change the package status to 'PROCESSED' and book all activities. This action cannot be undone.
        </p>
        <div class="bg-amber-100 border border-amber-300 text-amber-800 px-3 py-2 rounded-md text-sm mb-6">
          ⚠️ All associated activities will have their capacity reduced.
        </div>
        <div class="flex gap-3 justify-center">
          <button
            class="bg-cyan-500 hover:bg-cyan-600 text-white px-4 py-2 rounded-md min-w-[80px]"
            @click="handleProcess"
            :disabled="isProcessing"
          >
            {{ isProcessing ? 'Processing...' : 'OK' }}
          </button>
          <button
            class="bg-gray-600 hover:bg-gray-700 text-white px-4 py-2 rounded-md min-w-[80px]"
            @click="showProcessModal = false"
            :disabled="isProcessing"
          >
            Cancel
          </button>
        </div>
      </div>
    </div>
  </div>
</template>