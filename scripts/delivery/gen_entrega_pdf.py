#!/usr/bin/env python3
"""Gera PDF da entrega Fase 3 a partir do Markdown (fpdf2 + HTML)."""

from __future__ import annotations

import html
import re
import sys
from pathlib import Path

from fpdf import FPDF

DEJAVU = Path("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf")
DEJAVU_BOLD = Path("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf")

META_PREFIXES = (
    "Este Markdown",
    "Regenerar com:",
    "pip install",
    "python3 scripts/delivery",
    "Arquivo gerado:",
)


def normalize_unicode(text: str) -> str:
    return (
        text.replace("\u2014", "-")
        .replace("\u2013", "-")
        .replace("\u2192", "->")
        .replace("\u2264", "<=")
        .replace("\u2265", ">=")
    )


def plain_md(text: str) -> str:
    text = normalize_unicode(text)
    text = re.sub(r"\*\*([^*]+)\*\*", r"\1", text)
    text = re.sub(r"`([^`]+)`", r"\1", text)
    text = re.sub(r"\[([^\]]+)\]\(([^)]+)\)", r"\1 (\2)", text)
    text = re.sub(r"^\*([^*]+)\*$", r"\1", text)
    return html.escape(text, quote=True)


def inline_md(text: str) -> str:
    text = normalize_unicode(text)
    text = html.escape(text, quote=True)
    text = re.sub(r"\*\*([^*]+)\*\*", r"<b>\1</b>", text)
    text = re.sub(r"`([^`]+)`", r"<code>\1</code>", text)
    text = re.sub(r"\[([^\]]+)\]\(([^)]+)\)", r'<a href="\2">\1</a>', text)
    text = re.sub(r"(https?://[^\s<&]+)", r'<a href="\1">\1</a>', text)
    return text


def is_table_sep(cells: list[str]) -> bool:
    return bool(cells) and all(re.fullmatch(r":?-{3,}:?", c.strip()) for c in cells)


def markdown_to_html(md_text: str) -> str:
    parts: list[str] = []
    in_code = False
    in_ul = False
    table_rows: list[list[str]] = []

    def flush_table() -> None:
        nonlocal table_rows
        if not table_rows:
            return
        parts.append('<table border="1" cellpadding="4" cellspacing="0" width="100%">')
        for i, row in enumerate(table_rows):
            tag = "th" if i == 0 else "td"
            parts.append("<tr>")
            for cell in row:
                parts.append(f"<{tag}>{plain_md(cell)}</{tag}>")
            parts.append("</tr>")
        parts.append("</table>")
        table_rows = []

    def close_ul() -> None:
        nonlocal in_ul
        if in_ul:
            parts.append("</ul>")
            in_ul = False

    for raw in md_text.splitlines():
        line = raw.rstrip()

        if line.strip().startswith("```"):
            in_code = not in_code
            continue
        if in_code:
            continue
        if line.strip() == "---":
            close_ul()
            flush_table()
            parts.append("<br/>")
            continue

        stripped = line.strip()
        if not stripped:
            close_ul()
            flush_table()
            parts.append("<br/>")
            continue
        if any(stripped.startswith(p) for p in META_PREFIXES):
            continue

        if line.startswith("|"):
            close_ul()
            cells = [c.strip() for c in line.split("|")[1:-1]]
            if is_table_sep(cells):
                continue
            table_rows.append(cells)
            continue

        flush_table()

        if line.startswith("# "):
            close_ul()
            parts.append(f"<h1>{inline_md(line[2:])}</h1>")
        elif line.startswith("## "):
            close_ul()
            parts.append(f"<h2>{inline_md(line[3:])}</h2>")
        elif line.startswith("### "):
            close_ul()
            parts.append(f"<h3>{inline_md(line[4:])}</h3>")
        elif line.startswith("- "):
            if not in_ul:
                parts.append("<ul>")
                in_ul = True
            parts.append(f"<li>{inline_md(line[2:])}</li>")
        else:
            close_ul()
            parts.append(f"<p>{inline_md(line)}</p>")

    close_ul()
    flush_table()
    return "\n".join(parts)


class Doc(FPDF):
    def footer(self) -> None:
        self.set_y(-15)
        self.set_font("DejaVu", "", 9)
        self.cell(0, 10, f"Pagina {self.page_no()}", align="C")


def main() -> int:
    base = Path(__file__).resolve().parents[2]
    md_path = base / "docs/delivery/entrega-portal-fase3.md"
    pdf_path = base / "docs/delivery/entrega-portal-fase3.pdf"

    if not md_path.exists():
        print(f"Arquivo nao encontrado: {md_path}", file=sys.stderr)
        return 1
    if not DEJAVU.exists() or not DEJAVU_BOLD.exists():
        print("Fontes DejaVu nao encontradas.", file=sys.stderr)
        return 1

    body = markdown_to_html(md_path.read_text(encoding="utf-8"))

    pdf = Doc()
    pdf.set_auto_page_break(auto=True, margin=18)
    pdf.set_margins(18, 18, 18)
    pdf.add_font("DejaVu", "", str(DEJAVU))
    pdf.add_font("DejaVu", "B", str(DEJAVU_BOLD))
    pdf.add_page()
    pdf.write_html(body, font_family="DejaVu")

    pdf.output(str(pdf_path))
    print(f"PDF gerado: {pdf_path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
