# HMO-Bedwars


As from November 1st 2021 BedWars1058 by Andrei Dascălu becomes open source under GNU GPL 3.0 license. If you are a developer I would really appreciate if you'd come with pull requests instead of making hundreds of forks. Let's make updates available for everyone!

HMO-Bedwars 基于 BedWars1058，仅支持 Minecraft 1.8.8（`v1_8_R3`），保留英文和简体中文。玩家等级、经验奖励、万圣节特殊玩法和 bStats 上报已移除；玩家战绩、金币奖励、团队升级、药水强度及原版经验恢复保留。

## 从 BedWars1058 迁移

构建命令：`mvn -o clean install -DskipTests`。产物位于 `bedwars-plugin/target/HMO-Bedwars-0.2.4-beta.jar`；设置 `BED_WARS_SERVER_DIR` 时仍自动复制到对应的 `plugins` 目录。

1. 停服并备份旧插件 JAR、`plugins/BedWars1058` 和数据库。
2. 将旧数据复制到 `plugins/HMO-Bedwars`；若新目录已存在，不直接覆盖或合并，先确认要使用哪份数据。
3. 在中英文覆盖文件中移除聊天、Tab、侧边栏的玩家等级/经验行及等级、万圣节专用消息键；保留药水名称中的 `{level}`。旧默认 `poweredBy: BedWars1058` 可改为 `HMO-Bedwars`。
4. 将旧插件 JAR 移出 `plugins`，只保留新的 HMO-Bedwars JAR，再启动。不能同时加载两份插件。

旧等级数据库表和其它语言文件不会被主动删除，但插件不再访问等级表或加载其它语言。原 Java 包名、命令、权限、PlaceholderAPI 前缀 `bw1058` 和物品数据标识保留；依赖等级 API、等级事件或 `getForCurrentVersion` 的附属插件需要修改后重新编译。

[![Discord](https://discordapp.com/api/guilds/201345265821679617/widget.png?style=shield)](https://discord.gg/XdJfN2X)


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
主配置、告示牌、生成器、商店、升级、已加载语言的覆盖文件及当前已加载竞技场；不会自动发现新竞技场或语言。

内置语言的默认文件 `Languages/messages_<iso>.yml` 由插件管理，启动及 reload 时按当前内置文案重新生成；用户修改会被覆盖，旧版本遗留键也会被移除。
自定义文案请写入 `Languages/overrides/messages_<iso>.yml`，沿用默认文件的 YAML 键结构，只填写需要覆盖的键。
升级前请将默认文件中需要保留的自定义文案移到覆盖文件；插件不会把旧默认文件里的文案自动转成覆盖值。
覆盖列表整体替换（空列表也有效）；删除覆盖键或覆盖文件后 reload 恢复基础值。
reload 只读取中英文语言覆盖文件，并恢复内置默认文件；普通文案即时生效，商店/升级菜单及日期/计时格式仍需重启。其它 ISO 的文件不会被自动注册；不支持的默认语言回退到英文，玩家旧语言记录沿用默认语言回退。

简体中文玩家聊天统一按频道、队伍（适用时）、身份前缀、玩家名排列；玩家名默认绿色，中文冒号与正文默认白色。大厅/等待使用灰色标签，队内使用青色，公屏/全服使用金色，旁观使用深灰色。单人模式普通聊天使用公屏样式，接收范围不变；已有语言覆盖值仍优先于内置样式。

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


# System requirements
This software runs on [Spigot](https://www.spigotmc.org/) and NMS.
Spigot forks without compiled NMS code are not supported.
仅支持 Minecraft **1.8.8（v1_8_R3）** 的 [Spigot](https://www.spigotmc.org/) 和 [Paper](https://papermc.io/)，不包含其它服务端版本实现。
It is required to use **Java 11** or newer.

The internal world restore system is based on zipping and unzipping maps which can become
heavy if you are still making use of HDDs and you do not have a decent CPU, for a better
and faster restore system we recommend using one of the following solutions:
- [SlimeWorldManager](https://www.spigotmc.org/resources/slimeworldmanager.69974/) plug-in (v2.2.1 **only**)
- [AdvancedWorldManager](https://www.spigotmc.org/resources/advanced-slimeworldmanager.87209/) plug-in (v2.8.0 **only**)

HMO-Bedwars will hook into it and do everything for you, no additional configuration is needed.

# Pre-made setups and community add-ons

You can find a list of pre-made server setups and a lot of community add-ons [on BedWars1058 Wiki](https://wiki.andrei1058.dev/docs/BedWars1058/addons).

# Main features

###### Flexible | Ways you can run the plugin:
- **SHARED**: can run among other mini-games on the same spigot instance. Games will only be accessible via commands.
- **MULTIARENA**: will require an entire server instance for hosting the mini-game. It will protect the lobby world and games can be joined via commands, NPCs, signs and GUIs.
- **BUNGEE-LEGACY**: the old classic bungee mode where a game means an entire server instance. You'll be added to the game when joining the server. Arena status will be displayed as MOTD.
- **BUNGEE**: a brand new scalable bungee mode. It can host multiple arenas on the same server instance, clone and start new arenas when needed so other players can join. The server can be automatically restarted after a certain amount of games played. This will require installing [BedWarsProxy](https://www.spigotmc.org/resources/bedwarsproxy.66642/) on your lobby servers so players can join. And of course, you can run as many servers as you want in bungee mode.

###### Language | Per player language system:
- 玩家可用 `/bw lang` 选择英文（`en`）或简体中文（`zh_cn`）。
- 仅加载这两种语言，保留禁用语言配置；不能通过添加其它语言 YAML 扩展语言列表。
- 队伍名、竞技场组名、商城内容等可在对应语言覆盖文件中自定义。
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
- the default restore adapter from HMO-Bedwars is based on un-loading the map, un-zipping a backup and loading it again. This may be heavy for servers with cheap hardware. We recommend using gaming processors and a SSD.
- to improve performance we added support for SlimeWorldManager, which loads maps way faster with less performance impact thanks to its slime format. We really encourage you installing this plugin. No manual conversion is required. HMO-Bedwars will handle everything. Read how to install it here.
- you can also implement your own map adapter trough the API.
- it may seem heavy than other plugins because we don't simply keep track of modified blocks. We need to restore the entire map because server owners can allow players to destroy the maps like on a SkyWars game. Regions like generators, NPCs and team spawns will be protected.

###### Re-Join | Feature:
If you get disconnected, or if you leave a game (configurable) you can re-join it via command or by joining the server again. This is also available in bungee scalable mode.

###### TNT Jump | Feature:
- players are able to do tnt jump with configurable values.
- players with tnt in their inventory have a red particle on their head (configurable).


# Contributing
Any help is appreciated, just give a quick look at [CONTRIBUTING.md](https://github.com/andrei1058/BedWars1058/blob/master/CONTRIBUTING.md) first!

中英文文案修正可通过本仓库提交；上游贡献方式见上方链接。

# 3rd party libraries
- [SidebarLib](https://github.com/andrei1058/SiderbarLib)
- [Commons IO](https://mvnrepository.com/artifact/commons-io/commons-io)
- [HikariCP](https://mvnrepository.com/artifact/com.zaxxer/HikariCP)
- [SLF4J](http://www.slf4j.org/)

# Contact
[![Discord Server](https://discordapp.com/api/guilds/201345265821679617/widget.png?style=banner3)](https://discord.gg/XdJfN2X)