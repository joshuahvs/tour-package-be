<template>
  <div class="package-detail-view">
    <div v-if="loading" class="loading">Loading...</div>
    
    <div v-else-if="error" class="error">
      {{ error }}
    </div>
    
    <div v-else-if="packageDetail" class="detail-container">
      <!-- Package Header -->
      <h1 class="package-title">{{ packageDetail.packageName }}</h1>
      
      <!-- Action Buttons -->
      <div class="action-buttons">
        <button class="btn btn-edit">Edit Package</button>
        <button class="btn btn-delete" @click="showDeleteModal = true">Delete Package</button>
        <button class="btn btn-process">Process Package</button>
      </div>

      <!-- Package Information Card -->
      <div class="info-card">
        <h2 class="card-title">Package Information</h2>
        <div class="info-grid">
          <div class="info-item">
            <label>Package Name:</label>
            <span>{{ packageDetail.packageName }}</span>
          </div>
          <div class="info-item">
            <label>User ID:</label>
            <span>{{ packageDetail.userId }}</span>
          </div>
          <div class="info-item">
            <label>End Date:</label>
            <span>{{ formatDate(packageDetail.endDate) }}</span>
          </div>
          <div class="info-item">
            <label>Start Date:</label>
            <span>{{ formatDate(packageDetail.startDate) }}</span>
          </div>
          <div class="info-item">
            <label>Quota:</label>
            <span>{{ packageDetail.quota }}</span>
          </div>
          <div class="info-item">
            <label>Status:</label>
            <span :class="['status-badge', getStatusClass(packageDetail.status)]">
              {{ packageDetail.status }}
            </span>
          </div>
          <div class="info-item">
            <label>Total Price:</label>
            <span>{{ formatCurrency(packageDetail.price) }}</span>
          </div>
        </div>
      </div>

      <!-- Plans for Package Card -->
      <div class="plans-card">
        <h2 class="card-title">Plans for Package</h2>
        
        <div class="table-container">
          <table class="plans-table">
            <thead>
              <tr>
                <th>Plan Name</th>
                <th>Activity Type</th>
                <th>Price</th>
                <th>Start Date</th>
                <th>End Date</th>
                <th>Start Location</th>
                <th>End Location</th>
                <th>Status</th>
                <th>Activities</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="plan in packageDetail.plans" :key="plan.id">
                <td>{{ plan.planName }}</td>
                <td>{{ plan.activityType }}</td>
                <td>{{ formatCurrency(plan.price) }}</td>
                <td>{{ formatDateTime(plan.startDate) }}</td>
                <td>{{ formatDateTime(plan.endDate) }}</td>
                <td>{{ plan.startLocation }}</td>
                <td>{{ plan.endLocation }}</td>
                <td>
                  <span :class="['status-badge', getStatusClass(plan.status)]">
                    {{ plan.status }}
                  </span>
                </td>
                <td class="text-center">{{ plan.activitiesCount }}</td>
                <td>
                  <button class="btn btn-view btn-sm">View</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <div v-if="showDeleteModal" class="modal-overlay" @click="showDeleteModal = false">
      <div class="modal-content" @click.stop>
        <h3 class="modal-title">Delete Package</h3>
        <p class="modal-message">
          Are you sure you want to delete this package? This action cannot be undone and will also delete all associated plans.
        </p>
        <div class="modal-actions">
          <button 
            class="btn btn-confirm"
            @click="handleDelete"
            :disabled="isDeleting"
          >
            {{ isDeleting ? 'Deleting...' : 'OK' }}
          </button>
          <button 
            class="btn btn-cancel"
            @click="showDeleteModal = false"
            :disabled="isDeleting"
          >
            Cancel
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { packageApi } from '@/services/package.service'
import type { PackageDetailData } from '@/interface/package.interface'

const route = useRoute()
const router = useRouter()

const packageDetail = ref<PackageDetailData | null>(null)
const loading = ref(true)
const error = ref('')
const showDeleteModal = ref(false)
const isDeleting = ref(false)

const fetchPackageDetail = async () => {
  try {
    loading.value = true
    const id = route.params.id as string
    packageDetail.value = await packageApi.getPackageDetail(id)
  } catch (err) {
    error.value = 'Failed to load package details'
    console.error(err)
  } finally {
    loading.value = false
  }
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('id-ID', {
    day: 'numeric',
    month: 'long',
    year: 'numeric'
  })
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
  if (statusLower === 'active' || statusLower === 'confirmed') return 'status-success'
  if (statusLower === 'pending') return 'status-warning'
  return 'status-default'
}

const handleDelete = async () => {
  isDeleting.value = true
  
  try {
    const id = route.params.id as string
    await packageApi.deletePackage(id)
    showDeleteModal.value = false
    
    // Show success message
    alert('Package deleted successfully!')
    
    // Navigate back to packages list
    router.push('/packages')
  } catch (err: any) {
    showDeleteModal.value = false
    
    // Show error message from backend
    const errorMessage = err.message || 'Failed to delete package'
    alert(errorMessage)
    
    console.error(err)
  } finally {
    isDeleting.value = false
  }
}

onMounted(() => {
  fetchPackageDetail()
})
</script>

<style scoped>
.package-detail-view {
  padding: 2rem;
  max-width: 1400px;
  margin: 0 auto;
}

.loading,
.error {
  text-align: center;
  padding: 3rem;
  font-size: 1.125rem;
}

.error {
  color: #dc2626;
}

.detail-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.package-title {
  font-size: 2rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 1rem 0;
}

.action-buttons {
  display: flex;
  gap: 0.75rem;
  margin-bottom: 0.5rem;
}

.btn {
  padding: 0.625rem 1.25rem;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-sm {
  padding: 0.375rem 0.75rem;
  font-size: 0.875rem;
}

.btn-edit {
  background: #6366f1;
  color: white;
}

.btn-edit:hover {
  background: #4f46e5;
}

.btn-delete {
  background: #ef4444;
  color: white;
}

.btn-delete:hover {
  background: #dc2626;
}

.btn-process {
  background: #10b981;
  color: white;
}

.btn-process:hover {
  background: #059669;
}

.btn-view {
  background: #6366f1;
  color: white;
}

.btn-view:hover {
  background: #4f46e5;
}

.info-card,
.plans-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.card-title {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: white;
  padding: 1rem 1.5rem;
  margin: 0;
  font-size: 1.125rem;
  font-weight: 600;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.5rem;
  padding: 1.5rem;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.info-item label {
  font-weight: 600;
  color: #6b7280;
  font-size: 0.875rem;
}

.info-item span {
  color: #1f2937;
  font-size: 1rem;
}

.status-badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.875rem;
  font-weight: 500;
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

.table-container {
  overflow-x: auto;
}

.plans-table {
  width: 100%;
  border-collapse: collapse;
}

.plans-table thead {
  background: #f9fafb;
}

.plans-table th {
  padding: 0.75rem 1rem;
  text-align: left;
  font-weight: 600;
  color: #374151;
  font-size: 0.875rem;
  border-bottom: 2px solid #e5e7eb;
  white-space: nowrap;
}

.plans-table td {
  padding: 1rem;
  color: #1f2937;
  font-size: 0.875rem;
  border-bottom: 1px solid #e5e7eb;
}

.plans-table tbody tr:hover {
  background: #f9fafb;
}

.text-center {
  text-align: center;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
}

.modal-content {
  background: #1f2937;
  border-radius: 12px;
  padding: 2rem;
  max-width: 500px;
  width: 100%;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.3);
}

.modal-title {
  color: white;
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0 0 1rem 0;
}

.modal-message {
  color: #d1d5db;
  line-height: 1.6;
  margin: 0 0 1.5rem 0;
}

.modal-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: center;
}

.btn-confirm {
  background: #06b6d4;
  color: white;
  min-width: 80px;
}

.btn-confirm:hover:not(:disabled) {
  background: #0891b2;
}

.btn-cancel {
  background: #4b5563;
  color: white;
  min-width: 80px;
}

.btn-cancel:hover:not(:disabled) {
  background: #374151;
}

@media (max-width: 768px) {
  .package-detail-view {
    padding: 1rem;
  }

  .info-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .action-buttons {
    flex-direction: column;
  }

  .table-container {
    overflow-x: scroll;
  }
}
</style>
