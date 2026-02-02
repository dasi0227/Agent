## 角色和任务

角色：你是资深全栈工程师（Vue3 + Spring Boot + MyBatis/DDD）。

任务：本次只聚焦实现/完善前端 Admin 工作流页面 FLOW（路由 /admin/flow），并保证与后端联通。

## 必须先做

你必须先通读并严格遵守现有项目目录结构、代码风格、命名习惯、已有封装：
- 前端：router.js、request.js、api.js、pinia.js、main.js、Flow.vue
- 后端：backend/ai-agent-trigger/src/main/java/com/dasi/trigger/http/AdminController.java、backend/ai-agent-domain/src/main/java/com/dasi/domain/admin/service/AdminService.java、backend/ai-agent-infrastructure/src/main/java/com/dasi/infrastructure/repository/AdminRepository.java
- 样式图片：/frontend/reference1.png、/frontend/reference2.png、/frontend/reference3.png

## 总体目标

实现 AdminFlow.vue，对应路由 /admin/flow，包含：
1) 页面进入时必须先请求所有 Client 详情，数据来自 POST /flow/client，缓存以供后续渲染与选择使用。
2) 首屏：四列多行可滚动的 Agent 卡片网格（参考 reference1），数据来自 POST /flow/agent/list
3) 点击某个 Agent：进入同页详情视图，数据来自 /admin/flow/agent?agentId=xxx，展示该 Agent 的 Flow 配置卡槽（四个角色位，从左到右固定顺序），并提供 Client 选择与替换。
4) Client 替换规则：后端必须先删除后添加。

## 首屏：Agent 网格卡片（reference1）

布局：
- 四列多行网格，可上下滚动。
- 卡片为长方形，上下分区：中间展示 agentId，上面展示 agentType，下面展示 agentName。

交互：
- 右上角状态圆点：绿色=启用、红色=禁用（agentStatus）。
- hover 在 整卡 触发 flip 翻转，背面展示 agentDesc。
- 点击卡片进入“该 Agent 的 flow 配置详情视图”。

## 详情视图：根据 agentType 展示 Step / Loop

点击 Agent 卡片后：
1) 调用 POST /flow/agent 获取该 agent 的 flow 配置。
2) 根据 agentType 渲染不同的角色标题（顺序严禁更改）：
   - step：ANALYZER、PERFORMER、SUPERVISOR、SUMMARIZER（参考 reference2）
   - loop：INSPECTOR、PLANNER、RUNNER、REPLIER（参考 reference3）
3) 详情视图结构：
- 从左到右四个“角色卡槽”（长方形卡槽区域），每个卡槽上方有大标题（角色名）。
- 若该角色已配置 client：在卡槽内渲染一个 Client 卡片（字段至少选 Id 展示，可以自己决定还需要展示什么字段来使得效果更好）。
- 若未配置：卡槽内显示居中 “➕” 按钮，点击选择 Client。
- 同时仅允许一个角色处于“正在选择/可选择”状态（UI 高亮当前角色卡槽）。

## Client 候选区：横向滚动选择（仅在选中角色时出现）

当点击某个角色卡槽（无论已配置还是未配置）：
1) 在页面下方出现横向滚动的候选 Client 卡片列表。
2) 候选数据来源：页面初始化缓存的全部 clientList，但要排除已经占用在四个卡槽中的 client（避免重复绑定）。
3) 候选卡片只显示简要信息，点击后弹出确认替换/添加弹窗（oldClientId -> newClientId）。

## 提示

- UI/交互风格必须与现有 Admin UI 保持一致
- 参考图仅用于结构与交互提示：你可以在此基础上做更好的细节优化（间距、圆角、hover、空态、加载态、过渡动画）
- 允许按需引入外部 JS 库/代码库，但必须满足：不破坏项目既有请求封装、路由、状态管理与样式体系；依赖要小、用途明确；引入后要在 package.json 中固定版本；若项目已有同类能力，必须优先复用，不重复造轮子。
- 角色卡槽的顺序绝对固定，不允许任何重排（包括视觉上与数据上）
- 整体交互围绕“卡片”展开：以卡片作为主要信息载体与操作入口，强调“轻量但有趣”的动效反馈，让用户通过点击/拖拽/切换等卡片动作完成配置与编辑；动效需克制、短时、可预期，不喧宾夺主且不破坏现有 Admin UI 风格。