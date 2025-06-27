# MyBatis XML 解析错误报告

## 1. 错误描述

在应用程序启动过程中，出现以下严重错误，导致 Spring 上下文初始化失败：

```
org.apache.ibatis.builder.BuilderException: Error creating document instance.  Cause: org.xml.sax.SAXParseException; lineNumber: 1; columnNumber: 893; 元素内容必须由格式正确的字符数据或标记组成。
```

此错误最终追溯到 `tech.cspioneer.backend.mapper.NewsMapper` 接口的初始化过程。

## 2. 问题根源分析

错误的核心原因是 MyBatis 在处理基于注解的动态 SQL 时，会将 `<script>` 标签内的内容作为 XML 文档进行解析。

在 `NewsMapper.java` 的 `findNewsList` 方法中，其 `@Select` 注解包含的 SQL 语句中使用了 `>=` 和 `<=` 比较运算符。当 MyBatis 的 XML 解析器遇到这些运算符中的 `<` 和 `>` 字符时，会将其误解为 XML 标签的开始或结束，从而导致 XML 文档格式不正确，抛出 `SAXParseException`。

**问题代码片段:**

```java
// ...
            "<if test='query.startTime != null and query.startTime != \"\"'>",
            "   AND n.created_at >= #{query.startTime} ",
            "</if>",
            "<if test='query.endTime != null and query.endTime != \"\"'>",
            "   AND n.created_at <= #{query.endTime} ",
            "</if>",
// ...
```

## 3. 解决方案

为了解决这个问题，需要防止 MyBatis 的 XML 解析器错误地解析这些比较运算符。标准的做法是使用 `CDATA` 块将包含特殊字符的部分包裹起来，`CDATA` 块内的所有内容都会被解析器当作纯文本处理。

**修复后的代码:**

将原始的 `>=` 和 `<=` 替换为 `<![CDATA[ >= ]]>` 和 `<![CDATA[ <= ]]>`。

```java
// ...
            "<if test='query.startTime != null and query.startTime != \"\"'>",
            "   AND n.created_at <![CDATA[ >= ]]> #{query.startTime} ",
            "</if>",
            "<if test='query.endTime != null and query.endTime != \"\"'>",
            "   AND n.created_at <![CDATA[ <= ]]> #{query.endTime} ",
            "</if>",
// ...
```

此修改确保了 SQL 语句的正确性，同时符合 XML 解析规则，从而解决了应用程序启动失败的问题。
