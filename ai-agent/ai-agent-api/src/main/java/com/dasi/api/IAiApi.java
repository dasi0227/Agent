package com.dasi.api;

import com.dasi.types.dto.request.ArmoryRequest;
import com.dasi.types.dto.request.ChatRequest;
import com.dasi.types.dto.request.WorkRequest;
import com.dasi.types.dto.request.UploadGitRepoRequest;
import com.dasi.types.dto.result.Result;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.List;

public interface IAiApi {

    SseEmitter execute(WorkRequest workRequest);

    String complete(ChatRequest chatRequest);

    Flux<String> stream(ChatRequest chatRequest);

    Result<Void> armory(ArmoryRequest armoryRequest);

    Result<Void> uploadFile(String ragTag, List<MultipartFile> fileList);

    Result<Void> uploadGitRepo(UploadGitRepoRequest uploadGitRepoRequest);

}
