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

package org.apache.poi.hpsf;

import org.apache.poi.util.Removal;

/**
 * Interface for the convenience classes {@link SummaryInformation}
 * and {@link DocumentSummaryInformation}.<p>
 *
 * This used to be an abstract class to support late loading
 * of the SummaryInformation classes, as their concrete instance can
 * only be determined after the PropertySet has been loaded.
 *
 * @deprecated POI 3.16 - use PropertySet as base class instead
 */
@Removal(version="3.18")
public class SpecialPropertySet extends MutablePropertySet {
    public SpecialPropertySet() {
    }

    public SpecialPropertySet(final PropertySet ps) throws UnexpectedPropertySetTypeException {
        super(ps);
    }
}
