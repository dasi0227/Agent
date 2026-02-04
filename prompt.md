## 一、任务与项目事实

你是一个在现有项目里做功能开发的全栈工程师。请先阅读并严格遵守项目结构与现有代码风格，再实现 Session 领域全链路功能（后端 + 前端 + 管理后台）。
- 前端：Vue 3 + Vite + Pinia + vue-router 4 + TailwindCSS。
- 后端：Spring Boot 3（Java 17），DDD 分层，MyBatis，MySQL，Redis，Spring AI。

## 二、后端要求：Controller → Service → Repository → Dao → Mapper

### 2.1 Session API

修改类 backend/ai-agent-trigger/src/main/java/com/dasi/trigger/http/SessionController.java，允许调整，但必须满足：
- userId 不由前端传入，从 AuthContext 获取，并写入 session_user。
- 每种类型最多 3 个 session（chat/work 各 3 个），超过要 throw SessionException，前端展示错误信息。
- 删除 session 时必须同步删除该 session 下的所有 message。
- 消息查询必须按 message_seq ASC, created_at ASC 排序。

### 2.2 Message 写入与限制

当前无 message insert API，我的想法是在 util 实现一个 MessageService，用于在其他 Controller 和 domain 调用，同时针对每个 session 满足
- chat 会话：role=user 最多 20 条
- work 会话：role=user 最多 3 条
- work-sse → Work.vue 左侧流式卡片
- work-answer → Work.vue 右侧问答
- saveUserMessage(...)、saveAssistantMessage(...)、saveWorkSseMessage(...)、saveWorkAnswerMessage(...)

### 2.3 DDD 层实现

ISessionService / SessionService / IMessageService / MessageService
ISessionRepository / SessionRepository
SessionVO / MessageVO
SessionType / MessageType
ISessionDao / IMessageDao
SessionDao.xml / MessageDao.xml

## 三、前端改造（移除 localStorage，会话改为接入后端 API 存储）

### 3.1 新增前端 API

在 api.js 增加后端的接口，参照 SessionController

### 3.2 Chat / Work 会话改造

- session 列表从后端加载
- 创建 session 调后端 insert
- 重命名调后端 update
- 删除调后端 delete
- 切换 session 时拉消息
- 仅设置类数据可继续 localStorage

### 3.3 前端提示限制

- session 数量到上限：提示并禁止新建
- user message 达到上限：提示并禁止发送

## 四、Admin 后台：Session 查看模块（只读，不提供增删改）

### 4.1 菜单与路由

- 新增 AdminSession 页面，对应路由 /admin/session
- 在 CommonDataUtil.js 添加 SESSION 模块，作为用户管理，放在 USER 下面

### 4.2 UI

- 初始视图：卡片网格（类似 AdminFlow）
  - 正面：sessionId / sessionUser / sessionType
  - 背面：sessionTitle
- 详情视图：
  - chat：按 Chat.vue 样式渲染对话
  - work：左侧 work-sse，右侧 work-answer（复用 Work.vue 视觉风格）
  - 如果有必要，可以设置一个加载动画，防止加载聊天数据过久

### 4.3 后端

- 不需要添加 Redis 缓存

## 五、约束

### 5.1 必须参照的文件

- 表结构：table.sql
- Session Controller：backend/ai-agent-trigger/src/main/java/com/dasi/trigger/http/SessionController.java
- Session domain：backend/ai-agent-domain/src/main/java/com/dasi/domain/session/**
- Infrastructure：backend/ai-agent-infrastructure/src/main/java/com/dasi/infrastructure/**
- Mapper：backend/ai-agent-infrastructure/src/main/resources/mapper/**
- UI：frontend/src/components/**
- JS：frontend/src/router/** + frontend/src/request/** + frontend/src/utils/**
- Dark-Theme：frontend/src/style.css

### 5.2 限制

- 不要新增全局样式，所有样式尽量放在组件内，且与现有 Tailwind 风格一致。
- 必须兼容明暗主题：使用 style.css 已定义的 CSS 变量，不要写死颜色；如果必须写色值，需同时提供暗黑模式对应值。
- 黑白颜色转换：所有新增 UI（尤其是 AdminSession 页面与弹窗/按钮）必须在浅色与深色主题下保持可读和对比度。
- 不引入新的 UI 组件库，复用现有按钮/弹窗样式。
- 禁止改动 Auth/Admin/AI/Query 等领域代码，除非要添加 Message 逻辑