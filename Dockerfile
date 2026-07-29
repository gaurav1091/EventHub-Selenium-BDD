FROM maven:3.9.8-eclipse-temurin-17

ARG TARGETARCH

WORKDIR /eventhub

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        ca-certificates \
        curl \
        fonts-liberation \
        gnupg \
        libnss3 \
        libxss1 \
        libatk-bridge2.0-0 \
        libdbus-glib-1-2 \
        libgtk-3-0 \
        libgbm1 \
        xz-utils \
        xvfb \
    && rm -rf /var/lib/apt/lists/*

RUN if [ "${TARGETARCH}" != "amd64" ]; then \
        echo "This image installs Google Chrome, which requires linux/amd64. Build with --platform=linux/amd64." >&2; \
        exit 1; \
    fi \
    && curl -fsSL https://dl.google.com/linux/linux_signing_key.pub \
        | gpg --dearmor -o /usr/share/keyrings/google-linux-signing-keyring.gpg \
    && echo "deb [arch=amd64 signed-by=/usr/share/keyrings/google-linux-signing-keyring.gpg] http://dl.google.com/linux/chrome/deb/ stable main" \
        > /etc/apt/sources.list.d/google-chrome.list \
    && apt-get update \
    && apt-get install -y --no-install-recommends google-chrome-stable \
    && rm -rf /var/lib/apt/lists/*

RUN curl -fsSL "https://download.mozilla.org/?product=firefox-latest-ssl&os=linux64&lang=en-US" -o /tmp/firefox.tar.xz \
    && mkdir -p /opt/firefox \
    && tar -xJf /tmp/firefox.tar.xz -C /opt \
    && ln -sf /opt/firefox/firefox /usr/local/bin/firefox \
    && rm /tmp/firefox.tar.xz

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY . .
RUN chmod +x scripts/docker-entrypoint.sh

ENTRYPOINT ["scripts/docker-entrypoint.sh"]
