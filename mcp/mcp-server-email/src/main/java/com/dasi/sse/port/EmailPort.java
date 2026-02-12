package com.dasi.sse.port;

import com.dasi.mcp.dto.SendEmailToolRequest;
import com.dasi.mcp.dto.SendEmailToolResponse;
import com.dasi.mcp.port.IEmailPort;
import com.dasi.sse.properties.EmailProperties;
import jakarta.annotation.Resource;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;


@Slf4j
@Service
public class EmailPort implements IEmailPort {

    @Resource
    private JavaMailSender javaMailSender;

    @Resource
    private EmailProperties emailProperties;

    @Override
    public SendEmailToolResponse sendEmail(SendEmailToolRequest toolRequest) throws UnsupportedEncodingException {

        SendEmailToolResponse toolResponse = new SendEmailToolResponse();

        if (toolRequest == null) {
            toolResponse.setCode(500);
            toolResponse.setInfo("Email 请求为空");
            return toolResponse;
        }

        if (!StringUtils.hasText(toolRequest.getTo())) {
            toolResponse.setCode(500);
            toolResponse.setInfo("Email 收件人不能为空");
            return toolResponse;
        }

        if (!StringUtils.hasText(toolRequest.getSubject())) {
            toolResponse.setCode(500);
            toolResponse.setInfo("Email 主题不能为空");
            return toolResponse;
        }

        if (!StringUtils.hasText(toolRequest.getContent())) {
            toolResponse.setCode(500);
            toolResponse.setInfo("Email 正文不能为空");
            return toolResponse;
        }

        String fromAddress = emailProperties.getFromAddress();
        String fromName = emailProperties.getFromName();
        InternetAddress internetAddress = new InternetAddress(fromAddress, fromName, "UTF-8");

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            messageHelper.setFrom(internetAddress);
            messageHelper.setTo(toolRequest.getTo());
            messageHelper.setSubject(toolRequest.getSubject());

            boolean html = Boolean.TRUE.equals(toolRequest.getHtml());
            messageHelper.setText(toolRequest.getContent(), html);

            javaMailSender.send(mimeMessage);

            toolResponse.setCode(200);
            toolResponse.setInfo("Email 发送成功");
            toolResponse.setMessageId(mimeMessage.getMessageID());
            log.info("调用 SMTP 发送邮件成功：to={} subject={} from={}", toolRequest.getTo(), toolRequest.getSubject(), internetAddress);
            return toolResponse;

        } catch (MailException | jakarta.mail.MessagingException e) {
            toolResponse.setCode(500);
            toolResponse.setInfo("Email 发送失败: " + e.getMessage());
            log.error("调用 SMTP 发送邮件失败：to={} subject={} error={}", toolRequest.getTo(), toolRequest.getSubject(), e.getMessage(), e);
            return toolResponse;
        }
    }

}
