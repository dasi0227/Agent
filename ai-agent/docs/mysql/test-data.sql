USE `ai-agent`;

-- ai_api
DELETE
FROM ai_api;
INSERT INTO ai_api (api_id,
                    api_base_url,
                    api_key,
                    api_completions_path,
                    api_embeddings_path,
                    api_status)
VALUES ('api_demo_1',
        'https://api.minimaxi.com',
        'sk-api-HbbwFfrlQ-s_SsEUGZXzbcrJAD3fJ_GbqVsg4QXb9qGl6M8W9EaujWReoNnvEbb-li6NbtexcpO2Z711MJuSpAktGmW5wDD-Y0SiWd4HY0R1RqT05NY5im8',
        'v1/chat/completions',
        'v1/embeddings',
        1),
       ('api_demo_2',
        'https://open.bigmodel.cn',
        '1a90bb3e420e429086b8d4429a8a63d4.qHFlJlVbCzczwmA3',
        'api/paas/v4/chat/completions',
        'api/paas/v4/embeddings',
        1);

-- ai_model
DELETE
FROM ai_model;
INSERT INTO ai_model (model_id,
                      api_id,
                      model_name,
                      model_type,
                      model_status)
VALUES ('model_demo_1',
        'api_demo_1',
        'MiniMax-M2.1-lightning',
        'MiniMax',
        1),
       ('model_demo_2',
        'api_demo_2',
        'glm-4.7',
        'GLM',
        1);

-- ai_client
DELETE
FROM ai_client;
INSERT INTO ai_client (client_id,
                       client_name,
                       client_desc,
                       client_status)
VALUES ('client_demo_1',
        'demo-client',
        'demo client',
        1),
       ('client_analyzer_1',
        'analyzer-client',
        'auto execute analyzer client',
        1),
       ('client_performer_1',
        'performer-client',
        'auto execute performer client',
        1),
       ('client_supervisor_1',
        'supervisor-client',
        'auto execute supervisor client',
        1),
       ('client_summarizer_1',
        'summarizer-client',
        'auto execute summarizer client',
        1);

-- ai_mcp
DELETE
FROM ai_mcp;
INSERT INTO ai_mcp (mcp_id,
                    mcp_name,
                    mcp_type,
                    mcp_config,
                    mcp_desc,
                    mcp_timeout,
                    mcp_status)
VALUES ('mcp_sse_1',
        'mcp-server-csdn',
        'sse',
        '{"baseUri":"http://127.0.0.1:9001","sseEndPoint":"/sse"}',
        '发送 CSDN 文章',
        180,
        1),
       ('mcp_sse_2',
        'mcp-server-wecom',
        'sse',
        '{"baseUri":"http://127.0.0.1:9002","sseEndPoint":"/sse"}',
        '联网搜索',
        180,
        1),
       ('mcp_sse_3',
        'mcp-server-baidu-websearch',
        'sse',
        '{"baseUri":"http://appbuilder.baidu.com/v2/ai_search/mcp/","sseEndPoint":"sse?api_key=bce-v3/ALTAK-gzaY0Q1GYuLlHL4PlJYw0/b97cb1bb7cf0ae324b51f55fab04f12c9fa51acd"}',
        '联网搜索',
        180,
        1),
       ('mcp_stdio_1',
        'filesystem',
        'stdio',
        '{"filesystem":{"command":"npx","args":["-y","@modelcontextprotocol/server-filesystem","/Users/wyw/Downloads","/Users/wyw/Downloads"],"env":{}}}',
        '处理本机的文件系统',
        180,
        1);

-- ai_prompt
DELETE
FROM ai_prompt;
INSERT INTO ai_prompt (prompt_id,
                       prompt_name,
                       prompt_content,
                       prompt_desc,
                       prompt_status)
VALUES ('prompt_demo_1',
        'demo-prompt',
        'You are a helpful assistant.',
        'demo prompt',
        1),
       ('prompt_analyzer_1',
        'analyzer-prompt',
        '',
        'auto execute analyzer system prompt',
        1),
       ('prompt_performer_1',
        'performer-prompt',
        '',
        'auto execute performer system prompt',
        1),
       ('prompt_supervisor_1',
        'supervisor-prompt',
        '',
        'auto execute supervisor system prompt',
        1),
       ('prompt_summarizer_1',
        'summarizer-prompt',
        '',
        'auto execute summarizer system prompt',
        1);

-- ai_advisor
DELETE
FROM ai_advisor;
INSERT INTO ai_advisor (advisor_id,
                        advisor_name,
                        advisor_type,
                        advisor_desc,
                        advisor_order,
                        advisor_param,
                        advisor_status)
VALUES ('advisor_chat_memory_1',
        'ChatMemoryAdvisor',
        'ChatMemory',
        '对话历史记忆',
        1,
        '{"maxMessages":5}',
        1),
       ('advisor_rag_answer_1',
        'RagAnswerAdvisor',
        'RagAnswer',
        'Rag 知识库',
        2,
        '{"topK":4,"filterExpression":"knowledge == ''dasi-info''"}',
        1);

-- ai_config
DELETE
FROM ai_config;
INSERT INTO ai_config (source_type,
                       source_id,
                       target_type,
                       target_id,
                       config_param,
                       config_status)
VALUES ('client', 'client_analyzer_1', 'model', 'model_demo_1', NULL, 1),
       ('client', 'client_analyzer_1', 'prompt', 'prompt_analyzer_1', NULL, 1),
       ('client', 'client_analyzer_1', 'advisor', 'advisor_chat_memory_1', NULL, 1),

       ('client', 'client_performer_1', 'model', 'model_demo_1', NULL, 1),
       ('client', 'client_performer_1', 'prompt', 'prompt_performer_1', NULL, 1),
       ('client', 'client_performer_1', 'advisor', 'advisor_chat_memory_1', NULL, 1),
       ('client', 'client_performer_1', 'mcp', 'mcp_sse_3', NULL, 1),
       ('client', 'client_performer_1', 'mcp', 'mcp_stdio_1', NULL, 1),

       ('client', 'client_supervisor_1', 'model', 'model_demo_1', NULL, 1),
       ('client', 'client_supervisor_1', 'prompt', 'prompt_supervisor_1', NULL, 1),
       ('client', 'client_supervisor_1', 'advisor', 'advisor_chat_memory_1', NULL, 1),
       ('client', 'client_supervisor_1', 'mcp', 'mcp_sse_3', NULL, 1),

       ('client', 'client_summarizer_1', 'model', 'model_demo_1', NULL, 1),
       ('client', 'client_summarizer_1', 'prompt', 'prompt_summarizer_1', NULL, 1),
       ('client', 'client_summarizer_1', 'advisor', 'advisor_chat_memory_1', NULL, 1),
       ('client', 'client_summarizer_1', 'mcp', 'mcp_stdio_1', NULL, 1),

       ('model', 'model_demo_1', 'mcp', 'mcp_sse_3', NULL, 1),
       ('model', 'model_demo_1', 'mcp', 'mcp_stdio_1', NULL, 1),
       ('client', 'client_demo_1', 'model', 'model_demo_2', NULL, 1),
       ('client', 'client_demo_1', 'prompt', 'prompt_demo_1', NULL, 1),
       ('client', 'client_demo_1', 'mcp', 'mcp_sse_1', NULL, 1),
       ('client', 'client_demo_1', 'mcp', 'mcp_stdio_1', NULL, 1),
       ('client', 'client_demo_1', 'advisor', 'advisor_chat_memory_1', NULL, 1),
       ('client', 'client_demo_1', 'advisor', 'advisor_rag_answer_1', NULL, 1);

-- ai_agent
DELETE
FROM ai_agent;
INSERT INTO ai_agent (agent_id,
                      agent_name,
                      agent_channel,
                      agent_desc,
                      agent_status)
VALUES ('agent_demo_1',
        'demo-agent',
        NULL,
        'demo agent',
        1);

-- ai_task
DELETE
FROM ai_task;
INSERT INTO ai_task (agent_id,
                     task_name,
                     task_cron,
                     task_desc,
                     task_param,
                     task_status)
VALUES ('agent_demo_1',
        'demo-task',
        '0/5 * * * * ?',
        'demo task',
        '{}',
        1);

-- ai_flow
DELETE
FROM ai_flow;
INSERT INTO ai_flow (agent_id,
                     client_id,
                     client_name,
                     client_type,
                     flow_seq)
VALUES ('agent_demo_1',
        'client_analyzer_1',
        '任务分析器',
        'analyzer',
        1),
       ('agent_demo_1',
        'client_performer_1',
        '任务执行器',
        'performer',
        2),
       ('agent_demo_1',
        'client_supervisor_1',
        '任务监督器',
        'supervisor',
        3),
       ('agent_demo_1',
        'client_summarizer_1',
        '任务总结器',
        'summarizer',
        4);
