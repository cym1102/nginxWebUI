# nginxWebUI

#### 介绍
nginx网页配置工具

QQ技术交流群: 1106758598

#### 功能说明

本项目可以使用WebUI配置nginx的各项功能, 包括http协议转发, tcp协议转发, 反向代理, 负载均衡, ssl证书自动申请、续签、配置等, 最终生成nginx.conf文件并覆盖nginx的默认配置文件, 完成nginx的最终功能配置. 

本项目可管理多个nginx服务器集群, 随时一键切换到对应服务器上进行nginx配置, 也可以一键将某台服务器配置同步到其他服务器, 方便集群管理

nginx本身功能复杂, 本项目并不能涵盖nginx所有功能, 只能配置常用功能, 更高级的功能配置仍然需要在最终生成的nginx.conf中进行手动编写。

部署此项目后, 配置nginx再也不用上网各种搜索, 再也不用手动申请和配置ssl证书, 只需要在本项目中进行增删改查就可方便的配置nginx。

#### 技术说明

本项目是基于springBoot的web系统, 数据库使用sqlite, 因此服务器上不需要安装任何数据库

其中orm使用了本人自己开源的sqlHelper项目作为orm, 使用sqlite作为数据库, 项目启动时会释放一个.sqlite.db到系统用户文件夹中, 注意进行备份

> sqlHelper是一个可以像mongodb一样使用sql数据库的orm, 解放开发者对sql数据库表结构的维护工作, 支持sqlite, mysql, postgresql三种数据库, 有兴趣的可以了解一下 https://gitee.com/cym1102/sqlHelper

本系统通过Let's encrypt申请证书, 使用acme.sh脚本进行自动化申请和续签, 开启续签的证书将在每天凌晨2点进行续签, 只有超过60天的证书才会进行续签. 只支持在linux下签发证书.

因为申请证书必须要使用80端口, 因此在申请和续签的时候nginx将会短暂关闭，请注意。

添加tcp/ip转发配置支持时, 一些低版本的nginx可能需要重新编译，通过添加–with-stream参数指定安装stream模块才能使用, 但在ubuntu 18.04下, 官方软件库中的nginx已经带有stream模块, 不需要重新编译. 本系统如果配置了tcp转发项的话, 会自动引入ngx_stream_module.so的配置项, 如果没有开启则不引入, 最大限度优化ngnix配置文件. 

#### 安装说明
以Ubuntu操作系统为例, 以下命令请使用root账户权限执行

1.安装java运行环境和nginx

```
apt install openjdk-11-jdk
apt install nginx
```

2.下载最新版发行包jar,下载地址https://gitee.com/cym1102/nginxWebUI/releases

码云的服务器下载较慢, CDN地址(可使用wget下载): 

```
wget https://craccd.oss-cn-beijing.aliyuncs.com/nginxWebUI-1.1.7.jar
```

有新版本只需要修改路径中的版本即可

启动命令

```
nohup java -jar -Xms64m -Xmx64m nginxWebUI-1.1.7.jar --server.port=8080 --spring.database.sqlite-name=.sqlite > nginxWebUI.log &
```

参数说明(非必填)

-Xms64m 起始分配内存数

-Xmx64m 最大分配内存数

--server.port 占用端口, 默认以8080端口启动

--spring.database.sqlite-name sqlite文件释放后文件名, 默认释放为.sqlite, 修改此项可在一台机器上部署多个nginxWebUI

#### 使用说明

打开http://xxx.xxx.xxx.xx:8080
默认登录名密码为admin/admin

![输入图片说明](https://images.gitee.com/uploads/images/2020/0515/165140_ee1bd853_1100382.jpeg "login.jpg")

![输入图片说明](https://images.gitee.com/uploads/images/2020/0518/171206_4314a45a_1100382.jpeg "admin.jpg")

进入系统后,可在管理员管理里面添加修改管理员账号

![输入图片说明](https://images.gitee.com/uploads/images/2020/0518/171217_a1c2effc_1100382.jpeg "http.jpg")

在http转发配置中可以配置nginx的http项目,进行http转发,默认会给出几个常用配置,其他需要的配置可自由增删改查

![输入图片说明](https://images.gitee.com/uploads/images/2020/0518/171234_e767f5a6_1100382.jpeg "stream.jpg")

在tcp转发配置中可以配置nginx的steam项目参数,进行tcp转发

![输入图片说明](https://images.gitee.com/uploads/images/2020/0518/171323_8865fca3_1100382.jpeg "server.jpg")

在反向代理中可配置nginx的反向代理即server项功能, 可开启ssl功能, 可以直接从网页上上传pem文件和key文件, 或者使用内置申请的证书, 可以直接开启http转跳https功能

![输入图片说明](https://images.gitee.com/uploads/images/2020/0518/171358_95e7f619_1100382.jpeg "upstream.jpg")

在负载均衡中可配置nginx的负载均衡即upstream项功能, 在反向代理管理中可选择代理目标为配置好的负载均衡

![输入图片说明](https://images.gitee.com/uploads/images/2020/0518/171442_fa2cd767_1100382.jpeg "cert.jpg")

在证书管理中可添加证书, 并进行签发和续签, 开启定时续签后, 系统会自动续签即将过期的证书 

![输入图片说明](https://images.gitee.com/uploads/images/2020/0518/171555_802d926d_1100382.jpeg "bak.jpg")

备份文件管理, 这里可以看到nginx.cnf的备份历史版本, nginx出现错误时可以选择回滚到某一个历史版本

![输入图片说明](https://images.gitee.com/uploads/images/2020/0521/160033_74601d0e_1100382.jpeg "QQ截图20200521160020.jpg")

最终生成conf文件,可在此进行进一步手动修改,确认修改无误后,可覆盖本机conf文件,并进行效验和重启, 可以选择生成单一nginx.conf文件还是按域名将各个配置文件分开放在conf.d下

![输入图片说明](https://images.gitee.com/uploads/images/2020/0520/150617_c558509c_1100382.jpeg "remote.jpg")

远程服务器管理, 如果有多台nginx服务器, 可以都部署上nginxWebUI, 然后登录其中一台, 在远程管理中添加其他服务器的ip和用户名密码, 就可以在一台机器上管理所有的nginx服务器了.

提供一键同步功能, 可以将某一台服务器的数据配置和证书文件同步到其他服务器中