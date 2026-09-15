# Origami Necesse mods

Five Necesse **1.3.3** mods in one repo (Steam Workshop distribution).

| Folder | Mod ID |
|--------|--------|
| HelpfulNPCs | origami.helpfulnpcs |
| ItemBrowser | origami.itembrowser |
| VeinMiner | origami.veinminer |
| AutoTrash | origami.autotrash |
| SettlerAffix | origami.settleraffix |

## Build

```bash
cd HelpfulNPCs
python build-and-install.py
```

Jar goes to `%APPDATA%\Necesse\mods\`. Edit `GAME` path inside each `build-and-install.py` if Necesse is not under `D:\SteamLibrary\steamapps\common\Necesse`.

## Workshop upload

```bash
# from this repo root; game must be closed
python launch-for-upload.py HelpfulNPCs
```

Mods → enable → Upload. Paste copy from `steam_workshop/<Mod>/`. First upload is Hidden → set Public on Steam.

## After a Necesse update

1. Bump `gameVersion` / jar name / mod `version` in each `build-and-install.py`
2. Rebuild all five; fix patches if the game API moved
3. Smoke-test (see each folder’s README)
4. Re-upload as update to the existing Workshop items

## Previews

Edit `*/src/main/resources/preview.svg`, then:

```bash
python _make_previews.py
```

Rebuild jars so `preview.png` is inside.
