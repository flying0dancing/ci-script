package com.lombardrisk.ignis.design.server.productconfig.validation;

import com.lombardrisk.ignis.client.design.productconfig.validation.PipelineTask;
import com.lombardrisk.ignis.client.design.productconfig.validation.ProductConfigTaskList;
import com.lombardrisk.ignis.client.design.productconfig.validation.ValidationError;
import com.lombardrisk.ignis.client.design.productconfig.validation.ValidationTask;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.design.server.pipeline.model.Pipeline;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfig;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfigService;
import com.lombardrisk.ignis.design.server.productconfig.api.ProductPipelineRepository;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class ProductConfigValidator {

    private final ProductPipelineRepository productPipelineRepository;
    private final PipelineValidator pipelineValidator;
    private final ProductConfigService productService;

    public Validation<CRUDFailure, ProductConfigTaskList> productConfigTaskList(final Long productId) {
        return productService
                .findById(productId)
                .map(this::productConfigTaskList)
                .toValidation(CRUDFailure.notFoundIds(ProductConfig.class.getSimpleName(), productId));
    }

    public Flux<ValidationTask> validateProduct(final Long productId) {
        Option<ProductConfig> productConfigOption = productService.findById(productId);
        if (productConfigOption.isEmpty()) {
            return Flux.just(ValidationError.builder()
                    .message("ProductConfig with id " + productId + " not found")
                    .build());
        }
        return productValidationTaskStream(productConfigOption.get());
    }

    private ProductConfigTaskList productConfigTaskList(final ProductConfig product) {
        List<Pipeline> productPipelines = productPipelineRepository.findAllByProductId(product.getId());

        return ProductConfigTaskList.builder()
                .productId(product.getId())
                .pipelineTasks(createPipelineTasks(productPipelines))
                .build();
    }

    private Flux<ValidationTask> productValidationTaskStream(final ProductConfig product) {
        List<Pipeline> productPipelines = productPipelineRepository.findAllByProductId(product.getId());

        return Flux.fromIterable(productPipelines)
                .concatMap(pipelineValidator::pipelineValidationStream);
    }

    private Map<Long, PipelineTask> createPipelineTasks(final List<Pipeline> productPipelines) {
        return productPipelines.stream()
                .map(pipelineValidator::toInitialPipelineTask)
                .collect(Collectors.toMap(PipelineTask::getPipelineId, Function.identity()));
    }
}
