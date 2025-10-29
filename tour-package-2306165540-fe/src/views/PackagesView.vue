<template>
  <section class="page">
    <div class="header">
      <h1>Packages</h1>
      <button class="btn-primary" @click="navigateToCreate">Create New Package</button>
    </div>

    <div class="packages-container">
      <div class="table-header">
        <h2>All Packages</h2>
      </div>

      <!-- Search Section -->
      <div class="search-section">
        <div class="search-controls">
          <label for="search-input" class="search-label">Search packages:</label>
          <div class="search-input-group">
            <input
              id="search-input"
              v-model="searchQuery"
              type="text"
              placeholder="Enter package name..."
              class="search-input"
              @keyup.enter="handleSearch"
            />
            <button class="btn-search" @click="handleSearch" :disabled="loading">
              {{ loading ? 'Searching...' : 'Search' }}
            </button>
            <button 
              v-if="searchQuery" 
              class="btn-clear" 
              @click="clearSearch"
              :disabled="loading"
            >
              Clear
            </button>
          </div>
        </div>
      </div>

      <div v-if="loading" class="loading">Loading packages...</div>
      <div v-else-if="error" class="error">{{ error }}</div>
      <div v-else-if="packages.length === 0" class="empty">
        {{ searchQuery ? 'No packages found matching your search' : 'No packages found' }}
      </div>

      <div v-else class="table-wrapper">
        <table class="packages-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Period</th>
              <th>Quota</th>
              <th>Price</th>
              <th>Status</th>
              <th>User ID</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="pkg in packages" :key="pkg.id">
              <td class="name-cell">{{ pkg.packageName }}</td>
              <td class="period-cell">{{ formatPeriod(pkg.startDate, pkg.endDate) }}</td>
              <td class="quota-cell">{{ pkg.quota }}</td>
              <td class="price-cell">{{ formatPrice(pkg.price) }}</td>
              <td class="status-cell">
                <span :class="['status-badge', getStatusClass(pkg.status)]">
                  {{ pkg.status }}
                </span>
              </td>
              <td class="user-cell">{{ pkg.userId }}</td>
              <td class="actions-cell">
                <button class="btn-view" @click="viewPackage(pkg.id)">View</button>
              </td>
            </tr>
          </tbody>
        </table>

        <div class="pagination">
          <span class="showing-text">Showing 1 to {{ packages.length }} of {{ packages.length }} packages</span>
          <div class="pagination-controls">
            <button class="btn-pagination" disabled>Previous</button>
            <button class="btn-pagination active">1</button>
            <button class="btn-pagination" disabled>Next</button>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { packageApi } from '@/services/package.service'
import type { PackageData } from '@/interfaces/package.interface'

const router = useRouter()
const packages = ref<PackageData[]>([])
const loading = ref(true)
const error = ref<string | null>(null)
const searchQuery = ref('')

const fetchPackages = async (searchName?: string) => {
  loading.value = true
  error.value = null
  try {
    packages.value = await packageApi.getAllPackages(searchName)
  } catch (err) {
    error.value = 'Failed to load packages. Please try again later.'
    console.error(err)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  fetchPackages(searchQuery.value.trim() || undefined)
}

const clearSearch = () => {
  searchQuery.value = ''
  fetchPackages()
}

const formatPeriod = (startDate: string, endDate: string) => {
  const start = new Date(startDate)
  const end = new Date(endDate)
  
  const formatDate = (date: Date) => {
    return date.toLocaleDateString('en-GB', { 
      day: 'numeric', 
      month: 'long', 
      year: 'numeric' 
    })
  }
  
  return `${formatDate(start)} - ${formatDate(end)}`
}

const formatPrice = (price: number) => {
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    minimumFractionDigits: 0,
  }).format(price)
}

const getStatusClass = (status: string) => {
  const statusLower = status.toLowerCase()
  if (statusLower === 'active') return 'status-active'
  if (statusLower === 'pending') return 'status-pending'
  return 'status-default'
}

const viewPackage = (id: string) => {
  router.push(`/packages/${id}`)
}

const navigateToCreate = () => {
  console.log('Navigate to create package')
  // TODO: Navigate to create package page
}

onMounted(() => {
  fetchPackages()
})
</script>

<style scoped>
.page {
  max-width: 1280px;
  margin: 0 auto;
  padding: 2rem 1.5rem 4rem;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.header h1 {
  font-size: 2rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
}

.btn-primary {
  background: #6366f1;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary:hover {
  background: #4f46e5;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.packages-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.table-header {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  padding: 1.25rem 1.5rem;
}

.table-header h2 {
  color: white;
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0;
}

.search-section {
  padding: 1.5rem;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
}

.search-controls {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.search-label {
  font-size: 0.95rem;
  font-weight: 600;
  color: #374151;
}

.search-input-group {
  display: flex;
  gap: 0.75rem;
  align-items: center;
}

.search-input {
  flex: 1;
  padding: 0.75rem 1rem;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.2s;
}

.search-input:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.btn-search {
  background: #6366f1;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.btn-search:hover:not(:disabled) {
  background: #4f46e5;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.btn-search:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-clear {
  background: #6b7280;
  color: white;
  border: none;
  padding: 0.75rem 1.25rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-clear:hover:not(:disabled) {
  background: #4b5563;
}

.btn-clear:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.loading,
.error,
.empty {
  padding: 3rem;
  text-align: center;
  color: #6b7280;
  font-size: 1.1rem;
}

.error {
  color: #ef4444;
}

.table-wrapper {
  overflow-x: auto;
}

.packages-table {
  width: 100%;
  border-collapse: collapse;
}

.packages-table thead {
  background: #f8fafc;
  border-bottom: 2px solid #e2e8f0;
}

.packages-table th {
  padding: 1rem 1.5rem;
  text-align: left;
  font-weight: 600;
  font-size: 0.875rem;
  color: #475569;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.packages-table tbody tr {
  border-bottom: 1px solid #e2e8f0;
  transition: background-color 0.2s;
}

.packages-table tbody tr:hover {
  background: #f8fafc;
}

.packages-table td {
  padding: 1.25rem 1.5rem;
  color: #334155;
  font-size: 0.95rem;
}

.name-cell {
  font-weight: 600;
  color: #1e293b;
}

.period-cell {
  min-width: 250px;
}

.quota-cell,
.user-cell {
  color: #64748b;
}

.price-cell {
  font-weight: 600;
  color: #059669;
}

.status-badge {
  display: inline-block;
  padding: 0.375rem 0.875rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
  text-transform: capitalize;
}

.status-active {
  background: #d1fae5;
  color: #065f46;
}

.status-pending {
  background: #fed7aa;
  color: #9a3412;
}

.status-default {
  background: #e2e8f0;
  color: #475569;
}

.actions-cell {
  text-align: center;
}

.btn-view {
  background: #6366f1;
  color: white;
  border: none;
  padding: 0.5rem 1.25rem;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.9rem;
}

.btn-view:hover {
  background: #4f46e5;
  transform: translateY(-1px);
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.25rem 1.5rem;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
}

.showing-text {
  color: #64748b;
  font-size: 0.9rem;
}

.pagination-controls {
  display: flex;
  gap: 0.5rem;
}

.btn-pagination {
  padding: 0.5rem 1rem;
  border: 1px solid #e2e8f0;
  background: white;
  color: #475569;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.9rem;
}

.btn-pagination:hover:not(:disabled) {
  background: #f8fafc;
  border-color: #cbd5e1;
}

.btn-pagination:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-pagination.active {
  background: #6366f1;
  color: white;
  border-color: #6366f1;
}
</style>
