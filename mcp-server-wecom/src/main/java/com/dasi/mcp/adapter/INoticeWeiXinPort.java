package com.dasi.mcp.adapter;

import com.dasi.mcp.dto.NoticeWeComToolRequest;
import com.dasi.mcp.dto.NoticeWeComToolResponse;

import java.io.IOException;

public interface INoticeWeiXinPort {

    NoticeWeComToolResponse noticeArticle(NoticeWeComToolRequest toolRequest) throws IOException;

}
