#!/usr/bin/env bash

set -e

OUT_DIR="preview_png"
TMP_DIR="/tmp/vector_preview_tmp"

mkdir -p "$OUT_DIR"
mkdir -p "$TMP_DIR"

find . -type f \( -iname "*.png" -o -iname "*.svg" -o -iname "*.xml" \) | while read -r file; do

    safe_name=$(echo "$file" | sed 's|^\./||' | tr '/' '_')
    out="$OUT_DIR/${safe_name%.*}.png"

    ext="${file##*.}"

    case "$ext" in
        png|PNG)
            cp "$file" "$out"
            ;;

        svg|SVG)
            convert "$file" "$out"
            ;;

        xml|XML)
            tmp_svg="$TMP_DIR/${safe_name%.*}.svg"
            if vd2svg "$file" "$tmp_svg" 2>/dev/null; then
                convert "$tmp_svg" "$out"
            fi
            ;;

    esac

done

echo "Bitti. PNG'ler: $OUT_DIR"
