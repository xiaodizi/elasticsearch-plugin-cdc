package com.elasticsearch.cdc;

import com.elasticsearch.cdc.kafka.KafkaProducter;
import com.elasticsearch.cdc.kafka.dto.KafkaDto;
import lombok.Value;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexModule;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.shard.IndexingOperationListener;
import org.elasticsearch.index.shard.ShardId;
import org.json.JSONObject;

import java.util.function.Consumer;

public class CDCListener implements IndexingOperationListener, Consumer<Boolean> {


    private volatile boolean needInit = true;

    private final IndexModule indexModule;


    private String kafkaNodes;
    private String kafkaTopic;


    public CDCListener(IndexModule indexModule) {
        kafkaNodes = indexModule.getSettings().get(PluginSettings.KAFKA_CDC_NODES);
        kafkaTopic = indexModule.getSettings().get(PluginSettings.KAFKA_CDC_TOPIC);
        this.indexModule = indexModule;
    }


    private static final Logger log = LogManager.getLogger(CDCListener.class);


    @Override
    public void postDelete(ShardId shardId, Engine.Delete delete, Engine.DeleteResult result) {
        log.info("----------------------索引删除操作开始------------------------");
        String indexName = shardId.getIndex().getName();
        log.info("----------Delete start------------");
        log.info("删除索引名称:"+indexName);
        String deleteId = delete.id();
        log.info("删除ID:"+deleteId);
        boolean isFound = result.isFound();
        log.info("是否找到了数据："+isFound);
        if (result.isFound()){
            KafkaProducter kafkaProducter=new KafkaProducter(kafkaNodes,kafkaTopic);
            kafkaProducter.send(indexName,KafkaDto.builder()
                            .indexName(indexName)
                            .data("")
                            .id(deleteId)
                            .operationType("DELETE")
                    .build());
        }else {
            log.warn("未找到删除的数据，doc的ID为："+deleteId);
        }
        log.info("----------Delete end------------");
    }

    @Override
    public void postIndex(ShardId shardId, Engine.Index index, Engine.IndexResult result) {

        System.out.println("----------------------索引操作开始------------------------");
        String indexName=shardId.getIndex().getName();

        log.info("索引名称:"+indexName);

        log.info("--------------------------Index start----------------------------");
        String utf8ToString = index.parsedDoc().source().utf8ToString();
        boolean created = result.isCreated();

        String id = index.id();
        log.info("数据ID："+id);
        String operationType="";
        if (created == true){
            operationType = "CREATE";
        }else {
            operationType = "UPDATE";
        }

        try {
            JSONObject content=new JSONObject(utf8ToString);
            KafkaProducter kafkaProducter=new KafkaProducter(kafkaNodes,kafkaTopic);
            kafkaProducter.send(indexName, KafkaDto.builder()
                            .data(utf8ToString)
                            .operationType(operationType)
                            .indexName(indexName)
                            .id(id)
                    .build());
            log.info("数据:"+content);
            log.info("操作类型:"+created);
        } catch (Exception e) {
            log.error("postIndex异常:",e);
        }


        log.info("--------------------------Index end----------------------------");

    }


    @Override
    public void accept(Boolean cdcEnabled) {
        if (cdcEnabled) {
            needInit = true;
        }
    }
}
