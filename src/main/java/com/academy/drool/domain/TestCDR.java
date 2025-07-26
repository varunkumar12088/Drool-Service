package com.academy.drool.domain;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.ToString;


@Data
@ToString
public class TestCDR {

    private ObjectNode cdr;

    public TestCDR(ObjectNode cdr){
        this.cdr = cdr;
    }

}
