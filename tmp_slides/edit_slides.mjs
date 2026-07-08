import { FileBlob, PresentationFile } from "@oai/artifact-tool";
import { writeFile } from "fs/promises";

async function writeBlob(path, blob) {
  await writeFile(path, new Uint8Array(await blob.arrayBuffer()));
}

async function main() {
  const starterPath = "D:/厦理/大四/课程/tmp_slides/template-starter.pptx";
  const presentation = await PresentationFile.importPptx(await FileBlob.load(starterPath));

  const edits = {
    "sh/cfa9w3m9": "接口规范与权限设计",
    "sh/1032lszq": "统一 Result 结构、BaseController 通用 CRUD、双拦截器鉴权体系",
    "sh/q1orytsv": "统一返回结构 Result(code/message/data)，code=200 成功\nBaseController 提供 10 个通用 CRUD 端点\nAuthInterceptor 解析 JWT 存 UserHolder\nRoleInterceptor 按角色分层拦截：admin/merchant/user",
    "sh/8z25kb6x": "接口与权限分层架构",
    "sh/4vmtwra5": "请求 \u2192 AuthInterceptor(\u9A8C\u8BC1) \u2192 RoleInterceptor(\u6821\u9A8C) \u2192 Controller(\u8FC7\u6EE4)",
    "sh/tgfmlkjm": "/api/admin(admin) | /api/merchant(merchant) | /api/order(auth) | \u5176\u4F59\u516C\u5F00",

    "sh/8v6psfil": "\u7528\u6237\u767B\u5F55\u4E0E\u6CE8\u518C\u5B9E\u73B0",
    "sh/xczed8v6": "\u591A\u7B56\u7565\u8BA4\u8BC1\u4F53\u7CFB\uFF1A5 \u79CD\u767B\u5F55\u65B9\u5F0F\uFF0CJWT \u9274\u6743\uFF0C\u5BC6\u7801 BCrypt \u52A0\u76D0\u54C8\u5E0C",
    "sh/2x4nap4r": "\u5BC6\u7801\u767B\u5F55\uFF1A\u7528\u6237\u540D/\u624B\u673A\u53F7 + \u5BC6\u7801\uFF0CPasswordEncoder \u52A0\u76D0\u54C8\u5E0C\n\u90AE\u7BB1\u9A8C\u8BC1\u7801\uFF1A6\u4F4D\u7801\u2192Redis(5min)\u2192\u90AE\u4EF6\u53D1\u9001\n\u77ED\u4FE1\u767B\u5F55\uFF1A\u624B\u673A\u53F7+\u9A8C\u8BC1\u7801\uFF0C60\u79D2\u5012\u8BA1\u65F6\n\u5FAE\u4FE1\u767B\u5F55\uFF1Awx.login()\u83B7\u53D6code\u2192\u540E\u7AEF\u6362openid\n\u4EBA\u8138\u767B\u5F55\uFF1AFaceAuthService\u4EBA\u8138\u8BC6\u522B\u6821\u9A8C\n\u7BA1\u7406\u7AEF\uFF1A/api/admin/login\u5F3A\u5236\u5BC6\u7801",

    "sh/udg7adsj": "\u7528\u6237\u8D44\u6599\u4E0E\u5730\u5740\u7BA1\u7406",
    "sh/nyd0z610": "GET/PUT /api/user/profile\u8D44\u6599\uFF1B\u5730\u5740CRUD+user_id\u6570\u636E\u9694\u79BB",
    "sh/knih4byh": "\u4E2A\u4EBA\u8D44\u6599\uFF1A\u67E5\u770B/\u4FEE\u6539\u6635\u79F0\u3001\u5934\u50CF\u3001\u624B\u673A\u3001\u90AE\u7BB1\u3001\u751F\u65E5\n\u6536\u8D27\u5730\u5740\uFF1A\u65B0\u589E/\u7F16\u8F91/\u5220\u9664\uFF0C\u81EA\u52A8\u6CE8\u5165userId\n\u9ED8\u8BA4\u5730\u5740\uFF1A\u8BBE\u7F6E\u65B0\u9ED8\u8BA4\u524D\u5148\u53D6\u6D88\u65E7\u9ED8\u8BA4\n\u5B89\u5168\u6821\u9A8C\uFF1A\u6BCF\u6B21\u64CD\u4F5C\u524D\u67E5\u5E93\u9A8C\u8BC1user_id\u5F52\u5C5E",
    "sh/29gr2p8n": "\u5730\u5740\u5B57\u6BB5\u4E0E\u63A5\u53E3\u8868",
    "sh/vixczed8": "receiver\uFF1A\u6536\u8D27\u4EBA\u59D3\u540D",
    "sh/3itgfmlk": "phone\uFF1A\u8054\u7CFB\u7535\u8BDD",
    "sh/e9cvixcz": "province/city\uFF1A\u7701\u5E02\u533A",
    "sh/lwval87e": "defaulted\uFF1A\u662F\u5426\u9ED8\u8BA4(0/1)",
    "sh/ahoza1gv": "\u63A5\u53E3\uFF1APOST/GET/PUT/DELETE /api/user/address",

    "sh/hk3q5o7q": "\u9274\u6743\u4E0E\u89D2\u8272\u63A7\u5236",
    "sh/oza1gvy9": "AuthInterceptor+RoleInterceptor\u53CC\u62E6\u622A\u5668\uFF0CThreadLocal\u7EBF\u7A0B\u9694\u79BB",
    "sh/d0z61032": "AuthInterceptor\uFF1A\u63D0\u53D6Bearertoken\u2192JWT\u89E3\u6790\u2192\u5B58UserHolder\nRoleInterceptor\uFF1A\u67E5role\u2208allowedRoles\u2192\u653E\u884C/403\nUserHolder(ThreadLocal)\uFF1A\u8BF7\u6C42\u5185\u4EFB\u610F\u5C42\u53D6userId/username/role\n\u5F02\u5E38\u5904\u7406\uFF1A401\u672A\u767B\u5F55\u3001403\u65E0\u6743\u9650\u3001token\u8FC7\u671F\u81EA\u52A8\u62E6\u622A",
    "sh/v6psfil0": "\u9274\u6743\u6D41\u7A0B\u56FE",
    "sh/nqxs72x4": "\u89D2\u8272\u8FB9\u754C\uFF1Aadmin\u5168\u91CF|merchant\u81EA\u5BB6\u5E97\u94FA|user:user_id\u8FC7\u6EE4",

    "sh/6psfil0r": "\u524D\u7AEF\u4E2A\u4EBA\u4E2D\u5FC3\u5BF9\u63A5",
    "sh/val87ed8": "\u5C0F\u7A0B\u5E8F5\u54081\u767B\u5F55\u9875+\u4E2A\u4EBA\u4E2D\u5FC3+\u5730\u5740\u7BA1\u7406",
    "sh/u18jqh0f": "\u5173\u952E\u9875\u9762\u4E0E\u6587\u4EF6",
    "sh/k3q5o7qp": "\u4ECE Mock \u5207\u6362\u5230\u771F\u5B9EAPI\uFF0C\u6539\u52A8\u96C6\u4E2D\u5728utils/api/\u5C42",

    "sh/a1gvy90j": "\u8054\u8C03\u95EE\u9898\u4E0E\u6210\u679C",
    "sh/zidonyd0": "\u56DB\u5927\u6A21\u5757\u5168\u90E8\u5B8C\u6210\uFF0C\u652F\u6491\u8D2D\u7269\u8F66/\u8BA2\u5355/\u89C6\u9891/\u652F\u4ED8\u7B49\u4E0B\u6E38\u6D41\u7A0B",
    "sh/kzupwb2h": "\u767B\u5F55\uFF1A5\u79CD\u7B56\u7565\u5168\u8986\u76D6\uFF0CJWT\u8BA4\u8BC1\uFF0Ctoken\u5B58\u50A8\u4E0E\u5237\u65B0\n\u8D44\u6599\uFF1AGET/PUT profile\uFF0C\u542B\u4F1A\u5458\u7B49\u7EA7\u4E0E\u6298\u6263\u8BF4\u660E\n\u5730\u5740\uFF1A\u5B8C\u6574CRUD+\u9ED8\u8BA4\u5730\u5740+user_id\u5F52\u5C5E\u6821\u9A8C\n\u6743\u9650\uFF1A\u53CC\u62E6\u622A\u5668\u5206\u5C42\uFF0CSQL\u5C42user_id\u8FC7\u6EE4\u515C\u5E95",
    "sh/25kb6xkf": "\u5B8C\u6210\u60C5\u51B5\u6E05\u5355",
    "sh/r2p8nqxs": "\u767B\u5F55\uFF1A\u2705 5\u79CD\u65B9\u5F0F|JWT|\u7BA1\u7406\u7AEF\u5206\u79BB",
    "sh/fmlkjm1c": "\u8D44\u6599\uFF1A\u2705 \u67E5\u770B/\u4FEE\u6539|\u4F1A\u5458\u7B49\u7EA7\u52A8\u6001\u8BA1\u7B97",
    "sh/qtcj6tov": "\u5730\u5740\uFF1A\u2705 CRUD|\u9ED8\u8BA4\u5730\u5740|user_id\u9694\u79BB",
    "sh/1gvy90je": "\u6743\u9650\uFF1A\u2705 \u53CC\u62E6\u622A\u5668|\u89D2\u8272\u5206\u5C42|SQL\u515C\u5E95",
    "sh/qpg3ex0n": "\u4E0B\u6E38\u652F\u6491\uFF1A\u8D2D\u7269\u8F66(user_id)\u2192\u8BA2\u5355(\u5730\u5740\u5173\u8054)\u2192\u652F\u4ED8(\u8EAB\u4EFD\u6821\u9A8C)",
  };

  let applied = 0;
  for (const [id, newText] of Object.entries(edits)) {
    try {
      const target = presentation.resolve(id);
      target.text.replace(target.text.plainText, newText);
      applied++;
    } catch(e) {
      console.log("skip " + id + ": " + (e.message || e).toString().substring(0, 80));
    }
  }
  console.log("Applied " + applied + " / " + Object.keys(edits).length + " edits");

  const outPath = "D:/厦理/大四/课程/PPT-邓-演讲大纲.pptx";
  const pptx = await PresentationFile.exportPptx(presentation);
  await pptx.save(outPath);
  console.log("Saved to " + outPath);
}

main().catch(e => { console.error(e); process.exit(1); });