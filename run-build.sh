#!/bin/bash

echo docker compose down
docker compose down
docker compose rm -f

echo Clean Environment
containers=$(docker ps -a -q)

if [ -n "$containers" ]; then
    docker rm -f $containers
fi

docker volume prune --all --force
docker system prune --all --force

echo Build application
# docker buildx build --platform linux/amd64 -t vnrg/rinha-backend-2024-q1 .
# docker buildx build --platform linux/amd64 -t rinha-backend-2024-q1 .

cd payment-api
docker buildx build --platform linux/amd64 -t payment-api .
cd ..

cd payment-fraud-process
docker buildx build --platform linux/amd64 -t payment-fraud-process .
cd ..

cd payment-send
docker buildx build --platform linux/amd64 -t payment-send .
cd ..


# docker push vnrg/rinha-backend-2024-q1

docker compose up --force-recreate -d

docker compose logs -f