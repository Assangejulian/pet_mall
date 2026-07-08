import { FileBlob, PresentationFile } from "@oai/artifact-tool";

async function main() {
  const p = await PresentationFile.importPptx(await FileBlob.load("D:/厦理/大四/课程/PPT模板-邓已填充.pptx"));
  const slide = p.slides.items[14];
  const s5 = slide.shapes.items[5]; // 矩形5
  console.log("proto:", Object.getPrototypeOf(s5.text).constructor.name);
  console.log("own props:", Object.getOwnPropertyNames(s5.text));
  
  // Try to read text
  console.log("toString:", String(s5.text));
  console.log("valueOf:", s5.text.valueOf());
  
  // Try getting paragraphs
  try {
    const pp = s5.text.paragraphs;
    console.log("paragraphs type:", typeof pp, pp?.length);
  } catch(e) { console.log("err:", e.message); }
  
  // Try accessing runs
  try {
    console.log("plainText:", s5.text.plainText);
  } catch(e) { console.log("plainText err:", e.message); }
}
main().catch(e => console.error(e));