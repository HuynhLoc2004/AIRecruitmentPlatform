#!/bin/bash
# Spin up the Docker stack (backend is built internally inside the container)

echo "========== Starting Docker Stack Build & Run =========="
docker-compose up --build
