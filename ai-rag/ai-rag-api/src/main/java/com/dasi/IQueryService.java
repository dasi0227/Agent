package com.dasi;

import com.dasi.result.Result;

import java.util.List;

public interface IQueryService {

    public Result<List<String>> queryRagTagList();

    public Result<List<String>> queryChatModelList();

}
