#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Build and install Item Browser — no PowerShell."""
from __future__ import print_function

import os
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent
GAME = Path(r"D:\SteamLibrary\steamapps\common\Necesse")
JAVA_HOME = Path(r"E:\Java") if Path(r"E:\Java\bin\javac.exe").exists() else Path(os.environ.get("JAVA_HOME", ""))
JAVAC = JAVA_HOME / "bin" / "javac.exe"
JAR = JAVA_HOME / "bin" / "jar.exe"
OUT = ROOT / "build" / "mod"
JAR_OUT = ROOT / "build" / "jar"
MODS = Path(os.environ["APPDATA"]) / "Necesse" / "mods"
JAR_NAME = "ItemBrowser-1.3.3-1.0.jar"

# ModInfoFile rejects non-ASCII names ("illegal characters").
# Chinese UI text stays in locale; control group title uses this English name.
MOD_INFO = """{
\tid = origami.itembrowser,
\tname = Item Browser,
\tversion = 1.0,
\tgameVersion = 1.3.3,
\tauthor = Origami,
\tdescription = Item reverse lookup. ~ open\\, U crafting guide lookup.,
\tclientside = false
}
"""


def main():
    if not JAVAC.exists():
        sys.exit("javac not found: %s" % JAVAC)

    if OUT.exists():
        shutil.rmtree(OUT)
    OUT.mkdir(parents=True)
    JAR_OUT.mkdir(parents=True, exist_ok=True)

    cp = [str(GAME / "Necesse.jar")]
    lib = GAME / "lib"
    if lib.is_dir():
        cp.extend(str(p) for p in lib.glob("*.jar"))
    classpath = ";".join(cp)

    java_files = sorted((ROOT / "src" / "main" / "java").rglob("*.java"))
    if not java_files:
        sys.exit("No .java files")

    cmd = [
        str(JAVAC), "-encoding", "UTF-8", "-source", "8", "-target", "8",
        "-cp", classpath, "-d", str(OUT),
    ] + [str(f) for f in java_files]
    print("Compiling %d files..." % len(java_files))
    subprocess.check_call(cmd)

    (OUT / "mod.info").write_text(MOD_INFO, encoding="utf-8")

    res_src = ROOT / "src" / "main" / "resources"
    for sub in ("locale", "items"):
        src = res_src / sub
        dst = OUT / "resources" / sub
        if src.is_dir():
            dst.mkdir(parents=True, exist_ok=True)
            for f in src.iterdir():
                if f.is_file():
                    shutil.copy2(f, dst / f.name)
    preview = res_src / "preview.png"
    if preview.is_file():
        (OUT / "resources").mkdir(parents=True, exist_ok=True)
        shutil.copy2(preview, OUT / "resources" / "preview.png")

    jar_path = JAR_OUT / JAR_NAME
    if jar_path.exists():
        jar_path.unlink()
    subprocess.check_call([str(JAR), "cf", str(jar_path), "."], cwd=str(OUT))

    MODS.mkdir(parents=True, exist_ok=True)
    dest = MODS / JAR_NAME
    shutil.copy2(jar_path, dest)
    print("Installed:", dest)

    with __import__("zipfile").ZipFile(dest) as z:
        info = z.read("mod.info").decode("utf-8")
        assert "name = Item Browser" in info
        print("mod.info OK:", [l.strip() for l in info.splitlines() if "name" in l][0])


if __name__ == "__main__":
    main()
