# JavaTestSkeleton — Java 测试框架骨架

开箱即用的 Java 测试框架，拷贝到任意 Java 项目即可使用。

## 包含什么

| 组件 | 版本 | 用途 |
|------|------|------|
| JUnit 5 | 5.10.2 | 单元测试 + 参数化测试 |
| Mockito | 5.11.0 | Mock 对象 |
| H2 | 2.2.224 | 内存数据库（集成测试） |
| AssertJ | 3.25.3 | 流式断言 |
| Checkstyle | 10.14.2 | 代码风格检查 |
| SpotBugs | 4.8.3.1 | Bug 模式检测 |
| PMD | 3.21.2 | 代码质量分析 |

## 快速开始

### 1. 拷贝到你的项目

```bash
cp -r src/test/   你的项目/src/test/
cp -r .github/    你的项目/.github/
cp pom.xml        你的项目/pom.xml.bak
cp checkstyle.xml spotbugs-exclude.xml pmd-ruleset.xml  你的项目/
```

### 2. 合并 pom.xml

把骨架 `pom.xml` 中的 `<dependencies>` 和 `<plugins>` 合并到你项目的 `pom.xml 中。

### 3. 修改包名

把 `src/test/java/` 下测试类的 `import` 改为你的实际包名。

### 4. 运行

```bash
# 单元测试
mvn test

# 集成测试
mvn verify -DskipTests

# 全部测试 + lint
mvn verify

# 只跑 lint
mvn checkstyle:check pmd:check spotbugs:check
```

## 命令速查

| 命令 | 说明 |
|------|------|
| `mvn test` | 运行单元测试 |
| `mvn verify` | 运行全部测试 + lint |
| `mvn verify -DskipTests` | 只运行集成测试 |
| `mvn checkstyle:check` | 代码风格检查 |
| `mvn pmd:check` | PMD 静态分析 |
| `mvn spotbugs:check` | SpotBugs bug 检测 |

## 项目结构

```
├── pom.xml                              # Maven 配置
├── checkstyle.xml                       # Checkstyle 规则
├── spotbugs-exclude.xml                 # SpotBugs 排除规则
├── pmd-ruleset.xml                      # PMD 规则集
├── src/test/java/
│   ├── BaseTest.java                    # 测试基类
│   ├── UnitTest.java                    # 单元测试模板
│   └── IntegrationTest.java             # 集成测试模板
├── src/test/resources/
│   ├── hibernate.cfg.xml                # H2 测试数据库配置
│   └── test.properties                  # 测试参数
└── .github/workflows/test.yml           # CI 工作流
```

## 适配你的项目

### 只需要数据库测试？

删除不需要的依赖，保留 H2：

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.2.224</version>
    <scope>test</scope>
</dependency>
```

### 不需要 Hibernate？

骨架不依赖 Hibernate。H2 直接用 JDBC 连接，`IntegrationTest.java` 已展示原生 JDBC 用法。

### 不需要 lint？

从 `pom.xml` 中删除对应的 plugin 即可。

## 覆盖规则

| 规则 | 说明 |
|------|------|
| 测试类命名 | `XxxTest.java`（单元）/ `XxxIT.java`（集成） |
| 测试方法命名 | `@DisplayName` 中文描述 |
| 测试目录 | `src/test/java/` |
| 测试配置 | `src/test/resources/` |
