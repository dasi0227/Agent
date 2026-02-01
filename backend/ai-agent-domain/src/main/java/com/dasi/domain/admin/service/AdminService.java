package com.dasi.domain.admin.service;

import com.dasi.domain.admin.model.*;
import com.dasi.domain.admin.model.vo.*;
import com.dasi.domain.admin.repository.IAdminRepository;
import com.dasi.domain.login.model.User;
import com.dasi.types.dto.request.admin.*;
import com.dasi.types.dto.result.PageResult;
import com.dasi.types.exception.AdminException;
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
    public PageResult<ApiVO> apiPage(ApiPageRequest apiPageRequest) {
        List<ApiVO> apiVOList = adminRepository.apiPage(apiPageRequest);
        Integer pageSum = adminRepository.apiCount(apiPageRequest);
        return PageResult.<ApiVO>builder()
                .list(apiVOList)
                .pageSum(pageSum)
                .pageNum(apiPageRequest.getPageNum())
                .pageSize(apiPageRequest.getPageSize())
                .build();
    }

    @Override
    public void apiInsert(ApiManageRequest apiManageRequest) {
        ApiVO apiVO = adminRepository.apiQuery(apiManageRequest.getApiId());
        if (apiVO != null) {
            throw new AdminException("API 的 ID 已存在，请修改后重新添加");
        }
        adminRepository.apiInsert(apiManageRequest);
    }

    @Override
    public void apiUpdate(ApiManageRequest apiManageRequest) {
        ApiVO apiVO = adminRepository.apiQuery(apiManageRequest.getApiId());
        if (apiVO == null) {
            throw new AdminException("API 的 ID 不存在，请确认后重新提交");
        }
        adminRepository.apiUpdate(apiManageRequest);
    }

    @Override
    public void apiDelete(String apiId) {
        ApiVO apiVO = adminRepository.apiQuery(apiId);
        if (apiVO == null) {
            throw new AdminException("API 的 ID 不存在，请确认后重新删除");
        }
        assertNoDependency("模型", adminRepository.queryModelIdListByApiId(apiId));
        adminRepository.apiDelete(apiId);
    }

    @Override
    public void apiToggle(String apiId, Integer apiStatus) {
        ApiVO apiVO = adminRepository.apiQuery(apiId);
        if (apiVO == null) {
            throw new AdminException("API 的 ID 不存在，请确认后重新切换");
        }
        assertNoDependency("模型", adminRepository.queryModelIdListByApiId(apiId));
        adminRepository.apiToggle(apiId, apiStatus);
    }

    // -------------------- Model --------------------
    @Override
    public PageResult<ModelVO> modelPage(ModelPageRequest request) {
        PageConfig page = resolvePage(request.getPageNum(), request.getPageSize());
        String keyword = request.getKeyword() != null ? request.getKeyword() : firstNonNull(request.getIdKeyword(), request.getNameKeyword());
        List<AdminModel> list = adminRepository.queryModelList(keyword, request.getApiId(), null, page.getOffset(), page.getSize());
        Long total = adminRepository.countModel(keyword, request.getApiId(), null);
        return PageResult.<ModelVO>builder()
                .list(list.stream().map(this::toModelVO).toList())
                .pageNum(page.getPage())
                .pageSize(page.getSize())
                .pageSum(total == null ? 0 : total.intValue())
                .build();
    }

    @Override
    public ModelVO modelCreate(ModelManageRequest request) {
        String modelId = request.getModelId().trim();
        if (adminRepository.queryModelByModelId(modelId) != null) {
            throw new IllegalArgumentException("模型 ID 已存在");
        }
        request.setModelId(modelId);
        request.setModelStatus(request.getModelStatus() == null ? 1 : request.getModelStatus());
        adminRepository.insertModel(request);
        AdminModel saved = adminRepository.queryModelByModelId(modelId);
        return toModelVO(saved);
    }

    @Override
    public ModelVO modelUpdate(ModelManageRequest request) {
        if (request.getId() == null) {
            throw new IllegalArgumentException("缺少模型 ID");
        }
        AdminModel db = adminRepository.queryModelById(request.getId());
        if (db == null) {
            throw new IllegalArgumentException("模型不存在");
        }
        if (StringUtils.hasText(request.getModelId()) && !request.getModelId().equals(db.getModelId())) {
            if (adminRepository.queryModelByModelId(request.getModelId()) != null) {
                throw new IllegalArgumentException("模型 ID 已存在");
            }
            assertNoDependency("客户端", adminRepository.queryClientIdListByModelId(db.getModelId()));
            db.setModelId(request.getModelId().trim());
        }
        if (!StringUtils.hasText(request.getModelId())) {
            request.setModelId(db.getModelId());
        }
        if (!StringUtils.hasText(request.getApiId())) request.setApiId(db.getApiId());
        if (!StringUtils.hasText(request.getModelName())) request.setModelName(db.getModelName());
        if (!StringUtils.hasText(request.getModelType())) request.setModelType(db.getModelType());
        if (request.getModelStatus() == null) request.setModelStatus(db.getModelStatus());
        adminRepository.updateModel(request);
        return toModelVO(adminRepository.queryModelById(request.getId()));
    }

    @Override
    public void modelDelete(String modelId) {
        AdminModel db = adminRepository.queryModelByModelId(modelId);
        if (db == null) {
            return;
        }
        assertNoDependency("客户端", adminRepository.queryClientIdListByModelId(db.getModelId()));
        adminRepository.deleteModel(db.getId());
    }

    @Override
    public ModelVO modelToggle(String modelId, Integer status) {
        AdminModel db = adminRepository.queryModelByModelId(modelId);
        if (db == null) throw new IllegalArgumentException("模型不存在");
        if (status != null && status == 0) {
            assertNoDependency("客户端", adminRepository.queryClientIdListByModelId(db.getModelId()));
        }
        ModelManageRequest request = ModelManageRequest.builder()
                .id(db.getId())
                .modelId(db.getModelId())
                .apiId(db.getApiId())
                .modelName(db.getModelName())
                .modelType(db.getModelType())
                .modelStatus(status)
                .build();
        adminRepository.updateModel(request);
        return toModelVO(adminRepository.queryModelById(db.getId()));
    }

    // -------------------- MCP --------------------
    @Override
    public PageResult<McpVO> mcpPage(McpPageRequest request) {
        PageConfig page = resolvePage(request.getPageNum(), request.getPageSize());
        String keyword = request.getKeyword() != null ? request.getKeyword() : firstNonNull(request.getIdKeyword(), request.getNameKeyword());
        List<AdminMcp> list = adminRepository.queryMcpList(keyword, null, null, page.getOffset(), page.getSize());
        Long total = adminRepository.countMcp(keyword, null, null);
        return PageResult.<McpVO>builder()
                .list(list.stream().map(this::toMcpVO).toList())
                .pageNum(page.getPage())
                .pageSize(page.getSize())
                .pageSum(total == null ? 0 : total.intValue())
                .build();
    }

    @Override
    public McpVO mcpCreate(McpManageRequest request) {
        String mcpId = request.getMcpId().trim();
        if (adminRepository.queryMcpByMcpId(mcpId) != null) {
            throw new IllegalArgumentException("工具 ID 已存在");
        }
        request.setMcpId(mcpId);
        request.setMcpTimeout(request.getMcpTimeout() == null ? 180 : request.getMcpTimeout());
        request.setMcpChat(request.getMcpChat() == null ? 0 : request.getMcpChat());
        request.setMcpStatus(request.getMcpStatus() == null ? 1 : request.getMcpStatus());
        adminRepository.insertMcp(request);
        return toMcpVO(adminRepository.queryMcpByMcpId(mcpId));
    }

    @Override
    public McpVO mcpUpdate(McpManageRequest request) {
        if (request.getId() == null) {
            throw new IllegalArgumentException("缺少工具 ID");
        }
        AdminMcp db = adminRepository.queryMcpById(request.getId());
        if (db == null) throw new IllegalArgumentException("工具不存在");
        if (StringUtils.hasText(request.getMcpId()) && !request.getMcpId().equals(db.getMcpId())) {
            if (adminRepository.queryMcpByMcpId(request.getMcpId()) != null) {
                throw new IllegalArgumentException("工具 ID 已存在");
            }
            assertNoDependency("客户端", adminRepository.queryClientIdListByMcpId(db.getMcpId()));
            db.setMcpId(request.getMcpId().trim());
        }
        if (!StringUtils.hasText(request.getMcpId())) request.setMcpId(db.getMcpId());
        if (!StringUtils.hasText(request.getMcpName())) request.setMcpName(db.getMcpName());
        if (!StringUtils.hasText(request.getMcpType())) request.setMcpType(db.getMcpType());
        if (request.getMcpConfig() == null) request.setMcpConfig(db.getMcpConfig());
        if (request.getMcpDesc() == null) request.setMcpDesc(db.getMcpDesc());
        if (request.getMcpTimeout() == null) request.setMcpTimeout(db.getMcpTimeout());
        if (request.getMcpChat() == null) request.setMcpChat(db.getMcpChat());
        if (request.getMcpStatus() == null) request.setMcpStatus(db.getMcpStatus());
        adminRepository.updateMcp(request);
        return toMcpVO(adminRepository.queryMcpById(request.getId()));
    }

    @Override
    public void mcpDelete(String mcpId) {
        AdminMcp db = adminRepository.queryMcpByMcpId(mcpId);
        if (db == null) return;
        assertNoDependency("客户端", adminRepository.queryClientIdListByMcpId(db.getMcpId()));
        adminRepository.deleteMcp(db.getId());
    }

    @Override
    public McpVO mcpToggle(String mcpId, Integer status) {
        AdminMcp db = adminRepository.queryMcpByMcpId(mcpId);
        if (db == null) throw new IllegalArgumentException("工具不存在");
        if (status != null && status == 0) {
            assertNoDependency("客户端", adminRepository.queryClientIdListByMcpId(db.getMcpId()));
        }
        McpManageRequest request = McpManageRequest.builder()
                .id(db.getId())
                .mcpId(db.getMcpId())
                .mcpName(db.getMcpName())
                .mcpType(db.getMcpType())
                .mcpConfig(db.getMcpConfig())
                .mcpDesc(db.getMcpDesc())
                .mcpTimeout(db.getMcpTimeout())
                .mcpChat(db.getMcpChat())
                .mcpStatus(status)
                .build();
        adminRepository.updateMcp(request);
        return toMcpVO(adminRepository.queryMcpById(db.getId()));
    }

    // -------------------- Advisor --------------------
    @Override
    public PageResult<AdvisorVO> advisorPage(AdvisorPageRequest request) {
        PageConfig page = resolvePage(request.getPageNum(), request.getPageSize());
        String keyword = request.getKeyword() != null ? request.getKeyword() : firstNonNull(request.getIdKeyword(), request.getNameKeyword());
        List<AdminAdvisor> list = adminRepository.queryAdvisorList(keyword, null, null, page.getOffset(), page.getSize());
        Long total = adminRepository.countAdvisor(keyword, null, null);
        return PageResult.<AdvisorVO>builder()
                .list(list.stream().map(this::toAdvisorVO).toList())
                .pageNum(page.getPage())
                .pageSize(page.getSize())
                .pageSum(total == null ? 0 : total.intValue())
                .build();
    }

    @Override
    public AdvisorVO advisorCreate(AdvisorManageRequest request) {
        String advisorId = request.getAdvisorId().trim();
        if (adminRepository.queryAdvisorByAdvisorId(advisorId) != null) {
            throw new IllegalArgumentException("顾问 ID 已存在");
        }
        request.setAdvisorId(advisorId);
        request.setAdvisorOrder(request.getAdvisorOrder() == null ? 0 : request.getAdvisorOrder());
        request.setAdvisorStatus(request.getAdvisorStatus() == null ? 1 : request.getAdvisorStatus());
        adminRepository.insertAdvisor(request);
        return toAdvisorVO(adminRepository.queryAdvisorByAdvisorId(advisorId));
    }

    @Override
    public AdvisorVO advisorUpdate(AdvisorManageRequest request) {
        if (request.getId() == null) {
            throw new IllegalArgumentException("缺少顾问 ID");
        }
        AdminAdvisor db = adminRepository.queryAdvisorById(request.getId());
        if (db == null) throw new IllegalArgumentException("顾问不存在");
        if (StringUtils.hasText(request.getAdvisorId()) && !request.getAdvisorId().equals(db.getAdvisorId())) {
            if (adminRepository.queryAdvisorByAdvisorId(request.getAdvisorId()) != null) {
                throw new IllegalArgumentException("顾问 ID 已存在");
            }
            assertNoDependency("客户端", adminRepository.queryClientIdListByAdvisorId(db.getAdvisorId()));
            db.setAdvisorId(request.getAdvisorId().trim());
        }
        if (!StringUtils.hasText(request.getAdvisorId())) request.setAdvisorId(db.getAdvisorId());
        if (!StringUtils.hasText(request.getAdvisorName())) request.setAdvisorName(db.getAdvisorName());
        if (!StringUtils.hasText(request.getAdvisorType())) request.setAdvisorType(db.getAdvisorType());
        if (request.getAdvisorDesc() == null) request.setAdvisorDesc(db.getAdvisorDesc());
        if (request.getAdvisorOrder() == null) request.setAdvisorOrder(db.getAdvisorOrder());
        if (request.getAdvisorParam() == null) request.setAdvisorParam(db.getAdvisorParam());
        if (request.getAdvisorStatus() == null) request.setAdvisorStatus(db.getAdvisorStatus());
        adminRepository.updateAdvisor(request);
        return toAdvisorVO(adminRepository.queryAdvisorById(request.getId()));
    }

    @Override
    public void advisorDelete(String advisorId) {
        AdminAdvisor db = adminRepository.queryAdvisorByAdvisorId(advisorId);
        if (db == null) return;
        assertNoDependency("客户端", adminRepository.queryClientIdListByAdvisorId(db.getAdvisorId()));
        adminRepository.deleteAdvisor(db.getId());
    }

    @Override
    public AdvisorVO advisorToggle(String advisorId, Integer status) {
        AdminAdvisor db = adminRepository.queryAdvisorByAdvisorId(advisorId);
        if (db == null) throw new IllegalArgumentException("顾问不存在");
        if (status != null && status == 0) {
            assertNoDependency("客户端", adminRepository.queryClientIdListByAdvisorId(db.getAdvisorId()));
        }
        AdvisorManageRequest request = AdvisorManageRequest.builder()
                .id(db.getId())
                .advisorId(db.getAdvisorId())
                .advisorName(db.getAdvisorName())
                .advisorType(db.getAdvisorType())
                .advisorDesc(db.getAdvisorDesc())
                .advisorOrder(db.getAdvisorOrder())
                .advisorParam(db.getAdvisorParam())
                .advisorStatus(status)
                .build();
        adminRepository.updateAdvisor(request);
        return toAdvisorVO(adminRepository.queryAdvisorById(db.getId()));
    }

    // -------------------- Prompt --------------------
    @Override
    public PageResult<PromptVO> promptPage(PromptPageRequest request) {
        PageConfig page = resolvePage(request.getPageNum(), request.getPageSize());
        String keyword = request.getKeyword() != null ? request.getKeyword() : firstNonNull(request.getIdKeyword(), request.getNameKeyword());
        List<AdminPrompt> list = adminRepository.queryPromptList(keyword, null, page.getOffset(), page.getSize());
        Long total = adminRepository.countPrompt(keyword, null);
        return PageResult.<PromptVO>builder()
                .list(list.stream().map(this::toPromptVO).toList())
                .pageNum(page.getPage())
                .pageSize(page.getSize())
                .pageSum(total == null ? 0 : total.intValue())
                .build();
    }

    @Override
    public PromptVO promptCreate(PromptManageRequest request) {
        String promptId = request.getPromptId().trim();
        if (adminRepository.queryPromptByPromptId(promptId) != null) {
            throw new IllegalArgumentException("提示词 ID 已存在");
        }
        request.setPromptId(promptId);
        request.setPromptStatus(request.getPromptStatus() == null ? 1 : request.getPromptStatus());
        adminRepository.insertPrompt(request);
        return toPromptVO(adminRepository.queryPromptByPromptId(promptId));
    }

    @Override
    public PromptVO promptUpdate(PromptManageRequest request) {
        if (request.getId() == null) {
            throw new IllegalArgumentException("缺少提示词 ID");
        }
        AdminPrompt db = adminRepository.queryPromptById(request.getId());
        if (db == null) throw new IllegalArgumentException("提示词不存在");
        if (StringUtils.hasText(request.getPromptId()) && !request.getPromptId().equals(db.getPromptId())) {
            if (adminRepository.queryPromptByPromptId(request.getPromptId()) != null) {
                throw new IllegalArgumentException("提示词 ID 已存在");
            }
            assertNoDependency("客户端", adminRepository.queryClientIdListByPromptId(db.getPromptId()));
            db.setPromptId(request.getPromptId().trim());
        }
        if (!StringUtils.hasText(request.getPromptId())) request.setPromptId(db.getPromptId());
        if (!StringUtils.hasText(request.getPromptName())) request.setPromptName(db.getPromptName());
        if (request.getPromptContent() == null) request.setPromptContent(db.getPromptContent());
        if (request.getPromptDesc() == null) request.setPromptDesc(db.getPromptDesc());
        if (request.getPromptStatus() == null) request.setPromptStatus(db.getPromptStatus());
        adminRepository.updatePrompt(request);
        return toPromptVO(adminRepository.queryPromptById(request.getId()));
    }

    @Override
    public void promptDelete(String promptId) {
        AdminPrompt db = adminRepository.queryPromptByPromptId(promptId);
        if (db == null) return;
        assertNoDependency("客户端", adminRepository.queryClientIdListByPromptId(db.getPromptId()));
        adminRepository.deletePrompt(db.getId());
    }

    @Override
    public PromptVO promptToggle(String promptId, Integer status) {
        AdminPrompt db = adminRepository.queryPromptByPromptId(promptId);
        if (db == null) throw new IllegalArgumentException("提示词不存在");
        if (status != null && status == 0) {
            assertNoDependency("客户端", adminRepository.queryClientIdListByPromptId(db.getPromptId()));
        }
        PromptManageRequest request = PromptManageRequest.builder()
                .id(db.getId())
                .promptId(db.getPromptId())
                .promptName(db.getPromptName())
                .promptContent(db.getPromptContent())
                .promptDesc(db.getPromptDesc())
                .promptStatus(status)
                .build();
        adminRepository.updatePrompt(request);
        return toPromptVO(adminRepository.queryPromptById(db.getId()));
    }

    // -------------------- Client --------------------
    @Override
    public PageResult<ClientVO> clientPage(ClientPageRequest request) {
        PageConfig page = resolvePage(request.getPageNum(), request.getPageSize());
        String keyword = request.getKeyword() != null ? request.getKeyword() : firstNonNull(request.getIdKeyword(), request.getNameKeyword());
        List<AdminClient> list = adminRepository.queryClientList(keyword, request.getModelId(), request.getClientType(), null, page.getOffset(), page.getSize());
        Long total = adminRepository.countClient(keyword, request.getModelId(), request.getClientType(), null);
        return PageResult.<ClientVO>builder()
                .list(list.stream().map(this::toClientVO).toList())
                .pageNum(page.getPage())
                .pageSize(page.getSize())
                .pageSum(total == null ? 0 : total.intValue())
                .build();
    }

    @Override
    public ClientVO clientCreate(ClientManageRequest request) {
        String clientId = request.getClientId().trim();
        if (adminRepository.queryClientByClientId(clientId) != null) {
            throw new IllegalArgumentException("客户端 ID 已存在");
        }
        request.setClientId(clientId);
        if (!StringUtils.hasText(request.getModelName())) {
            request.setModelName("");
        }
        request.setClientStatus(request.getClientStatus() == null ? 1 : request.getClientStatus());
        adminRepository.insertClient(request);
        return toClientVO(adminRepository.queryClientByClientId(clientId));
    }

    @Override
    public ClientVO clientUpdate(ClientManageRequest request) {
        if (request.getId() == null) {
            throw new IllegalArgumentException("缺少客户端 ID");
        }
        AdminClient db = adminRepository.queryClientById(request.getId());
        if (db == null) throw new IllegalArgumentException("客户端不存在");
        if (StringUtils.hasText(request.getClientId()) && !request.getClientId().equals(db.getClientId())) {
            if (adminRepository.queryClientByClientId(request.getClientId()) != null) {
                throw new IllegalArgumentException("客户端 ID 已存在");
            }
            assertNoDependency("工作流", extractFlowDependents(adminRepository.queryFlowListByClientId(db.getClientId())));
            db.setClientId(request.getClientId().trim());
        }
        if (!StringUtils.hasText(request.getClientId())) request.setClientId(db.getClientId());
        if (!StringUtils.hasText(request.getClientType())) request.setClientType(db.getClientType());
        if (!StringUtils.hasText(request.getModelId())) request.setModelId(db.getModelId());
        if (!StringUtils.hasText(request.getModelName())) request.setModelName(db.getModelName());
        if (!StringUtils.hasText(request.getClientName())) request.setClientName(db.getClientName());
        if (request.getClientDesc() == null) request.setClientDesc(db.getClientDesc());
        if (request.getClientStatus() == null) request.setClientStatus(db.getClientStatus());
        adminRepository.updateClient(request);
        return toClientVO(adminRepository.queryClientById(request.getId()));
    }

    @Override
    public void clientDelete(String clientId) {
        AdminClient db = adminRepository.queryClientByClientId(clientId);
        if (db == null) return;
        assertNoDependency("工作流", extractFlowDependents(adminRepository.queryFlowListByClientId(db.getClientId())));
        adminRepository.deleteClient(db.getId());
    }

    @Override
    public ClientVO clientToggle(String clientId, Integer status) {
        AdminClient db = adminRepository.queryClientByClientId(clientId);
        if (db == null) throw new IllegalArgumentException("客户端不存在");
        if (status != null && status == 0) {
            assertNoDependency("工作流", extractFlowDependents(adminRepository.queryFlowListByClientId(db.getClientId())));
        }
        ClientManageRequest request = ClientManageRequest.builder()
                .id(db.getId())
                .clientId(db.getClientId())
                .clientType(db.getClientType())
                .modelId(db.getModelId())
                .modelName(db.getModelName())
                .clientName(db.getClientName())
                .clientDesc(db.getClientDesc())
                .clientStatus(status)
                .build();
        adminRepository.updateClient(request);
        return toClientVO(adminRepository.queryClientById(db.getId()));
    }

    // -------------------- Flow --------------------
    @Override
    public PageResult<FlowVO> flowPage(FlowPageRequest request) {
        PageConfig page = resolvePage(request.getPageNum(), request.getPageSize());
        List<AdminFlow> list = adminRepository.queryFlowList(request.getAgentId(), request.getClientId(), request.getStatus(), page.getOffset(), page.getSize());
        Long total = adminRepository.countFlow(request.getAgentId(), request.getClientId(), request.getStatus());
        return PageResult.<FlowVO>builder()
                .list(list.stream().map(this::toFlowVO).toList())
                .pageNum(page.getPage())
                .pageSize(page.getSize())
                .pageSum(total == null ? 0 : total.intValue())
                .build();
    }

    @Override
    public FlowVO flowCreate(FlowManageRequest request) {
        request.setClientType(StringUtils.hasText(request.getClientType()) ? request.getClientType().trim() : "");
        request.setFlowPrompt(request.getFlowPrompt() == null ? "" : request.getFlowPrompt());
        request.setFlowStatus(request.getFlowStatus() == null ? 1 : request.getFlowStatus());
        adminRepository.insertFlow(request);
        return toFlowVO(AdminFlow.builder()
                .agentId(request.getAgentId())
                .clientId(request.getClientId())
                .clientType(request.getClientType())
                .flowPrompt(request.getFlowPrompt())
                .flowSeq(request.getFlowSeq())
                .flowStatus(request.getFlowStatus())
                .build());
    }

    @Override
    public FlowVO flowUpdate(FlowManageRequest request) {
        if (request.getId() == null) {
            throw new IllegalArgumentException("缺少工作流 ID");
        }
        AdminFlow db = adminRepository.queryFlowById(request.getId());
        if (db == null) throw new IllegalArgumentException("工作流不存在");
        if (!StringUtils.hasText(request.getAgentId())) request.setAgentId(db.getAgentId());
        if (!StringUtils.hasText(request.getClientId())) request.setClientId(db.getClientId());
        if (!StringUtils.hasText(request.getClientType())) request.setClientType(db.getClientType());
        if (request.getFlowPrompt() == null) request.setFlowPrompt(db.getFlowPrompt());
        if (request.getFlowSeq() == null) request.setFlowSeq(db.getFlowSeq());
        if (request.getFlowStatus() == null) request.setFlowStatus(db.getFlowStatus());
        adminRepository.updateFlow(request);
        return toFlowVO(adminRepository.queryFlowById(request.getId()));
    }

    @Override
    public void flowDelete(String flowId) {
        Long id = Long.valueOf(flowId);
        adminRepository.deleteFlow(id);
    }

    @Override
    public FlowVO flowToggle(String flowId, Integer status) {
        Long id = Long.valueOf(flowId);
        AdminFlow db = adminRepository.queryFlowById(id);
        if (db == null) throw new IllegalArgumentException("工作流不存在");
        adminRepository.updateFlowStatus(id, status);
        db.setFlowStatus(status);
        return toFlowVO(db);
    }

    // -------------------- Agent --------------------
    @Override
    public PageResult<AdminAgentVO> agentPage(AgentPageRequest request) {
        PageConfig page = resolvePage(request.getPageNum(), request.getPageSize());
        String keyword = request.getKeyword() != null ? request.getKeyword() : firstNonNull(request.getIdKeyword(), request.getNameKeyword());
        List<AdminAgent> agentList = adminRepository.queryAgentList(keyword, request.getAgentStatus(), request.getAgentType(), page.getOffset(), page.getSize());
        Long total = adminRepository.countAgent(keyword, request.getAgentStatus(), request.getAgentType());
        List<AdminAgentVO> voList = agentList.stream().map(this::toAgentVO).toList();
        return PageResult.<AdminAgentVO>builder()
                .list(voList)
                .pageNum(page.getPage())
                .pageSize(page.getSize())
                .pageSum(total == null ? 0 : total.intValue())
                .build();
    }

    @Override
    public AdminAgentVO agentCreate(AgentManageRequest request) {
        String agentId = request.getAgentId().trim();
        if (adminRepository.queryAgentByAgentId(agentId) != null) {
            throw new IllegalArgumentException("智能体 ID 已存在");
        }
        request.setAgentId(agentId);
        request.setAgentType(StringUtils.hasText(request.getAgentType()) ? request.getAgentType().trim() : "loop");
        request.setAgentDesc(StringUtils.hasText(request.getAgentDesc()) ? request.getAgentDesc().trim() : "");
        request.setAgentStatus(request.getAgentStatus() == null ? 1 : request.getAgentStatus());
        adminRepository.insertAgent(request);
        return toAgentVO(adminRepository.queryAgentByAgentId(agentId));
    }

    @Override
    public AdminAgentVO agentUpdate(AgentManageRequest request) {
        if (request.getId() == null) {
            throw new IllegalArgumentException("缺少智能体 ID");
        }
        AdminAgent dbAgent = adminRepository.apiQuery(request.getId());
        if (dbAgent == null) {
            throw new IllegalArgumentException("智能体不存在");
        }

        if (StringUtils.hasText(request.getAgentId()) && !request.getAgentId().equals(dbAgent.getAgentId())) {
            AdminAgent exists = adminRepository.queryAgentByAgentId(request.getAgentId());
            if (exists != null && !exists.getId().equals(dbAgent.getId())) {
                throw new IllegalArgumentException("智能体 ID 已存在");
            }
            assertNoDependency("工作流", extractFlowDependents(adminRepository.queryFlowListByAgentId(dbAgent.getAgentId())));
            dbAgent.setAgentId(request.getAgentId().trim());
        }
        if (!StringUtils.hasText(request.getAgentId())) request.setAgentId(dbAgent.getAgentId());
        if (!StringUtils.hasText(request.getAgentName())) request.setAgentName(dbAgent.getAgentName());
        if (!StringUtils.hasText(request.getAgentType())) request.setAgentType(dbAgent.getAgentType());
        if (request.getAgentDesc() == null) request.setAgentDesc(dbAgent.getAgentDesc());
        if (request.getAgentStatus() == null) request.setAgentStatus(dbAgent.getAgentStatus());

        adminRepository.updateAgent(request);
        return toAgentVO(adminRepository.apiQuery(request.getId()));
    }

    @Override
    public void agentDelete(String agentId) {
        AdminAgent db = adminRepository.queryAgentByAgentId(agentId);
        if (db == null) return;
        assertNoDependency("工作流", extractFlowDependents(adminRepository.queryFlowListByAgentId(db.getAgentId())));
        adminRepository.deleteAgent(db.getId());
    }

    @Override
    public AdminAgentVO agentToggle(String agentId, Integer status) {
        AdminAgent db = adminRepository.queryAgentByAgentId(agentId);
        if (db == null) throw new IllegalArgumentException("智能体不存在");
        if (status != null && status == 0) {
            assertNoDependency("工作流", extractFlowDependents(adminRepository.queryFlowListByAgentId(db.getAgentId())));
        }
        AgentManageRequest request = AgentManageRequest.builder()
                .id(db.getId())
                .agentId(db.getAgentId())
                .agentName(db.getAgentName())
                .agentType(db.getAgentType())
                .agentDesc(db.getAgentDesc())
                .agentStatus(status)
                .build();
        adminRepository.updateAgent(request);
        return toAgentVO(adminRepository.queryAgentByAgentId(agentId));
    }

    // -------------------- User --------------------
    @Override
    public PageResult<UserAdminVO> userPage(UserPageRequest request) {
        PageConfig page = resolvePage(request.getPageNum(), request.getPageSize());
        List<User> userList = adminRepository.queryUserList(request.getUsername(), request.getRole(), page.getOffset(), page.getSize());
        Long total = adminRepository.countUser(request.getUsername(), request.getRole());

        List<UserAdminVO> voList = userList.stream().map(this::toUserAdminVO).toList();
        return PageResult.<UserAdminVO>builder()
                .list(voList)
                .pageNum(page.getPage())
                .pageSize(page.getSize())
                .pageSum(total == null ? 0 : total.intValue())
                .build();
    }

    @Override
    public UserAdminVO userCreate(UserManageRequest request) {
        String username = request.getUsername().trim();
        if (adminRepository.queryUserByUsername(username) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        request.setUsername(username);
        request.setPassword(passwordEncoder.encode(request.getPassword()));
        request.setRole(StringUtils.hasText(request.getRole()) ? request.getRole().trim() : "account");
        adminRepository.insertUser(request);
        return toUserAdminVO(adminRepository.queryUserByUsername(username));
    }

    @Override
    public UserAdminVO userUpdate(UserManageRequest request) {
        if (request.getId() == null) {
            throw new IllegalArgumentException("缺少用户 ID");
        }
        User user = adminRepository.queryUserById(request.getId());
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (StringUtils.hasText(request.getUsername()) && !request.getUsername().equals(user.getUsername())) {
            if (adminRepository.queryUserByUsername(request.getUsername()) != null) {
                throw new IllegalArgumentException("用户名已存在");
            }
            user.setUsername(request.getUsername().trim());
        }
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (StringUtils.hasText(request.getRole())) {
            user.setRole(request.getRole().trim());
        }
        request.setUsername(user.getUsername());
        request.setPassword(user.getPassword());
        request.setRole(user.getRole());
        adminRepository.updateUser(request);
        return toUserAdminVO(adminRepository.queryUserById(request.getId()));
    }

    @Override
    public void userDelete(String userId) {
        if (userId == null) {
            return;
        }
        Long id = Long.valueOf(userId);
        adminRepository.deleteUser(id);
    }

    // -------------------- helper --------------------
    private void assertNoDependency(String dependentName, List<String> dependents) {
        if (!CollectionUtils.isEmpty(dependents)) {
            throw new DependencyConflictException("存在依赖，无法执行操作，相关" + dependentName + "：" + String.join(",", dependents), dependents);
        }
    }

    private PageConfig resolvePage(Integer page, Integer size) {
        int p = page == null || page <= 0 ? 1 : page;
        int s = size == null || size <= 0 ? 10 : size;
        return PageConfig.builder()
                .page(p)
                .size(s)
                .offset((p - 1) * s)
                .build();
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

}
