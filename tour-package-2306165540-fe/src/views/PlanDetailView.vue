<template>
  <div class="plan-detail-view">
    <button class="back-button" @click="handleBack">
      ← Back
    </button>

    <h1 class="page-title">View Plan</h1>

    <div v-if="loading" class="loading">Loading...</div>

    <div v-else-if="error" class="error">{{ error }}</div>

    <div v-else-if="planDetail" class="detail-container">
      <!-- Plan Information Card -->
      <div class="info-card">
        <h2 class="card-title">Plan Information</h2>

        <div class="info-grid">
          <div class="info-item">
            <label>Plan Name:</label>
            <span>{{ planDetail.planName }}</span>
          </div>

          <div class="info-item">
            <label>Activity Type:</label>
            <span>{{ planDetail.activityType }}</span>
          </div>

          <div class="info-item">
            <label>Plan Status:</label>
            <span :class="['status-badge', getStatusClass(planDetail.status)]">
              {{ planDetail.status }}
            </span>
          </div>

          <div class="info-item">
            <label>Total Price:</label>
            <span>{{ formatCurrency(planDetail.totalPrice) }}</span>
          </div>

          <div class="info-item">
            <label>Start Date:</label>
            <span>{{ formatDateTime(planDetail.startDate) }}</span>
          </div>

          <div class="info-item">
            <label>End Date:</label>
            <span>{{ formatDateTime(planDetail.endDate) }}</span>
          </div>

          <div class="info-item">
            <label>Start Location:</label>
            <span>{{ planDetail.startLocation }}</span>
          </div>

          <div class="info-item">
            <label>End Location:</label>
            <span>{{ planDetail.endLocation }}</span>
          </div>

          <div class="info-item">
            <label>Package:</label>
            <router-link :to="`/packages/${planDetail.packageId}`" class="package-link">
              {{ planDetail.packageName }}
            </router-link>
          </div>
        </div>

        <div class="action-buttons">
          <button class="btn btn-view" @click="handleViewPackage">View Package</button>
          <button class="btn btn-edit" @click="handleEditPlan">Edit Plan</button>
          <button class="btn btn-delete">Delete Plan</button>
        </div>
      </div>

      <!-- Ordered Activities Card -->
      <div class="activities-card">
        <div class="card-header-with-button">
          <h2 class="card-title">Ordered Activities</h2>
          <button class="btn-add" @click="openAddActivityModal" :aria-busy="loadingActivities ? 'true' : 'false'" aria-label="Add Activity to this plan">
            <svg class="btn-icon" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
              <path fill-rule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clip-rule="evenodd"/>
            </svg>
            <span>Add Activity</span>
          </button>
        </div>

        <div v-if="planDetail.orderedQuantities.length === 0" class="no-data">
          No activities ordered yet.
        </div>

        <div v-else class="table-container">
          <table class="activities-table">
            <thead>
              <tr>
                <th>Activity Name</th>
                <th>Activity ID</th>
                <th>Start Date</th>
                <th>End Date</th>
                <th>Price</th>
                <th>Quota</th>
                <th>Ordered Quota</th>
                <th>Total</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="activity in planDetail.orderedQuantities" :key="activity.id">
                <td>{{ activity.activityName }}</td>
                <td>{{ activity.activityId }}</td>
                <td>{{ formatDateTime(activity.startDate) }}</td>
                <td>{{ formatDateTime(activity.endDate) }}</td>
                <td>{{ formatCurrency(activity.price) }}</td>
                <td class="text-center">{{ activity.quota }}</td>
                <td class="text-center">{{ activity.orderedQuota }}</td>
                <td>{{ formatCurrency(activity.total) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Add Activity Modal -->
    <div v-if="showAddActivityModal" class="modal-overlay" @click="closeAddActivityModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>Add Activity to Plan</h3>
          <button class="btn-close" @click="closeAddActivityModal">×</button>
        </div>

        <div class="modal-body">
          <div v-if="loadingActivities" class="loading-small">Loading activities...</div>
          <div v-else-if="activityError" class="error-small">{{ activityError }}</div>
          <div v-else>
            <div class="form-group">
              <label for="activitySelect">Activity <span class="required">*</span></label>
              <select id="activitySelect" v-model="selectedActivityId" @change="onActivitySelect">
                <option value="">Select an activity</option>
                <option 
                  v-for="activity in allActivities" 
                  :key="activity.id" 
                  :value="activity.id"
                >
                  {{ activity.planName }} - {{ activity.activityType }} ({{ formatCurrency(activity.price) }})
                </option>
              </select>
              <small v-if="planDetail" class="helper-text">
                Showing available plans with matching activity type, dates within range, and matching locations
              </small>
            </div>

            <div v-if="selectedActivity" class="activity-details">
              <h4>Selected Activity Details:</h4>
              <div class="detail-grid">
                <div class="detail-item">
                  <strong>Name:</strong> {{ selectedActivity.planName }}
                </div>
                <div class="detail-item">
                  <strong>Type:</strong> {{ selectedActivity.activityType }}
                </div>
                <div class="detail-item">
                  <strong>Price:</strong> {{ formatCurrency(selectedActivity.price) }}
                </div>
                <div class="detail-item">
                  <strong>Start Date:</strong> {{ formatDateTime(selectedActivity.startDate) }}
                </div>
                <div class="detail-item">
                  <strong>End Date:</strong> {{ formatDateTime(selectedActivity.endDate) }}
                </div>
                <div class="detail-item">
                  <strong>Location:</strong> {{ selectedActivity.startLocation }} → {{ selectedActivity.endLocation }}
                </div>
              </div>

              <div class="form-group">
                <label for="orderedQuantity">Ordered Quantity <span class="required">*</span></label>
                <input
                  id="orderedQuantity"
                  v-model.number="orderedQuantity"
                  type="number"
                  min="1"
                  required
                  placeholder="Enter quantity"
                />
                <small class="helper-text">Enter the quantity needed for this activity</small>
              </div>

              <div v-if="orderedQuantity > 0" class="total-price-display">
                <strong>Total Price:</strong> {{ formatCurrency(selectedActivity.price * orderedQuantity) }}
              </div>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button type="button" @click="closeAddActivityModal" class="btn-cancel">Cancel</button>
          <button 
            type="button" 
            @click="handleAddActivity" 
            :disabled="!selectedActivityId || !orderedQuantity || addingActivity"
            class="btn-submit"
          >
            {{ addingActivity ? 'Adding...' : 'Add Activity' }}
          </button>
        </div>

        <div v-if="addActivityError" class="error-message">{{ addActivityError }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { planApi } from '@/services/plan.service'
import type { PlanDetailData, PlanData, AddOrderedQuantityRequest } from '@/interface/plan.interface'

const route = useRoute()
const router = useRouter()

const planDetail = ref<PlanDetailData | null>(null)
const loading = ref(true)
const error = ref('')

// Add Activity Modal state
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
  } catch (err) {
    error.value = 'Failed to load plan details'
    console.error(err)
  } finally {
    loading.value = false
  }
}

const formatDateTime = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleString('id-ID', {
    hour: '2-digit',
    minute: '2-digit',
    day: 'numeric',
    month: 'long',
    year: 'numeric'
  })
}

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    minimumFractionDigits: 0
  }).format(amount)
}

const getStatusClass = (status: string) => {
  const statusLower = status.toLowerCase()
  if (statusLower === 'fulfilled') return 'status-success'
  if (statusLower === 'unfulfilled') return 'status-warning'
  return 'status-default'
}

const handleBack = () => {
  if (planDetail.value) {
    router.push(`/packages/${planDetail.value.packageId}`)
  } else {
    router.push('/packages')
  }
}

const handleViewPackage = () => {
  if (planDetail.value) {
    router.push(`/packages/${planDetail.value.packageId}`)
  }
}

const handleEditPlan = () => {
  if (planDetail.value) {
    router.push(`/plans/${planDetail.value.id}/edit`)
  }
}

const openAddActivityModal = async () => {
  showAddActivityModal.value = true
  loadingActivities.value = true
  activityError.value = ''
  
  try {
    if (planDetail.value) {
      allActivities.value = await planApi.getAvailablePlansForActivity(planDetail.value.id)
    }
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
  if (selectedActivityId.value) {
    selectedActivity.value = allActivities.value.find(a => a.id === selectedActivityId.value) || null
    orderedQuantity.value = 0
  } else {
    selectedActivity.value = null
    orderedQuantity.value = 0
  }
}

const handleAddActivity = async () => {
  if (!selectedActivityId.value || !orderedQuantity.value || !planDetail.value) return
  
  try {
    addingActivity.value = true
    addActivityError.value = ''

    const requestData: AddOrderedQuantityRequest = {
      activityId: selectedActivityId.value,
      orderedQuantity: orderedQuantity.value
    }

    const updatedPlan = await planApi.addOrderedQuantity(planDetail.value.id, requestData)
    
    // Update plan detail with new data
    planDetail.value = updatedPlan
    
    // Close modal and reset
    closeAddActivityModal()
  } catch (err: any) {
    addActivityError.value = err.message || 'Failed to add activity'
  } finally {
    addingActivity.value = false
  }
}

onMounted(() => {
  fetchPlanDetail()
})
</script>

<style scoped>
.plan-detail-view {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.back-button {
  background: #f3f4f6;
  color: #374151;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  margin-bottom: 1rem;
  transition: background 0.2s;
}

.back-button:hover {
  background: #e5e7eb;
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

.detail-container {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.info-card,
.activities-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.card-title {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: white;
  font-size: 1.25rem;
  font-weight: 600;
  padding: 1.5rem;
  margin: 0;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.5rem;
  padding: 2rem;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.info-item label {
  color: #6b7280;
  font-size: 0.875rem;
  font-weight: 500;
}

.info-item span {
  color: #1f2937;
  font-size: 1rem;
}

.package-link {
  color: #6366f1;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
}

.package-link:hover {
  color: #4f46e5;
  text-decoration: underline;
}

.status-badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.875rem;
  font-weight: 500;
  width: fit-content;
}

.status-success {
  background: #d1fae5;
  color: #065f46;
}

.status-warning {
  background: #fef3c7;
  color: #92400e;
}

.status-default {
  background: #e5e7eb;
  color: #374151;
}

.action-buttons {
  display: flex;
  gap: 1rem;
  padding: 2rem;
  border-top: 1px solid #e5e7eb;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  flex: 1;
}

.btn-view {
  background: #6366f1;
  color: white;
}

.btn-view:hover {
  background: #4f46e5;
}

.btn-edit {
  background: #10b981;
  color: white;
}

.btn-edit:hover {
  background: #059669;
}

.btn-delete {
  background: #ef4444;
  color: white;
}

.btn-delete:hover {
  background: #dc2626;
}

.no-data {
  padding: 3rem 2rem;
  text-align: center;
  color: #6b7280;
  font-size: 1.125rem;
}

.table-container {
  overflow-x: auto;
}

.activities-table {
  width: 100%;
  border-collapse: collapse;
}

.activities-table thead {
  background: #f9fafb;
}

.activities-table th {
  padding: 0.75rem 1rem;
  text-align: left;
  font-weight: 600;
  color: #374151;
  font-size: 0.875rem;
  border-bottom: 2px solid #e5e7eb;
  white-space: nowrap;
}

.activities-table td {
  padding: 1rem;
  color: #1f2937;
  font-size: 0.875rem;
  border-bottom: 1px solid #e5e7eb;
}

.activities-table tbody tr:hover {
  background: #f9fafb;
}

.text-center {
  text-align: center;
}

/* Card Header with Button */
.card-header-with-button {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.card-header-with-button .card-title {
  margin: 0;
}

.btn-add {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 0.6rem 1rem;
  border-radius: 9999px; /* pill */
  cursor: pointer;
  font-size: 0.95rem;
  font-weight: 600;
  letter-spacing: 0.2px;
  transition: transform 0.15s ease, box-shadow 0.2s ease, background 0.2s ease;
  box-shadow: 0 8px 18px rgba(99, 102, 241, 0.25);
  white-space: nowrap;
}

.btn-add:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 22px rgba(99, 102, 241, 0.35);
  background: linear-gradient(135deg, #5258ee 0%, #7c3aed 100%);
}

.btn-add:active {
  transform: translateY(0);
  box-shadow: 0 6px 14px rgba(99, 102, 241, 0.25);
}

.btn-add:focus-visible {
  outline: none;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.35);
}

.btn-add[aria-busy="true"] {
  opacity: 0.8;
  cursor: progress;
}

.btn-icon {
  width: 18px;
  height: 18px;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  max-width: 600px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #ddd;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

.btn-close {
  background: none;
  border: none;
  font-size: 2rem;
  line-height: 1;
  cursor: pointer;
  color: #666;
}

.btn-close:hover {
  color: #000;
}

.modal-body {
  padding: 1.5rem;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  padding: 1.5rem;
  border-top: 1px solid #ddd;
}

.loading-small {
  text-align: center;
  padding: 1rem;
  color: #666;
}

.error-small {
  padding: 1rem;
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
  border-radius: 4px;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #333;
}

.required {
  color: #dc3545;
}

.form-group select,
.form-group input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.form-group select:focus,
.form-group input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
}

.helper-text {
  display: block;
  margin-top: 0.25rem;
  font-size: 0.875rem;
  color: #666;
}

.activity-details {
  margin-top: 1.5rem;
  padding: 1rem;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.activity-details h4 {
  margin-top: 0;
  margin-bottom: 1rem;
  color: #333;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}

.detail-item {
  font-size: 0.9rem;
}

.detail-item strong {
  color: #555;
}

.total-price-display {
  padding: 1rem;
  background-color: #d4edda;
  border: 1px solid #c3e6cb;
  border-radius: 4px;
  text-align: center;
  font-size: 1.1rem;
  color: #155724;
}

.btn-cancel,
.btn-submit {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-cancel {
  background-color: #6c757d;
  color: white;
}

.btn-cancel:hover {
  background-color: #5a6268;
}

.btn-submit {
  background-color: #007bff;
  color: white;
}

.btn-submit:hover:not(:disabled) {
  background-color: #0056b3;
}

.btn-submit:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.error-message {
  margin-top: 1rem;
  padding: 1rem;
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
  border-radius: 4px;
}

@media (max-width: 768px) {
  .plan-detail-view {
    padding: 1rem;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .action-buttons {
    flex-direction: column;
  }

  .table-container {
    overflow-x: scroll;
  }

  .card-header-with-button {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
