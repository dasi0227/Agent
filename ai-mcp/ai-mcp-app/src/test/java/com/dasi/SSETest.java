package com.dasi;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class SSETest {

    @Resource
    private OpenAiChatModel openAiChatModel;

    @Autowired
    private ToolCallbackProvider tools;

    @Test
    public void testOpenaiMcp1() {
        String userMessage = """
            我需要你帮我生成一篇文章，要求如下：
            - 主题：关于 Java 的趣味小故事
            - 文风：好笑风趣，具贴吧老哥的黄色幽默和美国人的黑色幽默
            - 字数：限定在 500 字内
            - 标题：Java趣闻
            根据以上要求，将内容发布文章到 CSDN 上。
            最后你只需要告诉我文章的 id、url、qrcode 即可，其他内容不需要。
            """;

        ChatClient chatClient = ChatClient
                .builder(openAiChatModel)
                .defaultTools(tools)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("glm-4.7")
                        .build())
                .build();

        String content = chatClient.prompt(userMessage).call().content();

        log.info("测试结果：{}", content);
    }

    @Test
    public void testOpenaiMcp2() {
        String userMessage = """
            我需要你帮我发送一个通过企业微信的应用，发送消防演练通知，要求如下：
            - 标题 title不超过128个字符
            - 描述 description 不超过512个字符
            - 链接 url 暂时设置为 https://example.com
            最后你只需要告诉我企业微信调用状态码，状态信息和发送消息的id，其他内容不需要。
            """;

        ChatClient chatClient = ChatClient
                .builder(openAiChatModel)
                .defaultTools(tools)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("glm-4.7")
                        .build())
                .build();

        String content = chatClient.prompt(userMessage).call().content();

        log.info("测试结果：{}", content);
    }

    @Test
    public void testOpenaiMcp3() {
        String userMessage = """
            你需要按顺序完成两个工具调用（必须使用工具，不要只在文本里“假装成功”）：
            第1步：发布文章到 CSDN
            - 调用 PostCsdnTool.saveArticle 完成发布
            - 文章标题（title）：Java趣闻
            - 文章内容（markdowncontent）：请你先生成一篇 500 字以内、好笑风趣的 Java 趣味小故事（正常幽默即可，禁止低俗/黄色内容）
            第2步：发送企业微信应用消息
            - 调用 NoticeWeComTool.sendTextCard 完成发送
            - 文本卡片标题（title）：Java趣闻
            - 文本卡片描述（description）：基于第1步文章内容，写一段故事的简短摘要，不允许超过 512 字符
            - 文本卡片链接（url）：设置为第1步返回的 url
            输出要求：
            - 最终只输出一个 JSON（不要输出其他任何文字）：
            {
              "csdn": {"code":"", "msg":"", "id":"", "url":"", "qrcode":""},
              "wecom": {"code":"", "info":"", "msgid":""}
            }
            - 如果任一步失败，也按相同 JSON 输出，并在对应 info/msg 字段写明失败原因
            """;

        ChatClient chatClient = ChatClient
                .builder(openAiChatModel)
                .defaultTools(tools)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("glm-4.7")
                        .build())
                .build();

        String content = chatClient.prompt(userMessage).call().content();

        log.info("测试结果：{}", content);
    }

}
