package com.dasi;

import com.dasi.result.Result;

import java.util.List;

public interface IQueryService {

    Result<List<String>> queryRagTagList();

    Result<List<String>> queryChatModelList();

}
