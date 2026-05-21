#!/usr/bin/env python3
"""Gera PDF da entrega Fase 3 a partir do Markdown (fpdf2)."""

from __future__ import annotations

import re
import sys
from pathlib import Path

from fpdf import FPDF


def strip_md(text: str) -> str:
    replacements = {
        "\u2014": "-",
        "\u2013": "-",
        "\u2192": "->",
        "\u2264": "<=",
        "\u2265": ">=",
        "\u201c": '"',
        "\u201d": '"',
        "\u2018": "'",
        "\u2019": "'",
    }
    for src, dst in replacements.items():
        text = text.replace(src, dst)
    text = text.encode("ascii", "replace").decode("ascii")
    text = re.sub(r"!\[[^\]]*\]\([^)]+\)", "", text)
    text = re.sub(r"\[([^\]]+)\]\(([^)]+)\)", r"\1 (\2)", text)
    text = re.sub(r"`([^`]+)`", r"\1", text)
    text = re.sub(r"^#+\s*", "", text, flags=re.MULTILINE)
    text = re.sub(r"^\|\s*---.*$", "", text, flags=re.MULTILINE)
    text = text.replace("|", " ")
    return text


class Doc(FPDF):
    def footer(self) -> None:
        self.set_y(-15)
        self.set_font("Helvetica", "", 9)
        self.cell(0, 10, f"Pagina {self.page_no()}", align="C")


def main() -> int:
    base = Path(__file__).resolve().parents[2]
    md_path = base / "docs/delivery/entrega-portal-fase3.md"
    pdf_path = base / "docs/delivery/entrega-portal-fase3.pdf"
    if not md_path.exists():
        print(f"Arquivo nao encontrado: {md_path}", file=sys.stderr)
        return 1

    pdf = Doc()
    pdf.set_auto_page_break(auto=True, margin=15)
    pdf.add_page()
    pdf.set_font("Helvetica", size=10)
    width = pdf.w - pdf.l_margin - pdf.r_margin

    for raw_line in md_path.read_text(encoding="utf-8").splitlines():
        line = strip_md(raw_line.rstrip())
        if not line.strip():
            pdf.ln(3)
            continue
        if raw_line.startswith("# "):
            pdf.set_font("Helvetica", "B", 13)
            pdf.multi_cell(width, 7, line)
            pdf.set_font("Helvetica", size=10)
            continue
        if raw_line.startswith("## "):
            pdf.set_font("Helvetica", "B", 11)
            pdf.multi_cell(width, 6, line)
            pdf.set_font("Helvetica", size=10)
            continue
        if len(line) > 110:
            for i in range(0, len(line), 110):
                pdf.multi_cell(width, 5, line[i : i + 110])
        else:
            pdf.multi_cell(width, 5, line)

    pdf.output(str(pdf_path))
    print(f"PDF gerado: {pdf_path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
