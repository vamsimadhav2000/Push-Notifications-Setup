#!/bin/sh
# Generates ios/Flutter/DartDefines.xcconfig from the local secrets.env so that
# launching the app directly from Xcode (Runner.xcworkspace) still receives the
# --dart-define values (PLOTLINE_API_KEY, CLEVERTAP_*, CT_DEMO_*, ...).
#
# Without this, running from Xcode skips Plotline.init (and any other
# String.fromEnvironment secret) because dart-defines are only passed when
# launching via `flutter run` / `flutter build`.
#
# Format: DART_DEFINES = base64("KEY1=VALUE1"),base64("KEY2=VALUE2"),...
# (comma-separated base64 of each KEY=VALUE, as Flutter's xcode_backend expects).
#
# This merges with the Flutter defaults already set by ios/Flutter/Generated.xcconfig
# (FLUTTER_VERSION, FLUTTER_CHANNEL, ...) so nothing is lost.
#
# Usage: ./tool/generate_dart_defines.sh   (from the project root)
set -e
cd "$(dirname "$0")/.."

ENV_FILE="secrets.env"
OUT="ios/Flutter/DartDefines.xcconfig"
GENERATED="ios/Flutter/Generated.xcconfig"

if [ ! -f "$ENV_FILE" ]; then
  echo "error: $ENV_FILE not found (gitignored). Cannot generate $OUT." >&2
  exit 1
fi

python3 - "$ENV_FILE" "$OUT" "$GENERATED" <<'PY'
import base64, pathlib, re, sys

env_file, out, generated = sys.argv[1], sys.argv[2], sys.argv[3]

def decode_list(value: str):
    result = {}
    for item in value.split(","):
        item = item.strip()
        if not item:
            continue
        try:
            decoded = base64.b64decode(item).decode("utf-8")
        except Exception:
            continue
        if "=" in decoded:
            key, val = decoded.split("=", 1)
            result[key] = val
    return result

# Start with the Flutter defaults that Generated.xcconfig already sets so that
# overriding DART_DEFINES here does not drop them.
merged = {}
if pathlib.Path(generated).exists():
    m = re.search(r"^DART_DEFINES\s*=\s*(.+)$", pathlib.Path(generated).read_text(), re.MULTILINE)
    if m:
        merged.update(decode_list(m.group(1)))

# Overlay the local secrets (they win on key conflict).
for line in pathlib.Path(env_file).read_text().splitlines():
    line = line.strip()
    if not line or line.startswith("#") or "=" not in line:
        continue
    key, value = line.split("=", 1)
    merged[key.strip()] = value.strip()

encoded = [
    base64.b64encode(f"{k}={v}".encode("utf-8")).decode("ascii")
    for k, v in merged.items()
]
pathlib.Path(out).write_text(f"DART_DEFINES = {','.join(encoded)}\n")
print(f"Wrote {out} ({len(merged)} defines)")
PY