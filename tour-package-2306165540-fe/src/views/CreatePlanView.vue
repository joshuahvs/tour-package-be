<template>
  <div class="create-plan-view">
    <h1 class="page-title">Create New Plan</h1>

    <div v-if="loading" class="loading">Loading...</div>

    <div v-else-if="error" class="error">{{ error }}</div>

    <div v-else class="form-container">
      <h2 class="form-title">Plan Information</h2>

      <form @submit.prevent="handleSubmit">
        <div class="form-group">
          <label for="planName">Plan Name <span class="required">*</span></label>
          <input
            type="text"
            id="planName"
            v-model="formData.planName"
            placeholder="Jakarta Bali Flight Plan"
            required
          />
        </div>

        <div class="form-group">
          <label for="activityType">Activity Type <span class="required">*</span></label>
          <select id="activityType" v-model="formData.activityType" required>
            <option value="">Select activity type</option>
            <option value="Flight">Flight</option>
            <option value="Vehicle Rental">Vehicle Rental</option>
            <option value="Accommodation">Accommodation</option>
          </select>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="startDate">Start Date <span class="required">*</span></label>
            <input
              type="datetime-local"
              id="startDate"
              v-model="formData.startDate"
              required
            />
          </div>

          <div class="form-group">
            <label for="endDate">End Date <span class="required">*</span></label>
            <input
              type="datetime-local"
              id="endDate"
              v-model="formData.endDate"
              required
            />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="startLocation">Start Location</label>
            <select id="startLocation" v-model="formData.startLocation" required>
              <option value="">Select start location</option>
              <option v-for="location in locations" :key="location.code" :value="location.name">
                {{ location.name }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label for="endLocation">End Location</label>
            <select id="endLocation" v-model="formData.endLocation" required>
              <option value="">Select end location</option>
              <option v-for="location in locations" :key="location.code" :value="location.name">
                {{ location.name }}
              </option>
            </select>
          </div>
        </div>

        <div class="form-actions">
          <button type="submit" class="btn btn-primary" :disabled="isSubmitting">
            {{ isSubmitting ? 'Creating...' : 'Create Plan' }}
          </button>
          <button type="button" class="btn btn-secondary" @click="handleCancel">
            Cancel
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { planApi } from '@/services/plan.service'
import { packageApi } from '@/services/package.service'
import type { CreatePlanRequest, LocationData } from '@/interface/plan.interface'

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

<style scoped>
.create-plan-view {
  padding: 2rem;
  max-width: 800px;
  margin: 0 auto;
}

.page-title {
  color: #1f2937;
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 2rem;
}

.loading,
.error {
  text-align: center;
  padding: 2rem;
  font-size: 1.125rem;
}

.error {
  color: #ef4444;
  background: #fee2e2;
  border-radius: 8px;
}

.form-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.form-title {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: white;
  font-size: 1.25rem;
  font-weight: 600;
  padding: 1.5rem;
  margin: 0;
}

form {
  padding: 2rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

label {
  display: block;
  color: #374151;
  font-weight: 500;
  margin-bottom: 0.5rem;
}

.required {
  color: #ef4444;
}

input[type='text'],
input[type='datetime-local'],
select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 1rem;
  transition: border-color 0.2s;
}

input:focus,
select:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 1px solid #e5e7eb;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 1rem;
}

.btn-primary {
  background: #6366f1;
  color: white;
  flex: 1;
}

.btn-primary:hover:not(:disabled) {
  background: #4f46e5;
}

.btn-primary:disabled {
  background: #9ca3af;
  cursor: not-allowed;
  opacity: 0.6;
}

.btn-secondary {
  background: #f3f4f6;
  color: #374151;
  flex: 1;
}

.btn-secondary:hover {
  background: #e5e7eb;
}

@media (max-width: 768px) {
  .create-plan-view {
    padding: 1rem;
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .form-actions {
    flex-direction: column;
  }
}
</style>
