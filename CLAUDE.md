# CLAUDE.md

## Project Overview

学期项目 (Semester Project) - Spark 相关大数据项目

## Tech Stack

- **Java 17** (JDK 17)
- Apache Spark (版本待定)
- Python (版本待定)

**Important:** 开发前必须确认技术栈版本，确保代码符合要求。

## Repository Structure

每个仓库必须遵循以下结构：

```
repository-name/
├── repository-name/          # 源代码文件夹（与仓库同名）
├── Architecture.md           # 架构说明文档（中文）
├── README.md                 # 项目简介及使用说明（中文）
├── File_Index.md             # 文件索引（中文）
├── .git/
├── .gitignore
└── 其他 Git 管理文件
```

## Documentation Requirements

### Architecture.md
- 项目架构图或描述
- 模块职责说明
- 模块间关系和依赖
- 数据流向
- 关键技术点

### README.md
- 项目简介和目标
- 主要功能特性
- 环境要求
- 快速开始指南
- 构建和运行方法

### File_Index.md
- 每个文件的路径
- 文件主要作用
- 内容简要介绍

## Code Style

- 使用中文注释
- 遵循 Java 编码规范
- 代码变更后同步更新文档

## Java Package Naming (CRITICAL)

**所有 Java 代码必须遵守以下包命名规范，否则测试工具将无法正常工作：**

### 必须满足的条件

1. **每个 .java 文件必须有 `package` 声明**，且包名必须精确匹配文件目录路径
2. **包名全小写**，例如 `org.example.tool`，不能有大写字母
3. **测试类必须与生产代码在相同包结构下**：
   ```
   src/main/java/org/example/Tool/Foo.java    → package org.example.Tool;
   src/test/java/org/example/Tool/FooTest.java → package org.example.Tool;
   ```
4. **禁止使用 Java 保留字**：`value`、`test`、`class`、`new`、`import`、`package`、`public`、`private`、`protected`、`void`、`int`、`string` 等

### 为什么

- Maven Surefire 插件通过包名匹配发现测试类
- PIT 变异测试按包名定位要变异的生产代码
- JaCoCo 覆盖率按包名统计
- H2 数据库中 `value` 是 SQL 保留字，会导致 DDL 错误

### 推荐包结构

```
org.example.{ModuleName}     → 例如 org.example.Tool, org.example.Service
```

## Git Workflow

1. 确认技术栈版本
2. 创建仓库结构
3. 编写代码
4. 更新三份文档
5. 提交代码
