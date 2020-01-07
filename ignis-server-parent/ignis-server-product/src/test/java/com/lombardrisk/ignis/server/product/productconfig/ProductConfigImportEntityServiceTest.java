package com.lombardrisk.ignis.server.product.productconfig;

import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.failure.NotFoundByFailure;
import com.lombardrisk.ignis.feature.IgnisFeature;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.pipeline.PipelineImportService;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfigFileContents;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductImportDiff;
import com.lombardrisk.ignis.server.product.table.TableService;
import com.lombardrisk.ignis.web.common.exception.RollbackTransactionException;
import io.vavr.control.Validation;
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.togglz.junit.TogglzRule;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductConfigImportEntityServiceTest {

    @Mock
    private TableService tableService;
    @Mock
    private PipelineImportService pipelineService;
    @Mock
    private TimeSource timeSource;
    @Mock
    private ProductConfigRepository productRepository;

    @InjectMocks
    private ProductConfigImportEntityService productConfigImportEntityService;

    @Rule
    public TogglzRule togglzRule = TogglzRule.allEnabled(IgnisFeature.class);

    @Before
    public void setUp() {
        when(productRepository.save(any()))
                .thenReturn(ProductPopulated.productConfig().id(0L).build());

        when(pipelineService.importProductPipelines(any(), any(), any()))
                .thenReturn(Validation.valid(singletonList(ProductPopulated.pipeline().build())));
    }

    @Test
    public void saveProductConfig_PipelineSaveFails_ThrowsException() {
        ProductImportDiff productImportDiff = ProductPopulated.productImportDiff().build();
        ProductConfigFileContents fileContents = ProductPopulated.productConfigFileContents().build();

        NotFoundByFailure failure = CRUDFailure.cannotFind("Table")
                .with("physicalTableName", "test")
                .asFailure();

        when(pipelineService.importProductPipelines(any(), any(), any()))
                .thenReturn(Validation.invalid(singletonList(failure.toErrorResponse())));

        assertThatThrownBy(() -> productConfigImportEntityService.saveProductConfig(productImportDiff, fileContents))
                .isInstanceOf(RollbackTransactionException.class)
                .hasMessage("ErrorResponse=[\n"
                        + "\tcode=NOT_FOUND, message=Cannot find Table with physicalTableName 'test']")
                .has(new Condition<>(
                        exception ->
                                ((RollbackTransactionException) exception).toValidation()
                                        .getError()
                                        .contains(failure.toErrorResponse()),
                        "hasFailures"));
    }
}
