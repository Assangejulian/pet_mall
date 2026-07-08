import type { MerchantStorePayload } from "../api/merchantStore"

type CoordinateInput = number | string

export type MerchantStoreForm = Omit<MerchantStorePayload, "longitude" | "latitude"> & {
  longitude: CoordinateInput
  latitude: CoordinateInput
}

export type MerchantStorePayloadResult =
  | { ok: true; payload: MerchantStorePayload }
  | { ok: false; message: string }

export function emptyMerchantStoreForm(): MerchantStoreForm {
  return {
    storeName: "",
    storeLogo: "",
    storePhone: "",
    storeDesc: "",
    province: "",
    city: "",
    district: "",
    address: "",
    longitude: "",
    latitude: ""
  }
}

function parseCoordinate(value: CoordinateInput, label: string, min: number, max: number) {
  if (value === "" || (typeof value === "string" && value.trim() === "")) {
    return { ok: false as const, message: `请填写${label}` }
  }
  const numberValue = typeof value === "number" ? value : Number(value.trim())
  if (!Number.isFinite(numberValue)) {
    return { ok: false as const, message: `${label}必须是有效数字` }
  }
  if (numberValue < min || numberValue > max) {
    return { ok: false as const, message: `${label}必须在 ${min} 至 ${max} 之间` }
  }
  return { ok: true as const, value: numberValue }
}

export function buildMerchantStorePayload(form: MerchantStoreForm): MerchantStorePayloadResult {
  const longitude = parseCoordinate(form.longitude, "经度", -180, 180)
  if (!longitude.ok) return longitude
  const latitude = parseCoordinate(form.latitude, "纬度", -90, 90)
  if (!latitude.ok) return latitude

  return {
    ok: true,
    payload: {
      ...form,
      longitude: longitude.value,
      latitude: latitude.value
    }
  }
}
