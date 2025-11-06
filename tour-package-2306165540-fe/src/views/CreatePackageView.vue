
<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { packageApi } from '@/services/package.service'
import type { CreatePackageRequest } from '@/interfaces/package.interface'

const router = useRouter()

const formData = reactive<CreatePackageRequest>({
  packageName: '',
  userId: '',
  quota: 1,
  startDate: '',
  endDate: ''
})

const isSubmitting = ref(false)
const errorMessage = ref('')

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
    await packageApi.createPackage(formData)
    alert('Package created successfully!')
    router.push('/packages')
  } catch (error: any) {
    errorMessage.value = error.message || 'Failed to create package. Please try again.'
    console.error('Error creating package:', error)
  } finally {
    isSubmitting.value = false
  }
}

const handleCancel = () => {
  if (confirm('Are you sure you want to cancel?')) {
    router.push('/packages')
  }
}
</script>

<template>
  <div class="max-w-3xl mx-auto p-6">
    <h1 class="text-3xl font-bold text-gray-800 mb-8">Create New Package</h1>

    <div class="bg-white rounded-lg shadow">
      <div class="bg-gradient-to-r from-indigo-500 to-purple-500 text-white px-6 py-4 rounded-t-lg">
        <h2 class="text-lg font-semibold">Package Information</h2>
      </div>

      <form @submit.prevent="handleSubmit" class="p-6 flex flex-col gap-5">
        <!-- Package Name -->
        <div>
          <label for="packageName" class="block text-sm font-semibold text-gray-700 mb-1">
            Package Name <span class="text-red-500">*</span>
          </label>
          <input
            id="packageName"
            v-model="formData.packageName"
            type="text"
            placeholder="Jakarta - Bali Adventure Package"
            required
            class="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none"
          />
        </div>

        <!-- User ID -->
        <div>
          <label for="userId" class="block text-sm font-semibold text-gray-700 mb-1">
            User ID <span class="text-red-500">*</span>
          </label>
          <input
            id="userId"
            v-model="formData.userId"
            type="text"
            placeholder="user001"
            required
            class="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none"
          />
        </div>

        <!-- Start Date -->
        <div>
          <label for="startDate" class="block text-sm font-semibold text-gray-700 mb-1">
            Start Date <span class="text-red-500">*</span>
          </label>
          <input
            id="startDate"
            v-model="formData.startDate"
            type="datetime-local"
            required
            class="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none"
          />
        </div>

        <!-- End Date -->
        <div>
          <label for="endDate" class="block text-sm font-semibold text-gray-700 mb-1">
            End Date <span class="text-red-500">*</span>
          </label>
          <input
            id="endDate"
            v-model="formData.endDate"
            type="datetime-local"
            required
            class="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none"
          />
        </div>

        <!-- Quota -->
        <div>
          <label for="quota" class="block text-sm font-semibold text-gray-700 mb-1">
            Quota <span class="text-red-500">*</span>
          </label>
          <input
            id="quota"
            v-model.number="formData.quota"
            type="number"
            placeholder="25"
            min="1"
            required
            class="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none"
          />
        </div>

        <!-- Error Message -->
        <div v-if="errorMessage" class="bg-red-50 border border-red-200 text-red-600 px-4 py-2 rounded-md text-sm">
          {{ errorMessage }}
        </div>

        <!-- Buttons -->
        <div class="flex flex-col sm:flex-row gap-3 mt-4">
          <button
            type="submit"
            class="w-full sm:w-auto bg-indigo-600 hover:bg-indigo-700 text-white font-semibold py-2.5 px-6 rounded-md transition disabled:opacity-60 disabled:cursor-not-allowed"
            :disabled="isSubmitting"
          >
            {{ isSubmitting ? 'Creating...' : 'Create Package' }}
          </button>

          <button
            type="button"
            @click="handleCancel"
            class="w-full sm:w-auto bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold py-2.5 px-6 rounded-md transition disabled:opacity-60 disabled:cursor-not-allowed"
            :disabled="isSubmitting"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
