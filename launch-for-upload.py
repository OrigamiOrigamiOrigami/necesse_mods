# -*- coding: utf-8 -*-
"""
Launch Necesse with one mod via -mod so the Workshop Upload button appears.

Usage (Python, game must be closed):
  python launch-for-upload.py HelpfulNPCs
  python launch-for-upload.py ItemBrowser
  python launch-for-upload.py VeinMiner
  python launch-for-upload.py AutoTrash
  python launch-for-upload.py SettlerAffix

Then: Mods menu -> select that mod -> Upload.
First upload is Hidden; set Public on the Steam Workshop page when ready.
"""
from __future__ import print_function

import os
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent
GAME = Path(r"D:\SteamLibrary\steamapps\common\Necesse")
JAVA = Path(r"E:\Java\bin\java.exe")
if not JAVA.exists():
    JAVA = Path(os.environ.get("JAVA_HOME", "")) / "bin" / "java.exe"

MODS = {
    "HelpfulNPCs": "HelpfulNPCs",
    "ItemBrowser": "ItemBrowser",
    "VeinMiner": "VeinMiner",
    "AutoTrash": "AutoTrash",
    "SettlerAffix": "SettlerAffix",
}


def main():
    if len(sys.argv) < 2 or sys.argv[1] not in MODS:
        print("Usage: python launch-for-upload.py <%s>" % "|".join(MODS))
        sys.exit(1)

    key = sys.argv[1]
    mod_dir = ROOT / MODS[key]
    build_script = mod_dir / "build-and-install.py"
    jar_dir = mod_dir / "build" / "jar"

    print("Building", key, "...")
    subprocess.check_call([sys.executable, str(build_script)])

    preview = mod_dir / "src" / "main" / "resources" / "preview.png"
    if not preview.is_file():
        sys.exit("Missing preview.png: %s" % preview)

    # Verify preview is inside the built jar.
    # Dev mod folder may contain only preview.png plus exactly one jar.
    jars = sorted(jar_dir.glob("*.jar"), key=lambda p: p.stat().st_mtime, reverse=True)
    if not jars:
        sys.exit("No jar in %s" % jar_dir)
    keep = jars[0]
    for extra in jars[1:]:
        print("Removing extra jar (dev folder allows only one):", extra.name)
        extra.unlink()
    for extra in jar_dir.iterdir():
        if extra.name == "preview.png" or extra == keep:
            continue
        if extra.is_file():
            print("Removing extra file from dev folder:", extra.name)
            extra.unlink()
    import zipfile
    with zipfile.ZipFile(keep) as z:
        names = z.namelist()
        if "resources/preview.png" not in names:
            sys.exit("Jar missing resources/preview.png: %s" % keep)
        print("preview OK in", keep.name)

    # steam_appid.txt required next to working dir for StartSteamClient
    appid = ROOT / "steam_appid.txt"
    appid.write_text("1169040\n", encoding="ascii")
    game_appid = GAME / "steam_appid.txt"
    if not game_appid.exists():
        shutil.copy2(appid, game_appid)

    if not JAVA.exists():
        sys.exit("java not found: %s" % JAVA)

    # ExampleMod uses: -dev -mod "build/jar"
    # Heap capped to reduce OOM risk vs default huge heaps.
    cmd = [
        str(JAVA),
        "-Xms512m",
        "-Xmx3G",
        "-XX:+UnlockExperimentalVMOptions",
        "-XX:+UseG1GC",
        "-XX:G1NewSizePercent=20",
        "-XX:G1ReservePercent=20",
        "-XX:MaxGCPauseMillis=50",
        "-XX:G1HeapRegionSize=32M",
        "-cp", str(GAME / "Necesse.jar"),
        "StartSteamClient",
        "-dev",
        "-mod",
        str(jar_dir),
    ]
    print("Launching for Workshop upload:", key)
    print("After load: Mods ->", key, "-> Upload")
    subprocess.check_call(cmd, cwd=str(GAME))


if __name__ == "__main__":
    main()
