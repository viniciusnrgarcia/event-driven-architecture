FROM ghcr.io/graalvm/native-image-community:17 as build
WORKDIR /payment-fraud-process
COPY . /payment-fraud-process
RUN ./mvnw -Pnative native:compile

FROM alpine
COPY --from=build /payment-fraud-process/target/payment-fraud-process /payment-fraud-process

RUN apk add libc6-compat curl
CMD ["./payment-fraud-process"]