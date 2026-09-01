import request from '@/utils/request'

type ApiResponse<T> = {
  success: boolean
  code: string
  message: string
  data: T
}

export type PileListItem = {
  id: number
  pileCode: string
  chargeTypeName: string
  gunCount: number
  pileStatusName: string | null
  updatedAt: string
}

export type PileListSummary = {
  pileCount: number
  gunCount: number
}

export type PileCreatePayload = {
  chargeType: string
  gunCount: number
}

export async function getStationPiles(stationId: number) {
  const response = await request.get<ApiResponse<PileListItem[]>>(`/api/stations/${stationId}/piles`)
  return response.data.data
}

export async function getStationNextPileCode(stationId: number) {
  const response = await request.get<ApiResponse<{ pileCode: string }>>(
    `/api/stations/${stationId}/piles/next-code`,
  )
  return response.data.data.pileCode
}

export async function createStationPile(stationId: number, payload: PileCreatePayload) {
  const response = await request.post<ApiResponse<number>>(`/api/stations/${stationId}/piles`, payload)
  return response.data.data
}

export function summarizePiles(piles: PileListItem[]): PileListSummary {
  return {
    pileCount: piles.length,
    gunCount: piles.reduce((accumulator, pile) => accumulator + (pile.gunCount || 0), 0),
  }
}
