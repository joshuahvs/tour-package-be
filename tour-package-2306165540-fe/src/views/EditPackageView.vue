
<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { packageApi } from '@/services/package.service'
import type { UpdatePackageRequest } from '@/interface/package.interface'

const router = useRouter()
const route = useRoute()

const loading = ref(true)
const errorLoad = ref('')
const isSubmitting = ref(false)
const errorMessage = ref('')

const formData = reactive<UpdatePackageRequest>({
  packageName: '',
  userId: '',
  quota: 1,
  startDate: '',
  endDate: ''
})

const fetchPackageData = async () => {
  try {
    loading.value = true
    const id = route.params.id as string
    const packageData = await packageApi.getPackageById(id)

    formData.packageName = packageData.packageName
    formData.userId = packageData.userId
    formData.quota = packageData.quota
    formData.startDate = formatDateTimeLocal(packageData.startDate)
    formData.endDate = formatDateTimeLocal(packageData.endDate)
  } catch (error: any) {
    errorLoad.value = error.message || 'Failed to load package data'
    console.error('Error fetching package:', error)
  } finally {
    loading.value = false
  }
}

const formatDateTimeLocal = (dateString: string): string => {
  const date = new Date(dateString)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day}T${hours}:${minutes}`
}

const validateForm = (): boolean => {
  errorMessage.value = ''
  if (!formData.packageName || !formData.userId || !formData.startDate || !formData.endDate) {
    errorMessage.value = 'All fields are required'
    return false
  }

  const startDate = new Date(formData.startDate)
  const endDate = new Date(formData.endDate)
  if (endDate <= startDate) {
    errorMessage.value = 'End date must be after start date'
    return false
  }

  if (formData.quota < 1) {
    errorMessage.value = 'Quota must be at least 1'
    return false
  }

  return true
}

const handleSubmit = async () => {
  if (!validateForm()) return

  isSubmitting.value = true
  errorMessage.value = ''

  try {
    const id = route.params.id as string
    await packageApi.updatePackage(id, formData)
    alert('Package updated successfully!')
    router.push(`/packages/${id}`)
  } catch (error: any) {
    errorMessage.value = error.message || 'Failed to update package. Please try again.'
    console.error('Error updating package:', error)
  } finally {
    isSubmitting.value = false
  }
}

const handleCancel = () => {
  if (confirm('Are you sure you want to cancel? All unsaved changes will be lost.')) {
    const id = route.params.id as string
    router.push(`/packages/${id}`)
  }
}

onMounted(() => {
  fetchPackageData()
})
</script>

<template>
  <div class="max-w-3xl mx-auto p-6">
    <h1 class="text-3xl font-bold text-gray-800 mb-8">Edit Package</h1>

    <!-- Loading -->
    <div v-if="loading" class="text-center py-8 text-lg text-gray-600">
      Loading package data...
    </div>

    <!-- Error Load -->
    <div
      v-else-if="errorLoad"
      class="text-center bg-red-100 text-red-700 py-4 rounded-lg mb-4"
    >
      {{ errorLoad }}
    </div>

    <!-- Form -->
    <div v-else class="bg-white rounded-xl shadow-md overflow-hidden">
      <div class="bg-gradient-to-r from-indigo-500 to-purple-500 text-white px-6 py-4">
        <h2 class="text-xl font-semibold">Package Information</h2>
      </div>

      <form @submit.prevent="handleSubmit" class="p-6 space-y-6">
        <!-- Package Name -->
        <div>
          <label for="packageName" class="block text-gray-700 font-medium mb-2">
            Package Name <span class="text-red-500">*</span>
          </label>
          <input
            id="packageName"
            v-model="formData.packageName"
            type="text"
            required
            class="w-full border border-gray-300 rounded-md px-3 py-2 text-base focus:outline-none focus:ring-2 focus:ring-indigo-400"
          />
        </div>

        <!-- User ID -->
        <div>
          <label for="userId" class="block text-gray-700 font-medium mb-2">
            User ID <span class="text-red-500">*</span>
          </label>
          <input
            id="userId"
            v-model="formData.userId"
            type="text"
            readonly
            disabled
            title="User ID cannot be changed"
            class="w-full border border-gray-300 rounded-md px-3 py-2 text-base bg-gray-100 text-gray-500 cursor-not-allowed"
          />
        </div>

        <!-- Start Date -->
        <div>
          <label for="startDate" class="block text-gray-700 font-medium mb-2">
            Start Date <span class="text-red-500">*</span>
          </label>
          <input
            id="startDate"
            v-model="formData.startDate"
            type="datetime-local"
            required
            class="w-full border border-gray-300 rounded-md px-3 py-2 text-base focus:outline-none focus:ring-2 focus:ring-indigo-400"
          />
        </div>

        <!-- End Date -->
        <div>
          <label for="endDate" class="block text-gray-700 font-medium mb-2">
            End Date <span class="text-red-500">*</span>
          </label>
          <input
            id="endDate"
            v-model="formData.endDate"
            type="datetime-local"
            required
            class="w-full border border-gray-300 rounded-md px-3 py-2 text-base focus:outline-none focus:ring-2 focus:ring-indigo-400"
          />
        </div>

        <!-- Quota -->
        <div>
          <label for="quota" class="block text-gray-700 font-medium mb-2">
            Quota <span class="text-red-500">*</span>
          </label>
          <input
            id="quota"
            v-model.number="formData.quota"
            type="number"
            min="1"
            required
            class="w-full border border-gray-300 rounded-md px-3 py-2 text-base focus:outline-none focus:ring-2 focus:ring-indigo-400"
          />
        </div>

        <!-- Error Message -->
        <div
          v-if="errorMessage"
          class="bg-red-50 border border-red-200 text-red-600 px-4 py-2 rounded-md text-sm"
        >
          {{ errorMessage }}
        </div>

        <!-- Buttons -->
        <div class="flex flex-col md:flex-row gap-4 pt-6 border-t border-gray-200">
          <button
            type="submit"
            class="flex-1 bg-indigo-500 text-white py-2.5 rounded-md font-medium hover:bg-indigo-600 transition disabled:bg-gray-400 disabled:cursor-not-allowed"
            :disabled="isSubmitting"
          >
            {{ isSubmitting ? 'Updating...' : 'Update Package' }}
          </button>

          <button
            type="button"
            @click="handleCancel"
            :disabled="isSubmitting"
            class="flex-1 bg-gray-100 text-gray-800 py-2.5 rounded-md font-medium hover:bg-gray-200 transition"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  </div>
</template>