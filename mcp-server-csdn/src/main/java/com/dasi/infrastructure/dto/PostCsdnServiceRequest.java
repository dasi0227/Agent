package com.dasi.infrastructure.dto;

import lombok.Data;

import java.util.List;

@Data
public class PostCsdnServiceRequest {

    // 文章标题
    private String title;

    // Markdown 格式的正文内容
    private String markdowncontent;

    // HTML 格式的正文内容
    private String content;

    // 文章标签
    private String tags;

    // 文章分类栏目
    private String categories;

    // 文章摘要
    private String Description;

    // 阅读权限类型
    private String readType = "public";

    // 文章等级
    private String level = "0";

    // 文章状态
    private Integer status = 0;

    // 文章类型
    private String type = "original";

    // 原文链接
    private String original_link = "";

    // 是否授权转载
    private Boolean authorized_status = false;

    // 资源访问链接
    private String resource_url = "";

    // 是否自动保存
    private String not_auto_saved = "1";

    // 文章来源：
    private String source = "pc_mdeditor";

    // 封面图片地址列表
    private List<String> cover_images = List.of("https://dasi-blog.oss-cn-guangzhou.aliyuncs.com/Java/202601082147219.png");

    // 封面类型
    private Integer cover_type = 1;

    // 是否新建文章
    private Integer is_new = 1;

    // 投票活动 ID
    private Integer vote_id = 0;

    // 资源 ID
    private String resource_id = "";

    // 发布状态
    private String pubStatus = "publish";

    // 是否同步 Git 仓库代码
    private Integer sync_git_code = 0;

    // 创作者活动 ID
    private String creator_activity_id = "";

}
