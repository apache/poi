
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hssf.eventusermodel;

import org.apache.poi.hssf.record.Record;

/**
 * Interface for use with the HSSFRequest and HSSFEventFactory.  Users should create
 * a listener supporting this interface and register it with the HSSFRequest (associating
 * it with Record SID's).
 *
 * @see org.apache.poi.hssf.eventmodel.HSSFEventFactory
 * @see org.apache.poi.hssf.eventmodel.HSSFRequest
 * @author  acoliver@apache.org
 */

public interface HSSFListener
{

    /**
     * process an HSSF Record. Called when a record occurs in an HSSF file.
     */

    public void processRecord(Record record);
}
