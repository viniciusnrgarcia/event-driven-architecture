#!/bin/bash

echo docker compose -f docker-compose-infra.yml down
docker compose -f docker-compose-infra.yml down
docker compose rm -f

echo Clean Environment
containers=$(docker ps -a -q)

if [ -n "$containers" ]; then
    docker rm -f $containers
fi

docker volume prune --all --force
docker system prune --all --force

echo Build application

docker compose -f docker-compose-infra.yml up --force-recreate -d

docker compose -f docker-compose-infra.yml logs -f
