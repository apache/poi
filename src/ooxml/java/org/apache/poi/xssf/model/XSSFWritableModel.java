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
import java.io.OutputStream;

import org.apache.poi.xssf.usermodel.XSSFRelation;

/**
 * A very stripped down XSSF models interface,
 *  which only allows writing out. Normally only
 *  used with the children of a
 *  {@link XSSFChildContainingModel}.
 * Most proper models will go for {@link XSSFModel}
 *  instead of this one.
 * {@link XSSFRelation} needs classes to implement
 *  this, so that it can write them out.
 */
public interface XSSFWritableModel {
	/** Write to the supplied OutputStream, with default options */
	public void writeTo(OutputStream out) throws IOException;
}
