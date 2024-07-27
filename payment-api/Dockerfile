FROM ghcr.io/graalvm/native-image-community:17 as build
WORKDIR /payment-api
COPY . /payment-api
RUN ./mvnw -Pnative native:compile

FROM alpine
COPY --from=build /payment-api/target/payment-api /payment-api

RUN apk add libc6-compat curl
EXPOSE 8080
CMD ["./payment-api"]