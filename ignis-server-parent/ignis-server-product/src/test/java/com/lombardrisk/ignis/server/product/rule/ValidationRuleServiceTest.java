package com.lombardrisk.ignis.server.product.rule;

import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.rule.model.ValidationRule;
import io.vavr.control.Option;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidationRuleServiceTest {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Mock
    private ValidationRuleRepository ruleRepository;
    @InjectMocks
    private ValidationRuleService ruleService;

    @Test
    public void findById_RepositoryReturnsOptionalEmpty_ReturnsOptionEmpty() {
        when(ruleRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThat(ruleService.findById(100))
                .isEqualTo(Option.none());
    }

    @Test
    public void findById_RepositoryReturnsOptionalValue_ReturnsOption() {
        ValidationRule validationRule = ProductPopulated.validationRule().build();

        when(ruleRepository.findById(any()))
                .thenReturn(Optional.of(validationRule));

        assertThat(ruleService.findById(100))
                .isEqualTo(Option.of(validationRule));
    }

    @Test
    public void entityName_ReturnsRuleSimpleName() {
        assertThat(ruleService.entityName())
                .isEqualTo("ValidationRule");
    }

    @Test
    public void delete_DelegatesToRepository() {
        assertThatThrownBy(() ->
                ruleService.delete(ProductPopulated.validationRule().build())
        ).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void findByIds_DelegatesToRepository() {
        ruleService.findAllByIds(Arrays.asList(1L, 2L, 3L));

        verify(ruleRepository).findAllById(Arrays.asList(1L, 2L, 3L));
    }

    @Test
    public void findByIds_ReturnsResultFromRepository() {
        ValidationRule validationRule = ProductPopulated.validationRule().build();

        when(ruleRepository.findAllById(Arrays.asList(1L, 2L, 3L)))
                .thenReturn(Collections.singletonList(validationRule));

        List<ValidationRule> allByIds = ruleService.findAllByIds(Arrays.asList(1L, 2L, 3L));

        assertThat(allByIds)
                .containsExactly(validationRule);
    }
}
