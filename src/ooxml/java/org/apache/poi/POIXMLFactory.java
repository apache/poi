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

import java.lang.reflect.InvocationTargetException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Defines a factory API that enables sub-classes to create instances of <code>POIXMLDocumentPart</code>
 */
public abstract class POIXMLFactory {
    private static final POILogger LOGGER = POILogFactory.getLogger(POIXMLFactory.class);

    private static final Class<?>[] PARENT_PART = {POIXMLDocumentPart.class, PackagePart.class};
    private static final Class<?>[] ORPHAN_PART = {PackagePart.class};
    
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
        PackageRelationship rel = getPackageRelationship(parent, part);
        POIXMLRelation descriptor = getDescriptor(rel.getRelationshipType());
        
        if (descriptor == null || descriptor.getRelationClass() == null) {
            LOGGER.log(POILogger.DEBUG, "using default POIXMLDocumentPart for " + rel.getRelationshipType());
            return new POIXMLDocumentPart(parent, part);
        }

        Class<? extends POIXMLDocumentPart> cls = descriptor.getRelationClass();
        try {
            try {
                return createDocumentPart(cls, PARENT_PART, new Object[]{parent, part});
            } catch (NoSuchMethodException e) {
                return createDocumentPart(cls, ORPHAN_PART, new Object[]{part});
            }
        } catch (Exception e) {
            throw new POIXMLException(e);
        }
    }
    
    /**
     * Need to delegate instantiation to sub class because of constructor visibility
     *
     * @param cls the document class to be instantiated
     * @param classes the classes of the constructor arguments
     * @param values the values of the constructor arguments
     * @return the new document / part
     * @throws SecurityException thrown if the object can't be instantiated
     * @throws NoSuchMethodException thrown if there is no constructor found for the given arguments
     * @throws InstantiationException thrown if the object can't be instantiated
     * @throws IllegalAccessException thrown if the object can't be instantiated
     * @throws InvocationTargetException thrown if the object can't be instantiated
     * 
     * @since POI 3.14-Beta1
     */
    protected abstract POIXMLDocumentPart createDocumentPart
        (Class<? extends POIXMLDocumentPart> cls, Class<?>[] classes, Object[] values)
    throws SecurityException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException;
    
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
         Class<? extends POIXMLDocumentPart> cls = descriptor.getRelationClass();
         try {
             return createDocumentPart(cls, null, null);
         } catch (Exception e) {
             throw new POIXMLException(e);
         }
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
