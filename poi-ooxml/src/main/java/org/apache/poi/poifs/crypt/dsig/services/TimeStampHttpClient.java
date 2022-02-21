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
import java.net.HttpURLConnection;

import org.apache.poi.poifs.crypt.dsig.SignatureConfig;

/**
 * This interface is used to decouple the timestamp service logic from
 * the actual downloading code and to provide an interface for user code
 * using a different http client implementation.
 *
 * The implementation must be stateless regarding the http connection and
 * not expect to be called in a certain order, apart from being first initialized.
 */
public interface TimeStampHttpClient {
    interface TimeStampHttpClientResponse {
        default boolean isOK() {
            return getResponseCode() == HttpURLConnection.HTTP_OK;
        }

        /**
         * @return the http response code
         */
        int getResponseCode();

        /**
         * @return the http response bytes
         */
        byte[] getResponseBytes();
    }

    void init(SignatureConfig config);

    /** set request content type */
    void setContentTypeIn(String contentType);

    /** set expected response content type - use {@code null} if contentType is ignored */
    void setContentTypeOut(String contentType);

    void setBasicAuthentication(String username, String password);

    TimeStampHttpClientResponse post(String url, byte[] payload) throws IOException;

    TimeStampHttpClientResponse get(String url) throws IOException;

    /**
     * @return if the connection is reckless ignoring all https certificate trust issues
     */
    boolean isIgnoreHttpsCertificates();

    /**
     * @param ignoreHttpsCertificates set if the connection is reckless ignoring all https certificate trust issues
     */
    void setIgnoreHttpsCertificates(boolean ignoreHttpsCertificates);

    /**
     * @return if http redirects are followed once
     */
    boolean isFollowRedirects();

    /**
     * @param followRedirects set if http redirects are followed once
     */
    void setFollowRedirects(boolean followRedirects);
}
