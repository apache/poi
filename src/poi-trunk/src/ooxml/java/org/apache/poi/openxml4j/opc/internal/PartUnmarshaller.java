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

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.internal.unmarshallers.UnmarshallContext;

/**
 * Object implemented this interface are considered as part unmarshaller. A part
 * unmarshaller is responsible to unmarshall a part in order to load it from a
 * package.
 *
 * @author Julien Chable
 * @version 0.1
 */
public interface PartUnmarshaller {

	/**
	 * Save the content of the package in the stream
	 *
	 * @param in
	 *            The input stream from which the part will be unmarshall.
	 * @return The part freshly unmarshall from the input stream.
	 * @throws OpenXML4JException
	 *             Throws only if any other exceptions are thrown by inner
	 *             methods.
	 */
	public PackagePart unmarshall(UnmarshallContext context, InputStream in)
			throws InvalidFormatException, IOException;
}
