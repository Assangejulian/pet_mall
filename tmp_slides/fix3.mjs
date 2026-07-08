import { FileBlob, PresentationFile } from "@oai/artifact-tool";

async function main() {
  const p = await PresentationFile.importPptx(await FileBlob.load("D:/厦理/大四/课程/PPT-邓-演讲大纲.pptx"));

  let applied = 0;

  // Slide 19: fix "为页面截图】"
  const s19 = p.slides.items[18];
  for (const shape of s19.shapes.items) {
    const t = String(shape.text);
    if (t.includes("为页面截图")) { shape.text = "miniapp/pages/user + subpages/login/address/profile"; applied++; break; }
  }

  // Slide 20: fix checklist items that still have 】placeholder
  const fix20 = {
    "\u767B\u5F55\uFF1A": "\u767B\u5F55\uFF1A\u2705 5\u79CD\u65B9\u5F0F|JWT|\u7BA1\u7406\u7AEF\u5206\u79BB",
    "\u8D44\u6599\uFF1A": "\u8D44\u6599\uFF1A\u2705 \u67E5\u770B/\u4FEE\u6539|\u4F1A\u5458\u7B49\u7EA7\u52A8\u6001\u8BA1\u7B97",
    "\u5730\u5740\uFF1A": "\u5730\u5740\uFF1A\u2705 CRUD|\u9ED8\u8BA4\u5730\u5740|user_id\u9694\u79BB",
    "\u6743\u9650\uFF1A": "\u6743\u9650\uFF1A\u2705 \u53CC\u62E6\u622A\u5668|\u89D2\u8272\u5206\u5C42|SQL\u515C\u5E95",
    "\u4E0B\u6E38\u652F\u6491": "\u4E0B\u6E38\u652F\u6491\uFF1A\u8D2D\u7269\u8F66(user_id)\u2192\u8BA2\u5355(\u5730\u5740\u5173\u8054)\u2192\u652F\u4ED8(\u8EAB\u4EFD\u6821\u9A8C)",
  };

  const s20 = p.slides.items[19];
  const skipIds = new Set(); // skip shapes already containing ✅
  for (const shape of s20.shapes.items) {
    if (String(shape.text).includes("\u2705")) skipIds.add(shape);
  }

  for (const [search, replace] of Object.entries(fix20)) {
    let found = false;
    for (const shape of s20.shapes.items) {
      if (skipIds.has(shape)) continue;
      const t = String(shape.text);
      if (t.includes(search) && t.includes("\u3011")) { // includes closing bracket = broken placeholder
        shape.text = replace;
        applied++;
        found = true;
        break;
      }
    }
    if (!found) console.log("MISS S20: " + search);
  }

  console.log("Applied " + applied + " more fixes");
  const outPath = "D:/厦理/大四/课程/PPT-邓-演讲大纲.pptx";
  const pptx = await PresentationFile.exportPptx(p);
  await pptx.save(outPath);
  console.log("Saved to " + outPath);
}
main().catch(e => { console.error(e); process.exit(1); });