#!/bin/bash

echo docker compose down
docker compose -f docker-compose-infra.yml down
docker compose rm -f

echo Clean Environment
containers=$(docker ps -a -q)

if [ -n "$containers" ]; then
    docker rm -f $containers
fi

docker volume prune --all --force
# docker system prune --all --force

echo Build application
#docker buildx build --platform linux/amd64 -t bmp-moneyp .

# docker push bmp-moneyp

docker compose -f docker-compose-infra.yml up --force-recreate -d

docker compose logs -f