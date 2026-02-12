package com.dasi.mcp.port;

import com.dasi.mcp.dto.BochaSearchToolRequest;
import com.dasi.mcp.dto.BochaSearchToolResponse;

import java.io.IOException;

public interface IBochaPort {

    BochaSearchToolResponse webSearch(BochaSearchToolRequest toolRequest) throws IOException;

}
