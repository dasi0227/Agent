package com.dasi.domain.chat.repository;

import com.dasi.domain.chat.model.vo.AiMcpVO;
import com.dasi.types.dto.response.ChatClientResponse;
import com.dasi.types.dto.response.ChatMcpResponse;

import java.util.List;

public interface IChatRepository {

    List<ChatClientResponse> queryChatClientResponseList();

    List<String> queryRagTagList();

    List<ChatMcpResponse> queryChatMcpResponseList();

    List<AiMcpVO> queryAiMcpVOListByMcpIdList(List<String> mcpIdList);
}
