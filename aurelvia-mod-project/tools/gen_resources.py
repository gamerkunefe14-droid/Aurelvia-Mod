#!/usr/bin/env python3
"""Aurelvia Makineleri icin tum kaynak (resource) dosyalarini uretir.

Calistirma:  python3 tools/gen_resources.py
"""
import json
import os
import struct
import zlib

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")
ASSETS = os.path.join(RES, "assets", "aurelvia_machines")
DATA = os.path.join(RES, "data", "aurelvia_machines")
NS = "aurelvia_machines"


# ------------------------------------------------------------------ PNG yazici
def write_png(path, pixels, width, height):
    raw = bytearray()
    for row in pixels:
        raw.append(0)  # filter: none
        for (r, g, b, a) in row:
            raw += bytes((r, g, b, a))

    def chunk(tag, data):
        return (struct.pack(">I", len(data)) + tag + data
                + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF))

    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(bytes(raw), 9))
    png += chunk(b"IEND", b"")

    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as fh:
        fh.write(png)


def canvas(w, h, color=(0, 0, 0, 0)):
    return [[color for _ in range(w)] for _ in range(h)]


def rect(px, x0, y0, x1, y1, color):
    for y in range(y0, y1):
        for x in range(x0, x1):
            px[y][x] = color


def noise(px, seed, colors, x0=0, y0=0, x1=16, y1=16):
    """Deterministik 'doku' noktalamasi."""
    s = seed
    for y in range(y0, y1):
        for x in range(x0, x1):
            s = (s * 1103515245 + 12345) & 0x7FFFFFFF
            if (s >> 16) % 7 == 0:
                px[y][x] = colors[(s >> 8) % len(colors)]


# ------------------------------------------------------------------ dokular
def tex_computer_front():
    px = canvas(16, 16, (58, 62, 72, 255))
    noise(px, 7, [(50, 54, 64, 255), (66, 70, 80, 255)])
    rect(px, 0, 0, 16, 2, (40, 43, 51, 255))
    rect(px, 0, 14, 16, 16, (34, 37, 44, 255))
    # ekran
    rect(px, 2, 3, 14, 12, (12, 20, 34, 255))
    rect(px, 3, 4, 13, 11, (18, 90, 120, 255))
    for i, y in enumerate((5, 7, 9)):
        w = 8 if i % 2 == 0 else 5
        rect(px, 4, y, 4 + w, y + 1, (120, 235, 255, 255))
    # durum isiklari
    px[13][3] = (255, 210, 0, 255)
    px[13][5] = (60, 220, 90, 255)
    rect(px, 8, 13, 13, 14, (24, 26, 32, 255))
    return px


def tex_computer_side():
    px = canvas(16, 16, (52, 56, 66, 255))
    noise(px, 21, [(45, 48, 58, 255), (60, 64, 74, 255)])
    rect(px, 0, 0, 16, 2, (40, 43, 51, 255))
    rect(px, 0, 14, 16, 16, (34, 37, 44, 255))
    for y in (4, 6, 8, 10):
        rect(px, 3, y, 13, y + 1, (33, 36, 43, 255))
    return px


def tex_computer_top():
    px = canvas(16, 16, (62, 66, 76, 255))
    noise(px, 33, [(54, 58, 68, 255), (70, 74, 84, 255)])
    rect(px, 0, 0, 16, 1, (44, 47, 56, 255))
    rect(px, 0, 15, 16, 16, (44, 47, 56, 255))
    rect(px, 0, 0, 1, 16, (44, 47, 56, 255))
    rect(px, 15, 0, 16, 16, (44, 47, 56, 255))
    rect(px, 5, 5, 11, 11, (48, 52, 62, 255))
    px[7][7] = (255, 210, 0, 255)
    return px


def tex_conveyor_top():
    """Bant yukari (kuzeye) dogru akar; oklar kuzeyi gosterir."""
    px = canvas(16, 16, (46, 48, 54, 255))
    noise(px, 55, [(40, 42, 48, 255), (54, 56, 62, 255)])
    # yan raylar
    rect(px, 0, 0, 2, 16, (92, 96, 104, 255))
    rect(px, 14, 0, 16, 16, (92, 96, 104, 255))
    # bant cizgileri
    for y in (1, 5, 9, 13):
        rect(px, 2, y, 14, y + 1, (32, 34, 39, 255))
    # sari yon oklari (kuzeye dogru)
    gold = (255, 210, 0, 255)
    for cy in (3, 11):
        rect(px, 7, cy - 2, 9, cy + 2, gold)
        px[cy - 3][7] = gold
        px[cy - 3][8] = gold
        px[cy - 2][6] = gold
        px[cy - 2][9] = gold
    return px


def tex_conveyor_side():
    px = canvas(16, 16, (74, 78, 86, 255))
    noise(px, 71, [(66, 70, 78, 255), (84, 88, 96, 255)])
    rect(px, 0, 0, 16, 3, (48, 50, 56, 255))
    rect(px, 0, 13, 16, 16, (40, 42, 48, 255))
    for x in (2, 6, 10, 14):
        rect(px, x, 5, x + 2, 11, (52, 55, 62, 255))
    return px


def tex_id_machine_front():
    px = canvas(16, 16, (104, 108, 118, 255))
    noise(px, 91, [(96, 100, 110, 255), (114, 118, 128, 255)])
    rect(px, 0, 0, 16, 1, (70, 74, 82, 255))
    rect(px, 0, 15, 16, 16, (60, 63, 70, 255))
    # sari bant
    rect(px, 0, 2, 16, 5, (255, 210, 0, 255))
    rect(px, 0, 2, 16, 3, (226, 178, 0, 255))
    # kontrol paneli
    rect(px, 2, 7, 9, 13, (38, 41, 48, 255))
    rect(px, 3, 8, 8, 12, (30, 120, 150, 255))
    px[9][11] = (60, 220, 90, 255)
    px[10][11] = (255, 70, 60, 255)
    # kart yuvasi
    rect(px, 10, 8, 14, 12, (30, 32, 38, 255))
    rect(px, 11, 9, 13, 11, (255, 255, 255, 255))
    return px


def tex_id_machine_side():
    px = canvas(16, 16, (96, 100, 110, 255))
    noise(px, 103, [(88, 92, 102, 255), (106, 110, 120, 255)])
    rect(px, 0, 0, 16, 1, (70, 74, 82, 255))
    rect(px, 0, 15, 16, 16, (60, 63, 70, 255))
    for x in (1, 4, 7, 10, 13):
        rect(px, x, 3, x + 1, 13, (80, 84, 92, 255))
    return px


def tex_id_machine_top():
    px = canvas(16, 16, (110, 114, 124, 255))
    noise(px, 117, [(100, 104, 114, 255), (120, 124, 134, 255)])
    rect(px, 0, 0, 16, 1, (76, 80, 88, 255))
    rect(px, 0, 15, 16, 16, (76, 80, 88, 255))
    rect(px, 0, 0, 1, 16, (76, 80, 88, 255))
    rect(px, 15, 0, 16, 16, (76, 80, 88, 255))
    rect(px, 4, 4, 12, 12, (92, 96, 106, 255))
    rect(px, 6, 6, 10, 10, (255, 210, 0, 255))
    return px


def tex_id_card():
    px = canvas(16, 16, (0, 0, 0, 0))
    # kart govdesi
    rect(px, 1, 3, 15, 13, (238, 238, 244, 255))
    rect(px, 1, 3, 15, 5, (255, 210, 0, 255))
    rect(px, 1, 11, 15, 13, (255, 210, 0, 255))
    # fotograf alani
    rect(px, 2, 6, 6, 11, (150, 158, 172, 255))
    rect(px, 3, 7, 5, 9, (214, 176, 140, 255))
    rect(px, 3, 9, 5, 11, (60, 110, 190, 255))
    # yazi satirlari
    rect(px, 7, 6, 14, 7, (90, 96, 108, 255))
    rect(px, 7, 8, 13, 9, (140, 146, 158, 255))
    rect(px, 7, 10, 12, 11, (140, 146, 158, 255))
    return px


def tex_icon():
    size = 128
    px = canvas(size, size, (20, 20, 34, 255))
    for y in range(size):
        for x in range(size):
            if (x // 16 + y // 16) % 2 == 0:
                px[y][x] = (26, 26, 44, 255)
    rect(px, 4, 4, 124, 124, (255, 210, 0, 255))
    rect(px, 10, 10, 118, 118, (24, 24, 42, 255))
    rect(px, 24, 24, 104, 104, (238, 238, 244, 255))
    rect(px, 24, 24, 104, 42, (255, 210, 0, 255))
    rect(px, 24, 88, 104, 104, (255, 210, 0, 255))
    rect(px, 34, 50, 60, 82, (150, 158, 172, 255))
    rect(px, 40, 56, 54, 68, (214, 176, 140, 255))
    rect(px, 40, 68, 54, 82, (60, 110, 190, 255))
    for i, y in enumerate((52, 62, 72)):
        rect(px, 66, y, 98 - i * 4, y + 6, (90, 96, 108, 255))
    return px


# ------------------------------------------------------------------ JSON
def dump(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as fh:
        json.dump(obj, fh, indent=2, ensure_ascii=False)
        fh.write("\n")


def blockstate_horizontal_with_attached(model):
    variants = {}
    for facing, y in (("north", 0), ("south", 180), ("east", 90), ("west", 270)):
        for attached in ("true", "false"):
            variants["attached=%s,facing=%s" % (attached, facing)] = {
                "model": "%s:block/%s" % (NS, model), "y": y}
    return {"variants": variants}


def blockstate_horizontal(model):
    return {"variants": {
        "facing=%s" % f: {"model": "%s:block/%s" % (NS, model), "y": y}
        for f, y in (("north", 0), ("south", 180), ("east", 90), ("west", 270))
    }}


def main():
    # ---------------- dokular
    textures = {
        "block/computer_front": tex_computer_front(),
        "block/computer_side": tex_computer_side(),
        "block/computer_top": tex_computer_top(),
        "block/conveyor_top": tex_conveyor_top(),
        "block/conveyor_side": tex_conveyor_side(),
        "block/id_machine_front": tex_id_machine_front(),
        "block/id_machine_side": tex_id_machine_side(),
        "block/id_machine_top": tex_id_machine_top(),
        "item/id_card": tex_id_card(),
    }
    for name, pixels in textures.items():
        write_png(os.path.join(ASSETS, "textures", name + ".png"), pixels, 16, 16)
    write_png(os.path.join(ASSETS, "icon.png"), tex_icon(), 128, 128)

    # eski, tek yuzlu dokular artik kullanilmiyor
    for old in ("block/computer.png", "block/conveyor.png", "block/id_machine.png"):
        p = os.path.join(ASSETS, "textures", old)
        if os.path.exists(p):
            os.remove(p)

    # ---------------- blockstates
    dump(os.path.join(ASSETS, "blockstates", "computer.json"),
         blockstate_horizontal_with_attached("computer"))
    dump(os.path.join(ASSETS, "blockstates", "conveyor.json"),
         blockstate_horizontal_with_attached("conveyor"))
    dump(os.path.join(ASSETS, "blockstates", "id_machine.json"),
         blockstate_horizontal("id_machine"))

    # ---------------- blok modelleri
    dump(os.path.join(ASSETS, "models", "block", "computer.json"), {
        "parent": "minecraft:block/orientable",
        "textures": {
            "top": "%s:block/computer_top" % NS,
            "front": "%s:block/computer_front" % NS,
            "side": "%s:block/computer_side" % NS,
        }})
    dump(os.path.join(ASSETS, "models", "block", "id_machine.json"), {
        "parent": "minecraft:block/orientable",
        "textures": {
            "top": "%s:block/id_machine_top" % NS,
            "front": "%s:block/id_machine_front" % NS,
            "side": "%s:block/id_machine_side" % NS,
        }})
    dump(os.path.join(ASSETS, "models", "block", "conveyor.json"), {
        "parent": "minecraft:block/cube_bottom_top",
        "textures": {
            "top": "%s:block/conveyor_top" % NS,
            "side": "%s:block/conveyor_side" % NS,
            "bottom": "%s:block/conveyor_side" % NS,
        }})

    # ---------------- item modelleri
    dump(os.path.join(ASSETS, "models", "item", "id_card.json"), {
        "parent": "minecraft:item/generated",
        "textures": {"layer0": "%s:item/id_card" % NS}})
    for block in ("computer", "conveyor", "id_machine"):
        dump(os.path.join(ASSETS, "models", "item", block + ".json"),
             {"parent": "%s:block/%s" % (NS, block)})

    # 1.21.4+ item tanimlari (assets/<ns>/items/*.json)
    dump(os.path.join(ASSETS, "items", "id_card.json"), {
        "model": {"type": "minecraft:model", "model": "%s:item/id_card" % NS}})
    for block in ("computer", "conveyor", "id_machine"):
        dump(os.path.join(ASSETS, "items", block + ".json"), {
            "model": {"type": "minecraft:model", "model": "%s:block/%s" % (NS, block)}})

    # ---------------- crafting tarifleri
    dump(os.path.join(DATA, "recipe", "computer.json"), {
        "type": "minecraft:crafting_shaped",
        "category": "misc",
        "pattern": ["I A", "ICI"],
        "key": {
            "I": {"item": "minecraft:iron_ingot"},
            "A": {"item": "minecraft:gold_ingot"},
            "C": {"item": "minecraft:copper_ingot"},
        },
        "result": {"id": "%s:computer" % NS, "count": 1},
    })
    dump(os.path.join(DATA, "recipe", "conveyor.json"), {
        "type": "minecraft:crafting_shaped",
        "category": "redstone",
        "pattern": ["L L", "S S", " B "],
        "key": {
            "L": {"item": "minecraft:leather"},
            "S": {"item": "minecraft:stick"},
            "B": {"item": "minecraft:iron_block"},
        },
        "result": {"id": "%s:conveyor" % NS, "count": 1},
    })
    dump(os.path.join(DATA, "recipe", "id_machine.json"), {
        "type": "minecraft:crafting_shaped",
        "category": "misc",
        "pattern": ["C K", "I I", "III"],
        "key": {
            "C": {"item": "%s:computer" % NS},
            "K": {"item": "%s:conveyor" % NS},
            "I": {"item": "minecraft:iron_block"},
        },
        "result": {"id": "%s:id_machine" % NS, "count": 1},
    })

    # ---------------- loot tablolari
    def simple_drop(item_id, block_id=None, attached_condition=False):
        entry = {"type": "minecraft:item", "name": item_id}
        conditions = [{"condition": "minecraft:survives_explosion"}]
        if attached_condition:
            conditions.insert(0, {
                "condition": "minecraft:block_state_property",
                "block": block_id,
                "properties": {"attached": "false"},
            })
        return {"type": "minecraft:block",
                "pools": [{"rolls": 1, "entries": [entry], "conditions": conditions}]}

    dump(os.path.join(DATA, "loot_table", "blocks", "computer.json"),
         simple_drop("%s:computer" % NS, "%s:computer" % NS, True))
    dump(os.path.join(DATA, "loot_table", "blocks", "conveyor.json"),
         simple_drop("%s:conveyor" % NS, "%s:conveyor" % NS, True))
    dump(os.path.join(DATA, "loot_table", "blocks", "id_machine.json"),
         simple_drop("%s:id_machine" % NS))

    # ---------------- kazma ile kirilabilir
    dump(os.path.join(RES, "data", "minecraft", "tag", "block", "mineable", "pickaxe.json"), {
        "replace": False,
        "values": [
            "%s:computer" % NS,
            "%s:conveyor" % NS,
            "%s:id_machine" % NS,
        ]})

    print("Kaynaklar uretildi ->", RES)


if __name__ == "__main__":
    main()
