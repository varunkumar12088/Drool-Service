package com.academy.drool.controller;

import com.academy.drool.domain.ProductConfig;
import com.academy.drool.domain.TestCDR;
import com.academy.drool.service.DroolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class DroolController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DroolController.class);

    @Autowired
    private DroolService droolService;


    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public ResponseEntity<?> createProduct(@RequestBody ProductConfig productConfig){
        LOGGER.debug("product config : {}", productConfig);
        droolService.createRules(productConfig);
        return ResponseEntity.ok("Configured product");
    }

    @RequestMapping(value = "/execute", method = RequestMethod.POST)
    public ResponseEntity<?> executeRule(@RequestBody String cdr){
        TestCDR testCDR = droolService.executeRule(cdr);
        return ResponseEntity.ok(testCDR.getCdr());
    }
}
