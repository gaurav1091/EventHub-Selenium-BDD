FROM maven:3.9.8-eclipse-temurin-17

WORKDIR /eventhub

RUN apt-get update \
    && apt-get install -y --no-install-recommends chromium firefox-esr \
    && rm -rf /var/lib/apt/lists/*

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY . .

CMD ["mvn", "test", "-Dbrowser=chrome", "-Dheadless=true", "-Dcucumber.filter.tags=@smoke"]
