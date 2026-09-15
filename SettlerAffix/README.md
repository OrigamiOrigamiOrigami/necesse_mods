# Settler Affix

Necesse mod: Commands-panel one-click toggles for Gem / Flower / Hired Hand storage collecting.

| Field | Value |
|-------|--------|
| Mod ID | `origami.settleraffix` |
| Version | 1.0 |
| Game | 1.3.3 |
| Author | Origami |
| Clientside | false |

## Usage

Settlement → **Commands** panel → bottom buttons:

- 一键开关拿宝石 / 拿花 / 拿钱  
- Sets the same `allowTakeFromStorage` flag as the NPC dialogue option, for **all** settlers with that affix.

## Build / install

```bash
python build-and-install.py
```

## Steam Workshop

```bash
python launch-for-upload.py SettlerAffix
```

Workshop text: `../steam_workshop/SettlerAffix/`.

## Notes

- No dedicated hotkeys (UI buttons only).
- Advice in Commands form: no lambdas in Advice bodies.
- After game updates: open Commands panel + check NPC dialogue sync.
