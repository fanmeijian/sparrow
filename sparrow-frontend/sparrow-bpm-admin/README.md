转换字体
sudo dnf install woff2

for f in *.ttf; do base="${f%.ttf}"; woff2_compress "$f"; ttf2woff "$f" "${base}.woff"; echo "Converted $f -> ${base}.woff, ${base}.woff2"; done