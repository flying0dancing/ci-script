package com.lombardrisk.ignis.functional.test.steps;

import com.lombardrisk.ignis.client.core.view.IdView;
import com.lombardrisk.ignis.client.external.productconfig.ProductConfigClient;
import com.lombardrisk.ignis.client.external.productconfig.view.ProductConfigView;
import com.lombardrisk.ignis.functional.test.zip.ProductConfigZipService;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static com.lombardrisk.ignis.functional.test.steps.CallAssertion.callAndExpectSuccess;

@SuppressWarnings({ "squid:S2077" })
public class ProductConfigSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductConfigSteps.class);
    private static final MediaType MEDIA_TYPE_ZIP = MediaType.parse("application/zip");

    private final ProductConfigClient productConfigClient;
    private final ProductConfigZipService productConfigZipService;
    private final JdbcTemplate phoenixTemplate;
    private final DataSource phoenixDataSource;
    private final String updateSqlSyntax;

    public ProductConfigSteps(
            final ProductConfigClient productConfigClient,
            final ProductConfigZipService productConfigZipService,
            final JdbcTemplate phoenixTemplate,
            final DataSource phoenixDataSource,
            final String updateSqlSyntax) {
        this.productConfigClient = productConfigClient;
        this.productConfigZipService = productConfigZipService;
        this.phoenixTemplate = phoenixTemplate;
        this.phoenixDataSource = phoenixDataSource;
        this.updateSqlSyntax = updateSqlSyntax;
    }

    public IdView importProductConfigFromFolder(final String productConfigFolder) throws IOException {
        File folder = new File(productConfigFolder);
        File productConfigZip = productConfigZipService.writeProductConfig(folder);

        return importProductConfig(productConfigZip);
    }

    public IdView importProductConfig(final File productConfigFile) {
        MultipartBody.Part productConfig = MultipartBody.Part.createFormData(
                "file", productConfigFile.getName(), RequestBody.create(MEDIA_TYPE_ZIP, productConfigFile));

        IdView importedProductId =
                callAndExpectSuccess(productConfigClient.importProductConfig(productConfig));

        LOGGER.info(
                "Imported product config [{}] id [{}]",
                productConfigFile.getName(), importedProductId.getId());

        return importedProductId;
    }

    public Long deleteProduct(final Long id) {
        return callAndExpectSuccess(productConfigClient.deleteProductConfig(id)).getId();
    }

    public ProductConfigView findProduct(final Long id) {
        return callAndExpectSuccess(productConfigClient.getProductConfig(id));
    }

    public void createPhysicalSchema(final String datasetName) {
        phoenixTemplate.execute("create table " + datasetName + " ( ROW_KEY BIGINT primary key )");

        LOGGER.debug("Created empty dataset [{}]", datasetName);
    }

    public void populatePhysicalSchema(final String datasetName) {
        try (Connection connection = phoenixDataSource.getConnection();
             Statement statement = connection.createStatement()) {

            int insertedCount = statement.executeUpdate(updateSqlSyntax + " INTO " + datasetName + " (ROW_KEY) VALUES (12)");

            connection.commit();

            LOGGER.debug("Inserted [{}] rows into dataset [{}]", insertedCount, datasetName);
        } catch (SQLException e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
