import os
import re

# Script directory
SCRIPT_DIR = "gms-server/scripts/event"

# Pattern to find AreaBoss files
AREA_BOSS_PATTERN = re.compile(r"^AreaBoss.*\.js$")

def patch_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. Remove map.setEventInstance(eim);
    new_content = re.sub(r'(\s*)map\.setEventInstance\(eim\);', r'\1// map.setEventInstance(eim); // Removed to prevent blocking teleports', content)

    # 2. Add eim.registerMonster(bossMob); after spawn
    # Look for: map.spawnMonsterOnGroundBelow(bossMob, ...);
    # Insert: eim.registerMonster(bossMob);

    # Using a regex that captures indentation
    def replacement(match):
        full_match = match.group(0)
        indent = match.group(1)
        return f"{full_match}\n{indent}eim.registerMonster(bossMob);"

    new_content = re.sub(r'(\s*)map\.spawnMonsterOnGroundBelow\(bossMob, [^)]+\);', replacement, new_content)

    if new_content != content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Patched: {filepath}")
    else:
        print(f"Skipped (no changes needed): {filepath}")

def main():
    for filename in os.listdir(SCRIPT_DIR):
        if AREA_BOSS_PATTERN.match(filename):
            patch_file(os.path.join(SCRIPT_DIR, filename))

if __name__ == "__main__":
    main()
