<template>
  <div class="admin-page">
    <div class="page-header"><h2>我的商品</h2><p>维护自己门店的商品、库存与上下架状态</p></div>
    <div class="search-bar"><input v-model="keyword" placeholder="搜索商品名称" @keyup.enter="search" /><select v-model="storeFilter"><option value="">全部门店</option><option v-for="item in stores" :key="item.id" :value="item.id">{{ item.storeName }}</option></select><select v-model.number="statusFilter"><option :value="-1">全部状态</option><option :value="1">上架</option><option :value="0">下架</option><option :value="2">已售出</option></select><button class="btn btn-primary" @click="search">搜索</button><button class="btn btn-outline" @click="reset">重置</button><button class="btn btn-primary push-right" @click="openCreate">+ 新增商品</button></div>
    <div class="table-wrap"><table class="data-table"><thead><tr><th>商品</th><th>门店</th><th>价格</th><th>库存</th><th>状态</th><th>操作</th></tr></thead><tbody>
      <tr v-for="item in records" :key="item.id"><td>{{ item.productName || item.name }}</td><td>{{ item.store?.storeName || storeName(item.storeId) }}</td><td>¥{{ item.price }}</td><td>{{ item.stock }}</td><td><span class="badge" :class="statusBadge(productStatusCode(item.status,item.statusCode))">{{ productStatusLabel(item.status,item.statusCode) }}</span></td><td class="actions"><button class="btn btn-outline btn-sm" @click="openEdit(item.id)">编辑</button><button class="btn btn-sm" :class="productStatusCode(item.status,item.statusCode)===1?'btn-warning':'btn-success'" @click="toggle(item)">{{ productStatusCode(item.status,item.statusCode)===1?'下架':'上架' }}</button><button class="btn btn-danger btn-sm" @click="remove(item)">删除</button></td></tr>
      <tr v-if="!loading&&!records.length"><td colspan="6" class="empty-row">暂无商品</td></tr><tr v-if="loading"><td colspan="6" class="empty-row">加载中...</td></tr>
    </tbody></table><div class="pagination" v-if="total"><button :disabled="page<=1" @click="go(page-1)">上一页</button><span>第 {{ page }} / {{ totalPages }} 页，共 {{ total }} 条</span><button :disabled="page>=totalPages" @click="go(page+1)">下一页</button></div></div>
    <div v-if="showModal" class="modal-overlay" @click.self="showModal=false"><div class="modal-card"><div class="modal-header"><h3>{{ editingId?'编辑商品':'新增商品' }}</h3><button class="modal-close" @click="showModal=false">✕</button></div><div class="modal-body">
      <div class="form-group"><label>所属门店 *</label><select v-model="form.storeId"><option value="" disabled>请选择自己的门店</option><option v-for="item in stores" :key="item.id" :value="item.id">{{ item.storeName }}</option></select></div>
      <div class="form-group"><label>商品名称 *</label><input v-model.trim="form.productName" /></div><div class="form-row"><div class="form-group"><label>价格 *</label><input v-model.number="form.price" type="number" min="0" step="0.01" /></div><div class="form-group"><label>库存 *</label><input v-model.number="form.stock" type="number" min="0" /></div></div>
      <div class="form-row"><div class="form-group"><label>商品类型</label><select v-model.number="form.productType"><option :value="1">宠物</option><option :value="2">周边</option></select></div><div class="form-group"><label>状态</label><select v-model="form.status"><option value="1">上架</option><option value="0">下架</option></select></div></div>
      <div class="form-group"><label>分类</label><input v-model.trim="form.category" /></div><div class="form-group"><label>主图 URL</label><input v-model.trim="form.mainImage" /></div><div class="form-group"><label>商品描述</label><textarea v-model.trim="form.productDesc" rows="3"></textarea></div>
    </div><div class="modal-footer"><button class="btn btn-outline" @click="showModal=false">取消</button><button class="btn btn-primary" :disabled="saving" @click="save">{{ saving?'保存中...':'保存' }}</button></div></div></div>
  </div>
</template>

<script setup lang="ts">
import { computed,onMounted,ref } from "vue"
import { createMerchantProduct,deleteMerchantProduct,getMerchantProduct,listMerchantProducts,offlineMerchantProduct,onlineMerchantProduct,updateMerchantProduct,type MerchantProductPayload } from "../../api/merchantProduct"
import { listMerchantStores } from "../../api/merchantStore"
import type { Product } from "../../types/product";import type { Store } from "../../types/store"
import { productStatusCode,productStatusLabel,statusBadge } from "../../utils/status";import { notify } from "../../utils/notify"

const emptyForm=():MerchantProductPayload=>({storeId:"",productName:"",productType:1,category:"",productDesc:"",price:0,stock:0,mainImage:"",status:"1"})
const records=ref<Product[]>([]),stores=ref<Store[]>([]),total=ref(0),page=ref(1),keyword=ref(""),storeFilter=ref(""),statusFilter=ref(-1),loading=ref(false),showModal=ref(false),editingId=ref(""),saving=ref(false);const size=10;const form=ref<MerchantProductPayload>(emptyForm());const totalPages=computed(()=>Math.max(1,Math.ceil(total.value/size)))
const storeName=(id:string)=>stores.value.find(item=>item.id===id)?.storeName||"-"
async function loadStores(){const result=await listMerchantStores({current:1,size:100});stores.value=result.records}
async function fetchData(){loading.value=true;try{const result=await listMerchantProducts({page:page.value,size,keyword:keyword.value||undefined,storeId:storeFilter.value||undefined,status:statusFilter.value>=0?statusFilter.value:undefined});records.value=result.records;total.value=result.total}finally{loading.value=false}}
function search(){page.value=1;fetchData()}function reset(){keyword.value="";storeFilter.value="";statusFilter.value=-1;search()}function go(next:number){page.value=next;fetchData()}function openCreate(){editingId.value="";form.value=emptyForm();showModal.value=true}
async function openEdit(id:string){const item=await getMerchantProduct(id);editingId.value=id;form.value={storeId:item.storeId,productName:item.productName||item.name||"",productType:item.productType||1,category:item.category||"",productDesc:item.productDesc||item.detail||"",price:Number(item.price),stock:item.stock,mainImage:item.mainImage||item.image||"",images:item.images,status:productStatusCode(item.status,item.statusCode)===1?"1":"0"};showModal.value=true}
function valid(){return Boolean(form.value.storeId&&form.value.productName&&Number.isFinite(form.value.price)&&form.value.price>=0&&Number.isInteger(form.value.stock)&&form.value.stock>=0&&["0","1"].includes(form.value.status||""))}
async function save(){if(!valid()){notify("请检查门店、名称、价格、库存和状态","error");return}saving.value=true;try{if(editingId.value)await updateMerchantProduct(editingId.value,form.value);else await createMerchantProduct(form.value);showModal.value=false;notify("保存成功");await fetchData()}finally{saving.value=false}}
async function toggle(item:Product){const online=productStatusCode(item.status,item.statusCode)===1;if(online)await offlineMerchantProduct(item.id);else await onlineMerchantProduct(item.id);notify(online?"下架成功":"上架成功");await fetchData()}
async function remove(item:Product){if(!confirm(`确定删除商品「${item.productName||item.name}」？`))return;await deleteMerchantProduct(item.id);notify("删除成功");await fetchData()}
onMounted(async()=>{await loadStores();await fetchData()})
</script>
<style scoped>.push-right{margin-left:auto}.modal-card{width:min(600px,100%)}.form-row .form-group{flex:1}</style>
