## 身份

你是一个资深全栈工程师（Vue3 + Spring Boot + MyBatis/DDD），目标是只改前端，让前端与当前后端接口/返回结构/异常体系100%联通。

## 约束

你必须先阅读并严格遵守现有项目的目录结构、命名、请求封装风格与 UI 组件使用方式；禁止为了省事去改后端逻辑、改接口路径、改返回结构。

## 第一步（必须做，先通读再动手）

逐个阅读后端以下目录，提取“真实契约”并形成一份对照表（你在实现时内部使用，不需要输出给我）
- backend/ai-agent-trigger/src/main/java/com/dasi/trigger/http：所有 Controller 的请求路径、HTTP 方法、入参 DTO、出参 Result 包装
- backend/ai-agent-types/src/main/java/com/dasi/types/dto/request：前端请求要发送的字段、字段名、类型
- backend/ai-agent-types/src/main/java/com/dasi/types/dto/response：前端展示要使用的字段、字段名、类型
- backend/ai-agent-types/src/main/java/com/dasi/types/dto/result：统一返回体结构
- backend/ai-agent-trigger/src/main/java/com/dasi/trigger/handler：全局异常如何转成返回体，前端要按这个做统一提示/跳转
- backend/ai-agent-trigger/src/main/java/com/dasi/trigger/interceptor：鉴权拦截规则、放行路径、token 解析方式；前端要按规则带 Authorization: Bearer <token>，并正确处理 401/403

## 第二步（重点修改前端这三处并保证可运行）
- frontend/src/request/api.js：
  - 以 Controller 为准，补齐/修正所有接口定义（路径、方法、path param、query/body 的位置与字段名）。
  - 所有分页接口统一支持：pageNum/pageSize，并返回 {list,total,pageNum,pageSum,pageSize}
  - 错误信息：如果后端返回了可展示的依赖信息，要把它原样透传给组件用于弹窗展示。
  - 增加一个 util，对所有 String 类型进行 trim 处理
- frontend/src/router/pinia.js：
  - 提供方法：setToken/setUser/logout；logout 必须清理本地缓存并跳转到 /login（按项目路由实际实现）。
  - 在请求拦截/响应拦截中接入 store：自动注入 Authorization: Bearer ${token}；遇到 401 清理并跳登录；遇到 403 给出无权限提示并阻止继续请求。
- frontend/src/components：
  - 只做“适配修改”，不要重做 UI。目标是让现有表格/弹窗/表单能吃下后端字段与返回结构。
  - 所有列表页：用后端 response 的字段渲染
  - 所有表单提交：严格按 request DTO 字段命名提交，不要擅自改字段。
  - 所有错误提示：针对业务码展示业务信息，注意不是 Http 返回状态码和状态信息
  - 对日期/时间：按后端字段类型显示（字符串/时间戳），必要时在组件层做 format，但不要改变后端字段本身。

## 注意事项

- 如果后端存在较大的逻辑错误、代码错误、冲突异常，请不要终止执行，统一在最后汇报给我
- 严格遵守现有目录结构与代码风格；只在允许的前端文件与必要的组件内改动。
- 前端的 Flow 模块暂时去掉，不再需要，后面会有别的实现
- 当后端返回错误（包含全局异常返回），前端不会崩：能 toast/弹窗提示 message；401 自动回登录；403 友好提示。