package com.lombardrisk.ignis.functional.test.config;

import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

public class IgnoreSslTrustManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(
            final X509Certificate[] x509Certificates,
            final String s) {
        //no-op
    }

    @Override
    public void checkServerTrusted(
            final X509Certificate[] x509Certificates,
            final String s) {
        //no-op
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
