import type { ApiResponse } from '@/interface/plan.interface'

const API_BASE_URL = 'http://localhost:8080/api'

export interface ActivityTypeRevenue {
  activityType: string
  totalRevenue: number
}

export interface RevenueStatistics {
  revenueByActivityType: ActivityTypeRevenue[]
}

export const statisticsApi = {
  async getRevenueStatistics(year?: number, month?: number): Promise<RevenueStatistics> {
    try {
      const params = new URLSearchParams()
      if (year) params.append('year', year.toString())
      if (month) params.append('month', month.toString())
      
      const url = `${API_BASE_URL}/statistics${params.toString() ? '?' + params.toString() : ''}`
      const response = await fetch(url)
      const json: ApiResponse<RevenueStatistics> = await response.json()
      
      if (!response.ok) {
        throw new Error(json.message || 'Failed to fetch statistics')
      }
      
      return json.data
    } catch (error) {
      console.error('Error fetching statistics:', error)
      throw error
    }
  }
}
