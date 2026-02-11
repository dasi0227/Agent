package com.dasi.mcp.port;

import com.dasi.mcp.dto.SendEmailToolRequest;
import com.dasi.mcp.dto.SendEmailToolResponse;

import java.io.IOException;

public interface IEmailPort {

    SendEmailToolResponse sendEmail(SendEmailToolRequest toolRequest) throws IOException;

}
