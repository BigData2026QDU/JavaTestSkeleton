# File_Index.md

## 根目录

| 路径 | 作用 |
|------|------|
| `README.md` | 测试框架说明 |
| `Architecture.md` | 架构与验证路径说明 |
| `File_Index.md` | 当前文件 |
| `.gitmodules` | 被测项目 submodule 配置 |
| `.github/workflows/test.yml` | 项目级矩阵 CI |
| `AGENTS/` | 项目规范子模块 |

## `JavaTestSkeleton/`

| 路径 | 作用 |
|------|------|
| `JavaTestSkeleton/pom.xml` | 测试框架主构建文件 |
| `JavaTestSkeleton/checkstyle.xml` | Checkstyle 规则 |
| `JavaTestSkeleton/pmd-ruleset.xml` | PMD 规则 |
| `JavaTestSkeleton/spotbugs-exclude.xml` | SpotBugs 排除规则 |
| `JavaTestSkeleton/owasp-suppressions.xml` | Dependency Check 抑制规则 |

## `JavaTestSkeleton/suites/databaseconnect/`

| 路径 | 作用 |
|------|------|
| `java/BaseTest.java` | H2 / 临时目录等公共测试能力 |
| `java/TestItem.java` | Hibernate 集成测试实体 |
| `java/TestDataFactory.java` | 测试数据构造 |
| `java/ServicePoolManagerTest.java` | 数据库连接模块中的对象池测试 |
| `java/HibernateUtilIT.java` | Hibernate 工具集成测试 |
| `java/DatabaseMetaServiceIT.java` | 元数据服务集成测试 |
| `resources/hibernate.cfg.xml` | H2 测试 Hibernate 配置 |
| `resources/test.properties` | 测试参数 |

## `JavaTestSkeleton/suites/jsonutil/`

| 路径 | 作用 |
|------|------|
| `java/JsonUtilTest.java` | `JsonUtilModule` 独立测试 |

## `JavaTestSkeleton/suites/objectpool/`

| 路径 | 作用 |
|------|------|
| `java/com/servicepool/ObjectPoolServicePoolManagerTest.java` | `ObjectPoolModule` 独立测试 |

## `projects/`

| 路径 | 作用 |
|------|------|
| `projects/DatabaseConnect/` | 被测项目：数据库连接模块 |
| `projects/JsonUtilModule/` | 被测项目：JSON 工具模块 |
| `projects/ObjectPoolModule/` | 被测项目：对象池模块 |
| `projects/WebMain/` | 被测项目：主后端 Web 工程 |
