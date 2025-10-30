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

export interface CreatePlanRequest {
  planName: string
  activityType: string
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
