CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS public.vector_store_openai (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content TEXT NOT NULL,
    metadata JSONB,
    embedding VECTOR(1024)
)