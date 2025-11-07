
<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { packageApi } from '@/services/package.service'
import type { PackageData } from '@/interfaces/package.interface'
import DataTable from 'datatables.net-dt'
import 'datatables.net-dt/css/dataTables.dataTables.css'

const router = useRouter()
const packages = ref<PackageData[]>([])
const loading = ref(true)
const error = ref<string | null>(null)
const searchQuery = ref('')
const tableRef = ref<HTMLTableElement | null>(null)
let dt: any = null

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
    // Wait for DOM to update before initializing/updating DataTable
    await nextTick()
    if (dt) {
      loadDataIntoDataTable()
    } else if (tableRef.value && packages.value.length > 0) {
      initDataTable()
    }
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
    minimumFractionDigits: 0
  }).format(price)
}

const viewPackage = (id: string) => router.push(`/packages/${id}`)
const navigateToCreate = () => router.push('/packages/create')

const initDataTable = () => {
  if (!tableRef.value) return
  dt = new DataTable(tableRef.value, {
    data: mapPackagesToRows(),
    columns: [
      { title: 'Name', data: 'name' },
      { title: 'Period', data: 'period' },
      { title: 'Quota', data: 'quota' },
      { title: 'Price', data: 'price' },
      {
        title: 'Status',
        data: 'status',
        render: (data: string, type: string, row: any) => {
          if (type === 'display') return statusBadge(row.status)
          return data
        }
      },
      { title: 'User ID', data: 'userId' },
      {
        title: 'Actions',
        data: 'id',
        orderable: false,
        searchable: false,
        render: (id: string) => actionButtons(id)
      }
    ],
    paging: true,
    searching: false,
    ordering: true,
    info: true,
    lengthChange: false
  })
}

const loadDataIntoDataTable = () => {
  if (!dt) return
  dt.clear()
  dt.rows.add(mapPackagesToRows())
  dt.draw()
}

const mapPackagesToRows = () => {
  return packages.value.map((p) => ({
    id: p.id,
    name: p.packageName,
    period: formatPeriod(p.startDate, p.endDate),
    quota: p.quota,
    price: formatPrice(p.price),
    status: p.status,
    userId: p.userId
  }))
}

const statusBadge = (status: string) => {
  const st = status.toLowerCase()
  const cls =
    st === 'processed'
      ? 'bg-green-100 text-green-700'
      : st === 'pending'
      ? 'bg-orange-100 text-orange-800'
      : 'bg-gray-100 text-gray-700'
  return `<span class="inline-block px-3 py-1 rounded-full text-sm font-semibold capitalize ${cls}">${status}</span>`
}

const actionButtons = (id: string) => {
  return `<button class="dt-view-btn bg-indigo-500 hover:bg-indigo-600 text-white font-medium px-4 py-2 rounded-md text-sm transition" data-id="${id}">View</button>`
}

onMounted(() => {
  fetchPackages()
  // delegate clicks for action buttons inside DataTable
  const handler = (e: Event) => {
    const target = e.target as HTMLElement
    const btn = target.closest('.dt-view-btn') as HTMLButtonElement | null
    if (btn && btn.dataset.id) {
      viewPackage(btn.dataset.id!)
    }
  }
  tableRef.value?.addEventListener('click', handler)
  if (tableRef.value) {
    (tableRef.value as any)._dtClickHandler = handler
  }
})

onBeforeUnmount(() => {
  if (dt) {
    dt.destroy()
    dt = null
  }
  const el = tableRef.value as any
  if (el && el._dtClickHandler) {
    el.removeEventListener('click', el._dtClickHandler)
    delete el._dtClickHandler
  }
})
</script>


<template>
  <section class="max-w-7xl mx-auto px-6 py-8 lg:py-16">
    <!-- Header -->
    <div class="flex justify-between items-center mb-8">
      <h1 class="text-3xl font-bold text-slate-800">Packages</h1>
      <button
        class="bg-indigo-500 hover:bg-indigo-600 text-white font-semibold px-6 py-3 rounded-lg shadow-sm hover:shadow-md transition transform hover:-translate-y-0.5"
        @click="navigateToCreate"
      >
        Create New Package
      </button>
    </div>

    <!-- Container -->
    <div class="bg-white rounded-xl shadow-md overflow-hidden">
      <!-- Table Header -->
      <div class="bg-gradient-to-r from-indigo-500 to-violet-500 px-6 py-4">
        <h2 class="text-white text-lg font-semibold">All Packages</h2>
      </div>

      <!-- Search Section -->
      <div class="bg-gray-50 border-b border-gray-200 px-6 py-6">
        <div class="flex flex-col gap-3">
          <label for="search-input" class="text-gray-700 font-medium text-sm">Search packages:</label>
          <div class="flex items-center gap-3">
            <input
              id="search-input"
              v-model="searchQuery"
              type="text"
              placeholder="Enter package name..."
              class="flex-1 border-2 border-gray-200 rounded-lg px-4 py-2.5 text-base focus:outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 transition"
              @keyup.enter="handleSearch"
            />
            <button
              class="bg-indigo-500 hover:bg-indigo-600 text-white font-semibold px-5 py-2.5 rounded-lg transition disabled:opacity-60 disabled:cursor-not-allowed"
              @click="handleSearch"
              :disabled="loading"
            >
              {{ loading ? 'Searching...' : 'Search' }}
            </button>
            <button
              v-if="searchQuery"
              class="bg-gray-600 hover:bg-gray-700 text-white font-semibold px-4 py-2.5 rounded-lg transition disabled:opacity-60 disabled:cursor-not-allowed"
              @click="clearSearch"
              :disabled="loading"
            >
              Clear
            </button>
          </div>
        </div>
      </div>

      <!-- State Messages -->
      <div v-if="loading" class="py-12 text-center text-gray-500 text-lg">Loading packages...</div>
      <div v-else-if="error" class="py-12 text-center text-red-500 text-lg">{{ error }}</div>
      <div
        v-else-if="packages.length === 0"
        class="py-12 text-center text-gray-500 text-lg"
      >
        {{ searchQuery ? 'No packages found matching your search' : 'No packages found' }}
      </div>

      <!-- Table -->
      <div v-else class="overflow-x-auto">
        <table ref="tableRef" class="w-full border-collapse display">
          <thead class="bg-gray-50 border-b-2 border-gray-200">
            <tr>
              <th class="text-left px-6 py-3 text-sm font-semibold text-gray-600 uppercase tracking-wide">
                Name
              </th>
              <th class="text-left px-6 py-3 text-sm font-semibold text-gray-600 uppercase tracking-wide">
                Period
              </th>
              <th class="text-left px-6 py-3 text-sm font-semibold text-gray-600 uppercase tracking-wide">
                Quota
              </th>
              <th class="text-left px-6 py-3 text-sm font-semibold text-gray-600 uppercase tracking-wide">
                Price
              </th>
              <th class="text-left px-6 py-3 text-sm font-semibold text-gray-600 uppercase tracking-wide">
                Status
              </th>
              <th class="text-left px-6 py-3 text-sm font-semibold text-gray-600 uppercase tracking-wide">
                User ID
              </th>
              <th class="text-left px-6 py-3 text-sm font-semibold text-gray-600 uppercase tracking-wide">
                Actions
              </th>
            </tr>
          </thead>
          <tbody></tbody>
        </table>
      </div>
    </div>
  </section>
</template>