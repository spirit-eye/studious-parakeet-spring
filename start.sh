#!/usr/bin/env bash
set -euo pipefail

APP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [[ -z "${JAVA_HOME:-}" && -x "/home/ben/.jdks/openjdk-25.0.1/bin/java" ]]; then
  export JAVA_HOME="/home/ben/.jdks/openjdk-25.0.1"
fi

if [[ -n "${JAVA_HOME:-}" ]]; then
  export PATH="$JAVA_HOME/bin:$PATH"
fi

export SERVER_PORT="${SERVER_PORT:-8080}"
export SPRING_ELASTICSEARCH_URIS="${SPRING_ELASTICSEARCH_URIS:-http://localhost:9200}"

cd "$APP_DIR"
exec ./mvnw spring-boot:run "$@"
