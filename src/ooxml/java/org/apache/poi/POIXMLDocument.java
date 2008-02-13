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
package org.apache.poi;

import java.io.IOException;

import org.openxml4j.exceptions.InvalidFormatException;
import org.openxml4j.exceptions.OpenXML4JException;
import org.openxml4j.opc.Package;
import org.openxml4j.opc.PackagePart;
import org.openxml4j.opc.PackagePartName;
import org.openxml4j.opc.PackageRelationship;
import org.openxml4j.opc.PackageRelationshipTypes;
import org.openxml4j.opc.PackagingURIHelper;


public abstract class POIXMLDocument {

    public static final String CORE_PROPERTIES_REL_TYPE = "http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties";
    
    public static final String EXTENDED_PROPERTIES_REL_TYPE = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties";
    
    /** The OPC Package */
    private Package pkg;

    /** The OPC core Package Part */
    private PackagePart corePart;
    
    protected POIXMLDocument() {}
    
    protected POIXMLDocument(String path) throws IOException {
        try {
            this.pkg = Package.open(path);
            PackageRelationship coreDocRelationship = this.pkg.getRelationshipsByType(
                    PackageRelationshipTypes.CORE_DOCUMENT).getRelationship(0);
        
            // Get core part
            this.corePart = this.pkg.getPart(coreDocRelationship);
        } catch (InvalidFormatException e) {
            throw new IOException(e.toString());
        } catch (OpenXML4JException e) {
            throw new IOException(e.toString());
        }
    }
    
    protected Package getPackage() {
        return this.pkg;
    }
    
    protected PackagePart getCorePart() {
        return this.corePart;
    }

    protected PackagePart getPart(PackageRelationship rel) throws InvalidFormatException {
        PackagePartName relName = PackagingURIHelper.createPartName(rel.getTargetURI());
        PackagePart part = getPackage().getPart(relName);
        if (part == null) {
            throw new IllegalArgumentException("No part found for relationship " + rel);
        }
        return part;
    }
}
