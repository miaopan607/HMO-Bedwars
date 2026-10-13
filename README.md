![Logo](./.github/assets/logo_open_source.png)


As from November 1st 2021 BedWars1058 by Andrei Dascălu becomes open source under GNU GPL 3.0 license. If you are a developer I would really appreciate if you'd come with pull requests instead of making hundreds of forks. Let's make updates available for everyone!

[![Discord](https://discordapp.com/api/guilds/201345265821679617/widget.png?style=shield)](https://discord.gg/XdJfN2X)

[![Crowdin](https://support.crowdin.com/assets/badges/localization-at-white-rounded-bordered@1x.svg)](https://crowdin.com/project/bedwars1058)

# Description
BedWars is a mini-game where you have to defend your bed and destroy the others.  
Once your bed is destroyed, you cannot respawn.

Players can send arena-wide messages during a game with `/shout <message>` or `!<message>`.
The `bw.shout` permission is granted by default; explicitly denying it blocks shout messages
even when the player has `bw.*`. The existing shout cooldown still applies; `bw.shout.bypass`
only bypasses that cooldown and does not override a denied `bw.shout` permission.

Start a chat message with `@` to send it to every online player on the current server,
including lobby players, other arenas and spectators. This does not broadcast across
Bungee servers. Spectators can also send these messages to active contestants.
Only a leading ASCII `@` is recognized; an empty message shows usage without consuming cooldown.
The format uses the sender's language and the existing chat color permissions.

```yaml
global-chat:
  require-permission: false
  cooldown: 30
```

By default everyone can send server-wide messages. Set `require-permission: true` to
require `bw.global` or `bw.*`; `bw.global` is not granted by default.
An explicitly denied `bw.global` blocks sending in either mode, even with `bw.*`.
`bw.global.bypass` bypasses only the independent global cooldown; neither global permission
is granted by default. A cooldown of zero or less disables the cooldown.
Reconnects do not reset an active cooldown. The `@` route also works with
`chat-settings.format: false`; ordinary chat keeps its existing behavior.
Messages remain Bukkit chat events so cancellation by moderation plugins is respected.
Restart after installing the plugin so the new permission declarations are loaded.

## 配置热更新（Minecraft 1.8.8）

编辑配置文件后执行 `/bw reload`，沿用原有 reload 权限，不要求先设置大厅。
命令只向执行者列出变化的文件、完整键、增改删类型及旧值 → 新值，并按实际应用时机分类：

- **立即生效（后续操作）**：动态读取的设置及可安全刷新的普通缓存，例如聊天冷却、
  火球/TNT 参数、饥饿开关、消息前缀和商店特殊物品参数。不会重建已经打开的界面或已生成的实体。
- **下一局生效（竞技场重建后）**：竞技场配置、生成器配置及对局倒计时。
  当前竞技场保留自己的配置快照；不踢玩家，也不提前重启对局。
- **需重启生效**：数据库、服务器类型、语言启用集合、启动注册的功能、
  固定调度周期、商店菜单/升级定义缓存及无法确认安全热更新的扩展键。
  新值保留在磁盘，当前运行值不切换。
- **加载失败**：缺失、损坏或类型/取值不合法的文件保留原运行配置，
  不影响其他有效文件加载。修复后可再次 reload。

重复 reload 不会重复列出已经加载但尚待下一局或重启的变化；无新变化时显示“配置无变化”。
密码、令牌等敏感键隐藏旧值和新值。文件范围沿用原 reload 集合：
主配置、告示牌、生成器、商店、升级、已加载语言及当前已加载竞技场；不会自动发现新竞技场或语言。

### 仅 OP 可用的主配置修改命令

```text
/bw config set <已有键> <YAML值>
/bw config set global-chat.cooldown 15
/bw config set team-selector.enabled true
/bw config set storeLink 'https://example.org/store'
/bw config set allowed-commands [shout, bw, leave]
/bw config set allowed-commands []
```

`/bedwars` 等现有别名同样可用。检查 `isOp()`，授予 `bw.*` 不会让非 OP 获得修改权限。
仅修改主配置已有叶子键，支持与原类型匹配的布尔、有限数字、字符串和标量列表；
数字字符串使用 YAML 单引号（例如 `'123'`）。不允许未知键、删除值、
整个配置节、嵌套列表或映射，也不修改其他文件。

命令先验证和保存，再使用与 reload 相同的生效分类；无变化不写文件。
若主配置在磁盘上已有尚未 reload 的修改，命令会拒绝覆盖并提示先 reload。
该功能不使用 Bukkit 全服 `/reload`。首次安装新版 JAR 仍需正常重启服务器。

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



# System requirements
This software runs on [Spigot](https://www.spigotmc.org/) and NMS.
Spigot forks without compiled NMS code are not supported.
Officially supported servers are [spigot](https://www.spigotmc.org/) and [paper](https://papermc.io/).
It is required to use **Java 11** or newer.

The internal world restore system is based on zipping and unzipping maps which can become
heavy if you are still making use of HDDs and you do not have a decent CPU, for a better
and faster restore system we recommend using one of the following solutions:
- [SlimeWorldManager](https://www.spigotmc.org/resources/slimeworldmanager.69974/) plug-in (v2.2.1 **only**)
- [AdvancedWorldManager](https://www.spigotmc.org/resources/advanced-slimeworldmanager.87209/) plug-in (v2.8.0 **only**)
- [AdvancedSlimePaper](https://github.com/InfernalSuite/AdvancedSlimePaper) server jar (**1.20 or newer**)

BedWars1058 will hook into it and do everything for you, no additional configuration is needed.

# Pre-made setups and community add-ons

You can find a list of pre-made server setups and a lot of community add-ons [on BedWars1058 Wiki](https://wiki.andrei1058.dev/docs/BedWars1058/addons).

# Main features

###### Flexible | Ways you can run the plugin:
- **SHARED**: can run among other mini-games on the same spigot instance. Games will only be accessible via commands.
- **MULTIARENA**: will require an entire server instance for hosting the mini-game. It will protect the lobby world and games can be joined via commands, NPCs, signs and GUIs.
- **BUNGEE-LEGACY**: the old classic bungee mode where a game means an entire server instance. You'll be added to the game when joining the server. Arena status will be displayed as MOTD.
- **BUNGEE**: a brand new scalable bungee mode. It can host multiple arenas on the same server instance, clone and start new arenas when needed so other players can join. The server can be automatically restarted after a certain amount of games played. This will require installing [BedWarsProxy](https://www.spigotmc.org/resources/bedwarsproxy.66642/) on your lobby servers so players can join. And of course, you can run as many servers as you want in bungee mode.

###### Language | Per player language system:
- each player can receive messages, holograms, GUIs etc. in their desired language. /bw lang.
- you can either remove or add new languages.
- team names, group names, shop contents and a lot more can be translated in your languages.
- custom titles and subtitles for [starting countdown](https://gitlab.com/andrei1058/BedWars1058/-/wikis/language-configuration#custom-title-sub-title-for-arena-countdown).

###### Lobby removal | Optional:
The waiting-lobby inside the map can be removed once the game starts.

###### Arena Groups | Customization:
- you can group arenas by type (4v4, 50v50). You can name them however you want.
- groups can have custom scoreboard layouts, team upgrades, start items and custom generator settings.
- you can join maps by group: /bw join Solo, /bw gui Solo.

###### Shop | Customization:
- you may configure quick-buy default items.
- you may add or remove categories.
- you may add new shop items or execute commands when bought.
- permanent items are given after you re-spawn.
- permanent items can be downgradable which will make you lose one tier per death.
- items can have weight so you can't buy a weaker item than your current one etc.
- special items available: BedBug, Dream Defender, Egg Bridge, TNT Jump and Straight Fireball.
- quick buy feature is available and is synced between nodes as well in bungee mode.

###### Team Upgrades | Customization:
- you may have different team upgrades per arena group.
- you may either add and remove categories and contents.
- you may make upgrade elements that: enchant items, give potion effects (to team-mates/ base/ enemies when they enter the island), you can edit generator settings and change the dragons amount for the Sudden Death phase.
- you may add new traps that: disenchant-items (sword, armor, bow), give potion effects (team/ base/ enemies), remove potion effect when an enemy enters your island range and trigger commands.

###### Ways to join an arena:

- arena selector, which can be configured. /bw gui will display all arena groups while /bw gui Solo will show games from Solo groups and /bw gui Solo+4v4 will show games from Solo and 4v4.
- you can also join games via NPCs by installing Citizens.
- join-signs are also available with status block.
- commands can be used as well. /bw join random will bring you the most filled arena, while /bw join mapName will send you to the given arena and /bw join groupName+groupName2 will bring you on a map from the given groups.

###### Arena Settings | Customization:
- you can set a custom display name used on signs, GUIs etc.
- option to set the amount of min/ max players and team size.
- toggle options for: allowing spectators, disabling generators for empty teams, disabling NPCs for empty teams, disabling internal drops management, bed holograms usage.
- protection range for team-spawn and team NPCs.
- island radius (for features like triggering traps and map) border radius.
- instant kill on void based on Y coordinate.
- you can create as many teams as you want.
- you can allow map breaking like on a SkyWars game.
- you can toggle generator split.
- custom game rules per map.
- unlimited iron/ gold / emerald (this one can pe activated from upgrades) generators per team.

###### Team allocation and waiting-lobby selection:

```yaml
team-assignment:
  prefer-pairs: true
team-selector:
  enabled: false
```

- The built-in allocator balances unselected players within team capacity. With
  `prefer-pairs: true`, it first minimizes one-player teams, then keeps as many
  teams active as possible; some teams can remain empty. It still starts normally
  when pairs are impossible and keeps at least two opposing teams when automatic
  assignment can do so. Solo arenas use ordinary balanced allocation.
- Set `prefer-pairs: false` to balance across as many teams as possible instead.
  Parties stay together only when they fit the balanced target sizes; otherwise
  their members can be split.
- Enable `team-selector.enabled` to let players choose or cancel a team while
  waiting or during the countdown. The selector uses wool in hotbar slot 5,
  overriding any configured waiting item in that slot only while enabled.
  Selected teams preview their colors on leather armor and in TAB, including when
  waiting/starting TAB formatting is otherwise disabled. Selection menus use
  the configurable `team-selector-open` and `team-selector-close` sounds.
  Hovering a team's wool lists all players currently selecting that team; the
  list refreshes when players select, switch, cancel, or leave.
- Manual selections reserve capacity without joining the actual team until game
  start. They are preserved rather than moved for balance, so fixed choices can
  make the final teams uneven. Cancelling restores automatic assignment; leaving,
  spectating or starting the game clears the waiting preview.
- Use `/bw reload` after changing these settings; subsequent team-selection and
  allocation actions use the new values without moving existing selections.
  Existing explicit settings are preserved when missing defaults are added. The
  obsolete `use-experimental-team-assigner` setting is removed on load; its old
  value is not converted into either new setting.
- The existing team-assigner API and assignment-event overrides remain available.
  Third-party assigners receive already committed manual selections and must
  respect existing members; their own assignments or event overrides can change
  the final balance.

###### Vip Kick | Privilege:
Players with bw.vip permission are able to join full arenas in starting phase. This will kick a player without bw.vip permission from that game.

###### Player Statistics:
- we do not provide top holograms but you can use ajLeaderboards for that or LeaderHeads using the placeholders we provide.
- players can see their stats using the internal stats GUI, which can be customized and accessed by /bw stats.

###### Party System:
- we provide a basic and functional internal party system to play with your friends on the same team or arena.
- we also support Parties by AlessioDP and Party and Friends by Simonsator which could be a better solution if you are a large network.

###### Anti AFK System:
Inactive players for more than 45 seconds can't pick-up items from generators.

###### Custom Join Items:
- you can add and remove items that you receive when you join the server (only on multi-arena) and the items you receive when you join a game in starting/ waiting phase or when you join as a spectator.
- join items can execute commands.

###### Map Restore System:
- the default restore adapter from BedWars1058 is based on un-loading the map, un-zipping a backup and loading it again. This may be heavy for servers with cheap hardware. We recommend using gaming processors and a SSD.
- to improve performance we added support for SlimeWorldManager, which loads maps way faster with less performance impact thanks to its slime format. We really encourage you installing this plugin. No manual conversion is required. BedWars1058 will handle everything. Read how to install it here.
- you can also implement your own map adapter trough the API.
- it may seem heavy than other plugins because we don't simply keep track of modified blocks. We need to restore the entire map because server owners can allow players to destroy the maps like on a SkyWars game. Regions like generators, NPCs and team spawns will be protected.

###### Re-Join | Feature:
If you get disconnected, or if you leave a game (configurable) you can re-join it via command or by joining the server again. This is also available in bungee scalable mode.

###### TNT Jump | Feature:
- players are able to do tnt jump with configurable values.
- players with tnt in their inventory have a red particle on their head (configurable).

###### Season events:
- Halloween special. It is enabled automatically based on your machine timezone and will provide cool effects.

# Contributing
Any help is appreciated, just give a quick look at [CONTRIBUTING.md](https://github.com/andrei1058/BedWars1058/blob/master/CONTRIBUTING.md) first!

If you are not a programmer you can help answering people in the [Issues](https://github.com/andrei1058/BedWars1058/issues) section or even [translate the plugin in your languae on Crowdin](https://crowdin.com/project/bedwars1058).

### Translation progress
[Translation Chart](https://badges.awesome-crowdin.com/translation-12780139-594479.png)

# 3rd party libraries
- [bStats](https://bstats.org/getting-started/include-metrics)
- [SidebarLib](https://github.com/andrei1058/SiderbarLib)
- [Commons IO](https://mvnrepository.com/artifact/commons-io/commons-io)
- [HikariCP](https://mvnrepository.com/artifact/com.zaxxer/HikariCP)
- [SLF4J](http://www.slf4j.org/)

# Contact
[![Discord Server](https://discordapp.com/api/guilds/201345265821679617/widget.png?style=banner3)](https://discord.gg/XdJfN2X)