package com.academy.drool.domain;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class ProductConfig {

    private String udrType;
    private String udrSubType;
    private String name;
    private String skuName;
    private String message;
    private List<RuleDefinition> definitions;


}
