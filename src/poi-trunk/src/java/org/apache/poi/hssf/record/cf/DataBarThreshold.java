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

package org.apache.poi.hssf.record.cf;

import org.apache.poi.util.LittleEndianInput;

/**
 * Data Bar specific Threshold / value (CFVO),
 *  for changes in Conditional Formatting
 */
public final class DataBarThreshold extends Threshold implements Cloneable {
    public DataBarThreshold() {
        super();
    }

    /** Creates new Data Bar Threshold */
    public DataBarThreshold(LittleEndianInput in) {
        super(in);
    }

    @Override
    public DataBarThreshold clone() {
      DataBarThreshold rec = new DataBarThreshold();
      super.copyTo(rec);
      return rec;
    }
}
