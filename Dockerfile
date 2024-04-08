FROM gradle:7.6.4-jdk17 AS BUILD
WORKDIR /usr/app/
COPY . .
RUN gradle clean build

FROM openjdk:17
ENV JAR_NAME=app-0.0.1-SNAPSHOT.jar
ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME
COPY --from=BUILD $APP_HOME .
EXPOSE 8080
ENTRYPOINT exec java -jar $APP_HOME/build/libs/$JAR_NAME