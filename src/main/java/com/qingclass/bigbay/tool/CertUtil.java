package com.qingclass.bigbay.tool;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.security.KeyStore;

@SuppressWarnings("deprecation")
public class CertUtil {
    /**
     * 加载证书
     */
    public static SSLConnectionSocketFactory initCert(InputStream instream, String merchantId) throws Exception {

        KeyStore keyStore = KeyStore.getInstance("PKCS12");

        keyStore.load(instream, merchantId.toCharArray());
 
        if (null != instream) {
            instream.close();
        }
 
        SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore,merchantId.toCharArray()).build();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[]{"TLSv1"}, null, SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
 
        return sslsf;
    }
}

