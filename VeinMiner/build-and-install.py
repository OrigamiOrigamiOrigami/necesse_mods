#!/usr/bin/env python3
# -*- coding: utf-8 -*-
from __future__ import print_function
import os, shutil, subprocess, sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent
GAME = Path(r"D:\SteamLibrary\steamapps\common\Necesse")
JAVA_HOME = Path(r"E:\Java") if Path(r"E:\Java\bin\javac.exe").exists() else Path(os.environ.get("JAVA_HOME", ""))
JAVAC, JAR = JAVA_HOME / "bin" / "javac.exe", JAVA_HOME / "bin" / "jar.exe"
OUT, JAR_OUT = ROOT / "build" / "mod", ROOT / "build" / "jar"
MODS = Path(os.environ["APPDATA"]) / "Necesse" / "mods"
JAR_NAME = "VeinMiner-1.3.3-1.1.jar"
MOD_INFO = """{
\tid = origami.veinminer,
\tname = Vein Miner,
\tversion = 1.1,
\tgameVersion = 1.3.3,
\tauthor = Origami,
\tdescription = Chain-mine ores\\, rocks\\, and trees. V toggle\\, K config.,
\tclientside = false
}
"""

def main():
    if not JAVAC.exists():
        sys.exit("javac not found")
    if OUT.exists():
        shutil.rmtree(OUT)
    OUT.mkdir(parents=True)
    JAR_OUT.mkdir(parents=True, exist_ok=True)
    cp = [str(GAME / "Necesse.jar")]
    lib = GAME / "lib"
    if lib.is_dir():
        cp.extend(str(p) for p in lib.glob("*.jar"))
    java_files = sorted((ROOT / "src" / "main" / "java").rglob("*.java"))
    cmd = [str(JAVAC), "-encoding", "UTF-8", "-source", "8", "-target", "8",
           "-cp", ";".join(cp), "-d", str(OUT)] + [str(f) for f in java_files]
    print("Compiling %d files..." % len(java_files))
    subprocess.check_call(cmd)
    (OUT / "mod.info").write_text(MOD_INFO, encoding="utf-8")
    res_src = ROOT / "src" / "main" / "resources"
    res = res_src / "locale"
    if res.is_dir():
        dst = OUT / "resources" / "locale"
        dst.mkdir(parents=True, exist_ok=True)
        for f in res.iterdir():
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
    dest = MODS / JAR_NAME
    shutil.copy2(jar_path, dest)
    print("Installed:", dest)

if __name__ == "__main__":
    main()
