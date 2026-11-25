package com.andrei1058.bedwars.shop;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.andrei1058.bedwars.api.events.player.PlayerJoinArenaEvent;
import com.andrei1058.bedwars.api.events.player.PlayerLeaveArenaEvent;
import com.andrei1058.bedwars.api.events.player.PlayerReJoinEvent;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.database.PlayerPreferenceService;
import com.andrei1058.bedwars.shop.main.ShopCategory;
import com.andrei1058.bedwars.shop.main.ShopIndex;
import com.andrei1058.bedwars.shop.quickbuy.QuickBuyAdd;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

/**
 * 专门记录对局内商店与背包同步轨迹的诊断日志，用于定位客户端/服务端物品不同步问题。
 * 日志写入 plugins/HMO-Bedwars/logs/inventory-sync-YYYY-MM-DD.log。
 */
public final class InventorySyncAudit implements Listener, PluginMessageListener {

    private static final DateTimeFormatter TS_FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final int MAX_QUEUE_SIZE = 16384;

    private static final InventorySyncAudit INSTANCE = new InventorySyncAudit();

    private final Map<UUID, Session> activeSessions = new ConcurrentHashMap<>();
    private final Map<UUID, String> clientBrands = new ConcurrentHashMap<>();
    private final Map<UUID, String> pipelineNames = new ConcurrentHashMap<>();
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicLong droppedLines = new AtomicLong(0);

    private volatile File logDir;
    private volatile Thread writerThread;

    // NMS 反射缓存，仅读取只读字段，避免在 Netty 线程触碰 Bukkit API。
    private static boolean nmsReady = false;
    private static Method mCraftPlayerGetHandle;
    private static Field fEntityPlayerActiveContainer;
    private static Field fContainerWindowId;
    private static Field fNmsItemCount;
    private static Field fNmsItemDamage;
    private static Field fNmsItemItem;
    private static Field fNmsItemTag;
    private static Method mItemGetId;

    private static Field fInClickWinId, fInClickSlot, fInClickBtn, fInClickAction, fInClickItem, fInClickShift;
    private static Field fInCloseId;
    private static Field fInHeldSlot;
    private static Field fInPlacePos, fInPlaceFace, fInPlaceItem;
    private static Field fInUseEntityId, fInUseEntityAction;
    private static Field fOutOpenWinId, fOutOpenType, fOutOpenTitle, fOutOpenSlots;
    private static Field fOutCloseWinId;
    private static Field fOutItemsWinId, fOutItemsArray;
    private static Field fOutSlotWinId, fOutSlotIndex, fOutSlotItem;
    private static Field fOutTransWinId, fOutTransAction, fOutTransAccepted;
    private static Field fOutHeldSlot;

    // ViaVersion API 反射缓存（可选）。
    private static boolean viaChecked = false;
    private static Object viaApi = null;
    private static Method mViaGetPlayerVersion = null;

    static {
        initReflection();
    }

    private static final class Session {
        final UUID uuid;
        final String name;
        final String arena;
        volatile String lastPlaceItemKey;
        volatile String lastPlaceItemDesc;
        volatile int lastPlaceFace = -1;
        volatile long lastPlaceTimeMs;

        Session(UUID uuid, String name, String arena) {
            this.uuid = uuid;
            this.name = name;
            this.arena = arena;
        }
    }

    private InventorySyncAudit() {
    }

    public static InventorySyncAudit getInstance() {
        return INSTANCE;
    }

    private static void initReflection() {
        try {
            String pkg = Bukkit.getServer().getClass().getPackage().getName();
            String ver = pkg.substring(pkg.lastIndexOf('.') + 1);
            String nms = "net.minecraft.server." + ver + ".";

            Class<?> cCraftPlayer = Class.forName("org.bukkit.craftbukkit." + ver + ".entity.CraftPlayer");
            Class<?> cEntityPlayer = Class.forName(nms + "EntityPlayer");
            Class<?> cContainer = Class.forName(nms + "Container");
            Class<?> cItemStack = Class.forName(nms + "ItemStack");
            Class<?> cItem = Class.forName(nms + "Item");

            mCraftPlayerGetHandle = cCraftPlayer.getMethod("getHandle");
            fEntityPlayerActiveContainer = findField(cEntityPlayer, "activeContainer");
            fContainerWindowId = findField(cContainer, "windowId");

            fNmsItemCount = findField(cItemStack, "count");
            fNmsItemDamage = findField(cItemStack, "damage");
            fNmsItemItem = findField(cItemStack, "item");
            fNmsItemTag = findField(cItemStack, "tag");
            mItemGetId = cItem.getMethod("getId", cItem);

            Class<?> cInClick = Class.forName(nms + "PacketPlayInWindowClick");
            fInClickWinId = findField(cInClick, "a");
            fInClickSlot = findField(cInClick, "slot");
            fInClickBtn = findField(cInClick, "button");
            fInClickAction = findField(cInClick, "d");
            fInClickItem = findField(cInClick, "item");
            fInClickShift = findField(cInClick, "shift");

            Class<?> cInClose = Class.forName(nms + "PacketPlayInCloseWindow");
            fInCloseId = findField(cInClose, "id");

            Class<?> cInHeld = Class.forName(nms + "PacketPlayInHeldItemSlot");
            fInHeldSlot = findField(cInHeld, "itemInHandIndex");

            Class<?> cInPlace = Class.forName(nms + "PacketPlayInBlockPlace");
            fInPlacePos = findField(cInPlace, "b");
            fInPlaceFace = findField(cInPlace, "c");
            fInPlaceItem = findField(cInPlace, "d");

            Class<?> cInUse = Class.forName(nms + "PacketPlayInUseEntity");
            fInUseEntityId = findField(cInUse, "a");
            fInUseEntityAction = findField(cInUse, "action");

            Class<?> cOutOpen = Class.forName(nms + "PacketPlayOutOpenWindow");
            fOutOpenWinId = findField(cOutOpen, "a");
            fOutOpenType = findField(cOutOpen, "b");
            fOutOpenTitle = findField(cOutOpen, "c");
            fOutOpenSlots = findField(cOutOpen, "d");

            Class<?> cOutClose = Class.forName(nms + "PacketPlayOutCloseWindow");
            fOutCloseWinId = findField(cOutClose, "a");

            Class<?> cOutItems = Class.forName(nms + "PacketPlayOutWindowItems");
            fOutItemsWinId = findField(cOutItems, "a");
            fOutItemsArray = findField(cOutItems, "b");

            Class<?> cOutSlot = Class.forName(nms + "PacketPlayOutSetSlot");
            fOutSlotWinId = findField(cOutSlot, "a");
            fOutSlotIndex = findField(cOutSlot, "b");
            fOutSlotItem = findField(cOutSlot, "c");

            Class<?> cOutTrans = Class.forName(nms + "PacketPlayOutTransaction");
            fOutTransWinId = findField(cOutTrans, "a");
            fOutTransAction = findField(cOutTrans, "b");
            fOutTransAccepted = findField(cOutTrans, "c");

            Class<?> cOutHeld = Class.forName(nms + "PacketPlayOutHeldItemSlot");
            fOutHeldSlot = findField(cOutHeld, "a");

            nmsReady = true;
        } catch (Throwable t) {
            BedWars.plugin.getLogger().log(Level.WARNING, "InventorySyncAudit reflection init failed", t);
            nmsReady = false;
        }
    }

    private static Field findField(Class<?> cls, String name) throws NoSuchFieldException {
        Class<?> cur = cls;
        while (cur != null) {
            try {
                Field f = cur.getDeclaredField(name);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException ignored) {
                cur = cur.getSuperclass();
            }
        }
        throw new NoSuchFieldException(cls.getName() + "#" + name);
    }

    public static void init() {
        INSTANCE.start();
    }

    public static void shutdown() {
        INSTANCE.stop();
    }

    private void start() {
        if (!running.compareAndSet(false, true)) return;
        logDir = new File(BedWars.plugin.getDataFolder(), "logs");
        if (!logDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            logDir.mkdirs();
        }
        BedWars.registerEvents(this);
        try {
            Bukkit.getMessenger().registerIncomingPluginChannel(BedWars.plugin, "MC|Brand", this);
        } catch (Exception ignored) {
        }
        Thread t = new Thread(this::writerLoop, "HMO-Bedwars-InvAudit");
        t.setDaemon(true);
        writerThread = t;
        t.start();
        emit("SYSTEM", "-", "-", "AUDIT_START version=" + BedWars.plugin.getDescription().getVersion() + " nmsReady=" + nmsReady);
    }

    private void stop() {
        if (!running.compareAndSet(true, false)) return;
        emit("SYSTEM", "-", "-", "AUDIT_STOP dropped=" + droppedLines.get());
        try {
            Bukkit.getMessenger().unregisterIncomingPluginChannel(BedWars.plugin, "MC|Brand", this);
        } catch (Exception ignored) {
        }
        Thread t = writerThread;
        if (t != null) {
            t.interrupt();
            try {
                t.join(2000L);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
        activeSessions.clear();
        clientBrands.clear();
        pipelineNames.clear();
    }

    private void writerLoop() {
        LocalDate currentDate = null;
        BufferedWriter writer = null;
        try {
            while (running.get() || !queue.isEmpty()) {
                String line;
                try {
                    line = queue.poll(500, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    if (!running.get()) {
                        line = queue.poll();
                    } else {
                        continue;
                    }
                }
                if (line == null) {
                    if (writer != null) {
                        try {
                            writer.flush();
                        } catch (IOException ignored) {
                        }
                    }
                    continue;
                }
                LocalDate today = LocalDate.now();
                if (writer == null || !today.equals(currentDate)) {
                    closeQuietly(writer);
                    currentDate = today;
                    File file = new File(logDir, "inventory-sync-" + DATE_FMT.format(today) + ".log");
                    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8));
                }
                writer.write(line);
                writer.newLine();
                int drained = 0;
                while (drained < 256 && (line = queue.poll()) != null) {
                    writer.write(line);
                    writer.newLine();
                    drained++;
                }
                writer.flush();
            }
        } catch (Throwable t) {
            BedWars.plugin.getLogger().log(Level.WARNING, "InventorySyncAudit writer error", t);
        } finally {
            closeQuietly(writer);
        }
    }

    private static void closeQuietly(BufferedWriter writer) {
        if (writer == null) return;
        try {
            writer.flush();
            writer.close();
        } catch (IOException ignored) {
        }
    }

    private void emit(String tag, String arena, String playerName, String detail) {
        StringBuilder sb = new StringBuilder(160);
        sb.append(TS_FMT.format(Instant.now()))
                .append(" [").append(tag).append("] ")
                .append("arena=").append(arena)
                .append(" player=").append(playerName)
                .append(' ').append(detail);
        if (!queue.offer(sb.toString())) {
            droppedLines.incrementAndGet();
        }
    }

    // ==================== 客户端指纹与会话生命周期 ====================

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (player == null || message == null || message.length == 0) return;
        String brand = decodeBrand(message);
        if (!brand.isEmpty()) {
            clientBrands.put(player.getUniqueId(), brand);
            Session s = activeSessions.get(player.getUniqueId());
            if (s != null) {
                emit("CLIENT_BRAND", s.arena, s.name, "brand=" + brand);
            }
        }
    }

    private static String decodeBrand(byte[] data) {
        try {
            int offset = 0;
            // Minecraft VarInt 字符串前缀兼容
            if (data.length > 1 && (data[0] & 0xFF) == data.length - 1) {
                offset = 1;
            }
            return new String(data, offset, data.length - offset, StandardCharsets.UTF_8)
                    .replaceAll("[\\r\\n\\t]", " ").trim();
        } catch (Exception e) {
            return "unknown";
        }
    }

    public static void recordPipeline(Player player, List<String> names) {
        if (player == null || names == null) return;
        INSTANCE.pipelineNames.put(player.getUniqueId(), names.toString());
    }

    private void activatePlayer(Player player, IArena arena, String reason) {
        if (player == null || arena == null) return;
        if (arena.getStatus() != GameState.playing || arena.isSpectator(player)) return;
        Session session = new Session(player.getUniqueId(), player.getName(), arena.getArenaName());
        activeSessions.put(player.getUniqueId(), session);
        boolean bulk = PlayerPreferenceService.getInstance() != null
                && PlayerPreferenceService.getInstance().isShiftBulkBuy(player.getUniqueId());
        emit("SESSION", session.arena, session.name,
                "reason=" + reason
                        + " uuid=" + player.getUniqueId()
                        + " proto=" + getProtocolVersion(player.getUniqueId())
                        + " brand=" + clientBrands.getOrDefault(player.getUniqueId(), "unknown")
                        + " bulkMode=" + bulk
                        + " winId=" + getActiveWindowId(player)
                        + " pipeline=" + pipelineNames.getOrDefault(player.getUniqueId(), "unknown")
                        + " inv=" + snapshotInventory(player));
    }

    private void deactivatePlayer(UUID uuid, String reason) {
        Session removed = activeSessions.remove(uuid);
        if (removed != null) {
            emit("SESSION_END", removed.arena, removed.name, "reason=" + reason);
        }
    }

    private static String getProtocolVersion(UUID uuid) {
        if (!viaChecked) {
            viaChecked = true;
            try {
                Class<?> viaClass = Class.forName("com.viaversion.viaversion.api.Via");
                viaApi = viaClass.getMethod("getAPI").invoke(null);
                if (viaApi != null) {
                    mViaGetPlayerVersion = viaApi.getClass().getMethod("getPlayerVersion", UUID.class);
                }
            } catch (Throwable ignored) {
                viaApi = null;
            }
        }
        if (viaApi != null && mViaGetPlayerVersion != null) {
            try {
                Object v = mViaGetPlayerVersion.invoke(viaApi, uuid);
                return String.valueOf(v);
            } catch (Throwable ignored) {
            }
        }
        return "native(47)";
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameStateChange(GameStateChangeEvent event) {
        IArena arena = event.getArena();
        if (arena == null) return;
        if (event.getNewState() == GameState.playing) {
            for (Player p : arena.getPlayers()) {
                activatePlayer(p, arena, "GAME_START");
            }
        } else if (event.getNewState() == GameState.restarting) {
            for (Player p : arena.getPlayers()) {
                deactivatePlayer(p.getUniqueId(), "GAME_RESTART");
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onArenaJoin(PlayerJoinArenaEvent event) {
        if (event.getArena() != null && event.getArena().getStatus() == GameState.playing) {
            activatePlayer(event.getPlayer(), event.getArena(), "ARENA_JOIN");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onArenaReJoin(PlayerReJoinEvent event) {
        if (event.getArena() != null && event.getArena().getStatus() == GameState.playing) {
            activatePlayer(event.getPlayer(), event.getArena(), "ARENA_REJOIN");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onArenaLeave(PlayerLeaveArenaEvent event) {
        deactivatePlayer(event.getPlayer().getUniqueId(), "ARENA_LEAVE");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        deactivatePlayer(id, "QUIT");
        clientBrands.remove(id);
        pipelineNames.remove(id);
    }

    // ==================== Netty 双方向抓包（仅对局中玩家） ====================

    public static void onPacketInbound(Player player, Object packet) {
        if (!nmsReady || player == null || packet == null) return;
        Session s = INSTANCE.activeSessions.get(player.getUniqueId());
        if (s == null) return;
        String simple = packet.getClass().getSimpleName();
        try {
            switch (simple) {
                case "PacketPlayInWindowClick": {
                    int winId = fInClickWinId.getInt(packet);
                    int slot = fInClickSlot.getInt(packet);
                    int btn = fInClickBtn.getInt(packet);
                    short act = fInClickAction.getShort(packet);
                    int mode = fInClickShift.getInt(packet);
                    Object item = fInClickItem.get(packet);
                    INSTANCE.emit("PKT_IN", s.arena, s.name,
                            "WindowClick win=" + winId + " slot=" + slot + " btn=" + btn
                                    + " mode=" + mode + " act=" + act + " item=" + formatNmsItem(item));
                    break;
                }
                case "PacketPlayInCloseWindow": {
                    int winId = fInCloseId.getInt(packet);
                    INSTANCE.emit("PKT_IN", s.arena, s.name, "CloseWindow win=" + winId);
                    break;
                }
                case "PacketPlayInHeldItemSlot": {
                    int slot = fInHeldSlot.getInt(packet);
                    INSTANCE.emit("PKT_IN", s.arena, s.name, "HeldItemSlot slot=" + slot);
                    break;
                }
                case "PacketPlayInUseEntity": {
                    Object action = fInUseEntityAction.get(packet);
                    if (action != null && !"ATTACK".equals(action.toString())) {
                        int entityId = fInUseEntityId.getInt(packet);
                        INSTANCE.emit("PKT_IN", s.arena, s.name,
                                "UseEntity entityId=" + entityId + " action=" + action);
                    }
                    break;
                }
                case "PacketPlayInBlockPlace": {
                    int face = fInPlaceFace.getInt(packet);
                    Object item = fInPlaceItem.get(packet);
                    s.lastPlaceFace = face;
                    s.lastPlaceItemKey = nmsItemKey(item);
                    s.lastPlaceItemDesc = formatNmsItem(item);
                    s.lastPlaceTimeMs = System.currentTimeMillis();
                    break;
                }
                default:
                    break;
            }
        } catch (Throwable ignored) {
        }
    }

    public static void onPacketOutbound(Player player, Object packet) {
        if (!nmsReady || player == null || packet == null) return;
        Session s = INSTANCE.activeSessions.get(player.getUniqueId());
        if (s == null) return;
        String simple = packet.getClass().getSimpleName();
        try {
            switch (simple) {
                case "PacketPlayOutOpenWindow": {
                    int winId = fOutOpenWinId.getInt(packet);
                    String type = String.valueOf(fOutOpenType.get(packet));
                    int slots = fOutOpenSlots.getInt(packet);
                    Object title = fOutOpenTitle.get(packet);
                    INSTANCE.emit("PKT_OUT", s.arena, s.name,
                            "OpenWindow win=" + winId + " type=" + type + " slots=" + slots
                                    + " title=" + (title == null ? "null" : title.toString()));
                    break;
                }
                case "PacketPlayOutCloseWindow": {
                    int winId = fOutCloseWinId.getInt(packet);
                    INSTANCE.emit("PKT_OUT", s.arena, s.name, "CloseWindow win=" + winId);
                    break;
                }
                case "PacketPlayOutSetSlot": {
                    int winId = fOutSlotWinId.getInt(packet);
                    int slot = fOutSlotIndex.getInt(packet);
                    Object item = fOutSlotItem.get(packet);
                    INSTANCE.emit("PKT_OUT", s.arena, s.name,
                            "SetSlot win=" + winId + " slot=" + slot + " item=" + formatNmsItem(item));
                    break;
                }
                case "PacketPlayOutWindowItems": {
                    int winId = fOutItemsWinId.getInt(packet);
                    Object[] arr = (Object[]) fOutItemsArray.get(packet);
                    INSTANCE.emit("PKT_OUT", s.arena, s.name,
                            "WindowItems win=" + winId + " len=" + (arr == null ? 0 : arr.length)
                                    + " " + formatWindowItemsSummary(winId, arr));
                    break;
                }
                case "PacketPlayOutTransaction": {
                    int winId = fOutTransWinId.getInt(packet);
                    short act = fOutTransAction.getShort(packet);
                    boolean accepted = fOutTransAccepted.getBoolean(packet);
                    INSTANCE.emit("PKT_OUT", s.arena, s.name,
                            "Transaction win=" + winId + " act=" + act + " accepted=" + accepted);
                    break;
                }
                case "PacketPlayOutHeldItemSlot": {
                    int slot = fOutHeldSlot.getInt(packet);
                    INSTANCE.emit("PKT_OUT", s.arena, s.name, "HeldItemSlot slot=" + slot);
                    break;
                }
                default:
                    break;
            }
        } catch (Throwable ignored) {
        }
    }

    // ==================== 商店与背包主线程事件打点 ====================

    public static void logShopOpen(Player player, String source, boolean callEvent, int prevWinId, boolean wasIndex, boolean wasCategory) {
        if (player == null) return;
        Session s = INSTANCE.activeSessions.get(player.getUniqueId());
        if (s == null) {
            IArena a = Arena.getArenaByPlayer(player);
            if (a == null || a.getStatus() != GameState.playing) return;
            INSTANCE.activatePlayer(player, a, "LAZY_SHOP_OPEN");
            s = INSTANCE.activeSessions.get(player.getUniqueId());
            if (s == null) return;
        }
        INSTANCE.emit("SHOP_OPEN", s.arena, s.name,
                "src=" + source + " callEvent=" + callEvent
                        + " prevWin=" + prevWinId + " newWin=" + getActiveWindowId(player)
                        + " wasIdx=" + wasIndex + " wasCat=" + wasCategory
                        + " held=" + player.getInventory().getHeldItemSlot()
                        + "(" + formatBukkitItem(player.getItemInHand()) + ")"
                        + " cursor=" + formatBukkitItem(player.getItemOnCursor())
                        + " inv=" + snapshotInventory(player));
    }

    public static void logShopClick(Player player, InventoryClickEvent e, ShopClickMode mode, ShopClickMode.Action resolved) {
        if (player == null || e == null) return;
        Session s = INSTANCE.activeSessions.get(player.getUniqueId());
        if (s == null) return;
        INSTANCE.emit("SHOP_CLICK", s.arena, s.name,
                "win=" + getActiveWindowId(player)
                        + " click=" + e.getClick()
                        + " action=" + e.getAction()
                        + " rawSlot=" + e.getRawSlot()
                        + " slot=" + e.getSlot()
                        + " hotbar=" + e.getHotbarButton()
                        + " clickedInv=" + (e.getClickedInventory() == null ? "null" : e.getClickedInventory().getType())
                        + " topSize=" + (e.getView().getTopInventory() == null ? -1 : e.getView().getTopInventory().getSize())
                        + " mode=" + mode + " resolved=" + resolved
                        + " idx=" + ShopIndex.getIndexViewers().contains(player.getUniqueId())
                        + " cat=" + ShopCategory.getCategoryViewers().contains(player.getUniqueId())
                        + " qadd=" + QuickBuyAdd.getQuickBuyAdds().containsKey(player.getUniqueId())
                        + " current=" + formatBukkitItem(e.getCurrentItem())
                        + " cursor=" + formatBukkitItem(e.getCursor()));
    }

    public static void logShopBuyStart(Player player, String identifier, boolean bulk, int price, Material currency,
                                       int money, int limit, int amount, boolean batchItems) {
        if (player == null) return;
        Session s = INSTANCE.activeSessions.get(player.getUniqueId());
        if (s == null) return;
        INSTANCE.emit("SHOP_BUY_BEGIN", s.arena, s.name,
                "id=" + identifier + " bulk=" + bulk + " price=" + price + " cur=" + currency
                        + " money=" + money + " limit=" + limit + " amount=" + amount + " batch=" + batchItems
                        + " win=" + getActiveWindowId(player)
                        + " invBefore=" + snapshotInventory(player));
    }

    public static void logShopBuyEnd(Player player, String identifier, int amount) {
        if (player == null) return;
        Session s = INSTANCE.activeSessions.get(player.getUniqueId());
        if (s == null) return;
        INSTANCE.emit("SHOP_BUY_END", s.arena, s.name,
                "id=" + identifier + " amount=" + amount
                        + " win=" + getActiveWindowId(player)
                        + " invAfter=" + snapshotInventory(player));
    }

    public static void logShopBuyRejected(Player player, String identifier, String reason, int money, int price) {
        if (player == null) return;
        Session s = INSTANCE.activeSessions.get(player.getUniqueId());
        if (s == null) return;
        INSTANCE.emit("SHOP_BUY_REJECT", s.arena, s.name,
                "id=" + identifier + " reason=" + reason + " money=" + money + " price=" + price
                        + " win=" + getActiveWindowId(player));
    }

    public static void logUpgradableAction(Player player, String reason, InventoryClickEvent e) {
        if (player == null || e == null) return;
        Session s = INSTANCE.activeSessions.get(player.getUniqueId());
        if (s == null) return;
        INSTANCE.emit("SHOP_UPGRADABLE", s.arena, s.name,
                "reason=" + reason + " win=" + getActiveWindowId(player)
                        + " click=" + e.getClick() + " action=" + e.getAction()
                        + " rawSlot=" + e.getRawSlot() + " hotbar=" + e.getHotbarButton()
                        + " current=" + formatBukkitItem(e.getCurrentItem())
                        + " cursor=" + formatBukkitItem(e.getCursor()));
    }

    public static void logRespawn(Player player) {
        if (player == null) return;
        Session s = INSTANCE.activeSessions.get(player.getUniqueId());
        if (s == null) return;
        ShopCache sc = ShopCache.getShopCache(player.getUniqueId());
        StringBuilder perms = new StringBuilder("[");
        if (sc != null) {
            for (ShopCache.CachedItem ci : sc.getCachedPermanents()) {
                if (perms.length() > 1) perms.append(',');
                perms.append(ci.getCc().getIdentifier()).append(":t").append(ci.getTier());
            }
        }
        perms.append(']');
        INSTANCE.emit("RESPAWN", s.arena, s.name,
                "win=" + getActiveWindowId(player)
                        + " held=" + player.getInventory().getHeldItemSlot()
                        + " perms=" + perms
                        + " inv=" + snapshotInventory(player));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        Session s = activeSessions.get(player.getUniqueId());
        if (s == null) return;
        emit("INV_CLOSE", s.arena, s.name,
                "invType=" + event.getInventory().getType()
                        + " topSize=" + (event.getView().getTopInventory() == null ? -1 : event.getView().getTopInventory().getSize())
                        + " win=" + getActiveWindowId(player)
                        + " idx=" + ShopIndex.getIndexViewers().contains(player.getUniqueId())
                        + " cat=" + ShopCategory.getCategoryViewers().contains(player.getUniqueId())
                        + " held=" + player.getInventory().getHeldItemSlot()
                        + " inv=" + snapshotInventory(player));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        Session s = activeSessions.get(player.getUniqueId());
        if (s == null) return;
        boolean inShop = ShopIndex.getIndexViewers().contains(player.getUniqueId())
                || ShopCategory.getCategoryViewers().contains(player.getUniqueId());
        if (!inShop && !event.isCancelled()) return;
        emit("INV_DRAG", s.arena, s.name,
                "inShop=" + inShop + " cancelled=" + event.isCancelled()
                        + " win=" + getActiveWindowId(player)
                        + " rawSlots=" + event.getRawSlots()
                        + " oldCursor=" + formatBukkitItem(event.getOldCursor())
                        + " newCursor=" + formatBukkitItem(event.getCursor()));
    }

    /**
     * 关键不同步探测器：
     * 1) 1.8.8 客户端在 PacketPlayInBlockPlace 中会携带客户端认为手持的物品，与服务端 getItemInHand 对比即可直接捕获假物品。
     * 2) 1.9+ 经 ViaVersion 时该字段由服务端 HandItemCache 填充，因此当玩家拿金锭/剪刀/斧/镐/空手右键方块时也记录 SUSPECT_PLACE。
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteractCheck(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        Session s = activeSessions.get(player.getUniqueId());
        if (s == null) return;

        ItemStack serverHand = player.getInventory().getItemInHand();
        String serverKey = bukkitItemKey(serverHand);
        long now = System.currentTimeMillis();
        boolean recentPkt = (now - s.lastPlaceTimeMs) <= 250L && s.lastPlaceFace != 255;
        boolean packetMismatch = recentPkt && s.lastPlaceItemKey != null && !s.lastPlaceItemKey.equals(serverKey);
        Block clicked = event.getClickedBlock();
        boolean suspectNonBlock = isSuspectRightClickBlockItem(serverHand, clicked);
        if (!packetMismatch && !suspectNonBlock) return;

        String tag = packetMismatch ? "DESYNC_DETECTED" : "SUSPECT_PLACE";
        emit(tag, s.arena, s.name,
                "heldSlot=" + player.getInventory().getHeldItemSlot()
                        + " serverHand=" + formatBukkitItem(serverHand)
                        + " clientPktItem=" + (recentPkt ? s.lastPlaceItemDesc : "none")
                        + " clickedBlock=" + (clicked == null ? "null" : clicked.getType() + "@" + clicked.getX() + "," + clicked.getY() + "," + clicked.getZ())
                        + " face=" + event.getBlockFace()
                        + " win=" + getActiveWindowId(player)
                        + " idx=" + ShopIndex.getIndexViewers().contains(player.getUniqueId())
                        + " cat=" + ShopCategory.getCategoryViewers().contains(player.getUniqueId())
                        + " cursor=" + formatBukkitItem(player.getItemOnCursor())
                        + " inv=" + snapshotInventory(player));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlaceCheck(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Session s = activeSessions.get(player.getUniqueId());
        if (s == null) return;
        long now = System.currentTimeMillis();
        boolean recentPkt = (now - s.lastPlaceTimeMs) <= 250L && s.lastPlaceFace != 255;
        if (!recentPkt || s.lastPlaceItemKey == null) return;
        String placedKey = bukkitItemKey(event.getItemInHand());
        if (!s.lastPlaceItemKey.equals(placedKey)) {
            emit("DESYNC_BLOCK_PLACE", s.arena, s.name,
                    "cancelled=" + event.isCancelled()
                            + " heldSlot=" + player.getInventory().getHeldItemSlot()
                            + " serverHand=" + formatBukkitItem(event.getItemInHand())
                            + " placedBlock=" + event.getBlockPlaced().getType()
                            + " clientPktItem=" + s.lastPlaceItemDesc
                            + " inv=" + snapshotInventory(player));
        }
    }

    private static boolean isSuspectRightClickBlockItem(ItemStack item, Block clicked) {
        if (item == null || item.getType() == Material.AIR) {
            if (clicked != null) {
                switch (clicked.getType()) {
                    case CHEST:
                    case ENDER_CHEST:
                    case TRAPPED_CHEST:
                    case BED_BLOCK:
                    case WOODEN_DOOR:
                    case TRAP_DOOR:
                    case FENCE_GATE:
                    case LEVER:
                    case STONE_BUTTON:
                    case WOOD_BUTTON:
                        return false;
                    default:
                        break;
                }
            }
            return true;
        }
        switch (item.getType()) {
            case GOLD_INGOT:
            case IRON_INGOT:
            case DIAMOND:
            case EMERALD:
            case SHEARS:
            case WOOD_AXE:
            case STONE_AXE:
            case IRON_AXE:
            case GOLD_AXE:
            case DIAMOND_AXE:
            case WOOD_PICKAXE:
            case STONE_PICKAXE:
            case IRON_PICKAXE:
            case GOLD_PICKAXE:
            case DIAMOND_PICKAXE:
                return true;
            default:
                return false;
        }
    }

    // ==================== 辅助格式化 ====================

    public static int getActiveWindowId(Player player) {
        if (!nmsReady || player == null) return -999;
        try {
            Object handle = mCraftPlayerGetHandle.invoke(player);
            Object container = fEntityPlayerActiveContainer.get(handle);
            return container == null ? -999 : fContainerWindowId.getInt(container);
        } catch (Throwable ignored) {
            return -999;
        }
    }

    private static String snapshotInventory(Player player) {
        PlayerInventory inv = player.getInventory();
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (int slot = 0; slot < 36; slot++) {
            ItemStack item = inv.getItem(slot);
            if (item == null || item.getType() == Material.AIR) continue;
            if (!first) sb.append(',');
            first = false;
            sb.append(slot).append('=').append(formatBukkitItem(item));
        }
        sb.append('}');
        return sb.toString();
    }

    private static String bukkitItemKey(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return "AIR";
        return item.getType().name() + ":" + item.getDurability();
    }

    private static String formatBukkitItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return "AIR";
        StringBuilder sb = new StringBuilder();
        sb.append(item.getType().name());
        if (item.getDurability() != 0) {
            sb.append(':').append(item.getDurability());
        }
        sb.append('*').append(item.getAmount());
        return sb.toString();
    }

    private static String nmsItemKey(Object nmsItem) {
        if (nmsItem == null) return "AIR";
        try {
            Object item = fNmsItemItem.get(nmsItem);
            if (item == null) return "AIR";
            int id = (Integer) mItemGetId.invoke(null, item);
            @SuppressWarnings("deprecation")
            Material mat = Material.getMaterial(id);
            int data = fNmsItemDamage.getInt(nmsItem);
            return (mat == null ? ("ID" + id) : mat.name()) + ":" + data;
        } catch (Throwable t) {
            return "ERR";
        }
    }

    private static String formatNmsItem(Object nmsItem) {
        if (nmsItem == null) return "AIR";
        try {
            Object item = fNmsItemItem.get(nmsItem);
            if (item == null) return "AIR";
            int id = (Integer) mItemGetId.invoke(null, item);
            @SuppressWarnings("deprecation")
            Material mat = Material.getMaterial(id);
            int count = fNmsItemCount.getInt(nmsItem);
            int data = fNmsItemDamage.getInt(nmsItem);
            Object tag = fNmsItemTag.get(nmsItem);
            StringBuilder sb = new StringBuilder();
            sb.append(mat == null ? ("ID" + id) : mat.name());
            if (data != 0) sb.append(':').append(data);
            sb.append('*').append(count);
            if (tag != null) sb.append("[nbt]");
            return sb.toString();
        } catch (Throwable t) {
            return "ERR";
        }
    }

    private static String formatWindowItemsSummary(int winId, Object[] items) {
        if (items == null) return "items=null";
        // 玩家背包窗口 (len=45) 玩家格为 9..44；箱子窗口 (len=54+36=90 等) 玩家格为末尾 36 格。
        int playerStart = items.length >= 36 ? items.length - 36 : 0;
        StringBuilder sb = new StringBuilder();
        if (winId != 0 && playerStart > 0) {
            int topNonEmpty = 0;
            for (int i = 0; i < playerStart; i++) {
                if (items[i] != null) topNonEmpty++;
            }
            sb.append("topNonEmpty=").append(topNonEmpty).append(' ');
        }
        sb.append("playerSlots={");
        boolean first = true;
        for (int i = playerStart; i < items.length; i++) {
            if (items[i] == null) continue;
            if (!first) sb.append(',');
            first = false;
            int rel = i - playerStart;
            // 换算成 Bukkit 背包槽位：前 27 格是主背包 9..35，后 9 格是快捷栏 0..8。
            int bukkitSlot = rel >= 27 ? (rel - 27) : (rel + 9);
            sb.append(i).append("(b").append(bukkitSlot).append(")=").append(formatNmsItem(items[i]));
        }
        sb.append('}');
        return sb.toString();
    }
}
