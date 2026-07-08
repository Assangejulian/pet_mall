import { FileBlob, PresentationFile } from "@oai/artifact-tool";

async function main() {
  const p = await PresentationFile.importPptx(await FileBlob.load("D:/厦理/大四/课程/PPT-邓-演讲大纲.pptx"));
  for (const slide of p.slides.items) {
    const layout = await slide.export({format:"layout"});
    const data = JSON.parse(await layout.text());
    if (!data.elements) continue;
    const sn = data.slide?.id?.replace("sl/","")?.substring(0,6) || "?";
    let sel = -1;
    try { sel = JSON.parse(data.slide?.id?.split("/")[1] || "0"); } catch(e){}
    if (sel < 1 || sel > 6) continue;
    console.log("--- Slide " + sel + " ---");
    for (const el of data.elements) {
      const name = el.name || el.id || "?";
      const texts = [];
      function collectText(e) {
        if (e.text) texts.push(e.text.substring(0,100));
        if (e.children) for (const c of e.children) collectText(c);
      }
      collectText(el);
      if (texts.length > 0) console.log("  [" + name + "] " + texts.join(" "));
    }
  }
}
main().catch(e => console.error(e));