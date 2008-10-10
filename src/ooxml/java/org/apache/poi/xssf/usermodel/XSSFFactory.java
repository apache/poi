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
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.model.CommentsTable;
import org.openxml4j.opc.PackageRelationship;
import org.openxml4j.opc.PackagePart;

import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Constructor;

/**
 * Instantiates sub-classes of POIXMLDocumentPart depending on their relationship type
 *
 * @author Yegor Kozlov
 */
public class XSSFFactory extends POIXMLFactory  {
    protected static Map<String, Class> parts = new HashMap<String, Class>();
    static {
        parts.put(XSSFRelation.WORKSHEET.getRelation(), XSSFSheet.class);
        parts.put(XSSFRelation.SHARED_STRINGS.getRelation(), SharedStringsTable.class);
        parts.put(XSSFRelation.STYLES.getRelation(), StylesTable.class);
        parts.put(XSSFRelation.SHEET_COMMENTS.getRelation(), CommentsTable.class);
        parts.put(XSSFRelation.DRAWINGS.getRelation(), XSSFDrawing.class);
        parts.put(XSSFRelation.IMAGES.getRelation(), XSSFPictureData.class);
    }

    public POIXMLDocumentPart create(PackageRelationship rel, PackagePart p){
        Class cls = parts.get(rel.getRelationshipType());
        if(cls == null) return super.create(rel, p);

        try {
            Constructor<? extends POIXMLDocumentPart> constructor = cls.getConstructor(PackagePart.class, PackageRelationship.class);
            return constructor.newInstance(p, rel);
        } catch (Exception e){
            throw new POIXMLException(e);
        }
    }
}
