#!/bin/bash
set -e

echo "[Smart Medical] 停止 Docker 服务..."
docker-compose down
echo "已停止。"
