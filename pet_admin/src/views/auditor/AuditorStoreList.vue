<template>
  <div class="admin-page">
    <div class="page-header"><h2>门店审核</h2><p>查看经营资料并执行审核或关闭操作</p></div>
    <div class="search-bar"><input v-model="keyword" placeholder="搜索门店名称" @keyup.enter="search" /><select v-model.number="statusFilter"><option :value="-1">全部状态</option><option :value="0">待审核</option><option :value="1">营业中</option><option :value="2">已关闭</option></select><button class="btn btn-primary" @click="search">搜索</button><button class="btn btn-outline" @click="reset">重置</button></div>
    <div class="table-wrap"><table class="data-table"><thead><tr><th>门店</th><th>店主ID</th><th>电话</th><th>地址</th><th>商品数</th><th>状态</th><th>审核操作</th></tr></thead><tbody>
      <tr v-for="item in records" :key="item.id"><td>{{ item.storeName }}</td><td>{{ item.userId }}</td><td>{{ item.storePhone||'-' }}</td><td>{{ fullAddress(item) }}</td><td>{{ item.productCount??0 }}</td><td><span class="badge" :class="statusBadge(item.status)">{{ storeStatusLabel(item.status) }}</span></td><td class="actions"><button class="btn btn-outline btn-sm" @click="showDetail(item.id)">详情</button><button v-if="item.status===0" class="btn btn-success btn-sm" @click="audit(item,'approve')">通过</button><button v-if="item.status===0" class="btn btn-danger btn-sm" @click="audit(item,'reject')">拒绝</button><button v-if="item.status===1" class="btn btn-warning btn-sm" @click="audit(item,'close')">关闭</button></td></tr>
      <tr v-if="!loading&&!records.length"><td colspan="7" class="empty-row">暂无门店</td></tr><tr v-if="loading"><td colspan="7" class="empty-row">加载中...</td></tr>
    </tbody></table><div class="pagination" v-if="total"><button :disabled="page<=1" @click="go(page-1)">上一页</button><span>第 {{ page }} / {{ totalPages }} 页，共 {{ total }} 条</span><button :disabled="page>=totalPages" @click="go(page+1)">下一页</button></div></div>
    <div v-if="detail" class="modal-overlay" @click.self="detail=null"><div class="modal-card"><div class="modal-header"><h3>门店详情</h3><button class="modal-close" @click="detail=null">✕</button></div><div class="detail-list"><p><b>门店：</b>{{ detail.storeName }}</p><p><b>店主ID：</b>{{ detail.userId }}</p><p><b>电话：</b>{{ detail.storePhone||'-' }}</p><p><b>地址：</b>{{ fullAddress(detail) }}</p><p><b>介绍：</b>{{ detail.storeDesc||'-' }}</p><p><b>状态：</b>{{ storeStatusLabel(detail.status) }}</p></div></div></div>
  </div>
</template>

<script setup lang="ts">
import { computed,onMounted,ref } from "vue";import { approveStore,closeStore,getAuditorStore,listAuditorStores,rejectStore } from "../../api/auditorStore";import type { Store } from "../../types/store";import { statusBadge,storeStatusLabel } from "../../utils/status";import { notify } from "../../utils/notify"
const records=ref<Store[]>([]),detail=ref<Store|null>(null),total=ref(0),page=ref(1),keyword=ref(""),statusFilter=ref(0),loading=ref(false);const size=10;const totalPages=computed(()=>Math.max(1,Math.ceil(total.value/size)));const fullAddress=(item:Store)=>[item.province,item.city,item.district,item.address].filter(Boolean).join("")||"-"
async function fetchData(){loading.value=true;try{const result=await listAuditorStores({current:page.value,size,keyword:keyword.value||undefined,status:statusFilter.value>=0?statusFilter.value:undefined});records.value=result.records;total.value=result.total}finally{loading.value=false}}
function search(){page.value=1;fetchData()}function reset(){keyword.value="";statusFilter.value=0;search()}function go(next:number){page.value=next;fetchData()}async function showDetail(id:string){detail.value=await getAuditorStore(id)}
async function audit(item:Store,action:"approve"|"reject"|"close"){const labels={approve:"审核通过",reject:"审核拒绝",close:"关闭门店"};if(!confirm(`确定${labels[action]}「${item.storeName}」？`))return;const reason=prompt("处理原因（选填）：")||undefined;if(action==="approve")await approveStore(item.id,reason);else if(action==="reject")await rejectStore(item.id,reason);else await closeStore(item.id,reason);notify(`${labels[action]}成功`);await fetchData()}
onMounted(fetchData)
</script>
<style scoped>.detail-list{display:grid;gap:12px;color:var(--text2)}.detail-list b{color:var(--text1)}</style>
