export interface ActivityData {
  id: string
  activityName: string
  activityItem: string
  activityType: string
  price: number
  capacity: number
  startDate: string
  endDate: string
  startLocation: string
  endLocation: string
}

export interface ApiResponse<T> {
  status: number
  message: string
  timestamp: string
  data: T
}
