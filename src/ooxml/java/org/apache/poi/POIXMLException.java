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
 * Indicates a generic OOXML error.
 *
 * @author Yegor Kozlov
 */
public final class POIXMLException extends RuntimeException{
    /**
     * Create a new <code>POIXMLException</code> with no
     * detail mesage.
     */
    public POIXMLException() {
        super();
    }

    /**
     * Create a new <code>POIXMLException</code> with
     * the <code>String</code> specified as an error message.
     *
     * @param msg The error message for the exception.
     */
   public POIXMLException(String msg) {
        super(msg);
    }

    /**
     * Create a new <code>POIXMLException</code> with
     * the <code>String</code> specified as an error message and the cause.
     *
     * @param msg The error message for the exception.
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public POIXMLException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Create a new <code>POIXMLException</code> with
     * the specified cause.
     *
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
     public POIXMLException(Throwable cause) {
        super(cause);
    }
}
