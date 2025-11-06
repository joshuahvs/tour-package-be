import type { ApiResponse, CreatePlanRequest, UpdatePlanRequest, AddOrderedQuantityRequest, UpdateOrderedQuantityRequest, PlanData, PlanDetailData, LocationData } from '@/interfaces/plan.interface'

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

  async getPlanDetail(planId: string): Promise<PlanDetailData> {
    try {
      const response = await fetch(`${API_BASE_URL}/packages/plans/${planId}`)
      const json: ApiResponse<PlanDetailData> = await response.json()
      
      if (!response.ok) {
        throw new Error(json.message || 'Failed to fetch plan detail')
      }
      
      return json.data
    } catch (error) {
      console.error('Error fetching plan detail:', error)
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

  async updatePlan(planId: string, planData: UpdatePlanRequest): Promise<PlanDetailData> {
    try {
      const response = await fetch(`${API_BASE_URL}/plans/${planId}/edit`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(planData),
      })
      const json: ApiResponse<PlanDetailData> = await response.json()
      
      if (!response.ok) {
        throw new Error(json.message || 'Failed to update plan')
      }
      
      return json.data
    } catch (error) {
      console.error('Error updating plan:', error)
      throw error
    }
  },

  async addOrderedQuantity(planId: string, data: AddOrderedQuantityRequest): Promise<PlanDetailData> {
    try {
      const response = await fetch(`${API_BASE_URL}/ordered-activities/create?planId=${planId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(data),
      })
      const json: ApiResponse<PlanDetailData> = await response.json()
      
      if (!response.ok) {
        throw new Error(json.message || 'Failed to add activity to plan')
      }
      
      return json.data
    } catch (error) {
      console.error('Error adding activity to plan:', error)
      throw error
    }
  },

  async updateOrderedQuantity(orderedQuantityId: string, data: UpdateOrderedQuantityRequest): Promise<PlanDetailData> {
    try {
      const response = await fetch(`${API_BASE_URL}/ordered-activities/${orderedQuantityId}/edit`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(data),
      })
      const json: ApiResponse<PlanDetailData> = await response.json()
      
      if (!response.ok) {
        throw new Error(json.message || 'Failed to update ordered activity quantity')
      }
      
      return json.data
    } catch (error) {
      console.error('Error updating ordered activity quantity:', error)
      throw error
    }
  },

  async deleteOrderedQuantity(orderedQuantityId: string): Promise<PlanDetailData> {
    try {
      const response = await fetch(`${API_BASE_URL}/ordered-activities/${orderedQuantityId}/delete`, {
        method: 'DELETE',
      })
      const json: ApiResponse<PlanDetailData> = await response.json()
      
      if (!response.ok) {
        throw new Error(json.message || 'Failed to delete ordered activity')
      }
      
      return json.data
    } catch (error) {
      console.error('Error deleting ordered activity:', error)
      throw error
    }
  },

  async deletePlan(planId: string): Promise<void> {
    try {
      const response = await fetch(`${API_BASE_URL}/packages/plans/${planId}/delete`, {
        method: 'DELETE',
      })
      const json: ApiResponse<void> = await response.json()
      
      if (!response.ok) {
        throw new Error(json.message || 'Failed to delete plan')
      }
    } catch (error) {
      console.error('Error deleting plan:', error)
      throw error
    }
  },

  async getAvailablePlansForActivity(planId: string): Promise<PlanData[]> {
    try {
      const response = await fetch(`${API_BASE_URL}/plans/${planId}/available-activities`)
      const json: ApiResponse<PlanData[]> = await response.json()
      
      if (!response.ok) {
        throw new Error(json.message || 'Failed to fetch available activities')
      }
      
      return json.data
    } catch (error) {
      console.error('Error fetching available activities:', error)
      throw error
    }
  },
}
