package com.dasi.domain.agent.service.armory.model;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionTextParser;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RagAnswerAdvisor implements BaseAdvisor {

    private final VectorStore vectorStore;

    private final SearchRequest baseSearchRequest;

    /**
     * context 中存放检索结果的 key
     */
    private static final String CTX_RETRIEVED_DOCS_KEY = "qa_retrieved_documents";

    /**
     * context 中可选传入过滤表达式的 key
     */
    private static final String CTX_FILTER_EXPRESSION_KEY = "qa_filter_expression";

    /**
     * 模板参数名：检索到的上下文文本
     */
    private static final String PARAM_QUESTION_ANSWER_CONTEXT = "question_answer_context";

    /**
     * RAG 提示词模板
     */
    private final String userAdvise;

    public RagAnswerAdvisor(VectorStore vectorStore, SearchRequest baseSearchRequest) {
        this.vectorStore = vectorStore;
        this.baseSearchRequest = baseSearchRequest;
        this.userAdvise =
                "你将获得一段【知识库信息】，并根据它进行回答。\n" +
                "请严格遵守：\n" +
                "1) 基于【知识库信息】与【对话历史】中已给出的信息回答，禁止臆测；\n" +
                "2) 优先引用【知识库信息】中的关键短语/事实点，避免复述大量原文；\n" +
                "3) 若【知识库信息】中出现冲突信息，指出冲突并给出最稳妥的结论；\n" +
                "4) 若【知识库信息】不足以回答，表明根据当前知识库信息无法确定并说明缺少什么信息。\n\n" +
                "---------------------\n" +
                "<知识库信息>\n" +
                "{" + PARAM_QUESTION_ANSWER_CONTEXT + "}\n" +
                "</知识库信息>\n";
    }

    /**
     * 调用 LLM 之前，执行检索并注入知识库信息
     */
    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {

        String userText = new PromptTemplate(chatClientRequest.prompt().getUserMessage().getText()).render();
        Map<String, Object> context = new HashMap<>(chatClientRequest.context());

        // 1) 检索知识库文档
        String query = userText.trim().toLowerCase();
        Filter.Expression filterExpression = getFilterExpression(context);
        SearchRequest searchRequest = SearchRequest
                .from(baseSearchRequest)
                .query(query)
                .filterExpression(filterExpression)
                .build();
        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        if (documents == null || documents.isEmpty()) {
            return chatClientRequest;
        }

        // 2) 拼接所有知识库文档为一个知识库信息
        String documentContext = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining(System.lineSeparator()));

        // 3) 将检索结果和知识库信息放回上下文
        context.put(CTX_RETRIEVED_DOCS_KEY, documents);
        context.put(PARAM_QUESTION_ANSWER_CONTEXT, documentContext);

        // 4) 将知识库信息填入 userAdvise 模板
        Map<String, Object> ragMap = new HashMap<>(1);
        ragMap.put(PARAM_QUESTION_ANSWER_CONTEXT, documentContext);
        String ragInstruction = new PromptTemplate(userAdvise).render(ragMap);

        // 5) 构造用户消息和提示词
        String advisedText = userText + System.lineSeparator() + ragInstruction;
        UserMessage userMessage = new UserMessage(advisedText);
        Prompt prompt = new Prompt(userMessage);

        // 6) 返回处理后的请求
        return ChatClientRequest.builder()
                .prompt(prompt)
                .context(context)
                .build();
    }

    /**
     * 调用 LLM 之后，把检索到的文档挂到 ChatResponse.metadata 上
     */
    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {

        Object retrievedDocs = response.context().get(CTX_RETRIEVED_DOCS_KEY);
        if (retrievedDocs == null) {
            return response;
        }

        ChatResponse chatResponse = ChatResponse.builder()
                .from(response.chatResponse())
                .metadata(CTX_RETRIEVED_DOCS_KEY, retrievedDocs)
                .build();

        return ChatClientResponse.builder()
                .chatResponse(chatResponse)
                .context(response.context())
                .build();
    }

    @Override
    public int getOrder() {
        return 0;
    }

    protected Filter.Expression getFilterExpression(Map<String, Object> context) {

        Filter.Expression baseExpr = this.baseSearchRequest.getFilterExpression();

        // context 为空，直接返回 baseExpr
        if (context == null) {
            return baseExpr;
        }

        // context 不包含过滤表达式，直接返回 base
        Object exprObj = context.get(CTX_FILTER_EXPRESSION_KEY);
        if (exprObj == null || !StringUtils.hasText(exprObj.toString())) {
            return baseExpr;
        }

        // 解析表达式并复合
        Filter.Expression ctxExpr = new FilterExpressionTextParser().parse(exprObj.toString());
        return baseExpr == null ? ctxExpr : new Filter.Expression(Filter.ExpressionType.AND, baseExpr, ctxExpr);
    }

}
