#!/usr/bin/env bash
set -euo pipefail

if command -v systemctl >/dev/null 2>&1 && systemctl is-system-running >/dev/null 2>&1; then
  exec sudo systemctl start elasticsearch
fi

if command -v service >/dev/null 2>&1; then
  exec sudo service elasticsearch start
fi

echo "Cannot find systemctl or service. Install Elasticsearch with ./install-es-apt.sh first." >&2
exit 1
