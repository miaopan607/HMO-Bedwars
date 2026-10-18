package com.andrei1058.bedwars.practice;

import com.andrei1058.bedwars.BedWars;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** 先落盘原状态，再改变玩家；每次放块先追加坐标，崩溃后仅清理本插件放置的羊毛。 */
final class PracticeRecovery {
    private final Path root = BedWars.plugin.getDataFolder().toPath().resolve("practice-recovery");
    private final Set<String> blockedWorlds = new HashSet<>();

    PracticeRecovery() { cleanupBlocks(); }

    boolean hasSnapshot(UUID player) { return Files.isRegularFile(snapshotPath(player)); }
    boolean isBlocked(String world) { return blockedWorlds.contains(world); }
    void block(String world) { blockedWorlds.add(world); }
    private Path snapshotPath(UUID player) { return root.resolve("players").resolve(player + ".yml"); }

    PracticeSnapshot capture(Player player) throws IOException {
        if (hasSnapshot(player.getUniqueId())) throw new IOException("Pending practice recovery");
        PracticeSnapshot snapshot = PracticeSnapshot.capture(player);
        Path destination = snapshotPath(player.getUniqueId());
        Files.createDirectories(destination.getParent());
        Path temporary = destination.resolveSibling(destination.getFileName() + ".tmp");
        byte[] data = snapshot.yaml.saveToString().getBytes(StandardCharsets.UTF_8);
        try (FileChannel output = FileChannel.open(temporary, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            while (buffer.hasRemaining()) output.write(buffer);
            output.force(true);
        }
        Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE);
        return snapshot;
    }

    PracticeSnapshot read(UUID player) throws IOException, InvalidConfigurationException {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.load(snapshotPath(player).toFile());
        return new PracticeSnapshot(yaml);
    }
    void complete(Player player) throws IOException {
        // 确保恢复后的背包已进入玩家存档，再移除恢复记录。
        player.saveData();
        Files.deleteIfExists(snapshotPath(player.getUniqueId()));
    }

    Journal open(Player player, World world) throws IOException {
        Path directory = root.resolve("blocks").resolve(world.getName());
        Files.createDirectories(directory);
        return new Journal(directory.resolve(player.getUniqueId() + ".bin"));
    }

    void cleanupBlocks() {
        blockedWorlds.clear();
        File[] worlds = root.resolve("blocks").toFile().listFiles(File::isDirectory);
        if (worlds == null) return;
        for (File directory : worlds) {
            File[] journals = directory.listFiles((parent, name) -> name.endsWith(".bin"));
            if (journals == null || journals.length == 0) continue;
            try {
                World world = Bukkit.getWorld(directory.getName());
                if (world == null && new File(Bukkit.getWorldContainer(), directory.getName() + "/level.dat").isFile())
                    world = Bukkit.createWorld(new WorldCreator(directory.getName()));
                if (world == null) throw new IOException("World unavailable");
                for (File journal : journals) {
                    Set<org.bukkit.Chunk> changed = new HashSet<>();
                    try (DataInputStream input = new DataInputStream(new FileInputStream(journal))) {
                        while (input.available() >= 12) {
                            Block block = world.getBlockAt(input.readInt(), input.readInt(), input.readInt());
                            if (block.getType() == Material.WOOL) block.setType(Material.AIR, false);
                            changed.add(block.getChunk());
                        }
                    }
                    BedWars.nms.savePracticeChunks(changed);
                    Files.delete(journal.toPath());
                }
            } catch (IOException | RuntimeException ex) {
                blockedWorlds.add(directory.getName());
                BedWars.plugin.getLogger().severe("Practice block recovery " + directory.getName() + ": " + ex.getMessage());
            }
        }
    }

    static final class Journal {
        private final Path path;
        private final FileChannel channel;
        private final ByteBuffer buffer = ByteBuffer.allocate(12);
        private final Set<org.bukkit.Chunk> changed = new HashSet<>();
        Journal(Path path) throws IOException {
            this.path = path;
            channel = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
        }
        void record(Block block) throws IOException {
            buffer.clear(); buffer.putInt(block.getX()).putInt(block.getY()).putInt(block.getZ()).flip();
            while (buffer.hasRemaining()) channel.write(buffer);
            changed.add(block.getChunk());
        }
        void clear() throws IOException {
            channel.close();
            BedWars.nms.savePracticeChunks(changed);
            Files.deleteIfExists(path);
        }
    }
}
