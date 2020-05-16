# nginxWebUI

#### 介绍
nginx网页配置工具


#### 功能说明

本项目可以使用WebUI配置nginx的各项功能, 包括http协议转发, tcp协议转发, 反向代理, 负载均衡, ssl证书自动申请、续签、配置等, 最终生成nginx.conf文件并覆盖nginx的默认配置文件, 完成nginx的最终功能配置. 

nginx本身功能复杂, 本项目并不能涵盖nginx所有功能, 只能配置常用功能, 更高级的功能配置仍然需要在最终生成的nginx.conf中进行手动编写。

部署此项目后, 配置nginx再也不用上网各种搜索, 再也不用手动申请和配置ssl证书, 只需要在本项目中进行增删改查就可方便的配置nginx。

#### 技术说明

本项目是基于springBoot的web系统, 数据库使用sqlite, 因此服务器上不需要安装任何数据库

其中orm使用了本人自己开源的sqlHelper项目作为orm, 使用sqlite作为数据库, 项目启动时会释放一个.sqlite.db到系统用户文件夹中, 注意进行备份

> sqlHelper是一个可以像mongodb一样使用sql数据库的orm, 解放开发者对sql数据库表结构的维护工作, 支持sqlite, mysql, postgresql三种数据库, 有兴趣的可以了解一下 https://gitee.com/cym1102/sqlHelper

本系统通过Let's encrypt申请证书, 使用acme.sh脚本进行自动化申请, 开启续签的证书将在每天凌晨2点进行续签, 只有超过60天的证书才会进行续签.

#### 安装说明
以Ubuntu操作系统为例, 以下命令请使用root账户权限执行

1.安装java运行环境

```
apt install openjdk-11-jdk
```

2.下载最新版发行包jar,下载地址https://gitee.com/cym1102/nginxWebUI/releases

启动命令

nohup java -jar nginxWebUI-1.0.0.jar --server.port=8080 > nginxWebUI.log &

如果不加--server.port=xxxx, 默认以8080端口启动

#### 使用说明

打开http://xxx.xxx.xxx.xx:8080
默认登录名密码为admin/admin

![输入图片说明](https://images.gitee.com/uploads/images/2020/0515/165140_ee1bd853_1100382.jpeg "login.jpg")

![输入图片说明](https://images.gitee.com/uploads/images/2020/0515/165148_c9f7149c_1100382.jpeg "admin.jpg")

进入系统后,可在管理员管理里面添加修改管理员账号

![输入图片说明](https://images.gitee.com/uploads/images/2020/0515/165203_30d187ee_1100382.jpeg "http.jpg")

在http转发配置中可以配置nginx的http项目,进行http转发,默认会给出几个常用配置,其他需要的配置可自由增删改查

![输入图片说明](https://images.gitee.com/uploads/images/2020/0515/165301_bb31fafa_1100382.jpeg "stream.jpg")

在TCP转发配置中可以配置nginx的stream项目,进行tcp/ip转发

![输入图片说明](https://images.gitee.com/uploads/images/2020/0515/165421_c47d02bb_1100382.jpeg "sever.jpg")

在反向代理中可配置nginx的反向代理即server项功能,可开启ssl功能,可以直接从网页上上传pem文件和key文件,或者使用内置申请的证书,可以直接开启http转跳https功能,用户直接访问http会转跳到https

![输入图片说明](https://images.gitee.com/uploads/images/2020/0515/165523_dbe27513_1100382.jpeg "upstream.jpg")

在负载均衡中可配置nginx的负载均衡即upstream项功能,在反向代理中可选择代理目标为负载均衡项

![输入图片说明](https://images.gitee.com/uploads/images/2020/0515/165543_71e210e2_1100382.jpeg "ca.jpg")

在证书管理中可添加证书, 并进行签发和续签. 

因为申请证书必须要使用80端口, 因此在申请和续签的时候nginx将会短暂关闭，请注意。

![输入图片说明](https://images.gitee.com/uploads/images/2020/0515/170135_30539807_1100382.jpeg "conf.jpg")

最终生成conf文件,可在此进行进一步手动修改,确认修改无误后,可覆盖本机conf文件,并进行效验和重启

