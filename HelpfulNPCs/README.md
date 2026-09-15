# Helpful NPCs

Necesse mod: vendor settlers (trinket, potion, vinyl, exotic merchant).

| Field | Value |
|-------|--------|
| Mod ID | `origami.helpfulnpcs` |
| Version | 1.0 |
| Game | 1.3.3 |
| Author | Origami |
| Clientside | false |

## Build / install

```bash
python build-and-install.py
```

Installs to `%APPDATA%\Necesse\mods\HelpfulNPCs-1.3.3-1.0.jar`.

## Steam Workshop

```bash
# from repo root
python launch-for-upload.py HelpfulNPCs
```

Then: Mods → enable → Upload. Workshop text: `../steam_workshop/HelpfulNPCs/`.

## Notes

- After a Necesse update: bump `gameVersion` / jar name in `build-and-install.py`, rebuild, smoke-test visitors/shops, re-upload.
- `mod.info` name must stay ASCII.
- Keep descriptions generic; do not put one-off bugfix notes in tips/`mod.info`.
