package com.elasticsearch.cdc;

import com.elasticsearch.cdc.kafka.KafkaProducter;
import com.elasticsearch.cdc.kafka.dto.KafkaDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.IndexModule;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.shard.IndexingOperationListener;
import org.elasticsearch.index.shard.ShardId;
import org.json.JSONObject;


import java.util.function.Consumer;

public class CDCListener implements IndexingOperationListener, Consumer<Boolean> {

    private static final Logger logger = LogManager.getLogger(CDCListener.class);

    private volatile boolean needInit = true;

    private final IndexModule indexModule;


    private String kafkaNodes;
    private String kafkaTopic;


    public CDCListener(IndexModule indexModule) {
        kafkaNodes = indexModule.getSettings().get(PluginSettings.KAFKA_CDC_NODES);
        kafkaTopic = indexModule.getSettings().get(PluginSettings.KAFKA_CDC_TOPIC);
        this.indexModule = indexModule;
    }



    @Override
    public void postDelete(ShardId shardId, Engine.Delete delete, Engine.DeleteResult result) {
        logger.info("----------------------索引删除操作开始------------------------");
        String indexName = shardId.getIndex().getName();
        logger.info("----------Delete start------------");
        logger.info("删除索引名称:"+indexName);
        String deleteId = delete.id();
        logger.info("删除ID:"+deleteId);
        boolean isFound = result.isFound();
        logger.info("是否找到了数据："+isFound);
        if (result.isFound()){
            KafkaProducter producter=new KafkaProducter(kafkaNodes,kafkaTopic);
            producter.send(indexName,KafkaDto.builder()
                            .indexName(indexName)
                            .data("")
                            .id(deleteId)
                            .operationType("DELETE")
                    .build());
        }else {
            logger.info("未找到删除的数据，doc的ID为："+deleteId);
        }
        logger.info("----------Delete end------------");
    }

    @Override
    public void postIndex(ShardId shardId, Engine.Index index, Engine.IndexResult result) {

        logger.info("----------------------索引操作开始------------------------");
        String indexName=shardId.getIndex().getName();

        logger.info("索引名称:"+indexName);

        logger.info("--------------------------Index start----------------------------");
        String utf8ToString = index.parsedDoc().source().utf8ToString();
        boolean created = result.isCreated();

        String id = index.id();
        logger.info("数据ID："+id);
        String operationType="";
        if (created == true){
            operationType = "CREATE";
        }else {
            operationType = "UPDATE";
        }

        try {
            JSONObject content=new JSONObject(utf8ToString);
            KafkaProducter producter=new KafkaProducter(kafkaNodes,kafkaTopic);
            producter.send(indexName, KafkaDto.builder()
                            .data(utf8ToString)
                            .operationType(operationType)
                            .indexName(indexName)
                            .id(id)
                    .build());
            logger.info("数据:"+content);
            logger.info("操作类型:"+created);
        } catch (Exception e) {
            logger.error("postIndex 报错：",e);
        }


        logger.info("--------------------------Index end----------------------------");

    }


    @Override
    public void accept(Boolean cdcEnabled) {
        if (cdcEnabled) {
            needInit = true;
        }
    }
}
