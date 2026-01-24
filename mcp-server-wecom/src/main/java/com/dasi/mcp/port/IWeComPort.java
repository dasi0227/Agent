package com.dasi.mcp.port;

import com.dasi.mcp.dto.SendMessageToolResponse;
import com.dasi.mcp.dto.SendTextCardToolRequest;
import com.dasi.mcp.dto.SendTextToolRequest;

import java.io.IOException;

public interface IWeComPort {

    SendMessageToolResponse sendTextCard(SendTextCardToolRequest toolRequest) throws IOException;

    SendMessageToolResponse sendText(SendTextToolRequest toolRequest) throws IOException;

}
