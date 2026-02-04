现在你需要在我的项目基础上，实现 Session 领域的全链路后端，包括 Controller、Service、Repository、Dao、Mapper

我已经定义了数据库表，位于 backend/docs/mysql/table.sql

我已经写了我一部分 SessionController，这是我认为应该提供的 API，如果有必要，你可以进行适当的修改，包括 VO 和 Request 对象

同时，现在前端的实现是使用 pinia 和 localStorage，请你改为接入后端的 API

你需要注意的地方
- userId 不需要传入，而是从 AuthContext 取
- 删除 session 的时候需要同步删除所有的 message
- 根据类型来获取 message，按 seq 和 create 时间来排序
- work-sse 对应 Work.vue 左半边的数据
- work-answer 对应 Work.vue 右半边的数据
- work 和 chat 的 session 上限都是三个，你需要想办法给出提示和报错
- 在 chat 的会话之中，角色 user 的 message 最多只能是 20 个
- 在 work 的会话之中，角色 user 的 message 最多只能是 3 个

同时，你需要 AdminRepository 