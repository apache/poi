
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
 * A common exception thrown by our binary format parsers
 *  (especially HSSF and DDF), when they hit invalid
 *  format or data when processing a record.
 */
public class RecordFormatException
    extends RuntimeException
{
    public RecordFormatException(String exception)
    {
        super(exception);
    }
    
    public RecordFormatException(String exception, Throwable thr) {
      super(exception, thr);
    }
    
    public RecordFormatException(Throwable thr) {
      super(thr);
    }

    /**
     * Syntactic sugar to check whether a RecordFormatException should
     * be thrown.  If assertTrue is <code>false</code>, this will throw this
     * exception with the message.
     *
     * @param assertTrue
     * @param message
     */
    public static void check(boolean assertTrue, String message) {
        if (! assertTrue) {
            throw new RecordFormatException(message);
        }
    }
}
