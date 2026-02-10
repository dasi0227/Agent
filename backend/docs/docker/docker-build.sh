# 创建多架构构建的容器
docker buildx create --use --name multiarch-builder 2>/dev/null || docker buildx use multiarch-builder
docker buildx inspect --bootstrap

# mcp-server-csdn
docker buildx build --platform linux/amd64,linux/arm64 \
  -t dasi0227/mcp-server-csdn:0.1.0 \
  --push \
  ./mcp-server-csdn

# mcp-server-amap
docker buildx build --platform linux/amd64,linux/arm64 \
  -t dasi0227/mcp-server-amap:0.1.0 \
  --push \
  ./mcp-server-amap

# mcp-server-wecom
docker buildx build --platform linux/amd64,linux/arm64 \
  -t dasi0227/mcp-server-wecom:0.1.0 \
  --push \
  ./mcp-server-wecom
