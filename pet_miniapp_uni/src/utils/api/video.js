import request from "../request";
export default {
  list(page, size) { return request.get("/api/video/feed", { page: page || 1, size: size || 10 }); },
  detail(id) { return request.get("/api/video/" + id); },
  like(id) { return request.post("/api/video/" + id + "/like"); },
  comments(id) { return request.get("/api/video/" + id + "/comments"); },
  addComment(id, text) { return request.post("/api/video/" + id + "/comment", { content: text }); }
};
