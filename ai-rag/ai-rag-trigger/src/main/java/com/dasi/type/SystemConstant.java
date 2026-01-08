package com.dasi.type;

public class SystemConstant {

    public static final String REDIS_RAG_TAG_LIST_KEY = "ragTagList";

    public static final String REDIS_CHAT_MODEL_LIST_KEY = "chatModelList";

    public static final String PGVECTOR_KNOWLEDGE_KEY = "knowledge";

    public static final String GIT_CLONE_DIRECTORY = "./git-cloned-repo";

    public static final String SYSTEM_PROMPT = """
                Use the information from the DOCUMENTS section to provide accurate answers but act as if you knew this information innately.
                If unsure, simply state that you don't know.
                Another thing you need to note is that your reply must be in Chinese!
                DOCUMENTS:
                    {documents}
                """;
}
