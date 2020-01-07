package com.lombardrisk.ignis.spark.config;

import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Slf4j
public class BasicTrustManager implements X509TrustManager {

    private final  X509TrustManager defaultTm;
    private final X509TrustManager myTm;

    public BasicTrustManager(final X509TrustManager defaultTm, final X509TrustManager myTm) {
        this.defaultTm = defaultTm;
        this.myTm = myTm;
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        // If you're planning to use client-cert auth,
        // merge results from "defaultTm" and "myTm".
        return defaultTm.getAcceptedIssuers();
    }

    @Override
    public void checkServerTrusted(
            final X509Certificate[] chain,
            final String authType) throws CertificateException {
        try {
            myTm.checkServerTrusted(chain, authType);
        } catch (CertificateException e) {
            // This will throw another CertificateException if this fails too.
            log.warn("An error occur while checking server", e);
            defaultTm.checkServerTrusted(chain, authType);
        }
    }

    @Override
    public void checkClientTrusted(
            final X509Certificate[] chain,
            final String authType) throws CertificateException {
        // If you're planning to use client-cert auth,
        // do the same as checking the server.
        defaultTm.checkClientTrusted(chain, authType);
    }
}
