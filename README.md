# JavaTestSkeleton

Java 后端统一测试框架仓库。所有被测项目以 Git submodule 的形式放在 `projects/` 目录下，但每个项目都按独立目标分别验证，不再把多个被测项目源码混在一次测试里。

## 当前被测项目

- `DatabaseConnectModule`
- `JsonUtilModule`
- `ObjectPoolModule`
- `WebMain`

## 仓库结构

```text
JavaTestSkeleton/
├── .github/workflows/test.yml   # GitHub Actions 矩阵 CI
├── AGENTS/                      # 规范子模块
├── JavaTestSkeleton/            # 测试框架代码
│   ├── pom.xml
│   ├── checkstyle.xml
│   ├── pmd-ruleset.xml
│   ├── spotbugs-exclude.xml
│   ├── owasp-suppressions.xml
│   └── suites/
│       ├── databaseconnect/
│       ├── jsonutil/
│       └── objectpool/
├── projects/                    # 全部被测项目 submodule
│   ├── DatabaseConnect/
│   ├── JsonUtilModule/
│   ├── ObjectPoolModule/
│   └── WebMain/
├── README.md
├── Architecture.md
└── File_Index.md
```

## 验证策略

| 被测项目 | 验证方式 | 说明 |
|---------|---------|------|
| `DatabaseConnectModule` | `-Pdatabaseconnect clean verify` | 直接加载该项目源码并运行单元 / 集成测试 |
| `JsonUtilModule` | `-Pjsonutil clean verify` | 直接加载该项目源码并运行 `JsonUtil` 测试 |
| `ObjectPoolModule` | `-Pobjectpool clean verify` | 直接加载该项目源码并运行对象池测试 |
| `WebMain` | 先安装 3 个共享模块 artifact，再执行自身 `clean test` | 保持 `WebMain` 只消费 Maven artifact，不混入其他被测项目源码 |

## 常用命令

### 1. 独立验证共享模块

```bash
mvn -f JavaTestSkeleton/pom.xml -Pdatabaseconnect clean verify -B -ntp
mvn -f JavaTestSkeleton/pom.xml -Pjsonutil clean verify -B -ntp
mvn -f JavaTestSkeleton/pom.xml -Pobjectpool clean verify -B -ntp
```

### 2. 验证 WebMain

```bash
mvn -f projects/DatabaseConnect/pom.xml clean install -DskipTests -B -ntp
mvn -f projects/JsonUtilModule/JsonUtilModule/pom.xml clean install -DskipTests -B -ntp
mvn -f projects/ObjectPoolModule/service-pool/pom.xml clean install -DskipTests -B -ntp
mvn -f projects/WebMain/hivehbase/pom.xml clean test -B -ntp
```

## CI 行为

`.github/workflows/test.yml` 使用矩阵逐个项目执行：

1. 拉取所有 submodule
2. 固定 `AGENTS` 标准版本
3. 以 JDK 17 运行项目独立验证
4. 上传对应项目的测试 / 覆盖率 / 质量报告
5. 成功时向对应被测仓库创建 `[可发布]` Issue
6. 失败时在 `JavaTestSkeleton` 仓库记录 `[测试报告]` Issue

## 关键约束

- 被测仓库必须始终保留在 `projects/` 下的独立 submodule 中
- `WebMain` 不直接编译其他被测项目源码，只依赖本地已安装的 Maven artifact
- `JsonUtilModule`、`DatabaseConnectModule` 的 artifact 元数据必须完整，否则 `WebMain` 侧验证会失败
- 本仓库 CI 使用 JDK 17，与课程项目统一环境一致
