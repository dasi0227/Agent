package com.dasi.domain.admin.service;

import com.dasi.domain.admin.model.enumeration.*;
import com.dasi.domain.admin.model.vo.*;
import com.dasi.domain.admin.repository.IAdminRepository;
import com.dasi.types.dto.request.admin.manage.*;
import com.dasi.types.dto.request.admin.query.*;
import com.dasi.types.dto.result.PageResult;
import com.dasi.types.exception.AdminException;
import com.dasi.types.exception.DependencyConflictException;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService implements IAdminService {

    @Resource
    private IAdminRepository adminRepository;

    @Resource
    private PasswordEncoder passwordEncoder;

    // -------------------- API --------------------
    @Override
    public PageResult<ApiVO> apiPage(ApiPageRequest request) {
        List<ApiVO> apiVOList = adminRepository.apiPage(request);
        Integer total = adminRepository.apiCount(request);
        Integer size = request.getPageSize();
        Integer pageSum = (total + size - 1) / size;
        return PageResult.<ApiVO>builder()
                .list(apiVOList)
                .total(total)
                .pageSum(pageSum)
                .pageNum(request.getPageNum())
                .pageSize(size)
                .build();
    }

    @Override
    public void apiInsert(ApiManageRequest request) {
        if (adminRepository.apiQuery(request.getApiId()) != null) {
            throw new AdminException("API 已存在，请修改后重新添加");
        }
        adminRepository.apiInsert(request);
    }

    @Override
    public void apiUpdate(ApiManageRequest request) {
        if (adminRepository.apiQuery(request.getId()) == null) {
            throw new AdminException("API 不存在，请确认后重新更改");
        }
        adminRepository.apiUpdate(request);
    }

    @Override
    public void apiDelete(Long id) {
        ApiVO apiVO = adminRepository.apiQuery(id);
        if (apiVO == null) {
            throw new AdminException("API 不存在，请确认后重新删除");
        }
        assertNoDependency("模型", adminRepository.queryModelDependOnApi(apiVO.getApiId()));
        adminRepository.apiDelete(id);
    }

    // -------------------- Model --------------------
    @Override
    public PageResult<ModelVO> modelPage(ModelPageRequest request) {
        List<ModelVO> list = adminRepository.modelPage(request);
        Integer total = adminRepository.modelCount(request);
        Integer size = request.getPageSize();
        Integer pageSum = (total + size - 1) / size;
        return PageResult.<ModelVO>builder()
                .list(list)
                .total(total)
                .pageSum(pageSum)
                .pageNum(request.getPageNum())
                .pageSize(size)
                .build();
    }

    @Override
    public void modelInsert(ModelManageRequest request) {
        if (adminRepository.modelQuery(request.getModelId()) != null) {
            throw new AdminException("MODEL 已存在，请修改后重新添加");
        }
        adminRepository.modelInsert(request);
    }

    @Override
    public void modelUpdate(ModelManageRequest request) {
        if (adminRepository.modelQuery(request.getId()) == null) {
            throw new AdminException("MODEL 不存在，请确认后重新更改");
        }
        adminRepository.modelUpdate(request);
    }

    @Override
    public void modelDelete(Long id) {
        ModelVO modelVO = adminRepository.modelQuery(id);
        if (modelVO == null) {
            throw new AdminException("MODEL 不存在，请确认后重新删除");
        }
        assertNoDependency("客户端", adminRepository.queryClientDependOnModel(modelVO.getModelId()));
        adminRepository.modelDelete(id);
    }

    // -------------------- MCP --------------------
    @Override
    public PageResult<McpVO> mcpPage(McpPageRequest request) {
        List<McpVO> list = adminRepository.mcpPage(request);
        Integer total = adminRepository.mcpCount(request);
        Integer size = request.getPageSize();
        Integer pageSum = (total + size - 1) / size;
        return PageResult.<McpVO>builder()
                .list(list)
                .total(total)
                .pageSum(pageSum)
                .pageNum(request.getPageNum())
                .pageSize(size)
                .build();
    }

    @Override
    public void mcpInsert(McpManageRequest request) {
        if (adminRepository.mcpQuery(request.getMcpId()) != null) {
            throw new AdminException("MCP 已存在，请修改后重新添加");
        }
        adminRepository.mcpInsert(request);
    }

    @Override
    public void mcpUpdate(McpManageRequest request) {
        if (adminRepository.mcpQuery(request.getId()) == null) {
            throw new AdminException("MCP 不存在，请确认后重新更改");
        }
        adminRepository.mcpUpdate(request);
    }

    @Override
    public void mcpDelete(Long id) {
        McpVO mcpVO = adminRepository.mcpQuery(id);
        if (mcpVO == null) {
            throw new AdminException("MCP 不存在，请确认后重新删除");
        }
        assertNoDependency("客户端", adminRepository.queryClientDependOnMcp(mcpVO.getMcpId()));
        adminRepository.mcpDelete(id);
    }

    // -------------------- Advisor --------------------
    @Override
    public PageResult<AdvisorVO> advisorPage(AdvisorPageRequest request) {
        List<AdvisorVO> list = adminRepository.advisorPage(request);
        Integer total = adminRepository.advisorCount(request);
        Integer size = request.getPageSize();
        Integer pageSum = (total + size - 1) / size;
        return PageResult.<AdvisorVO>builder()
                .list(list)
                .total(total)
                .pageSum(pageSum)
                .pageNum(request.getPageNum())
                .pageSize(size)
                .build();
    }

    @Override
    public void advisorInsert(AdvisorManageRequest request) {
        if (adminRepository.advisorQuery(request.getAdvisorId()) != null) {
            throw new AdminException("ADVISOR 已存在，请修改后重新添加");
        }
        adminRepository.advisorInsert(request);
    }

    @Override
    public void advisorUpdate(AdvisorManageRequest request) {
        if (adminRepository.advisorQuery(request.getId()) == null) {
            throw new AdminException("ADVISOR 不存在，请确认后重新更改");
        }
        adminRepository.advisorUpdate(request);
    }

    @Override
    public void advisorDelete(Long id) {
        AdvisorVO advisorVO = adminRepository.advisorQuery(id);
        if (advisorVO == null) {
            throw new AdminException("ADVISOR 不存在，请确认后重新删除");
        }
        assertNoDependency("客户端", adminRepository.queryClientDependOnAdvisor(advisorVO.getAdvisorId()));
        adminRepository.advisorDelete(id);
    }

    // -------------------- Prompt --------------------
    @Override
    public PageResult<PromptVO> promptPage(PromptPageRequest request) {
        List<PromptVO> list = adminRepository.promptPage(request);
        Integer total = adminRepository.promptCount(request);
        Integer size = request.getPageSize();
        Integer pageSum = (total + size - 1) / size;
        return PageResult.<PromptVO>builder()
                .list(list)
                .total(total)
                .pageSum(pageSum)
                .pageNum(request.getPageNum())
                .pageSize(size)
                .build();
    }

    @Override
    public void promptInsert(PromptManageRequest request) {
        if (adminRepository.promptQuery(request.getPromptId()) != null) {
            throw new AdminException("PROMPT 已存在，请修改后重新添加");
        }
        adminRepository.promptInsert(request);
    }

    @Override
    public void promptUpdate(PromptManageRequest request) {
        if (adminRepository.promptQuery(request.getId()) == null) {
            throw new AdminException("PROMPT 不存在，请确认后重新更改");
        }
        adminRepository.promptUpdate(request);
    }

    @Override
    public void promptDelete(Long id) {
        PromptVO promptVO = adminRepository.promptQuery(id);
        if (promptVO == null) {
            throw new AdminException("PROMPT 不存在，请确认后重新删除");
        }
        assertNoDependency("客户端", adminRepository.queryClientDependOnPrompt(promptVO.getPromptId()));
        adminRepository.promptDelete(id);
    }

    // -------------------- Client --------------------
    @Override
    public PageResult<ClientVO> clientPage(ClientPageRequest request) {
        List<ClientVO> list = adminRepository.clientPage(request);
        Integer total = adminRepository.clientCount(request);
        Integer size = request.getPageSize();
        Integer pageSum = (total + size - 1) / size;
        return PageResult.<ClientVO>builder()
                .list(list)
                .total(total)
                .pageSum(pageSum)
                .pageNum(request.getPageNum())
                .pageSize(size)
                .build();
    }

    @Override
    public void clientInsert(ClientManageRequest request) {
        if (adminRepository.clientQuery(request.getClientId()) != null) {
            throw new AdminException("CLIENT 已存在，请修改后重新添加");
        }
        adminRepository.clientInsert(request);
    }

    @Override
    public void clientUpdate(ClientManageRequest request) {
        if (adminRepository.clientQuery(request.getId()) == null) {
            throw new AdminException("CLIENT 不存在，请确认后重新更改");
        }
        adminRepository.clientUpdate(request);
    }

    @Override
    public void clientDelete(Long id) {
        ClientVO clientVO = adminRepository.clientQuery(id);
        if (clientVO == null) {
            throw new AdminException("CLIENT 不存在，请确认后重新删除");
        }
        assertNoDependency("客户端", adminRepository.queryAgentDependOnClient(clientVO.getClientId()));
        adminRepository.clientDelete(id);
    }

    @Override
    public void clientToggle(Long id, Integer status) {
        ClientVO clientVO = adminRepository.clientQuery(id);
        if (clientVO == null) {
            throw new AdminException("CLIENT 不存在，请确认后重新切换");
        }
        if (status == 0) {
            assertNoDependency("客户端", adminRepository.queryAgentDependOnClient(clientVO.getClientId()));
        }
        adminRepository.clientToggle(id, status);
    }

    // -------------------- Agent --------------------
    @Override
    public PageResult<AgentVO> agentPage(AgentPageRequest request) {
        List<AgentVO> voList = adminRepository.agentPage(request);
        Integer total = adminRepository.agentCount(request);
        Integer size = request.getPageSize();
        Integer pageSum = (total + size - 1) / size;
        return PageResult.<AgentVO>builder()
                .list(voList)
                .total(total)
                .pageSum(pageSum)
                .pageNum(request.getPageNum())
                .pageSize(size)
                .build();
    }

    @Override
    public List<AgentVO> agentList(AgentListRequest request) {
        return adminRepository.agentList(request);
    }

    @Override
    public void agentInsert(AgentManageRequest request) {
        if (adminRepository.agentQuery(request.getAgentId()) != null) {
            throw new AdminException("AGENT 已存在，请修改后重新添加");
        }
        adminRepository.agentInsert(request);
    }

    @Override
    public void agentUpdate(AgentManageRequest request) {
        if (adminRepository.agentQuery(request.getId()) == null) {
            throw new AdminException("AGENT 不存在，请确认后重新更改");
        }
        adminRepository.agentUpdate(request);
    }

    @Override
    public void agentDelete(Long id) {
        AgentVO agentVO = adminRepository.agentQuery(id);
        if (agentVO == null) {
            throw new AdminException("AGENT 不存在，请确认后重新删除");
        }
        adminRepository.agentDelete(id);
    }

    @Override
    public void agentToggle(Long id, Integer status) {
        AgentVO agentVO = adminRepository.agentQuery(id);
        if (agentVO == null) {
            throw new AdminException("AGENT 不存在，请确认后重新切换");
        }
        adminRepository.agentToggle(id, status);
    }

    // -------------------- User --------------------
    @Override
    public PageResult<UserVO> userPage(UserPageRequest request) {
        List<UserVO> voList = adminRepository.userPage(request);
        Integer total = adminRepository.userCount(request);
        Integer size = request.getPageSize();
        Integer pageSum = (total + size - 1) / size;
        return PageResult.<UserVO>builder()
                .list(voList)
                .total(total)
                .pageSum(pageSum)
                .pageNum(request.getPageNum())
                .pageSize(size)
                .build();
    }

    @Override
    public void userInsert(UserManageRequest request) {
        if (adminRepository.userQuery(request.getUsername()) != null) {
            throw new AdminException("USER 已存在，请修改后重新添加");
        }
        request.setPassword(passwordEncoder.encode(request.getPassword()));
        adminRepository.userInsert(request);
    }

    @Override
    public void userUpdate(UserManageRequest request) {
        if (adminRepository.userQuery(request.getId()) == null) {
            throw new AdminException("USER 不存在，请修改后重新更改");
        }
        request.setPassword(passwordEncoder.encode(request.getPassword()));
        adminRepository.userUpdate(request);
    }

    @Override
    public void userDelete(Long id) {
        if (adminRepository.userQuery(id) == null) {
            throw new AdminException("USER 不存在，请修改后重新删除");
        }
        adminRepository.userDelete(id);
    }

    // -------------------- Config --------------------
    @Override
    public Map<String, List<ConfigVO>> configList(ConfigListRequest request) {
        List<ConfigVO> configVOList = adminRepository.configList(request);
        return configVOList.stream()
                .collect(Collectors.groupingBy(
                        ConfigVO::getClientId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    @Override
    public void configInsert(ConfigManageRequest request) {
        if (adminRepository.configQuery(request) != null) {
            throw new AdminException("CONFIG 已存在，请修改后重新添加");
        }
        adminRepository.configInsert(request);
    }

    @Override
    public void configUpdate(ConfigManageRequest request) {
        if (adminRepository.configQuery(request.getId()) == null) {
            throw new AdminException("CONFIG 不存在，请修改后重新更改");
        }
        adminRepository.configUpdate(request);
    }

    @Override
    public void configDelete(Long id) {
        ConfigVO configVO = adminRepository.configQuery(id);
        if (configVO == null) {
            throw new AdminException("CONFIG 不存在，请修改后重新删除");
        }
        assertNoDependency("客户端", adminRepository.queryAgentDependOnClient(configVO.getClientId()));
        adminRepository.configDelete(id);
    }

    @Override
    public void configToggle(Long id, Integer status) {
        ConfigVO configVO = adminRepository.configQuery(id);
        if (configVO == null) {
            throw new AdminException("CONFIG 不存在，请确认后重新切换");
        }
        if (status == 0) {
            assertNoDependency("客户端", adminRepository.queryAgentDependOnClient(configVO.getClientId()));
        }
        adminRepository.configToggle(id, status);
    }


    // -------------------- Flow --------------------
    @Override
    public List<ClientDetailVO> flowClient() {
        return adminRepository.flowClient();
    }

    @Override
    public List<FlowVO> flowAgent(String agentId) {
        return adminRepository.flowAgent(agentId);
    }

    @Override
    public void flowInsert(FlowManageRequest request) {
        if (adminRepository.flowQuery(request.getAgentId(), request.getClientId()) != null) {
            throw new AdminException("FLOW 已存在，请修改后重新添加");
        }
        adminRepository.flowInsert(request);
    }

    @Override
    public void flowUpdate(FlowManageRequest request) {
        if (adminRepository.flowQuery(request.getId()) == null) {
            throw new AdminException("FLOW 不存在，请修改后重新更改");
        }
        adminRepository.flowUpdate(request);
    }

    @Override
    public void flowDelete(Long id) {
        if (adminRepository.flowQuery(id) == null) {
            throw new AdminException("FLOW 不存在，请修改后重新删除");
        }
        adminRepository.flowDelete(id);
    }

    // -------------------- List --------------------
    @Override
    public List<String> listClientType() {
        return Arrays.stream(AiClientType.values())
                .map(AiClientType::getType)
                .toList();
    }

    @Override
    public List<String> listAgentType() {
        return Arrays.stream(AiAgentType.values())
                .map(AiAgentType::getType)
                .toList();
    }

    @Override
    public List<String> listClientRole() {
        return Arrays.stream(AiClientRole.values())
                .map(AiClientRole::getRole)
                .toList();
    }

    @Override
    public List<String> listUserRole() {
        return Arrays.stream(UserRole.values())
                .map(UserRole::getRole)
                .toList();
    }

    @Override
    public List<String> listConfigType() {
        return Arrays.stream(AiConfigType.values())
                .map(AiConfigType::getType)
                .toList();
    }

    @Override
    public List<String> listApiId() {
        return adminRepository.listApiId();
    }

    @Override
    public List<String> listModelId() {
        return adminRepository.listModelId();
    }


    private void assertNoDependency(String dependentName, List<String> dependents) {
        if (!CollectionUtils.isEmpty(dependents)) {
            throw new DependencyConflictException("存在依赖，无法执行操作，相关" + dependentName + "：" + String.join(",", dependents), dependents);
        }
    }

}
