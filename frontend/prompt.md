1. 404 页面不允许前往后台，只保留返回上页和前往首页
2. 现在登录之后进入到 /chat，手动在网址输入 /login，会自动跳转到 /admin，行为不对，应该就是 /login
3. 我现在需要你新建一个页面 AuthAdmin.vue，对应的路由是 /admin/login，样式与 Auth.vue 保持一致，但是去掉注册和登录选项，只能登录，标题是“后台管理”，同时最下面的提示改为注册管理员请联系开发者
4. 只有通过登录  /admin/login 才能进入 /admin，不再需要 /login?type=admin