package com.elasticsearch.cdc.kafka;

import com.alibaba.fastjson2.JSON;
import com.elasticsearch.cdc.CDCListener;
import com.elasticsearch.cdc.kafka.dto.KafkaDto;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;
import java.util.concurrent.Future;

public class KafkaProducter {

    private static final Logger logger = LogManager.getLogger(KafkaProducter.class);

    private String kafkaNodes;


    private String kafkaTopic;


    public KafkaProducter(String kafkaNodes, String kafkaTopic) {
        this.kafkaNodes = kafkaNodes;
        this.kafkaTopic = kafkaTopic;
    }



    public void send(String indexName, KafkaDto dto) {
        logger.warn("---------kafka节点配置----------");
        logger.warn("节点:"+kafkaNodes);
        logger.warn("topic:"+kafkaTopic);
        logger.warn("--------------------------");

        if (kafkaTopic != null && kafkaNodes !=null) {

            Properties conf = new Properties();
            conf.setProperty(ProducerConfig.ACKS_CONFIG, "0");
            conf.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaNodes);
            conf.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            conf.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            Thread.currentThread().setContextClassLoader(null);
            KafkaProducer<String, String> producer = new KafkaProducer<String, String>(conf);

            ProducerRecord<String, String> msg = new ProducerRecord<String, String>(kafkaTopic, indexName, JSON.toJSONString(dto));
            Future<RecordMetadata> send = producer.send(msg, new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    if (exception != null) {
                        logger.error("回调:", exception);
                    }
                }
            });
        }else {
            logger.warn("kafka 节点配置未配置");
        }


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
