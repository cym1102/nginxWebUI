# nginxWebUI

#### Introduce
Nginx web page configuration tool

QQ Group1: 1106758598
QQ Group2: 560797506

Email: cym1102@qq.com

official website: http://www.nginxwebui.cn

WeChat Donate: 

<img src="http://www.nginxwebui.cn/img/weixin.png"  height="200" width="200">

#### Function description

NginxWebuUI is a graphical management tool for nginx configuration. You can use web pages to quickly configure various functions of nginx, including HTTP forwarding, TCP forwarding, reverse proxy, load balancing, static HTML server, SSL certificate automatic application, renewal, configuration, etc.  Nginx. conf file can be generated after configuration, and nginx can be controlled to use this file for startup and reload, complete the graphical control of nginx closed loop.  
 
The nginx webui allows you to manage multiple Nginx server clusters. You can switch to the corresponding server for nginx configuration at any time. You can also synchronize the configuration of a server to other servers with one click, facilitating cluster management.  
 
The nginx web user interface (webui) does not cover all nginx functions, but covers 90% of the daily nginx configuration. If nginx configuration items are not covered by the platform, you can use custom parameter templates to generate unique configuration parameters in the CONF file.  
 
After the deployment of this project, the configuration of nginx no longer need to search the web configuration code, no longer need to manually apply for and configure SSL certificates, just need to add, delete, change and check in this project can easily configure and start nginx.  

```
Demo address: http://test.nginxwebui.cn:8080
User: admin
password: Admin123
```

#### Technical note

This project is a Web system based on springBoot. The database use SQLite, so there is no need to install any database on the server.

sqlite.db will be released into the system user folder when the project starts, so pay attention to backup.

This system applies for the certificate through Let's ENCRYPT and USES acme.sh script to automatically apply for and renew the certificate. Once the certificate is renewed, it will be renewed at 2 am every day, and only certificates exceeding 60 days will be renewed.

When adding TCP/IP forwarding configuration support, some lower versions of Nginx may need to be recompiled,You can install the stream module by adding the -with-stream parameter, but under Ubuntu 18.04, the nginx in the official software library already has the stream module, which does not need to be recompiled. If the TCP forwarding item is configured in this system, the configuration item of ngx_stream_module.so will be introduced automatically, and the configuration file of Ngnix will be optimized to the maximum.

#### jar installation instructions 
Take the Ubuntu operating system, for example.

 **Note: This project needs to run the system command under the root user, which is very easy to be exploited by hackers. Please be sure to change the password to complex password**

1.Install the Java runtime environment and Nginx

Ubuntu:

```
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
Download the JDK installation package https://www.oracle.com/java/technologies/downloads/
Download the nginx http://nginx.org/en/download.html
Configure the JAVA runtime environment 
JAVA_HOME : JDK installation directory
Path : JDK installation directory\bin
reboot
```

2.Download the latest release of the distribution jar

```
Linux: wget -O /home/nginxWebUI/nginxWebUI.jar http://file.nginxwebui.cn/nginxWebUI-2.8.3.jar

Windows: Download directly from your browser http://file.nginxwebui.cn/nginxWebUI-2.8.3.jar
```

With a new version, you just need to change the version in the path

3.Start program

```
Linux: nohup java -jar -Xmx64m /home/nginxWebUI/nginxWebUI.jar --server.port=8080 --project.home=/home/nginxWebUI/ > /dev/null &

Windows: java -jar -Xmx64m D:/home/nginxWebUI/nginxWebUI.jar --server.port=8080 --project.home=D:/home/nginxWebUI/
```

Parameter description (both non-required)

-Xmx64m Maximum number of memory allocated

--server.port Occupied port, default starts at port 8080

--project.home Project profile directory for database files, certificate files, logs, etc. Default is /home/nginxwebui/

--spring.database.type=mysql Use other databases, not filled with native SQLite, options include mysql and postgresql

--spring.datasource.url=jdbc:mysql://ip:port/nginxwebui Databases url

--spring.datasource.username=root  Databases user

--spring.datasource.password=pass  Databases password

--knife4j.production=false  false:Open interface debugging page. true:Close interface debugging page.

Note that the command ends with an & to indicate that the project is running in the background

#### docker installation instructions 

Docker image supports x86/x86_64/arm64/arm v7/arm v6/ppc64 platforms. Note that an & sign is added at the end of the command, indicating that the docker image of this project has been produced by the background operation of the project, including nginx and nginxWebUI, for integrated management and operation of Nginx.

1.Install the Docker environment

ubuntu:

```
apt install docker.io
```

centos:

```
yum install docker
```

2.Download images:

```
docker pull cym1102/nginxwebui:latest
```

3.start container

```
docker run -itd -v /home/nginxWebUI:/home/nginxWebUI -e BOOT_OPTIONS="--server.port=8080" --privileged=true --net=host  cym1102/nginxwebui:latest
```

notice: 

1. When you start the container, use the --net=host parameter to map the native port directly, because internal Nginx may use any port, so you must map all the native ports. 

2. Container need to map path/home/nginxWebUI:/home/nginxWebUI, this path for a project all data files, including database, nginx configuration files, log, certificate, etc., and updates the mirror, this directory to ensure that project data is not lost. Please note that backup.

3. -e BOOT_OPTIONS Parameter to populate the Java startup parameter, which can be used to modify the port number

--server.port Occupied port, do not fill the default port 8080 startup

4. Logs are stored by default /home/nginxWebUI/log/nginxWebUI.log

moreover: The following configuration file is used when using docker-compose

```
version: "3.2"
services:
  nginxWebUi-server:
    image: cym1102/nginxwebui:latest
    volumes:
      - type: bind
        source: "/home/nginxWebUI"
        target: "/home/nginxWebUI"
    environment:
      BOOT_OPTIONS: "--server.port=8080"
    privileged: true
    network_mode: "host"

```

#### Compile 

Compile the package with Maven

```
mvn clean package
```

Compile the image with Docker

```
docker build -t nginxwebui:latest .
```


#### Add boot up run

1. Edit service file

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

Then execute

```
systemctl daemon-reload
systemctl enable nginxwebui.service
systemctl start nginxwebui.service
```

#### instructions

open http://xxx.xxx.xxx.xxx:8080 Enter the main page

![输入图片说明](http://www.nginxwebui.cn/img/login.jpeg "login.jpg")

The login page, opened for the first time, asks to initialize the administrator account

![输入图片说明](http://www.nginxwebui.cn/img/admin.jpeg "admin.jpg")

After entering the system, you can add and modify the administrator account in the administrator management

![输入图片说明](http://www.nginxwebui.cn/img/http.jpeg "http.jpg")

In the HTTP parameters can be configured in the configuration of nginx HTTP project forward HTTP, the default will give several commonly used configuration, other configuration are free to add and delete. You can check the open log to track and generate log.

![输入图片说明](http://www.nginxwebui.cn/img/tcp.jpeg "tcp.jpg")

Nginx's Stream project parameters can be configured in the TCP parameter configuration, but in most cases they are not.

![输入图片说明](http://www.nginxwebui.cn/img/server.jpeg "server.jpg")

In the reverse proxy, the reverse proxy of Nginx, namely the Server item function, can be configured to enable SSL function, can directly upload PEM file and key file from the web page, or use the certificate applied in the system, can directly enable HTTP switch HTTPS function, or can open http2 protocol

![输入图片说明](http://www.nginxwebui.cn/img/upstream.jpeg "upstream.jpg")

In load balancing, the upstream function of Nginx can be configured. In reverse agent management, the configured load balancing agent target can be selected

![输入图片说明](http://www.nginxwebui.cn/img/html.jpeg "html.jpg")

In the HTML static file upload can be directly uploaded HTML compression package to the specified path, after uploading can be directly used in the reverse proxy, save the steps of uploading HTML files in Linux

![输入图片说明](http://www.nginxwebui.cn/img/cert.jpeg "cert.jpg")

In the certificate management, you can add the certificate, issue and renew it. After the periodic renewal is started, the system will automatically renew the certificate which will expire soon. Note: the certificate is issued using the DNS mode of Acme. sh, and it needs to be used together with aliKey and aliSecret of Aliyun

![输入图片说明](http://www.nginxwebui.cn/img/bak.jpeg "bak.jpg")

Backup file management. Here you can see the backup history version of Nginx.cnF. If an error occurs in Nginx, you can choose to roll back to a certain history version

![输入图片说明](http://www.nginxwebui.cn/img/conf.jpeg "conf.jpg")

Finally, the conF file can be generated, which can be further modified manually. After the modification is confirmed to be correct, the native conF file can be overwritten, and the effectiveness and restart can be carried out. You can choose to generate a single Nginx.conf file or separate each configuration file under conF.d by domain name
 
![输入图片说明](http://www.nginxwebui.cn/img/remote.jpeg "remote.jpg")

Remote server management. If you have multiple Nginx servers, you can deploy nginxWebUI, log in to one of them, add the IP and username and password of other servers to the remote management, and then you can manage all Nginx servers on one machine.

Provides one-click synchronization to synchronize data configuration and certificate files from one server to another

#### Interface development 

This system provides the HTTP interface to invoke, as long as the boot parameters added --knife4j.production=false, then open the page http://xxx.xxx.xxx.xxx:8080/doc.html to view the knife4j interface.  

Interface invocation needs to add a token in the header. To obtain the token, you need to open the interface invocation permission of the user in the administrator management, and then invoke the token interface through the user name and password to get the token. Then set the global token in the document management of Knife4j.  

Note: In the parameter description, all fields with * prefix are required.


![输入图片说明](http://www.nginxwebui.cn/img/knife4j.png "knife4j.png")

#### Forgot Password

If you forget your login password, follow the following tutorial to retrieve it

1. install sqlite3 (docker image has already included)

```
apt install sqlite3
```

2. read sqlite.db

```
sqlite3 /home/nginxWebUI/sqlite.db
```

3. search admin table

```
select * from admin;
```

4. exit sqlite3

```
.quit
```


