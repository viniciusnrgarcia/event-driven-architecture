cd payment-api
docker buildx build --platform linux/amd64 -t payment-api .
cd ..

cd payment-fraud-process
docker buildx build --platform linux/amd64 -t payment-fraud-process .
cd ..

cd payment-send
docker buildx build --platform linux/amd64 -t payment-send .
cd ..