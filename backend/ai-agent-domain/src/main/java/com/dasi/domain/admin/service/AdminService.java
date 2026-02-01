package com.dasi.domain.admin.service;

import com.dasi.domain.admin.model.vo.*;
import com.dasi.domain.admin.repository.IAdminRepository;
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
        if (adminRepository.apiQuery(request.getApiId()) == null) {
            throw new AdminException("API 的 ID 已存在，请修改后重新添加");
        }
        adminRepository.apiInsert(request);
    }

    @Override
    public void apiUpdate(ApiManageRequest request) {
        if (adminRepository.apiQuery(request.getApiId()) == null) {
            throw new AdminException("API 的 ID 不存在，请确认后重新更改");
        }
        adminRepository.apiUpdate(request);
    }

    @Override
    public void apiDelete(String apiId) {
        if (adminRepository.apiQuery(apiId) == null) {
            throw new AdminException("API 的 ID 不存在，请确认后重新删除");
        }
        assertNoDependency("模型", adminRepository.queryModelDependOnApi(apiId));
        adminRepository.apiDelete(apiId);
    }

    @Override
    public void apiToggle(String apiId, Integer apiStatus) {
        if (adminRepository.apiQuery(apiId) == null) {
            throw new AdminException("API 的 ID 不存在，请确认后重新切换");
        }
        if (apiStatus == 0) {
            assertNoDependency("模型", adminRepository.queryModelDependOnApi(apiId));
        }
        adminRepository.apiToggle(apiId, apiStatus);
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
            throw new AdminException("MODEL 的 ID 已存在，请修改后重新添加");
        }
        adminRepository.modelInsert(request);
    }

    @Override
    public void modelUpdate(ModelManageRequest request) {
        if (adminRepository.modelQuery(request.getModelId()) == null) {
            throw new AdminException("MODEL 的 ID 不存在，请确认后重新更改");
        }
        adminRepository.modelUpdate(request);
    }

    @Override
    public void modelDelete(String modelId) {
        if (adminRepository.modelQuery(modelId) == null) {
            return;
        }
        assertNoDependency("客户端", adminRepository.queryClientDependOnModel(modelId));
        adminRepository.modelDelete(modelId);
    }

    @Override
    public void modelToggle(String modelId, Integer modelStatus) {
        if (adminRepository.modelQuery(modelId) == null) {
            throw new AdminException("MODEL 的 ID 不存在，请确认后重新切换");
        }
        if (modelStatus == 0) {
            assertNoDependency("客户端", adminRepository.queryClientDependOnModel(modelId));
        }
        adminRepository.modelToggle(modelId, modelStatus);
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
            throw new AdminException("MCP 的 ID 已存在，请修改后重新添加");
        }
        adminRepository.mcpInsert(request);
    }

    @Override
    public void mcpUpdate(McpManageRequest request) {
        if (adminRepository.mcpQuery(request.getMcpId()) == null) {
            throw new AdminException("MCP 的 ID 不存在，请确认后重新更改");
        }
        adminRepository.mcpUpdate(request);
    }

    @Override
    public void mcpDelete(String mcpId) {
        if (adminRepository.mcpQuery(mcpId) == null) {
            throw new AdminException("MCP 的 ID 不存在，请确认后重新删除");
        };
        assertNoDependency("客户端", adminRepository.queryClientDependOnMcp(mcpId));
        adminRepository.mcpDelete(mcpId);
    }

    @Override
    public void mcpToggle(String mcpId, Integer mcpStatus) {
        if (adminRepository.mcpQuery(mcpId) == null) {
            throw new AdminException("MCP 的 ID 不存在，请确认后重新切换");
        }
        if (mcpStatus == 0) {
            assertNoDependency("客户端", adminRepository.queryClientDependOnMcp(mcpId));
        }
        adminRepository.mcpToggle(mcpId, mcpStatus);
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
            throw new AdminException("ADVISOR 的 ID 已存在，请修改后重新添加");
        }
        adminRepository.advisorInsert(request);
    }

    @Override
    public void advisorUpdate(AdvisorManageRequest request) {
        if (adminRepository.advisorQuery(request.getAdvisorId()) == null) {
            throw new AdminException("ADVISOR 的 ID 不存在，请确认后重新更改");
        }
        adminRepository.advisorUpdate(request);
    }

    @Override
    public void advisorDelete(String advisorId) {
        if (adminRepository.advisorQuery(advisorId) == null) {
            throw new AdminException("ADVISOR 的 ID 不存在，请确认后重新删除");
        }
        assertNoDependency("客户端", adminRepository.queryClientDependOnAdvisor(advisorId));
        adminRepository.advisorDelete(advisorId);
    }

    @Override
    public void advisorToggle(String advisorId, Integer status) {
        if (adminRepository.advisorQuery(advisorId) == null) {
            throw new AdminException("ADVISOR 的 ID 不存在，请确认后重新切换");
        }
        if (status == 0) {
            assertNoDependency("客户端", adminRepository.queryClientDependOnAdvisor(advisorId));
        }
        adminRepository.advisorToggle(advisorId, status);
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
            throw new AdminException("PROMPT 的 ID 已存在，请修改后重新添加");
        }
        adminRepository.promptInsert(request);
    }

    @Override
    public void promptUpdate(PromptManageRequest request) {
        if (adminRepository.promptQuery(request.getPromptId()) == null) {
            throw new AdminException("PROMPT 的 ID 不存在，请确认后重新更改");
        }
        adminRepository.promptUpdate(request);
    }

    @Override
    public void promptDelete(String promptId) {
        if (adminRepository.promptQuery(promptId) == null) {
            throw new AdminException("PROMPT 的 ID 不存在，请确认后重新删除");
        }
        assertNoDependency("客户端", adminRepository.queryClientDependOnPrompt(promptId));
        adminRepository.promptDelete(promptId);
    }

    @Override
    public void promptToggle(String promptId, Integer status) {
        if (adminRepository.promptQuery(promptId) == null) {
            throw new AdminException("PROMPT 的 ID 不存在，请确认后重新切换");
        }
        if (status == 0) {
            assertNoDependency("客户端", adminRepository.queryClientDependOnPrompt(promptId));
        }
        adminRepository.promptToggle(promptId, status);
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
            throw new AdminException("CLIENT 的 ID 已存在，请修改后重新添加");
        }
        adminRepository.clientInsert(request);
    }

    @Override
    public void clientUpdate(ClientManageRequest request) {
        if (adminRepository.clientQuery(request.getClientId()) == null) {
            throw new AdminException("CLIENT 的 ID 不存在，请确认后重新更改");
        }
        adminRepository.clientUpdate(request);
    }

    @Override
    public void clientDelete(String clientId) {
        if (adminRepository.clientQuery(clientId) == null) {
            throw new AdminException("CLIENT 的 ID 不存在，请确认后重新删除");
        }
        assertNoDependency("工作流", extractFlowDependents(adminRepository.queryFlowListByClientId(clientId)));
        adminRepository.clientDelete(clientId);
    }

    @Override
    public void clientToggle(String clientId, Integer status) {
        if (adminRepository.clientQuery(clientId) == null) {
            throw new AdminException("CLIENT 的 ID 不存在，请确认后重新切换");
        }
        if (status == 0) {
            assertNoDependency("工作流", extractFlowDependents(adminRepository.queryFlowListByClientId(clientId)));
        }
        adminRepository.clientToggle(clientId, status);
    }

    // -------------------- Agent --------------------
    @Override
    public PageResult<AdminAgentVO> agentPage(AgentPageRequest request) {
        List<AdminAgentVO> voList = adminRepository.agentPage(request);
        Integer total = adminRepository.agentCount(request);
        Integer size = request.getPageSize();
        Integer pageSum = (total + size - 1) / size;
        return PageResult.<AdminAgentVO>builder()
                .list(voList)
                .total(total)
                .pageSum(pageSum)
                .pageNum(request.getPageNum())
                .pageSize(size)
                .build();
    }

    @Override
    public void agentInsert(AgentManageRequest request) {
        if (adminRepository.agentQuery(request.getAgentId()) != null) {
            throw new AdminException("AGENT 的 ID 已存在，请修改后重新添加");
        }
        adminRepository.agentInsert(request);
    }

    @Override
    public void agentUpdate(AgentManageRequest request) {
        if (adminRepository.agentQuery(request.getAgentId()) == null) {
            throw new AdminException("AGENT 的 ID 不存在，请确认后重新更改");
        }
        adminRepository.agentUpdate(request);
    }

    @Override
    public void agentDelete(String agentId) {
        if (adminRepository.agentQuery(agentId) == null) {
            throw new AdminException("AGENT 的 ID 不存在，请确认后重新删除");
        }
        adminRepository.agentDelete(agentId);
    }

    @Override
    public void agentToggle(String agentId, Integer status) {
        if (adminRepository.agentQuery(agentId) == null) {
            throw new AdminException("AGENT 的 ID 不存在，请确认后重新切换");
        }
        adminRepository.agentToggle(agentId, status);
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
        String username = request.getUsername().trim();
        if (adminRepository.userQuery(username) != null) {
            throw new AdminException("用户名已存在");
        }
        request.setUsername(username);
        request.setPassword(passwordEncoder.encode(request.getPassword()));
        request.setRole(StringUtils.hasText(request.getRole()) ? request.getRole().trim() : "account");
        adminRepository.userInsert(request);
    }

    @Override
    public void userUpdate(UserManageRequest request) {
        UserVO dbUser = adminRepository.userQuery(request.getUsername());
        if (dbUser == null) {
            throw new AdminException("用户不存在");
        }
        request.setPassword(passwordEncoder.encode(request.getPassword()));
        adminRepository.userUpdate(request);
    }

    @Override
    public void userDelete(String userId) {
        UserVO dbUser = adminRepository.userQuery(userId);
        if (dbUser == null) {
            throw new AdminException("用户不存在");
        }
        adminRepository.userDelete(userId);
    }

    // -------------------- helper --------------------
    private List<String> extractFlowDependents(List<AdminFlow> flows) {
        if (CollectionUtils.isEmpty(flows)) {
            return List.of();
        }
        return flows.stream()
                .map(AdminFlow::getFlowId)
                .filter(StringUtils::hasText)
                .toList();
    }

    private void assertNoDependency(String dependentName, List<String> dependents) {
        if (!CollectionUtils.isEmpty(dependents)) {
            throw new DependencyConflictException("存在依赖，无法执行操作，相关" + dependentName + "：" + String.join(",", dependents), dependents);
        }
    }

}
