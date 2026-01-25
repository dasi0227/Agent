package com.dasi.api;

import com.dasi.types.model.Result;
import reactor.core.publisher.Flux;

import java.util.List;

public interface IChatService {

    String complete(String model, String message);

    Flux<String> stream(String model, String message);

    Result<List<String>> queryModelIdList();

}
