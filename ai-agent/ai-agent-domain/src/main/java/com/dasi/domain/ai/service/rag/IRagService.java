package com.dasi.domain.ai.service.rag;

import com.dasi.types.dto.request.UploadGitRepoRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IRagService {

    void uploadTextFile(String ragTag, List<MultipartFile> fileList);

    void uploadGitRepo(UploadGitRepoRequest uploadGitRepoRequest);

}
