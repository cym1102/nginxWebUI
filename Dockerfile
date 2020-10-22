FROM ubuntu:20.04
MAINTAINER Chenyimeng "cym1102@qq.com"
RUN apt-get clean && apt-get update &&\
	apt-get install -y nginx &&\
	apt-get install -y openjdk-8-jre &&\
	apt-get install -y curl &&\
	apt-get install -y wget
COPY target/nginxWebUI-*.jar /home/nginxWebUI.jar
ADD nginxWebUI.sh /home/
RUN chmod 777 /home/nginxWebUI.sh
ENTRYPOINT ["sh","-c", "/home/nginxWebUI.sh ${BOOT_OPTIONS} && tail -f /dev/null"]
