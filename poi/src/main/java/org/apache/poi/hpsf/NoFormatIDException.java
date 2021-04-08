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

package org.apache.poi.hpsf;

/**
 * This exception is thrown if a {@link PropertySet} is to be written
 * but does not have a formatID set (see {@link Section#setFormatID(ClassID)} or
 * {@link org.apache.poi.hpsf.Section#setFormatID(byte[])}. 
 */
public class NoFormatIDException extends HPSFRuntimeException {

    /**
     * Constructor
     */
    public NoFormatIDException() {
        super();
    }


    /**
     * Constructor
     * 
     * @param msg The exception's message string
     */
    public NoFormatIDException(final String msg) {
        super(msg);
    }


    /**
     * Constructor
     * 
     * @param reason This exception's underlying reason
     */
    public NoFormatIDException(final Throwable reason) {
        super(reason);
    }


    /**
     * Constructor
     * 
     * @param msg The exception's message string
     * @param reason This exception's underlying reason
     */
    public NoFormatIDException(final String msg, final Throwable reason) {
        super(msg, reason);
    }

}
