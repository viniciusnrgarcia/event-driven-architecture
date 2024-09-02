FROM ghcr.io/graalvm/native-image-community:17 as build
WORKDIR /payment-batch-service
COPY . /payment-batch-service
RUN ./mvnw -Pnative native:compile

FROM alpine
COPY --from=build /payment-batch-service/target/payment-batch-service /payment-batch-service

RUN apk add libc6-compat curl
EXPOSE 8080
CMD ["./payment-batch-service"]