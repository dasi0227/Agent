USE `ai-agent`;

-- ai_api
DELETE FROM ai_api WHERE api_id = 'api_demo_1';
INSERT INTO ai_api (
    api_id,
    api_base_url,
    api_key,
    api_completions_path,
    api_embeddings_path,
    api_status
) VALUES (
    'api_demo_1',
    'https://api.example.com',
    'demo-key-123',
    '/v1/chat/completions',
    '/v1/embeddings',
    1
);

-- ai_model
DELETE FROM ai_model WHERE model_id = 'model_demo_1';
INSERT INTO ai_model (
    model_id,
    api_id,
    model_name,
    model_type,
    model_status
) VALUES (
    'model_demo_1',
    'api_demo_1',
    'demo-model',
    'chat',
    1
);

-- ai_client
DELETE FROM ai_client WHERE client_id = 'client_demo_1';
INSERT INTO ai_client (
    client_id,
    client_name,
    client_desc,
    client_status
) VALUES (
    'client_demo_1',
    'demo-client',
    'demo client',
    1
);

-- ai_mcp
DELETE FROM ai_mcp WHERE mcp_id = 'mcp_demo_1';
INSERT INTO ai_mcp (
    mcp_id,
    mcp_name,
    mcp_type,
    mcp_path,
    mcp_desc,
    mcp_timeout,
    mcp_status
) VALUES (
    'mcp_demo_1',
    'demo-mcp',
    'sse',
    'http://127.0.0.1:9001',
    'demo mcp',
    180,
    1
);

-- ai_prompt
DELETE FROM ai_prompt WHERE prompt_id = 'prompt_demo_1';
INSERT INTO ai_prompt (
    prompt_id,
    prompt_name,
    prompt_content,
    prompt_desc,
    prompt_status
) VALUES (
    'prompt_demo_1',
    'demo-prompt',
    'You are a helpful assistant.',
    'demo prompt',
    1
);

-- ai_advisor
DELETE FROM ai_advisor WHERE advisor_id = 'advisor_demo_1';
INSERT INTO ai_advisor (
    advisor_id,
    advisor_name,
    advisor_type,
    advisor_desc,
    advisor_order,
    advisor_param,
    advisor_status
) VALUES (
    'advisor_demo_1',
    'demo-advisor',
    'memory',
    'demo advisor',
    1,
    '{}',
    1
);

-- ai_config
DELETE FROM ai_config
WHERE source_type = 'client' AND source_id = 'client_demo_1';
INSERT INTO ai_config (
    source_type,
    source_id,
    target_type,
    target_id,
    config_param,
    config_status
) VALUES
    ('client', 'client_demo_1', 'model', 'model_demo_1', NULL, 1),
    ('client', 'client_demo_1', 'prompt', 'prompt_demo_1', NULL, 1),
    ('client', 'client_demo_1', 'mcp', 'mcp_demo_1', NULL, 1),
    ('client', 'client_demo_1', 'advisor', 'advisor_demo_1', NULL, 1);

-- ai_agent
DELETE FROM ai_agent WHERE agent_id = 'agent_demo_1';
INSERT INTO ai_agent (
    agent_id,
    agent_name,
    agent_channel,
    agent_desc,
    agent_status
) VALUES (
    'agent_demo_1',
    'demo-agent',
    NULL,
    'demo agent',
    1
);

-- ai_task
DELETE FROM ai_task WHERE agent_id = 'agent_demo_1' AND task_name = 'demo-task';
INSERT INTO ai_task (
    agent_id,
    task_name,
    task_cron,
    task_desc,
    task_param,
    task_status
) VALUES (
    'agent_demo_1',
    'demo-task',
    '0/5 * * * * ?',
    'demo task',
    '{}',
    1
);

-- ai_flow
DELETE FROM ai_flow WHERE agent_id = 'agent_demo_1' AND client_id = 'client_demo_1';
INSERT INTO ai_flow (
    agent_id,
    client_id,
    flow_seq
) VALUES (
    'agent_demo_1',
    'client_demo_1',
    1
);
