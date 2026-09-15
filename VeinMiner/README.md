# Vein Miner

Necesse mod: chain-mine connected ore veins (vanilla-style cluster mining).

| Field | Value |
|-------|--------|
| Mod ID | `origami.veinminer` |
| Version | 1.0 |
| Game | 1.3.3 |
| Author | Origami |
| Clientside | false |

## Default controls

| Key | Action |
|-----|--------|
| `V` | Toggle vein mining |
| `K` | Open config |

Remappable in Settings. Default: **ON**.

## Build / install

```bash
python build-and-install.py
```

Config saved under `%APPDATA%\Necesse\cfg\mods\origami.veinminer.cfg`.

## Steam Workshop

```bash
python launch-for-upload.py VeinMiner
```

Workshop text: `../steam_workshop/VeinMiner/`.

## Notes

- Method patches: after game updates, retest mining + hotkeys first.
- Advice helpers used from vanilla classes must stay **public** (no lambdas in Advice).
