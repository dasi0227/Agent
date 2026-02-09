package com.dasi.trigger.http;

import com.dasi.domain.admin.model.vo.*;
import com.dasi.domain.admin.service.IAdminService;
import com.dasi.domain.session.model.vo.SessionVO;
import com.dasi.domain.util.stat.IStatService;
import com.dasi.types.dto.response.admin.DashboardResponse;
import com.dasi.types.dto.request.admin.manage.*;
import com.dasi.types.dto.request.admin.query.*;
import com.dasi.types.dto.result.PageResult;
import com.dasi.types.dto.result.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @Resource
    private IAdminService adminService;

    @Resource
    private IStatService statService;

    // -------------------- Dashboard --------------------
    @GetMapping("/dashboard")
    public Result<DashboardResponse> dashboard() {
        return Result.success(adminService.dashboard());
    }


    // -------------------- API --------------------
    @PostMapping("/api/page")
    public Result<PageResult<ApiVO>> apiPage(@Valid @RequestBody ApiPageRequest request) {
        return Result.success(adminService.apiPage(request));
    }

    @PostMapping("/api/insert")
    public Result<Void> apiInsert(@Valid @RequestBody ApiManageRequest request) {
        adminService.apiInsert(request);
        return Result.success();
    }

    @PostMapping("/api/update")
    public Result<Void> apiUpdate(@Valid @RequestBody ApiManageRequest request) {
        adminService.apiUpdate(request);
        return Result.success();
    }

    @PostMapping("/api/delete")
    public Result<Void> apiDelete(@RequestParam("id") Long id) {
        adminService.apiDelete(id);
        return Result.success();
    }

    // -------------------- Model --------------------
    @PostMapping("/model/page")
    public Result<PageResult<ModelVO>> modelPage(@Valid @RequestBody ModelPageRequest request) {
        return Result.success(adminService.modelPage(request));
    }

    @PostMapping("/model/insert")
    public Result<Void> modelInsert(@Valid @RequestBody ModelManageRequest request) {
        adminService.modelInsert(request);
        return Result.success();
    }

    @PostMapping("/model/update")
    public Result<Void> modelUpdate(@Valid @RequestBody ModelManageRequest request) {
        adminService.modelUpdate(request);
        return Result.success();
    }

    @PostMapping("/model/delete")
    public Result<Void> modelDelete(@RequestParam("id") Long id) {
        adminService.modelDelete(id);
        return Result.success();
    }

    // -------------------- MCP --------------------
    @PostMapping("/mcp/page")
    public Result<PageResult<McpVO>> mcpPage(@Valid @RequestBody McpPageRequest request) {
        return Result.success(adminService.mcpPage(request));
    }

    @PostMapping("/mcp/insert")
    public Result<Void> mcpInsert(@Valid @RequestBody McpManageRequest request) {
        adminService.mcpInsert(request);
        return Result.success();
    }

    @PostMapping("/mcp/update")
    public Result<Void> mcpUpdate(@Valid @RequestBody McpManageRequest request) {
        adminService.mcpUpdate(request);
        return Result.success();
    }

    @PostMapping("/mcp/delete")
    public Result<Void> mcpDelete(@RequestParam("id") Long id) {
        adminService.mcpDelete(id);
        return Result.success();
    }

    // -------------------- Advisor --------------------
    @PostMapping("/advisor/page")
    public Result<PageResult<AdvisorVO>> advisorPage(@Valid @RequestBody AdvisorPageRequest request) {
        return Result.success(adminService.advisorPage(request));
    }

    @PostMapping("/advisor/insert")
    public Result<Void> advisorInsert(@Valid @RequestBody AdvisorManageRequest request) {
        adminService.advisorInsert(request);
        return Result.success();
    }

    @PostMapping("/advisor/update")
    public Result<Void> advisorUpdate(@Valid @RequestBody AdvisorManageRequest request) {
        adminService.advisorUpdate(request);
        return Result.success();
    }

    @PostMapping("/advisor/delete")
    public Result<Void> advisorDelete(@RequestParam("id") Long id) {
        adminService.advisorDelete(id);
        return Result.success();
    }

    // -------------------- Prompt --------------------
    @PostMapping("/prompt/page")
    public Result<PageResult<PromptVO>> promptPage(@Valid @RequestBody PromptPageRequest request) {
        return Result.success(adminService.promptPage(request));
    }

    @PostMapping("/prompt/insert")
    public Result<Void> promptInsert(@Valid @RequestBody PromptManageRequest request) {
        adminService.promptInsert(request);
        return Result.success();
    }

    @PostMapping("/prompt/update")
    public Result<Void> promptUpdate(@Valid @RequestBody PromptManageRequest request) {
        adminService.promptUpdate(request);
        return Result.success();
    }

    @PostMapping("/prompt/delete")
    public Result<Void> promptDelete(@RequestParam("id") Long id) {
        adminService.promptDelete(id);
        return Result.success();
    }

    // -------------------- Client --------------------
    @PostMapping("/client/page")
    public Result<PageResult<ClientVO>> clientPage(@Valid @RequestBody ClientPageRequest request) {
        return Result.success(adminService.clientPage(request));
    }

    @PostMapping("/client/insert")
    public Result<Void> clientInsert(@Valid @RequestBody ClientManageRequest request) {
        adminService.clientInsert(request);
        return Result.success();
    }

    @PostMapping("/client/update")
    public Result<Void> clientUpdate(@Valid @RequestBody ClientManageRequest request) {
        adminService.clientUpdate(request);
        return Result.success();
    }

    @PostMapping("/client/delete")
    public Result<Void> clientDelete(@RequestParam("id") Long id) {
        adminService.clientDelete(id);
        return Result.success();
    }

    @PostMapping("/client/toggle")
    public Result<Void> clientToggle(@RequestParam("id") Long id, @RequestParam("clientStatus") Integer clientStatus) {
        adminService.clientToggle(id, clientStatus);
        return Result.success();
    }

    // -------------------- Agent --------------------
    @PostMapping("/agent/page")
    public Result<PageResult<AgentVO>> agentPage(@Valid @RequestBody AgentPageRequest request) {
        return Result.success(adminService.agentPage(request));
    }

    @PostMapping("/agent/list")
    public Result<List<AgentVO>> agentList(@Valid @RequestBody AgentListRequest request) {
        return Result.success(adminService.agentList(request));
    }

    @PostMapping("/agent/insert")
    public Result<Void> agentInsert(@Valid @RequestBody AgentManageRequest request) {
        adminService.agentInsert(request);
        return Result.success();
    }

    @PostMapping("/agent/update")
    public Result<Void> agentUpdate(@Valid @RequestBody AgentManageRequest request) {
        adminService.agentUpdate(request);
        return Result.success();
    }

    @PostMapping("/agent/delete")
    public Result<Void> agentDelete(@RequestParam("id") Long id) {
        adminService.agentDelete(id);
        return Result.success();
    }

    @PostMapping("/agent/toggle")
    public Result<Void> agentToggle(@RequestParam("id") Long id, @RequestParam("agentStatus") Integer agentStatus) {
        adminService.agentToggle(id, agentStatus);
        return Result.success();
    }

    // -------------------- User --------------------
    @PostMapping("/user/page")
    public Result<PageResult<UserVO>> userPage(@Valid @RequestBody UserPageRequest request) {
        return Result.success(adminService.userPage(request));
    }

    @PostMapping("/user/insert")
    public Result<Void> userInsert(@Valid @RequestBody UserManageRequest request) {
        adminService.userInsert(request);
        return Result.success();
    }

    @PostMapping("/user/update")
    public Result<Void> userUpdate(@Valid @RequestBody UserManageRequest request) {
        adminService.userUpdate(request);
        return Result.success();
    }

    @PostMapping("/user/delete")
    public Result<Void> userDelete(@RequestParam("id") Long id) {
        adminService.userDelete(id);
        return Result.success();
    }

    @PostMapping("/user/toggle")
    public Result<Void> userToggle(@RequestParam("id") Long id, @RequestParam("userStatus") Integer userStatus) {
        adminService.userToggle(id, userStatus);
        return Result.success();
    }

    // -------------------- Config --------------------
    @PostMapping("/config/list")
    public Result<Map<String, List<ConfigVO>>> configList(@Valid @RequestBody ConfigListRequest request) {
        return Result.success(adminService.configList(request));
    }

    @PostMapping("/config/insert")
    public Result<Void> configInsert(@Valid @RequestBody ConfigManageRequest request) {
        adminService.configInsert(request);
        return Result.success();
    }

    @PostMapping("/config/update")
    public Result<Void> configUpdate(@Valid @RequestBody ConfigManageRequest request) {
        adminService.configUpdate(request);
        return Result.success();
    }

    @PostMapping("/config/delete")
    public Result<Void> configDelete(@RequestParam("id") Long id) {
        adminService.configDelete(id);
        return Result.success();
    }

    @PostMapping("/config/toggle")
    public Result<Void> configToggle(@RequestParam("id") Long id, @RequestParam("configStatus") Integer configStatus) {
        adminService.configToggle(id, configStatus);
        return Result.success();
    }

    // -------------------- Flow --------------------
    @PostMapping("/flow/client")
    public Result<List<ClientDetailVO>> flowClient() {
        return Result.success(adminService.flowClient());
    }

    @PostMapping("/flow/agent")
    public Result<List<FlowVO>> flowAgent(@RequestParam("agentId") String agentId) {
        return Result.success(adminService.flowAgent(agentId));
    }

    @PostMapping("/flow/insert")
    public Result<Void> flowInsert(@Valid @RequestBody FlowManageRequest request) {
        adminService.flowInsert(request);
        return Result.success();
    }

    @PostMapping("/flow/update")
    public Result<Void> flowUpdate(@Valid @RequestBody FlowManageRequest request) {
        adminService.flowUpdate(request);
        return Result.success();
    }

    @PostMapping("/flow/delete")
    public Result<Void> flowDelete(@RequestParam("id") Long id) {
        adminService.flowDelete(id);
        return Result.success();
    }

    // -------------------- Task --------------------
    @PostMapping("/task/page")
    public Result<PageResult<TaskVO>> taskPage(@Valid @RequestBody TaskPageRequest request) {
        return Result.success(adminService.taskPage(request));
    }

    @PostMapping("/task/insert")
    public Result<Void> taskInsert(@Valid @RequestBody TaskManageRequest request) {
        adminService.taskInsert(request);
        return Result.success();
    }

    @PostMapping("/task/update")
    public Result<Void> taskUpdate(@Valid @RequestBody TaskManageRequest request) {
        adminService.taskUpdate(request);
        return Result.success();
    }

    @PostMapping("/task/delete")
    public Result<Void> taskDelete(@RequestParam("id") Long id) {
        adminService.taskDelete(id);
        return Result.success();
    }

    @PostMapping("/task/toggle")
    public Result<Void> taskToggle(@RequestParam("id") Long id, @RequestParam("taskStatus") Integer taskStatus) {
        adminService.taskToggle(id, taskStatus);
        return Result.success();
    }

    // -------------------- Session --------------------
    @GetMapping("/session/list")
    public Result<List<SessionVO>> listSession() {
        return Result.success(adminService.listSession());
    }

    // -------------------- List --------------------
    @GetMapping("/list/clientType")
    public Result<List<String>> listClientType() {
        return Result.success(adminService.listClientType());
    }

    @GetMapping("/list/agentType")
    public Result<List<String>> listAgentType() {
        return Result.success(adminService.listAgentType());
    }

    @GetMapping("/list/configType")
    public Result<List<String>> listConfigType() {
        return Result.success(adminService.listConfigType());
    }

    @GetMapping("/list/clientRole")
    public Result<List<String>> listClientRole() {
        return Result.success(adminService.listClientRole());
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

}
