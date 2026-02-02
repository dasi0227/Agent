现在你需要在 Admin.vue 中的管理菜单新增一个板块叫做“工作流管理”，放在最上面，即“模型管理”的上面，并且在其中新增两个模块分别是 CONFIG 和 FLOW

但是这两个模块对应的右侧主区域，除了 header 一样之外，数据展示不再是分页数据，你需要新建 AdminConfig.vue 和 AdminFlow.vue（分别对应 /admin/config 和 /admin/flow），而之前的你可以统一用 AdminTable.vue 统一作为 (/admin/agent,/admin/client,/admin/model,/admin/api,/admin/mcp,/admin/prompt,/admin/advisor,/admin/user)

- FLOW 暂不处理，用空白代替（注意！）
- CONFIG
  - 同样支持筛选条件
  ```java
  public class ConfigListRequest {
      private String idKeyword;
      private String valueKeyword;
      private String configType;

  }
  ```
  - configType 从下面的接口获取
  ```java
  @GetMapping("/list/configType")
  public Result<List<String>> listConfigType() {
      return Result.success(adminService.listConfigType());
  }
  ```
  - CONFIG 收到的数据为：Map<String, List<ConfigVO>>，展示效果是一个可以滑动的竖直列表，你需要根据 Map 的 clientId Key，将 List<ConfigVO> 的数据放到一个表格卡片展示（只需要给出configType、configValue、configParam），效果类似我给的图片，同时还有操作栏（和之前一样）；updateTime 是所有 Config 里面最近的 updateTime
  ```java
  public class ConfigVO {
      private Long id;
      private String clientId;
      private String configType;
      private String configValue;
      private String configParam;
      private Integer configStatus;
      private LocalDateTime updateTime;
  }
  ```
