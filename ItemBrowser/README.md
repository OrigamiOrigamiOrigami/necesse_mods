# Item Browser

Necesse mod: reverse-lookup item sources (~ / crafting guide U).

| Field | Value |
|-------|--------|
| Mod ID | `origami.itembrowser` |
| Version | 1.0 |
| Game | 1.3.3 |
| Author | Origami |
| Clientside | false |

## Default controls

| Key | Action |
|-----|--------|
| `~` | Open Item Browser |
| `U` | Crafting Guide: look up hovered item |

Remappable in Settings.

## Build / install

```bash
python build-and-install.py
```

## Steam Workshop

```bash
python launch-for-upload.py ItemBrowser
```

Workshop text: `../steam_workshop/ItemBrowser/`.

## Notes

- After a Necesse update: bump versions, rebuild, test `~` / `U` / recipes & shops index.
- Keep descriptions generic (no “doesn’t put items in inventory” style fix notes).
