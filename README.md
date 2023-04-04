# elasticsearch-plugin-cdc 插件

这是目前没有在生产环境验证的插件，使用请慎重。

elastisearch 7.9.3 版本验证通过

elastisearch 7.12.1 版本验证通过 

又向上升级了几个版本做尝试，不过高版本的 elasticsearch服务端使用了 lz4-java-1.8.0.jar 的里边的压缩算法，而 kafka的客户端也使用了lz4-java-1.8.0.jar里的压缩算法，所以包冲突了。服务端里的包的无法排除掉，就把kafka客户端里的包排除掉了，但是kafka反而不正常了。

在考虑高版本是不是应该把数据丢到别的地方去。


不同版本的 elasticsearch 对应不同版本的分支。

其他版本不敢保证是否能够正常使用


# elasticsearch 7.12.1 版本安装特殊说明

1.不要使用内置jdk，需要单独安装jdk，配置

```shell
export ES_JAVA_HOME="JDK16 HOME 路径"
```

2.在jdk的Home目录下的conf/security/java.policy文件里，grant 下增加授权
```shell
    permission javax.management.MBeanServerPermission "createMBeanServer";
    permission java.security.SecurityPermission "setProperty.networkaddress.cache.ttl";
    permission java.util.PropertyPermission "es.networkaddress.cache.ttl", "read";
    permission java.util.PropertyPermission "es.networkaddress.cache.negative.ttl","read";
    permission java.security.SecurityPermission "setProperty.networkaddress.cache.negative.ttl";
    permission java.lang.RuntimePermission "createSecurityManager";
    permission java.lang.RuntimePermission "setSecurityManager";
    permission javax.management.MBeanPermission "*", "registerMBean";
    permission javax.management.MBeanTrustPermission "register";
```
3.再按照下边的操作正常安装和配置即可。

# 安装

1. 编译打包
   mvn clean package
2. 移除之前版本(如果之前安装过)
   ${elasticsearch_home}/bin/elasticsearch-plugin remove elasticsearch-plugin-cdc
3. 安装

   ```shell
   ${elasticsearch_home}/bin/elasticsearch-plugin install file:///${elasticsearch_cdc_home_dir}/target/releases/elasticsearch-plugin-cdc-1.0-SNAPSHOT.zip
   ```
4. 配置es的javax相关权限
   /etc/elasticsearch/jvm.options 文件加入相关java配置
   ```
   -Djava.security.policy=/your_elasticsearch_home/plugins/elasticsearch-plugin-cdc/plugin-security.policy
   ```
5. 在 elasticsearch.yml 增加两个配置

   ```shell
   kafka.cdc.topic: lei
   kafka.cdc.nodes: node01:9092  # 这就是kafka的broker列表
   ```
6. 重启elasticsearch 服务
