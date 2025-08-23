#!/usr/bin/env bash
set -euo pipefail

IN="./data-duckdb/orders_sf10.csv"     # input CSV
OUTDIR="./data-duckdb/out_chunks"      # output dir
CHUNK=200000               # lines per file (excluding header)

mkdir -p "$OUTDIR"

awk -v CHUNK="$CHUNK" -v OUTDIR="$OUTDIR" '
NR==1 {
  header = $0 ";"
  next
}
{
  # Start a new file every CHUNK lines
  if (((NR-2) % CHUNK) == 0) {
    if (out) close(out)
    fname = sprintf("%s/part_%05d.csv", OUTDIR, i++)
    out = fname
    print header > out
  }
  print $0 ";" >> out
}
END { if (out) close(out) }
' "$IN"

echo "Done. Files in $OUTDIR/"