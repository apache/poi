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

package org.apache.poi.hwpf.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * This class holds all the FSPA (File Shape Address) structures.
 * 
 * @author Squeeself
 */
public class FSPATable 
{
    protected ArrayList shapes = new ArrayList();
    protected HashMap cps = new HashMap();
    protected List _text;
    
    public FSPATable(byte[] tableStream, int fcPlcspa, int lcbPlcspa, List tpt)
    {
        _text = tpt;
        // Will be 0 if no drawing objects in document
        if (fcPlcspa == 0)
            return;
        
        PlexOfCps plex = new PlexOfCps(tableStream, fcPlcspa, lcbPlcspa, FSPA.FSPA_SIZE);
        for (int i=0; i < plex.length(); i++)
        {
            GenericPropertyNode property = plex.getProperty(i);
            FSPA fspa = new FSPA(property.getBytes(), 0);
            
            shapes.add(fspa);
            cps.put(Integer.valueOf(property.getStart()), Integer.valueOf(i));
        }
    }
    
    public FSPA getFspaFromCp(int cp)
    {
        Integer idx = (Integer)cps.get(Integer.valueOf(cp));
        if (idx == null)
            return null;
        return (FSPA)shapes.get(idx.intValue());
    }
    
    public List getShapes()
    {
        return shapes;
    }
    
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("[FPSA PLC size=").append(shapes.size()).append("]\n");
        for (Iterator it = cps.keySet().iterator(); it.hasNext(); )
        {
            Integer i = (Integer) it.next();
            FSPA fspa = (FSPA) shapes.get(((Integer)cps.get(i)).intValue());
            buf.append("  [FC: ").append(i.toString()).append("] ");
            buf.append(fspa.toString());
            buf.append("\n");
        }
        buf.append("[/FSPA PLC]");
        return buf.toString();
    }
}
