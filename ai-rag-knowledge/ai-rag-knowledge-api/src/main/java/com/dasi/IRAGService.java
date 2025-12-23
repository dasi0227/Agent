package com.dasi;

import com.dasi.result.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IRAGService {

    Result<List<String>> queryRagTagList();

    Result<Void> uploadFile(String ragTag, List<MultipartFile> fileList);

}
