<template>
  <section class="page">
    <h2 class="text-3xl font-bold mb-6">Potential Revenue Statistics</h2>

    <!-- Filters -->
    <div class="bg-white rounded-lg shadow-sm p-6 mb-6">
      <div class="flex items-end gap-4">
        <div class="flex-1">
          <label class="block text-sm font-medium text-gray-700 mb-2">Year:</label>
          <select
            v-model="selectedYear"
            class="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500"
          >
            <option :value="null">All</option>
            <option v-for="year in availableYears" :key="year" :value="year">{{ year }}</option>
          </select>
        </div>
        <div class="flex-1">
          <label class="block text-sm font-medium text-gray-700 mb-2">Month:</label>
          <select
            v-model="selectedMonth"
            class="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500"
          >
            <option :value="null">All</option>
            <option v-for="month in months" :key="month.value" :value="month.value">
              {{ month.label }}
            </option>
          </select>
        </div>
        <button
          @click="loadStatistics"
          :disabled="loading"
          class="px-6 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
        >
          {{ loading ? 'Loading...' : 'Show Statistic' }}
        </button>
      </div>
    </div>

    <!-- Error Message -->
    <div v-if="error" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-6">
      {{ error }}
    </div>

    <!-- Chart -->
    <div class="bg-white rounded-lg shadow-sm p-6">
      <h3 class="text-xl font-semibold mb-4 text-indigo-600">Revenue by Activity Type</h3>
      <div v-if="loading" class="h-96 flex items-center justify-center text-gray-500">
        Loading chart...
      </div>
      <div v-else-if="!statistics || statistics.revenueByActivityType.length === 0" class="h-96 flex items-center justify-center">
        <div class="text-center text-gray-600">
          <p class="text-lg">No data available for the selected period.</p>
        </div>
      </div>
      <div v-else class="h-96">
        <canvas ref="chartCanvas"></canvas>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue'
import { Chart, registerables } from 'chart.js'
import { statisticsApi, type RevenueStatistics } from '@/services/statistics.service'

Chart.register(...registerables)

const selectedYear = ref<number | null>(new Date().getFullYear())
const selectedMonth = ref<number | null>(null)
const loading = ref(false)
const error = ref('')
const statistics = ref<RevenueStatistics | null>(null)
const chartCanvas = ref<HTMLCanvasElement | null>(null)
let chartInstance: Chart | null = null

const availableYears = Array.from({ length: 10 }, (_, i) => new Date().getFullYear() - i)

const months = [
  { value: 1, label: 'January' },
  { value: 2, label: 'February' },
  { value: 3, label: 'March' },
  { value: 4, label: 'April' },
  { value: 5, label: 'May' },
  { value: 6, label: 'June' },
  { value: 7, label: 'July' },
  { value: 8, label: 'August' },
  { value: 9, label: 'September' },
  { value: 10, label: 'October' },
  { value: 11, label: 'November' },
  { value: 12, label: 'December' }
]

const formatCurrency = (value: number) => {
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    minimumFractionDigits: 0
  }).format(value)
}

const loadStatistics = async () => {
  loading.value = true
  error.value = ''
  try {
    const year = selectedYear.value || undefined
    const month = selectedMonth.value || undefined
    
    console.log('Loading statistics with year:', year, 'month:', month)
    statistics.value = await statisticsApi.getRevenueStatistics(year, month)
    console.log('Statistics loaded:', statistics.value)
    // flip loading off BEFORE we render so the canvas actually exists
    loading.value = false
    await nextTick()
    renderChart()
  } catch (err: any) {
    console.error('Error loading statistics:', err)
    error.value = err.message || 'Failed to load statistics'
    loading.value = false
  }
}

const renderChart = () => {
  console.log('renderChart called')
  console.log('chartCanvas.value:', chartCanvas.value)
  console.log('statistics.value:', statistics.value)

  if (!chartCanvas.value || !statistics.value) {
    console.log('Missing canvas or statistics, returning')
    return
  }

  // Ensure canvas has explicit size based on container
  const canvas = chartCanvas.value
  const container = canvas.parentElement as HTMLElement | null
  const containerWidth = container?.clientWidth ?? 800
  const containerHeight = container?.clientHeight ?? 400
  // Set both attributes and styles so Chart.js can compute size
  canvas.width = containerWidth
  canvas.height = containerHeight
  canvas.style.width = '100%'
  canvas.style.height = '100%'

  // Destroy existing chart if any
  if (chartInstance) {
    chartInstance.destroy()
  }

  const ctx = canvas.getContext('2d')
  if (!ctx) {
    console.log('Failed to get canvas context')
    return
  }

  const data = statistics.value.revenueByActivityType
  console.log('Chart data:', data)

  const labels = data.map((item) => item.activityType)
  const values = data.map((item) => Number(item.totalRevenue) || 0)

  console.log('Creating chart with labels:', labels, 'values:', values)

  chartInstance = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: labels,
      datasets: [
        {
          label: 'Revenue',
          data: values,
          backgroundColor: [
            'rgba(79, 70, 229, 0.8)',
            'rgba(59, 130, 246, 0.8)',
            'rgba(16, 185, 129, 0.8)',
            'rgba(245, 158, 11, 0.8)',
            'rgba(239, 68, 68, 0.8)'
          ],
          borderColor: [
            'rgb(79, 70, 229)',
            'rgb(59, 130, 246)',
            'rgb(16, 185, 129)',
            'rgb(245, 158, 11)',
            'rgb(239, 68, 68)'
          ],
          borderWidth: 2
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: false
        },
        tooltip: {
          callbacks: {
            label: function (context) {
              return formatCurrency(context.parsed.y ?? 0)
            }
          }
        }
      },
      scales: {
        x: {
          ticks: {
            color: '#111827'
          },
          grid: {
            color: 'rgba(17, 24, 39, 0.08)'
          }
        },
        y: {
          beginAtZero: true,
          ticks: {
            color: '#111827',
            callback: function (value) {
              return formatCurrency(Number(value))
            }
          },
          grid: {
            color: 'rgba(17, 24, 39, 0.08)'
          }
        }
      }
    }
  })

  // Ensure initial resize after creation
  chartInstance.resize()
}

onMounted(() => {
  loadStatistics()
})

// Watch for year/month changes to clear month when year is cleared
watch(selectedYear, (newYear) => {
  if (!newYear) {
    selectedMonth.value = null
  }
})
</script>

<style scoped>
.page {
  max-width: 1120px;
  margin: 0 auto;
  padding: 2rem 1rem 4rem;
}
</style>
