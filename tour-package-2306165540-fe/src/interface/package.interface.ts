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

export interface PlanData {
  id: string
  planName: string
  price: number
  activityType: string
  status: string
  startDate: string
  endDate: string
  startLocation: string
  endLocation: string
  activitiesCount: number
}

export interface PackageDetailData {
  id: string
  userId: string
  packageName: string
  quota: number
  price: number
  status: string
  startDate: string
  endDate: string
  plans: PlanData[]
}

export interface CreatePackageRequest {
  packageName: string
  userId: string
  quota: number
  startDate: string
  endDate: string
}

export interface ApiResponse<T> {
  status: number
  message: string
  timestamp: string
  data: T
}
