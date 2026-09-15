# Auto Trash

Necesse mod: move blacklisted items to the trash slot.

| Field | Value |
|-------|--------|
| Mod ID | `origami.autotrash` |
| Version | 1.0 |
| Game | 1.3.3 |
| Author | Origami |
| Clientside | false |

## Default controls

| Key | Action |
|-----|--------|
| `N` | Toggle auto trash |
| `O` | Open blacklist UI |
| `Delete` | Add hovered inventory item |

Remappable in Settings. Default: **ON**.

## Build / install

```bash
python build-and-install.py
```

Blacklist: `%APPDATA%\Necesse\cfg\mods\origami.autotrash.cfg`.

## Steam Workshop

```bash
python launch-for-upload.py AutoTrash
```

Workshop text: `../steam_workshop/AutoTrash/`.

## Notes

- Items go to the **trash slot** (recoverable), not hard-deleted.
- After game updates: retest pickup → trash + hotkeys.
