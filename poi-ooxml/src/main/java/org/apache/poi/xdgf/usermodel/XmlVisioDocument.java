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
import java.util.Collection;
import java.util.List;

import org.apache.poi.ooxml.POIXMLDocument;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.ooxml.util.PackageHelper;
import org.apache.xmlbeans.XmlException;

import com.microsoft.schemas.office.visio.x2012.main.VisioDocumentDocument1;
import com.microsoft.schemas.office.visio.x2012.main.VisioDocumentType;

/**
 * This is your high-level starting point for working with Visio XML
 * documents (.vsdx).
 *
 * Currently, only read support has been implemented, and the API is
 * not mature and is subject to change.
 *
 * For more information about the visio XML format (with an XSD 1.0
 * schema), you can find documentation at
 * https://msdn.microsoft.com/en-us/library/hh645006(v=office.12).aspx
 *
 * That document lacks in some areas, but you can find additional
 * documentation and an updated XSD 1.1 schema at
 * https://msdn.microsoft.com/en-us/library/office/jj684209(v=office.15).aspx
 *
 * Each provides different details, but the SharePoint reference
 * has better documentation and is more useful.
 */
public class XmlVisioDocument extends POIXMLDocument {

    protected XDGFPages _pages;
    protected XDGFMasters _masters;
    protected XDGFDocument _document;

    public XmlVisioDocument(OPCPackage pkg) throws IOException {
        super(pkg, PackageRelationshipTypes.VISIO_CORE_DOCUMENT);

        VisioDocumentType document;

        try (InputStream stream = getPackagePart().getInputStream()){
            document = VisioDocumentDocument1.Factory.parse(stream).getVisioDocument();
        } catch (XmlException | IOException e) {
            throw new POIXMLException(e);
        }

        _document = new XDGFDocument(document);

        //build a tree of POIXMLDocumentParts, this document being the root
        load(new XDGFFactory(_document));
    }

    public XmlVisioDocument(InputStream is) throws IOException {
        this(PackageHelper.open(is));
    }

    @Override
    protected void onDocumentRead() {
        // by the time this gets called, all other document parts should
        // have been loaded, so it's safe to build the document structure

        // note that in other onDocumentRead(), relations/etc may not have
        // loaded yet, so it's not quite safe

        for (POIXMLDocumentPart part : getRelations()) {
            // organize the document pieces
            if (part instanceof XDGFPages) {
                _pages = (XDGFPages) part;
            } else if (part instanceof XDGFMasters) {
                _masters = (XDGFMasters) part;
            }
        }

        if (_masters != null) {
            _masters.onDocumentRead();
        }

        if (_pages != null) {
            _pages.onDocumentRead();
        }
    }

    /**
     * Not currently implemented
     */
    @Override
    public List<PackagePart> getAllEmbeddedParts() {
        return new ArrayList<>();
    }

    //
    // Useful public API goes here
    //

    /**
     * @return pages ordered by page number
     */
    public Collection<XDGFPage> getPages() {
        return _pages.getPageList();
    }

    public XDGFStyleSheet getStyleById(long id) {
        return _document.getStyleById(id);
    }

}
