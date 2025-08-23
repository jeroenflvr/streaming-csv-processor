#!/usr/bin/env bash
set -euo pipefail

CERT_DIR="${1:-./certs}"
DAYS="${DAYS:-3650}"
COMMON_NAME="${CN:-redpanda}"

mkdir -p "$CERT_DIR"
cd "$CERT_DIR"

echo "[1/4] Creating CA..."
openssl req -x509 -new -newkey rsa:4096 -nodes \
  -keyout ca.key -out ca.crt -days "$DAYS" -subj "/CN=rp-ca"

echo "[2/4] Creating broker key + CSR..."
openssl req -new -newkey rsa:4096 -nodes \
  -keyout server.key -out server.csr -subj "/CN=${COMMON_NAME}" \
  -addext "subjectAltName = DNS:redpanda, DNS:localhost, DNS:host.docker.internal, IP:127.0.0.1"

echo "[3/4] Signing broker cert..."
cat >san.cnf <<'EOF'
[v3_req]
subjectAltName = DNS:redpanda, DNS:localhost, DNS:host.docker.internal, IP:127.0.0.1
EOF
openssl x509 -req -in server.csr -CA ca.crt -CAkey ca.key -CAcreateserial \
  -out server.crt -days "$DAYS" -extfile san.cnf -extensions v3_req

echo "[4/4] Building Java truststore (password: changeit)..."
if ! command -v keytool >/dev/null 2>&1; then
  echo "keytool not found. Install a JRE/JDK (e.g., OpenJDK) to create truststore.jks."
  exit 1
fi
keytool -importcert -noprompt -trustcacerts -alias redpanda-ca -file ca.crt \
  -keystore truststore.jks -storepass changeit

echo "Done. Files in $(pwd):"
ls -1
