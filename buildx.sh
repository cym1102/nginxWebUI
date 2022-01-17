#!/usr/bin/env bash

## 需要安装好docker，并登陆docker login
## 需要安装好docker-buildx：https://docs.docker.com/buildx/working-with-buildx/
## 如需自动推送readme至dockerhub，需要安装docker-pushrm：https://github.com/christian-korneck/docker-pushrm
## Dockerfile同目录下运行此脚本
## 如果在同目录下存在maven的镜像加速配置文件settings.xml，也会作为编译时的加速配置

set -o pipefail

## 基本信息
repo="cym1102/nginxwebui"
arch="linux/386,linux/amd64,linux/arm64,linux/arm/v7"
ver=$(cat pom.xml | grep -A1 nginxWebUI | grep version | grep -oP "\d+\.\d+\.\d+")
echo "构建镜像：$repo"
echo "构建架构：$arch"
echo "构建版本：$ver"

## 编译jar文件
echo "3秒后开始编译jar文件..."
sleep 3
mvn clean package

## 准备跨平台构建环境
echo "准备跨平台构建环境"
docker pull tonistiigi/binfmt
docker run --privileged --rm tonistiigi/binfmt --install all
docker buildx create --name builder --use 2>/dev/null || docker buildx use builder
docker buildx inspect --bootstrap

## 多平台镜像同时构建并推送
echo "构建镜像并推送至Docker Hub"
docker buildx build \
    --cache-from "type=local,src=/tmp/.buildx-cache" \
    --cache-to "type=local,dest=/tmp/.buildx-cache" \
    --platform "$arch" \
    --tag ${repo}:${ver} \
    --tag ${repo}:latest \
    --push \
    .

## 推送readme.md至dockerhub，需要docker-pushrm
# docker pushrm $repo
