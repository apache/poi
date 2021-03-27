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

package org.apache.poi.xslf.usermodel;

import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import java.io.IOException;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.util.Beta;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.presentationml.x2006.main.CTComment;
import org.openxmlformats.schemas.presentationml.x2006.main.CTCommentList;
import org.openxmlformats.schemas.presentationml.x2006.main.CmLstDocument;

@Beta
public class XSLFComments extends POIXMLDocumentPart {
    private final CmLstDocument doc;

    /**
     * Create a new set of slide comments
     */
    XSLFComments() {
        doc = CmLstDocument.Factory.newInstance();
    }

    /**
     * Construct a SpreadsheetML slide comments from a package part
     *
     * @param part the package part holding the comments data,
     *             the content type must be <code>application/vnd.openxmlformats-officedocument.comments+xml</code>
     * @since POI 3.14-Beta1
     */
    XSLFComments(PackagePart part) throws IOException, XmlException {
        super(part);

        doc = CmLstDocument.Factory.parse(getPackagePart().getInputStream(), DEFAULT_XML_OPTIONS);
    }

    public CTCommentList getCTCommentsList() {
        return doc.getCmLst();
    }

    public int getNumberOfComments() {
        return doc.getCmLst().sizeOfCmArray();
    }

    public CTComment getCommentAt(int pos) {
        return doc.getCmLst().getCmArray(pos);
    }
}
