<template>
  <div class="create-package-view">
    <h1 class="page-title">Create New Package</h1>

    <div class="form-container">
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
            placeholder="Jakarta - Bali Adventure Package"
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
            placeholder="user001"
            required
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
            placeholder="25"
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
            {{ isSubmitting ? 'Creating...' : 'Create Package' }}
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
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { packageApi } from '@/services/package.service'
import type { CreatePackageRequest } from '@/interface/package.interface'

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
  // Reset error
  errorMessage.value = ''

  // Check if all fields are filled
  if (!formData.packageName || !formData.userId || !formData.startDate || !formData.endDate) {
    errorMessage.value = 'All fields are required'
    return false
  }

  // Check if end date is after start date
  const startDate = new Date(formData.startDate)
  const endDate = new Date(formData.endDate)

  if (endDate <= startDate) {
    errorMessage.value = 'End date must be after start date'
    return false
  }

  // Check quota
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
    const createdPackage = await packageApi.createPackage(formData)
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
  if (confirm('Are you sure you want to cancel? All unsaved changes will be lost.')) {
    router.push('/packages')
  }
}
</script>

<style scoped>
.create-package-view {
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
  .create-package-view {
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
