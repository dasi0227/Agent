package com.dasi;

import com.dasi.mcp.dto.SendEmailToolRequest;
import com.dasi.mcp.dto.SendEmailToolResponse;
import com.dasi.mcp.tool.EmailTool;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = McpServerEmailApplication.class)
public class SendEmailTest {

    @Resource
    private EmailTool emailTool;

    private final String testHtml = """
            <!doctype html>
            <html lang="zh-CN">
            <head>
                <meta charset="UTF-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1" />
                <title>Test Email</title>
            </head>
            <body style="margin:0;padding:0;background:#f6f7fb;">
            <table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%" style="background:#f6f7fb;padding:24px 0;">
                <tr>
                    <td align="center">
                        <table role="presentation" cellpadding="0" cellspacing="0" border="0" width="600" style="width:600px;max-width:600px;background:#ffffff;border-radius:12px;overflow:hidden;border:1px solid #e8eaf2;">
                            <tr>
                                <td style="padding:18px 20px;background:#0f172a;color:#ffffff;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,'PingFang SC','Hiragino Sans GB','Microsoft YaHei',sans-serif;">
                                    <div style="font-size:16px;font-weight:700;letter-spacing:0.2px;">Dasi-Agent · 邮件发送测试</div>
                                    <div style="margin-top:6px;font-size:12px;opacity:0.85;">mcp-server-email / SMTP HTML Render Check</div>
                                </td>
                            </tr>
            
                            <tr>
                                <td style="padding:20px;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,'PingFang SC','Hiragino Sans GB','Microsoft YaHei',sans-serif;color:#111827;">
                                    <div style="font-size:18px;font-weight:700;margin:0 0 8px 0;">这是一封 HTML 测试邮件</div>
                                    <div style="font-size:14px;line-height:1.6;color:#374151;">
                                        如果你能正常看到：<b>标题</b>、<i>斜体</i>、按钮、表格、引用块与列表，说明邮件 HTML 渲染基本 OK。
                                    </div>
            
                                    <div style="height:14px;line-height:14px;">&nbsp;</div>
            
                                    <table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%" style="border-collapse:collapse;">
                                        <tr>
                                            <td style="padding:10px 12px;border:1px solid #e5e7eb;background:#f9fafb;font-size:13px;color:#111827;">
                                                <b>Message-ID</b>
                                            </td>
                                            <td style="padding:10px 12px;border:1px solid #e5e7eb;font-size:13px;color:#111827;">
                                                {{messageId}}
                                            </td>
                                        </tr>
                                        <tr>
                                            <td style="padding:10px 12px;border:1px solid #e5e7eb;background:#f9fafb;font-size:13px;color:#111827;">
                                                <b>Timestamp</b>
                                            </td>
                                            <td style="padding:10px 12px;border:1px solid #e5e7eb;font-size:13px;color:#111827;">
                                                {{timestamp}}
                                            </td>
                                        </tr>
                                        <tr>
                                            <td style="padding:10px 12px;border:1px solid #e5e7eb;background:#f9fafb;font-size:13px;color:#111827;">
                                                <b>Env</b>
                                            </td>
                                            <td style="padding:10px 12px;border:1px solid #e5e7eb;font-size:13px;color:#111827;">
                                                {{env}}
                                            </td>
                                        </tr>
                                    </table>
            
                                    <div style="height:16px;line-height:16px;">&nbsp;</div>
            
                                    <table role="presentation" cellpadding="0" cellspacing="0" border="0">
                                        <tr>
                                            <td style="background:#2563eb;border-radius:10px;">
                                                <a href="https://example.com" target="_blank"
                                                   style="display:inline-block;padding:12px 16px;color:#ffffff;text-decoration:none;font-size:14px;font-weight:700;">
                                                    打开测试链接
                                                </a>
                                            </td>
                                        </tr>
                                    </table>
            
                                    <div style="height:16px;line-height:16px;">&nbsp;</div>
            
                                    <div style="padding:12px 14px;border-left:4px solid #22c55e;background:#f0fdf4;color:#14532d;font-size:13px;line-height:1.6;">
                                        提示：若按钮样式丢失或表格错位，通常是某些邮箱客户端对 CSS 支持有限；但纯内联样式一般最稳。
                                    </div>
            
                                    <div style="height:14px;line-height:14px;">&nbsp;</div>
            
                                    <div style="font-size:14px;color:#111827;font-weight:700;margin-bottom:6px;">Checklist</div>
                                    <ul style="margin:0;padding-left:18px;font-size:13px;line-height:1.7;color:#374151;">
                                        <li>能看到深色头部区域</li>
                                        <li>能看到蓝色按钮并可点击</li>
                                        <li>表格三行数据边框完整</li>
                                        <li>绿色提示块样式正常</li>
                                    </ul>
                                </td>
                            </tr>
            
                            <tr>
                                <td style="padding:14px 20px;background:#f9fafb;border-top:1px solid #e5e7eb;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,'PingFang SC','Hiragino Sans GB','Microsoft YaHei',sans-serif;">
                                    <div style="font-size:12px;color:#6b7280;line-height:1.6;">
                                        这是一封自动化测试邮件，请勿回复。<br />
                                        © Dasi-Agent
                                    </div>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
            </body>
            </html>
            """;

    @Test
    void testSendEmailTool() throws Exception {
        SendEmailToolRequest toolRequest = new SendEmailToolRequest();
        toolRequest.setTo("1740929297@qq.com");
        toolRequest.setSubject("Test");
        toolRequest.setContent(testHtml);
        toolRequest.setHtml(true);

        SendEmailToolResponse toolResponse = emailTool.sendEmail(toolRequest);
        System.out.println("Email toolResponse\n" + toolResponse);
    }

}
