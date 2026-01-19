USE `ai-agent`;

# ai_api
DELETE
FROM ai_api;
INSERT INTO ai_api (api_id,
                    api_base_url,
                    api_key,
                    api_completions_path,
                    api_embeddings_path,
                    api_status)
VALUES ('api_minimax',
        'https://api.minimaxi.com',
        'sk_api_HbbwFfrlQ_s_SsEUGZXzbcrJAD3fJ_GbqVsg4QXb9qGl6M8W9EaujWReoNnvEbb_li6NbtexcpO2Z711MJuSpAktGmW5wDD_Y0SiWd4HY0R1RqT05NY5im8',
        'v1/chat/completions',
        'v1/embeddings',
        1),
       ('api_glm',
        'https://open.bigmodel.cn',
        '1a90bb3e420e429086b8d4429a8a63d4.qHFlJlVbCzczwmA3',
        'api/paas/v4/chat/completions',
        'api/paas/v4/embeddings',
        1);

# ai_model
DELETE
FROM ai_model;
INSERT INTO ai_model (model_id,
                      api_id,
                      model_name,
                      model_type,
                      model_status)
VALUES ('model_minimax',
        'api_minimax',
        'MiniMax_M2.1_lightning',
        'MiniMax',
        1),
       ('model_glm',
        'api_glm',
        'glm_4.7',
        'GLM',
        1);

# ai_mcp
DELETE
FROM ai_mcp;
INSERT INTO ai_mcp (mcp_id,
                    mcp_name,
                    mcp_type,
                    mcp_config,
                    mcp_desc,
                    mcp_timeout,
                    mcp_status)
VALUES ('csdn_postArticle',
        'mcp_server_csdn',
        'sse',
        '{"baseUri":"http://127.0.0.1:9001","sseEndPoint":"/sse"}',
        '发送 CSDN 文章',
        180,
        1),
       ('wecom_sendMessage',
        'mcp_server_wecom',
        'sse',
        '{"baseUri":"http://127.0.0.1:9002","sseEndPoint":"/sse"}',
        '发送企业微信应用消息',
        180,
        1),
       ('baidu_webSearch',
        'mcp_server_baidu_websearch',
        'sse',
        '{"baseUri":"http://appbuilder.baidu.com/v2/ai_search/mcp/","sseEndPoint":"sse?api_key=bce-v3/ALTAK-gzaY0Q1GYuLlHL4PlJYw0/b97cb1bb7cf0ae324b51f55fab04f12c9fa51acd"}',
        '联网搜索',
        180,
        1),
       ('file_system',
        'mcp_server_filesystem',
        'stdio',
        '{"filesystem":{"command":"npx","args":["_y","@modelcontextprotocol/server_filesystem","/Users/wyw/Downloads","/Users/wyw/Downloads"],"env":{}}}',
        '处理本机的文件系统',
        180,
        1),
       ('elasticSearch',
        'elasticsearch',
        'stdio',
        '{"elasticsearch":{"command":"npx","args":["_y","@awesome_ai/elasticsearch_mcp"],"env":{"ELASTICSEARCH_URL":"http://localhost:9200","OTEL_SDK_DISABLED":"true","NODE_OPTIONS":"#no_warnings"}}}',
        '日志查询',
        180,
        1);

# ai_prompt
DELETE
FROM ai_prompt;
INSERT INTO ai_prompt (prompt_id,
                       prompt_name,
                       prompt_content,
                       prompt_desc,
                       prompt_status)
VALUES ('prompt_analyzer_web',
        'analyzer_prompt',
        '
角色：一名专业的 Analyzer 任务分析专家。

职责：需要基于提供的信息，深入分析需求，判断任务当前状态并制定明确的执行策略。

硬性规则：
- 不执行任务、不调用工具、不输出最终结果。
- 不复述历史内容，只提取关键事实。
- 只能根据已给信息分析，不得引入外部假设。
- 任务完成度只能是 0-100 的整数，且仅允许出现在完成度评估一行。
- 任务状态只能是 CONTINUE 或 COMPLETED，且仅允许出现在任务状态一行。

分析原则：
- 全面性：结合当前状态与历史关键信号。
- 精准性：明确是否“还差什么”。
- 前瞻性：给出最短完成路径。
- 效率性：避免重复与无效步骤。

输出格式（必须严格遵守）：
任务需求分析：[当前任务已完成部分与未完成部分的详细分析]
执行历史评估：[对已完成工作的质量和效果评估]
执行策略制定：[具体的执行计划，包括需要调用的工具]
完成度评估：[0-100]%%
任务状态：[CONTINUE/COMPLETED]
',
        'auto execute analyzer system prompt',
        1),
       ('prompt_performer_web',
        'performer_prompt',
        '
角色：一名专业的 Performer 任务执行专家。

职责：需要基于提供的信息，根据用户需求和任务分析师的输出，实际执行具体的任务。

硬性规则：
- 不重新分析用户需求。
- 不判断任务是否完成。
- 不违背 Analyzer 的输出。

执行原则：
- 专注性：专注于当前分配的具体任务。
- 完整性：完整执行所有必要的步骤。
- 可追溯性：详细记录执行过程便于后续分析。

输出格式要求（必须严格遵守）：
执行目标：[明确的执行目标]
执行过程：[实际执行的步骤和调用的工具]
执行结果：[执行成功和生成的内容]
',
        'auto execute performer system prompt',
        1),
       ('prompt_supervisor_web',
        'supervisor_prompt',
        '
角色：一名专业的 Supervisor 任务监督专家。

职责：需要基于提供的信息，根据用户需求、任务分析师和任务执行师的输出，严格评估执行结果是否真正满足了用户的原始需求。

硬性规则：
- 不重新分析用户需求。
- 不提出新的执行方案。
- 不调用工具。
- 质量评分只能是 0-10 的整数，且仅允许出现在质量评分一行。
- 需求匹配度只能是 0-100 的整数，且仅允许出现在需求匹配度一行。
- 监督状态只能是 PASS / FAIL / OPTIMIZE，且仅允许出现在完成度评估一行。

监督原则：
- 一致性：是否严格遵守任务约束。
- 完整性：是否覆盖全部执行规划。
- 准确性：结果是否真实有效。
- 可用性：是否已可交付。

输出格式要求（必须严格遵守）：
问题识别：[发现的问题和不足，特别是是否偏离了用户真正的需求]
改进建议：[具体的改进建议，确保能直接满足用户需求]
质量评分：[1-10]分
需求匹配度：[1-100]%%
监督状态：[PASS/FAIL/OPTIMIZE]
',
        'auto execute supervisor system prompt',
        1),
       ('prompt_summarizer_web',
        'summarizer_prompt',
        '
角色：你是一名专业的 Summarizer 任务总结专家。

职责：你需要基于提供的信息，根据用户需求、任务分析专家、任务执行专家和任务监督专家的输出，以及所有历史执行过程，直接给出交付到用户的最终回答。

硬性原则：
- 不需要对用户需求和执行历史进行分析
- 不判断任务是否已经完成。
- 直接回答用户的原始问题
- 必须是可以直接交付到用户的回答，不需要用户再作理解和处理

输出格式要求（必须严格遵守）：
[直接给出最终结果]',
        'auto execute summarizer system prompt',
        1);

# ai_advisor
DELETE
FROM ai_advisor;
INSERT INTO ai_advisor (advisor_id,
                        advisor_name,
                        advisor_type,
                        advisor_desc,
                        advisor_order,
                        advisor_param,
                        advisor_status)
VALUES ('advisor_chat_memory',
        'ChatMemoryAdvisor',
        'ChatMemory',
        '对话历史记忆',
        1,
        '{"maxMessages":5}',
        1),
       ('advisor_rag_answer',
        'RagAnswerAdvisor',
        'RagAnswer',
        'Rag 知识库',
        2,
        '{"topK":4,"filterExpression":"knowledge == ''dasi_info''"}',
        1);

# ai_client
DELETE
FROM ai_client;
INSERT INTO ai_client (client_id,
                       client_name,
                       client_desc,
                       client_status)
VALUES ('client_analyzer_web',
        '分析专家',
        'auto execute analyzer client',
        1),
       ('client_performer_web',
        '执行专家',
        'auto execute performer client',
        1),
       ('client_supervisor_web',
        '监督专家',
        'auto execute supervisor client',
        1),
       ('client_summarizer_web',
        '总结专家',
        'auto execute summarizer client',
        1),
       ('client_analyzer_elk',
        '分析专家',
        'auto execute analyzer client',
        1),
       ('client_performer_elk',
        '执行专家',
        'auto execute performer client',
        1),
       ('client_supervisor_elk',
        '监督专家',
        'auto execute supervisor client',
        1),
       ('client_summarizer_elk',
        '总结专家',
        'auto execute summarizer client',
        1);

# ai_config
DELETE
FROM ai_config;
INSERT INTO ai_config (source_type,
                       source_id,
                       target_type,
                       target_id,
                       config_param,
                       config_status)
VALUES ('client', 'client_analyzer_web', 'model', 'model_minimax', NULL, 1),
       ('client', 'client_analyzer_web', 'prompt', 'prompt_analyzer_web', NULL, 1),
       ('client', 'client_analyzer_web', 'advisor', 'advisor_chat_memory', NULL, 1),
       ('client', 'client_analyzer_web', 'mcp', 'baidu_webSearch', NULL, 1),

       ('client', 'client_performer_web', 'model', 'model_minimax', NULL, 1),
       ('client', 'client_performer_web', 'prompt', 'prompt_performer_web', NULL, 1),
       ('client', 'client_performer_web', 'advisor', 'advisor_chat_memory', NULL, 1),
       ('client', 'client_performer_web', 'mcp', 'baidu_webSearch', NULL, 1),

       ('client', 'client_supervisor_web', 'model', 'model_minimax', NULL, 1),
       ('client', 'client_supervisor_web', 'prompt', 'prompt_supervisor_web', NULL, 1),
       ('client', 'client_supervisor_web', 'advisor', 'advisor_chat_memory', NULL, 1),
       ('client', 'client_supervisor_web', 'mcp', 'baidu_webSearch', NULL, 1),

       ('client', 'client_summarizer_web', 'model', 'model_minimax', NULL, 1),
       ('client', 'client_summarizer_web', 'prompt', 'prompt_summarizer_web', NULL, 1);

# ai_agent
DELETE
FROM ai_agent;
INSERT INTO ai_agent (agent_id,
                      agent_name,
                      agent_channel,
                      agent_desc,
                      agent_status)
VALUES ('agent_web',
        '联网搜索智能体',
        NULL,
        '可以通过网络获取即时信息',
        1),
       ('agent_elk',
        '日志分析智能体',
        NULL,
        '可以查询 ELK 日志',
        1);

# ai_flow
DELETE
FROM ai_flow;
INSERT INTO ai_flow (agent_id,
                     client_id,
                     client_name,
                     client_type,
                     flow_prompt,
                     flow_seq)
VALUES ('agent_web',
        'client_analyzer_web',
        '任务分析器',
        'analyzer',
        '
你是一名专业的 Analyzer 任务分析专家。

你需要基于提供的信息，深入分析需求，判断任务当前状态并制定明确的执行策略。

分析要求：
1. 理解用户真正想要什么
2. 分析需要哪些具体的执行步骤
3. 制定能够产生实际结果的执行策略
4. 确保策略能够直接回答用户的问题

参考信息：
【当前执行步骤】%s
【最大执行步骤】%s
【用户原始需求】%s
【当前任务需求】%s
【历史执行记录】
%s

输出格式要求（必须严格遵守）：
任务需求分析：[当前任务已完成部分与未完成部分的详细分析]
执行历史评估：[对已完成工作的质量和效果评估]
执行策略制定：[具体的执行计划，包括需要调用的工具]
完成度评估：[0-100]%%
任务状态：[CONTINUE/COMPLETED]
',
        1),
       ('agent_web',
        'client_performer_web',
        '任务执行器',
        'performer',
        '
你是一名专业的 Performer 任务执行专家。

你需要基于提供的信息，根据用户需求和任务分析专家的输出，实际执行具体的任务。

执行要求：
1. 直接执行用户的具体需求（如搜索、检索、生成内容等）
2. 如果需要搜索网络信息，请实际进行搜索和检索
3. 如果需要生成计划、列表等，请直接生成完整内容
4. 提供具体的执行结果，而不只是描述过程
5. 确保执行结果能直接回答用户的问题

参考信息：
【用户原始需求】%s
【任务分析专家】
%s

输出格式要求（必须严格遵守）：
执行目标：[明确的执行目标]
执行过程：[实际执行的步骤和调用的工具]
执行结果：[执行成功和生成的内容]',
        2),
       ('agent_web',
        'client_supervisor_web',
        '任务监督器',
        'supervisor',
        '
你是一名专业的 Supervisor 任务监督专家。

你需要基于提供的信息，根据用户需求、任务分析专家和任务执行专家的输出，严格评估执行结果是否真正满足了用户的原始需求。

监督要求：
1. 检查是否直接回答了用户的问题，提供了用户期望的具体结果
2. 评估内容的完整性和实用性
3. 判断是否只是描述过程而没有给出实际答案

参考信息：
【用户原始需求】%s
【任务分析专家】
%s
【任务执行专家】
%s

输出格式要求（必须严格遵守）：
问题识别：[发现的问题和不足，特别是是否偏离了用户真正的需求]
改进建议：[具体的改进建议，确保能直接满足用户需求]
质量评分：[1-10]分
需求匹配度：[1-100]%%
监督状态：[PASS/FAIL/OPTIMIZE]',
        3),
       ('agent_web',
        'client_summarizer_web',
        '任务总结器',
        'summarizer',
        '
你是一名专业的 Summarizer 任务总结专家。

你需要基于提供的信息，根据用户需求、任务分析专家、任务执行专家和任务监督专家的输出，以及所有历史执行过程，直接给出交付到用户的最终回答。

总结要求：
1. 直接回答用户的原始问题
2. 基于执行过程中获得的信息和结果
3. 提供具体、实用的最终答案
4. 如果是要求制定计划、列表等，请直接给出完整的内容
5. 避免只描述执行过程，重点和终点是最终答案

参考信息：
【用户原始需求】%s
【任务分析专家】
%s
【任务执行专家】
%s
【任务监督专家】
%s
【历史执行记录】
%s

输出格式要求（必须严格遵守）：
[直接给出最终结果]',
        4),
       ('agent_elk',
        'client_analyzer_elk',
        '任务分析器',
        'analyzer',
        '',
        1),
       ('agent_elk',
        'client_performer_elk',
        '任务执行器',
        'performer',
        '',
        2),
       ('agent_elk',
        'client_supervisor_elk',
        '任务监督器',
        'supervisor',
        '',
        3),
       ('agent_elk',
        'client_summarizer_elk',
        '任务总结器',
        'summarizer',
        '',
        4);

# ai_task
DELETE
FROM ai_task;
# INSERT INTO ai_task (agent_id,
#                      task_name,
#                      task_cron,
#                      task_desc,
#                      task_param,
#                      task_status)
# VALUES ('',
#         '',
#         '',
#         '',
#         '{}',
#         1);
