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
        'https://api.deepseek.com/',
        'sk-795b8c28cfdc4c82beb38bce7f729c4f',
        '/v1/chat/completions',
        '/v1/embeddings',
        1),
       ('api_demo_2',
        'https://open.bigmodel.cn/',
        '8d64e63b2fe447b6923040f1c8bfbb5b.OEvd8dp4wJa2f1qr',
        '/api/paas/v4/chat/completions',
        '/api/paas/v4/embeddings',
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
        'deepseek-chat',
        'DeepSeek',
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
        'mcp-server-wecom',
        'sse',
        '{"baseUri":"http://127.0.0.1:9002","sseEndPoint":"/sse"}',
        '发送企业微信的应用消息',
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
VALUES ('advisor_demo_1',
        'ChatMemoryAdvisor',
        'ChatMemory',
        '对话历史记忆',
        1,
        '{"maxMessages":5}',
        1),
       ('advisor_demo_2',
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
VALUES ('client', 'client_demo_1', 'model', 'model_demo_2', NULL, 1),
       ('client', 'client_demo_1', 'prompt', 'prompt_demo_1', NULL, 1),
       ('client', 'client_demo_1', 'mcp', 'mcp_sse_1', NULL, 1),
       ('client', 'client_demo_1', 'mcp', 'mcp_stdio_1', NULL, 1),
       ('client', 'client_demo_1', 'advisor', 'advisor_demo_1', NULL, 1),
       ('client', 'client_demo_1', 'advisor', 'advisor_demo_2', NULL, 1),
       ('model', 'model_demo_2', 'mcp', 'mcp_sse_1', NULL, 1),
       ('model', 'model_demo_2', 'mcp', 'mcp_stdio_1', NULL, 1);

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
                     flow_seq)
VALUES ('agent_demo_1',
        'client_demo_1',
        1);
