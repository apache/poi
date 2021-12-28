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
import java.io.InputStream;

import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.sl.usermodel.MetroShapeProvider;
import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGroupShape;

/**
 * Experimental class for metro blobs, i.e. an alternative escher property
 * containing an ooxml representation of the shape.
 * This is the helper class for HSLFMetroShape to dive into OOXML classes
 */
@Internal
public class XSLFMetroShape implements MetroShapeProvider {
    /** parses the metro bytes to a XSLF shape */
    @Override
    public XSLFShape parseShape(byte[] metroBytes) throws IOException {
        try (OPCPackage pkg = OPCPackage.open(new UnsynchronizedByteArrayInputStream(metroBytes))) {
            PackagePartName shapePN = PackagingURIHelper.createPartName("/drs/shapexml.xml");
            PackagePart shapePart = pkg.getPart(shapePN);
            if (shapePart == null) {
                return null;
            }
            try (InputStream stream = shapePart.getInputStream()) {
                CTGroupShape gs = CTGroupShape.Factory.parse(stream, DEFAULT_XML_OPTIONS);
                XSLFGroupShape xgs = new XSLFGroupShape(gs, null);
                return xgs.getShapes().get(0);
            }
        } catch (InvalidFormatException | XmlException e) {
            throw new IOException("can't parse metro shape", e);
        }
    }
}
