package com.andrei1058.bedwars.support.version.v1_8_R3;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/** 只改变旁观接收者看到的表示，不修改真实游戏模式或服务器计分板。 */
final class SpectatorVisibilitySupport {
    private static final String HANDLER = "HMO_SpectatorVisibility";
    private static final String TEAM = "y~bw_spectators";
    private static final Field[] TRACKING_FIELDS = fields(EntityTrackerEntry.class);
    private static final Field[] SPAWN_FIELDS = fields(PacketPlayOutNamedEntitySpawn.class);
    private static final Field[] TEAM_FIELDS = fields(PacketPlayOutScoreboardTeam.class);
    private static final Field TRACKING_SET = field(EntityTracker.class, "c");
    private static final Field VELOCITY = field(EntityTrackerEntry.class, "u");
    private static final Field INFO_ACTION = field(PacketPlayOutPlayerInfo.class, "a");
    private static final Field INFO_ENTRIES = field(PacketPlayOutPlayerInfo.class, "b");
    private static final Field SPAWN_ID = field(PacketPlayOutNamedEntitySpawn.class, "a");
    private static final Field SPAWN_WATCHER = field(PacketPlayOutNamedEntitySpawn.class, "i");
    private static final Field META_ID = field(PacketPlayOutEntityMetadata.class, "a");
    private static final Field META_VALUES = field(PacketPlayOutEntityMetadata.class, "b");
    private static final Field TEAM_NAME = field(PacketPlayOutScoreboardTeam.class, "a");
    private static final Field TEAM_PREFIX = field(PacketPlayOutScoreboardTeam.class, "c");
    private static final Field TEAM_SUFFIX = field(PacketPlayOutScoreboardTeam.class, "d");
    private static final Field TEAM_MEMBERS = field(PacketPlayOutScoreboardTeam.class, "g");
    private static final Field TEAM_ACTION = field(PacketPlayOutScoreboardTeam.class, "h");
    private final Map<UUID, View> views = new HashMap<>();
    private final Map<UUID, TrackingEntry> bindings = new HashMap<>();

    void update(Player viewer, List<Player> spectators, boolean showOthers) {
        EntityPlayer receiver = handle(viewer);
        Map<UUID, Target> targets = new LinkedHashMap<>();
        for (Player player : spectators) {
            EntityPlayer entity = handle(player);
            targets.put(player.getUniqueId(), new Target(entity));
            install(entity);
        }
        Snapshot snapshot = new Snapshot(viewer.getUniqueId(), targets, showOthers);
        View view = views.get(viewer.getUniqueId());
        if (view == null) {
            view = new View(receiver.playerConnection.networkManager.channel);
            views.put(viewer.getUniqueId(), view);
        }
        view.snapshot = snapshot;
        View current = view;
        // 管线操作和投影重建同队列，先发布快照，再发送实体包。
        current.channel.eventLoop().execute(() -> {
            if (!current.channel.isOpen()) return;
            if (current.handler == null) {
                current.handler = new ViewHandler();
                current.channel.pipeline().addBefore("packet_handler", HANDLER, current.handler);
            }
            current.handler.update(snapshot);
        });
        for (Player target : spectators) {
            if (target.equals(viewer)) continue;
            viewer.showPlayer(target);
            TrackingEntry entry = bindings.get(target.getUniqueId());
            if (entry != null) entry.updatePlayer(receiver);
        }
    }

    void clear(Player viewer) {
        View view = views.remove(viewer.getUniqueId());
        if (view != null) clearView(view);
        restore(viewer.getUniqueId());
    }

    void clearAll() {
        for (View view : views.values()) clearView(view);
        views.clear();
        for (UUID target : new ArrayList<>(bindings.keySet())) restore(target);
    }

    private static void clearView(View view) {
        view.channel.eventLoop().execute(() -> {
            // 断线会自动销毁管线，此时无需再向客户端恢复表示。
            if (view.handler != null && view.channel.pipeline().context(view.handler) != null) {
                view.handler.clear();
                view.channel.pipeline().remove(view.handler);
            }
        });
    }

    private void install(EntityPlayer target) {
        EntityTracker tracker = ((WorldServer) target.world).getTracker();
        EntityTrackerEntry original = tracker.trackedEntities.get(target.getId());
        TrackingEntry installed = bindings.get(target.getUniqueID());
        if (installed != null && original == installed) return;
        restore(target.getUniqueID());
        if (original == null) return; // 世界传送尚未注册实体；生命周期的下一 tick 会重建。
        TrackingEntry entry = new TrackingEntry(original, tracker);
        replace(tracker, original, entry);
        bindings.put(target.getUniqueID(), entry);
    }

    private void restore(UUID target) {
        TrackingEntry entry = bindings.remove(target);
        if (entry != null && entry.owner.trackedEntities.get(entry.tracker.getId()) == entry) {
            copy(TRACKING_FIELDS, entry, entry.original);
            replace(entry.owner, entry, entry.original);
        }
    }

    @SuppressWarnings("unchecked")
    private static void replace(EntityTracker tracker, EntityTrackerEntry previous, EntityTrackerEntry next) {
        Set<EntityTrackerEntry> entries = (Set<EntityTrackerEntry>) get(TRACKING_SET, tracker);
        entries.remove(previous);
        entries.add(next);
        tracker.trackedEntities.a(next.tracker.getId(), next);
    }

    private final class TrackingEntry extends EntityTrackerEntry {
        final EntityTrackerEntry original;
        final EntityTracker owner;

        TrackingEntry(EntityTrackerEntry original, EntityTracker owner) {
            super(original.tracker, original.b, original.c, (Boolean) get(VELOCITY, original));
            this.original = original;
            this.owner = owner;
            copy(TRACKING_FIELDS, original, this);
        }

        @Override
        public boolean c(EntityPlayer receiver) {
            View view = views.get(receiver.getUniqueID());
            if (view != null && view.snapshot.targets.containsKey(tracker.getUniqueID())) {
                if (!view.snapshot.show || receiver.world != tracker.world) return false;
                double x = receiver.locX - tracker.locX;
                double z = receiver.locZ - tracker.locZ;
                return x >= -b && x <= b && z >= -b && z <= b;
            }
            original.b = b;
            return original.c(receiver);
        }
    }

    private static final class View {
        final Channel channel;
        Snapshot snapshot;
        ViewHandler handler;
        View(Channel channel) { this.channel = channel; }
    }

    private static final class Target {
        final UUID uuid;
        final int id;
        final String name;
        final PacketPlayOutPlayerInfo info;
        final byte flags;
        Target(EntityPlayer entity) {
            uuid = entity.getUniqueID();
            id = entity.getId();
            name = entity.getProfile().getName();
            info = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entity);
            flags = entity.getDataWatcher().getByte(0);
        }
    }

    private static final class Snapshot {
        final UUID self;
        final Map<UUID, Target> targets;
        final Set<Integer> entities;
        final Set<String> names;
        final boolean show;
        Snapshot(UUID self, Map<UUID, Target> targets, boolean show) {
            this.self = self;
            this.targets = Collections.unmodifiableMap(targets);
            Set<Integer> entities = new HashSet<>();
            Set<String> names = new HashSet<>();
            for (Target target : targets.values()) {
                names.add(target.name);
                if (!target.uuid.equals(self)) entities.add(target.id);
            }
            this.entities = Collections.unmodifiableSet(entities);
            this.names = Collections.unmodifiableSet(names);
            this.show = show;
        }
    }

    private static final class RawTeam {
        String prefix = "", suffix = "";
        final Set<String> names = new HashSet<>();
    }

    private static final class ViewHandler extends ChannelDuplexHandler {
        private final Map<String, RawTeam> teams = new HashMap<>();
        private final Map<String, String> memberships = new HashMap<>();
        private final Map<UUID, PacketPlayOutPlayerInfo.PlayerInfoData> rawInfo = new HashMap<>();
        private final Map<Integer, Byte> rawFlags = new HashMap<>();
        private ChannelHandlerContext context;
        private Snapshot snapshot;
        private boolean enabled = true;

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) { context = ctx; }

        void update(Snapshot next) {
            Snapshot previous = snapshot;
            snapshot = next;
            boolean membersChanged = previous == null || !previous.names.equals(next.names);
            if (previous != null) {
                for (Target target : previous.targets.values()) {
                    if (!next.targets.containsKey(target.uuid)) {
                        restoreTarget(target);
                        rawFlags.remove(target.id);
                    }
                }
                if (membersChanged) emit(new PacketPlayOutScoreboardTeam(commonTeam(previous.names), 1));
            }
            if (membersChanged) emit(new PacketPlayOutScoreboardTeam(commonTeam(next.names), 0));
            for (Target target : next.targets.values()) {
                rememberInfo(target.info);
                emit(info(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, entries(target.info), true));
                if (!target.uuid.equals(next.self)) {
                    emit(info(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_GAME_MODE, entries(target.info), true));
                    rawFlags.put(target.id, target.flags);
                    emit(metadata(target.id, (byte) (target.flags | 0x20)));
                }
            }
            context.flush();
        }

        void clear() {
            enabled = false;
            if (snapshot != null) {
                emit(new PacketPlayOutScoreboardTeam(commonTeam(snapshot.names), 1));
                for (Target target : snapshot.targets.values()) restoreTarget(target);
            }
            context.flush();
        }

        private void restoreTarget(Target target) {
            String team = memberships.get(target.name);
            if (team != null) {
                ScoreboardTeam original = new ScoreboardTeam(new Scoreboard(), team);
                emit(new PacketPlayOutScoreboardTeam(original, Collections.singleton(target.name), 3));
            }
            PacketPlayOutPlayerInfo.PlayerInfoData data = rawInfo.get(target.uuid);
            if (data != null) {
                emit(info(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, Collections.singletonList(data), false));
                if (!target.uuid.equals(snapshot.self))
                    emit(info(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_GAME_MODE, Collections.singletonList(data), false));
            }
            if (!target.uuid.equals(snapshot.self)) {
                Byte flags = rawFlags.get(target.id);
                if (flags != null) emit(metadata(target.id, flags));
            }
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object message, ChannelPromise promise) throws Exception {
            if (!enabled || snapshot == null) { ctx.write(message, promise); return; }
            Object projected = message;
            if (message instanceof PacketPlayOutPlayerInfo) {
                PacketPlayOutPlayerInfo packet = (PacketPlayOutPlayerInfo) message;
                rememberInfo(packet);
                projected = projectInfo(packet);
            } else if (message instanceof PacketPlayOutScoreboardTeam) {
                projected = projectTeam((PacketPlayOutScoreboardTeam) message);
            } else if (message instanceof PacketPlayOutNamedEntitySpawn) {
                PacketPlayOutNamedEntitySpawn packet = (PacketPlayOutNamedEntitySpawn) message;
                int id = (Integer) get(SPAWN_ID, packet);
                if (snapshot.entities.contains(id)) {
                    if (!snapshot.show) { promise.setSuccess(); return; }
                    DataWatcher watcher = (DataWatcher) get(SPAWN_WATCHER, packet);
                    List<DataWatcher.WatchableObject> raw = watcher.c();
                    rememberFlags(id, raw);
                    if (needsInvisibility(raw)) {
                        PacketPlayOutNamedEntitySpawn replacement = new PacketPlayOutNamedEntitySpawn();
                        copy(SPAWN_FIELDS, packet, replacement);
                        DataWatcher own = new DataWatcher(null);
                        for (DataWatcher.WatchableObject value : invisible(raw)) own.a(value.a(), value.b());
                        set(SPAWN_WATCHER, replacement, own);
                        projected = replacement;
                    }
                }
            } else if (message instanceof PacketPlayOutEntityMetadata) {
                PacketPlayOutEntityMetadata packet = (PacketPlayOutEntityMetadata) message;
                int id = (Integer) get(META_ID, packet);
                if (snapshot.entities.contains(id)) {
                    List<DataWatcher.WatchableObject> raw = values(packet);
                    rememberFlags(id, raw);
                    if (needsInvisibility(raw)) projected = metadata(id, invisible(raw));
                }
            }
            ctx.write(projected, promise);
        }

        private void rememberFlags(int id, List<DataWatcher.WatchableObject> values) {
            if (values != null) for (DataWatcher.WatchableObject value : values)
                if (value.a() == 0) rawFlags.put(id, (Byte) value.b());
        }

        private void rememberInfo(PacketPlayOutPlayerInfo packet) {
            PacketPlayOutPlayerInfo.EnumPlayerInfoAction action = (PacketPlayOutPlayerInfo.EnumPlayerInfoAction) get(INFO_ACTION, packet);
            for (PacketPlayOutPlayerInfo.PlayerInfoData data : entries(packet)) {
                UUID uuid = data.a().getId();
                PacketPlayOutPlayerInfo.PlayerInfoData old = rawInfo.get(uuid);
                if (action == PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER) rawInfo.remove(uuid);
                else if (action == PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER || old == null) rawInfo.put(uuid, data);
                else rawInfo.put(uuid, packet.new PlayerInfoData(old.a(),
                        action == PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_LATENCY ? data.b() : old.b(),
                        action == PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_GAME_MODE ? data.c() : old.c(),
                        action == PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME ? data.d() : old.d()));
            }
        }

        private PacketPlayOutPlayerInfo projectInfo(PacketPlayOutPlayerInfo packet) {
            PacketPlayOutPlayerInfo.EnumPlayerInfoAction action = (PacketPlayOutPlayerInfo.EnumPlayerInfoAction) get(INFO_ACTION, packet);
            if (action != PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER
                    && action != PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_GAME_MODE
                    && action != PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME) return packet;
            for (PacketPlayOutPlayerInfo.PlayerInfoData data : entries(packet))
                if (snapshot.targets.containsKey(data.a().getId())) return info(action, entries(packet), true);
            return packet;
        }

        private PacketPlayOutPlayerInfo info(PacketPlayOutPlayerInfo.EnumPlayerInfoAction action,
                                             List<PacketPlayOutPlayerInfo.PlayerInfoData> entries, boolean project) {
            PacketPlayOutPlayerInfo result = new PacketPlayOutPlayerInfo();
            set(INFO_ACTION, result, action);
            List<PacketPlayOutPlayerInfo.PlayerInfoData> output = entries(result);
            for (PacketPlayOutPlayerInfo.PlayerInfoData data : entries) {
                boolean target = project && snapshot.targets.containsKey(data.a().getId());
                IChatBaseComponent display = data.d();
                String teamName = memberships.get(data.a().getName());
                RawTeam team = teams.get(teamName);
                if (target && team != null) display = new ChatComponentText(team.prefix + data.a().getName() + team.suffix);
                output.add(result.new PlayerInfoData(data.a(), data.b(),
                        target && !snapshot.self.equals(data.a().getId()) ? WorldSettings.EnumGamemode.ADVENTURE : data.c(), display));
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        private PacketPlayOutScoreboardTeam projectTeam(PacketPlayOutScoreboardTeam packet) {
            String name = (String) get(TEAM_NAME, packet);
            if (TEAM.equals(name)) return packet;
            int action = (Integer) get(TEAM_ACTION, packet);
            Collection<String> members = (Collection<String>) get(TEAM_MEMBERS, packet);
            RawTeam team = teams.get(name);
            if (action == 0) { team = new RawTeam(); teams.put(name, team); }
            if (team != null) {
                if (action == 0 || action == 2) {
                    team.prefix = (String) get(TEAM_PREFIX, packet);
                    team.suffix = (String) get(TEAM_SUFFIX, packet);
                }
                if (action == 0 || action == 3) for (String member : members) {
                    String previous = memberships.put(member, name);
                    RawTeam old = teams.get(previous);
                    if (old != null) old.names.remove(member);
                    team.names.add(member);
                }
                if (action == 4) for (String member : members) {
                    team.names.remove(member);
                    memberships.remove(member, name);
                }
                if (action == 1) {
                    for (String member : team.names) memberships.remove(member, name);
                    teams.remove(name);
                }
            }
            for (Target target : snapshot.targets.values()) {
                if (!name.equals(memberships.get(target.name)) && !members.contains(target.name)
                        && (team == null || !team.names.contains(target.name))) continue;
                PacketPlayOutPlayerInfo.PlayerInfoData raw = rawInfo.get(target.uuid);
                if (raw != null) emit(info(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, Collections.singletonList(raw), true));
            }
            if (action != 0 && action != 3 && action != 4) return packet;
            if (Collections.disjoint(members, snapshot.names)) return packet;
            List<String> filtered = new ArrayList<>(members);
            filtered.removeAll(snapshot.names);
            PacketPlayOutScoreboardTeam replacement = new PacketPlayOutScoreboardTeam();
            copy(TEAM_FIELDS, packet, replacement);
            set(TEAM_MEMBERS, replacement, filtered);
            return replacement;
        }

        private void emit(Object packet) { context.write(packet); }
    }

    private static ScoreboardTeam commonTeam(Collection<String> names) {
        ScoreboardTeam team = new ScoreboardTeam(new Scoreboard(), TEAM);
        team.setCanSeeFriendlyInvisibles(true);
        team.getPlayerNameSet().addAll(names);
        return team;
    }

    private static boolean needsInvisibility(List<DataWatcher.WatchableObject> values) {
        if (values != null) for (DataWatcher.WatchableObject value : values)
            if (value.a() == 0) return (((Byte) value.b()) & 0x20) == 0;
        return false;
    }

    private static List<DataWatcher.WatchableObject> invisible(List<DataWatcher.WatchableObject> values) {
        List<DataWatcher.WatchableObject> result = new ArrayList<>(values.size());
        for (DataWatcher.WatchableObject value : values) result.add(value.a() == 0
                ? new DataWatcher.WatchableObject(value.c(), 0, (byte) (((Byte) value.b()) | 0x20)) : value);
        return result;
    }

    @SuppressWarnings("unchecked")
    private static List<DataWatcher.WatchableObject> values(PacketPlayOutEntityMetadata packet) {
        return (List<DataWatcher.WatchableObject>) get(META_VALUES, packet);
    }

    private static PacketPlayOutEntityMetadata metadata(int id, byte flags) {
        return metadata(id, Collections.singletonList(new DataWatcher.WatchableObject(0, 0, flags)));
    }

    private static PacketPlayOutEntityMetadata metadata(int id, List<DataWatcher.WatchableObject> values) {
        PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata();
        set(META_ID, packet, id);
        set(META_VALUES, packet, values);
        return packet;
    }

    @SuppressWarnings("unchecked")
    private static List<PacketPlayOutPlayerInfo.PlayerInfoData> entries(PacketPlayOutPlayerInfo packet) {
        return (List<PacketPlayOutPlayerInfo.PlayerInfoData>) get(INFO_ENTRIES, packet);
    }

    private static EntityPlayer handle(Player player) { return ((CraftPlayer) player).getHandle(); }

    private static Field field(Class<?> type, String name) {
        try {
            Field field = type.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException e) { throw new IllegalStateException("Cannot initialize spectator visibility: " + type.getName() + "." + name, e); }
    }

    private static Field[] fields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Field field : type.getDeclaredFields()) if (!Modifier.isStatic(field.getModifiers())) {
            field.setAccessible(true);
            fields.add(field);
        }
        return fields.toArray(new Field[0]);
    }

    private static Object get(Field field, Object object) {
        try { return field.get(object); }
        catch (IllegalAccessException e) { throw new IllegalStateException("Cannot read spectator visibility field", e); }
    }

    private static void set(Field field, Object object, Object value) {
        try { field.set(object, value); }
        catch (IllegalAccessException e) { throw new IllegalStateException("Cannot write spectator visibility field", e); }
    }

    private static void copy(Field[] fields, Object source, Object target) {
        for (Field field : fields) set(field, target, get(field, source));
    }
}
