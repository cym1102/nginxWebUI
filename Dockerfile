FROM alpine:3.14 AS builder
COPY . /build
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories \
    && apk add --update --no-cache maven \
    && cd /build \
    && if [[ -s settings.xml ]]; then \
           mkdir -p /root/.m2; \
           cp -fv settings.xml /root/.m2/settings.xml; \
       fi \
    && mvn clean package \
    && mkdir -p /out/home \
    && cp target/nginxWebUI-*.jar /out/home/nginxWebUI.jar
COPY entrypoint.sh /out/usr/local/bin/entrypoint.sh

FROM alpine:3.14
ENV LANG=zh_CN.UTF-8 \
    TZ=Asia/Shanghai \
    PS1="\u@\h:\w \$ "
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories \
    && apk add --update --no-cache \
       nginx \
       nginx-mod-stream \
       openjdk8-jre \
       net-tools \
       curl \
       wget \
       ttf-dejavu \
       fontconfig \
       tzdata \
       tini \
       acme.sh \
       sqlite \
    && fc-cache -f -v \
    && ln -sf /usr/share/zoneinfo/${TZ} /etc/localtime \
    && echo "${TZ}" > /etc/timezone \
    && rm -rf /var/cache/apk/* /tmp/*
COPY --from=builder /out /
VOLUME ["/home/nginxWebUI"]
ENTRYPOINT ["tini", "entrypoint.sh"]
       
