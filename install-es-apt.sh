#!/usr/bin/env bash
set -euo pipefail

ELASTIC_VERSION="${ELASTIC_VERSION:-8.15.4}"
KEYRING="/etc/apt/keyrings/elasticsearch-keyring.gpg"
SOURCE_LIST="/etc/apt/sources.list.d/elastic-8.x.list"

sudo install -d -m 0755 /etc/apt/keyrings
curl -fsSL https://artifacts.elastic.co/GPG-KEY-elasticsearch \
  | gpg --dearmor \
  | sudo tee "$KEYRING" >/dev/null

echo "deb [signed-by=$KEYRING] https://artifacts.elastic.co/packages/8.x/apt stable main" \
  | sudo tee "$SOURCE_LIST" >/dev/null

sudo apt-get update
sudo apt-get install -y "elasticsearch=$ELASTIC_VERSION"

sudo install -d -o elasticsearch -g elasticsearch /var/lib/elasticsearch /var/log/elasticsearch

sudo sed -i \
  -e 's/^#\?cluster.name:.*/cluster.name: studious-parakeet-local/' \
  -e 's/^#\?node.name:.*/node.name: wsl-dev-node/' \
  -e 's/^#\?network.host:.*/network.host: 127.0.0.1/' \
  -e 's/^#\?http.port:.*/http.port: 9200/' \
  /etc/elasticsearch/elasticsearch.yml

if ! grep -q '^discovery.type:' /etc/elasticsearch/elasticsearch.yml; then
  echo 'discovery.type: single-node' | sudo tee -a /etc/elasticsearch/elasticsearch.yml >/dev/null
fi

if grep -q '^xpack.security.enabled:' /etc/elasticsearch/elasticsearch.yml; then
  sudo sed -i 's/^xpack.security.enabled:.*/xpack.security.enabled: false/' /etc/elasticsearch/elasticsearch.yml
else
  echo 'xpack.security.enabled: false' | sudo tee -a /etc/elasticsearch/elasticsearch.yml >/dev/null
fi

if grep -q '^xpack.security.enrollment.enabled:' /etc/elasticsearch/elasticsearch.yml; then
  sudo sed -i 's/^xpack.security.enrollment.enabled:.*/xpack.security.enrollment.enabled: false/' /etc/elasticsearch/elasticsearch.yml
else
  echo 'xpack.security.enrollment.enabled: false' | sudo tee -a /etc/elasticsearch/elasticsearch.yml >/dev/null
fi

if command -v systemctl >/dev/null 2>&1 && systemctl is-system-running >/dev/null 2>&1; then
  sudo systemctl daemon-reload
  sudo systemctl enable elasticsearch
  sudo systemctl restart elasticsearch
else
  sudo service elasticsearch restart
fi

echo "Waiting for Elasticsearch on http://localhost:9200 ..."
for _ in {1..60}; do
  if curl -fsS http://localhost:9200 >/dev/null; then
    curl -sS http://localhost:9200/_cluster/health?pretty
    exit 0
  fi
  sleep 2
done

echo "Elasticsearch did not become ready within 120 seconds." >&2
exit 1
