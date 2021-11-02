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

package org.apache.poi.xdgf.exceptions;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.POIXMLException;

public class XDGFException {

    /**
     * Creates an error message to be thrown
     */
    public static POIXMLException error(String message, Object o) {
        return new POIXMLException(o + ": " + message);
    }

    public static POIXMLException error(String message, Object o, Throwable t) {
        return new POIXMLException(o + ": " + message, t);
    }

    //
    // Use these to wrap error messages coming up so that we have at least
    // some idea where the error was located
    //

    public static POIXMLException wrap(POIXMLDocumentPart part,
            POIXMLException e) {
        return new POIXMLException(part.getPackagePart().getPartName()
                + ": " + e.getMessage(), e.getCause() == null ? e
                        : e.getCause());
    }

    public static POIXMLException wrap(String where, POIXMLException e) {
        return new POIXMLException(where + ": " + e.getMessage(),
                e.getCause() == null ? e : e.getCause());
    }
}
