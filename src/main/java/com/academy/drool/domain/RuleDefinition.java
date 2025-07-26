package com.academy.drool.domain;

import lombok.Data;

@Data
public class RuleDefinition {

    private Condition condition;
    private Action action;
}
