package com.lombardrisk.ignis.functional.test.steps.service;

import com.lombardrisk.ignis.client.external.productconfig.export.ValidationRuleExport;

import java.io.IOException;
import java.util.List;

public interface SaveableRulesService {

    List<ValidationRuleExport> saveRules(
            final String ruleSetRequestFileName,
            final long productId,
            final long tableId) throws IOException;
}
