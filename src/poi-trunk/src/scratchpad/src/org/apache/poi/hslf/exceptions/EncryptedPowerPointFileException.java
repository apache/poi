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

package org.apache.poi.hslf.exceptions;

import org.apache.poi.EncryptedDocumentException;

/**
 * This exception is thrown when we try to open a PowerPoint file, and
 *  discover that it is encrypted
 */
public final class EncryptedPowerPointFileException extends EncryptedDocumentException
{
	public EncryptedPowerPointFileException(String s) {
		super(s);
	}
	
	public EncryptedPowerPointFileException(String s, Throwable t) {
        super(s, t);
	}

    public EncryptedPowerPointFileException(Throwable t) {
        super(t);
    }
}
