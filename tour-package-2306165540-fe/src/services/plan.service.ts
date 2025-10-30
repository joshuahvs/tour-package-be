import type { ApiResponse, CreatePlanRequest, PlanData, LocationData } from '@/interface/plan.interface'

const API_BASE_URL = 'http://localhost:8080/api'

export const planApi = {
  async createPlan(packageId: string, planData: CreatePlanRequest): Promise<PlanData> {
    try {
      const response = await fetch(`${API_BASE_URL}/packages/${packageId}/plans/create`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(planData),
      })
      const json: ApiResponse<PlanData> = await response.json()
      
      if (!response.ok) {
        throw new Error(json.message || 'Failed to create plan')
      }
      
      return json.data
    } catch (error) {
      console.error('Error creating plan:', error)
      throw error
    }
  },

  async getLocations(): Promise<LocationData[]> {
    try {
      const response = await fetch(`${API_BASE_URL}/locations`)
      const json: ApiResponse<LocationData[]> = await response.json()
      
      if (!response.ok) {
        throw new Error(json.message || 'Failed to fetch locations')
      }
      
      return json.data || []
    } catch (error) {
      console.error('Error fetching locations:', error)
      throw error
    }
  },
}
