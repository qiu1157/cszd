export const CHARGE_TYPE_OPTIONS = [
  { label: '快充', value: 'FAST' },
  { label: '慢充', value: 'SLOW' },
] as const

export type ChargeTypeValue = (typeof CHARGE_TYPE_OPTIONS)[number]['value']

export function resolveChargeTypeLabel(value: string | null) {
  return CHARGE_TYPE_OPTIONS.find((option) => option.value === value)?.label ?? '--'
}
