package com.dasi.domain.ai.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiPromptVO {

    /** 提示词 id */
    private String promptId;

    /** 提示词名称 */
    private String promptName;

    /** 提示词内容 */
    private String promptContent;

    /** 提示词描述 */
    private String promptDesc;

}
