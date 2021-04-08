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
package org.apache.poi.hwpf.model;

import org.apache.poi.util.Internal;

@Internal
public enum FieldsDocumentPart {

    /**
     * annotation subdocument
     */
    ANNOTATIONS( FIBFieldHandler.PLCFFLDATN ),

    /**
     * endnote subdocument
     */
    ENDNOTES( FIBFieldHandler.PLCFFLDEDN ),

    /**
     * footnote subdocument
     */
    FOOTNOTES( FIBFieldHandler.PLCFFLDFTN ),

    /**
     * header subdocument
     */
    HEADER( FIBFieldHandler.PLCFFLDHDR ),

    /**
     * header textbox subdoc
     */
    HEADER_TEXTBOX( FIBFieldHandler.PLCFFLDHDRTXBX ),

    /**
     * main document
     */
    MAIN( FIBFieldHandler.PLCFFLDMOM ),

    /**
     * textbox subdoc
     */
    TEXTBOX( FIBFieldHandler.PLCFFLDTXBX );

    private final int fibFieldsField;

    private FieldsDocumentPart( final int fibHandlerField )
    {
        this.fibFieldsField = fibHandlerField;
    }

    public int getFibFieldsField()
    {
        return fibFieldsField;
    }

}
