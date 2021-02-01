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
package org.apache.poi.ooxml;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ooxml.POIXMLRelation.PackagePartConstructor;
import org.apache.poi.ooxml.POIXMLRelation.ParentPartConstructor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.xmlbeans.XmlException;

/**
 * Defines a factory API that enables sub-classes to create instances of <code>POIXMLDocumentPart</code>
 */
public abstract class POIXMLFactory {
    private static final Logger LOGGER = LogManager.getLogger(POIXMLFactory.class);

    /**
     * Create a POIXMLDocumentPart from existing package part and relation. This method is called
     * from {@link POIXMLDocument#load(POIXMLFactory)} when parsing a document
     *
     * @param parent parent part
     * @param part  the PackagePart representing the created instance
     * @return A new instance of a POIXMLDocumentPart.
     *
     * @since by POI 3.14-Beta1
     */
    public POIXMLDocumentPart createDocumentPart(POIXMLDocumentPart parent, PackagePart part) {
        final PackageRelationship rel = getPackageRelationship(parent, part);
        final String relType = rel.getRelationshipType();
        final POIXMLRelation descriptor = getDescriptor(relType);

        // don't parse the document parts, if its class can't be determined
        // or if it's a package relation of another embedded resource
        try {
            if (descriptor != null && !POIXMLDocument.PACK_OBJECT_REL_TYPE.equals(relType)) {
                ParentPartConstructor parentPartConstructor = descriptor.getParentPartConstructor();
                if (parentPartConstructor != null) {
                    return parentPartConstructor.init(parent, part);
                }
                PackagePartConstructor packagePartConstructor = descriptor.getPackagePartConstructor();
                if (packagePartConstructor != null) {
                    return packagePartConstructor.init(part);
                }
            }

            LOGGER.atDebug().log("using default POIXMLDocumentPart for {}", rel.getRelationshipType());
            return new POIXMLDocumentPart(parent, part);
        } catch (IOException | XmlException e) {
            throw new POIXMLException(e.getMessage(), e);
        }
    }

    /**
     * returns the descriptor for the given relationship type
     *
     * @param relationshipType the relationship type of the descriptor
     * @return the descriptor or null if type is unknown
     *
     * @since POI 3.14-Beta1
     */
    protected abstract POIXMLRelation getDescriptor(String relationshipType);

    /**
     * Create a new POIXMLDocumentPart using the supplied descriptor. This method is used when adding new parts
     * to a document, for example, when adding a sheet to a workbook, slide to a presentation, etc.
     *
     * @param descriptor  describes the object to create
     * @return A new instance of a POIXMLDocumentPart.
     */
     public POIXMLDocumentPart newDocumentPart(POIXMLRelation descriptor) {
         if (descriptor == null || descriptor.getNoArgConstructor() == null) {
             throw new POIXMLException("can't initialize POIXMLDocumentPart");
         }

         return descriptor.getNoArgConstructor().init();
     }

     /**
      * Retrieves the package relationship of the child part within the parent
      *
      * @param parent the parent to search for the part
      * @param part the part to look for
      *
      * @return the relationship
      *
      * @throws POIXMLException if the relations are erroneous or the part is not related
      *
      * @since POI 3.14-Beta1
      */
     protected PackageRelationship getPackageRelationship(POIXMLDocumentPart parent, PackagePart part) {
         try {
             String partName = part.getPartName().getName();
             for (PackageRelationship pr : parent.getPackagePart().getRelationships()) {
                 String packName = pr.getTargetURI().toASCIIString();
                 if (packName.equalsIgnoreCase(partName)) {
                     return pr;
                 }
             }
         } catch (InvalidFormatException e) {
             throw new POIXMLException("error while determining package relations", e);
         }

         throw new POIXMLException("package part isn't a child of the parent document.");
     }
}
