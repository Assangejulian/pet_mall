<template>
  <div class="admin-page">
    <div class="page-header"><h2>我的门店</h2><p>维护自己的门店资料与审核状态</p></div>
    <div class="search-bar">
      <input v-model="keyword" placeholder="搜索门店名称" @keyup.enter="search" />
      <select v-model.number="statusFilter"><option :value="-1">全部状态</option><option :value="0">待审核</option><option :value="1">营业中</option><option :value="2">已关闭</option><option :value="3">审核驳回</option></select>
      <button class="btn btn-primary" @click="search">搜索</button><button class="btn btn-outline" @click="reset">重置</button>
      <button class="btn btn-primary push-right" @click="openCreate">+ 新增门店</button>
    </div>
    <div class="table-wrap">
      <table class="data-table"><thead><tr><th>门店</th><th>联系电话</th><th>地址</th><th>商品数</th><th>状态</th><th>审核/关闭说明</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="item in records" :key="item.id"><td>{{ item.storeName }}</td><td>{{ item.storePhone || '-' }}</td><td>{{ fullAddress(item) }}</td><td>{{ item.productCount ?? 0 }}</td><td><span class="badge" :class="statusBadge(item.status)">{{ storeStatusLabel(item.status) }}</span></td><td><span v-if="item.status===2 && item.closeReason">{{ item.closeReason }}</span><span v-else-if="item.auditRemark">{{ item.auditRemark }}<small v-if="item.auditTime" class="meta-time">{{ item.auditTime }}</small></span><span v-else>-</span></td><td class="actions"><button class="btn btn-outline btn-sm" @click="openEdit(item.id)">编辑</button><button class="btn btn-danger btn-sm" @click="remove(item)">删除</button></td></tr>
          <tr v-if="!loading && !records.length"><td colspan="7" class="empty-row">暂无门店</td></tr><tr v-if="loading"><td colspan="7" class="empty-row">加载中...</td></tr>
        </tbody>
      </table>
      <div class="pagination" v-if="total"><button :disabled="page <= 1" @click="go(page-1)">上一页</button><span>第 {{ page }} / {{ totalPages }} 页，共 {{ total }} 条</span><button :disabled="page >= totalPages" @click="go(page+1)">下一页</button></div>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="showModal=false"><div class="modal-card">
      <div class="modal-header"><h3>{{ editingId ? '编辑门店' : '新增门店' }}</h3><button class="modal-close" @click="showModal=false">✕</button></div>
      <div class="modal-body">
        <div class="form-group"><label>门店名称 *</label><input v-model.trim="form.storeName" /></div>
        <div class="form-row"><div class="form-group"><label>联系电话</label><input v-model.trim="form.storePhone" /></div><div class="form-group"><label>Logo URL</label><input v-model.trim="form.storeLogo" /></div></div>
        <div class="form-row"><div class="form-group"><label>省份</label><input v-model.trim="form.province" /></div><div class="form-group"><label>城市</label><input v-model.trim="form.city" /></div><div class="form-group"><label>区县</label><input v-model.trim="form.district" /></div></div>
        <div class="form-group"><label>详细地址 *</label><input v-model.trim="form.address" /></div>
        <div class="form-group"><label>门店介绍</label><textarea v-model.trim="form.storeDesc" rows="3"></textarea></div>
        <p class="hint">门店资料提交后将重新进入待审核状态</p>
      </div>
      <div class="modal-footer"><button class="btn btn-outline" @click="showModal=false">取消</button><button class="btn btn-primary" :disabled="saving" @click="save">{{ saving ? '保存中...' : '保存' }}</button></div>
    </div></div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue"
import { createMerchantStore, deleteMerchantStore, getMerchantStore, listMerchantStores, updateMerchantStore, type MerchantStorePayload } from "../../api/merchantStore"
import type { Store } from "../../types/store"
import { statusBadge, storeStatusLabel } from "../../utils/status"
import { notify } from "../../utils/notify"

const emptyForm = (): MerchantStorePayload => ({ storeName: "", storeLogo: "", storePhone: "", storeDesc: "", province: "", city: "", district: "", address: "" })
const records = ref<Store[]>([]); const total = ref(0); const page = ref(1); const size = 10; const loading = ref(false)
const keyword = ref(""); const statusFilter = ref(-1); const showModal = ref(false); const editingId = ref(""); const saving = ref(false); const form = ref<MerchantStorePayload>(emptyForm())
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size)))
const fullAddress = (item: Store) => [item.province,item.city,item.district,item.address].filter(Boolean).join("") || "-"

async function fetchData(){loading.value=true;try{const result=await listMerchantStores({current:page.value,size,keyword:keyword.value||undefined,status:statusFilter.value>=0?statusFilter.value:undefined});records.value=result.records;total.value=result.total}finally{loading.value=false}}
function search(){page.value=1;fetchData()} function reset(){keyword.value="";statusFilter.value=-1;search()} function go(next:number){page.value=next;fetchData()}
function openCreate(){editingId.value="";form.value=emptyForm();showModal.value=true}
async function openEdit(id:string){const item=await getMerchantStore(id);editingId.value=id;form.value={storeName:item.storeName,storeLogo:item.storeLogo||"",storePhone:item.storePhone||"",storeDesc:item.storeDesc||"",province:item.province||"",city:item.city||"",district:item.district||"",address:item.address||"",longitude:item.longitude,latitude:item.latitude};showModal.value=true}
async function save(){if(!form.value.storeName||!form.value.address){notify("请填写门店名称和详细地址","error");return}saving.value=true;try{if(editingId.value)await updateMerchantStore(editingId.value,form.value);else await createMerchantStore(form.value);showModal.value=false;notify("保存成功，门店将重新进入待审核状态");await fetchData()}finally{saving.value=false}}
async function remove(item:Store){if(!confirm(`确定删除门店「${item.storeName}」？`))return;await deleteMerchantStore(item.id);notify("删除成功");await fetchData()}
onMounted(fetchData)
</script>

<style scoped>.push-right{margin-left:auto}.modal-card{width:min(620px,100%)}.form-row .form-group{flex:1}.meta-time{display:block;color:var(--text3);margin-top:4px}</style>
