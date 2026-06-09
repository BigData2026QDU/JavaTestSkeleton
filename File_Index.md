# File_Index.md — 文件索引

## 根目录

| 文件 | 作用 |
|------|------|
| `pom.xml` | Maven 配置。包含 JUnit 5、Mockito、H2、AssertJ 测试依赖，Checkstyle/PMD/SpotBugs lint 插件 |
| `checkstyle.xml` | Checkstyle 规则集。基于 Google Java Style，包含命名规范、格式、文件长度等检查 |
| `spotbugs-exclude.xml` | SpotBugs 排除规则。排除测试代码和自动生成代码 |
| `pmd-ruleset.xml` | PMD 规则集。检查未使用变量、空 catch、冗余代码等，排除测试代码 |
| `README.md` | 项目简介及使用说明 |
| `Architecture.md` | 架构说明文档 |
| `File_Index.md` | 文件索引（本文件） |
| `.gitignore` | Git 忽略规则 |

## src/test/java/

| 文件 | 作用 |
|------|------|
| `BaseTest.java` | 测试基类。提供 H2 数据库连接、临时目录（`@TempDir`）、测试配置读取、SQL 执行辅助、超时断言辅助 |
| `UnitTest.java` | 单元测试模板。覆盖基本断言、AssertJ 流式断言、参数化测试、Mockito mock、异常检测、超时检测、嵌套测试 |
| `IntegrationTest.java` | 集成测试模板。继承 BaseTest，覆盖 H2 数据库 CRUD、事务提交/回滚、边界测试、临时目录使用 |

## src/test/resources/

| 文件 | 作用 |
|------|------|
| `hibernate.cfg.xml` | Hibernate 测试配置。使用 H2 内存数据库，DDL 自动建表，无需外部 MySQL |
| `test.properties` | 测试参数。数据库连接、超时时间、线程数、日志级别等可配置项 |

## .github/workflows/

| 文件 | 作用 |
|------|------|
| `test.yml` | CI 工作流。5 个并行 Job：单元测试、集成测试、Checkstyle、PMD、SpotBugs |
