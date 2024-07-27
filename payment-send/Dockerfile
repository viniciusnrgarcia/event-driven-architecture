FROM ghcr.io/graalvm/native-image-community:17 as build
WORKDIR /payment-send
COPY . /payment-send
RUN ./mvnw -Pnative native:compile

FROM alpine
COPY --from=build /payment-send/target/payment-send /payment-send

RUN apk add libc6-compat curl
EXPOSE 8080
CMD ["./payment-send"]