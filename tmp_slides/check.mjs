import { FileBlob, PresentationFile } from "@oai/artifact-tool";

async function main() {
  const p = await PresentationFile.importPptx(await FileBlob.load("D:/厦理/大四/课程/PPT模板-邓已填充.pptx"));
  
  console.log("Total slides:", p.slides.items.length);
  
  // Check what slide APIs are available
  const slide = p.slides.items.find(s => {
    try { return s.shapes && s.shapes.items && s.shapes.items.length > 0; }
    catch(e) { return false; }
  });
  if (slide) {
    console.log("Sample slide shapes count:", slide.shapes.items.length);
    for (const s of slide.shapes.items.slice(0, 5)) {
      console.log("  shape:", s.name, "text:", (s.text?.plainText || s.text || "").substring(0,40));
    }
  }
  
  // Try shapes.forEach
  let count = 0;
  for (const sl of p.slides.items) {
    for (const sh of sl.shapes.items) {
      const t = sh.text?.plainText || "";
      if (t.includes("邓")) { count++; break; }
    }
  }
  console.log("Slides with '邓':", count);
}
main().catch(e => console.error(e));