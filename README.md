# 心理健康 AI 助手

一个前后端分离的心理健康对话助手：支持用户注册登录（JWT 鉴权）、开启咨询会话、与 AI 心理助手**流式对话**（SSE 逐字返回）。

- 后端：Spring Boot + MyBatis-Plus + Spring Security(JWT) + Spring AI
- 前端：Vue 3 + Vite + Element Plus

---

## 功能

- 用户注册 / 登录（密码 BCrypt 加密，登录签发 JWT）
- 获取当前登录用户信息
- 开启心理咨询会话，保存会话与聊天记录
- AI 流式对话（SSE 逐字推送，接入 DeepSeek）
- 统一响应结构 `Result{code,msg,data}` + 全局异常处理

---

## 技术栈

### 后端 `backend/ai-mentalhealth`

| 组件 | 说明 |
|---|---|
| Spring Boot | 4.1.1 |
| Java | 17+ |
| MyBatis-Plus | 3.5.17（`mybatis-plus-spring-boot4-starter`） |
| Spring Security + java-jwt | 4.6.0，JWT 无状态鉴权 |
| Spring AI | 2.0.1（OpenAI 兼容协议，接 DeepSeek） |
| MySQL | 8.x |
| Hutool / Lombok | 工具库 / 简化代码 |

### 前端 `frontend`

Vue 3 · Vite · Element Plus · Pinia · Vue Router · Axios · ECharts · wangEditor

---

## 目录结构

```
.
├── backend/
│   └── ai-mentalhealth/
│       └── src/main/java/com/example/aimentalhealth/
│           ├── controller/    # 接口层（User、PsychologicalChat）
│           ├── service/       # 业务层（含 convert 转换）
│           ├── mapper/        # MyBatis-Plus Mapper
│           ├── entity/        # 数据库实体
│           ├── DTO/           # 请求/响应 DTO
│           ├── config/        # 安全、JWT、AI 客户端配置
│           ├── common/        # Result、ResultCode、全局异常处理
│           ├── util/          # JWT 工具、JWT 过滤器、响应工具
│           ├── enumClass/     # 枚举（UserStatus、UserType）
│           ├── exception/     # BusinessException
│           └── AiService/     # AI 对话服务与提示词
└── frontend/                  # Vue3 前端工程
```

---

## 快速开始

### 1. 后端

**① 建库导表**

创建数据库 `mental_health_assistant`（utf8mb4），导入表结构。

**② 创建本地配置**

在 `backend/ai-mentalhealth/src/main/resources/` 下**新建 `application.yml`**（该文件已被 `.gitignore` 忽略，仓库里没有，需要自己建）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mental_health_assistant?useSSL=false&serverTimezone=UTC
    username: root
    password: 你的数据库密码

  ai:
    model:
      chat: openai          # 用 openai 客户端调 DeepSeek（OpenAI 兼容）
      embedding: none
      image: none
      moderation: none
      audio:
        speech: none
        transcription: none
    openai:
      api-key: ${DEEPSEEK_API_KEY:sk-你的key}
      base-url: https://api.deepseek.com
      chat:
        options:
          model: deepseek-chat

server:
  port: 1236

jwt:
  secret: 请改成你自己的随机密钥
  expiration: 86400000          # 24 小时（毫秒）
  refresh-expiration: 604800000 # 7 天（毫秒）
  header: Authorization
  token-prefix: "Bearer "
```

**③ 启动**

```bash
cd backend/ai-mentalhealth
./mvnw spring-boot:run
```

服务跑在 `http://localhost:1236`。

### 2. 前端

```bash
cd frontend
npm install
npm run dev
```

> ⚠️ `frontend/vite.config.js` 里的 `/api` 代理 `target` 要指向后端地址，本地开发改成 `http://127.0.0.1:1236`。

---

## 接口一览

| 方法 | 路径 | 说明 | 需登录 |
|---|---|---|---|
| GET | `/api/test` | 连通性测试 | 否 |
| POST | `/api/user/login` | 登录，返回 JWT | 否 |
| POST | `/api/user/add` | 注册 | 否 |
| GET | `/api/user/current` | 获取当前用户信息 | 是 |
| POST | `/api/psychological-chat/session/chat` | 开启会话 | 是 |
| POST | `/api/psychological-chat/stream` | 流式对话（SSE） | 是 |

统一响应格式：

```json
{ "code": "200", "msg": "操作成功", "data": { } }
```

---

## 鉴权说明

1. 登录成功后拿到 JWT，前端在请求头携带：`token: <jwt>`
2. 后端的 `JwtAuthticationFilter` 校验 token → 查用户 → 写入 `SecurityContext` → 放行
3. 公开路径（无需登录）：`/api/test`、`/api/user/login`、`/api/user/add`
4. 其余请求需要登录；token 无效/过期统一返回未授权错误

---

## SSE 流式对话约定

**请求**

```
POST /api/psychological-chat/stream
Content-Type: application/json
token: <jwt>

{ "sessionId": "session_24", "userMessage": "最近有点焦虑" }
```

**响应** `text/event-stream`

```
event: message
data: {"code":"200","msg":"操作成功","data":{"content":"我","type":"normal"}}

event: message
data: {"code":"200","msg":"操作成功","data":{"content":"在","type":"normal"}}

event: done
data: {}
```

前端按 `message` 事件逐段渲染，收到 `done` 表示回复结束。

---

## 注意事项

- **不要提交密钥**：`application.yml`（含数据库密码、AI key、JWT 密钥）已被 `.gitignore` 排除，请只在本地创建。
- 生产环境务必更换 `jwt.secret` 为随机长字符串。
- 数据库脚本、设计文档仅保存在本地，未纳入版本管理。
