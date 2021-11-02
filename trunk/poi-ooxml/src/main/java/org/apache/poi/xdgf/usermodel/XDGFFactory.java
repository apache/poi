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

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.POIXMLFactory;
import org.apache.poi.ooxml.POIXMLRelation;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.xdgf.xml.XDGFXMLDocumentPart;

/**
 * Instantiates sub-classes of POIXMLDocumentPart depending on their relationship type
 */
public class XDGFFactory extends POIXMLFactory {

    private final XDGFDocument document;

    public XDGFFactory(XDGFDocument document) {
        this.document = document;
    }

    /**
     * @since POI 3.14-Beta1
     */
    protected POIXMLRelation getDescriptor(String relationshipType) {
        return XDGFRelation.getInstance(relationshipType);
    }

    @Override
    public POIXMLDocumentPart createDocumentPart(POIXMLDocumentPart parent, PackagePart part) {
        POIXMLDocumentPart newPart = super.createDocumentPart(parent, part);
        if (newPart instanceof XDGFXMLDocumentPart) {
            ((XDGFXMLDocumentPart)newPart).setDocument(document);
        }
        return newPart;
    }

    @Override
    public POIXMLDocumentPart newDocumentPart(POIXMLRelation descriptor) {
        POIXMLDocumentPart newPart = super.newDocumentPart(descriptor);
        if (newPart instanceof XDGFXMLDocumentPart) {
            ((XDGFXMLDocumentPart)newPart).setDocument(document);
        }
        return newPart;
    }
}
