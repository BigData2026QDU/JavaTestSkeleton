# JavaTestSkeleton — Java 通用测试框架

通用的 Java 项目测试框架，通过 Git Submodule 方式集成到任意 Java 项目。

## 功能特性

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

## 快速开始

### 1. 添加为 Git Submodule

```bash
# 在你的测试项目中
git submodule add https://github.com/BigData2026QDU/JavaTestSkeleton.git
```

### 2. 配置 pom.xml

```xml
<!-- 引入测试框架源码 -->
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>build-helper-maven-plugin</artifactId>
    <version>3.5.0</version>
    <executions>
        <execution>
            <id>add-sources</id>
            <phase>generate-sources</phase>
            <goals><goal>add-source</goal></goals>
            <configuration>
                <sources>
                    <source>JavaTestSkeleton/src/main/java</source>
                </sources>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### 3. 添加被测项目

```bash
# 添加被测项目作为 submodule
git submodule add https://github.com/BigData2026QDU/YourProject.git projects/YourProject
```

在 pom.xml 中配置被测项目源码：

```xml
<configuration>
    <sources>
        <source>projects/YourProject/src/main/java</source>
    </sources>
</configuration>
```

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

## 项目结构

```
YourTestProject/
├── AGENTS/                          # 项目规范文档
├── JavaTestSkeleton/                # 测试框架（submodule）
│   ├── src/main/java/               # 测试工具类
│   ├── src/test/java/               # 测试模板
│   └── pom.xml                      # 测试框架配置
├── projects/                        # 被测项目（submodule）
│   └── YourProject/
├── pom.xml                          # 主项目配置
├── Architecture.md
├── README.md
└── File_Index.md
```

## 测试规范

### 命名规范

| 类型 | 命名规则 | 示例 |
|------|----------|------|
| 单元测试类 | `XxxTest.java` | `UserServiceTest.java` |
| 集成测试类 | `XxxIT.java` | `DatabaseIT.java` |
| 测试方法 | `@DisplayName` 中文描述 | `@DisplayName("用户登录成功")` |

### 包命名规范（重要）

**所有 Java 代码必须遵守包命名规范，否则测试工具将无法正常工作：**

1. 包名必须与目录结构完全匹配
2. 包名只使用小写字母
3. 测试类与生产代码使用相同包结构
4. 禁止使用 Java 保留字（`value`、`test`、`class` 等）

```java
// ✅ 正确
src/main/java/org/example/UserService.java  → package org.example;
src/test/java/org/example/UserServiceTest.java → package org.example;

// ❌ 错误
src/test/java/UserServiceTest.java  // 缺少 package 声明
```

## 适配你的项目

### 不需要数据库测试？

删除 H2 依赖和相关配置。

### 不需要 lint？

从 pom.xml 中删除对应的 plugin 即可。

### 不需要变异测试？

删除 pitest-maven 插件配置。

## CI/CD

`.github/workflows/test.yml` 包含 7 个并行任务：

1. 单元测试 + 覆盖率
2. 集成测试
3. 变异测试（PIT）
4. 依赖漏洞扫描（OWASP）
5. Checkstyle
6. PMD
7. SpotBugs
