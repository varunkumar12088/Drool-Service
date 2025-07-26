package com.academy.drool.service;

import com.academy.drool.domain.ProductConfig;
import com.academy.drool.domain.TestCDR;

public interface DroolService {

    void createRules(ProductConfig productConfig);

    TestCDR executeRule(String json);
}
