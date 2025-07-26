package com.academy.drool.service.impl;

import com.academy.drool.domain.*;
import com.academy.drool.repository.DroolRepository;
import com.academy.drool.service.DroolService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.utils.KieHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DroolServiceImpl implements DroolService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DroolServiceImpl.class);

    @Autowired
    private DroolRepository droolRepository;

    @Autowired
    private ObjectMapper objectMapper;


    @Override
    public void createRules(ProductConfig productConfig) {
        LOGGER.debug("create the rules for productConfig {} ", productConfig);
        List<Drool> drools = new ArrayList<>();
        List<RuleDefinition> definitions = productConfig.getDefinitions();
        for(RuleDefinition ruleDefinition : definitions) {
            String whenCondition = getWhen(ruleDefinition);
            String action = getAction(ruleDefinition);
            Drool drool = new Drool();
            drool.setEngineId(EngineId.PRE);
            drool.setProductName(productConfig.getName());
            drool.setWhen(whenCondition);
            drool.setThen(action);
            drools.add(drool);
        }
        droolRepository.saveAll(drools);
    }

    @Override
    public TestCDR executeRule(String json) {
        try {
            ObjectNode cdr = this.objectMapper.readValue(json, ObjectNode.class);
            TestCDR testCDR = new TestCDR(cdr);

            StatelessKieSession statelessKieSession = buildStatelessKieSession();
            statelessKieSession.execute(testCDR);
            System.out.println("message: " + testCDR.getCdr().get("message"));
            System.out.println("skuName: " + cdr.get("skuName"));
            LOGGER.debug("CDR data : {}", testCDR.getCdr());
            LOGGER.debug("Test CDR data : {}", testCDR.getCdr());
            return testCDR;
        } catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    private String getWhen(RuleDefinition definition){
        Condition condition = definition.getCondition();
        List<Property> properties = condition.getProperties();
        StringBuilder sb = new StringBuilder("when\n\t$testCdr: TestCDR($cdrJson: cdr,");
        for (Property property : properties) {
            String key = property.getKey();
            String value = property.getValue();
            sb.append(" $cdrJson.get(\"");
            sb.append(key);
            sb.append("\"");
            sb.append(").asText().matches(");
            sb.append("\"(?i)");
            sb.append(value);
            sb.append("\")");
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(" )");
        return sb.toString();
    }

    private String getAction(RuleDefinition definition){
        Action action = definition.getAction();
        List<Property> properties = action.getProperties();
        StringBuilder sb = new StringBuilder("then\n");
        for (Property property : properties) {
            String setField = "\t$cdrJson.put(\"" + property.getKey()  + "\"" +
                    ", " + "\"" + property.getValue() + "\"" +");";
            sb.append(setField);
            sb.append("\n");
        }
        //sb.append("\tupdate($testCdr);\n");
        return sb.toString();
    }


    public String assembleDRL(List<Drool> rules) {
        StringBuilder drlBuilder = new StringBuilder();
        drlBuilder.append("package com.academy.rules;\n");
        drlBuilder.append("import com.fasterxml.jackson.databind.node.ObjectNode;\n");
        drlBuilder.append("import com.academy.drool.domain.*;\n");

        for(Drool drool : rules){
            drlBuilder.append("rule \"").append(drool.getId()).append("\"\n");
            drlBuilder.append(drool.getWhen()).append("\n");
            drlBuilder.append(drool.getThen()).append("\n");
            drlBuilder.append("end\n\n");
        }
        LOGGER.debug("final rules : {} ", drlBuilder);
        return drlBuilder.toString();
    }

    private StatelessKieSession buildStatelessKieSession() {
        List<Drool> drools = droolRepository.findAll();
        String drl = assembleDRL(drools);
        KieHelper kieHelper = new KieHelper();
        kieHelper.addContent(drl, ResourceType.DRL);
        return kieHelper.build().newStatelessKieSession();
    }
}
