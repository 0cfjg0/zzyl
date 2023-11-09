FROM openjdk:11.0-jre-buster
LABEL maintainer="itheima@itcast.cn"
ENV JAVA_OPTS=""
# 设定时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ADD target/zzyl-web.jar /app.jar

ENTRYPOINT ["sh","-c","java  -jar $JAVA_OPTS /app.jar"]