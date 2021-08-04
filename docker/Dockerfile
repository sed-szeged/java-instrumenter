FROM openjdk:8-jdk-buster

RUN apt-get update && \
    apt-get install software-properties-common -y && \
    apt-get update && \
    apt-get install -y \
                maven \
                git \
                build-essential \
                subversion \
                perl \
                curl \
                unzip \
                cpanminus \
                make \
                && \
    rm -rf /var/lib/apt/lists/*

# Timezone
ENV TZ=America/Los_Angeles
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Setup Defects4j
WORKDIR /
RUN git clone https://github.com/Frenkymd/defects4j.git /defects4j

WORKDIR /defects4j
RUN git checkout chain
RUN cpanm --installdeps . && ./init.sh

ENV PATH="/defects4j/framework/bin:${PATH}"

WORKDIR /instrumenter

COPY src src
COPY pom.xml .

RUN mvn clean package

RUN cp target/method-agent-*-jar-with-dependencies.jar /defects4j/framework/lib/agent.jar

CMD defects4j compile && defects4j test
