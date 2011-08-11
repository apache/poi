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
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideLayout;
import org.openxmlformats.schemas.presentationml.x2006.main.SldLayoutDocument;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideMaster;

import java.io.IOException;

@Beta
public class XSLFSlideLayout extends XSLFSheet {
    private CTSlideLayout _layout;
    private XSLFSlideMaster _master;

    XSLFSlideLayout() {
        super();
        _layout = CTSlideLayout.Factory.newInstance();
    }

    public XSLFSlideLayout(PackagePart part, PackageRelationship rel) throws IOException, XmlException {
        super(part, rel);
        SldLayoutDocument doc =
            SldLayoutDocument.Factory.parse(getPackagePart().getInputStream());
        _layout = doc.getSldLayout();
    }


    public String getName(){
        return _layout.getCSld().getName();
    }

    /**
     * While developing only!
     */
    @Internal
    public CTSlideLayout getXmlObject() {
        return _layout;
    }

    @Override
    protected String getRootElementName(){
        return "sldLayout";
    }

    /**
     * Slide master object associated with this layout.
     * <p>
     *  Within a slide master slide are contained all elements
     * that describe the objects and their corresponding formatting
     * for within a presentation slide.
     * </p>
     * <p>
     * Within a slide master slide are two main elements.
     * The cSld element specifies the common slide elements such as shapes and
     * their attached text bodies. Then the txStyles element specifies the
     * formatting for the text within each of these shapes. The other properties
     * within a slide master slide specify other properties for within a presentation slide
     * such as color information, headers and footers, as well as timing and
     * transition information for all corresponding presentation slides.
     * </p>
     *
     * @return slide master. Never null.
     * @throws IllegalStateException if slide master was not found
     */
    public XSLFSlideMaster getSlideMaster(){
        if(_master == null){
            for (POIXMLDocumentPart p : getRelations()) {
               if (p instanceof XSLFSlideMaster){
                  _master = (XSLFSlideMaster)p;
               }
           }
        }
        if(_master == null) {
            throw new IllegalStateException("SlideMaster was not found for " + this.toString());
        }
        return _master;
    }

    public XMLSlideShow getSlideShow() {
        return (XMLSlideShow)getParent().getParent();
    }
}