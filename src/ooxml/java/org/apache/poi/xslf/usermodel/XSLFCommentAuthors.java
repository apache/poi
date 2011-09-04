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

import java.io.IOException;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.util.Beta;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.presentationml.x2006.main.CTCommentAuthor;
import org.openxmlformats.schemas.presentationml.x2006.main.CTCommentAuthorList;
import org.openxmlformats.schemas.presentationml.x2006.main.CmAuthorLstDocument;

@Beta
public class XSLFCommentAuthors extends POIXMLDocumentPart {
    private final CTCommentAuthorList _authors;
    
    /**
     * Create a new set of slide comments
     */
    XSLFCommentAuthors() {
       super();
       CmAuthorLstDocument doc = CmAuthorLstDocument.Factory.newInstance();
       _authors = doc.addNewCmAuthorLst();
    }

    /**
     * Construct a SpreadsheetML slide authors from a package part
     *
     * @param part the package part holding the comment authors data,
     * the content type must be <code>application/vnd.openxmlformats-officedocument.commentAuthors+xml</code>
     * @param rel  the package relationship holding this comment authors,
     * the relationship type must be http://schemas.openxmlformats.org/officeDocument/2006/relationships/commentAuthors
     */
    XSLFCommentAuthors(PackagePart part, PackageRelationship rel) throws IOException, XmlException {
        super(part, rel);

        CmAuthorLstDocument doc =
           CmAuthorLstDocument.Factory.parse(getPackagePart().getInputStream());
        _authors = doc.getCmAuthorLst();
    }
    
    public CTCommentAuthorList getCTCommentAuthorsList() {
       return _authors;
    }
    
    public CTCommentAuthor getAuthorById(long id) {
       // TODO Have a map
       for (CTCommentAuthor author : _authors.getCmAuthorList()) {
          if (author.getId() == id) {
             return author;
          }
       }
       return null;
    }
}
