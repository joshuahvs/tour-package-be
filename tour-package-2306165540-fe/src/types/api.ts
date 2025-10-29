// API Types
export interface PackageData {
  id: string
  userId: string
  packageName: string
  quota: number
  price: number
  status: string
  startDate: string
  endDate: string
}

export interface ApiResponse<T> {
  status: number
  message: string
  timestamp: string
  data: T
}
