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

package org.apache.poi.xssf.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.xssf.usermodel.XSSFMap;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTMap;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTMapInfo;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSchema;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.MapInfoDocument;

/**
 * 
 * This class implements the Custom XML Mapping Part (Open Office XML Part 1:
 * chapter 12.3.6)
 * 
 * An instance of this part type contains a schema for an XML file, and
 * information on the behavior that is used when allowing this custom XML schema
 * to be mapped into the spreadsheet.
 * 
 * @author Roberto Manicardi
 */

public class MapInfo extends POIXMLDocumentPart {

	private CTMapInfo mapInfo;
	
	private Map<Integer, XSSFMap> maps ;

	public MapInfo() {
		super();
		mapInfo = CTMapInfo.Factory.newInstance();

	}

	public MapInfo(PackagePart part, PackageRelationship rel)
			throws IOException {
		super(part, rel);
		readFrom(part.getInputStream());
	}

	public void readFrom(InputStream is) throws IOException {
		try {
			MapInfoDocument doc = MapInfoDocument.Factory.parse(is);
			mapInfo = doc.getMapInfo();

            maps= new HashMap<Integer, XSSFMap>();
            for(CTMap map :mapInfo.getMapArray()){
                maps.put((int)map.getID(), new XSSFMap(map,this));
            }

		} catch (XmlException e) {
			throw new IOException(e.getLocalizedMessage());
		}
	}
	
	/**
     * Returns the parent XSSFWorkbook
     *
     * @return the parent XSSFWorkbook
     */
    public XSSFWorkbook getWorkbook() {
        return (XSSFWorkbook)getParent();
    }
	
	/**
	 * 
	 * @return the internal data object
	 */
	public CTMapInfo getCTMapInfo(){
		return mapInfo;
		
	}

	/**
	 * Gets the CTSchema buy it's ID
	 * @param schemaId the schema ID
	 * @return 
	 */
	public CTSchema getCTSchemaById(String schemaId){
		CTSchema xmlSchema = null;

		CTSchema[] schemas = mapInfo.getSchemaArray();
		for(CTSchema schema: schemas){
			if(schema.getID().equals(schemaId)){
				xmlSchema = schema;
				break;
			}
		}
		return xmlSchema;
	}
	
	
	public XSSFMap getXSSFMapById(int id){
		return maps.get(id);
	}
	
	public XSSFMap getXSSFMapByName(String name){
		
		XSSFMap matchedMap = null;
		
		for(XSSFMap map :maps.values()){
			if(map.getCtMap().getName()!=null && map.getCtMap().getName().equals(name)){
				matchedMap = map;
			}
		}		
		
		return matchedMap;
	}
	
	/**
	 * 
	 * @return all the mappings configured in this document
	 */
	public Collection<XSSFMap> getAllXSSFMaps(){
		return maps.values();
	}

	protected void writeTo(OutputStream out) throws IOException {
		MapInfoDocument doc = MapInfoDocument.Factory.newInstance();
		doc.setMapInfo(mapInfo);
		doc.save(out, DEFAULT_XML_OPTIONS);
	}

	@Override
	protected void commit() throws IOException {
		PackagePart part = getPackagePart();
		OutputStream out = part.getOutputStream();
		writeTo(out);
		out.close();
	}

}
