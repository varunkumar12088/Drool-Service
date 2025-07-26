package com.academy.drool.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "rules")
public class Drool {

    @Id
    private String id;
    private EngineId engineId;
    private String productName;
    private String when;
    private String then;
}
