<template>
  <div class="edit-package-view">
    <h1 class="page-title">Edit Package</h1>

    <div v-if="loading" class="loading">Loading package data...</div>
    
    <div v-else-if="errorLoad" class="error-message">{{ errorLoad }}</div>

    <div v-else class="form-container">
      <div class="form-header">
        <h2>Package Information</h2>
      </div>

      <form @submit.prevent="handleSubmit" class="package-form">
        <div class="form-group">
          <label for="packageName" class="form-label">
            Package Name <span class="required">*</span>
          </label>
          <input
            id="packageName"
            v-model="formData.packageName"
            type="text"
            class="form-input"
            required
          />
        </div>

        <div class="form-group">
          <label for="userId" class="form-label">
            User ID <span class="required">*</span>
          </label>
          <input
            id="userId"
            v-model="formData.userId"
            type="text"
            class="form-input"
            readonly
            disabled
            title="User ID cannot be changed"
          />
        </div>

        <div class="form-group">
          <label for="startDate" class="form-label">
            Start Date <span class="required">*</span>
          </label>
          <input
            id="startDate"
            v-model="formData.startDate"
            type="datetime-local"
            class="form-input"
            required
          />
        </div>

        <div class="form-group">
          <label for="endDate" class="form-label">
            End Date <span class="required">*</span>
          </label>
          <input
            id="endDate"
            v-model="formData.endDate"
            type="datetime-local"
            class="form-input"
            required
          />
        </div>

        <div class="form-group">
          <label for="quota" class="form-label">
            Quota <span class="required">*</span>
          </label>
          <input
            id="quota"
            v-model.number="formData.quota"
            type="number"
            class="form-input"
            min="1"
            required
          />
        </div>

        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>

        <div class="form-actions">
          <button 
            type="submit" 
            class="btn btn-primary"
            :disabled="isSubmitting"
          >
            {{ isSubmitting ? 'Updating...' : 'Update Package' }}
          </button>
          <button 
            type="button" 
            class="btn btn-secondary"
            @click="handleCancel"
            :disabled="isSubmitting"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { packageApi } from '@/services/package.service'
import type { UpdatePackageRequest } from '@/interface/package.interface'

const router = useRouter()
const route = useRoute()

const loading = ref(true)
const errorLoad = ref('')
const formData = reactive<UpdatePackageRequest>({
  packageName: '',
  userId: '',
  quota: 1,
  startDate: '',
  endDate: ''
})

const isSubmitting = ref(false)
const errorMessage = ref('')

const fetchPackageData = async () => {
  try {
    loading.value = true
    const id = route.params.id as string
    const packageData = await packageApi.getPackageById(id)
    
    // Populate form with existing data
    formData.packageName = packageData.packageName
    formData.userId = packageData.userId
    formData.quota = packageData.quota
    
    // Convert datetime to datetime-local format (YYYY-MM-DDTHH:mm)
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
  if (!validateForm()) {
    return
  }

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

<style scoped>
.edit-package-view {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem 1.5rem;
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 2rem;
}

.loading {
  text-align: center;
  padding: 3rem;
  font-size: 1.125rem;
  color: #6b7280;
}

.form-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.form-header {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: white;
  padding: 1rem 1.5rem;
}

.form-header h2 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
}

.package-form {
  padding: 2rem 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-label {
  font-weight: 600;
  color: #374151;
  font-size: 0.875rem;
}

.required {
  color: #ef4444;
}

.form-input {
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 1rem;
  transition: all 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.form-input:disabled {
  background: #f3f4f6;
  cursor: not-allowed;
  color: #6b7280;
}

.form-input::placeholder {
  color: #9ca3af;
}

.error-message {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #dc2626;
  padding: 0.75rem;
  border-radius: 6px;
  font-size: 0.875rem;
}

.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 1rem;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background: #6366f1;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #4f46e5;
}

.btn-secondary {
  background: #e5e7eb;
  color: #374151;
}

.btn-secondary:hover:not(:disabled) {
  background: #d1d5db;
}

@media (max-width: 768px) {
  .edit-package-view {
    padding: 1rem;
  }

  .form-actions {
    flex-direction: column;
  }

  .btn {
    width: 100%;
  }
}
</style>
