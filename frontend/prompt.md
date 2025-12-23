1.你是资深前端工程师与架构师。请基于 Vue3 + Vite + JavaScript（Composition API） 实现一个“类 ChatGPT 官方页面”的问答式 AI 交互窗口（单页布局即可）。要求：代码可直接运行、4 空格缩进、尽量少依赖 UI 框架（可纯 CSS）、所有关键文件必须给出完整可复制代码与必要注释。

2.UI 总体布局（100vh，全屏）
	•	左侧固定 Sidebar.vue；右侧主聊天区 Chat.vue。
	•	Main 顶部固定 Header（模型选择 + 回答设置）。
	•	Main 底部固定 Input 和 Footer （输入问题 + 版权等信息提示）。
	•	Main 中间 MessageList 可滚动；整体风格贴近 ChatGPT：留白、圆角、柔和阴影、消息气泡、滚动自然、自动吸底。

3.Sidebar（左侧，固定宽度，如 280px，可折叠可不做但结构预留）：
	•	顶部：网站信息区（站点名/Logo 占位/一句简介）。
	•	中间：历史聊天列表（可滚动），支持：新建会话、点击切换会话、高亮当前会话、会话标题可用“用户第一句的前 20 字”。
	•	底部：用户信息区（头像占位、昵称、状态）。
	•	历史会话数据先用 localStorage 持久化（key 自定），结构需清晰（chatId、title、messages、createdAt）。

4.Footer（全局最底部固定）
	•	左侧版权信息（如 © 2025 XXX），右侧提醒信息（如“内容仅供参考”）。
	•	不遮挡主聊天区与输入栏：主区域必须预留 Footer 高度（用 CSS 变量/布局计算实现）。

5.Header（全局最顶部固定）：
	•	左侧：模型下拉选择（当前只有一个选项 deepseek-r1:1.5b，但实现必须支持未来扩展为数组）。
	•	右侧：回答设置按钮，点击弹出 SettingsModal：
	•	type：complete / stream（单选）
	•	temperature：0~2 数字输入，默认 0.7
	•	topK：整数输入，默认 40
	•	保存后立即生效：保存到 localStorage 并同步到 Pinia 状态

6.消息区 MessageList（中间可滚动区域）：
	•	一问一答：用户消息右侧气泡，助手消息左侧气泡。
	•	关键对齐约束（必须严格实现）：
	•	“用户消息气泡的右边缘”必须与“输入框右边缘”对齐；
	•	“助手消息气泡的左边缘”必须与“输入框左边缘”对齐；
	•	通过 MessageList 与 InputBar 使用同一个居中的 content 容器（max-width 如 900px）实现；不要用随意的 margin 来凑。
	•	助手消息支持展示“思考过程”：
	•	若内容包含 <think>...</think>，将 think 内文本提取显示在正文上方（灰色小字），正文显示 think 外内容。
	•	complete 模式：
	•	请求未返回期间，助手侧显示“思考中”占位气泡 + 等待动画（如三点闪烁）。
	•	成功后替换为真实内容，并正确分离 think/answer。
	•	stream 模式（SSE）：
	•	token 边到边渲染：
	•	<think> 与 </think> 之间 token 追加到“思考区”
	•	其他 token 追加到“正文区”
	•	最后一条 finishReason:“stop” 后结束。
	•	自动滚动（重要）：
	•	新增 token / 新消息时平滑滚动到底部；
	•	若用户手动上滑查看历史，不要强行拉回；实现“是否在底部”的判断（阈值如 80px）。

7.Input（位于 Header 之下，Footer 之上）：
	•	文本框 + 发送按钮；Enter 发送，Shift+Enter 换行。
	•	发送逻辑：
	•	用户问题立即写入当前会话 messages 并渲染用户气泡；
	•	随后根据设置调用 API，渲染助手消息（complete：先 pending 气泡；stream：边接收边追加）。
	•	禁用态：请求进行中时发送按钮禁用；如果实现“停止生成”，需要支持立即取消（AbortController）。

8.后端接口对接（必须按以下方式封装）：
	•	完整问答：
	•	GET http://localhost:8001/api/v1/ollama/complete?model=deepseek-r1%3A1.5b&message=hi
	•	返回 JSON，文本在 result.output.content（或 .text，要做兼容兜底），可能包含 <think>...</think>
	•	流式问答 SSE：
	•	GET http://localhost:8001/api/v1/ollama/stream?model=deepseek-r1%3A1.5b&message=hi
	•	Header：Accept: text/event-stream
	•	返回多行 data:{json}\n\n，每个 data 的增量 token 在 result.output.content（或 .text）
	•	最后一条含 finishReason:"stop"

9.请求封装文件（必须创建并按要求组织）：
	•	创建 axios 实例：baseURL=http://localhost:8001，timeout 合理（如 600000）
	•	前置拦截器：统一加 headers（Content-Type 等），从 localStorage 读取设置/token（即便暂无也要预留），记录请求开始时间并打印
	•	后置拦截器：统一处理错误（网络错误、非 2xx），打印耗时，返回规范化错误对象（便于 UI 用助手气泡提示）
	•	额外提供 streamFetch(url, onData, onError, onDone, signal)：
	•	使用原生 fetch 读取 ReadableStream（不要用 EventSource）
	•	按 \n\n 分隔事件块，提取 data: 行，JSON.parse 后回调增量 token

10.状态管理与数据结构（需明确并实现）：
	•	使用 Pinia 管理：
	•	settings（type/temperature/topK/model）+ localStorage 同步
	•	chats（会话列表、当前 chatId、messages、创建/切换/更新、持久化）
	•	sending 状态、abortController 引用（用于停止生成）

11.需要完成的内容：
	•	main.js / App.vue / index.html
    •   api.js / request.js / router.js / pinia.js
	•	最好用两个 Vue 文件搞定布局：Chat.vue / Sidebar.vue
    •   你可以补充你需要的工具文件到utils/下

12.验收自测清单（请你在生成代码时逐条对照保证满足）
	•	布局与对齐约束正确；Sidebar/Footer/InputBar/HeaderBar 不互相遮挡；MessageList 滚动正常。
	•	complete：发送后立刻出现用户气泡 + 助手“思考中”动画；返回后替换真实回答，并正确分离 think/answer。
	•	stream：token 实时追加；think 灰色区实时增长；收到 stop 结束；支持取消能立刻停止。
	•	request.js：有 axios 拦截器、耗时日志、规范化错误对象；api.js 统一管理接口；错误在 UI 中以助手气泡提示。
