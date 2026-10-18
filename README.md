# HMO-Bedwars

基于 [BedWars1058](https://github.com/andrei1058/BedWars1058) 的起床战争插件，仅支持 Minecraft **1.8.8（v1_8_R3）**，需要包含 NMS 的 Spigot/Paper 和 **Java 11+**。

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

## 独立练习场（Minecraft 1.8.8）

MULTIARENA/SHARED 模式支持火球跳、搭路和末影珍珠自由练习，练习不加入正式竞技场、不产生战绩。每个练习点一次只分配一名玩家；可以在同一独立世界划定多个不重叠的点。

玩家使用 `/bw practice` 打开菜单，或 `/bw practice join fireball|bridge|pearl` 直接进入。
快捷栏箭头重试、床退出，也支持 `/bw practice retry`、`/bw practice leave` 和 `/bw leave`。
跌出区域、低于失败高度或致死伤害会回起点；重试只恢复本轮羊毛并移除投射物，退出恢复进入前的物品与玩家状态。

管理员需要 OP、`bw.*` 或 `bw.practice.admin`，先准备并加载独立世界，在安全起点执行：

```text
/bw practice create bridge-a bridge
/bw practice setspawn bridge-a
```

站在练习范围两个角点分别执行 `/bw practice pos1 bridge-a`、`/bw practice pos2 bridge-a`，
然后执行 `/bw practice faily bridge-a <整数>` 和 `/bw practice enable bridge-a`。
区域必须包括起跳、飞行和落地区域；失败高度不能高于区域下界。`fireball`、`pearl` 使用相同设置流程。

在主城执行 `/bw practice npc set` 放置唯一的静止村民入口，玩家右键打开菜单，不需要 Citizens。
再次 set 移动入口；`/bw practice npc remove` 移除入口。
`/bw practice list` 查看空闲点，`disable|enable|delete <id>` 管理点；
delete 不删除世界，正在使用的点不能修改。管理员 `/bw build` 可编辑没有被占用的练习地形，进入练习前必须关闭建设模式。

配置单独保存在插件数据目录的 `practice.yml`，使用 `/bw practice reload` 重新加载；有玩家练习时拒绝重载。
`/bw reload` 不加载该文件。插件不会生成地形或把现有 Arena 自动转换成练习地图；
练习世界不能是主世界、大厅、正式 Arenas 配置对应世界，世界名不得包含 `bw_temp_`。
未加载的世界仅在已有 `level.dat` 时尝试加载，缺失世界的点不可分配。
正常退出、断线与停服会恢复玩家状态；进程崩溃不保证内存快照恢复。
断线和停服时，原光标物品转回背包以便 1.8 玩家数据保存；背包已满时，多余原物品掉落在返回点。


## 许可证

沿用 [GPL-3.0](LICENSE)，保留原作者 Andrei Dascălu 及第三方依赖署名。
