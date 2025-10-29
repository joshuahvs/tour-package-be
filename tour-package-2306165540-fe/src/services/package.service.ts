import type { ApiResponse, PackageData, PackageDetailData } from '@/interfaces/package.interface'

const API_BASE_URL = 'http://localhost:8080/api'

export const packageApi = {
  async getAllPackages(searchName?: string): Promise<PackageData[]> {
    try {
      const url = new URL(`${API_BASE_URL}/package`)
      if (searchName) {
        url.searchParams.append('name', searchName)
      }
      
      const response = await fetch(url.toString())
      const json: ApiResponse<PackageData[]> = await response.json()
      
      if (!response.ok) {
        throw new Error(json.message || 'Failed to fetch packages')
      }
      
      return json.data || []
    } catch (error) {
      console.error('Error fetching packages:', error)
      throw error
    }
  },

  async getPackageById(id: string): Promise<PackageData> {
    try {
      const response = await fetch(`${API_BASE_URL}/package/${id}`)
      const json: ApiResponse<PackageData> = await response.json()
      
      if (!response.ok) {
        throw new Error(json.message || 'Failed to fetch package')
      }
      
      return json.data
    } catch (error) {
      console.error('Error fetching package:', error)
      throw error
    }
  },

  async createPackage(packageData: Omit<PackageData, 'id'>): Promise<PackageData> {
    try {
      const response = await fetch(`${API_BASE_URL}/package`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(packageData),
      })
      const json: ApiResponse<PackageData> = await response.json()
      
      if (!response.ok) {
        throw new Error(json.message || 'Failed to create package')
      }
      
      return json.data
    } catch (error) {
      console.error('Error creating package:', error)
      throw error
    }
  },

  async updatePackage(id: string, packageData: PackageData): Promise<PackageData> {
    try {
      const response = await fetch(`${API_BASE_URL}/package/${id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(packageData),
      })
      const json: ApiResponse<PackageData> = await response.json()
      
      if (!response.ok) {
        throw new Error(json.message || 'Failed to update package')
      }
      
      return json.data
    } catch (error) {
      console.error('Error updating package:', error)
      throw error
    }
  },

  async deletePackage(id: string): Promise<void> {
    try {
      const response = await fetch(`${API_BASE_URL}/package/${id}`, {
        method: 'DELETE',
      })
      const json: ApiResponse<null> = await response.json()
      
      if (!response.ok) {
        throw new Error(json.message || 'Failed to delete package')
      }
    } catch (error) {
      console.error('Error deleting package:', error)
      throw error
    }
  },

  async getPackageDetail(id: string): Promise<PackageDetailData> {
    try {
      const response = await fetch(`${API_BASE_URL}/package/${id}/detail`)
      const json: ApiResponse<PackageDetailData> = await response.json()
      
      if (!response.ok) {
        throw new Error(json.message || 'Failed to fetch package detail')
      }
      
      return json.data
    } catch (error) {
      console.error('Error fetching package detail:', error)
      throw error
    }
  },
}
