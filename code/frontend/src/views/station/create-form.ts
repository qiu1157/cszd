export type StationCreateForm = {
  stationName: string
  platformItemId: number | null
  platformStationCode: string
  address: string
  contactName: string
  contactPhone: string
}

export function createInitialStationCreateForm(): StationCreateForm {
  return {
    stationName: '',
    platformItemId: null,
    platformStationCode: '',
    address: '',
    contactName: '',
    contactPhone: '',
  }
}

export function toCreateStationPayload(form: StationCreateForm) {
  return {
    stationName: form.stationName.trim(),
    platformItemId: form.platformItemId,
    platformStationCode: form.platformStationCode.trim(),
    address: form.address.trim(),
    contactName: form.contactName.trim(),
    contactPhone: form.contactPhone.trim(),
  }
}
