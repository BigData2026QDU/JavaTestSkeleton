# Architecture.md — Java 通用测试框架架构说明

## 项目概述

JavaTestSkeleton 是一个通用的 Java 测试框架，通过 Git Submodule 方式集成到任意 Java 项目。提供测试基础设施、代码质量检查、变异测试和 CI 工作流。

## 架构图

```
┌──────────────────────────────────────────────────────────────┐
│                      CI / GitHub Actions                      │
│  ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐ │
│  │ Unit Test  │  │Integration│  │ Mutation  │  │   Lint    │ │
│  │ (Surefire) │  │(Failsafe) │  │  (PIT)    │  │Checkstyle/│ │
│  │            │  │           │  │           │  │PMD/SpotBugs│ │
│  └─────┬─────┘  └─────┬─────┘  └─────┬─────┘  └─────┬─────┘ │
│        │              │              │               │        │
│        └──────────────┼──────────────┼───────────────┘        │
│                       │              │                         │
│  ┌────────────────────▼──────────────▼────────────────────┐  │
│  │                    Maven Build                          │  │
│  │  compile → test → verify → pitest → checkstyle →        │  │
│  │  pmd → spotbugs → owasp                                 │  │
│  └─────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│                      测试基础设施                              │
│                                                              │
│  ┌─────────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐ │
│  │  BaseTest   │  │  H2 内存  │  │ AssertJ  │  │ TestData │ │
│  │ (公共工具)   │  │  数据库   │  │(流式断言)│  │ Factory  │ │
│  └──────┬──────┘  └─────┬────┘  └─────┬────┘  └─────┬────┘ │
│         │               │             │              │        │
│  ┌──────▼───────────────▼─────────────▼──────────────▼────┐ │
│  │              JUnit 5 + Mockito + JaCoCo                 │ │
│  └─────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│                    Git Submodule 架构                         │
│                                                              │
│  YourTestProject/                                            │
│  ├── JavaTestSkeleton/ (submodule)                           │
│  │   ├── src/main/java/   → 测试工具类                       │
│  │   └── src/test/java/   → 测试模板                         │
│  ├── projects/                                               │
│  │   ├── ProjectA/ (submodule) → 被测项目 A                  │
│  │   └── ProjectB/ (submodule) → 被测项目 B                  │
│  └── pom.xml                → 通过 build-helper 引入源码    │
└──────────────────────────────────────────────────────────────┘
```

## 模块职责

### BaseTest（测试基类）

- H2 内存数据库连接创建与销毁
- 临时目录自动管理（`@TempDir`）
- 测试配置读取（`test.properties`）
- 超时断言辅助
- SQL 执行辅助方法

### TestDataFactory（测试数据工厂）

- 生成标准化测试数据
- 支持参数化测试
- 避免测试数据硬编码

### Lint 工具

| 工具 | 检查维度 | 阶段 |
|------|---------|------|
| Checkstyle | 代码风格、命名规范、格式 | `validate` |
| PMD | 未使用变量、空 catch、冗余代码 | `verify` |
| SpotBugs | 空指针、资源泄漏、并发 bug | `verify` |
| OWASP | 依赖漏洞扫描 | `verify` |

### 质量工具

| 工具 | 用途 | 说明 |
|------|------|------|
| JaCoCo | 代码覆盖率 | 行覆盖率 ≥70%，分支覆盖率 ≥60% |
| PIT | 变异测试 | 验证测试用例的有效性 |

### CI 工作流

7 个并行 Job：

```
unit-test ──────────┐
integration-test ───┤
mutation-test ──────┤
dependency-check ───┼──► 全部通过才 ✅
checkstyle ─────────┤
pmd ────────────────┤
spotbugs ───────────┘
```

## 数据流

```
开发者 push
  │
  ▼
GitHub Actions 触发
  │
  ├──► Job 1: mvn test ────────────────── 单元测试
  ├──► Job 2: mvn verify -DskipTests ──── 集成测试
  ├──► Job 3: mvn pitest:mutationCoverage ─ 变异测试
  ├──► Job 4: mvn owasp:check ─────────── 依赖漏洞扫描
  ├──► Job 5: mvn checkstyle:check ────── 代码风格
  ├──► Job 6: mvn pmd:check ───────────── 静态分析
  └──► Job 7: mvn spotbugs:check ──────── Bug 检测
```

## 关键设计决策

1. **Git Submodule 架构**：测试框架和被测项目均为独立仓库，通过 submodule 引用
2. **Surefire vs Failsafe**：单元测试用 Surefire（`*Test.java`），集成测试用 Failsafe（`*IT.java`），互不干扰
3. **H2 内存数据库**：零外部依赖，测试数据自动销毁，CI 友好
4. **4 种 Lint 互补**：Checkstyle 管风格、PMD 管质量、SpotBugs 管 bug 模式、OWASP 管依赖漏洞
5. **PIT 变异测试**：验证测试用例是否真正有效，而非仅看覆盖率
6. **BaseTest 封装**：数据库连接、临时文件、配置读取等公共逻辑，子类只写测试
7. **并行 CI**：7 个 Job 独立运行，总耗时 = 最慢的单个 Job
