CREATE DATABASE IF NOT EXISTS `ai-agent` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

USE `ai-agent`;

# ai_api 表：存储 OpenAI 的 API 信息
DROP TABLE IF EXISTS `ai_api`;
CREATE TABLE `ai_api`
(
    `id`                   BIGINT       NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '自增 id',
    `api_id`               VARCHAR(32)  NOT NULL UNIQUE COMMENT '接口 id',
    `api_base_url`         VARCHAR(255) NOT NULL COMMENT '基础路径',
    `api_key`              VARCHAR(255) NOT NULL COMMENT '密钥',
    `api_completions_path` VARCHAR(255) NOT NULL COMMENT '对话路径',
    `api_embeddings_path`  VARCHAR(255) NOT NULL COMMENT '嵌入路径',
    `api_status`           TINYINT      NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
    `create_time`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '接口配置表';

# ai_model 表：添加 API、模型、MCP 工具
DROP TABLE IF EXISTS `ai_model`;
CREATE TABLE `ai_model`
(
    `id`           BIGINT      NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '自增 id',
    `model_id`     VARCHAR(32) NOT NULL UNIQUE COMMENT '模型 id',
    `api_id`       VARCHAR(32) NOT NULL COMMENT '接口 id',
    `model_name`   VARCHAR(32) NOT NULL COMMENT '模型名称',
    `model_type`   VARCHAR(32) NOT NULL COMMENT '模型类型',
    `model_status` tinyINT     NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
    `create_time`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='模型配置表';

# ai_client：添加系统提示词，顾问角色
DROP TABLE IF EXISTS `ai_client`;
CREATE TABLE `ai_client`
(
    `id`            BIGINT       NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '自增 ID',
    `client_id`     VARCHAR(32)  NOT NULL UNIQUE COMMENT '客户端 id',
    `client_name`   VARCHAR(32)  NOT NULL COMMENT '客户端名称',
    `client_desc`   VARCHAR(255) NOT NULL DEFAULT '暂无' COMMENT '客户端描述',
    `client_status` TINYINT      NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='客户端配置表';

# ai_mcp：配置 MCP 工具
DROP TABLE IF EXISTS `ai_mcp`;
CREATE TABLE `ai_mcp`
(
    `id`          BIGINT       NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '自增 id',
    `mcp_id`      VARCHAR(32)  NOT NULL UNIQUE COMMENT '工具 id',
    `mcp_name`    VARCHAR(32)  NOT NULL COMMENT '工具名称',
    `mcp_type`    VARCHAR(32)  NOT NULL COMMENT '工具类型',
    `mcp_config`  TEXT         NOT NULL COMMENT '工具配置',
    `mcp_desc`    VARCHAR(255) NOT NULL DEFAULT '暂无' COMMENT '工具描述',
    `mcp_timeout` INT          NOT NULL DEFAULT '180' COMMENT '请求超时时间',
    `mcp_status`  TINYINT      NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='工具配置表';

# ai_prompt：配置系统提示词
DROP TABLE IF EXISTS `ai_prompt`;
CREATE TABLE `ai_prompt`
(
    `id`             BIGINT       NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '自增 id',
    `prompt_id`      VARCHAR(32)  NOT NULL UNIQUE COMMENT '提示词 id',
    `prompt_name`    VARCHAR(32)  NOT NULL COMMENT '提示词名称',
    `prompt_content` TEXT         NOT NULL COMMENT '提示词内容',
    `prompt_desc`    VARCHAR(255) NOT NULL DEFAULT '暂无' COMMENT '提示词描述',
    `prompt_status`  TINYINT      NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='系统提示词配置表';

# ai_advisor：配置顾问角色
DROP TABLE IF EXISTS `ai_advisor`;
CREATE TABLE `ai_advisor`
(
    `id`             BIGINT       NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '自增 id',
    `advisor_id`     VARCHAR(32)  NOT NULL UNIQUE COMMENT '顾问 id',
    `advisor_name`   VARCHAR(32)  NOT NULL COMMENT '顾问名称',
    `advisor_type`   VARCHAR(32)  NOT NULL COMMENT '顾问类型',
    `advisor_desc`   VARCHAR(255) NOT NULL DEFAULT '暂无' COMMENT '顾问描述',
    `advisor_order`  TINYINT      NOT NULL DEFAULT '0' COMMENT '顾问顺序号',
    `advisor_param`  TEXT         NULL     DEFAULT NULL COMMENT '顾问参数配置',
    `advisor_status` TINYINT      NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='顾问配置表';

# ai_config：用于存储 client 的关联信息
DROP TABLE IF EXISTS `ai_config`;
CREATE TABLE `ai_config`
(
    `id`            BIGINT      NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '自增 id',
    `source_type`   VARCHAR(32) NOT NULL COMMENT '源类型',
    `source_id`     VARCHAR(32) NOT NULL COMMENT '源 id',
    `target_type`   VARCHAR(32) NOT NULL COMMENT '目标类型',
    `target_id`     VARCHAR(32) NOT NULL COMMENT '目标 id',
    `config_param`  TEXT        NULL     DEFAULT NULL COMMENT '关联参数配置',
    `config_status` TINYINT     NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
    `create_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='客户端关联表';

# ai_agent：智能体的元信息
DROP TABLE IF EXISTS `ai_agent`;
CREATE TABLE `ai_agent`
(
    `id`            BIGINT       NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '自增 id',
    `agent_id`      VARCHAR(32)  NOT NULL UNIQUE COMMENT '全局 id',
    `agent_name`    VARCHAR(32)  NOT NULL COMMENT '智能体名称',
    `agent_channel` VARCHAR(32)  NULL     DEFAULT NULL COMMENT '智能体渠道',
    `agent_desc`    VARCHAR(255) NOT NULL DEFAULT '暂无' COMMENT '智能体描述',
    `agent_status`  TINYINT      NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='智能体配置表';

# ai_task：智能体负责的工作
DROP TABLE IF EXISTS `ai_task`;
CREATE TABLE `ai_task`
(
    `id`          BIGINT       NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '自增 id',
    `agent_id`    VARCHAR(32)  NOT NULL COMMENT '智能体 id',
    `task_name`   VARCHAR(32)  NOT NULL COMMENT '任务名称',
    `task_cron`   VARCHAR(32)  NOT NULL COMMENT '任务时间表达式',
    `task_desc`   VARCHAR(255) NOT NULL DEFAULT '暂无' COMMENT '任务描述',
    `task_param`  TEXT         NULL     DEFAULT NULL COMMENT '任务参数配置',
    `task_status` TINYINT      NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='智能体任务调度配置表';

# ai_flow：配置客户端工作流
DROP TABLE IF EXISTS `ai_flow`;
CREATE TABLE `ai_flow`
(
    `id`          BIGINT      NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `agent_id`    VARCHAR(32) NOT NULL COMMENT '智能体ID',
    `client_id`   VARCHAR(32) NOT NULL COMMENT '客户端ID',
    `client_name` VARCHAR(32) NOT NULL COMMENT '客户端名称',
    `client_type` VARCHAR(32) NOT NULL COMMENT '客户端类型',
    `flow_prompt` TEXT        NOT NULL COMMENT '工作流提示词',
    `flow_seq`    TINYINT     NOT NULL COMMENT '工作流顺序',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_agent_client_seq` (`agent_id`, `client_id`, `flow_seq`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='智能体-客户端关联表';