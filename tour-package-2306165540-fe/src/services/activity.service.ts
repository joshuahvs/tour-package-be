import type { ApiResponse, ActivityData } from '@/interface/activity.interface'

const API_BASE_URL = 'http://localhost:8080/api'

export const activityApi = {
  async getActivities(): Promise<ActivityData[]> {
    try {
      const response = await fetch(`${API_BASE_URL}/activities`)
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

  async getFilteredActivities(activityType: string, startDate: string, endDate: string): Promise<ActivityData[]> {
    try {
      const response = await fetch(
        `${API_BASE_URL}/activities?activityType=${encodeURIComponent(activityType)}&startDate=${encodeURIComponent(startDate)}&endDate=${encodeURIComponent(endDate)}`
      )
      const json: ApiResponse<ActivityData[]> = await response.json()
      
      if (!response.ok) {
        throw new Error(json.message || 'Failed to fetch activities')
      }
      
      return json.data || []
    } catch (error) {
      console.error('Error fetching filtered activities:', error)
      throw error
    }
  },
}
