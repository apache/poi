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

import org.apache.poi.xssf.usermodel.XSSFWorkbook.XSSFRelation;
import org.openxml4j.exceptions.InvalidFormatException;
import org.openxml4j.opc.PackagePart;

/**
 * Common interface for XSSF models, which have (typically
 *  binary) children to them.
 * One example is a VmlDrawing (Drawing), which can have
 *  raw images associated with it. 
 */
public interface XSSFChildContainingModel extends XSSFModel {
	/**
	 * Returns the relationship type of any children we
	 *  expect
	 */
	public String[] getChildrenRelationshipTypes();
	/**
	 * Called for each matching child, so that the 
	 *  appropriate model or usermodel thing can be 
	 *  created for it.
	 * @param childPart The PackagePart of the child
	 * @param childId the ID of the relationship the child comes from
	 */
	public void generateChild(PackagePart childPart, String childRelId);

	/**
	 * Returns the number of children contained, which
	 *  will need to be written out.
	 */
	public int getNumberOfChildren();
	/**
	 * Called for each child in turn when writing out, 
	 *  which returns a WritableChild for the child in
	 *  that position. The WritableChild has enough
	 *  information to be able to write it out.  
	 * @param index A zero based index of this child
	 */
	public WritableChild getChildForWriting(int index); 
	
	static class WritableChild {
		private XSSFWritableModel model;
		private XSSFRelation relation;
		public WritableChild(XSSFWritableModel model, XSSFRelation rel) {
			this.model = model;
			this.relation = rel;
		}
		public XSSFWritableModel getModel() { return model; }
		public XSSFRelation getRelation() { return relation; }
	}
}
