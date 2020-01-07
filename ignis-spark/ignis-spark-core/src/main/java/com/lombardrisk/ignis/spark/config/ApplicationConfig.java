package com.lombardrisk.ignis.spark.config;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@Configuration
@Slf4j
public class ApplicationConfig {

    @Value("${debug.mode}")
    @Setter(AccessLevel.PACKAGE)
    private boolean debugMode;

    @Value("${keystore.file}")
    @Setter(AccessLevel.PACKAGE)
    private String keystoreFile;

    @Value("${keystore.password}")
    @Setter(AccessLevel.PACKAGE)
    private String keystorePassword;

    @Bean
    public SparkSession sparkSession() {
        if (debugMode) {
            String target = new File("target").getAbsolutePath();
            String sparkWarehouse = target + "/spark-warehouse";
            String hiveMetaStore = String.format("jdbc:derby:;databaseName=%s/metastore_db;create=true", target);

            return SparkSession.builder()
                    .config("spark.sql.warehouse.dir", sparkWarehouse)
                    .config("javax.jdo.option.ConnectionURL", hiveMetaStore)
                    .master("local[*]")
                    .enableHiveSupport()
                    .getOrCreate();
        } else {
            return SparkSession.builder().getOrCreate();
        }
    }

    @Bean
    public JavaSparkContext javaSparkContext() {
        return new JavaSparkContext(sparkSession().sparkContext());
    }

    @Bean
    public X509TrustManager getCustomTrustManager()
            throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException {

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        // Using null here initialises the TMF with the default trust store.
        tmf.init((KeyStore) null);

        // Get hold of the default trust manager
        X509TrustManager defaultTm = null;
        for (TrustManager tm : tmf.getTrustManagers()) {
            if (tm instanceof X509TrustManager) {
                defaultTm = (X509TrustManager) tm;
                break;
            }
        }

        try (FileInputStream myKeys = new FileInputStream(keystoreFile)) {

            // Do the same with your trust store this time
            // Adapt how you load the keystore to your needs
            KeyStore myTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            myTrustStore.load(myKeys, keystorePassword.toCharArray());
            tmf = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(myTrustStore);

            // Get hold of the default trust manager
            X509TrustManager myTm = null;
            for (TrustManager tm : tmf.getTrustManagers()) {
                if (tm instanceof X509TrustManager) {
                    myTm = (X509TrustManager) tm;
                    break;
                }
            }

            return new BasicTrustManager(defaultTm, myTm);
        }
    }
}
