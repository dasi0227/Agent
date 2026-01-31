package com.dasi.trigger.http;

import com.dasi.domain.admin.model.command.*;
import com.dasi.domain.admin.model.query.*;
import com.dasi.domain.admin.model.vo.*;
import com.dasi.domain.admin.service.IAdminService;
import com.dasi.types.dto.result.Result;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @Resource
    private IAdminService adminService;

    // -------------------- API --------------------
    @GetMapping("/apis")
    public Result<PageResult<ApiVO>> listApis(@RequestParam(value = "keyword", required = false) String keyword,
                                              @RequestParam(value = "idKeyword", required = false) String idKeyword,
                                              @RequestParam(value = "page", required = false) Integer page,
                                              @RequestParam(value = "size", required = false) Integer size) {
        ApiQuery query = ApiQuery.builder()
                .keyword(keyword != null ? keyword : (idKeyword))
                .page(page)
                .size(size)
                .build();

        return Result.success(adminService.pageApi(query));
    }

    @PostMapping("/apis")
    public Result<ApiVO> createApi(@RequestBody ApiRequest request) {
        ApiCommand command = ApiCommand.builder()
                .apiId(request.getApiId())
                .apiBaseUrl(request.getApiBaseUrl())
                .apiKey(request.getApiKey())
                .apiCompletionsPath(request.getApiCompletionsPath())
                .apiEmbeddingsPath(request.getApiEmbeddingsPath())
                .apiStatus(request.getApiStatus())
                .build();
        return Result.success(adminService.createApi(command));
    }

    @PutMapping("/apis/{id}")
    public Result<ApiVO> updateApi(@PathVariable("id") Long id, @RequestBody ApiRequest request) {
        ApiCommand command = ApiCommand.builder()
                .id(id)
                .apiId(request.getApiId())
                .apiBaseUrl(request.getApiBaseUrl())
                .apiKey(request.getApiKey())
                .apiCompletionsPath(request.getApiCompletionsPath())
                .apiEmbeddingsPath(request.getApiEmbeddingsPath())
                .apiStatus(request.getApiStatus())
                .build();
        return Result.success(adminService.updateApi(command));
    }

    @DeleteMapping("/apis/{id}")
    public Result<Void> deleteApi(@PathVariable("id") Long id) {
        adminService.deleteApi(id);
        return Result.success();
    }

    @PutMapping("/apis/{id}/status")
    public Result<ApiVO> switchApiStatus(@PathVariable("id") Long id, @RequestBody StatusRequest request) {
        return Result.success(adminService.switchApiStatus(id, request.getStatus()));
    }

    // -------------------- Model --------------------
    @GetMapping("/models")
    public Result<PageResult<ModelVO>> listModels(@RequestParam(value = "keyword", required = false) String keyword,
                                                  @RequestParam(value = "idKeyword", required = false) String idKeyword,
                                                  @RequestParam(value = "nameKeyword", required = false) String nameKeyword,
                                                  @RequestParam(value = "apiId", required = false) String apiId,
                                                  @RequestParam(value = "page", required = false) Integer page,
                                                  @RequestParam(value = "size", required = false) Integer size) {
        ModelQuery query = ModelQuery.builder()
                .keyword(keyword != null ? keyword : firstNonNull(idKeyword, nameKeyword))
                .apiId(apiId)
                .page(page)
                .size(size)
                .build();
        return Result.success(adminService.pageModel(query));
    }

    @PostMapping("/models")
    public Result<ModelVO> createModel(@RequestBody ModelRequest request) {
        ModelCommand command = ModelCommand.builder()
                .modelId(request.getModelId())
                .apiId(request.getApiId())
                .modelName(request.getModelName())
                .modelType(request.getModelType())
                .modelStatus(request.getModelStatus())
                .build();
        return Result.success(adminService.createModel(command));
    }

    @PutMapping("/models/{id}")
    public Result<ModelVO> updateModel(@PathVariable("id") Long id, @RequestBody ModelRequest request) {
        ModelCommand command = ModelCommand.builder()
                .id(id)
                .modelId(request.getModelId())
                .apiId(request.getApiId())
                .modelName(request.getModelName())
                .modelType(request.getModelType())
                .modelStatus(request.getModelStatus())
                .build();
        return Result.success(adminService.updateModel(command));
    }

    @DeleteMapping("/models/{id}")
    public Result<Void> deleteModel(@PathVariable("id") Long id) {
        adminService.deleteModel(id);
        return Result.success();
    }

    @PutMapping("/models/{id}/status")
    public Result<ModelVO> switchModelStatus(@PathVariable("id") Long id, @RequestBody StatusRequest request) {
        return Result.success(adminService.switchModelStatus(id, request.getStatus()));
    }

    // -------------------- MCP --------------------
    @GetMapping("/mcps")
    public Result<PageResult<McpVO>> listMcps(@RequestParam(value = "keyword", required = false) String keyword,
                                              @RequestParam(value = "idKeyword", required = false) String idKeyword,
                                              @RequestParam(value = "nameKeyword", required = false) String nameKeyword,
                                              @RequestParam(value = "page", required = false) Integer page,
                                              @RequestParam(value = "size", required = false) Integer size) {
        McpQuery query = McpQuery.builder()
                .keyword(keyword != null ? keyword : firstNonNull(idKeyword, nameKeyword))
                .page(page)
                .size(size)
                .build();
        return Result.success(adminService.pageMcp(query));
    }

    @PostMapping("/mcps")
    public Result<McpVO> createMcp(@RequestBody McpRequest request) {
        McpCommand command = McpCommand.builder()
                .mcpId(request.getMcpId())
                .mcpName(request.getMcpName())
                .mcpType(request.getMcpType())
                .mcpConfig(request.getMcpConfig())
                .mcpDesc(request.getMcpDesc())
                .mcpTimeout(request.getMcpTimeout())
                .mcpChat(request.getMcpChat())
                .mcpStatus(request.getMcpStatus())
                .build();
        return Result.success(adminService.createMcp(command));
    }

    @PutMapping("/mcps/{id}")
    public Result<McpVO> updateMcp(@PathVariable("id") Long id, @RequestBody McpRequest request) {
        McpCommand command = McpCommand.builder()
                .id(id)
                .mcpId(request.getMcpId())
                .mcpName(request.getMcpName())
                .mcpType(request.getMcpType())
                .mcpConfig(request.getMcpConfig())
                .mcpDesc(request.getMcpDesc())
                .mcpTimeout(request.getMcpTimeout())
                .mcpChat(request.getMcpChat())
                .mcpStatus(request.getMcpStatus())
                .build();
        return Result.success(adminService.updateMcp(command));
    }

    @DeleteMapping("/mcps/{id}")
    public Result<Void> deleteMcp(@PathVariable("id") Long id) {
        adminService.deleteMcp(id);
        return Result.success();
    }

    @PutMapping("/mcps/{id}/status")
    public Result<McpVO> switchMcpStatus(@PathVariable("id") Long id, @RequestBody StatusRequest request) {
        return Result.success(adminService.switchMcpStatus(id, request.getStatus()));
    }

    // -------------------- Advisor --------------------
    @GetMapping("/advisors")
    public Result<PageResult<AdvisorVO>> listAdvisors(@RequestParam(value = "keyword", required = false) String keyword,
                                                      @RequestParam(value = "idKeyword", required = false) String idKeyword,
                                                      @RequestParam(value = "nameKeyword", required = false) String nameKeyword,
                                                      @RequestParam(value = "page", required = false) Integer page,
                                                      @RequestParam(value = "size", required = false) Integer size) {
        AdvisorQuery query = AdvisorQuery.builder()
                .keyword(keyword != null ? keyword : firstNonNull(idKeyword, nameKeyword))
                .page(page)
                .size(size)
                .build();
        return Result.success(adminService.pageAdvisor(query));
    }

    @PostMapping("/advisors")
    public Result<AdvisorVO> createAdvisor(@RequestBody AdvisorRequest request) {
        AdvisorCommand command = AdvisorCommand.builder()
                .advisorId(request.getAdvisorId())
                .advisorName(request.getAdvisorName())
                .advisorType(request.getAdvisorType())
                .advisorDesc(request.getAdvisorDesc())
                .advisorOrder(request.getAdvisorOrder())
                .advisorParam(request.getAdvisorParam())
                .advisorStatus(request.getAdvisorStatus())
                .build();
        return Result.success(adminService.createAdvisor(command));
    }

    @PutMapping("/advisors/{id}")
    public Result<AdvisorVO> updateAdvisor(@PathVariable("id") Long id, @RequestBody AdvisorRequest request) {
        AdvisorCommand command = AdvisorCommand.builder()
                .id(id)
                .advisorId(request.getAdvisorId())
                .advisorName(request.getAdvisorName())
                .advisorType(request.getAdvisorType())
                .advisorDesc(request.getAdvisorDesc())
                .advisorOrder(request.getAdvisorOrder())
                .advisorParam(request.getAdvisorParam())
                .advisorStatus(request.getAdvisorStatus())
                .build();
        return Result.success(adminService.updateAdvisor(command));
    }

    @DeleteMapping("/advisors/{id}")
    public Result<Void> deleteAdvisor(@PathVariable("id") Long id) {
        adminService.deleteAdvisor(id);
        return Result.success();
    }

    @PutMapping("/advisors/{id}/status")
    public Result<AdvisorVO> switchAdvisorStatus(@PathVariable("id") Long id, @RequestBody StatusRequest request) {
        return Result.success(adminService.switchAdvisorStatus(id, request.getStatus()));
    }

    // -------------------- Prompt --------------------
    @GetMapping("/prompts")
    public Result<PageResult<PromptVO>> listPrompts(@RequestParam(value = "keyword", required = false) String keyword,
                                                    @RequestParam(value = "idKeyword", required = false) String idKeyword,
                                                    @RequestParam(value = "nameKeyword", required = false) String nameKeyword,
                                                    @RequestParam(value = "page", required = false) Integer page,
                                                    @RequestParam(value = "size", required = false) Integer size) {
        PromptQuery query = PromptQuery.builder()
                .keyword(keyword != null ? keyword : firstNonNull(idKeyword, nameKeyword))
                .page(page)
                .size(size)
                .build();
        return Result.success(adminService.pagePrompt(query));
    }

    @PostMapping("/prompts")
    public Result<PromptVO> createPrompt(@RequestBody PromptRequest request) {
        PromptCommand command = PromptCommand.builder()
                .promptId(request.getPromptId())
                .promptName(request.getPromptName())
                .promptContent(request.getPromptContent())
                .promptDesc(request.getPromptDesc())
                .promptStatus(request.getPromptStatus())
                .build();
        return Result.success(adminService.createPrompt(command));
    }

    @PutMapping("/prompts/{id}")
    public Result<PromptVO> updatePrompt(@PathVariable("id") Long id, @RequestBody PromptRequest request) {
        PromptCommand command = PromptCommand.builder()
                .id(id)
                .promptId(request.getPromptId())
                .promptName(request.getPromptName())
                .promptContent(request.getPromptContent())
                .promptDesc(request.getPromptDesc())
                .promptStatus(request.getPromptStatus())
                .build();
        return Result.success(adminService.updatePrompt(command));
    }

    @DeleteMapping("/prompts/{id}")
    public Result<Void> deletePrompt(@PathVariable("id") Long id) {
        adminService.deletePrompt(id);
        return Result.success();
    }

    @PutMapping("/prompts/{id}/status")
    public Result<PromptVO> switchPromptStatus(@PathVariable("id") Long id, @RequestBody StatusRequest request) {
        return Result.success(adminService.switchPromptStatus(id, request.getStatus()));
    }

    // -------------------- Client --------------------
    @GetMapping("/clients")
    public Result<PageResult<ClientVO>> listClients(@RequestParam(value = "keyword", required = false) String keyword,
                                                    @RequestParam(value = "idKeyword", required = false) String idKeyword,
                                                    @RequestParam(value = "nameKeyword", required = false) String nameKeyword,
                                                    @RequestParam(value = "modelId", required = false) String modelId,
                                                    @RequestParam(value = "type", required = false) String type,
                                                    @RequestParam(value = "page", required = false) Integer page,
                                                    @RequestParam(value = "size", required = false) Integer size) {
        ClientQuery query = ClientQuery.builder()
                .keyword(keyword != null ? keyword : firstNonNull(idKeyword, nameKeyword))
                .modelId(modelId)
                .clientType(type)
                .page(page)
                .size(size)
                .build();
        return Result.success(adminService.pageClient(query));
    }

    @PostMapping("/clients")
    public Result<ClientVO> createClient(@RequestBody ClientRequest request) {
        ClientCommand command = ClientCommand.builder()
                .clientId(request.getClientId())
                .clientType(request.getClientType())
                .modelId(request.getModelId())
                .modelName(request.getModelName())
                .clientName(request.getClientName())
                .clientDesc(request.getClientDesc())
                .clientStatus(request.getClientStatus())
                .build();
        return Result.success(adminService.createClient(command));
    }

    @PutMapping("/clients/{id}")
    public Result<ClientVO> updateClient(@PathVariable("id") Long id, @RequestBody ClientRequest request) {
        ClientCommand command = ClientCommand.builder()
                .id(id)
                .clientId(request.getClientId())
                .clientType(request.getClientType())
                .modelId(request.getModelId())
                .modelName(request.getModelName())
                .clientName(request.getClientName())
                .clientDesc(request.getClientDesc())
                .clientStatus(request.getClientStatus())
                .build();
        return Result.success(adminService.updateClient(command));
    }

    @DeleteMapping("/clients/{id}")
    public Result<Void> deleteClient(@PathVariable("id") Long id) {
        adminService.deleteClient(id);
        return Result.success();
    }

    @PutMapping("/clients/{id}/status")
    public Result<ClientVO> switchClientStatus(@PathVariable("id") Long id, @RequestBody StatusRequest request) {
        return Result.success(adminService.switchClientStatus(id, request.getStatus()));
    }

    // -------------------- Flow --------------------
    @GetMapping("/flows")
    public Result<PageResult<FlowVO>> listFlows(@RequestParam(value = "agentId", required = false) String agentId,
                                                @RequestParam(value = "clientId", required = false) String clientId,
                                                @RequestParam(value = "status", required = false) Integer status,
                                                @RequestParam(value = "page", required = false) Integer page,
                                                @RequestParam(value = "size", required = false) Integer size) {
        FlowQuery query = FlowQuery.builder()
                .agentId(agentId)
                .clientId(clientId)
                .status(status)
                .page(page)
                .size(size)
                .build();
        return Result.success(adminService.pageFlow(query));
    }

    @PostMapping("/flows")
    public Result<FlowVO> createFlow(@RequestBody FlowRequest request) {
        FlowCommand command = FlowCommand.builder()
                .agentId(request.getAgentId())
                .clientId(request.getClientId())
                .clientType(request.getClientType())
                .flowPrompt(request.getFlowPrompt())
                .flowSeq(request.getFlowSeq())
                .flowStatus(request.getFlowStatus())
                .build();
        return Result.success(adminService.createFlow(command));
    }

    @PutMapping("/flows/{id}")
    public Result<FlowVO> updateFlow(@PathVariable("id") Long id, @RequestBody FlowRequest request) {
        FlowCommand command = FlowCommand.builder()
                .id(id)
                .agentId(request.getAgentId())
                .clientId(request.getClientId())
                .clientType(request.getClientType())
                .flowPrompt(request.getFlowPrompt())
                .flowSeq(request.getFlowSeq())
                .flowStatus(request.getFlowStatus())
                .build();
        return Result.success(adminService.updateFlow(command));
    }

    @DeleteMapping("/flows/{id}")
    public Result<Void> deleteFlow(@PathVariable("id") Long id) {
        adminService.deleteFlow(id);
        return Result.success();
    }

    @PutMapping("/flows/{id}/status")
    public Result<FlowVO> switchFlowStatus(@PathVariable("id") Long id, @RequestBody StatusRequest request) {
        return Result.success(adminService.switchFlowStatus(id, request.getStatus()));
    }

    // -------------------- Agent --------------------
    @GetMapping("/agents")
    public Result<PageResult<AdminAgentVO>> listAgents(@RequestParam(value = "keyword", required = false) String keyword,
                                                       @RequestParam(value = "idKeyword", required = false) String idKeyword,
                                                       @RequestParam(value = "nameKeyword", required = false) String nameKeyword,
                                                       @RequestParam(value = "type", required = false) String type,
                                                       @RequestParam(value = "page", required = false) Integer page,
                                                       @RequestParam(value = "size", required = false) Integer size) {
        AgentQuery query = AgentQuery.builder()
                .keyword(keyword != null ? keyword : firstNonNull(idKeyword, nameKeyword))
                .agentType(type)
                .page(page)
                .size(size)
                .build();
        return Result.success(adminService.pageAgent(query));
    }

    @PostMapping("/agents")
    public Result<AdminAgentVO> createAgent(@RequestBody AgentRequest request) {
        AgentCommand command = AgentCommand.builder()
                .agentId(request.getAgentId())
                .agentName(request.getAgentName())
                .agentType(request.getAgentType())
                .agentDesc(request.getAgentDesc())
                .agentStatus(request.getAgentStatus())
                .build();
        return Result.success(adminService.createAgent(command));
    }

    @PutMapping("/agents/{id}")
    public Result<AdminAgentVO> updateAgent(@PathVariable("id") Long id, @RequestBody AgentRequest request) {
        AgentCommand command = AgentCommand.builder()
                .id(id)
                .agentId(request.getAgentId())
                .agentName(request.getAgentName())
                .agentType(request.getAgentType())
                .agentDesc(request.getAgentDesc())
                .agentStatus(request.getAgentStatus())
                .build();
        return Result.success(adminService.updateAgent(command));
    }

    @DeleteMapping("/agents/{id}")
    public Result<Void> deleteAgent(@PathVariable("id") Long id) {
        adminService.deleteAgent(id);
        return Result.success();
    }

    @PutMapping("/agents/{id}/status")
    public Result<AdminAgentVO> switchAgentStatus(@PathVariable("id") Long id, @RequestBody StatusRequest request) {
        return Result.success(adminService.switchAgentStatus(id, request.getStatus()));
    }

    // -------------------- User --------------------
    @GetMapping("/users")
    public Result<PageResult<UserAdminVO>> listUsers(@RequestParam(value = "username", required = false) String username,
                                                     @RequestParam(value = "role", required = false) String role,
                                                     @RequestParam(value = "page", required = false) Integer page,
                                                     @RequestParam(value = "size", required = false) Integer size) {
        UserQuery query = UserQuery.builder()
                .username(username)
                .role(role)
                .page(page)
                .size(size)
                .build();
        return Result.success(adminService.pageUser(query));
    }

    @PostMapping("/users")
    public Result<UserAdminVO> createUser(@RequestBody UserRequest request) {
        UserCommand command = UserCommand.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .role(request.getRole())
                .build();
        return Result.success(adminService.createUser(command));
    }

    @PutMapping("/users/{id}")
    public Result<UserAdminVO> updateUser(@PathVariable("id") Long id, @RequestBody UserRequest request) {
        UserCommand command = UserCommand.builder()
                .id(id)
                .username(request.getUsername())
                .password(request.getPassword())
                .role(request.getRole())
                .build();
        return Result.success(adminService.updateUser(command));
    }

    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable("id") Long id) {
        adminService.deleteUser(id);
        return Result.success();
    }

    // -------------------- Request DTOs --------------------
    @Data
    private static class ApiRequest {
        private String apiId;
        private String apiBaseUrl;
        private String apiKey;
        private String apiCompletionsPath;
        private String apiEmbeddingsPath;
        private Integer apiStatus;
    }

    @Data
    private static class ModelRequest {
        private String modelId;
        private String apiId;
        private String modelName;
        private String modelType;
        private Integer modelStatus;
    }

    @Data
    private static class McpRequest {
        private String mcpId;
        private String mcpName;
        private String mcpType;
        private String mcpConfig;
        private String mcpDesc;
        private Integer mcpTimeout;
        private Integer mcpChat;
        private Integer mcpStatus;
    }

    @Data
    private static class AdvisorRequest {
        private String advisorId;
        private String advisorName;
        private String advisorType;
        private String advisorDesc;
        private Integer advisorOrder;
        private String advisorParam;
        private Integer advisorStatus;
    }

    @Data
    private static class PromptRequest {
        private String promptId;
        private String promptName;
        private String promptContent;
        private String promptDesc;
        private Integer promptStatus;
    }

    @Data
    private static class ClientRequest {
        private String clientId;
        private String clientType;
        private String modelId;
        private String modelName;
        private String clientName;
        private String clientDesc;
        private Integer clientStatus;
    }

    @Data
    private static class FlowRequest {
        private String agentId;
        private String clientId;
        private String clientType;
        private String flowPrompt;
        private Integer flowSeq;
        private Integer flowStatus;
    }

    @Data
    private static class AgentRequest {
        private String agentId;
        private String agentName;
        private String agentType;
        private String agentDesc;
        private Integer agentStatus;
    }

    @Data
    private static class UserRequest {
        private String username;
        private String password;
        private String role;
    }

    @Data
    private static class StatusRequest {
        private Integer status;
    }

    private String firstNonNull(String a, String b) {
        return a != null && !a.isEmpty() ? a : b;
    }
}
