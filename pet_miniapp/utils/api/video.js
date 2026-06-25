var request = require("../request");
var mock = require("../data/mock");
module.exports = {
  list: function(page, size) {
    return request.get("/api/video/feed", { page: page || 1, size: size || 10 })
      .catch(function() { return mock.videos; });
  },
  detail: function(id) {
    return request.get("/api/video/" + id)
      .catch(function() { return mock.videoDetail(id); });
  },
  like: function(id) {
    return request.post("/api/video/" + id + "/like")
      .catch(function() { return null; });
  },
  comments: function(id) {
    return request.get("/api/video/" + id + "/comments")
      .catch(function() { return mock.comments; });
  },
  addComment: function(id, text) {
    return request.post("/api/video/" + id + "/comment", { content: text, userId: 1 })
      .catch(function() { return null; });
  }
};
