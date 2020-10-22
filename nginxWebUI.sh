#不存在目标jar,就释放jar
if [ ! -f "/home/nginxWebUI/nginxWebUI.jar" ]; then 
    if [ ! -d "/home/nginxWebUI/" ]; then
       mkdir /home/nginxWebUI/
    fi
    cp /home/nginxWebUI.jar /home/nginxWebUI/
fi

#启动jar
nohup java -jar -Xmx64m /home/nginxWebUI/nginxWebUI.jar $1 > /dev/null &