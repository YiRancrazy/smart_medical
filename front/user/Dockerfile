# 智慧医疗用户端前端镜像
# 构建时自动执行 npm install + npm run build，无需手动生成 dist

# 构建期参数：可用 --build-arg 覆盖
ARG NODE_IMAGE=node:20-alpine
ARG NGINX_IMAGE=nginx:alpine
ARG NPM_REGISTRY=https://registry.npmmirror.com

FROM ${NODE_IMAGE} AS builder

WORKDIR /app

# 先复制 package 文件并安装依赖，依赖不变时可利用 Docker 缓存
COPY package*.json ./
# 移除 Windows 平台特定依赖，避免 Linux 容器内安装失败
RUN sed -i '/"@esbuild\//d' package.json

ARG NPM_REGISTRY
RUN npm config set registry ${NPM_REGISTRY}
RUN npm install

# 复制源码并构建生产包
COPY . .
RUN npm run build

# 运行阶段：仅保留 nginx + 构建产物
FROM ${NGINX_IMAGE}

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
