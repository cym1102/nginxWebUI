FROM registry.cn-hangzhou.aliyuncs.com/cym1102/ubuntu:20.04
ADD nginxWebUI-1.9.8.jar /home/
ADD nginxWebUI.sh /home/
RUN chmod 777 /home/nginxWebUI.sh
ENTRYPOINT ["sh","-c", "/home/nginxWebUI.sh ${BOOT_OPTIONS} && tail -f /dev/null"]
