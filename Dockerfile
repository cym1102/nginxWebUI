FROM ubuntu
ARG DEBIAN_FRONTEND=noninteractive
ENV LANG=zh_CN.UTF-8 \
    TZ=Asia/Shanghai \
    PS1="\u@\h:\w \$ "
RUN apt update \
    && apt install -y --no-install-recommends \
       nginx \
       libnginx-mod-stream \
       openjdk-8-jre \
       net-tools \
       curl \
       wget \
       ttf-dejavu \
       fontconfig \
       tzdata \
       tini \
       sqlite3 \
    && fc-cache -f -v \
    && ln -sf /usr/share/zoneinfo/${TZ} /etc/localtime \
    && echo "${TZ}" > /etc/timezone \
    && rm -rf /var/cache/apk/* /tmp/*
COPY target/nginxWebUI-*.jar /home/nginxWebUI.jar
COPY entrypoint.sh /usr/local/bin/entrypoint.sh
VOLUME ["/home/nginxWebUI"]
ENTRYPOINT ["tini", "entrypoint.sh"]
