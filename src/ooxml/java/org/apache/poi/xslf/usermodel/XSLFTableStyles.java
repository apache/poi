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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.util.Beta;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTableStyle;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTableStyleList;
import org.openxmlformats.schemas.drawingml.x2006.main.TblStyleLstDocument;

@Beta
public class XSLFTableStyles extends POIXMLDocumentPart implements Iterable<XSLFTableStyle>{
    private CTTableStyleList _tblStyleLst;
    private List<XSLFTableStyle> _styles;

    public XSLFTableStyles(){
        super();
    }

    /**
     * @since POI 3.14-Beta1
     */
    public XSLFTableStyles(PackagePart part) throws IOException, XmlException {
        super(part);

        InputStream is = getPackagePart().getInputStream();
        TblStyleLstDocument styleDoc = TblStyleLstDocument.Factory.parse(is);
        is.close();
        _tblStyleLst = styleDoc.getTblStyleLst();
        List<CTTableStyle> tblStyles = _tblStyleLst.getTblStyleList();
        _styles = new ArrayList<>(tblStyles.size());
        for(CTTableStyle c : tblStyles){
            _styles.add(new XSLFTableStyle(c));
        }
    }
    
    public CTTableStyleList getXmlObject(){
        return _tblStyleLst;
    }

    public Iterator<XSLFTableStyle> iterator(){
        return _styles.iterator();
    }

    public List<XSLFTableStyle> getStyles(){
        return Collections.unmodifiableList(_styles);
    }
}