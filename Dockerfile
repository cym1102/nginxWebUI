FROM alpine:3.22

ENV LANG=zh_CN.UTF-8 \
    TZ=Asia/Shanghai \
    PS1="\u@\h:\w \$ "

# 只给 nginx 相关包使用 edge/main，避免整个系统无意切到 edge
RUN echo "https://dl-cdn.alpinelinux.org/alpine/edge/main" >> /etc/apk/repositories \
    && apk add --update --no-cache \
    nginx=1.30.1-r0 \
    nginx-mod-stream=1.30.1-r0 \
    nginx-mod-stream-geoip=1.30.1-r0 \
    nginx-mod-stream-geoip2=1.30.1-r0 \
    nginx-mod-stream-js=1.30.1-r0 \
    nginx-mod-stream-keyval=1.30.1-r0 \
    nginx-mod-http-headers-more=1.30.1-r0 \
    nginx-mod-http-js=1.30.1-r0 \
    nginx-mod-http-keyval=1.30.1-r0 \
    nginx-mod-http-lua=1.30.1-r0 \
    nginx-mod-http-brotli=1.30.1-r0 \
    nginx-mod-rtmp=1.30.1-r0 \
    nginx-mod-mail=1.30.1-r0 \
    nginx-mod-http-geoip=1.30.1-r0 \
    nginx-mod-http-geoip2=1.30.1-r0 \
    nginx-mod-http-zip=1.30.1-r0 \
    nginx-mod-http-zstd=1.30.1-r0 \
    nginx-mod-http-perl=1.30.1-r0 \
    nginx-mod-http-upload=1.30.1-r0 \
    nginx-mod-http-upload-progress=1.30.1-r0 \
    nginx-mod-http-upstream-fair=1.30.1-r0 \
    nginx-mod-http-echo=1.30.1-r0 \
    nginx-mod-http-cache-purge=1.30.1-r0 \
    nginx-mod-dynamic-upstream=1.30.1-r0 \
    nginx-mod-dynamic-healthcheck=1.30.1-r0 \
    openjdk8-jre \
    net-tools \
    curl \
    wget \
    ttf-dejavu \
    fontconfig \
    tzdata \
    logrotate \
    tini \
    acme.sh \
    && fc-cache -f -v \
    && ln -sf /usr/share/zoneinfo/${TZ} /etc/localtime \
    && echo "${TZ}" > /etc/timezone \
    && nginx -v \
    && nginx -V \
    && rm -rf /var/cache/apk/* /tmp/*

COPY target/nginxWebUI-*.jar /home/nginxWebUI.jar

COPY entrypoint.sh /usr/local/bin/entrypoint.sh

RUN ["chmod", "+x", "/usr/local/bin/entrypoint.sh"]

VOLUME ["/home/nginxWebUI"]

ENTRYPOINT ["tini", "entrypoint.sh"]
