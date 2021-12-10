FROM cym1102/nginxwebui-base:latest
ENV LANG=zh_CN.UTF-8 \
    TZ=Asia/Shanghai \
    PS1="\u@\h:\w \$ "
COPY target/nginxWebUI-*.jar /home/nginxWebUI.jar
COPY entrypoint.sh /usr/local/bin/entrypoint.sh
VOLUME ["/home/nginxWebUI"]
ENTRYPOINT ["tini", "entrypoint.sh"]