FROM ubuntu:20.04
LABEL maintainer="cym1102@qq.com"
ENV DEBIAN_FRONTEND=noninteractive
ENV TZ=Asia/Shanghai
RUN apt-get clean && apt-get update &&\
	apt-get install -y nginx &&\
	apt-get install -y net-tools &&\
	apt-get install -y curl &&\
	apt-get install -y wget &&\
	ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone &&\
	apt-get install tzdata
ENV LANG C.UTF-8
ADD jre.tar.xz /home/
RUN chmod 777 /home/jre/bin/java
ADD nginxWebUI.sh /home/
RUN chmod 777 /home/nginxWebUI.sh
COPY target/nginxWebUI-*.jar /home/nginxWebUI.jar
ENTRYPOINT ["sh","-c", "/home/nginxWebUI.sh ${BOOT_OPTIONS} && tail -f /dev/null"]
