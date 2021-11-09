# nginxWebUI

### [README.md English version](https://github.com/cym1102/nginxWebUI/blob/master/README_EN.md)

#### 介绍
nginx网页配置工具

QQ技术交流群1: 1106758598
QQ技术交流群2: 560797506

邮箱: cym1102@qq.com

官网地址: http://www.nginxwebui.cn

github: https://github.com/cym1102/nginxWebUI

微信捐赠二维码

<img src="http://www.nginxwebui.cn/img/weixin.png"  height="200" width="200">

#### 功能说明

nginxWebUI是一款图形化管理nginx配置得工具, 可以使用网页来快速配置nginx的各项功能, 包括http协议转发, tcp协议转发, 反向代理, 负载均衡, 静态html服务器, ssl证书自动申请、续签、配置等, 配置好后可一建生成nginx.conf文件, 同时可控制nginx使用此文件进行启动与重载, 完成对nginx的图形化控制闭环.

nginxWebUI也可管理多个nginx服务器集群, 随时一键切换到对应服务器上进行nginx配置, 也可以一键将某台服务器配置同步到其他服务器, 方便集群管理.

nginx本身功能复杂, nginxWebUI并不能涵盖nginx所有功能, 但能覆盖nginx日常90%的功能使用配置, 平台没有涵盖到的nginx配置项, 可以使用自定义参数模板, 在conf文件中生成配置独特的参数。

部署此项目后, 配置nginx再也不用上网各种搜索配置代码, 再也不用手动申请和配置ssl证书, 只需要在本项目中进行增删改查就可方便的配置和启动nginx。

```
演示地址: http://test.nginxwebui.cn:8080
用户名: admin
密码: Admin123
```


#### 技术说明

本项目是基于springBoot的web系统, 数据库使用sqlite, 因此服务器上不需要安装任何数据库

项目启动时会释放一个.sqlite.db到系统用户文件夹中, 注意进行备份

本系统通过Let's encrypt申请证书, 使用acme.sh脚本进行自动化申请和续签, 开启续签的证书将在每天凌晨2点进行续签, 只有超过60天的证书才会进行续签. 只支持在linux下签发证书.

添加tcp/ip转发配置支持时, 一些低版本的nginx可能需要重新编译，通过添加–with-stream参数指定安装stream模块才能使用, 但在ubuntu 18.04下, 官方软件库中的nginx已经带有stream模块, 不需要重新编译. 本系统如果配置了tcp转发项的话, 会自动引入ngx_stream_module.so的配置项, 如果没有开启则不引入, 最大限度优化ngnix配置文件. 

#### jar安装说明
以Ubuntu操作系统为例,

 **注意：本项目需要在root用户下运行系统命令，极容易被黑客利用，请一定修改密码为复杂密码**

1.安装java运行环境和nginx

Ubuntu:

```
apt update
apt install openjdk-11-jdk
apt install nginx
```

Centos:

```
yum install java-11-openjdk
yum install nginx
```

Windows:

```
下载JDK安装包 https://www.oracle.com/java/technologies/downloads/
配置JAVA运行环境 
JAVA_HOME : JDK安装目录
Path : JDK安装目录\bin
重启电脑
```


2.下载最新版发行包jar

```
Linux: wget -O /home/nginxWebUI/nginxWebUI.jar http://file.nginxwebui.cn/nginxWebUI-2.7.8.jar

Windows: 直接使用浏览器下载 http://file.nginxwebui.cn/nginxWebUI-2.7.8.jar
```

有新版本只需要修改路径中的版本即可

3.启动程序

```
Linux: nohup java -jar -Xmx64m /home/nginxWebUI/nginxWebUI.jar --server.port=8080 --project.home=/home/nginxWebUI/ > /dev/null &

Windows: java -jar -Xmx64m D:/home/nginxWebUI/nginxWebUI.jar --server.port=8080 --project.home=D:/home/nginxWebUI/
```

参数说明(都是非必填)

-Xmx64m 最大分配内存数

--server.port 占用端口, 默认以8080端口启动

--project.home 项目配置文件目录，存放数据库文件，证书文件，日志等, 默认为/home/nginxWebUI/

--spring.database.type=mysql 使用其他数据库，不填为使用本地sqlite，选项包括mysql和postgresql

--spring.datasource.url=jdbc:mysql://ip:port/nginxwebui 数据库url

--spring.datasource.username=root  数据库用户

--spring.datasource.password=pass  数据库密码

--knife4j.production=false  false:打开接口调试页面 true:关闭接口调试页面

注意命令最后加一个&号, 表示项目后台运行

#### docker安装说明

本项目制作了docker镜像, 同时包含nginx和nginxWebUI在内, 一体化管理与运行nginx. 

1.安装docker容器环境

Ubuntu:

```
apt install docker.io
```

Centos:

```
yum install docker
```

2.拉取镜像: 

```
docker pull cym1102/nginxwebui:latest
```

3.启动容器: 

```
docker run -itd -v /home/nginxWebUI:/home/nginxWebUI -e BOOT_OPTIONS="--server.port=8080" --privileged=true --net=host  cym1102/nginxwebui:latest /bin/bash
```

注意: 

1. 启动容器时请使用--net=host参数, 直接映射本机端口, 因为内部nginx可能使用任意一个端口, 所以必须映射本机所有端口. 

2. 容器需要映射路径/home/nginxWebUI:/home/nginxWebUI, 此路径下存放项目所有数据文件, 包括数据库, nginx配置文件, 日志, 证书等, 升级镜像时, 此目录可保证项目数据不丢失. 请注意备份.

3. -e BOOT_OPTIONS 参数可填充java启动参数, 可以靠此项参数修改端口号

--server.port 占用端口, 不填默认以8080端口启动

4. 日志默认存放在/home/nginxWebUI/log/nginxWebUI.log

另: 使用docker-compose时配置文件如下

```
version: "3.2"
services:
  nginxWebUi-server:
    image: cym1102/nginxwebui:2.7.8
    volumes:
      - type: bind
        source: "/home/nginxWebUI"
        target: "/home/nginxWebUI"
    environment:
      BOOT_OPTIONS: "--server.port=8080"
    privileged: true
    network_mode: "host"

```

#### 编译说明

1. 使用maven编译打包

```
mvn clean package
```

2. 使用docker构建镜像

```
docker build -t nginxwebui:2.7.8 .
```

#### 添加开机启动


1. 编辑service配置

```
vim /etc/systemd/system/nginxwebui.service
```

```
[Unit]
Description=NginxWebUI
After=syslog.target
After=network.target
 
[Service]
Type=simple
User=root
Group=root
WorkingDirectory=/home/nginxWebUI
ExecStart=/usr/bin/java -jar /home/nginxWebUI/nginxWebUI.jar
Restart=always
 
[Install]
WantedBy=multi-user.target
```

之后执行

```
systemctl daemon-reload
systemctl enable nginxwebui.service
systemctl start nginxwebui.service
```

#### 使用说明

打开 http://xxx.xxx.xxx.xxx:8080 进入主页

![输入图片说明](http://www.nginxwebui.cn/img/login.jpeg "login.jpg")

登录页面, 第一次打开会要求初始化管理员账号

![输入图片说明](http://www.nginxwebui.cn/img/admin.jpeg "admin.jpg")

进入系统后, 可在管理员管理里面添加修改管理员账号

![输入图片说明](http://www.nginxwebui.cn/img/http.jpeg "http.jpg")

在http参数配置中可以配置nginx的http项目,进行http转发, 默认会给出几个常用配置, 其他需要的配置可自由增删改查. 可以勾选开启日志跟踪, 生成日志文件。

![输入图片说明](http://www.nginxwebui.cn/img/tcp.jpeg "tcp.jpg")

在TCP参数配置中可以配置nginx的stream项目参数, 大多数情况下可不配.

![输入图片说明](http://www.nginxwebui.cn/img/server.jpeg "server.jpg")

在反向代理中可配置nginx的反向代理即server项功能, 可开启ssl功能, 可以直接从网页上上传pem文件和key文件, 或者使用系统内申请的证书, 可以直接开启http转跳https功能，也可开启http2协议

![输入图片说明](http://www.nginxwebui.cn/img/upstream.jpeg "upstream.jpg")

在负载均衡中可配置nginx的负载均衡即upstream项功能, 在反向代理管理中可选择代理目标为配置好的负载均衡

![输入图片说明](http://www.nginxwebui.cn/img/html.jpeg "html.jpg")

在html静态文件上传中可直接上传html压缩包到指定路径,上传后可直接在反向代理中使用,省去在Linux中上传html文件的步骤

![输入图片说明](http://www.nginxwebui.cn/img/cert.jpeg "cert.jpg")

在证书管理中可添加证书, 并进行签发和续签, 开启定时续签后, 系统会自动续签即将过期的证书, 注意:证书的签发是用的acme.sh的dns模式, 需要配合阿里云的aliKey和aliSecret来使用. 请先申请好aliKey和aliSecret

![输入图片说明](http://www.nginxwebui.cn/img/bak.jpeg "bak.jpg")

备份文件管理, 这里可以看到nginx.cnf的备份历史版本, nginx出现错误时可以选择回滚到某一个历史版本

![输入图片说明](http://www.nginxwebui.cn/img/conf.jpeg "conf.jpg")

最终生成conf文件,可在此进行进一步手动修改,确认修改无误后,可覆盖本机conf文件,并进行效验和重启, 可以选择生成单一nginx.conf文件还是按域名将各个配置文件分开放在conf.d下
 
![输入图片说明](http://www.nginxwebui.cn/img/remote.jpeg "remote.jpg")

远程服务器管理, 如果有多台nginx服务器, 可以都部署上nginxWebUI, 然后登录其中一台, 在远程管理中添加其他服务器的ip和用户名密码, 就可以在一台机器上管理所有的nginx服务器了.

提供一键同步功能, 可以将某一台服务器的数据配置和证书文件同步到其他服务器中

#### 接口开发

本系统提供http接口调用, 只要在启动参数添加 --knife4j.production=false, 再打开 http://xxx.xxx.xxx.xxx:8080/doc.html 即可查看knife4j接口页面.

接口调用需要在header中添加token, 其中token的获取需要在管理员管理中, 打开用户的接口调用权限, 然后通过用户名密码调用获取token接口, 才能得到token, 然后在knife4j的文档管理中设置全局token. 

注意: 参数说明中, 带*前缀的均是必填项

![输入图片说明](http://www.nginxwebui.cn/img/knife4j.png "knife4j.png")

#### 找回密码

如果忘记了登录密码，可按如下教程找回密码

1. 安装sqlite3命令

```
apt install sqlite3
```

2. 读取sqlite.db文件

```
sqlite3 /home/nginxWebUI/sqlite.db
```

3. 查找admin表

```
select * from admin;
```

4. 退出sqlite3

```
.quit
```
