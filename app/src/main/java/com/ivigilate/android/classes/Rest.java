package com.ivigilate.android.classes;


import android.content.Context;

import com.ivigilate.android.AppContext;
import com.ivigilate.android.BuildConfig;
import com.ivigilate.android.R;
import com.ivigilate.android.utils.Logger;
import com.squareup.okhttp.OkHttpClient;

import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

import retrofit.RestAdapter;
import retrofit.client.OkClient;

public class Rest {
    public static RestAdapter createAdapter(Context context, String serverAddress) {
        if (serverAddress.startsWith("https://")) {
            try {

                // loading CAs from an InputStream
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                InputStream cert = context.getResources().openRawResource(R.raw.localhost);
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
                okHttpClient.setSslSocketFactory(sslContext.getSocketFactory());
                okHttpClient.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        if (hostname.contains("192.168") ||
                                hostname.equalsIgnoreCase("portal.ivigilate.com")) {
                            return true;
                        }
                        return false;
                    }
                });

                // creating a RestAdapter using the custom client
                return new RestAdapter.Builder()
                        .setEndpoint(serverAddress)
                        .setClient(new OkClient(okHttpClient))
                        .build();

            } catch (Exception ex) {
                Logger.e(ex.getMessage());
            }
            return null;
        } else {
            return new RestAdapter.Builder()
                    .setEndpoint(serverAddress)
                    .build();
        }
    }
}
