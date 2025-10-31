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
        <h2 class="card-title">Ordered Activities</h2>

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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { planApi } from '@/services/plan.service'
import type { PlanDetailData } from '@/interface/plan.interface'

const route = useRoute()
const router = useRouter()

const planDetail = ref<PlanDetailData | null>(null)
const loading = ref(true)
const error = ref('')

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
}
</style>
