import { FileBlob, PresentationFile } from "@oai/artifact-tool";

async function main() {
  const p = await PresentationFile.importPptx(await FileBlob.load("D:/厦理/大四/课程/PPT模板-邓已填充.pptx"));
  const slide = p.slides.items[14]; // slide 15 (0-indexed)
  console.log("Slide 15 shapes:", slide.shapes.items.length);
  for (const s of slide.shapes.items.slice(0, 10)) {
    console.log("name:", s.name, "type:", typeof s.text);
    if (s.text) {
      console.log("  text.plainText:", JSON.stringify(s.text.plainText?.substring(0,80)));
      console.log("  text keys:", Object.keys(s.text));
      if (s.text.paragraphs) console.log("  paragraphs:", s.text.paragraphs.length);
    }
  }
}
main().catch(e => console.error(e));