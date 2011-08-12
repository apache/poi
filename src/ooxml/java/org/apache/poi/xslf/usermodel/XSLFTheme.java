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

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideLayout;
import org.openxmlformats.schemas.presentationml.x2006.main.SldLayoutDocument;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideMaster;
import org.openxmlformats.schemas.drawingml.x2006.main.ThemeDocument;
import org.openxmlformats.schemas.drawingml.x2006.main.CTOfficeStyleSheet;
import org.openxmlformats.schemas.officeDocument.x2006.relationships.STRelationshipId;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.HashMap;

@Beta
public class XSLFTheme extends POIXMLDocumentPart {
    private CTOfficeStyleSheet _theme;

    XSLFTheme() {
        super();
        _theme = CTOfficeStyleSheet.Factory.newInstance();
    }

    public XSLFTheme(PackagePart part, PackageRelationship rel) throws IOException, XmlException {
        super(part, rel);
        ThemeDocument doc =
            ThemeDocument.Factory.parse(getPackagePart().getInputStream());
        _theme = doc.getTheme();
    }


    public String getName(){
        return _theme.getName();
    }

    public void setName(String name){
        _theme.setName(name);
    }

    /**
     * While developing only!
     */
    @Internal
    public CTOfficeStyleSheet getXmlObject() {
        return _theme;
    }

    protected final void commit() throws IOException {
        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);

        Map<String, String> map = new HashMap<String, String>();
        map.put("http://schemas.openxmlformats.org/drawingml/2006/main", "a");
        xmlOptions.setSaveSuggestedPrefixes(map);
        xmlOptions.setSaveSyntheticDocumentElement(
                new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "theme"));

        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        getXmlObject().save(out, xmlOptions);
        out.close();
    }

}