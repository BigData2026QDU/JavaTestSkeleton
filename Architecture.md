# Architecture.md — 测试项目架构说明

## 项目概述

JavaTestSkeleton 是一个测试项目，用于测试 Java 项目。被测项目作为 Git Submodule 加入到 `projects/` 目录，测试代码通过 `build-helper-maven-plugin` 引入被测项目源码进行测试。

## 架构图

```
┌──────────────────────────────────────────────────────────────┐
│                    Git Submodule 架构                         │
│                                                              │
│  JavaTestSkeleton/               # 测试项目                  │
│  ├── JavaTestSkeleton/           # 测试代码                  │
│  │   ├── pom.xml                                           │
│  │   └── src/test/java/         # 测试用例                  │
│  ├── projects/                   # 被测项目（全部为 submodule）│
│  │   ├── DatabaseConnect/        # 被测项目 A                │
│  │   └── YourProject/            # 被测项目 B                │
│  └── pom.xml                     # 主配置                    │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│                      编译流程                                 │
│                                                              │
│  ┌─────────────────┐    ┌─────────────────┐                  │
│  │ projects/       │    │ JavaTestSkeleton/│                  │
│  │ YourProject/    │    │ src/test/java/   │                  │
│  │ src/main/java/  │    │                  │                  │
│  └────────┬────────┘    └────────┬─────────┘                  │
│           │                      │                            │
│           └──────────┬───────────┘                            │
│                      ▼                                        │
│           ┌─────────────────────┐                             │
│           │ build-helper-maven  │                             │
│           │ 引入被测项目源码    │                             │
│           └──────────┬──────────┘                             │
│                      ▼                                        │
│           ┌─────────────────────┐                             │
│           │    Maven Compile    │                             │
│           │    测试用例编译     │                             │
│           └──────────┬──────────┘                             │
│                      ▼                                        │
│           ┌─────────────────────┐                             │
│           │     Surefire        │                             │
│           │     运行测试        │                             │
│           └─────────────────────┘                             │
└──────────────────────────────────────────────────────────────┘

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
```

## 模块职责

### 测试代码

- 测试用例放在 `JavaTestSkeleton/src/test/java/`
- 通过 import 引入被测项目的类
- 使用 JUnit 5 + Mockito + H2 + AssertJ

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

1. **Git Submodule 架构**：被测项目作为 submodule 加入，测试代码测试这些 submodule 中的代码
2. **Surefire vs Failsafe**：单元测试用 Surefire（`*Test.java`），集成测试用 Failsafe（`*IT.java`），互不干扰
3. **H2 内存数据库**：零外部依赖，测试数据自动销毁，CI 友好
4. **4 种 Lint 互补**：Checkstyle 管风格、PMD 管质量、SpotBugs 管 bug 模式、OWASP 管依赖漏洞
5. **PIT 变异测试**：验证测试用例是否真正有效，而非仅看覆盖率
6. **并行 CI**：7 个 Job 独立运行，总耗时 = 最慢的单个 Job
