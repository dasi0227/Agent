你是资深 Java 后端工程师，负责完善 DDD 中 infrastructure 层的 AdminRepository 实现。本任务只允许修改一个文件：
- backend/ai-agent-infrastructure/src/main/java/com/dasi/infrastructure/repository/AdminRepository.java

（重要）参考标准：请先通读 AdminRepository 中已实现的 API 模块方法，并完全遵循其编码风格与实现模式
- apiPage / apiCount / apiQuery / apiInsert / apiUpdate / apiDelete / apiToggle
- toApiVO / toApiPO
- 位于 backend/ai-agent-infrastructure/src/main/java/com/dasi/infrastructure/persistent/dao 的每个 Dao 接口

任务要求：要求你按同样的结构和已经完成的 Dao，为以下模块补齐空实现：
- Model、Mcp、Advisor、Prompt、Client、Agent、User

注意：
- 严禁修改：任何 Dao、Mapper、Service、Controller、Domain、DTO、VO、SQL 脚本、配置文件。
- 严禁新增文件、严禁重构包结构、严禁改变接口方法签名、严禁改动 API 模块现有实现。
- User 模块无 toggle：不要实现 userToggle
- 空集合判断统一用 CollectionUtils.isEmpty，返回空集合用 List.of()