package org.apache.poi.xssf.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.drawingml.x2006.main.*;

/**
 * An instance of this part type contains information about a document's theme, which is a combination of color
 * scheme, font scheme, and format scheme (the latter also being referred to as effects).
 * For a SpreadsheetML document, the choice of theme affects the color and style of cell contents and charts,
 * among other things.
 *
 * @author Yegor Kozlov
 */
public class ThemeTable implements XSSFModel {
	private ThemeDocument doc;
	private String originalId;

	public ThemeTable(InputStream is, String originalId) throws IOException {
		readFrom(is);
		this.originalId = originalId;
	}
	
	public String getOriginalId() {
		return this.originalId;
	}

	public ThemeTable() {
		this.doc = ThemeDocument.Factory.newInstance();
	}

	public void readFrom(InputStream is) throws IOException {
		try {
			doc = ThemeDocument.Factory.parse(is);
        } catch (XmlException e) {
            throw new IOException(e.getLocalizedMessage());
        }
	}

	public void writeTo(OutputStream out) throws IOException {
        XmlOptions options = new XmlOptions();
        options.setSaveOuter();
        options.setUseDefaultNamespace();        

        doc.save(out, options);
	}

}