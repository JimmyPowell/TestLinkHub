# TestLinkHub 身份鉴权与授权机制详解

本文档详细阐述了 TestLinkHub 项目后端所采用的基于 JWT (JSON Web Token) 的身份验证和授权机制。该机制围绕 Spring Security 框架构建，并采用无状态（Stateless）设计，确保了系统的安全性、可扩展性和高性能。

## 1. 核心设计理念

系统采用**双 Token 模式**来平衡安全性和用户体验：

-   **AccessToken**: 一种时效性较短的凭证（例如15分钟），用于访问受保护的 API 资源。其 Payload 中包含了用户的核心身份信息，以避免频繁查询数据库。AccessToken 通过 HTTP 请求头的 `Authorization` 字段以 `Bearer` 方式传递。
-   **RefreshToken**: 一种时效性较长的凭证（例如7天），其唯一用途是用于获取新的 AccessToken。为了增强安全性，RefreshToken 被存储在客户端的 `HttpOnly` Cookie 中，有效防止了跨站脚本（XSS）攻击。

当 AccessToken 过期时，前端应用会自动携带 RefreshToken 向特定的刷新接口发起请求，为用户换取新的 AccessToken，从而实现对用户透明的“无感刷新”体验。

## 2. JWT 实现细节

JWT 的生成与解析由自定义的 `JwtUtils` 工具类负责，未使用第三方库，以实现最大程度的控制。

### 2.1. 签名算法与密钥

-   **算法**: 所有 Token 均采用 **HMAC-SHA256 (`HmacSHA256`)** 算法进行签名。
-   **密钥**: 签名密钥从应用的配置文件 (`application.properties`) 中的 `jwt.secret` 属性动态加载，确保了密钥与代码分离。

### 2.2. Token Payload 结构

Token 的 Payload 中包含了以下关键声明（Claims）：

-   `uid`: **用户唯一标识符 (User UUID)**，作为认证主体的唯一标识。
-   `identity`: **用户身份/角色** (例如 "ADMIN", "COMPANY", "USER")，直接用于后端的授权判断。
-   `exp`: **过期时间 (Expiration Time)**，一个Unix时间戳（毫秒），定义了 Token 的有效期限。
-   `iat`: **签发时间 (Issued At)**，一个Unix时间戳（毫秒），记录了 Token 的创建时间。
-   `ver` (仅 RefreshToken): **版本号**，此声明仅存在于 RefreshToken 中，用于实现高级安全策略，如在用户修改密码或登出时，通过服务端递增版本号来使所有旧的 RefreshToken 失效。

### 2.3. Token 生成流程

-   `generateAccessToken(userUuid, identity)`: 生成 AccessToken，包含 `uid` 和 `identity`，并设置较短的过期时间（由 `jwt.access-token-expiration-ms` 配置）。
-   `generateRefreshToken(userUuid, identity, version)`: 生成 RefreshToken，额外包含 `ver` 声明，并设置较长的过期时间（由 `jwt.refresh-token-expiration-ms` 配置）。

## 3. Spring Security 集成与过滤器链

系统的安全控制由 Spring Security 提供支持，其核心配置位于 `SecurityConfig` 类中。

### 3.1. 核心配置

-   **无状态会话**: 通过 `sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)` 明确配置系统为无状态，不依赖 HTTP Session。
-   **CSRF 禁用**: 由于是无状态 API，不使用基于 Cookie 的认证，因此 CSRF 保护被禁用 (`csrf().disable()`)。
-   **密码加密**: 使用 `BCryptPasswordEncoder` 作为密码编码器，确保用户密码在数据库中被安全地哈希存储。

### 3.2. JwtAuthenticationFilter

这是自定义的核心认证过滤器，继承自 `OncePerRequestFilter`，被注入到 Spring Security 过滤器链的 `UsernamePasswordAuthenticationFilter` 之前。其主要职责如下：

1.  **提取 Token**: 从 `Authorization` 请求头中解析 `Bearer` 类型的 AccessToken。
2.  **跳过特定路径**: 忽略对 `/api/auth/refresh` 接口的过滤。
3.  **验证 Token**:
    -   **解码 Payload**: 调用 `JwtUtils.decode()` 解析 Token。
    -   **校验过期**: **手动检查** `exp` 声明，确保 Token 未过期。
    -   **签名校验**: 隐式依赖 `JwtUtils` 在解码或签名验证失败时抛出异常。
4.  **构建安全上下文**:
    -   如果 Token 验证通过，过滤器会从 Payload 中提取 `uid` 和 `identity`。
    -   **不查询数据库**，直接利用 Token 中的信息创建一个 `UsernamePasswordAuthenticationToken`。
    -   `userUuid` 被设为认证主体 (Principal)，`identity` 被转换为 `SimpleGrantedAuthority` 作为用户的权限。
    -   将构建的 `Authentication` 对象设置到 `SecurityContextHolder` 中，供后续授权使用。
5.  **异常处理**: 任何解析或验证失败都会清空安全上下文，确保请求以未认证状态继续处理。

## 4. 授权与访问控制

系统的授权逻辑在 `SecurityConfig` 中通过 `authorizeHttpRequests` 进行集中配置，实现了精细化的访问控制。

### 4.1. 路径权限 (Ant Matchers)

-   **公共访问 (Permit All)**:
    -   `/api/auth/**`: 所有认证相关端点，包括登录、注册、验证码、刷新 Token。
    -   `/swagger-ui/**`, `/v3/api-docs/**`: API 文档。
    -   静态资源。

-   **基于角色的访问控制**:
    -   `/api/admin/**`: 要求用户必须拥有 `ADMIN` **或** `COMPANY` 权限 (`hasAnyAuthority("ADMIN", "COMPANY")`)。
    -   `/api/root/**`: 要求用户必须拥有 `ADMIN` 权限 (`hasAuthority("ADMIN")`)，这是系统的最高权限。
    -   `/api/test/company-only`: 仅限 `COMPANY` 权限访问。

-   **默认规则**:
    -   所有其他未明确配置的路径 (`anyRequest()`) 都要求用户必须经过认证 (`authenticated()`)。

### 4.2. 异常处理

-   **认证入口点 (`AuthenticationEntryPoint`)**: 自定义了认证失败的处理器。当一个未认证的用户尝试访问受保护资源时，系统会返回标准的 `401 Unauthorized` HTTP 状态码和错误信息，而不是重定向到登录页面。

## 5. 认证流程概览 (时序图)

```mermaid
sequenceDiagram
    participant Client
    participant Server
    
    Client->>Server: POST /api/auth/login (携带用户名/密码)
    Server->>Server: 验证凭证
    alt 凭证有效
        Server->>Server: 生成 AccessToken (短时效)
        Server->>Server: 生成 RefreshToken (长时效, 含版本号)
        Server-->>Client: 200 OK (返回 AccessToken, RefreshToken 存入 HttpOnly Cookie)
    else 凭证无效
        Server-->>Client: 401 Unauthorized
    end

    Client->>Server: GET /api/protected/resource (Header: Authorization: Bearer AccessToken)
    Server->>Server: JwtAuthenticationFilter 拦截
    Server->>Server: 验证 AccessToken (签名, 过期时间)
    alt Token 有效
        Server->>Server: 设置 SecurityContext (含用户UUID和角色)
        Server-->>Client: 200 OK (返回资源)
    else Token 过期
        Server-->>Client: 401 Unauthorized
    end

    Client->>Server: POST /api/auth/refresh (Cookie: RefreshToken)
    Server->>Server: 验证 RefreshToken
    alt RefreshToken 有效
        Server->>Server: 生成新的 AccessToken
        Server-->>Client: 200 OK (返回新的 AccessToken)
    else RefreshToken 无效
        Server-->>Client: 401 Unauthorized (要求重新登录)
    end
