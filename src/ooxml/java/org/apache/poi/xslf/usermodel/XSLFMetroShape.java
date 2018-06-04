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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.sl.usermodel.Shape;
import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGroupShape;

/**
 * Experimental class for metro blobs, i.e. an alternative escher property
 * containing an ooxml representation of the shape.
 * This is the helper class for HSLFMetroShape to dive into OOXML classes
 */
@Internal
public class XSLFMetroShape {
    /*
     * parses the metro bytes to a XSLF shape
     */
    public static Shape<?,?> parseShape(byte metroBytes[])
    throws InvalidFormatException, IOException, XmlException {
        PackagePartName shapePN = PackagingURIHelper.createPartName("/drs/shapexml.xml");
        OPCPackage pkg = null;
        try {
            pkg = OPCPackage.open(new ByteArrayInputStream(metroBytes));
            PackagePart shapePart = pkg.getPart(shapePN);
            CTGroupShape gs = CTGroupShape.Factory.parse(shapePart.getInputStream(), DEFAULT_XML_OPTIONS);
            XSLFGroupShape xgs = new XSLFGroupShape(gs, null);
            return xgs.getShapes().get(0);               
        } finally {
            if (pkg != null) {
                pkg.close();
            }
        }
    }
}
