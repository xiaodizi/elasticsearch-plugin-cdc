package com.elasticsearch.cdc.kafka.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KafkaDto {

    private String indexName;
    private String data;
    private String operationType;
    private String id;
}
