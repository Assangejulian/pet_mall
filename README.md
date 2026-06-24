feat(video): 完成视频模块后端核心基建与系统框架修复
【新特性 (New Features)】
1. 创建了视频模块底层数据库脚本 `video.sql`。
2. 搭建了完整的 Video 模块 MVC 结构代码：
   - 实体类 `Video.java`：配置 MyBatis-Plus `@TableLogic` 实现软删除。
   - 数据访问层 `VideoMapper.java`。
   - 业务逻辑层 `IVideoService` 与 `VideoServiceImpl`：增加了 `incrementPlayCount` 核心统计逻辑。
   - 控制器 `VideoController.java`：提供完善的 Restful CRUD，及基于本地文件目录存储的视频上传接口 (`/upload`)。
【Bug 修复与项目调试 (Bug Fixes & Tweaks)】
1. 修复了全局 `AdminAuthInterceptor.java` 中因双引号转义不当导致的编译致命错误。
2. 修复了主启动类 `DemoApplication.java`：加入 `@MapperScan("com.pat.**.mapper")`，解决 UserMapper 及 VideoMapper 无法被注入的问题。
3. 将 `pom.xml` 中 `spring-boot-starter-parent` 的版本由异常的 4.1.0 降级为稳定的 3.3.0，成功解决了 MyBatis-Plus 3.5.7 内部因兼容性抛出 `NoSuchMethodError` 从而导致后端框架崩溃的严重问题。
【验证状态 (Test Status)】
- 编译状态：`mvn clean compile` 编译通过。
- 启动状态：Spring Boot Tomcat Web 服务已在本地 8080 端口稳定运行。
- 接口验证：`/api/video/list` 分页列表接口返回 200 HTTP Status 验证正常。
- 
