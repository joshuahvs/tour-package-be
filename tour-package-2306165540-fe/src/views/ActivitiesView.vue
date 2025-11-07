<script setup lang="ts">
import { reactive, ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import DataTable from 'datatables.net-dt'
import 'datatables.net-dt/css/dataTables.dataTables.css'
import { activityApi } from '@/services/activity.service'
import type { ActivityData } from '@/interfaces/activity.interface'

const activities = ref<ActivityData[]>([])
const loading = ref(true)
const error = ref<string | null>(null)
const filterForm = reactive({
  search: '',
  activityType: '',
  startDate: '',
  endDate: ''
})
const activityTypes = ref<string[]>([])
const allActivityTypes = ref<string[]>([])
const tableRef = ref<HTMLTableElement | null>(null)
let dt: any = null

const normalizeDateInput = (value: string) => {
  if (!value) return ''
  return value.length === 16 ? `${value}:00` : value
}

const buildFilterPayload = () => {
  const payload: Record<string, string> = {}
  if (filterForm.activityType) payload.activityType = filterForm.activityType
  if (filterForm.startDate) payload.startDate = normalizeDateInput(filterForm.startDate)
  if (filterForm.endDate) payload.endDate = normalizeDateInput(filterForm.endDate)
  if (filterForm.search) payload.search = filterForm.search.trim()
  return Object.keys(payload).length ? payload : undefined
}

const formatCurrency = (amount: number) =>
  new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', minimumFractionDigits: 0 }).format(amount)

const formatDateTime = (raw: string) => {
  if (!raw) return '-'
  const date = new Date(raw)
  return date.toLocaleString('id-ID', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const deriveActivityTypes = (items: ActivityData[]) => {
  const unique = Array.from(
    new Set(items.map((a) => a.activityType).filter((type): type is string => Boolean(type)))
  )
  unique.sort()
  return unique
}

const syncActivityTypes = (items: ActivityData[], override = false) => {
  if (override || allActivityTypes.value.length === 0) {
    allActivityTypes.value = deriveActivityTypes(items)
  }
  if (allActivityTypes.value.length === 0 && items.length > 0) {
    allActivityTypes.value = deriveActivityTypes(items)
  }
  activityTypes.value = allActivityTypes.value
}

const initDataTable = () => {
  if (!tableRef.value) return
  dt = new DataTable(tableRef.value, {
    data: mapActivitiesToRows(),
    columns: [
      { title: 'Name', data: 'name' },
      { title: 'Type', data: 'type' },
      { title: 'Item', data: 'item' },
      { title: 'Start', data: 'startDate' },
      { title: 'End', data: 'endDate' },
      { title: 'Start Location', data: 'startLocation' },
      { title: 'End Location', data: 'endLocation' },
      { title: 'Price', data: 'price' },
      { title: 'Capacity', data: 'capacity' }
    ],
    paging: true,
    ordering: true,
    info: true,
    searching: false,
    lengthChange: false
  })
}

const loadDataIntoDataTable = () => {
  if (!dt) return
  dt.clear()
  dt.rows.add(mapActivitiesToRows())
  dt.draw()
}

const mapActivitiesToRows = () =>
  activities.value.map((activity) => ({
    name: activity.activityName,
    type: activity.activityType,
    item: activity.activityItem,
    startDate: formatDateTime(activity.startDate),
    endDate: formatDateTime(activity.endDate),
    startLocation: activity.startLocation,
    endLocation: activity.endLocation,
    price: formatCurrency(activity.price),
    capacity: activity.capacity
  }))

const fetchActivities = async () => {
  loading.value = true
  error.value = null
  try {
    const payload = buildFilterPayload()
    activities.value = await activityApi.getActivities(payload)
    if (!payload) {
      syncActivityTypes(activities.value, true)
    } else {
      syncActivityTypes(activities.value)
    }
  } catch (err: any) {
    console.error(err)
    error.value = err?.message || 'Failed to load activities. Please try again later.'
    activities.value = []
  } finally {
    loading.value = false
    await nextTick()
    if (dt) {
      loadDataIntoDataTable()
    } else if (tableRef.value && activities.value.length > 0) {
      initDataTable()
    }
  }
}

const applyFilters = () => {
  if (filterForm.startDate && filterForm.endDate && filterForm.endDate < filterForm.startDate) {
    error.value = 'End date cannot be earlier than start date.'
    return
  }
  if (dt) {
    dt.destroy()
    dt = null
  }
  fetchActivities()
}

const resetFilters = () => {
  filterForm.search = ''
  filterForm.activityType = ''
  filterForm.startDate = ''
  filterForm.endDate = ''
  error.value = null
  if (dt) {
    dt.destroy()
    dt = null
  }
  fetchActivities()
}

onMounted(() => {
  fetchActivities()
})

onBeforeUnmount(() => {
  if (dt) {
    dt.destroy()
    dt = null
  }
})
</script>

<template>
  <section class="max-w-7xl mx-auto px-6 py-8 lg:py-16">
    <div class="flex flex-col gap-2 md:flex-row md:items-end md:justify-between mb-8">
      <div>
        <h1 class="text-3xl font-bold text-slate-800">Activities</h1>
        <p class="text-slate-500 mt-1">Browse all available activities and refine the list using the filters below.</p>
      </div>
    </div>

    <div class="bg-white rounded-xl shadow-md overflow-hidden">
      <div class="bg-gradient-to-r from-blue-500 to-indigo-500 px-6 py-4">
        <h2 class="text-white text-lg font-semibold">Filter Activities</h2>
      </div>

      <form class="bg-gray-50 border-b border-gray-200 px-6 py-6 grid gap-4 md:grid-cols-2 lg:grid-cols-4" @submit.prevent="applyFilters">
        <div class="flex flex-col gap-2">
          <label for="activity-search" class="text-gray-700 font-medium text-sm">Keyword</label>
          <input
            id="activity-search"
            v-model="filterForm.search"
            type="text"
            placeholder="Search by name, item, or location"
            class="border-2 border-gray-200 rounded-lg px-4 py-2.5 text-base focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition"
          />
        </div>

        <div class="flex flex-col gap-2">
          <label for="activity-type" class="text-gray-700 font-medium text-sm">Activity Type</label>
          <select
            id="activity-type"
            v-model="filterForm.activityType"
            class="border-2 border-gray-200 rounded-lg px-4 py-2.5 text-base focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition"
          >
            <option value="">All Types</option>
            <option
              v-for="type in activityTypes"
              :key="type"
              :value="type"
            >
              {{ type }}
            </option>
          </select>
        </div>

        <div class="flex flex-col gap-2">
          <label for="start-date" class="text-gray-700 font-medium text-sm">Start From</label>
          <input
            id="start-date"
            v-model="filterForm.startDate"
            type="datetime-local"
            class="border-2 border-gray-200 rounded-lg px-4 py-2.5 text-base focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition"
          />
        </div>

        <div class="flex flex-col gap-2">
          <label for="end-date" class="text-gray-700 font-medium text-sm">End Until</label>
          <input
            id="end-date"
            v-model="filterForm.endDate"
            type="datetime-local"
            class="border-2 border-gray-200 rounded-lg px-4 py-2.5 text-base focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition"
          />
        </div>

        <div class="flex items-center gap-3 md:col-span-2 lg:col-span-4 mt-2">
          <button
            type="submit"
            class="bg-blue-500 hover:bg-blue-600 text-white font-semibold px-5 py-2.5 rounded-lg transition disabled:opacity-60 disabled:cursor-not-allowed"
            :disabled="loading"
          >
            {{ loading ? 'Filtering...' : 'Apply Filters' }}
          </button>
          <button
            type="button"
            class="bg-gray-600 hover:bg-gray-700 text-white font-semibold px-4 py-2.5 rounded-lg transition disabled:opacity-60 disabled:cursor-not-allowed"
            :disabled="loading"
            @click="resetFilters"
          >
            Reset
          </button>
        </div>
      </form>

      <div v-if="error" class="py-6 text-center text-red-500 text-lg">{{ error }}</div>
      <div v-else-if="loading" class="py-6 text-center text-gray-500 text-lg">Loading activities...</div>
      <div v-else-if="activities.length === 0" class="py-6 text-center text-gray-500 text-lg">
        No activities found for the selected criteria.
      </div>

      <div v-else class="overflow-x-auto">
        <table ref="tableRef" class="w-full border-collapse display">
          <thead class="bg-gray-50 border-b-2 border-gray-200">
            <tr>
              <th class="text-left px-6 py-3 text-sm font-semibold text-gray-600 uppercase tracking-wide">Name</th>
              <th class="text-left px-6 py-3 text-sm font-semibold text-gray-600 uppercase tracking-wide">Type</th>
              <th class="text-left px-6 py-3 text-sm font-semibold text-gray-600 uppercase tracking-wide">Item</th>
              <th class="text-left px-6 py-3 text-sm font-semibold text-gray-600 uppercase tracking-wide">Start</th>
              <th class="text-left px-6 py-3 text-sm font-semibold text-gray-600 uppercase tracking-wide">End</th>
              <th class="text-left px-6 py-3 text-sm font-semibold text-gray-600 uppercase tracking-wide">Start Location</th>
              <th class="text-left px-6 py-3 text-sm font-semibold text-gray-600 uppercase tracking-wide">End Location</th>
              <th class="text-left px-6 py-3 text-sm font-semibold text-gray-600 uppercase tracking-wide">Price</th>
              <th class="text-left px-6 py-3 text-sm font-semibold text-gray-600 uppercase tracking-wide">Capacity</th>
            </tr>
          </thead>
          <tbody></tbody>
        </table>
      </div>
    </div>
  </section>
</template>

<style scoped>
</style>
