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

package org.apache.poi.hwpf.usermodel;

import org.apache.poi.hwpf.model.ListFormatOverride;
import org.apache.poi.hwpf.model.ListFormatOverrideLevel;
import org.apache.poi.hwpf.model.ListLevel;
import org.apache.poi.hwpf.model.ListTables;
import org.apache.poi.hwpf.model.PAPX;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

public final class ListEntry
  extends Paragraph
{
	private static POILogger log = POILogFactory.getLogger(ListEntry.class);

	ListLevel _level;
	ListFormatOverrideLevel _overrideLevel;

  ListEntry(PAPX papx, Range parent, ListTables tables)
  {
    super(papx, parent);

    if(tables != null) {
	    ListFormatOverride override = tables.getOverride(_props.getIlfo());
	    _overrideLevel = override.getOverrideLevel(_props.getIlvl());
	    _level = tables.getLevel(override.getLsid(), _props.getIlvl());
    } else {
    	log.log(POILogger.WARN, "No ListTables found for ListEntry - document probably partly corrupt, and you may experience problems");
    }
  }

  public int type()
  {
    return TYPE_LISTENTRY;
  }
}
