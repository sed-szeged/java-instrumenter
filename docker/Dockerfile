FROM openjdk:7-jdk-jessie

ARG AGENT_VERSION

RUN apt-get update && \
    apt-get install -y build-essential maven && \
    curl -L https://cpanmin.us | perl - App::cpanminus

RUN git clone https://github.com/Frenkymd/defects4j.git /defects4j
WORKDIR /defects4j
RUN git checkout chain

RUN cpanm --installdeps . && ./init.sh

ENV PATH="/defects4j/framework/bin:${PATH}"

WORKDIR /instrumenter

COPY src src
COPY pom.xml .

RUN mvn clean package

RUN cp target/method-agent-$AGENT_VERSION-jar-with-dependencies.jar /defects4j/framework/lib/agent.jar

CMD defects4j compile && defects4j test
