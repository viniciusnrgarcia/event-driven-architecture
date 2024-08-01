FROM ghcr.io/graalvm/native-image-community:17 as build
WORKDIR /payment-service
COPY . /payment-service
RUN ./mvnw -Pnative native:compile

FROM alpine
COPY --from=build /payment-service/target/payment-service /payment-service

RUN apk add libc6-compat curl
EXPOSE 8080
CMD ["./payment-service"]