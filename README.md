# HMO-Bedwars

基于 [BedWars1058](https://github.com/andrei1058/BedWars1058) 的起床战争插件，仅支持 Minecraft **1.8.8（v1_8_R3）**，需要包含 NMS 的 Spigot/Paper 和 **Java 11+**。

## 构建与安装

```bash
mvn -o clean install -DskipTests
```

缺少本地依赖时去掉 `-o`。将 `bedwars-plugin/target/HMO-Bedwars-<版本>.jar` 放入服务器 `plugins/` 后启动。

设置 `BED_WARS_SERVER_DIR` 时，构建会自动复制 JAR 到该服务器的 `plugins/`，不会自动重启。配置位于 `plugins/HMO-Bedwars/`。

## 常用命令

| 命令 | 用途 |
|---|---|
| `/bw cmds` | 查看帮助 |
| `/bw join <地图或分组>` | 加入游戏 |
| `/bw gui` | 选择竞技场 |
| `/bw stats` | 查看战绩 |
| `/bw lang` | 选择语言 |
| `/bw reload` | 重载配置并显示生效时机 |
| `/bw config set <已有键> <YAML值>` | 修改主配置，仅 OP 可用 |

`/bedwars` 同样可用。对局公屏使用 `/shout <内容>` 或 `!<内容>`；`@<内容>` 向当前服务器所有在线玩家发送，不跨子服广播。

## 配置与文案

配置位于 `plugins/HMO-Bedwars/`，修改后执行 `/bw reload`；生效时机以命令报告为准。

文案覆盖写入 `Languages/overrides/messages_en.yml` 或 `messages_zh_cn.yml`，仅填写需要修改的键，不要编辑自动生成的内置语言文件。

## 练习场

MULTIARENA/SHARED 模式支持火球跳、搭路和珍珠练习，不计入正式战绩。

- `/bw practice` 打开菜单；火球和珍珠支持多人共享，搭路提供近远、平高、直斜八种组合。
- 指南针切换练习，箭头重试，床退出；也可使用 `/bw practice retry|leave`。
- 配置文件为 `practice.yml`，使用 `/bw practice reload [点位ID]` 重载。

地图生成、导入及配置见 [练习地图说明](docs/practice-training-map.md)。

## 许可证

沿用 [GPL-3.0](LICENSE)，保留原作者 Andrei Dascălu 及第三方依赖署名。
