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

public abstract class POIXMLTextExtractor extends POITextExtractor {
	/** The POIXMLDocument that's open */
	protected POIXMLDocument document;

	/**
	 * Creates a new text extractor for the given document
	 */
	public POIXMLTextExtractor(POIXMLDocument document) {
		super(null);
		
		this.document = document;
	}
}
