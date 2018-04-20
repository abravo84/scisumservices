############################################################
# Dockerfile to build SciSum Services container images
# Based on Ubuntu
############################################################

#FROM openjdk:8-jdk
FROM ubuntu:16.04
#FROM openminted_basic_container
MAINTAINER Àlex Bravo <abravo@upf.edu>

USER root

RUN apt-get -y update && apt-get -y upgrade && apt-get clean
RUN apt-get -y install default-jdk && apt-get clean
RUN apt-get -y install maven

RUN mvn -version

# Define working directory.
COPY . /scisumservices

WORKDIR /scisumservices

RUN mvn install:install-file -DgroupId=edu.upf.taln.summa -DartifactId=summaupf -Dversion=1.0 -Dfile=lib/Summa_UPF.jar -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true

RUN mvn install:install-file -Dfile=lib/ScoreComputation.jar -DgroupId=edu.upf.taln.sc -DartifactId=scorecomputation -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true

RUN mvn install:install-file -Dfile=lib/lib-4.0.0b.jar -DgroupId=edu.upf.taln.dri -DartifactId=lib -Dversion=4.0.0b -Dpackaging=jar -DgeneratePom=false -DcreateChecksum=true -DpomFile=lib/lib-4.0.0b.pom


#COPY ./src /scisumservices/
RUN mvn clean package -Dmaven.test.skip=true

RUN chmod a+x /scisumservices/process.sh && cp /scisumservices/process.sh /bin/process.sh
