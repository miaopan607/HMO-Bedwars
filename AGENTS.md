# 仓库开发规范

## 项目与代码入口

- HMO-Bedwars 仅支持 Minecraft 1.8.8（Spigot `1.8.8-R0.1-SNAPSHOT`、NMS `v1_8_R3`），语言仅保留 `en` 和 `zh_cn`。
- `bedwars-api`：公共接口、事件、配置工具及 `VersionSupport` 契约。
- `bedwars-plugin`：游戏实现；`BedWars.java` 是入口，`API.java` 实现公共 API，`arena/Arena.java` 管理竞技场状态。
- `versionsupport_1_8_R3`、`versionsupport_common`：NMS 实现与共用监听器。版本相关行为通过 `VersionSupport` 调用。

## 修改约定

- 沿用现有 Java 包名、四空格缩进和邻近代码风格，添加简洁必要的注释。
- 公共 API 变更同步修改接口、实现和所有调用方；未经要求不改变命令、权限、`bw1058` 占位符前缀和持久化标识。
- 通过 `Arena` 的现有方法更新游戏状态与集合；保留事件顺序和取消检查。
- Bukkit 世界、实体、物品栏和玩家状态的修改必须在主线程执行。
- 配置沿用 `ConfigPath`、`ConfigManager` 和现有配置类；默认值从源码查看，运行时数据位于 `plugins/HMO-Bedwars/`。

## 构建与提交

使用 Java 11+ 和 Maven，优先离线构建；缺少本地依赖时去掉 `-o`：

```bash
mvn -o clean install -DskipTests
```

- 保留 `BED_WARS_SERVER_DIR`，构建钩子会将插件 JAR 复制到该服务器的 `plugins` 目录；未经要求不要绕过。
- 默认不新增永久测试。
- 未经要求，不重启真实服务器、不迁移或删除真实数据。
- 提交信息遵循 Conventional Commits：`type(scope): description`，描述使用中文。
- 不要测试。