package com.dasi;

import com.dasi.result.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IRagService {

    Result<Void> uploadFile(String ragTag, List<MultipartFile> fileList);

    Result<Void> uploadGitRepo(String repo, String username, String password);

}
