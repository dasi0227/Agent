package com.dasi.api;

import com.dasi.types.dto.request.ai.AiArmoryRequest;
import com.dasi.types.dto.request.ai.AiChatRequest;
import com.dasi.types.dto.request.ai.AiWorkRequest;
import com.dasi.types.dto.request.ai.AiUploadRequest;
import com.dasi.types.dto.result.Result;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.List;

public interface IAiApi {

    SseEmitter execute(AiWorkRequest aiWorkRequest);

    String complete(AiChatRequest aiChatRequest);

    Flux<String> stream(AiChatRequest aiChatRequest);

    Result<Void> armory(AiArmoryRequest aiArmoryRequest);

    Result<Void> uploadFile(String ragTag, List<MultipartFile> fileList);

    Result<Void> uploadGitRepo(AiUploadRequest aiUploadRequest);

}
