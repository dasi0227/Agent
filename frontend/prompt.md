## 身份与需求

- 身份：你是一个资深全栈工程师（Vue3 + Spring Boot + MyBatis/DDD）
- 任务：本次任务只做前端 Admin 后台的结构扩展与页面拆分：在保持现有 UI 风格与代码结构不变的前提下，新增“工作流管理”板块，并为 CONFIG/FLOW 采用独立页面（非分页表格），其余模块继续复用你已经实现的通用分页表格页面。

## 阅读与对齐（不可跳过）【重要】
1.	阅读前端现有目录与实现，特别是：
- frontend/src/router/router.js：路由组织方式
- frontend/src/request/api.js：接口定义方式
- frontend/src/request/request.js：数据请求方式
- frontend/src/router/pinia.js：数据存储方式
- frontend/src/components：组件风格与封装方式

2.	阅读后端现有目录与实现，特别是：
- backend/ai-agent-trigger/src/main/java/com/dasi/trigger/http/AdminController.java
- backend/ai-agent-types/src/main/java/com/dasi/types/dto/request/admin/page/ConfigListRequest.java
- backend/ai-agent-types/src/main/java/com/dasi/types/dto/request/admin/manage/ConfigManageRequest.java
- backend/ai-agent-domain/src/main/java/com/dasi/domain/admin/model/vo/ConfigVO.java

## 本次修改目标（只改 Admin 后台相关前端）

你已完成的：
- Admin.vue：后台数据布局（内容区 + header）
- SidebarAdmin.vue：侧边菜单布局：模型管理 / 基础管理 / 用户管理

你现在需要新增与调整：
1. 左侧菜单新增一个板块「工作流管理」，放在最上面（位于“模型管理”上方），该板块包含两个模块：
- CONFIG
- FLOW
2. 你需要将 Admin.vue 改名为 AdminTable.vue，并且用作统一分页 CRUD 页面，对应路由为，点击侧边菜单会自动切换路由，默认是 /admin/agent：/admin/agent,/admin/client,/admin/model,/admin/api,/admin/mcp,/admin/prompt,/admin/advisor,/admin/user）
3. 路由新增两条：注意这两个页面不使用分页表格 AdminTable.vue 的统一逻辑（数据结构不同）。
- /admin/config -> 新页面 AdminConfig.vue
- /admin/flow -> 新页面 AdminFlow.vue
4. FLOW 暂不处理：只要空白占位即可，但 header 与布局要与 Admin 一致，保证菜单切换与标题正常。

## CONFIG 页面需求（AdminConfig.vue）【重要】
1. Header 与布局：保持 Admin 一致
2. 查询/筛选：保持 Admin 一致，字段有 idKeyword、valueKeyword 和 configType（下拉选择（options 来自接口）
3. 数据获取与展示结构（关键）：CONFIG 的列表接口返回的不是分页数组，而是 Map<String, List<ConfigVO>>
4. 展示效果
- 右侧主区域为一个可纵向滚动的列表（容器固定高度，内容溢出滚动）
- Map 的每个 key（clientId）渲染为一个「卡片」
- 卡片头部左侧：clientId（大号）
- 卡片头部右侧：新增按钮
- 卡片内容：一个表格，仅展示 3 列数据 + 操作列，样式与之前尽量保持调和

## 约束
- 严禁引入新的 UI 框架；严禁大改全局样式
- 新增组件/页面必须放到你项目现有的合理目录下（views/components 的组织方式保持一致）
- 不改后端、不改 DTO、不改 Controller
- FLOW 页面暂时只做空白占位，但路由、菜单、header 必须完整