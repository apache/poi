
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


package org.apache.poi.util;

/**
 * This is similar to {@link RecordFormatException}, except this is thrown
 * when there's a higher order problem with parsing a document beyond individual records.
 */
public class DocumentFormatException extends RuntimeException {

    public DocumentFormatException(String exception) {
        super(exception);
    }

    public DocumentFormatException(String exception, Throwable thr) {
        super(exception, thr);
    }

    public DocumentFormatException(Throwable thr) {
        super(thr);
    }

    /**
     * Syntactic sugar to check whether a DocumentFormatException should
     * be thrown.  If assertTrue is <code>false</code>, this will throw this
     * exception with the message.
     *
     * @param assertTrue
     * @param message
     */
    public static void check(boolean assertTrue, String message) {
        if (!assertTrue) {
            throw new DocumentFormatException(message);
        }
    }
}
