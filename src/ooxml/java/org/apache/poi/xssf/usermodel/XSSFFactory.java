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

package org.apache.poi.xssf.usermodel;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.POIXMLFactory;
import org.apache.poi.POIXMLException;
import org.apache.poi.POIXMLRelation;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackagePart;

import java.lang.reflect.Constructor;

/**
 * Instantiates sub-classes of POIXMLDocumentPart depending on their relationship type
 *
 * @author Yegor Kozlov
 */
public final class XSSFFactory extends POIXMLFactory  {
    private static POILogger logger = POILogFactory.getLogger(XSSFFactory.class);

    private XSSFFactory(){

    }

    private static final XSSFFactory inst = new XSSFFactory();

    public static XSSFFactory getInstance(){
        return inst;
    }

    public POIXMLDocumentPart createDocumentPart(PackageRelationship rel, PackagePart part){
        POIXMLRelation descriptor = XSSFRelation.getInstance(rel.getRelationshipType());
        if(descriptor == null || descriptor.getRelationClass() == null){
            logger.log(POILogger.DEBUG, "using default POIXMLDocumentPart for " + rel.getRelationshipType());
            return new POIXMLDocumentPart(part, rel);
        }

        try {
            Class cls = descriptor.getRelationClass();
            Constructor<? extends POIXMLDocumentPart> constructor = cls.getDeclaredConstructor(PackagePart.class, PackageRelationship.class);
            return constructor.newInstance(part, rel);
        } catch (Exception e){
            throw new POIXMLException(e);
        }
    }

    public POIXMLDocumentPart newDocumentPart(POIXMLRelation descriptor){
        try {
            Class cls = descriptor.getRelationClass();
            Constructor<? extends POIXMLDocumentPart> constructor = cls.getDeclaredConstructor();
            return constructor.newInstance();
        } catch (Exception e){
            throw new POIXMLException(e);
        }
    }

}
