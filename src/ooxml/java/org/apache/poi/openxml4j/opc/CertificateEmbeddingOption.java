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

/**
 * Specifies the location where the X.509 certificate that is used in signing is stored.
 *
 * @author Julien Chable
 */
public enum CertificateEmbeddingOption {
	/** The certificate is embedded in its own PackagePart. */
	IN_CERTIFICATE_PART,
	/** The certificate is embedded in the SignaturePart that is created for the signature being added. */
	IN_SIGNATURE_PART,
	/** The certificate in not embedded in the package. */
	NOT_EMBEDDED
}
