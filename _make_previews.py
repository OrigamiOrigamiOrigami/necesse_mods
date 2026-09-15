# -*- coding: utf-8 -*-
"""Write unique SVG workshop previews and export preview.png (PyMuPDF)."""
from pathlib import Path

import fitz

ROOT = Path(__file__).resolve().parent
SIZE = 512

# Solid colors only: MuPDF SVG rasterizer skips many gradients/filters.
FRAME = """
  <rect width="512" height="512" fill="#1b2433"/>
  <rect x="22" y="22" width="468" height="468" rx="36" fill="#243044"/>
  <rect x="22" y="22" width="468" height="468" rx="36" fill="none" stroke="#0d1218" stroke-width="18"/>
  <rect x="40" y="40" width="432" height="432" rx="26" fill="none" stroke="#3d4d64" stroke-width="6"/>
  <rect x="52" y="52" width="408" height="408" rx="20" fill="none" stroke="#d4b06a" stroke-width="3"/>
"""


def wrap(inner):
    return (
        '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" width="512" height="512">\n'
        + FRAME
        + inner
        + "\n</svg>\n"
    )


HELPFUL = wrap(
    """
  <path d="M92 176 L256 92 L420 176 L420 204 L92 204 Z" fill="#c45a48"/>
  <path d="M108 176 L256 108 L404 176 L404 188 L256 124 L108 188 Z" fill="#e07a66"/>
  <rect x="120" y="204" width="272" height="16" fill="#6a3828"/>
  <rect x="132" y="220" width="248" height="156" rx="10" fill="#7a4a28"/>
  <rect x="132" y="220" width="248" height="18" fill="#9a6238"/>
  <rect x="152" y="248" width="208" height="18" rx="4" fill="#e4d2a8"/>
  <!-- coin -->
  <circle cx="192" cy="318" r="32" fill="#efc56a"/>
  <circle cx="192" cy="318" r="32" fill="none" stroke="#9a6a20" stroke-width="4"/>
  <circle cx="192" cy="318" r="14" fill="none" stroke="#9a6a20" stroke-width="4"/>
  <!-- potion -->
  <rect x="242" y="276" width="28" height="18" rx="4" fill="#d7dee8"/>
  <path d="M232 294 h48 l10 52 h-68 z" fill="#4db6a0"/>
  <path d="M244 294 h24 l6 20 h-36 z" fill="#8fe0d0"/>
  <!-- vinyl -->
  <circle cx="340" cy="322" r="36" fill="#111"/>
  <circle cx="340" cy="322" r="24" fill="none" stroke="#555" stroke-width="3"/>
  <circle cx="340" cy="322" r="8" fill="#efc56a"/>
    """
)

BROWSER = wrap(
    """
  <rect x="96" y="96" width="250" height="250" rx="22" fill="#16202e" stroke="#4a5d78" stroke-width="8"/>
  <g fill="#2b3a50">
    <rect x="118" y="118" width="58" height="58" rx="10"/>
    <rect x="192" y="118" width="58" height="58" rx="10"/>
    <rect x="266" y="118" width="58" height="58" rx="10"/>
    <rect x="118" y="192" width="58" height="58" rx="10"/>
    <rect x="192" y="192" width="58" height="58" rx="10"/>
    <rect x="266" y="192" width="58" height="58" rx="10"/>
    <rect x="118" y="266" width="58" height="58" rx="10"/>
    <rect x="192" y="266" width="58" height="58" rx="10"/>
    <rect x="266" y="266" width="58" height="58" rx="10"/>
  </g>
  <rect x="130" y="130" width="34" height="34" rx="7" fill="#4db6a0"/>
  <rect x="204" y="130" width="34" height="34" rx="7" fill="#efc56a"/>
  <rect x="278" y="130" width="34" height="34" rx="7" fill="#8e7cc3"/>
  <rect x="130" y="204" width="34" height="34" rx="7" fill="#efc56a"/>
  <rect x="204" y="204" width="34" height="34" rx="7" fill="#c45a48"/>
  <rect x="278" y="204" width="34" height="34" rx="7" fill="#4db6a0"/>
  <rect x="130" y="278" width="34" height="34" rx="7" fill="#8e7cc3"/>
  <rect x="204" y="278" width="34" height="34" rx="7" fill="#4db6a0"/>
  <rect x="278" y="278" width="34" height="34" rx="7" fill="#efc56a"/>
  <circle cx="332" cy="332" r="86" fill="#4db6a0" fill-opacity="0.16"/>
  <circle cx="332" cy="332" r="86" fill="none" stroke="#f0ead8" stroke-width="20"/>
  <path d="M392 392 L456 456" stroke="#f0ead8" stroke-width="28" stroke-linecap="round"/>
    """
)

VEIN = wrap(
    """
  <g stroke="#0d1218" stroke-width="5">
    <rect x="150" y="286" width="70" height="56" rx="8" fill="#4db6a0"/>
    <rect x="212" y="262" width="76" height="60" rx="8" fill="#efc56a"/>
    <rect x="280" y="290" width="68" height="54" rx="8" fill="#3a9e8a"/>
    <rect x="176" y="330" width="72" height="54" rx="8" fill="#c48a2e"/>
    <rect x="244" y="336" width="76" height="56" rx="8" fill="#4db6a0"/>
    <rect x="128" y="334" width="58" height="48" rx="8" fill="#efc56a"/>
  </g>
  <g transform="rotate(-36 250 230)">
    <rect x="236" y="128" width="28" height="228" rx="12" fill="#8b5a32" stroke="#0d1218" stroke-width="4"/>
    <path d="M156 138 L344 138 L368 186 L250 214 L132 186 Z" fill="#eef2f6" stroke="#0d1218" stroke-width="5"/>
    <path d="M180 148 L320 148 L332 172 L250 188 L168 172 Z" fill="#ffffff"/>
  </g>
    """
)

TRASH = wrap(
    """
  <rect x="222" y="88" width="68" height="34" rx="10" fill="#e07a66"/>
  <rect x="156" y="116" width="200" height="34" rx="10" fill="#c45a48"/>
  <path d="M168 164 L344 164 L324 400 Q256 428 188 400 Z" fill="#2b3a50" stroke="#f0ead8" stroke-width="12"/>
  <path d="M204 196 L216 368" stroke="#4a5d78" stroke-width="10" stroke-linecap="round"/>
  <path d="M256 196 L256 380" stroke="#4a5d78" stroke-width="10" stroke-linecap="round"/>
  <path d="M308 196 L296 368" stroke="#4a5d78" stroke-width="10" stroke-linecap="round"/>
  <rect x="108" y="188" width="32" height="32" rx="6" fill="#efc56a" transform="rotate(-20 124 204)"/>
  <rect x="372" y="210" width="28" height="28" rx="6" fill="#4db6a0" transform="rotate(18 386 224)"/>
    """
)

AFFIX = wrap(
    """
  <circle cx="228" cy="168" r="62" fill="#efc56a"/>
  <circle cx="210" cy="156" r="10" fill="#3a2a10" opacity="0.25"/>
  <circle cx="246" cy="156" r="10" fill="#3a2a10" opacity="0.25"/>
  <path d="M158 244 Q158 220 196 220 H260 Q298 220 298 244 V372 Q228 404 158 372 Z" fill="#4db6a0"/>
  <rect x="136" y="248" width="40" height="92" rx="18" fill="#3a9e8a"/>
  <rect x="280" y="248" width="40" height="92" rx="18" fill="#3a9e8a"/>
  <!-- gem -->
  <rect x="322" y="108" width="90" height="74" rx="18" fill="#1c2636" stroke="#f0ead8" stroke-width="6"/>
  <polygon points="367,124 390,162 344,162" fill="#5ec8ff"/>
  <!-- flower -->
  <rect x="322" y="198" width="90" height="74" rx="18" fill="#1c2636" stroke="#f0ead8" stroke-width="6"/>
  <circle cx="367" cy="244" r="18" fill="#e07a66"/>
  <ellipse cx="367" cy="234" rx="11" ry="16" fill="#7ad47a"/>
  <!-- coin -->
  <rect x="322" y="288" width="90" height="74" rx="18" fill="#1c2636" stroke="#f0ead8" stroke-width="6"/>
  <circle cx="367" cy="325" r="20" fill="#efc56a"/>
  <circle cx="367" cy="325" r="11" fill="none" stroke="#9a6a20" stroke-width="4"/>
    """
)

MODS = {
    "HelpfulNPCs": HELPFUL,
    "ItemBrowser": BROWSER,
    "VeinMiner": VEIN,
    "AutoTrash": TRASH,
    "SettlerAffix": AFFIX,
}


def main():
    for name, svg in MODS.items():
        folder = ROOT / name / "src" / "main" / "resources"
        folder.mkdir(parents=True, exist_ok=True)
        svg_path = folder / "preview.svg"
        png_path = folder / "preview.png"
        svg_path.write_text(svg, encoding="utf-8")
        doc = fitz.open(stream=svg.encode("utf-8"), filetype="svg")
        page = doc[0]
        zoom = SIZE / max(page.rect.width, 1)
        pix = page.get_pixmap(matrix=fitz.Matrix(zoom, zoom), alpha=False)
        pix.save(str(png_path))
        doc.close()
        print("%s svg=%d png=%d" % (name, svg_path.stat().st_size, png_path.stat().st_size))


if __name__ == "__main__":
    main()
