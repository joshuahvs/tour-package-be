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

export interface OrderedQuantityData {
  id: string
  activityName: string
  activityId: string
  startDate: string
  endDate: string
  price: number
  quota: number
  orderedQuota: number
  total: number
}

export interface PlanDetailData {
  id: string
  planName: string
  activityType: string
  status: string
  totalPrice: number
  startDate: string
  endDate: string
  startLocation: string
  endLocation: string
  packageId: string
  packageName: string
  orderedQuantities: OrderedQuantityData[]
}

export interface CreatePlanRequest {
  planName: string
  activityType: string
  startDate: string
  endDate: string
  startLocation: string
  endLocation: string
}

export interface UpdatePlanRequest {
  planName: string
  startDate: string
  endDate: string
  startLocation: string
  endLocation: string
}

export interface LocationData {
  code: string
  name: string
}

export interface ApiResponse<T> {
  status: number
  message: string
  timestamp: string
  data: T
}
