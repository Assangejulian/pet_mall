var request = require("../request");
module.exports = {
  list: function(page, size) {
    return request.get("/api/video/search", { page: page || 1, size: size || 10 });
  },
  detail: function(id) {
    return request.get("/api/video/" + id);
  },
  like: function(id) {
    return request.post("/api/video/" + id + "/like");
  },
  comments: function(id) {
    return request.get("/api/video/" + id + "/comments");
  },
  addComment: function(id, text) {
    return request.post("/api/video/" + id + "/comment", { content: text });
  }
};
