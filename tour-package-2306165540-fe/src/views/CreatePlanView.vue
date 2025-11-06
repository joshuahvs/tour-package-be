<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { planApi } from '@/services/plan.service'
import { packageApi } from '@/services/package.service'
import type { CreatePlanRequest, LocationData } from '@/interfaces/plan.interface'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const error = ref('')
const isSubmitting = ref(false)
const locations = ref<LocationData[]>([])

const formData = ref<CreatePlanRequest>({
  planName: '',
  activityType: '',
  startDate: '',
  endDate: '',
  startLocation: '',
  endLocation: '',
})

const fetchLocations = async () => {
  try {
    locations.value = await planApi.getLocations()
  } catch (err) {
    console.error('Failed to fetch locations:', err)
    error.value = 'Failed to load locations'
  }
}

const validatePackageStatus = async () => {
  try {
    const packageId = route.params.id as string
    const packageData = await packageApi.getPackageById(packageId)

    if (packageData.status !== 'PENDING') {
      error.value = 'Cannot create plan. Package must have status "Pending".'
      return false
    }

    return true
  } catch (err) {
    console.error('Failed to validate package:', err)
    error.value = 'Failed to validate package status'
    return false
  }
}

const handleSubmit = async () => {
  isSubmitting.value = true
  try {
    const packageId = route.params.id as string
    await planApi.createPlan(packageId, formData.value)
    alert('Plan created successfully!')
    router.push(`/packages/${packageId}`)
  } catch (err: any) {
    const errorMessage = err.message || 'Failed to create plan'
    alert(errorMessage)
    console.error(err)
  } finally {
    isSubmitting.value = false
  }
}

const handleCancel = () => {
  const packageId = route.params.id as string
  router.push(`/packages/${packageId}`)
}

onMounted(async () => {
  loading.value = true
  const isValid = await validatePackageStatus()
  if (!isValid) {
    loading.value = false
    return
  }
  await fetchLocations()
  loading.value = false
})
</script>

<template>
  <div class="max-w-3xl mx-auto p-6">
    <h1 class="text-3xl font-bold text-gray-800 mb-8">Create New Plan</h1>

    <div v-if="loading" class="text-center py-8 text-lg text-gray-600">
      Loading...
    </div>

    <div v-else-if="error" class="text-center bg-red-100 text-red-700 py-4 rounded-lg mb-4">
      {{ error }}
    </div>

    <div v-else class="bg-white rounded-xl shadow-md overflow-hidden">
      <h2 class="bg-gradient-to-r from-indigo-500 to-purple-500 text-white text-xl font-semibold px-6 py-4">
        Plan Information
      </h2>

      <form @submit.prevent="handleSubmit" class="p-6 space-y-6">
        <!-- Plan Name -->
        <div>
          <label for="planName" class="block text-gray-700 font-medium mb-2">
            Plan Name <span class="text-red-500">*</span>
          </label>
          <input
            type="text"
            id="planName"
            v-model="formData.planName"
            placeholder="Jakarta Bali Flight Plan"
            required
            class="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-400"
          />
        </div>

        <!-- Activity Type -->
        <div>
          <label for="activityType" class="block text-gray-700 font-medium mb-2">
            Activity Type <span class="text-red-500">*</span>
          </label>
          <select
            id="activityType"
            v-model="formData.activityType"
            required
            class="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-400"
          >
            <option value="">Select activity type</option>
            <option value="Flight">Flight</option>
            <option value="Vehicle Rental">Vehicle Rental</option>
            <option value="Accommodation">Accommodation</option>
          </select>
        </div>

        <!-- Dates -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label for="startDate" class="block text-gray-700 font-medium mb-2">
              Start Date <span class="text-red-500">*</span>
            </label>
            <input
              type="datetime-local"
              id="startDate"
              v-model="formData.startDate"
              required
              class="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-400"
            />
          </div>

          <div>
            <label for="endDate" class="block text-gray-700 font-medium mb-2">
              End Date <span class="text-red-500">*</span>
            </label>
            <input
              type="datetime-local"
              id="endDate"
              v-model="formData.endDate"
              required
              class="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-400"
            />
          </div>
        </div>

        <!-- Locations -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label for="startLocation" class="block text-gray-700 font-medium mb-2">
              Start Location
            </label>
            <select
              id="startLocation"
              v-model="formData.startLocation"
              required
              class="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-400"
            >
              <option value="">Select start location</option>
              <option
                v-for="location in locations"
                :key="location.code"
                :value="location.name"
              >
                {{ location.name }}
              </option>
            </select>
          </div>

          <div>
            <label for="endLocation" class="block text-gray-700 font-medium mb-2">
              End Location
            </label>
            <select
              id="endLocation"
              v-model="formData.endLocation"
              required
              class="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-400"
            >
              <option value="">Select end location</option>
              <option
                v-for="location in locations"
                :key="location.code"
                :value="location.name"
              >
                {{ location.name }}
              </option>
            </select>
          </div>
        </div>

        <!-- Buttons -->
        <div class="flex flex-col md:flex-row gap-4 pt-6 border-t border-gray-200">
          <button
            type="submit"
            class="flex-1 bg-indigo-500 text-white py-2.5 rounded-md font-medium hover:bg-indigo-600 transition disabled:bg-gray-400 disabled:cursor-not-allowed"
            :disabled="isSubmitting"
          >
            {{ isSubmitting ? 'Creating...' : 'Create Plan' }}
          </button>

          <button
            type="button"
            @click="handleCancel"
            class="flex-1 bg-gray-100 text-gray-800 py-2.5 rounded-md font-medium hover:bg-gray-200 transition"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  </div>
</template>