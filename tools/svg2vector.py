#!/usr/bin/env python3
"""Converts Oiê's brand SVGs (stickers, marks) to Android vector drawables.

    python3 tools/svg2vector.py <in.svg> <out.xml> [size_dp]

Handles what the brand files use: rect (with rx), circle, path; fill or stroke; and <g> with
translate / scale / rotate transforms. Text must be outlined first (stickers' _02 masters are).
"""
import re
import sys
import xml.etree.ElementTree as ET

NS = "{http://www.w3.org/2000/svg}"


def colour(c):
    if not c or c == "none":
        return None
    c = c.strip()
    if c.startswith("#") and len(c) == 7:
        return "#FF" + c[1:].upper()
    raise SystemExit(f"unsupported colour {c!r}: use a literal #RRGGBB in the brand SVG")


def circle(cx, cy, r):
    return f"M{cx - r},{cy}a{r},{r} 0 1,0 {2 * r},0a{r},{r} 0 1,0 {-2 * r},0z"


def rrect(x, y, w, h, r):
    r = min(r, w / 2, h / 2)
    if r == 0:
        return f"M{x},{y}h{w}v{h}h{-w}z"
    return (f"M{x + r},{y}h{w - 2 * r}a{r},{r} 0 0,1 {r},{r}v{h - 2 * r}a{r},{r} 0 0,1 {-r},{r}"
            f"h{-(w - 2 * r)}a{r},{r} 0 0,1 {-r},{-r}v{-(h - 2 * r)}a{r},{r} 0 0,1 {r},{-r}z")


def shape(el):
    tag = el.tag.replace(NS, "")
    a = el.attrib
    f = lambda k, d=0.0: float(a.get(k, d))
    if tag == "rect":
        d = rrect(f("x"), f("y"), f("width"), f("height"), f("rx"))
    elif tag == "circle":
        d = circle(f("cx"), f("cy"), f("r"))
    elif tag == "path":
        d = a["d"]
    else:
        return ""
    attrs = []
    fill = colour(a.get("fill", "#000000" if "stroke" not in a else "none"))
    if fill:
        attrs.append(f'android:fillColor="{fill}"')
        if a.get("fill-rule") == "evenodd":
            attrs.append('android:fillType="evenOdd"')
    stroke = colour(a.get("stroke"))
    if stroke:
        attrs.append(f'android:strokeColor="{stroke}" android:strokeWidth="{a.get("stroke-width", "1")}"')
        if a.get("stroke-linecap"):
            attrs.append(f'android:strokeLineCap="{a["stroke-linecap"]}"')
    return f'<path {" ".join(attrs)} android:pathData="{d}"/>'


def group(el, depth=1):
    out = []
    for child in el:
        tag = child.tag.replace(NS, "")
        if tag == "g":
            inner = group(child, depth + 1)
            ops = re.findall(r"(translate|scale|rotate)\(([^)]*)\)", child.attrib.get("transform", ""))
            # SVG applies the list right to left, so the last op wraps the content first.
            for op, args in reversed(ops):
                n = [float(v) for v in re.split(r"[ ,]+", args.strip())]
                if op == "translate":
                    g = f'android:translateX="{n[0]}" android:translateY="{n[1] if len(n) > 1 else 0}"'
                elif op == "scale":
                    g = f'android:scaleX="{n[0]}" android:scaleY="{n[1] if len(n) > 1 else n[0]}"'
                else:
                    g = f'android:rotation="{n[0]}"'
                inner = f"<group {g}>{inner}</group>"
            out.append(inner)
        elif tag in ("rect", "circle", "path"):
            if "transform" in child.attrib:
                raise SystemExit("transform on a shape: wrap it in a <g> in the brand SVG")
            out.append(shape(child))
    return "".join(out)


def convert(src, dst, size_dp=None):
    root = ET.parse(src).getroot()
    _, _, w, h = [float(v) for v in root.attrib["viewBox"].split()]
    size_dp = size_dp or w / 4
    body = group(root)
    xml = (f'<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="{size_dp:g}dp" '
           f'android:height="{size_dp * h / w:g}dp" android:viewportWidth="{w:g}" android:viewportHeight="{h:g}">\n'
           f"{body}\n</vector>\n")
    open(dst, "w").write(xml)


if __name__ == "__main__":
    convert(sys.argv[1], sys.argv[2], float(sys.argv[3]) if len(sys.argv) > 3 else None)
