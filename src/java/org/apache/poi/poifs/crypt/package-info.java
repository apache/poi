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

/**
 * Implementation of the ECMA-376 and MS-propritary document encryptions<p>
 *
 * The implementation is split into the following packages:<p>
 *
 * <ul>
 * <li>This package contains common functions for both current implemented cipher modes.</li>
 * <li>the {@link org.apache.poi.poifs.crypt.agile agile} package is part of the poi ooxml jar and the provides agile encryption support.</li>
 * <li>the {@link org.apache.poi.poifs.crypt.binaryrc4 binaryrc} package is used for the fixed length RC4 encryption of biff/H**F formats</li>
 * <li>the {@link org.apache.poi.poifs.crypt.cryptoapi cryptoapi} package is used for the variable length RC encryption of biff/H**F formats</li>
 * <li>the {@link org.apache.poi.poifs.crypt.standard standard} package contains classes for the standard encryption ...</li>
 * <li>the {@link org.apache.poi.poifs.crypt.xor xor} package contains classes for the xor obfuscation of biff/H**F formats</li>
 * </ul>
 *
 * @see <a href="http://poi.apache.org/encryption.html">Apache POI - Encryption support</a>
 * @see <a href="http://msdn.microsoft.com/en-us/library/dd952186(v=office.12).aspx">ECMA-376 Document Encryption</a>
 */
package org.apache.poi.poifs.crypt;