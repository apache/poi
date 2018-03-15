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

package org.apache.poi.openxml4j.opc.internal;

import java.io.OutputStream;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.PackagePart;

/**
 * Object implemented this interface are considered as part marshaller. A part
 * marshaller is responsible to marshall a part in order to be save in a
 * package.
 *
 * @author Julien Chable
 * @version 0.1
 */
public interface PartMarshaller {

	/**
	 * Save the content of the package in the stream
	 *
	 * @param part
	 *            Part to marshall.
	 * @param out
	 *            The output stream into which the part will be marshall.
	 * @return <b>false</b> if any marshall error occurs, else <b>true</b>
	 * @throws OpenXML4JException
	 *             Throws only if any other exceptions are thrown by inner
	 *             methods.
	 */
	public boolean marshall(PackagePart part, OutputStream out)
			throws OpenXML4JException;
}
