package com.dasi.mcp.port;

import com.dasi.mcp.dto.SaveArticleToolRequest;
import com.dasi.mcp.dto.SaveArticleToolResponse;

import java.io.IOException;

public interface ICsdnPort {

    SaveArticleToolResponse saveArticle(SaveArticleToolRequest request) throws IOException;

}
