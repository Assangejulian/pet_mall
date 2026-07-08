import { FileBlob, PresentationFile } from "@oai/artifact-tool";
import { writeFile, mkdir } from "fs/promises";

async function writeBlob(path, blob) {
  await writeFile(path, new Uint8Array(await blob.arrayBuffer()));
}

async function main() {
  await mkdir("D:/厦理/大四/课程/tmp_slides/final_preview", {recursive:true});
  const p = await PresentationFile.importPptx(await FileBlob.load("D:/厦理/大四/课程/PPT-邓-演讲大纲.pptx"));
  console.log("Total slides:", p.slides.items.length);
  for (const [i, slide] of p.slides.items.entries()) {
    const png = await p.export({slide, format:"png", scale:1});
    await writeBlob("D:/厦理/大四/课程/tmp_slides/final_preview/slide-" + String(i+1).padStart(2,"0") + ".png", png);
    const layout = await slide.export({format:"layout"});
    await writeFile("D:/厦理/大四/课程/tmp_slides/final_preview/slide-" + String(i+1).padStart(2,"0") + ".json", await layout.text());
  }
  console.log("Done exporting previews");
}
main().catch(e => console.error(e));