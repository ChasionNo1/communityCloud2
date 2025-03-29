仿牛客网项目-微服务版
1、版本约定
<spring.boot.version>3.0.2</spring.boot.version>
<spring-cloud.version>2022.0.0</spring-cloud.version>
<spring-cloud-alibaba.version>2022.0.0.0-RC2</spring-cloud-alibaba.version>
nacos:2.2.3
es:8.5.3
kafka:2.13-3.7.0
sentinel:1.8.6

2、主要模块
<modules>
    <module>cloud-commons-api</module>
    <module>cloud-gateway</module>
    <module>cloud-post</module>
    <module>cloud-user</module>
    <module>cloud-web-ui</module>
    <module>cloud-comment</module>
    <module>cloud-message</module>
</modules>
cloud-commons-api：存放统一返回接口和openfeign接口

cloud-gateway：网关模块

cloud-post：帖子模块

cloud-user：用户模块

cloud-comment：评论模块

cloud-message：消息模块

cloud-web-ui：页面渲染模块

工作流程：

请求--->nginx--->gateway----->web-ui---openfeign-->user/post/comment
