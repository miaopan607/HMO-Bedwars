#!/usr/bin/env python3
"""生成 Minecraft 1.8.8 Anvil 虚空训练世界，仅使用 Python 标准库。"""

import argparse
from collections import defaultdict, deque
import gzip
from math import atan2, degrees
from pathlib import Path
import struct
import time
import zipfile
import zlib


ROOMS = [("fireball-a", "fireball", 0), ("fireball-b", "fireball", 192),
         ("bridge-a", "bridge", 512), ("bridge-b", "bridge", 896),
         ("pearl-a", "pearl", 1216), ("pearl-b", "pearl", 1408)]
HALF_WIDTH = {"fireball": 80, "bridge": 160, "pearl": 80}
BRIDGE_ROUTES = [
    ("近·平·直", -140, -144, -136, 29, 37, 79),
    ("远·平·直", -100, -104, -96, 53, 61, 79),
    ("近·高·直", -60, -64, -56, 29, 37, 83),
    ("远·高·直", -20, -24, -16, 53, 61, 83),
    ("近·平·斜", 20, -7, 1, 29, 37, 79),
    ("远·平·斜", 60, 9, 17, 53, 61, 79),
    ("近·高·斜", 100, 73, 81, 29, 37, 83),
    ("远·高·斜", 140, 89, 97, 53, 61, 83),
]
ISLANDS = {
    "fireball": [(-5, 5, 25, 35, 79), (13, 23, 42, 52, 81),
                 (-20, -10, 42, 52, 77), (-5, 5, 59, 69, 79),
                 (32, 42, 59, 69, 79), (-34, -24, 59, 69, 79),
                 (13, 23, 76, 86, 81), (-20, -10, 76, 86, 77),
                 (-5, 5, 93, 103, 79), (13, 23, 110, 120, 81),
                 (-20, -10, 110, 120, 77), (-5, 5, 127, 137, 79)],
    "pearl": [(-12, 12, 27, 43, 79), (-40, -24, 29, 43, 71),
              (25, 43, 31, 47, 91), (-25, -11, 50, 62, 83),
              (5, 21, 53, 67, 75), (43, 59, 55, 69, 95),
              (-57, -41, 61, 75, 77), (-9, 9, 73, 89, 87),
              (23, 37, 78, 90, 81), (-39, -25, 88, 100, 93),
              (-65, -53, 95, 107, 73), (49, 65, 97, 111, 85),
              (-13, 5, 107, 123, 79), (17, 31, 117, 131, 89)],
}


def chunk_bounds():
    return ((min(offset - HALF_WIDTH[mode] for _, mode, offset in ROOMS) - 32) // 16,
            (max(offset + HALF_WIDTH[mode] for _, mode, offset in ROOMS) + 32) // 16,
            (-24 - 32) // 16, (144 + 32) // 16)


def text(value):
    data = value.encode("utf-8")
    return struct.pack(">H", len(data)) + data


def payload(kind, value):
    if kind == 1:
        return struct.pack(">b", value)
    if kind == 3:
        return struct.pack(">i", value)
    if kind == 4:
        return struct.pack(">q", value)
    if kind == 7:
        return struct.pack(">i", len(value)) + value
    if kind == 8:
        return text(value)
    if kind == 9:
        element_kind, elements = value
        return bytes([element_kind]) + struct.pack(">i", len(elements)) + b"".join(
            payload(element_kind, element) for element in elements)
    if kind == 10:
        return b"".join(bytes([tag_kind]) + text(name) + payload(tag_kind, item)
                        for name, (tag_kind, item) in value.items()) + b"\0"
    if kind == 11:
        return struct.pack(">i", len(value)) + struct.pack(">" + "i" * len(value), *value)
    raise ValueError(kind)


def nbt(value):
    return b"\x0a\0\0" + payload(10, value)


def nibble(data, index, value):
    shift = (index & 1) * 4
    data[index // 2] = (data[index // 2] & ~(15 << shift)) | (value << shift)


class Lighting:
    """在统一体积内传播光照，不以区块或区块段截断。"""

    def __init__(self, blocks, bounds):
        min_cx, max_cx, min_cz, max_cz = bounds
        self.min_x, self.min_z = min_cx * 16, min_cz * 16
        self.width, self.depth = (max_cx - min_cx + 1) * 16, (max_cz - min_cz + 1) * 16
        self.height = ((max(position[1] for position in blocks) + 31) // 16) * 16
        self.plane = self.width * self.depth
        size = self.plane * self.height
        self.ids = bytearray(size)
        self.sky = bytearray([15]) * size
        self.block = bytearray(size)
        # Spigot v1_8_R3 Block.p() 的实际遮光值，发光值取 Block.r()。
        self.opacity = {0: 0, 5: 15, 17: 15, 85: 0, 95: 0,
                        126: 15, 155: 15, 159: 15, 169: 15}
        columns = defaultdict(list)
        lamps = deque()
        for (x, y, z), (block, _) in blocks.items():
            position = self.index(x, y, z)
            self.ids[position] = block
            if self.opacity[block]:
                columns[x, z].append(y)
            if block == 169:
                self.block[position] = 15
                lamps.append(position)
        shadow = deque()
        for (x, z), heights in columns.items():
            highest = max(heights)
            for y in range(highest + 1):
                self.sky[self.index(x, y, z)] = 0
        # 每列直射天空光已确定，只需从阴影侧面向内部传播。
        for (x, z), heights in columns.items():
            for y in range(max(heights) + 1):
                position = self.index(x, y, z)
                if self.opacity[self.ids[position]]:
                    continue
                value = max(self.sky[position - 1], self.sky[position + 1],
                            self.sky[position - self.width], self.sky[position + self.width]) - 1
                if value > 0:
                    self.sky[position] = value
                    shadow.append(position)
        self.spread(self.sky, shadow)
        self.spread(self.block, lamps)

    def index(self, x, y, z):
        return y * self.plane + (z - self.min_z) * self.width + x - self.min_x

    def spread(self, light, queue):
        while queue:
            position = queue.popleft()
            value = light[position] - 1
            if value <= 0:
                continue
            y, column = divmod(position, self.plane)
            z, x = divmod(column, self.width)
            for allowed, step in ((x > 0, -1), (x + 1 < self.width, 1),
                                  (z > 0, -self.width), (z + 1 < self.depth, self.width),
                                  (y > 0, -self.plane), (y + 1 < self.height, self.plane)):
                if allowed:
                    neighbor = position + step
                    candidate = light[position] - max(1, self.opacity[self.ids[neighbor]])
                    if candidate > light[neighbor]:
                        light[neighbor] = candidate
                        queue.append(neighbor)


class Harbor:
    def __init__(self):
        self.blocks = {}

    def put(self, x, y, z, block, meta=0):
        self.blocks[x, y, z] = (block, meta)

    def fill(self, x1, x2, y1, y2, z1, z2, block, meta=0):
        for x in range(x1, x2 + 1):
            for y in range(y1, y2 + 1):
                for z in range(z1, z2 + 1):
                    self.put(x, y, z, block, meta)


    def platform(self, x1, x2, z1, z2, y, color, target=False):
        self.fill(x1, x2, y - 2, y - 2, z1, z2, 5, 5)
        self.fill(x1, x2, y - 1, y - 1, z1, z2, 155)
        self.fill(x1, x2, y, y, z1, z2, 155)
        # 彩色外沿与木质内圈，训练表面不使用羊毛。
        for x in range(x1, x2 + 1):
            for z in range(z1, z2 + 1):
                edge = min(x - x1, x2 - x, z - z1, z2 - z)
                if edge == 0:
                    self.put(x, y, z, 159, color)
                elif edge == 1:
                    self.put(x, y, z, 5, 5)
                elif target and edge % 3 == 2:
                    self.put(x, y, z, 159, color)
        # 下方逐层收窄，形成浮岛船底。
        for depth in range(3, 7):
            inset = (depth - 1) * 2
            if x1 + inset <= x2 - inset and z1 + inset <= z2 - inset:
                self.fill(x1 + inset, x2 - inset, y - depth, y - depth,
                          z1 + inset, z2 - inset, 5, 5)
        for x in (x1 + 2, x2 - 2):
            for z in (z1 + 2, z2 - 2):
                self.put(x, y, z, 169)

    def beacon(self, x, z, y, color):
        self.fill(x, x, y + 1, y + 2, z, z, 17, 1)
        self.put(x, y + 3, z, 169)
        self.put(x, y + 4, z, 95, color)
        self.put(x, y + 5, z, 126, 5)

    def room(self, mode, offset):
        color = {"fireball": 1, "bridge": 9, "pearl": 10}[mode]
        half, front = (152, -8) if mode == "bridge" else (24, 12)
        self.platform(offset - half, offset + half, -16, front, 79, color)
        # 后栏避开出生点，前缘保持开放。
        self.fill(offset - half, offset + half, 80, 81, -16, -16, 85)
        for x in (offset - half, offset + half):
            self.fill(x, x, 80, 81, -15, -9 if mode == "bridge" else -4, 85)
            self.beacon(x, -15, 79, color)
        for x in (offset - 5, offset + 5):
            self.fill(x, x, 80, 85, -10, -10, 17, 1)
        self.fill(offset - 5, offset + 5, 85, 85, -10, -10, 5, 5)
        self.fill(offset - 3, offset + 3, 82, 83, -10, -10, 159, color)
        if mode == "bridge":
            self.bridge_room(offset)
        elif mode == "fireball":
            for z in (6, 9, 12):
                self.fill(offset - 8, offset + 8, 79, 79, z, z, 159, color)
            self.fireball_room(offset)
        else:
            self.fill(offset - 3, offset + 3, 79, 79, 10, 10, 159, color)
            self.pearl_room(offset)

    def bridge_room(self, offset):
        for _, center, x1, x2, z1, z2, y in BRIDGE_ROUTES:
            self.platform(offset + center - 6, offset + center + 6, -8, 12, 79, 9)
            for x in range(offset + center - 6, offset + center + 7):
                self.put(x, 79, 12, 155 if x % 2 == 0 else 159, 0 if x % 2 == 0 else 9)
            self.fill(offset + center, offset + center, 79, 79, 9, 11, 159, 9)
            self.platform(offset + x1, offset + x2, z1, z2, y, 9)

    def irregular_island(self, x1, x2, z1, z2, y, variant, color, protrusions):
        # 椭圆轮廓叠加错位边缘，各岛保持完整的中央落脚面。
        cx, cz = (x1 + x2) // 2, (z1 + z2) // 2
        rx, rz = (x2 - x1) // 2, (z2 - z1) // 2
        surface = set()
        for z in range(z1, z2 + 1):
            shift = ((z - z1) // 3 + variant) % 3 - 1
            for x in range(x1, x2 + 1):
                if (x - cx - shift) ** 2 * rz ** 2 + (z - cz) ** 2 * rx ** 2 <= rx ** 2 * rz ** 2:
                    surface.add((x, z))
        for x, z in surface:
            edge = any((x + dx, z + dz) not in surface for dx, dz in ((-1, 0), (1, 0), (0, -1), (0, 1)))
            self.put(x, y - 2, z, 5, 5)
            self.put(x, y - 1, z, 155)
            self.put(x, y, z, 159 if edge else 155, color if edge else 0)
        underside = surface
        for depth in range(3, 7):
            underside = {(x, z) for x, z in underside
                         if all((x + dx, z + dz) in underside for dx, dz in ((-1, 0), (1, 0), (0, -1), (0, 1)))}
            for x, z in underside:
                self.put(x, y - depth, z, 5, 5)
        for x in (cx - 2, cx + 2):
            self.put(x, y, cz, 169)
        # 低岛平坦，高岛只放不超过两格的小突起，不设高墙或灯塔。
        if protrusions and y > 75:
            height = 2 if y >= 89 else 1 + variant % 2
            self.fill(cx + 2, cx + 3, y + 1, y + height, cz + 2, cz + 3, 159, color)

    def fireball_room(self, offset):
        # 不规则小岛保留平坦落脚面，相邻支路高差两格。
        for index, (x1, x2, z1, z2, y) in enumerate(ISLANDS["fireball"]):
            self.irregular_island(offset + x1, offset + x2, z1, z2, y, index, 1, False)

    def pearl_room(self, offset):
        for index, (x1, x2, z1, z2, y) in enumerate(ISLANDS["pearl"]):
            self.irregular_island(offset + x1, offset + x2, z1, z2, y, index, 10, True)

    def write_world(self, directory, world_name):
        directory.mkdir()
        region_dir = directory / "region"
        region_dir.mkdir()
        columns = defaultdict(dict)
        for (x, y, z), block in self.blocks.items():
            columns[x // 16, z // 16][x, y, z] = block
        bounds = chunk_bounds()
        lighting = Lighting(self.blocks, bounds)
        min_cx, max_cx, min_cz, max_cz = bounds
        # 地图四周预生成虚空缓冲区，之外继续使用空气超平坦生成器。
        chunks = {}
        for cx in range(min_cx, max_cx + 1):
            for cz in range(min_cz, max_cz + 1):
                chunks[cx, cz] = self.chunk(cx, cz, columns.get((cx, cz), {}), lighting)
        regions = defaultdict(dict)
        for (cx, cz), chunk in chunks.items():
            regions[cx // 32, cz // 32][(cx % 32) + (cz % 32) * 32] = chunk
        for (rx, rz), entries in regions.items():
            locations = bytearray(4096)
            timestamps = bytearray(4096)
            body = bytearray()
            sector = 2
            for index, chunk in sorted(entries.items()):
                compressed = zlib.compress(chunk)
                record = struct.pack(">I", len(compressed) + 1) + b"\x02" + compressed
                count = (len(record) + 4095) // 4096
                struct.pack_into(">I", locations, index * 4, (sector << 8) | count)
                struct.pack_into(">I", timestamps, index * 4, int(time.time()))
                body.extend(record)
                body.extend(bytes(count * 4096 - len(record)))
                sector += count
            (region_dir / f"r.{rx}.{rz}.mca").write_bytes(locations + timestamps + body)
        data = {
            "version": (3, 19133), "LevelName": (8, world_name), "RandomSeed": (4, 0),
            "generatorName": (8, "flat"), "generatorVersion": (3, 0),
            "generatorOptions": (8, "3;minecraft:air;1;"),
            "GameType": (3, 0), "MapFeatures": (1, 0), "hardcore": (1, 0),
            "allowCommands": (1, 1), "initialized": (1, 1),
            "SpawnX": (3, 0), "SpawnY": (3, 80), "SpawnZ": (3, 0),
            "Time": (4, 6000), "DayTime": (4, 6000), "LastPlayed": (4, int(time.time() * 1000)),
            "SizeOnDisk": (4, 0), "Difficulty": (1, 1), "DifficultyLocked": (1, 0),
            "raining": (1, 0), "rainTime": (3, 2147483647),
            "thundering": (1, 0), "thunderTime": (3, 2147483647),
            "GameRules": (10, {key: (8, value) for key, value in {
                "doDaylightCycle": "false", "doMobSpawning": "false", "doFireTick": "false",
                "mobGriefing": "false", "keepInventory": "true", "randomTickSpeed": "0"
            }.items()}),
        }
        (directory / "level.dat").write_bytes(gzip.compress(nbt({"Data": (10, data)})))
        return len(chunks), len(regions)

    def chunk(self, cx, cz, blocks, lighting):
        sections = {sy: [bytearray(4096), bytearray(2048), bytearray(2048), bytearray(2048)]
                    for sy in range(lighting.height // 16)}
        heights = [0] * 256
        for (x, y, z), (block, meta) in blocks.items():
            sy = y // 16
            ids, metadata, _, _ = sections[sy]
            index = (y % 16) * 256 + (z % 16) * 16 + x % 16
            ids[index] = block
            nibble(metadata, index, meta)
            if lighting.opacity[block]:
                heights[(z % 16) * 16 + x % 16] = max(heights[(z % 16) * 16 + x % 16], y + 1)
        serialized = []
        for sy, (ids, metadata, block_light, sky_light) in sorted(sections.items()):
            for index in range(4096):
                lx, lz, y = index % 16, (index // 16) % 16, sy * 16 + index // 256
                position = lighting.index(cx * 16 + lx, y, cz * 16 + lz)
                nibble(sky_light, index, lighting.sky[position])
                nibble(block_light, index, lighting.block[position])
            serialized.append({"Y": (1, sy), "Blocks": (7, bytes(ids)), "Data": (7, bytes(metadata)),
                               "BlockLight": (7, bytes(block_light)), "SkyLight": (7, bytes(sky_light))})
        return nbt({"Level": (10, {
            "xPos": (3, cx), "zPos": (3, cz), "LastUpdate": (4, 0), "InhabitedTime": (4, 0),
            "TerrainPopulated": (1, 1), "LightPopulated": (1, 1),
            "V": (1, 1), "HeightMap": (11, heights), "Biomes": (7, bytes([1]) * 256),
            "Sections": (9, (10, serialized)), "Entities": (9, (10, [])),
            "TileEntities": (9, (10, [])),
            "TileTicks": (9, (10, [])),
        })})


def configuration(world_name):
    lines = ["points:"]
    for name, mode, offset in ROOMS:
        half = HALF_WIDTH[mode]
        spawn_z = -11.5 if mode == "bridge" else 0.5
        lines.extend([f"  {name}:", f"    mode: {mode}", f"    world: {world_name}",
                      f"    spawn: '{offset + 0.5},80,{spawn_z},0,0'",
                      f"    pos1: '{offset - half},48,-24,0,0'",
                      f"    pos2: '{offset + half},159,144,0,0'",
                      "    fail-y: -1", "    enabled: true"])
        if mode != "bridge":
            continue
        lines.append("    targets:")
        for index, (label, center, x1, x2, z1, z2, y) in enumerate(BRIDGE_ROUTES, 1):
            sx = offset + center + 0.5
            tx = (x1 + x2 + 1) / 2 + offset
            yaw = -degrees(atan2(tx - sx, (z1 + z2 + 1) / 2 - 10.5))
            lines.extend([f"      target-{index}:", f"        name: '{label}'",
                          f"        spawn: '{sx},80,10.5,{yaw:.3f},0'",
                          f"        pos1: '{x1 + offset},{y + 1},{z1},0,0'",
                          f"        pos2: '{x2 + offset},{y + 2},{z2},0,0'"])
    return "\n".join(lines) + "\n"


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", type=Path, default=Path("target/practice-shared"),
                        help="新建交付目录，默认 target/practice-shared；不覆盖现有目录")
    parser.add_argument("--world", default="practice_shared", help="世界目录名，默认 practice_shared")
    args = parser.parse_args()
    args.output.mkdir(parents=True, exist_ok=False)
    harbor = Harbor()
    for _, mode, offset in ROOMS:
        harbor.room(mode, offset)
    chunks, regions = harbor.write_world(args.output / args.world, args.world)
    (args.output / "practice.yml").write_text(configuration(args.world), encoding="utf-8")
    with zipfile.ZipFile(args.output / "practice-shared.zip", "w", zipfile.ZIP_DEFLATED) as archive:
        for path in sorted((args.output / args.world).rglob("*")):
            if path.is_file():
                archive.write(path, path.relative_to(args.output))
        archive.write(args.output / "practice.yml", "practice.yml")
    print(f"已生成 {len(ROOMS)} 个训练点，{len(harbor.blocks)} 个方块，{chunks} 个区块，{regions} 个区域文件")
    print(f"世界与配置：{args.output.resolve()}")
    print(f"导入压缩包：{(args.output / 'practice-shared.zip').resolve()}")


if __name__ == "__main__":
    main()
