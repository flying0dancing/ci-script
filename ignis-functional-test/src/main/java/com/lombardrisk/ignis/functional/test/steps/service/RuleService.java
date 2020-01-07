package com.lombardrisk.ignis.functional.test.steps.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lombardrisk.ignis.client.external.productconfig.export.ValidationRuleExport;
import com.lombardrisk.ignis.client.design.RuleSetClient;
import com.lombardrisk.ignis.client.design.ValidationRuleRequest;
import com.lombardrisk.ignis.functional.test.config.properties.ClientProperties;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.lombardrisk.ignis.functional.test.steps.CallAssertion.callAndExpectSuccess;

public class RuleService implements SaveableRulesService {

    private final RuleSetClient ruleSetClient;
    private final ObjectMapper objectMapper;
    private final ClientProperties clientProperties;

    public RuleService(
            final RuleSetClient ruleSetClient,
            final ObjectMapper objectMapper, final ClientProperties clientProperties) {
        this.ruleSetClient = ruleSetClient;
        this.objectMapper = objectMapper;
        this.clientProperties = clientProperties;
    }

    @Override
    public List<ValidationRuleExport> saveRules(
            final String ruleSetRequestFileName, final long productId, final long tableId) throws IOException {

        File ruleSetRequestFile = clientProperties.getRequestsPath().resolve(ruleSetRequestFileName).toFile();

        List<ValidationRuleRequest> newRules =
                objectMapper.readValue(ruleSetRequestFile, new TypeReference<List<ValidationRuleRequest>>() {
                });

        List<ValidationRuleExport> savedRules = new ArrayList<>();
        for (final ValidationRuleRequest rule : newRules) {
            ValidationRuleExport savedRule =
                    callAndExpectSuccess(ruleSetClient.saveRule(productId, tableId, rule));

            savedRules.add(savedRule);
        }

        return savedRules;
    }
}
