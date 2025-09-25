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
package org.apache.poi.hwpf;

import org.apache.poi.POIException;

/**
 * An exception that indicates a problem reading a doc file. Usually, a more
 * specific exception will be wrapped as the cause.
 * <p>This exception is only used by some new methods.
 * Historically, POI has used {@link RuntimeException} for most of its
 * exceptions, but this is not a good practice. This class is a checked
 * class that extends {@link Exception} so needs to be explicitly
 * caught or declared in the method signature.</p>
 * @since POI 5.5.0
 */
public class HWPFReadException extends POIException {
    private static final long serialVersionUID = 1L;

    /**
     * Create a new {@code HWPFReadException} with
     * the {@code String} specified as an error message.
     *
     * @param msg The error message for the exception.
     */
    public HWPFReadException(String msg) {
        super(msg);
    }

    /**
     * Create a new {@code HWPFReadException} with
     * the {@code String} specified as an error message and the cause.
     *
     * @param msg The error message for the exception.
     * @param cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A {@code null} value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public HWPFReadException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
