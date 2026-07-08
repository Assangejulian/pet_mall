import assert from "node:assert/strict"
import { buildMerchantStorePayload, emptyMerchantStoreForm } from "../src/utils/merchantStoreForm.ts"

const validForm = () => ({
  ...emptyMerchantStoreForm(),
  storeName: "真实坐标门店",
  address: "测试地址",
  longitude: "118.123456",
  latitude: "24.654321"
})

for (const field of ["longitude", "latitude"]) {
  const form = validForm()
  form[field] = ""
  assert.equal(buildMerchantStorePayload(form).ok, false, `空${field}必须被拒绝`)
}

for (const [field, value] of [["longitude", "181"], ["longitude", "NaN"], ["latitude", "-91"], ["latitude", "Infinity"]]) {
  const form = validForm()
  form[field] = value
  assert.equal(buildMerchantStorePayload(form).ok, false, `非法${field}=${value}必须被拒绝`)
}

const createResult = buildMerchantStorePayload(validForm())
assert.equal(createResult.ok, true)
assert.equal(createResult.payload.longitude, 118.123456)
assert.equal(createResult.payload.latitude, 24.654321)
assert.equal(typeof createResult.payload.longitude, "number")
assert.equal(typeof createResult.payload.latitude, "number")

const editForm = validForm()
editForm.longitude = 117.9988
editForm.latitude = 25.1122
const editResult = buildMerchantStorePayload(editForm)
assert.equal(editResult.ok, true)
assert.equal(editResult.payload.longitude, 117.9988, "编辑时经度不得丢失")
assert.equal(editResult.payload.latitude, 25.1122, "编辑时纬度不得丢失")

console.log("Merchant store coordinate verification passed: 12 checks")
