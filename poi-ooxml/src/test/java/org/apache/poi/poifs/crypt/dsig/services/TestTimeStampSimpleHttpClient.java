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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.apache.poi.poifs.crypt.dsig.DummyKeystore;
import org.apache.poi.poifs.crypt.dsig.DummyKeystore.KeyCertPair;
import org.apache.poi.poifs.crypt.dsig.SignatureConfig;
import org.junit.jupiter.api.Test;

class TestTimeStampSimpleHttpClient {
    private static final String STORE_PASS = "test";
    private static final String LOCALHOST = "127.0.0.1";

    @Test
    void basicAuthNotForwardedOnHttpsToHttpRedirect() throws Exception {
        KeyCertPair pair = new DummyKeystore(STORE_PASS).createDummyKey();
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);
        ks.setKeyEntry("tsp", pair.getKey(), STORE_PASS.toCharArray(), pair.getX509Chain().toArray(new Certificate[0]));
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, STORE_PASS.toCharArray());
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(kmf.getKeyManagers(), null, null);

        InetAddress lo = InetAddress.getByName(LOCALHOST);
        try (ServerSocket tsp = ctx.getServerSocketFactory().createServerSocket(0, 1, lo);
             ServerSocket other = new ServerSocket(0, 1, lo)) {
            String otherUrl = "http://" + LOCALHOST + ":" + other.getLocalPort() + "/";
            CompletableFuture<List<String>> tspReq = serve(tsp,
                "HTTP/1.1 302 Found\r\nLocation: " + otherUrl + "\r\nContent-Length: 0\r\nConnection: close\r\n\r\n");
            CompletableFuture<List<String>> otherReq = serve(other,
                "HTTP/1.1 200 OK\r\nContent-Length: 2\r\nConnection: close\r\n\r\nok");

            SignatureConfig config = new SignatureConfig();
            config.setTspUser("user");
            config.setTspPass("secret");
            TimeStampSimpleHttpClient client = new TimeStampSimpleHttpClient();
            client.init(config);
            client.setIgnoreHttpsCertificates(true);
            client.setFollowRedirects(true);

            String tspUrl = "https://" + LOCALHOST + ":" + tsp.getLocalPort() + "/";
            TimeStampHttpClient.TimeStampHttpClientResponse resp = client.post(tspUrl, new byte[]{1, 2, 3});
            assertEquals(200, resp.getResponseCode());
            assertArrayEquals("ok".getBytes(StandardCharsets.US_ASCII), resp.getResponseBytes());

            assertTrue(hasAuthorization(tspReq.get(10, TimeUnit.SECONDS)));
            assertFalse(hasAuthorization(otherReq.get(10, TimeUnit.SECONDS)),
                "Basic credentials were sent to the redirect target over plain http");
        }
    }

    private static boolean hasAuthorization(List<String> headers) {
        return headers.stream().anyMatch(h -> h.toLowerCase(Locale.ROOT).startsWith("authorization:"));
    }

    private static CompletableFuture<List<String>> serve(ServerSocket server, String response) {
        return CompletableFuture.supplyAsync(() -> {
            try (Socket s = server.accept()) {
                BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.ISO_8859_1));
                List<String> headers = new ArrayList<>();
                int contentLength = 0;
                for (String line; (line = r.readLine()) != null && !line.isEmpty(); ) {
                    headers.add(line);
                    if (line.toLowerCase(Locale.ROOT).startsWith("content-length:")) {
                        contentLength = Integer.parseInt(line.substring(15).trim());
                    }
                }
                for (int i = 0; i < contentLength; i++) {
                    r.read();
                }
                OutputStream os = s.getOutputStream();
                os.write(response.getBytes(StandardCharsets.ISO_8859_1));
                os.flush();
                return headers;
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
    }
}
