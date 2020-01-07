package com.lombardrisk.ignis.server.eraser.controller;
import com.lombardrisk.ignis.server.eraser.service.PipelineService;
import com.lombardrisk.ignis.server.eraser.service.ProductService;
import com.lombardrisk.ignis.server.eraser.service.SchemaService;
import com.lombardrisk.ignis.server.product.pipeline.model.Pipeline;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigRepository;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfig;
import com.lombardrisk.ignis.server.product.productconfig.view.ProductSchemaPipelineListToProductConfigViewList;
import com.lombardrisk.ignis.server.product.table.model.Table;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@AllArgsConstructor(onConstructor = @__({ @Autowired }))
@Slf4j
public class PageController {

    private static final String REDIRECT_HOME = "redirect:/";
    private final ProductConfigRepository productConfigRepository;
    private final SchemaService schemaService;
    private final ProductService productService;
    private final PipelineService pipelineService;

    private final ProductSchemaPipelineListToProductConfigViewList
            productSchemaPipelineListToProductConfigViewList = new ProductSchemaPipelineListToProductConfigViewList();

    @GetMapping("/")
    @Transactional
    public String main(final Model model) {
        model.addAttribute("products", productSchemaPipelineListToProductConfigViewList
                .apply(productConfigRepository.findAllWithoutFieldsRulesAndSteps()));
        return "home"; //view-name
    }

    @PostMapping("/deleteProduct")
    @Transactional
    public ModelAndView deleteProduct(@ModelAttribute ProductConfig productConfig) {
        log.info("Delete product{}", productConfig.getId());
        productService.deleteProduct(productConfig.getId());
        return new ModelAndView(REDIRECT_HOME);
    }

    @PostMapping("/deleteSchema")
    @Transactional
    public ModelAndView deleteSchema(@ModelAttribute Table schema) {
        log.info("Delete schema {}", schema.getId());
        schemaService.deleteSchema(schema.getId());
        return new ModelAndView(REDIRECT_HOME);
    }

    @PostMapping("/deletePipeline")
    @Transactional
    public ModelAndView deletePipeline(@ModelAttribute Pipeline pipeline) {
        log.info("Delete pipeline {}", pipeline.getId());
        pipelineService.deletePipeline(pipeline.getId());
        return new ModelAndView(REDIRECT_HOME);
    }
}
