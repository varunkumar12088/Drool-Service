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
        LOGGER.debug("Creating rules for productConfig {}", productConfig);
        List<Drool> drools = new ArrayList<>();
        List<RuleDefinition> definitions = productConfig.getDefinitions();

        for (RuleDefinition ruleDefinition : definitions) {
            Drool drool = new Drool();
            drool.setEngineId(EngineId.PRE);
            drool.setProductName(productConfig.getName());
            drool.setWhen(buildWhen(ruleDefinition));
            drool.setThen(buildThen(ruleDefinition));
            drools.add(drool);
        }
        droolRepository.saveAll(drools);
    }

    @Override
    public TestCDR executeRule(String json) {
        try {
            ObjectNode cdr = objectMapper.readValue(json, ObjectNode.class);
            TestCDR testCDR = new TestCDR(cdr);

            StatelessKieSession statelessKieSession = buildStatelessKieSession();
            statelessKieSession.execute(testCDR);

            // Optionally, check and log updated fields
            LOGGER.debug("Output message: {}", testCDR.getCdr().get("message"));
            LOGGER.debug("Output skuName: {}", testCDR.getCdr().get("skuName"));
            return testCDR;
        } catch (Exception ex) {
            LOGGER.error("Error executing rule", ex);
            throw new RuntimeException(ex);
        }
    }

    // --- Utility: Builds 'when' clause ---
    private String buildWhen(RuleDefinition definition) {
        Condition condition = definition.getCondition();
        List<Property> properties = condition != null ? condition.getProperties() : null;

        StringBuilder sb = new StringBuilder("when\n\t$testCdr: TestCDR($cdrJson: cdr");

        if (properties != null && !properties.isEmpty()) {
            for (Property property : properties) {
                sb.append(", ")
                        .append("$cdrJson.get(\"")
                        .append(property.getKey())
                        .append("\").asText().matches(\"(?i)")
                        .append(property.getValue())
                        .append("\")");
            }
        }
        sb.append(" )"); // always close the parenthesis
        return sb.toString();
    }


    // --- Utility: Builds 'then' clause ---
    private String buildThen(RuleDefinition definition) {
        Action action = definition.getAction();
        List<Property> properties = action != null ? action.getProperties() : null;

        StringBuilder sb = new StringBuilder("then\n");
        if (properties != null && !properties.isEmpty()) {
            for (Property property : properties) {
                sb.append("\t$cdrJson.put(\"")
                        .append(property.getKey()).append("\", ")
                        .append("\"").append(property.getValue()).append("\");\n");
            }
        }
        // No update() for StatelessKieSession!
        return sb.toString();
    }


    // --- DRL Assembler ---
    public String assembleDRL(List<Drool> rules) {
        StringBuilder drlBuilder = new StringBuilder();
        drlBuilder.append("package com.academy.rules;\n")
                .append("import com.fasterxml.jackson.databind.node.ObjectNode;\n")
                .append("import com.academy.drool.domain.*;\n\n");

        for (Drool drool : rules) {
            drlBuilder.append("rule \"").append(drool.getId()).append("\"\n")
                    .append(drool.getWhen()).append("\n")
                    .append(drool.getThen()).append("\n")
                    .append("end\n\n");
        }
        LOGGER.info("Generated DRL:\n{}", drlBuilder);
        return drlBuilder.toString();
    }

    // --- KIE Helper, builds from latest rules ---
    private StatelessKieSession buildStatelessKieSession() {
        List<Drool> drools = droolRepository.findAll();
        String drl = assembleDRL(drools);
        KieHelper kieHelper = new KieHelper();
        kieHelper.addContent(drl, ResourceType.DRL);
        return kieHelper.build().newStatelessKieSession();
    }
}
