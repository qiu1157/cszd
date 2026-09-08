import request from '@/utils/request'

export interface TimelineEvent {
  id: number
  operatedAt: string
  operatorAccount: string
  operatorRole: string
  targetType: string
  targetId: string
  targetName: string
  operationType: string
  remark: string
  beforeState: string | null
  afterState: string | null
}

export interface OperationLogPageData {
  records: TimelineEvent[]
  total: number
  page: number
  size: number
}

export interface OperationLogQueryParams {
  startTime?: string
  endTime?: string
  operatorAccount?: string
  targetType?: string
  stationId?: number
  keyword?: string
  page?: number
  size?: number
}

export async function queryOperationLogs(
  params: OperationLogQueryParams
): Promise<OperationLogPageData> {
  const response = await request.get<OperationLogPageData>('/operation-logs', { params })
  return response.data
}

export async function getOperationLogDetail(id: number): Promise<TimelineEvent> {
  const response = await request.get<TimelineEvent>(`/operation-logs/${id}`)
  return response.data
}

export async function getStationTimeline(stationId: number): Promise<TimelineEvent[]> {
  const response = await request.get<TimelineEvent[]>(`/operation-logs/timeline/station/${stationId}`)
  return response.data
}

export async function getShareholderTimeline(
  shareholderId: number,
  stationId?: number
): Promise<TimelineEvent[]> {
  const params = stationId ? { stationId } : undefined
  const response = await request.get<TimelineEvent[]>(
    `/operation-logs/timeline/shareholder/${shareholderId}`,
    { params }
  )
  return response.data
}
