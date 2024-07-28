FROM ghcr.io/graalvm/native-image-community:17 as build
WORKDIR /fraud-api
COPY . /fraud-api
RUN ./mvnw -Pnative native:compile

FROM alpine
COPY --from=build /fraud-api/target/fraud-api /fraud-api

RUN apk add libc6-compat curl
EXPOSE 8080
CMD ["./fraud-api"]