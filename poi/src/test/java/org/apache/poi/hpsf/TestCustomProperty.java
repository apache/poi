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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

final class TestCustomProperty {
    @Test
    void equalsIsSymmetricWithProperty() {
        Property property = new Property(5, Variant.VT_LPWSTR, "value");
        CustomProperty custom = new CustomProperty(property, "name");

        assertEquals(property, custom);
        assertEquals(custom, property);

        Property other = new Property(5, Variant.VT_LPWSTR, "other");
        assertNotEquals(other, custom);
        assertNotEquals(custom, other);
    }

    @Test
    void customPropertiesCompareNames() {
        Property property = new Property(5, Variant.VT_LPWSTR, "value");
        assertEquals(new CustomProperty(property, "name"), new CustomProperty(property, "name"));
        assertNotEquals(new CustomProperty(property, "name"), new CustomProperty(property, "other"));
    }
}
