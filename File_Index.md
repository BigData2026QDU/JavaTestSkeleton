# File_Index.md — 测试项目文件索引

## 根目录

| 文件 | 作用 |
|------|------|
| `pom.xml` | 主配置。引入被测项目源码，配置 JUnit 5、Mockito、H2、AssertJ、JaCoCo、PIT、Checkstyle/PMD/SpotBugs/OWASP |
| `checkstyle.xml` | Checkstyle 规则集。基于 Google Java Style，包含命名规范、格式、文件长度等检查 |
| `spotbugs-exclude.xml` | SpotBugs 排除规则。排除测试代码和自动生成代码 |
| `pmd-ruleset.xml` | PMD 规则集。检查未使用变量、空 catch、冗余代码等，排除测试代码 |
| `owasp-suppressions.xml` | OWASP 依赖漏洞抑制规则 |
| `README.md` | 项目简介及使用说明 |
| `Architecture.md` | 架构说明文档 |
| `File_Index.md` | 文件索引（本文件） |
| `.gitignore` | Git 忽略规则 |
| `.gitmodules` | Git Submodule 配置 |

## JavaTestSkeleton/

| 文件 | 作用 |
|------|------|
| `pom.xml` | 测试框架配置（依赖、插件） |
| `test/java/HibernateUtilIT.java` | HibernateUtil 集成测试。测试 CRUD、事务、HQL 查询 |
| `test/java/DatabaseMetaServiceIT.java` | DatabaseMetaService 集成测试。测试表名查询、列名查询、数据预览 |
| `test/java/ServicePoolManagerTest.java` | ServicePoolManager 单元测试。测试对象池管理 |
| `test/java/TestItem.java` | 测试实体类。用于 Hibernate 集成测试 |
| `test/java/TestDataFactory.java` | 测试数据工厂。生成标准化测试数据 |
| `test/java/BaseTest.java` | 测试基类（如存在）。提供 H2 数据库连接、临时目录等 |
| `test/resources/hibernate.cfg.xml` | Hibernate 测试配置。使用 H2 内存数据库，DDL 自动建表 |
| `test/resources/test.properties` | 测试参数。数据库连接、超时时间、线程数、日志级别等可配置项 |

## projects/ (被测项目，全部为 submodule)

| 目录 | 作用 |
|------|------|
| `projects/AGENTS/` | 项目规范文档 |
| `projects/DatabaseConnect/` | 被测项目：数据库连接模块 |

## .github/workflows/

| 文件 | 作用 |
|------|------|
| `test.yml` | CI 工作流。7 个并行 Job：单元测试、集成测试、变异测试、依赖漏洞扫描、Checkstyle、PMD、SpotBugs |
