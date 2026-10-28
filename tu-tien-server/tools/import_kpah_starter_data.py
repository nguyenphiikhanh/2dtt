"""Import KPAH map 105 and character-part metadata into the game asset bundle."""

from __future__ import annotations

import json
import shutil
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
SQL_DUMP = ROOT / "kpah.sql"
KPAH_DATA = ROOT / "KPAH" / "data"
ASSET_ROOT = ROOT / "tu-tien-server" / "src" / "main" / "resources" / "static" / "assets" / "kpah"


def unquote_sql(value: str) -> str:
    value = value.strip()
    if value.startswith("'") and value.endswith("'"):
        value = value[1:-1]
        value = value.replace("''", "'").replace("\\'", "'")
    return value


def parse_sql_tuple(line: str) -> list[str]:
    start = line.find("(")
    end = line.rfind(")")
    if start < 0 or end <= start:
        raise ValueError("Expected a SQL value tuple")

    values: list[str] = []
    token: list[str] = []
    quoted = False
    escaped = False
    nested = 0
    index = start + 1
    while index < end:
        char = line[index]
        if quoted:
            token.append(char)
            if escaped:
                escaped = False
            elif char == "\\":
                escaped = True
            elif char == "'":
                if index + 1 < end and line[index + 1] == "'":
                    token.append("'")
                    index += 1
                else:
                    quoted = False
        elif char == "'":
            quoted = True
            token.append(char)
        elif char in "[{(":
            nested += 1
            token.append(char)
        elif char in "]})":
            nested -= 1
            token.append(char)
        elif char == "," and nested == 0:
            values.append(unquote_sql("".join(token)))
            token = []
        else:
            token.append(char)
        index += 1
    values.append(unquote_sql("".join(token)))
    return values


def json_field(value: str):
    return json.loads(value)


def find_insert_rows(table: str):
    in_insert = False
    with SQL_DUMP.open(encoding="utf-8") as source:
        for line in source:
            if line.startswith("INSERT INTO `"):
                in_insert = line.startswith(f"INSERT INTO `{table}`")
                continue
            if not in_insert:
                continue
            stripped = line.strip()
            if stripped.startswith("("):
                yield stripped
            if stripped.endswith(";"):
                in_insert = False


def load_starter_map() -> dict:
    row = next(row for row in find_insert_rows("maps") if row.startswith("(105,"))
    values = parse_sql_tuple(row)
    columns = [
        "id", "name", "maxZone", "isOfflineMap", "idXaPhu", "idHoaTieu", "childMap",
        "wayPoints", "mobs", "npcsActor", "npcs", "npcServer", "data", "tileTop", "tree",
        "locationWayPoints",
    ]
    record = dict(zip(columns, values, strict=True))
    raw_map = json_field(record["data"])
    width, height = raw_map[0]
    tiles = raw_map[1]

    # KPAH chooses t_thanh for map IDs > 200, t for IDs < 110, and t_hang otherwise.
    type_of_tile = list((KPAH_DATA / "map" / "typeOfTile" / "t").read_bytes())
    type_of_tile = [value if value < 128 else value - 256 for value in type_of_tile]
    collision = []
    for tile_id in tiles:
        if tile_id == 254:
            tile_id = 255
        collision.append(0 if tile_id in (-1, 255) else type_of_tile[tile_id])

    return {
        "id": 105,
        "name": record["name"].strip() or "KPAH Map 105",
        "tileSize": 16,
        "width": width,
        "height": height,
        "spawn": {"x": 184, "y": 280},
        "tiles": tiles,
        "collision": collision,
        "tileTop": json_field(record["tileTop"]),
        "trees": json_field(record["tree"]),
        "npcs": json_field(record["npcs"]),
        "npcActors": json_field(record["npcsActor"]),
        "npcServers": json_field(record["npcServer"]),
        "mobs": json_field(record["mobs"]),
        "waypoints": json_field(record["wayPoints"]),
        "locationWaypoints": json_field(record["locationWayPoints"]),
        "isOfflineMap": record["isOfflineMap"] == "1",
    }


def load_character_parts() -> list[dict]:
    parts = []
    for row in find_insert_rows("char_part"):
        values = parse_sql_tuple(row)
        if len(values) != 15:
            continue
        parts.append({
            "id": int(values[0]),
            "partId": int(values[1]),
            "type": int(values[2]),
            "avxf": int(values[3]),
            "avyf": int(values[4]),
            "avx0": int(values[5]),
            "avy0": int(values[6]),
            "avw0": int(values[7]),
            "avh0": int(values[8]),
            "x": json_field(values[9]),
            "y": json_field(values[10]),
            "w": json_field(values[11]),
            "h": json_field(values[12]),
            "dx": json_field(values[13]),
            "dy": json_field(values[14]),
        })
    return parts


def write_json(path: Path, value) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, separators=(",", ":")), encoding="utf-8")


def main() -> None:
    map_data = load_starter_map()
    parts = load_character_parts()
    write_json(ASSET_ROOT / "maps" / "105.json", map_data)
    write_json(ASSET_ROOT / "character-parts.json", {"source": "KPAH char_part", "parts": parts})
    shutil.copytree(KPAH_DATA / "image" / "cloth", ASSET_ROOT / "character" / "cloth", dirs_exist_ok=True)
    shutil.copytree(KPAH_DATA / "image" / "tree", ASSET_ROOT / "maps" / "trees", dirs_exist_ok=True)
    cloth_count = sum(1 for path in (KPAH_DATA / "image" / "cloth").rglob("*.png"))
    tree_count = sum(1 for path in (KPAH_DATA / "image" / "tree").glob("*.png"))
    print(f"Imported map {map_data['id']}: {map_data['width']}x{map_data['height']} tiles, {len(map_data['collision'])} collision values")
    print(f"Imported {len(parts)} char_part records, {cloth_count} KPAH cloth sprites, {tree_count} tree images, and {len(map_data['trees'])} map tree placements")


if __name__ == "__main__":
    main()
