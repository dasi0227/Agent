package com.dasi.trigger.http;

import com.dasi.domain.admin.model.vo.*;
import com.dasi.domain.admin.service.IAdminService;
import com.dasi.types.dto.request.admin.*;
import com.dasi.types.dto.result.PageResult;
import com.dasi.types.dto.result.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @Resource
    private IAdminService adminService;

    // -------------------- API --------------------
    @PostMapping("/api/page")
    public Result<PageResult<ApiVO>> apiPage(@Valid @RequestBody ApiPageRequest apiPageRequest) {
        return Result.success(adminService.apiPage(apiPageRequest));
    }

    @PostMapping("/api/insert")
    public Result<Void> apiInsert(@Valid @RequestBody ApiManageRequest apiManageRequest) {
        adminService.apiInsert(apiManageRequest);
        return Result.success();
    }

    @PostMapping("/api/update")
    public Result<Void> apiUpdate(@Valid @RequestBody ApiManageRequest apiManageRequest) {
        adminService.apiUpdate(apiManageRequest);
        return Result.success();
    }

    @PostMapping("/api/delete")
    public Result<Void> apiDelete(@PathParam("apiId") String apiId) {
        adminService.apiDelete(apiId);
        return Result.success();
    }

    @PostMapping("/api/toggle")
    public Result<Void> apiToggle(@PathParam("apiId") String apiId, @PathParam("apiStatus") Integer apiStatus) {
        adminService.apiToggle(apiId, apiStatus);
        return Result.success();
    }

    // -------------------- Model --------------------
    @PostMapping("/model/page")
    public Result<PageResult<ModelVO>> modelPage(@Valid @RequestBody ModelPageRequest modelPageRequest) {
        return Result.success(adminService.modelPage(modelPageRequest));
    }

    @PostMapping("/model/insert")
    public Result<ModelVO> createModel(@Valid @RequestBody ModelManageRequest request) {
        return Result.success(adminService.modelCreate(request));
    }

    @PostMapping("/model/update")
    public Result<ModelVO> updateModel(@Valid @RequestBody ModelManageRequest request) {
        return Result.success(adminService.modelUpdate(request));
    }

    @PostMapping("/model/delete")
    public Result<Void> deleteModel(@PathParam("modelId") String modelId) {
        adminService.modelDelete(modelId);
        return Result.success();
    }

    @PostMapping("/model/toggle")
    public Result<ModelVO> modelToggle(@PathParam("modelId") String modelId, @PathParam("modelStatus") Integer modelStatus) {
        return Result.success(adminService.modelToggle(modelId, modelStatus));
    }

    // -------------------- MCP --------------------
    @PostMapping("/mcp/page")
    public Result<PageResult<McpVO>> listMcps(@Valid @RequestBody McpPageRequest request) {
        return Result.success(adminService.mcpPage(request));
    }

    @PostMapping("/mcp/insert")
    public Result<McpVO> createMcp(@Valid @RequestBody McpManageRequest request) {
        return Result.success(adminService.mcpCreate(request));
    }

    @PostMapping("/mcp/update")
    public Result<McpVO> updateMcp(@Valid @RequestBody McpManageRequest request) {
        return Result.success(adminService.mcpUpdate(request));
    }

    @PostMapping("/mcp/delete")
    public Result<Void> deleteMcp(@PathParam("mcpId") String mcpId) {
        adminService.mcpDelete(mcpId);
        return Result.success();
    }

    @PostMapping("/mcp/toggle")
    public Result<McpVO> mcpToggle(@PathParam("mcpId") String mcpId, @PathParam("mcpStatus") Integer mcpStatus) {
        return Result.success(adminService.mcpToggle(mcpId, mcpStatus));
    }

    // -------------------- Advisor --------------------
    @PostMapping("/advisor/page")
    public Result<PageResult<AdvisorVO>> listAdvisors(@Valid @RequestBody AdvisorPageRequest request) {
        return Result.success(adminService.advisorPage(request));
    }

    @PostMapping("/advisor/insert")
    public Result<AdvisorVO> createAdvisor(@Valid @RequestBody AdvisorManageRequest request) {
        return Result.success(adminService.advisorCreate(request));
    }

    @PostMapping("/advisor/update")
    public Result<AdvisorVO> updateAdvisor(@Valid @RequestBody AdvisorManageRequest request) {
        return Result.success(adminService.advisorUpdate(request));
    }

    @PostMapping("/advisor/delete")
    public Result<Void> deleteAdvisor(@PathParam("advisorId") String advisorId) {
        adminService.advisorDelete(advisorId);
        return Result.success();
    }

    @PostMapping("/advisor/toggle")
    public Result<AdvisorVO> advisorToggle(@PathParam("advisorId") String advisorId, @PathParam("advisorStatus") Integer advisorStatus) {
        return Result.success(adminService.advisorToggle(advisorId, advisorStatus));
    }

    // -------------------- Prompt --------------------
    @PostMapping("/prompt/page")
    public Result<PageResult<PromptVO>> listPrompts(@Valid @RequestBody PromptPageRequest request) {
        return Result.success(adminService.promptPage(request));
    }

    @PostMapping("/prompt/insert")
    public Result<PromptVO> createPrompt(@Valid @RequestBody PromptManageRequest request) {
        return Result.success(adminService.promptCreate(request));
    }

    @PostMapping("/prompt/update")
    public Result<PromptVO> updatePrompt(@Valid @RequestBody PromptManageRequest request) {
        return Result.success(adminService.promptUpdate(request));
    }

    @PostMapping("/prompt/delete")
    public Result<Void> deletePrompt(@PathParam("promptId") String promptId) {
        adminService.promptDelete(promptId);
        return Result.success();
    }

    @PostMapping("/prompt/toggle")
    public Result<PromptVO> promptToggle(@PathParam("promptId") String promptId, @PathParam("promptStatus") Integer promptStatus) {
        return Result.success(adminService.promptToggle(promptId, promptStatus));
    }

    // -------------------- Client --------------------
    @PostMapping("/client/page")
    public Result<PageResult<ClientVO>> listClients(@Valid @RequestBody ClientPageRequest request) {
        return Result.success(adminService.clientPage(request));
    }

    @PostMapping("/client/insert")
    public Result<ClientVO> createClient(@Valid @RequestBody ClientManageRequest request) {
        return Result.success(adminService.clientCreate(request));
    }

    @PostMapping("/client/update")
    public Result<ClientVO> updateClient(@Valid @RequestBody ClientManageRequest request) {
        return Result.success(adminService.clientUpdate(request));
    }

    @PostMapping("/client/delete")
    public Result<Void> deleteClient(@PathParam("clientId") String clientId) {
        adminService.clientDelete(clientId);
        return Result.success();
    }

    @PostMapping("/client/toggle")
    public Result<ClientVO> clientToggle(@PathParam("clientId") String clientId, @PathParam("clientStatus") Integer clientStatus) {
        return Result.success(adminService.clientToggle(clientId, clientStatus));
    }

    // -------------------- Flow --------------------
    @PostMapping("/flow/page")
    public Result<PageResult<FlowVO>> listFlows(@Valid @RequestBody FlowPageRequest request) {
        return Result.success(adminService.flowPage(request));
    }

    @PostMapping("/flow/insert")
    public Result<FlowVO> createFlow(@Valid @RequestBody FlowManageRequest request) {
        return Result.success(adminService.flowCreate(request));
    }

    @PostMapping("/flow/update")
    public Result<FlowVO> updateFlow(@Valid @RequestBody FlowManageRequest request) {
        return Result.success(adminService.flowUpdate(request));
    }

    @PostMapping("/flow/delete")
    public Result<Void> deleteFlow(@PathParam("flowId") String flowId) {
        adminService.flowDelete(flowId);
        return Result.success();
    }

    @PostMapping("/flow/toggle")
    public Result<FlowVO> flowToggle(@PathParam("flowId") String flowId, @PathParam("flowStatus") Integer flowStatus) {
        return Result.success(adminService.flowToggle(flowId, flowStatus));
    }

    // -------------------- Agent --------------------
    @PostMapping("/agent/page")
    public Result<PageResult<AdminAgentVO>> listAgents(@Valid @RequestBody AgentPageRequest request) {
        return Result.success(adminService.agentPage(request));
    }

    @PostMapping("/agent/insert")
    public Result<AdminAgentVO> createAgent(@Valid @RequestBody AgentManageRequest request) {
        return Result.success(adminService.agentCreate(request));
    }

    @PostMapping("/agent/update")
    public Result<AdminAgentVO> updateAgent(@Valid @RequestBody AgentManageRequest request) {
        return Result.success(adminService.agentUpdate(request));
    }

    @PostMapping("/agent/delete")
    public Result<Void> deleteAgent(@PathParam("agentId") String agentId) {
        adminService.agentDelete(agentId);
        return Result.success();
    }

    @PostMapping("/agent/toggle")
    public Result<AdminAgentVO> agentToggle(@PathParam("agentId") String agentId, @PathParam("agentStatus") Integer agentStatus) {
        return Result.success(adminService.agentToggle(agentId, agentStatus));
    }

    // -------------------- User --------------------
    @PostMapping("/user/page")
    public Result<PageResult<UserAdminVO>> listUsers(@Valid @RequestBody UserPageRequest request) {
        return Result.success(adminService.userPage(request));
    }

    @PostMapping("/user/insert")
    public Result<UserAdminVO> createUser(@Valid @RequestBody UserManageRequest request) {
        return Result.success(adminService.userCreate(request));
    }

    @PostMapping("/user/update")
    public Result<UserAdminVO> updateUser(@Valid @RequestBody UserManageRequest request) {
        return Result.success(adminService.userUpdate(request));
    }

    @PostMapping("/user/delete")
    public Result<Void> deleteUser(@PathParam("userId") String userId) {
        adminService.userDelete(userId);
        return Result.success();
    }

}
