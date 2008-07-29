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
	 * Find any children associated with the {@link XSSFModel}.
	 * @param modelPart The PackagePart of this model 
	 */
	public void findChildren(PackagePart modelPart) throws IOException, InvalidFormatException;
	/** 
	 * Writes out any children associated with the {@link XSSFModel},
	 *  along with the required relationship stuff.
	 * @param modelPart The new PackagePart of this model 
	 */
	public void writeChildren(PackagePart modelPart) throws IOException, InvalidFormatException;
}
