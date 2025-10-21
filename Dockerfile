FROM alpine:3.22
ENV LANG=zh_CN.UTF-8 \
    TZ=Asia/Shanghai \
    PS1="\u@\h:\w \$ "
# RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories \
RUN    apk add --update --no-cache \
       nginx \
	   nginx-mod-stream \
	   nginx-mod-stream-geoip \
	   nginx-mod-stream-geoip2 \
	   nginx-mod-stream-js \
	   nginx-mod-stream-keyval \
	   nginx-mod-http-headers-more \
	   nginx-mod-http-js \
	   nginx-mod-http-keyval \
	   nginx-mod-http-lua \
	   nginx-mod-http-brotli \
	   nginx-mod-rtmp \
	   nginx-mod-mail \
	   nginx-mod-http-geoip \
	   nginx-mod-http-geoip2 \
	   nginx-mod-http-zip \
	   nginx-mod-http-zstd \
	   nginx-mod-http-perl \
	   nginx-mod-http-upload \
	   nginx-mod-http-upload-progress \
	   nginx-mod-http-upstream-fair \
	   nginx-mod-http-upstream-jdomain \
	   nginx-mod-http-echo \
	   nginx-mod-http-cache-purge \
	   nginx-mod-dynamic-upstream \
	   nginx-mod-dynamic-healthcheck \
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
    && rm -rf /var/cache/apk/* /tmp/*
COPY target/nginxWebUI-*.jar /home/nginxWebUI.jar
COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN ["chmod", "+x", "/usr/local/bin/entrypoint.sh"]
VOLUME ["/home/nginxWebUI"]
ENTRYPOINT ["tini", "entrypoint.sh"]
