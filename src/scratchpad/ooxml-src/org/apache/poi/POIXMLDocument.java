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

import org.apache.poi.hxf.HXFDocument;

/** 
 * Parent class of all UserModel POI XML (ooxml) 
 *  implementations.
 * Provides a similar function to {@link POIDocument},
 *  for the XML based classes.
 */
public abstract class POIXMLDocument {
	private HXFDocument document;

	/**
	 * Creates a new POI XML Document, wrapping up
	 *  the underlying raw HXFDocument
	 */
	protected POIXMLDocument(HXFDocument document) {
		this.document = document;
	}

	/**
	 * Returns the underlying HXFDocument, typically
	 *  used for unit testing
	 */
	public HXFDocument _getHXFDocument() {
		return document;
	}
}
