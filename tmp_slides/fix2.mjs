import { FileBlob, PresentationFile } from "@oai/artifact-tool";

async function main() {
  const p = await PresentationFile.importPptx(await FileBlob.load("D:/厦理/大四/课程/PPT-邓-演讲大纲.pptx"));

  const fixes = [
    [19, "为页面截图", "miniapp/pages/user + subpages/login + subpages/address"],
    [19, "个人中心/地址管理截图", "关键页面与文件"],
    [20, "四大模块全部完成", "四大模块全部完成，支撑购物车/订单/视频/支付等下游"],
    [20, "登录：", "登录：\u2705 5种方式|JWT|管理端分离"],
    [20, "资料：", "资料：\u2705 查看/修改|会员等级动态计算"],
    [20, "地址：", "地址：\u2705 CRUD|默认地址|user_id隔离"],
    [20, "权限：", "权限：\u2705 双拦截器|角色分层|SQL兜底"],
    [20, "支撑下游", "下游支撑：购物车(user_id)\u2192订单(地址关联)\u2192支付(身份校验)"],
  ];

  // For slide 20, skip shapes that already have the ✅ content
  let applied = 0;
  for (const [slideNum, searchText, newText] of fixes) {
    const slide = p.slides.items[slideNum - 1];
    let matched = null;
    for (const shape of slide.shapes.items) {
      const t = String(shape.text);
      // For slide 20 checklist items, skip if shape already has ✅
      if (slideNum === 20 && t.includes("\u2705")) continue;
      if (t.includes(searchText)) {
        matched = shape;
        break;
      }
    }
    if (matched) {
      matched.text = newText;
      applied++;
    } else {
      console.log("MISS S" + slideNum + ": " + searchText.substring(0,30));
    }
  }
  console.log("Applied " + applied + "/" + fixes.length);

  const outPath = "D:/厦理/大四/课程/PPT-邓-演讲大纲.pptx";
  const pptx = await PresentationFile.exportPptx(p);
  await pptx.save(outPath);
  console.log("Saved to " + outPath);
}
main().catch(e => { console.error(e); process.exit(1); });