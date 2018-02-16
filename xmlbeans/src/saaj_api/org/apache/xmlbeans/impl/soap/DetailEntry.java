/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.xmlbeans.impl.soap;

/**
 * The content for a <code>Detail</code> object, giving details for
 * a <code>SOAPFault</code> object.  A <code>DetailEntry</code> object,
 * which carries information about errors related to the <code>SOAPBody</code>
 * object that contains it, is application-specific.
 * <P>
 */
public interface DetailEntry extends SOAPElement {}
