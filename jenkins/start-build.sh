#!/bin/bash -e

PROJECT=${1}
JOB_NAME=${2}
GRANULARITY=${3}

JENKINS_CLI_JAR="jenkins-cli.jar"
JENKINS_URL="http://localhost:8080/"

NUMBER_OF_BUGS=$(defects4j info -p ${PROJECT} | grep "Number of bugs:" | cut -d':' -f2)

for VERSION in $(seq 1 ${NUMBER_OF_BUGS})
do
	java -jar ${JENKINS_CLI_JAR} -s ${JENKINS_URL} build ${JOB_NAME} -w -p project=${PROJECT} -p version=${VERSION}b -p granularity=${GRANULARITY}
done
