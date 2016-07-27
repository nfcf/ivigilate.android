package com.ivigilate.android.library.classes;


import android.content.Context;

import com.ivigilate.android.library.BuildConfig;
import com.ivigilate.android.library.R;
import com.ivigilate.android.library.utils.Logger;
import com.ivigilate.android.library.utils.StringUtils;
import com.squareup.okhttp.OkHttpClient;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

public class Rest {
    static final String IVIGILATE_DEV_HOSTNAME = "dev.ivigilate.com";
    static final String IVIGILATE_PRD_HOSTNAME = "portal.ivigilate.com";
    static final int TIMEOUT_IN_SECONDS = BuildConfig.DEBUG ? 20 : 10;

    public static <T> T createService(Class<T> serviceClass, Context context, final String serverAddress) {
        return createService(serviceClass, context, serverAddress, "");
    }

    public static <T> T createService(Class<T> serviceClass, Context context, final String serverAddress, final String authToken) {

        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setErrorHandler(new CustomErrorHandler(context))
                .setEndpoint(serverAddress);

        if (serverAddress.startsWith("https://")) {
            try {
                // loading CAs from an InputStream
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                InputStream cert = context.getResources().
                        openRawResource(serverAddress.contains(IVIGILATE_PRD_HOSTNAME) ?
                                R.raw.portal_ivigilate_com: R.raw.dev_ivigilate_com );
                Certificate ca;
                try {
                    ca = cf.generateCertificate(cert);
                } finally {
                    cert.close();
                }

                // creating a KeyStore containing our trusted CAs
                String keyStoreType = KeyStore.getDefaultType();
                KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                keyStore.load(null, null);
                keyStore.setCertificateEntry("ca", ca);

                // creating a TrustManager that trusts the CAs in our KeyStore
                String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                tmf.init(keyStore);

                // creating an SSLSocketFactory that uses our TrustManager
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, tmf.getTrustManagers(), null);

                OkHttpClient okHttpClient = new OkHttpClient();
                okHttpClient.setReadTimeout(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
                okHttpClient.setConnectTimeout(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
                okHttpClient.setSslSocketFactory(sslContext.getSocketFactory());
                okHttpClient.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        if (hostname.contains("192.168") ||
                                hostname.equalsIgnoreCase(IVIGILATE_DEV_HOSTNAME) ||
                                hostname.equalsIgnoreCase(IVIGILATE_PRD_HOSTNAME)) {
                            return true;
                        }
                        return false;
                    }
                });

                // creating a RestAdapter using the custom client
                builder = builder.setClient(new OkClient(okHttpClient));

            } catch (Exception ex) {
                Logger.e(ex.getMessage());
                return null;
            }
        } else {
            OkHttpClient okHttpClient = new OkHttpClient();
            okHttpClient.setReadTimeout(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
            okHttpClient.setConnectTimeout(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);

            builder = builder.setClient(new OkClient(okHttpClient));
        }

        if (!StringUtils.isNullOrBlank(authToken)) {
            builder.setRequestInterceptor(new RequestInterceptor() {
                @Override
                public void intercept(RequestInterceptor.RequestFacade request) {
                    request.addHeader("Authorization", "Token " + authToken);
                }
            });
        }

        RestAdapter ra = builder.build();
        return ra.create(serviceClass);
    }
}
