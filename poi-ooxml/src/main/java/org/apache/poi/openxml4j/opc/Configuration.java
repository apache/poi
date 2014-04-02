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

package org.apache.poi.openxml4j.opc;

import java.io.File;

/**
 * Storage class for configuration storage parameters.
 * TODO xml syntax checking is no longer done with DOM4j parser -> remove the schema or do it ?
 *
 * @author CDubettier, Julen Chable
 * @version 1.0
 */
public final class Configuration {
	// TODO configuration by default. should be clearly stated that it should be
	// changed to match installation path
	// as schemas dir is needed in runtime
	static private String pathForXmlSchema = System.getProperty("user.dir")
			+ File.separator + "src" + File.separator + "schemas";

	public static String getPathForXmlSchema() {
		return pathForXmlSchema;
	}

	public static void setPathForXmlSchema(String pathForXmlSchema) {
		Configuration.pathForXmlSchema = pathForXmlSchema;
	}
}
