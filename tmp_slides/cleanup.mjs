import { FileBlob, PresentationFile } from "@oai/artifact-tool";
async function main() {
  const p = await PresentationFile.importPptx(await FileBlob.load("D:/厦理/大四/课程/PPT-邓-演讲大纲.pptx"));

  // Find ALL shapes containing "请填写" and clear/replace them
  let cleared = 0;
  for (const slide of p.slides.items) {
    for (const shape of slide.shapes.items) {
      const t = String(shape.text);
      if (t.includes("请填写") || t.includes("请放入") || t.includes("请连接") || t.includes("请替换") || t.includes("请补充") || t.includes("可自行替换")) {
        // Try to set reasonable fallback text based on context
        if (t.includes("填写提示")) continue; // skip hint labels
        if (t.includes("页面截图") || t.includes("请放入") || t.includes("请替换")) {
          shape.text = "（此处可插入页面截图或演示效果）";
        } else if (t.includes("请连接") || t.includes("请补充")) {
          shape.text = ""; // remove connector hints
        } else if (t.includes("可自行替换")) {
          shape.text = ""; // remove self-replace hints
        } else if (t.includes("：】") || t.includes("：【请填写】")) {
          shape.text = t.replace(/【请填写】/g, "").replace(/[：:【】]/g, "").trim() || "";
        } else {
          shape.text = t.replace(/【请填写[^】]*】/g, "").replace(/【[^】]*】/g, "").trim();
        }
        cleared++;
      }
    }
  }
  console.log("Cleared " + cleared + " shapes");

  const pptx = await PresentationFile.exportPptx(p);
  await pptx.save("D:/厦理/大四/课程/PPT-邓-演讲大纲.pptx");
  console.log("Done");
}
main().catch(e=>{console.error(e);process.exit(1);});