package com.dasi.domain.admin.service;

import com.dasi.domain.admin.model.*;
import com.dasi.domain.admin.model.command.*;
import com.dasi.domain.admin.model.query.*;
import com.dasi.domain.admin.model.vo.*;
import com.dasi.domain.admin.repository.IAdminRepository;
import com.dasi.domain.login.model.User;
import com.dasi.types.exception.DependencyConflictException;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminService implements IAdminService {

    @Resource
    private IAdminRepository adminRepository;

    @Resource
    private PasswordEncoder passwordEncoder;

    // -------------------- API --------------------
    @Override
    public PageResult<ApiVO> pageApi(ApiQuery query) {
        PageConfig page = resolvePage(query.getPage(), query.getSize());
        List<AdminApi> list = adminRepository.queryApiList(query.getKeyword(), query.getStatus(), page.offset, page.size);
        Long total = adminRepository.countApi(query.getKeyword(), query.getStatus());
        return PageResult.<ApiVO>builder()
                .list(list.stream().map(this::toApiVO).toList())
                .total(total == null ? 0L : total)
                .build();
    }

    @Override
    public ApiVO createApi(ApiCommand command) {
        validateRequired(command.getApiBaseUrl(), "基础路径不能为空");
        validateRequired(command.getApiKey(), "密钥不能为空");
        validateRequired(command.getApiCompletionsPath(), "对话路径不能为空");
        validateRequired(command.getApiEmbeddingsPath(), "嵌入路径不能为空");
        validateRequired(command.getApiId(), "API ID 不能为空");
        String apiId = command.getApiId().trim();
        if (adminRepository.queryApiByApiId(apiId) != null) {
            throw new IllegalArgumentException("接口 ID 已存在");
        }
        AdminApi api = AdminApi.builder()
                .apiId(apiId)
                .apiBaseUrl(command.getApiBaseUrl().trim())
                .apiKey(command.getApiKey().trim())
                .apiCompletionsPath(command.getApiCompletionsPath().trim())
                .apiEmbeddingsPath(command.getApiEmbeddingsPath().trim())
                .apiStatus(command.getApiStatus() == null ? 1 : command.getApiStatus())
                .build();
        adminRepository.insertApi(api);
        return toApiVO(api);
    }

    @Override
    public ApiVO updateApi(ApiCommand command) {
        if (command.getId() == null) {
            throw new IllegalArgumentException("缺少接口 ID");
        }
        AdminApi db = adminRepository.queryApiById(command.getId());
        if (db == null) {
            throw new IllegalArgumentException("接口不存在");
        }
        if (StringUtils.hasText(command.getApiId()) && !command.getApiId().equals(db.getApiId())) {
            if (adminRepository.queryApiByApiId(command.getApiId()) != null) {
                throw new IllegalArgumentException("接口 ID 已存在");
            }
            assertNoDependency("模型", adminRepository.queryModelIdListByApiId(db.getApiId()));
            db.setApiId(command.getApiId().trim());
        }
        if (StringUtils.hasText(command.getApiBaseUrl())) db.setApiBaseUrl(command.getApiBaseUrl().trim());
        if (StringUtils.hasText(command.getApiKey())) db.setApiKey(command.getApiKey().trim());
        if (StringUtils.hasText(command.getApiCompletionsPath())) db.setApiCompletionsPath(command.getApiCompletionsPath().trim());
        if (StringUtils.hasText(command.getApiEmbeddingsPath())) db.setApiEmbeddingsPath(command.getApiEmbeddingsPath().trim());
        if (command.getApiStatus() != null) db.setApiStatus(command.getApiStatus());
        adminRepository.updateApi(db);
        return toApiVO(db);
    }

    @Override
    public void deleteApi(Long id) {
        AdminApi db = adminRepository.queryApiById(id);
        if (db == null) {
            return;
        }
        assertNoDependency("模型", adminRepository.queryModelIdListByApiId(db.getApiId()));
        adminRepository.deleteApi(id);
    }

    @Override
    public ApiVO switchApiStatus(Long id, Integer status) {
        AdminApi db = adminRepository.queryApiById(id);
        if (db == null) {
            throw new IllegalArgumentException("接口不存在");
        }
        if (status != null && status == 0) {
            assertNoDependency("模型", adminRepository.queryModelIdListByApiId(db.getApiId()));
        }
        db.setApiStatus(status);
        adminRepository.updateApi(db);
        return toApiVO(db);
    }

    // -------------------- Model --------------------
    @Override
    public PageResult<ModelVO> pageModel(ModelQuery query) {
        PageConfig page = resolvePage(query.getPage(), query.getSize());
        List<AdminModel> list = adminRepository.queryModelList(query.getKeyword(), query.getApiId(), query.getStatus(), page.offset, page.size);
        Long total = adminRepository.countModel(query.getKeyword(), query.getApiId(), query.getStatus());
        return PageResult.<ModelVO>builder()
                .list(list.stream().map(this::toModelVO).toList())
                .total(total == null ? 0L : total)
                .build();
    }

    @Override
    public ModelVO createModel(ModelCommand command) {
        validateRequired(command.getModelName(), "模型名称不能为空");
        validateRequired(command.getApiId(), "请选择接口");
        validateRequired(command.getModelId(), "模型 ID 不能为空");
        String modelId = command.getModelId().trim();
        if (adminRepository.queryModelByModelId(modelId) != null) {
            throw new IllegalArgumentException("模型 ID 已存在");
        }
        AdminModel model = AdminModel.builder()
                .modelId(modelId)
                .apiId(command.getApiId().trim())
                .modelName(command.getModelName().trim())
                .modelType(StringUtils.hasText(command.getModelType()) ? command.getModelType().trim() : "default")
                .modelStatus(command.getModelStatus() == null ? 1 : command.getModelStatus())
                .build();
        adminRepository.insertModel(model);
        return toModelVO(model);
    }

    @Override
    public ModelVO updateModel(ModelCommand command) {
        if (command.getId() == null) {
            throw new IllegalArgumentException("缺少模型 ID");
        }
        AdminModel db = adminRepository.queryModelById(command.getId());
        if (db == null) {
            throw new IllegalArgumentException("模型不存在");
        }
        if (StringUtils.hasText(command.getModelId()) && !command.getModelId().equals(db.getModelId())) {
            if (adminRepository.queryModelByModelId(command.getModelId()) != null) {
                throw new IllegalArgumentException("模型 ID 已存在");
            }
            assertNoDependency("客户端", adminRepository.queryClientIdListByModelId(db.getModelId()));
            db.setModelId(command.getModelId().trim());
        }
        if (StringUtils.hasText(command.getApiId())) db.setApiId(command.getApiId().trim());
        if (StringUtils.hasText(command.getModelName())) db.setModelName(command.getModelName().trim());
        if (StringUtils.hasText(command.getModelType())) db.setModelType(command.getModelType().trim());
        if (command.getModelStatus() != null) db.setModelStatus(command.getModelStatus());
        adminRepository.updateModel(db);
        return toModelVO(db);
    }

    @Override
    public void deleteModel(Long id) {
        AdminModel db = adminRepository.queryModelById(id);
        if (db == null) return;
        assertNoDependency("客户端", adminRepository.queryClientIdListByModelId(db.getModelId()));
        adminRepository.deleteModel(id);
    }

    @Override
    public ModelVO switchModelStatus(Long id, Integer status) {
        AdminModel db = adminRepository.queryModelById(id);
        if (db == null) throw new IllegalArgumentException("模型不存在");
        if (status != null && status == 0) {
            assertNoDependency("客户端", adminRepository.queryClientIdListByModelId(db.getModelId()));
        }
        db.setModelStatus(status);
        adminRepository.updateModel(db);
        return toModelVO(db);
    }

    // -------------------- MCP --------------------
    @Override
    public PageResult<McpVO> pageMcp(McpQuery query) {
        PageConfig page = resolvePage(query.getPage(), query.getSize());
        List<AdminMcp> list = adminRepository.queryMcpList(query.getKeyword(), query.getMcpType(), query.getStatus(), page.offset, page.size);
        Long total = adminRepository.countMcp(query.getKeyword(), query.getMcpType(), query.getStatus());
        return PageResult.<McpVO>builder()
                .list(list.stream().map(this::toMcpVO).toList())
                .total(total == null ? 0L : total)
                .build();
    }

    @Override
    public McpVO createMcp(McpCommand command) {
        validateRequired(command.getMcpName(), "工具名称不能为空");
        validateRequired(command.getMcpType(), "工具类型不能为空");
        validateRequired(command.getMcpConfig(), "工具配置不能为空");
        validateRequired(command.getMcpId(), "MCP ID 不能为空");
        String mcpId = command.getMcpId().trim();
        if (adminRepository.queryMcpByMcpId(mcpId) != null) {
            throw new IllegalArgumentException("工具 ID 已存在");
        }
        AdminMcp mcp = AdminMcp.builder()
                .mcpId(mcpId)
                .mcpName(command.getMcpName().trim())
                .mcpType(command.getMcpType().trim())
                .mcpConfig(command.getMcpConfig())
                .mcpDesc(StringUtils.hasText(command.getMcpDesc()) ? command.getMcpDesc().trim() : "")
                .mcpTimeout(command.getMcpTimeout() == null ? 180 : command.getMcpTimeout())
                .mcpChat(command.getMcpChat() == null ? 0 : command.getMcpChat())
                .mcpStatus(command.getMcpStatus() == null ? 1 : command.getMcpStatus())
                .build();
        adminRepository.insertMcp(mcp);
        return toMcpVO(mcp);
    }

    @Override
    public McpVO updateMcp(McpCommand command) {
        if (command.getId() == null) {
            throw new IllegalArgumentException("缺少工具 ID");
        }
        AdminMcp db = adminRepository.queryMcpById(command.getId());
        if (db == null) throw new IllegalArgumentException("工具不存在");
        if (StringUtils.hasText(command.getMcpId()) && !command.getMcpId().equals(db.getMcpId())) {
            if (adminRepository.queryMcpByMcpId(command.getMcpId()) != null) {
                throw new IllegalArgumentException("工具 ID 已存在");
            }
            assertNoDependency("客户端", adminRepository.queryClientIdListByMcpId(db.getMcpId()));
            db.setMcpId(command.getMcpId().trim());
        }
        if (StringUtils.hasText(command.getMcpName())) db.setMcpName(command.getMcpName().trim());
        if (StringUtils.hasText(command.getMcpType())) db.setMcpType(command.getMcpType().trim());
        if (command.getMcpConfig() != null) db.setMcpConfig(command.getMcpConfig());
        if (command.getMcpDesc() != null) db.setMcpDesc(command.getMcpDesc());
        if (command.getMcpTimeout() != null) db.setMcpTimeout(command.getMcpTimeout());
        if (command.getMcpChat() != null) db.setMcpChat(command.getMcpChat());
        if (command.getMcpStatus() != null) db.setMcpStatus(command.getMcpStatus());
        adminRepository.updateMcp(db);
        return toMcpVO(db);
    }

    @Override
    public void deleteMcp(Long id) {
        AdminMcp db = adminRepository.queryMcpById(id);
        if (db == null) return;
        assertNoDependency("客户端", adminRepository.queryClientIdListByMcpId(db.getMcpId()));
        adminRepository.deleteMcp(id);
    }

    @Override
    public McpVO switchMcpStatus(Long id, Integer status) {
        AdminMcp db = adminRepository.queryMcpById(id);
        if (db == null) throw new IllegalArgumentException("工具不存在");
        if (status != null && status == 0) {
            assertNoDependency("客户端", adminRepository.queryClientIdListByMcpId(db.getMcpId()));
        }
        db.setMcpStatus(status);
        adminRepository.updateMcp(db);
        return toMcpVO(db);
    }

    // -------------------- Advisor --------------------
    @Override
    public PageResult<AdvisorVO> pageAdvisor(AdvisorQuery query) {
        PageConfig page = resolvePage(query.getPage(), query.getSize());
        List<AdminAdvisor> list = adminRepository.queryAdvisorList(query.getKeyword(), query.getAdvisorType(), query.getStatus(), page.offset, page.size);
        Long total = adminRepository.countAdvisor(query.getKeyword(), query.getAdvisorType(), query.getStatus());
        return PageResult.<AdvisorVO>builder()
                .list(list.stream().map(this::toAdvisorVO).toList())
                .total(total == null ? 0L : total)
                .build();
    }

    @Override
    public AdvisorVO createAdvisor(AdvisorCommand command) {
        validateRequired(command.getAdvisorName(), "顾问名称不能为空");
        validateRequired(command.getAdvisorType(), "顾问类型不能为空");
        validateRequired(command.getAdvisorId(), "顾问 ID 不能为空");
        String advisorId = command.getAdvisorId().trim();
        if (adminRepository.queryAdvisorByAdvisorId(advisorId) != null) {
            throw new IllegalArgumentException("顾问 ID 已存在");
        }
        AdminAdvisor advisor = AdminAdvisor.builder()
                .advisorId(advisorId)
                .advisorName(command.getAdvisorName().trim())
                .advisorType(command.getAdvisorType().trim())
                .advisorDesc(command.getAdvisorDesc())
                .advisorOrder(command.getAdvisorOrder() == null ? 0 : command.getAdvisorOrder())
                .advisorParam(command.getAdvisorParam())
                .advisorStatus(command.getAdvisorStatus() == null ? 1 : command.getAdvisorStatus())
                .build();
        adminRepository.insertAdvisor(advisor);
        return toAdvisorVO(advisor);
    }

    @Override
    public AdvisorVO updateAdvisor(AdvisorCommand command) {
        if (command.getId() == null) {
            throw new IllegalArgumentException("缺少顾问 ID");
        }
        AdminAdvisor db = adminRepository.queryAdvisorById(command.getId());
        if (db == null) throw new IllegalArgumentException("顾问不存在");
        if (StringUtils.hasText(command.getAdvisorId()) && !command.getAdvisorId().equals(db.getAdvisorId())) {
            if (adminRepository.queryAdvisorByAdvisorId(command.getAdvisorId()) != null) {
                throw new IllegalArgumentException("顾问 ID 已存在");
            }
            assertNoDependency("客户端", adminRepository.queryClientIdListByAdvisorId(db.getAdvisorId()));
            db.setAdvisorId(command.getAdvisorId().trim());
        }
        if (StringUtils.hasText(command.getAdvisorName())) db.setAdvisorName(command.getAdvisorName().trim());
        if (StringUtils.hasText(command.getAdvisorType())) db.setAdvisorType(command.getAdvisorType().trim());
        if (command.getAdvisorDesc() != null) db.setAdvisorDesc(command.getAdvisorDesc());
        if (command.getAdvisorOrder() != null) db.setAdvisorOrder(command.getAdvisorOrder());
        if (command.getAdvisorParam() != null) db.setAdvisorParam(command.getAdvisorParam());
        if (command.getAdvisorStatus() != null) db.setAdvisorStatus(command.getAdvisorStatus());
        adminRepository.updateAdvisor(db);
        return toAdvisorVO(db);
    }

    @Override
    public void deleteAdvisor(Long id) {
        AdminAdvisor db = adminRepository.queryAdvisorById(id);
        if (db == null) return;
        assertNoDependency("客户端", adminRepository.queryClientIdListByAdvisorId(db.getAdvisorId()));
        adminRepository.deleteAdvisor(id);
    }

    @Override
    public AdvisorVO switchAdvisorStatus(Long id, Integer status) {
        AdminAdvisor db = adminRepository.queryAdvisorById(id);
        if (db == null) throw new IllegalArgumentException("顾问不存在");
        if (status != null && status == 0) {
            assertNoDependency("客户端", adminRepository.queryClientIdListByAdvisorId(db.getAdvisorId()));
        }
        db.setAdvisorStatus(status);
        adminRepository.updateAdvisor(db);
        return toAdvisorVO(db);
    }

    // -------------------- Prompt --------------------
    @Override
    public PageResult<PromptVO> pagePrompt(PromptQuery query) {
        PageConfig page = resolvePage(query.getPage(), query.getSize());
        List<AdminPrompt> list = adminRepository.queryPromptList(query.getKeyword(), query.getStatus(), page.offset, page.size);
        Long total = adminRepository.countPrompt(query.getKeyword(), query.getStatus());
        return PageResult.<PromptVO>builder()
                .list(list.stream().map(this::toPromptVO).toList())
                .total(total == null ? 0L : total)
                .build();
    }

    @Override
    public PromptVO createPrompt(PromptCommand command) {
        validateRequired(command.getPromptName(), "提示词名称不能为空");
        validateRequired(command.getPromptContent(), "提示词内容不能为空");
        validateRequired(command.getPromptId(), "提示词 ID 不能为空");
        String promptId = command.getPromptId().trim();
        if (adminRepository.queryPromptByPromptId(promptId) != null) {
            throw new IllegalArgumentException("提示词 ID 已存在");
        }
        AdminPrompt prompt = AdminPrompt.builder()
                .promptId(promptId)
                .promptName(command.getPromptName().trim())
                .promptContent(command.getPromptContent())
                .promptDesc(command.getPromptDesc())
                .promptStatus(command.getPromptStatus() == null ? 1 : command.getPromptStatus())
                .build();
        adminRepository.insertPrompt(prompt);
        return toPromptVO(prompt);
    }

    @Override
    public PromptVO updatePrompt(PromptCommand command) {
        if (command.getId() == null) {
            throw new IllegalArgumentException("缺少提示词 ID");
        }
        AdminPrompt db = adminRepository.queryPromptById(command.getId());
        if (db == null) throw new IllegalArgumentException("提示词不存在");
        if (StringUtils.hasText(command.getPromptId()) && !command.getPromptId().equals(db.getPromptId())) {
            if (adminRepository.queryPromptByPromptId(command.getPromptId()) != null) {
                throw new IllegalArgumentException("提示词 ID 已存在");
            }
            assertNoDependency("客户端", adminRepository.queryClientIdListByPromptId(db.getPromptId()));
            db.setPromptId(command.getPromptId().trim());
        }
        if (StringUtils.hasText(command.getPromptName())) db.setPromptName(command.getPromptName().trim());
        if (command.getPromptContent() != null) db.setPromptContent(command.getPromptContent());
        if (command.getPromptDesc() != null) db.setPromptDesc(command.getPromptDesc());
        if (command.getPromptStatus() != null) db.setPromptStatus(command.getPromptStatus());
        adminRepository.updatePrompt(db);
        return toPromptVO(db);
    }

    @Override
    public void deletePrompt(Long id) {
        AdminPrompt db = adminRepository.queryPromptById(id);
        if (db == null) return;
        assertNoDependency("客户端", adminRepository.queryClientIdListByPromptId(db.getPromptId()));
        adminRepository.deletePrompt(id);
    }

    @Override
    public PromptVO switchPromptStatus(Long id, Integer status) {
        AdminPrompt db = adminRepository.queryPromptById(id);
        if (db == null) throw new IllegalArgumentException("提示词不存在");
        if (status != null && status == 0) {
            assertNoDependency("客户端", adminRepository.queryClientIdListByPromptId(db.getPromptId()));
        }
        db.setPromptStatus(status);
        adminRepository.updatePrompt(db);
        return toPromptVO(db);
    }

    // -------------------- Client --------------------
    @Override
    public PageResult<ClientVO> pageClient(ClientQuery query) {
        PageConfig page = resolvePage(query.getPage(), query.getSize());
        List<AdminClient> list = adminRepository.queryClientList(query.getKeyword(), query.getClientType(), query.getModelId(), query.getStatus(), page.offset, page.size);
        Long total = adminRepository.countClient(query.getKeyword(), query.getClientType(), query.getModelId(), query.getStatus());
        return PageResult.<ClientVO>builder()
                .list(list.stream().map(this::toClientVO).toList())
                .total(total == null ? 0L : total)
                .build();
    }

    @Override
    public ClientVO createClient(ClientCommand command) {
        validateRequired(command.getClientName(), "客户端名称不能为空");
        validateRequired(command.getModelId(), "请选择模型");
        validateRequired(command.getClientType(), "客户端类型不能为空");
        validateRequired(command.getClientId(), "客户端 ID 不能为空");
        String clientId = command.getClientId().trim();
        if (adminRepository.queryClientByClientId(clientId) != null) {
            throw new IllegalArgumentException("客户端 ID 已存在");
        }
        AdminClient client = AdminClient.builder()
                .clientId(clientId)
                .clientType(command.getClientType().trim())
                .modelId(command.getModelId().trim())
                .modelName(StringUtils.hasText(command.getModelName()) ? command.getModelName().trim() : "")
                .clientName(command.getClientName().trim())
                .clientDesc(StringUtils.hasText(command.getClientDesc()) ? command.getClientDesc().trim() : "")
                .clientStatus(command.getClientStatus() == null ? 1 : command.getClientStatus())
                .build();
        adminRepository.insertClient(client);
        return toClientVO(client);
    }

    @Override
    public ClientVO updateClient(ClientCommand command) {
        if (command.getId() == null) {
            throw new IllegalArgumentException("缺少客户端 ID");
        }
        AdminClient db = adminRepository.queryClientById(command.getId());
        if (db == null) throw new IllegalArgumentException("客户端不存在");
        if (StringUtils.hasText(command.getClientId()) && !command.getClientId().equals(db.getClientId())) {
            if (adminRepository.queryClientByClientId(command.getClientId()) != null) {
                throw new IllegalArgumentException("客户端 ID 已存在");
            }
            assertNoDependency("工作流", extractFlowDependents(adminRepository.queryFlowListByClientId(db.getClientId())));
            db.setClientId(command.getClientId().trim());
        }
        if (StringUtils.hasText(command.getClientType())) db.setClientType(command.getClientType().trim());
        if (StringUtils.hasText(command.getModelId())) db.setModelId(command.getModelId().trim());
        if (StringUtils.hasText(command.getModelName())) db.setModelName(command.getModelName().trim());
        if (StringUtils.hasText(command.getClientName())) db.setClientName(command.getClientName().trim());
        if (command.getClientDesc() != null) db.setClientDesc(command.getClientDesc());
        if (command.getClientStatus() != null) db.setClientStatus(command.getClientStatus());
        adminRepository.updateClient(db);
        return toClientVO(db);
    }

    @Override
    public void deleteClient(Long id) {
        AdminClient db = adminRepository.queryClientById(id);
        if (db == null) return;
        assertNoDependency("工作流", extractFlowDependents(adminRepository.queryFlowListByClientId(db.getClientId())));
        adminRepository.deleteClient(id);
    }

    @Override
    public ClientVO switchClientStatus(Long id, Integer status) {
        AdminClient db = adminRepository.queryClientById(id);
        if (db == null) throw new IllegalArgumentException("客户端不存在");
        if (status != null && status == 0) {
            assertNoDependency("工作流", extractFlowDependents(adminRepository.queryFlowListByClientId(db.getClientId())));
        }
        db.setClientStatus(status);
        adminRepository.updateClient(db);
        return toClientVO(db);
    }

    // -------------------- Flow --------------------
    @Override
    public PageResult<FlowVO> pageFlow(FlowQuery query) {
        PageConfig page = resolvePage(query.getPage(), query.getSize());
        List<AdminFlow> list = adminRepository.queryFlowList(query.getAgentId(), query.getClientId(), query.getStatus(), page.offset, page.size);
        Long total = adminRepository.countFlow(query.getAgentId(), query.getClientId(), query.getStatus());
        return PageResult.<FlowVO>builder()
                .list(list.stream().map(this::toFlowVO).toList())
                .total(total == null ? 0L : total)
                .build();
    }

    @Override
    public FlowVO createFlow(FlowCommand command) {
        validateRequired(command.getAgentId(), "请选择智能体");
        validateRequired(command.getClientId(), "请选择客户端");
        if (command.getFlowSeq() == null) {
            throw new IllegalArgumentException("请设置顺序");
        }
        AdminFlow flow = AdminFlow.builder()
                .agentId(command.getAgentId().trim())
                .clientId(command.getClientId().trim())
                .clientType(StringUtils.hasText(command.getClientType()) ? command.getClientType().trim() : "")
                .flowPrompt(command.getFlowPrompt() == null ? "" : command.getFlowPrompt())
                .flowSeq(command.getFlowSeq())
                .flowStatus(command.getFlowStatus() == null ? 1 : command.getFlowStatus())
                .build();
        adminRepository.insertFlow(flow);
        return toFlowVO(flow);
    }

    @Override
    public FlowVO updateFlow(FlowCommand command) {
        if (command.getId() == null) {
            throw new IllegalArgumentException("缺少工作流 ID");
        }
        AdminFlow db = adminRepository.queryFlowById(command.getId());
        if (db == null) throw new IllegalArgumentException("工作流不存在");
        if (StringUtils.hasText(command.getAgentId())) db.setAgentId(command.getAgentId().trim());
        if (StringUtils.hasText(command.getClientId())) db.setClientId(command.getClientId().trim());
        if (StringUtils.hasText(command.getClientType())) db.setClientType(command.getClientType().trim());
        if (command.getFlowPrompt() != null) db.setFlowPrompt(command.getFlowPrompt());
        if (command.getFlowSeq() != null) db.setFlowSeq(command.getFlowSeq());
        if (command.getFlowStatus() != null) db.setFlowStatus(command.getFlowStatus());
        adminRepository.updateFlow(db);
        return toFlowVO(db);
    }

    @Override
    public void deleteFlow(Long id) {
        adminRepository.deleteFlow(id);
    }

    @Override
    public FlowVO switchFlowStatus(Long id, Integer status) {
        AdminFlow db = adminRepository.queryFlowById(id);
        if (db == null) throw new IllegalArgumentException("工作流不存在");
        db.setFlowStatus(status);
        adminRepository.updateFlow(db);
        return toFlowVO(db);
    }

    // -------------------- Agent --------------------
    @Override
    public PageResult<AdminAgentVO> pageAgent(AgentQuery query) {
        PageConfig page = resolvePage(query.getPage(), query.getSize());
        List<AdminAgent> agentList = adminRepository.queryAgentList(query.getKeyword(), query.getAgentStatus(), query.getAgentType(), page.offset, page.size);
        Long total = adminRepository.countAgent(query.getKeyword(), query.getAgentStatus(), query.getAgentType());
        List<AdminAgentVO> voList = agentList.stream().map(this::toAgentVO).toList();
        return PageResult.<AdminAgentVO>builder()
                .list(voList)
                .total(total == null ? 0L : total)
                .build();
    }

    @Override
    public AdminAgentVO createAgent(AgentCommand command) {
        validateRequired(command.getAgentName(), "智能体名称不能为空");
        validateRequired(command.getAgentId(), "智能体 ID 不能为空");
        String agentId = command.getAgentId().trim();
        if (adminRepository.queryAgentByAgentId(agentId) != null) {
            throw new IllegalArgumentException("智能体 ID 已存在");
        }
        AdminAgent agent = AdminAgent.builder()
                .agentId(agentId)
                .agentName(command.getAgentName().trim())
                .agentType(StringUtils.hasText(command.getAgentType()) ? command.getAgentType().trim() : "loop")
                .agentDesc(StringUtils.hasText(command.getAgentDesc()) ? command.getAgentDesc().trim() : "")
                .agentStatus(command.getAgentStatus() == null ? 1 : command.getAgentStatus())
                .build();
        adminRepository.insertAgent(agent);
        return toAgentVO(agent);
    }

    @Override
    public AdminAgentVO updateAgent(AgentCommand command) {
        if (command.getId() == null) {
            throw new IllegalArgumentException("缺少智能体 ID");
        }
        AdminAgent dbAgent = adminRepository.queryAgentById(command.getId());
        if (dbAgent == null) {
            throw new IllegalArgumentException("智能体不存在");
        }

        if (StringUtils.hasText(command.getAgentId()) && !command.getAgentId().equals(dbAgent.getAgentId())) {
            AdminAgent exists = adminRepository.queryAgentByAgentId(command.getAgentId());
            if (exists != null && !exists.getId().equals(dbAgent.getId())) {
                throw new IllegalArgumentException("智能体 ID 已存在");
            }
            assertNoDependency("工作流", extractFlowDependents(adminRepository.queryFlowListByAgentId(dbAgent.getAgentId())));
            dbAgent.setAgentId(command.getAgentId().trim());
        }
        if (StringUtils.hasText(command.getAgentName())) {
            dbAgent.setAgentName(command.getAgentName().trim());
        }
        if (StringUtils.hasText(command.getAgentType())) {
            dbAgent.setAgentType(command.getAgentType().trim());
        }
        if (command.getAgentDesc() != null) {
            dbAgent.setAgentDesc(command.getAgentDesc().trim());
        }
        if (command.getAgentStatus() != null) {
            dbAgent.setAgentStatus(command.getAgentStatus());
        }

        adminRepository.updateAgent(dbAgent);
        return toAgentVO(dbAgent);
    }

    @Override
    public void deleteAgent(Long id) {
        AdminAgent db = adminRepository.queryAgentById(id);
        if (db == null) return;
        assertNoDependency("工作流", extractFlowDependents(adminRepository.queryFlowListByAgentId(db.getAgentId())));
        adminRepository.deleteAgent(id);
    }

    @Override
    public AdminAgentVO switchAgentStatus(Long id, Integer status) {
        AdminAgent db = adminRepository.queryAgentById(id);
        if (db == null) throw new IllegalArgumentException("智能体不存在");
        if (status != null && status == 0) {
            assertNoDependency("工作流", extractFlowDependents(adminRepository.queryFlowListByAgentId(db.getAgentId())));
        }
        db.setAgentStatus(status);
        adminRepository.updateAgent(db);
        return toAgentVO(db);
    }

    // -------------------- User --------------------
    @Override
    public PageResult<UserAdminVO> pageUser(UserQuery query) {
        PageConfig page = resolvePage(query.getPage(), query.getSize());
        List<User> userList = adminRepository.queryUserList(query.getUsername(), query.getRole(), page.offset, page.size);
        Long total = adminRepository.countUser(query.getUsername(), query.getRole());

        List<UserAdminVO> voList = userList.stream().map(this::toUserAdminVO).toList();
        return PageResult.<UserAdminVO>builder()
                .list(voList)
                .total(total == null ? 0L : total)
                .build();
    }

    @Override
    public UserAdminVO createUser(UserCommand command) {
        if (!StringUtils.hasText(command.getUsername()) || !StringUtils.hasText(command.getPassword())) {
            throw new IllegalArgumentException("用户名或密码不能为空");
        }
        String username = command.getUsername().trim();
        if (adminRepository.queryUserByUsername(username) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(command.getPassword()))
                .role(StringUtils.hasText(command.getRole()) ? command.getRole().trim() : "account")
                .build();
        adminRepository.insertUser(user);
        return toUserAdminVO(user);
    }

    @Override
    public UserAdminVO updateUser(UserCommand command) {
        if (command.getId() == null) {
            throw new IllegalArgumentException("缺少用户 ID");
        }
        User user = adminRepository.queryUserById(command.getId());
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (StringUtils.hasText(command.getUsername()) && !command.getUsername().equals(user.getUsername())) {
            if (adminRepository.queryUserByUsername(command.getUsername()) != null) {
                throw new IllegalArgumentException("用户名已存在");
            }
            user.setUsername(command.getUsername().trim());
        }
        if (StringUtils.hasText(command.getPassword())) {
            user.setPassword(passwordEncoder.encode(command.getPassword()));
        }
        if (StringUtils.hasText(command.getRole())) {
            user.setRole(command.getRole().trim());
        }
        adminRepository.updateUser(user);
        return toUserAdminVO(user);
    }

    @Override
    public void deleteUser(Long id) {
        if (id == null) {
            return;
        }
        adminRepository.deleteUser(id);
    }

    // -------------------- helper --------------------
    private void validateRequired(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
    }

    private void assertNoDependency(String dependentName, List<String> dependents) {
        if (!CollectionUtils.isEmpty(dependents)) {
            throw new DependencyConflictException("存在依赖，无法执行操作，相关" + dependentName + "：" + String.join(",", dependents), dependents);
        }
    }

    private PageConfig resolvePage(Integer page, Integer size) {
        int p = page == null || page <= 0 ? 1 : page;
        int s = size == null || size <= 0 ? 10 : size;
        return new PageConfig((p - 1) * s, s);
    }

    private AdminAgentVO toAgentVO(AdminAgent agent) {
        return AdminAgentVO.builder()
                .id(agent.getId())
                .agentId(agent.getAgentId())
                .agentName(agent.getAgentName())
                .agentType(agent.getAgentType())
                .agentDesc(agent.getAgentDesc())
                .agentStatus(agent.getAgentStatus())
                .createTime(agent.getCreateTime())
                .updateTime(agent.getUpdateTime())
                .build();
    }

    private ApiVO toApiVO(AdminApi api) {
        return ApiVO.builder()
                .id(api.getId())
                .apiId(api.getApiId())
                .apiBaseUrl(api.getApiBaseUrl())
                .apiKey(api.getApiKey())
                .apiCompletionsPath(api.getApiCompletionsPath())
                .apiEmbeddingsPath(api.getApiEmbeddingsPath())
                .apiStatus(api.getApiStatus())
                .createTime(api.getCreateTime())
                .updateTime(api.getUpdateTime())
                .build();
    }

    private ModelVO toModelVO(AdminModel model) {
        return ModelVO.builder()
                .id(model.getId())
                .modelId(model.getModelId())
                .apiId(model.getApiId())
                .modelName(model.getModelName())
                .modelType(model.getModelType())
                .modelStatus(model.getModelStatus())
                .createTime(model.getCreateTime())
                .updateTime(model.getUpdateTime())
                .build();
    }

    private McpVO toMcpVO(AdminMcp mcp) {
        return McpVO.builder()
                .id(mcp.getId())
                .mcpId(mcp.getMcpId())
                .mcpName(mcp.getMcpName())
                .mcpType(mcp.getMcpType())
                .mcpConfig(mcp.getMcpConfig())
                .mcpDesc(mcp.getMcpDesc())
                .mcpTimeout(mcp.getMcpTimeout())
                .mcpChat(mcp.getMcpChat())
                .mcpStatus(mcp.getMcpStatus())
                .createTime(mcp.getCreateTime())
                .updateTime(mcp.getUpdateTime())
                .build();
    }

    private AdvisorVO toAdvisorVO(AdminAdvisor advisor) {
        return AdvisorVO.builder()
                .id(advisor.getId())
                .advisorId(advisor.getAdvisorId())
                .advisorName(advisor.getAdvisorName())
                .advisorType(advisor.getAdvisorType())
                .advisorDesc(advisor.getAdvisorDesc())
                .advisorOrder(advisor.getAdvisorOrder())
                .advisorParam(advisor.getAdvisorParam())
                .advisorStatus(advisor.getAdvisorStatus())
                .createTime(advisor.getCreateTime())
                .updateTime(advisor.getUpdateTime())
                .build();
    }

    private PromptVO toPromptVO(AdminPrompt prompt) {
        return PromptVO.builder()
                .id(prompt.getId())
                .promptId(prompt.getPromptId())
                .promptName(prompt.getPromptName())
                .promptContent(prompt.getPromptContent())
                .promptDesc(prompt.getPromptDesc())
                .promptStatus(prompt.getPromptStatus())
                .createTime(prompt.getCreateTime())
                .updateTime(prompt.getUpdateTime())
                .build();
    }

    private ClientVO toClientVO(AdminClient client) {
        return ClientVO.builder()
                .id(client.getId())
                .clientId(client.getClientId())
                .clientType(client.getClientType())
                .modelId(client.getModelId())
                .modelName(client.getModelName())
                .clientName(client.getClientName())
                .clientDesc(client.getClientDesc())
                .clientStatus(client.getClientStatus())
                .createTime(client.getCreateTime())
                .updateTime(client.getUpdateTime())
                .build();
    }

    private FlowVO toFlowVO(AdminFlow flow) {
        return FlowVO.builder()
                .id(flow.getId())
                .agentId(flow.getAgentId())
                .clientId(flow.getClientId())
                .clientType(flow.getClientType())
                .flowPrompt(flow.getFlowPrompt())
                .flowSeq(flow.getFlowSeq())
                .flowStatus(flow.getFlowStatus())
                .createTime(flow.getCreateTime())
                .updateTime(flow.getUpdateTime())
                .build();
    }

    private UserAdminVO toUserAdminVO(User user) {
        return UserAdminVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .createTime(user.getCreateTime())
                .build();
    }

    private String buildId(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private List<String> extractFlowDependents(List<AdminFlow> flows) {
        if (CollectionUtils.isEmpty(flows)) {
            return List.of();
        }
        return flows.stream()
                .map(flow -> flow.getAgentId() + "->" + flow.getClientId())
                .collect(Collectors.toList());
    }

    private record PageConfig(int offset, int size) {
    }
}
