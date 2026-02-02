现在请你更新 Admin.vue、api.js 和 pinia.js
在这之前，选择类型和 xxxId 一直是在前端直接预设，现在后端提供以下接口，要求从这里拿到可供选择的列表
```java
@GetMapping("/list/clientType")
public Result<List<String>> listClientType() {
    return Result.success(adminService.listClientType());
}

@GetMapping("/list/agentType")
public Result<List<String>> listAgentType() {
    return Result.success(adminService.listAgentType());
}

@GetMapping("/list/userRole")
public Result<List<String>> listUserRole() {
    return Result.success(adminService.listUserRole());
}

@GetMapping("/list/apiId")
public Result<List<String>> listApiId() {
    return Result.success(adminService.listApiId());
}

@GetMapping("/list/modelId")
public Result<List<String>> listModelId() {
    return Result.success(adminService.listModelId());
}
```