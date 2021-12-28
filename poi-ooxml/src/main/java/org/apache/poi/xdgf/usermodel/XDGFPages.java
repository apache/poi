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
package org.apache.poi.xdgf.usermodel;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.microsoft.schemas.office.visio.x2012.main.PageType;
import com.microsoft.schemas.office.visio.x2012.main.PagesDocument;
import com.microsoft.schemas.office.visio.x2012.main.PagesType;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.util.Internal;
import org.apache.poi.xdgf.exceptions.XDGFException;
import org.apache.poi.xdgf.xml.XDGFXMLDocumentPart;
import org.apache.xmlbeans.XmlException;


/**
 * Contains a list of Page objects (not page content!)
 */
public class XDGFPages extends XDGFXMLDocumentPart {

    PagesType _pagesObject;

    // ordered by page number
    List<XDGFPage> _pages = new ArrayList<>();

    /**
     * @since POI 3.14-Beta1
     */
    public XDGFPages(PackagePart part) {
        super(part);
    }

    @Internal
    PagesType getXmlObject() {
        return _pagesObject;
    }

    @Override
    protected void onDocumentRead() {
        try {
            try (InputStream stream = getPackagePart().getInputStream()) {
                _pagesObject = PagesDocument.Factory.parse(stream).getPages();
            } catch (XmlException | IOException e) {
                throw new POIXMLException(e);
            }

            // this iteration is ordered by page number
            for (PageType pageSettings: _pagesObject.getPageArray()) {

                String relId = pageSettings.getRel().getId();

                POIXMLDocumentPart pageContentsPart = getRelationById(relId);
                if (pageContentsPart == null)
                    throw new POIXMLException("PageSettings relationship for " + relId + " not found");

                if (!(pageContentsPart instanceof XDGFPageContents))
                    throw new POIXMLException("Unexpected pages relationship for " + relId + ": " + pageContentsPart);

                XDGFPageContents contents = (XDGFPageContents)pageContentsPart;
                XDGFPage page = new XDGFPage(pageSettings, contents, _document, this);

                contents.onDocumentRead();

                _pages.add(page);
            }

        } catch (POIXMLException e) {
            throw XDGFException.wrap(this, e);
        }
    }

    /**
     * @return A list of pages ordered by page number
     */
    public List<XDGFPage> getPageList() {
        return Collections.unmodifiableList(_pages);
    }
}
