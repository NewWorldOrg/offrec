FROM eclipse-temurin:17-jdk

ARG SBT_VERSION=1.10.0

WORKDIR /code

RUN apt-get update \
    && apt-get install -y curl gnupg \
    && curl -fsSL https://keyserver.ubuntu.com/pks/lookup?op=get\&search=0x99E82A75642AC823 | gpg --dearmor > /usr/share/keyrings/sbt.gpg \
    && echo "deb [signed-by=/usr/share/keyrings/sbt.gpg] https://repo.scala-sbt.org/scalasbt/debian all main" > /etc/apt/sources.list.d/sbt.list \
    && echo "deb [signed-by=/usr/share/keyrings/sbt.gpg] https://repo.scala-sbt.org/scalasbt/debian /" > /etc/apt/sources.list.d/sbt_old.list \
    && apt-get update \
    && apt-get install -y sbt=${SBT_VERSION}* \
    && rm -rf /var/lib/apt/lists/*

COPY . /code

RUN  git config --global --add safe.directory /code
