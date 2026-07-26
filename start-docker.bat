@echo off
chcp 65001 >nul
echo [Smart Medical] 启动 Docker 一键部署...
docker-compose up -d
echo.
echo 服务已启动：
echo   管理端:     http://localhost:5173
echo   用户端:     http://localhost:5174
echo   后端 API:   http://localhost:8080
echo   Swagger:    http://localhost:8080/swagger-ui.html
echo   MinIO 控制台: http://localhost:9001
echo   MySQL:      localhost:3306
echo   Redis:      localhost:6379
echo.
echo 查看后端日志：docker-compose logs -f backend
pause
