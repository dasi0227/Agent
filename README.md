# Dasi Agent



## 项目简介

### 概述

Dasi Agent 是一个集成了 AI 对话、多角色 Agent 工作流、RAG 知识库和 MCP 工具调用的全栈项目，包含用户端与管理后台两套前端页面，以及基于 DDD 的后端服务。

### 技术栈

- 前端：Vue 3、Vite、TailwindCSS、Axios、Pinia、vue-router、vue-flow
- 后端：Spring Boot 3、Java 17、Spring AI、MyBatis、MySQL、PostgreSQL、Redis、Docker

### 目录结构

```
.
├── backend                     # 后端工程
│   ├── ai-agent-api            # API 层
│   ├── ai-agent-app            # 应用启动与配置层
│   ├── ai-agent-domain         # 领域层
│   ├── ai-agent-infrastructure # 基础设施层
│   ├── ai-agent-trigger        # 接口适配层
│   ├── ai-agent-types          # 公共对象层
│   ├── docs                    # 脚本文件
│   └── pom.xml                 # Maven 聚合配置
│
├── frontend                    # 前端工程
│   ├── index.html              # 入口 HTML
│   ├── node_modules            # 依赖目录
│   ├── package.json            # 前端依赖与脚本定义
│   ├── postcss.config.js       # PostCSS 配置
│   ├── public                  # 公共静态资源
│   ├── src                     # 前端源码目录
│   ├── tailwind.config.js      # TailwindCSS 配置
│   └── vite.config.js          # Vite 构建配置
│
└── mcp                         # MCP 自建服务集合
    ├── docker-build.sh         # MCP 镜像构建脚本
    ├── mcp-server-amap         # 高德地图 MCP 服务
    ├── mcp-server-bocha        # 博查搜索 MCP 服务
    ├── mcp-server-csdn         # CSDN MCP 服务
    ├── mcp-server-email        # 邮件发送 MCP 服务
    └── mcp-server-wecom        # 企业微信 MCP 服务
```

## 核心功能

### AI 对话

- 支持非流式对话（complete）和流式对话（stream）。
- 对话前增强：通过 `AugmentService` 注入 RAG 检索上下文与 MCP 工具回调。
- 会话记忆：基于 `sessionId` 透传到 `ChatMemory Advisor`，实现多轮上下文。
- 参数可控：`temperature`、`presencePenalty`、`maxCompletionTokens`、MCP 列表、RAG 标签均可由前端传入。
- 数据持久化：Controller 侧统一做用户消息兜底持久化，助手消息按有效输出持久化，便于追溯和后台审计。

### AI 工作

- 支持 SSE 持续输出执行分段结果。
- 动态策略：通过 `DispatchService` + `ExecuteStrategyFactory`，按 `agentId` 动态选择执行策略。
- loop 策略：`Analyzer -> Performer -> Supervisor` 多轮循环，直到通过或达到 `maxRound`，最后 `Summarizer` 总结。
- step 策略：`Inspector -> Planner -> Runner -> Replier` 顺序执行，`Runner` 按步骤重试直到成功或达到 `maxRetry`。
- 流程可配置：每个角色实际使用的 `clientId`、提示词与顺序由后台 Flow 配置驱动。
- 结果分段：节点输出按 `sectionType` 包装为统一结构对象，前端可按阶段实时渲染。

### RAG

- 支持文件上传和 Git 仓库导入。
- 文档处理链路：`TikaDocumentReader` 读取文档 -> `TokenTextSplitter` 切片 -> 写入 `PgVectorStore`。
- 标签隔离：每个文档块写入 `knowledge=ragTag` 元数据，实现按知识库标签检索。
- 检索增强：对话时默认 `topK=5`，命中片段注入系统提示后与用户问题一起发给模型。

### MCP-CLIENT

- 支持动态调用和预装配调用两种模式。
- 动态调用：聊天时前端传入 `mcpIdList`，后端即时创建 MCP Client 并挂载到当前请求。
- 预装配调用：系统启动后可根据 `armory` 配置自动装配（API、Model、MCP、Advisor、Client），并注册到 Spring 容器。
- SSE 方式：通过 `HttpClientSseClientTransport` 连接远程 MCP 服务。
- STDIO 方式：通过 `StdioClientTransport` 启动本地进程并走标准输入输出通信。
- 统一封装：封装为 `SyncMcpToolCallbackProvider`，挂载到 `ChatClient.toolCallbacks`。

### MCP-SERVER

- `mcp-server-csdn`（默认端口 `9001`）：工具 `saveArticle`，发布文章到 CSDN；关键入参 `title`、`markdownContent`。
- `mcp-server-wecom`（默认端口 `9002`）：工具 `sendText`、`sendTextCard`，发送企业微信应用消息；关键入参 `content` 或 `title/description/url`。
- `mcp-server-amap`（默认端口 `9003`）：工具 `checkWeather`，根据地址查询天气；关键入参 `address`
- `mcp-server-email`（默认端口 `9004`）：工具 `sendEmail`，发送邮件通知；关键入参 `to`、`subject`、`content`、`html`。
- `mcp-server-bocha`（默认端口 `9005`）：工具 `webSearch`，联网搜索；关键入参 `query`、`freshness`。


### ADMIN 管理后台

- 支持管理员在线对核心配置做 CRUD 与状态切换（启用/禁用）。
- 通用管理页面：`api`、`model`、`mcp`、`advisor`、`prompt`、`client`、`agent`、`task`、`user`。
- 特殊页面 `Dashboard`：总量统计、消息趋势（7d/30d）、chat/work 使用分布、Top 使用排行；支持自动刷新，同时支持右上角手动刷新按钮。
- 特殊页面 `Config`：按 `clientId` 分组管理配置项（`prompt/advisor/mcp` 等）。
- 特殊页面 `Flow`：按 Agent 类型（loop/step）维护角色链路与顺序。
- 特殊页面 `Canvas`：基于 `vue-flow` 的可视化流程画布，展示 Agent 与 Client 的连接关系。
- 特殊页面 `Session`：按会话类型查看 `chat/work` 历史消息与执行卡片。
- 分页策略：通用管理页统一使用 `PageResult`（`pageNum/pageSize/total/pageSum`）进行分页查询。
- 权限控制：`/admin/*` 路由要求管理员登录，非 admin 角色不可访问。

## 页面展示

### 首页

![image-20260213114200034](./assets/image-20260213114200034.png)

![image-20260213120205084](./assets/image-20260213120205084.png)

### Chat 对话

#### 普通对话

![image-20260212165822929](./assets/image-20260212165822929.png)

#### RAG 对话

![image-20260212170152042](./assets/image-20260212170152042.png)

![image-20260212182609418](./assets/image-20260212182609418.png)

#### MCP 对话

![image-20260212182749683](./assets/image-20260212182749683.png)

![image-20260212182922052](./assets/image-20260212182922052.png)

### AGENT 对话

#### AMAP + EMAIL

![image-20260212184929850](./assets/image-20260212184929850.png)

<img src="./assets/IMG_0093.PNG" alt="IMG_0093" style="zoom: 33%;" />

#### CSDN + WECOM

![image-20260212185834327](./assets/image-20260212185834327.png)

![image-20260212185944249](./assets/image-20260212185944249.png)

<img src="./assets/IMG_0094.PNG" alt="IMG_0094" style="zoom: 33%;" />

#### BOCHA + WECOM

![image-20260213034054539](./assets/image-20260213034054539.png)

<img src="./assets/IMG_0095.PNG" alt="IMG_0095" style="zoom: 33%;" />

### ADMIN 后台

#### 仪表盘

![image-20260213124223004](./assets/image-20260213124223004.png)

![image-20260213124401589](./assets/image-20260213124401589.png)

#### 工作流管理

![image-20260213124714732](./assets/image-20260213124714732.png)

![image-20260213124759820](./assets/image-20260213124759820.png)

![image-20260213124831899](./assets/image-20260213124831899.png)

![image-20260213124728232](./assets/image-20260213124728232.png)

#### 服务管理

![image-20260213124443407](./assets/image-20260213124443407.png)

![image-20260213124512567](./assets/image-20260213124512567.png)

#### 模型管理

![image-20260213124536607](./assets/image-20260213124536607.png)

![image-20260213124547530](./assets/image-20260213124547530.png)

#### 会话管理

![image-20260213124640237](./assets/image-20260213124640237.png)

![image-20260213124648168](./assets/image-20260213124648168.png)
