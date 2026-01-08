package com.dasi.mcp.adapter;

import com.dasi.mcp.dto.PostCsdnToolRequest;
import com.dasi.mcp.dto.PostCsdnToolResponse;

import java.io.IOException;

public interface IPostCsdnPort {

    PostCsdnToolResponse saveArticle(PostCsdnToolRequest request) throws IOException;

}
