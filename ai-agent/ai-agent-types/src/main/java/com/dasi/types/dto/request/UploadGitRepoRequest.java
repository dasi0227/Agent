package com.dasi.types.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadGitRepoRequest {

    private String repoUrl;

    private String username;

    private String password;

}
