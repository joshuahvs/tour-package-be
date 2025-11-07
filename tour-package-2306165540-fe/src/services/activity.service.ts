import type { ApiResponse, ActivityData } from '@/interfaces/activity.interface'

const API_BASE_URL = 'http://localhost:8080/api'

export const activityApi = {
  async getActivities(filters?: { activityType?: string; startDate?: string; endDate?: string; search?: string }): Promise<ActivityData[]> {
    try {
      const url = new URL(`${API_BASE_URL}/activities`)
      if (filters) {
        const params = new URLSearchParams()
        if (filters.activityType) params.append('activityType', filters.activityType)
        if (filters.startDate) params.append('startDate', filters.startDate)
        if (filters.endDate) params.append('endDate', filters.endDate)
        if (filters.search) params.append('search', filters.search)
        const queryString = params.toString()
        if (queryString) {
          url.search = queryString
        }
      }
      const response = await fetch(url.toString())
      const json: ApiResponse<ActivityData[]> = await response.json()
      
      if (!response.ok) {
        throw new Error(json.message || 'Failed to fetch activities')
      }
      
      return json.data || []
    } catch (error) {
      console.error('Error fetching activities:', error)
      throw error
    }
  },
}
