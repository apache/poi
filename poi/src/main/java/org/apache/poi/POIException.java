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
package org.apache.poi;

/**
 * Indicates a generic POI exception. This is not commonly used in POI
 * but this is intended to be a base class for some new POI exceptions.
 * Historically, POI has used {@link RuntimeException} for most of its
 * exceptions, but this is not a good practice. This class is a checked
 * class that extends {@link Exception} so needs to be explicitly
 * caught or declared in the method signature.
 *
 * @since POI 5.5.0
 */
public class POIException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Create a new {@code POIException} with the specified message.
     *
     * @param msg The error message for the exception.
     */
    public POIException(String msg) {
        super(msg);
    }

    /**
     * Create a new {@code POIException} with the specified cause.
     *
     * @param cause the cause of this exception
     */
    public POIException(Throwable cause) {
        super(cause);
    }

    /**
     * Create a new {@code POIException} with the specified message and cause.
     *
     * @param msg The error message for the exception.
     * @param cause the cause of this exception
     */
    public POIException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
