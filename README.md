# HMO-Bedwars

基于 [BedWars1058](https://github.com/andrei1058/BedWars1058) 的起床战争插件，仅支持 Minecraft **1.8.8（v1_8_R3）**，需要包含 NMS 的 Spigot/Paper 和 **Java 11+**。

语言仅保留英文 `en` 和简体中文 `zh_cn`。已移除玩家等级与经验奖励、万圣节玩法及插件自身的 bStats；保留战绩、金币奖励、商店、团队升级、组队、观战和地图恢复等功能。

## 构建与安装

```bash
mvn -o clean install -DskipTests
```

缺少本地依赖时去掉 `-o`。将 `bedwars-plugin/target/HMO-Bedwars-<版本>.jar` 放入服务器 `plugins/` 后启动。

设置 `BED_WARS_SERVER_DIR` 时，构建会自动复制 JAR 到该服务器的 `plugins/`，不会自动重启。配置位于 `plugins/HMO-Bedwars/`。

GitHub Actions 在推送和 PR 时按 POM 版本构建，可从成功任务的 `HMO-Bedwars` 制品下载 JAR；不依赖上游版本接口或发布凭据。

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

配置默认值见生成的 YAML。修改后执行 `/bw reload`；部分设置下一局或重启后生效，以命令报告为准。

自定义文案写入 `Languages/overrides/messages_en.yml` 或 `messages_zh_cn.yml`，仅填写需要覆盖的键。内置 `Languages/messages_<iso>.yml` 会在启动和重载时重新生成，不要直接修改。

## 许可证

沿用 [GPL-3.0](LICENSE)，保留原作者 Andrei Dascălu 及第三方依赖署名。
