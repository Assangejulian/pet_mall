import { FileBlob, PresentationFile } from "@oai/artifact-tool";
async function main() {
  const p = await PresentationFile.importPptx(await FileBlob.load("D:/厦理/大四/课程/PPT-邓-演讲大纲.pptx"));
  const fixes = [
    [30,"库存：【请填写】","库存：数据库行锁防超卖，失败提示"],
    [30,"金额：【请填写】","金额：DECIMAL(10,2)精度，防浮点误差"],
    [30,"权限：【请填写】","权限：user_id/store_id归属校验拦截"],
    [30,"状态：【请填写】","状态：状态机校验，非法跳转拒绝"],
    [20,"登录：【请填写】","登录：\u2705 5种方式|JWT|管理端分离"],
    [20,"资料：【请填写】","资料：\u2705 查看/修改|会员等级动态计算"],
    [20,"地址：【请填写】","地址：\u2705 CRUD|默认地址|user_id隔离"],
    [20,"权限：【请填写】","权限：\u2705 双拦截器|角色分层|SQL兜底"],
    [8,"请填写：功能模块表","功能模块清单"],
    [22,"建议说明宠物/周边","ProductController：商品CRUD+上下架，product_type(1宠物2周边)，多图JSON\n多条件筛选：store_id+product_type+category+status+keyword\n商家端：MerchantProductController，requireOwnedProduct()校验归属\n审核端：AuditorProductController，管理员审核上下架"],
  ];
  let ok = 0;
  for (const [sn, search, replace] of fixes) {
    const slide = p.slides.items[sn - 1];
    for (const shape of slide.shapes.items) {
      if (String(shape.text).includes(search)) { shape.text = replace; ok++; break; }
    }
  }
  console.log("Fixed " + ok);
  const pptx = await PresentationFile.exportPptx(p);
  await pptx.save("D:/厦理/大四/课程/PPT-邓-演讲大纲.pptx");
  console.log("Done");
}
main().catch(e=>{console.error(e);process.exit(1);});