package com.gema.soft.dataacquisition.utils;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gema.soft.dataacquisition.interfaces.httpInterFace;
import com.gema.soft.dataacquisition.models.FileModel;

import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HTTPSUtil implements httpInterFace {
    private static HTTPSUtil httpsUtil;
    private OkHttpClient client;
    public Context mContext;

    private final String BASE_PATH="http://192.168.137.1:/smooth/pwcz";
    public static HTTPSUtil getInstance(Context context)
    {
        if(httpsUtil==null){
            synchronized (HTTPSUtil.class){
                if(httpsUtil==null){
                    httpsUtil=new HTTPSUtil(context);
                }
            }
        }
        return httpsUtil;
    }


    /**
     * 初始化HTTPS,添加信任证书
     * @param context
     */
    private HTTPSUtil(Context context) {
        mContext = context;
        X509TrustManager trustManager;
        SSLSocketFactory sslSocketFactory;
        final InputStream inputStream;
        try {
            inputStream = mContext.getAssets().open("bfservice.cer"); // 得到证书的输入流
            try {

                trustManager = trustManagerForCertificates(inputStream);//以流的方式读入证书
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new TrustManager[]{trustManager}, null);
                sslSocketFactory = sslContext.getSocketFactory();

            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
            client = new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, trustManager)
                    //如果没有安装证书,跳过认证；//接口是用于主机名验证，准确说是验证服务器ca证书中的host是否和请求地址host一致，为什么要进行主机名验证呢？其实目的是加强一层安全防护，防止恶意程序利用中间人攻击。
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    })
                    .readTimeout(30000, TimeUnit.MILLISECONDS)
                    .writeTimeout(30000, TimeUnit.MILLISECONDS)
                    .connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /**
     * 以流的方式添加信任证书
     */
    /**
     * Returns a trust manager that trusts {@code certificates} and none other. HTTPS services whose
     * certificates have not been signed by these certificates will fail with a {@code
     * SSLHandshakeException}.
     * <p>
     * <p>This can be used to replace the host platform's built-in trusted certificates with a custom
     * set. This is useful in development where certificate authority-trusted certificates aren't
     * available. Or in production, to avoid reliance on third-party certificate authorities.
     * <p>
     * <p>
     * <h3>Warning: Customizing Trusted Certificates is Dangerous!</h3>
     * <p>
     * <p>Relying on your own trusted certificates limits your server team's ability to update their
     * TLS certificates. By installing a specific set of trusted certificates, you take on additional
     * operational complexity and limit your ability to migrate between certificate authorities. Do
     * not use custom trusted certificates in production without the blessing of your server's TLS
     * administrator.
     */
    private X509TrustManager trustManagerForCertificates(InputStream in)
            throws GeneralSecurityException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(in);
        if (certificates.isEmpty()) {
            throw new IllegalArgumentException("expected non-empty set of trusted certificates");
        }

        // Put the certificates a key store.
        char[] password = "Bofei@360.com".toCharArray(); // Any password will work.
        KeyStore keyStore = newEmptyKeyStore(password);
        int index = 0;
        for (Certificate certificate : certificates) {
            String certificateAlias = Integer.toString(index++);
            keyStore.setCertificateEntry(certificateAlias, certificate);
        }

        // Use it to build an X509 trust manager.
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:"
                    + Arrays.toString(trustManagers));
        }
        return (X509TrustManager) trustManagers[0];
    }
    /**
     * 添加password
     * @param password
     * @return
     * @throws GeneralSecurityException
     */
    private KeyStore newEmptyKeyStore(char[] password) throws GeneralSecurityException {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType()); // 这里添加自定义的密码，默认
            InputStream in = null; // By convention, 'null' creates an empty key store.
            keyStore.load(in, password);
            return keyStore;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public void asynch(String url, String type, Map<String,Object> params, Callback callback){
        if(TextUtils.isEmpty(type))
            type=TYPE_POST;
        if(TYPE_GET.equals(type)){
            StringBuffer paramsStr=new StringBuffer();
            String paramStr="";
            if(params!=null&&params.size()>0){
                for (String key:params.keySet()
                     ) {
                    paramsStr.append(key).append("=").append(params.get(key)).append("&");
                }
                if(paramsStr.toString().length()>0)
                    paramStr+="?"+paramStr.toString();
            }
            Request request=new Request.Builder().url(BASE_PATH+url).build();
            client.newCall(request).enqueue(callback);
        }else{
            Map result=new HashMap();
            result.put("data",params.get("data"));
            MediaType mediaType=MediaType.Companion.parse("application/json;charset=utf-8");
            RequestBody requestBody=RequestBody.Companion.create(JSONObject.toJSONString(result),mediaType);
            Request request=new Request.Builder().url(BASE_PATH+url).post(requestBody).build();
            client.newCall(request).enqueue(callback);
        }
    }

    public void asynchFile(FileModel fileModel, Callback callback) {
        String filePath = fileModel.getFilePath();
        String fileName = fileModel.getFileName();
        File file = new File(filePath);
        if (file.exists()) {
            MediaType mediaType=MediaType.parse("multipart/form-data");
            RequestBody fileBody = RequestBody.create(file,mediaType);
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", fileName, fileBody)
                    .build();
            try {
                Request request = new Request.Builder().url(DataUtil.getInstance().FILE_PATH ).addHeader("header", "").post(requestBody).build();
                client.newCall(request).enqueue(callback);
            } catch (Exception e) {
                MyException myException = new MyException();
                myException.buildException(e);
            }
        }
    }
}
