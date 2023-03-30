# elasticsearch-plugin-cdc 插件

这是目前没有在生产环境验证的插件，使用请慎重。

elastisearch 7.9.3 版本验证通过

其他版本不敢保证是否能够正常使用

# 安装

1. 编译打包
   mvn clean package
2. 移除之前版本(如果之前安装过)
   ${elasticsearch_home}/bin/elasticsearch-plugin remove elasticsearch-plugin-cdc
3. 安装

   ~~~shell
   ${elasticsearch_home}/bin/elasticsearch-plugin install file:///${elasticsearch_cdc_home_dir}/target/releases/elasticsearch-plugin-cdc-1.0-SNAPSHOT.zip
   ~~~
4. 配置es的javax相关权限
   /etc/elasticsearch/jvm.options 文件加入相关java配置
   -Djava.security.policy=/your_elasticsearch_home/plugins/elasticsearch-plugin-cdc/plugin-security.policy
5. 在 elasticsearch.yml 增加两个配置

   ~~~shell
   kafka.cdc.topic: lei
   kafka.cdc.nodes: node01:9092  # 这就是kafka的broker列表
   ~~~
6. 重启elasticsearch 服务
