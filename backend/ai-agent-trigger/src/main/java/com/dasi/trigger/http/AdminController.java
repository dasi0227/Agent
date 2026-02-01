package com.dasi.trigger.http;

import com.dasi.domain.admin.model.vo.*;
import com.dasi.domain.admin.service.IAdminService;
import com.dasi.types.dto.request.admin.manage.*;
import com.dasi.types.dto.request.admin.page.*;
import com.dasi.types.dto.result.PageResult;
import com.dasi.types.dto.result.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @Resource
    private IAdminService adminService;

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

    @PostMapping("/api/toggle")
    public Result<Void> apiToggle(@RequestParam("id") Long id, @RequestParam("apiStatus") Integer apiStatus) {
        adminService.apiToggle(id, apiStatus);
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

    @PostMapping("/model/toggle")
    public Result<Void> modelToggle(@RequestParam("id") Long id, @RequestParam("modelStatus") Integer modelStatus) {
        adminService.modelToggle(id, modelStatus);
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

    @PostMapping("/mcp/toggle")
    public Result<Void> mcpToggle(@RequestParam("id") Long id, @RequestParam("mcpStatus") Integer mcpStatus) {
        adminService.mcpToggle(id, mcpStatus);
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

    @PostMapping("/advisor/toggle")
    public Result<Void> advisorToggle(@RequestParam("id") Long id, @RequestParam("advisorStatus") Integer advisorStatus) {
        adminService.advisorToggle(id, advisorStatus);
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

    @PostMapping("/prompt/toggle")
    public Result<Void> promptToggle(@RequestParam("id") Long id, @RequestParam("promptStatus") Integer promptStatus) {
        adminService.promptToggle(id, promptStatus);
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
    public Result<PageResult<AdminAgentVO>> agentPage(@Valid @RequestBody AgentPageRequest request) {
        return Result.success(adminService.agentPage(request));
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

}
