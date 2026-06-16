# JavaTestSkeleton — Java 测试项目

用于测试 Java 项目的通用测试框架。被测项目作为 Git Submodule 加入到 `projects/` 目录。

## 工作原理

```
JavaTestSkeleton/           # 本仓库（测试项目）
├── JavaTestSkeleton/       # 测试代码和配置
│   ├── pom.xml
│   └── src/test/java/      # 测试用例
├── projects/               # 被测项目（全部为 submodule）
│   ├── DatabaseConnect/    # 被测项目 A
│   └── YourProject/        # 被测项目 B
└── pom.xml                 # 主配置，引入被测项目源码
```

**核心机制：** 通过 `build-helper-maven-plugin` 将 `projects/` 中的被测项目源码引入到编译路径，测试用例直接测试这些 submodule 中的代码。

## 包含组件

| 组件 | 版本 | 用途 |
|------|------|------|
| JUnit 5 | 5.10.2 | 单元测试 + 参数化测试 |
| Mockito | 5.11.0 | Mock 对象 |
| H2 | 2.2.224 | 内存数据库（集成测试） |
| AssertJ | 3.25.3 | 流式断言 |
| JaCoCo | 0.8.11 | 代码覆盖率 |
| PIT | 1.19.1 | 变异测试 |
| Checkstyle | 10.14.2 | 代码风格检查 |
| SpotBugs | 4.8.3.1 | Bug 模式检测 |
| PMD | 3.21.2 | 代码质量分析 |
| OWASP | 9.0.9 | 依赖漏洞扫描 |

## 添加被测项目

### 1. 添加 submodule

```bash
git submodule add https://github.com/BigData2026QDU/YourProject.git projects/YourProject
```

### 2. 配置 pom.xml

在主项目 `pom.xml` 中添加被测项目源码路径：

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>build-helper-maven-plugin</artifactId>
    <version>3.5.0</version>
    <executions>
        <execution>
            <id>add-project-sources</id>
            <phase>generate-sources</phase>
            <goals><goal>add-source</goal></goals>
            <configuration>
                <sources>
                    <!-- 被测项目源码路径 -->
                    <source>projects/YourProject/src/main/java</source>
                </sources>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### 3. 配置 PIT（可选）

如果需要变异测试，还需配置 `pitest-maven` 插件：

```xml
<plugin>
    <groupId>org.pitest</groupId>
    <artifactId>pitest-maven</artifactId>
    <configuration>
        <additionalSources>
            <additionalSource>projects/YourProject/src/main/java</additionalSource>
        </additionalSources>
    </configuration>
</plugin>
```

## 编写测试

测试代码放在 `JavaTestSkeleton/src/test/java/` 目录下。

### 包命名规范（重要）

**本项目统一使用 `org.bigdata` 包名。**

被测项目的 Java 代码必须遵守包命名规范，否则测试工具将无法正常工作：

1. 包名必须与目录结构完全匹配
2. 包名只使用小写字母
3. 禁止使用 Java 保留字（`value`、`test`、`class` 等）

```java
// ✅ 正确：被测项目代码
projects/DatabaseConnect/src/main/java/org/bigdata/tool/HibernateUtil.java
→ package org.bigdata.tool;

// ✅ 正确：测试代码（无需 package 声明，或使用默认包）
JavaTestSkeleton/test/java/HibernateUtilIT.java
```

### 测试类命名

| 类型 | 命名规则 | 示例 |
|------|----------|------|
| 单元测试类 | `XxxTest.java` | `UserServiceTest.java` |
| 集成测试类 | `XxxIT.java` | `DatabaseIT.java` |
| 测试方法 | `@DisplayName` 中文描述 | `@DisplayName("用户登录成功")` |

## 命令速查

| 命令 | 说明 |
|------|------|
| `mvn test` | 运行单元测试 |
| `mvn verify` | 运行全部测试 + lint |
| `mvn verify -DskipTests` | 只运行集成测试 |
| `mvn pitest:mutationCoverage` | 运行变异测试 |
| `mvn jacoco:report` | 生成覆盖率报告 |
| `mvn checkstyle:check` | 代码风格检查 |
| `mvn pmd:check` | PMD 静态分析 |
| `mvn spotbugs:check` | SpotBugs bug 检测 |
| `mvn org.owasp:dependency-check-maven:check` | 依赖漏洞扫描 |

## CI/CD

`.github/workflows/test.yml` 包含 7 个并行任务：

1. 单元测试 + 覆盖率
2. 集成测试
3. 变异测试（PIT）
4. 依赖漏洞扫描（OWASP）
5. Checkstyle
6. PMD
7. SpotBugs
