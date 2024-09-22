FROM ghcr.io/graalvm/native-image-community:17 as build
WORKDIR /event-monitor
COPY . /event-monitor
RUN ./mvnw -Pnative native:compile

FROM alpine
COPY --from=build /event-monitor/target/event-monitor /event-monitor

RUN apk add libc6-compat curl
EXPOSE 8080
CMD ["./event-monitor"]