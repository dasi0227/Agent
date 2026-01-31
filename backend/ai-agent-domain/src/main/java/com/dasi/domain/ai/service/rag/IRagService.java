package com.dasi.domain.ai.service.rag;

import com.dasi.types.dto.request.ai.AiUploadRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IRagService {

    void uploadTextFile(String ragTag, List<MultipartFile> fileList);

    void uploadGitRepo(AiUploadRequest aiUploadRequest);

}
