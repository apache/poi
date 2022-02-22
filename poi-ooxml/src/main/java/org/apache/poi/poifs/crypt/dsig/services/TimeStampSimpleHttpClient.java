/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.poifs.crypt.dsig.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.poifs.crypt.dsig.SignatureConfig;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.RandomSingleton;

/**
 * This default implementation is used to decouple the timestamp service logic from
 * the actual downloading code and to provide a base for user code
 * using a different http client implementation
 */
public class TimeStampSimpleHttpClient implements TimeStampHttpClient {
    protected static final String CONTENT_TYPE = "Content-Type";
    protected static final String USER_AGENT = "User-Agent";
    protected static final String BASIC_AUTH = "Authorization";
    protected static final String REDIRECT_LOCATION = "Location";

    private static final Logger LOG = LogManager.getLogger(TimeStampSimpleHttpClient.class);

    // how large a timestamp response is expected to be
    // can be overwritten via IOUtils.setByteArrayMaxOverride()
    private static final int DEFAULT_TIMESTAMP_RESPONSE_SIZE = 10_000_000;
    private static int MAX_TIMESTAMP_RESPONSE_SIZE = DEFAULT_TIMESTAMP_RESPONSE_SIZE;

    /**
     * @param maxTimestampResponseSize the max timestamp response size allowed
     */
    public static void setMaxTimestampResponseSize(int maxTimestampResponseSize) {
        MAX_TIMESTAMP_RESPONSE_SIZE = maxTimestampResponseSize;
    }

    /**
     * @return the max timestamp response size allowed
     */
    public static int getMaxTimestampResponseSize() {
        return MAX_TIMESTAMP_RESPONSE_SIZE;
    }


    private static class TimeStampSimpleHttpClientResponse implements TimeStampHttpClientResponse {
        private final int responseCode;
        private final byte[] responseBytes;

        public TimeStampSimpleHttpClientResponse(int responseCode, byte[] responseBytes) {
            this.responseCode = responseCode;
            this.responseBytes = responseBytes;
        }

        @Override
        public int getResponseCode() {
            return responseCode;
        }

        @Override
        public byte[] getResponseBytes() {
            return responseBytes;
        }


    }

    protected SignatureConfig config;
    protected Proxy proxy = Proxy.NO_PROXY;
    protected final Map<String,String> header = new HashMap<>();
    protected String contentTypeOut = null;
    protected boolean ignoreHttpsCertificates = false;
    protected boolean followRedirects = false;

    @Override
    public void init(SignatureConfig config) {
        this.config = config;
        header.clear();

        header.put(USER_AGENT, config.getUserAgent());

        contentTypeOut = null;
        // don't reset followRedirects/ignoreHttpsCertificates, as they aren't contained in SignatureConfig by design
        // followRedirects = false;
        // ignoreHttpsCertificates = false;

        setProxy(config.getProxyUrl());
        setBasicAuthentication(config.getTspUser(), config.getTspPass());
    }

    public void setProxy(String proxyUrl) {
        if (proxyUrl == null || proxyUrl.isEmpty()) {
            proxy = Proxy.NO_PROXY;
        } else {
            try {
                URL pUrl = new URL(proxyUrl);
                String host = pUrl.getHost();
                int port = pUrl.getPort();
                proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(InetAddress.getByName(host), (port == -1 ? 80 : port)));
            } catch (IOException ignored) {
            }
        }
    }

    public Proxy getProxy() {
        return proxy;
    }

    @Override
    public void setContentTypeIn(String contentType) {
        header.put(CONTENT_TYPE, contentType);
    }

    @Override
    public void setContentTypeOut(String contentType) {
        contentTypeOut = contentType;
    }

    @Override
    public void setBasicAuthentication(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            header.remove(BASIC_AUTH);
        } else {
            String userPassword = username + ":" + password;
            String encoding = Base64.getEncoder().encodeToString(userPassword.getBytes(StandardCharsets.ISO_8859_1));
            header.put(BASIC_AUTH, "Basic " + encoding);
        }

    }

    @Override
    public boolean isIgnoreHttpsCertificates() {
        return ignoreHttpsCertificates;
    }

    @Override
    public void setIgnoreHttpsCertificates(boolean ignoreHttpsCertificates) {
        this.ignoreHttpsCertificates = ignoreHttpsCertificates;
    }

    @Override
    public boolean isFollowRedirects() {
        return followRedirects;
    }

    @Override
    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    @Override
    public TimeStampHttpClientResponse post(String url, byte[] payload) throws IOException {
        MethodHandler handler = (huc) -> {
            huc.setRequestMethod("POST");
            huc.setDoOutput(true);
            try (OutputStream hucOut = huc.getOutputStream()) {
                hucOut.write(payload);
            }
        };
        return handleRedirect(url, handler, isFollowRedirects());
    }

    @Override
    public TimeStampHttpClientResponse get(String url)  throws IOException {
        // connection is by default a GET call
        return handleRedirect(url, (huc) -> {}, isFollowRedirects());
    }

    protected interface MethodHandler {
        void handle(HttpURLConnection huc) throws IOException;
    }

    protected TimeStampHttpClientResponse handleRedirect(String url, MethodHandler handler, boolean followRedirect) throws IOException {
        HttpURLConnection huc = (HttpURLConnection)new URL(url).openConnection(proxy);
        if (ignoreHttpsCertificates) {
            recklessConnection(huc);
        }
        huc.setConnectTimeout(20000);
        huc.setReadTimeout(20000);

        header.forEach(huc::setRequestProperty);

        try {
            handler.handle(huc);

            huc.connect();

            final int responseCode = huc.getResponseCode();
            final byte[] responseBytes;

            switch (responseCode) {
                case HttpURLConnection.HTTP_MOVED_TEMP:
                case HttpURLConnection.HTTP_MOVED_PERM:
                case HttpURLConnection.HTTP_SEE_OTHER:
                    String newUrl = huc.getHeaderField(REDIRECT_LOCATION);
                    if (newUrl != null && followRedirect) {
                        LOG.atWarn().log("Received redirect: {} -> {}", url, newUrl);
                        return handleRedirect(newUrl, handler, false);
                    }

                    LOG.atWarn().log("Redirect ignored - giving up: {} -> {}", url, newUrl);
                    responseBytes = null;
                    break;
                case HttpURLConnection.HTTP_OK:
                    // HTTP input validation
                    String contentType = huc.getHeaderField(CONTENT_TYPE);
                    if (contentTypeOut != null && !contentTypeOut.equals(contentType)) {
                        throw new IOException("Content-Type mismatch - expected `" + contentTypeOut + "', received '" + contentType + "'");
                    }

                    try (InputStream is = huc.getInputStream()) {
                        responseBytes = IOUtils.toByteArrayWithMaxLength(is, getMaxTimestampResponseSize());
                    }
                    break;
                default:
                    final String message = "Error contacting TSP server " + url +
                        ", had status code " + responseCode + "/" + huc.getResponseMessage();
                    LOG.atError().log(message);
                    throw new IOException(message);
            }

            return new TimeStampSimpleHttpClientResponse(responseCode, responseBytes);
        } finally {
            huc.disconnect();
        }
    }

    protected void recklessConnection(HttpURLConnection conn) throws IOException {
        if (!(conn instanceof HttpsURLConnection)) {
            return;
        }
        HttpsURLConnection conns = (HttpsURLConnection)conn;

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new UnsafeTrustManager()}, RandomSingleton.getInstance());
            conns.setSSLSocketFactory(sc.getSocketFactory());
            conns.setHostnameVerifier((hostname, session) -> true);
        } catch (GeneralSecurityException e) {
            throw new IOException("Unable to reckless wrap connection.", e);
        }
    }

    private static class UnsafeTrustManager implements X509TrustManager {
        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) { }
        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) { }
    }
}
