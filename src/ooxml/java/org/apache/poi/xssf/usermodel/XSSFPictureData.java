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

import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.util.IOUtils;
import org.openxml4j.opc.PackagePart;

/**
 * Raw picture data, normally attached to a 
 *  vmlDrawing
 */
public class XSSFPictureData implements PictureData {
    private PackagePart packagePart;
    private String originalId;

    public XSSFPictureData(PackagePart packagePart, String originalId) {
        this(packagePart);
        this.originalId = originalId;
    }
    
    public XSSFPictureData(PackagePart packagePart) {
        this.packagePart = packagePart;
    }

    public String getOriginalId() {
    	return originalId;
    }
    
    protected PackagePart getPart() {
    	return packagePart;
    }
    
	public void writeTo(OutputStream out) throws IOException {
		IOUtils.copy(packagePart.getInputStream(), out);
	}

    public byte[] getData() {
    	try {
    		return IOUtils.toByteArray(packagePart.getInputStream());
    	} catch(IOException e) {
    		throw new RuntimeException(e);
    	}
    }

    public String suggestFileExtension() {
    	return packagePart.getPartName().getExtension();
    }
}
