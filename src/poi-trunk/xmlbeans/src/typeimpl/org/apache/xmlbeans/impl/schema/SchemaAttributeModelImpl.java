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

package org.apache.xmlbeans.impl.schema;

import org.apache.xmlbeans.SchemaAttributeModel;
import org.apache.xmlbeans.SchemaGlobalAttribute;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.QNameSetBuilder;
import org.apache.xmlbeans.SchemaLocalAttribute;
import javax.xml.namespace.QName;

import java.util.Map;
import java.util.LinkedHashMap;

public class SchemaAttributeModelImpl implements SchemaAttributeModel
{
    private Map attrMap;
    private QNameSet wcSet;
    private int wcProcess;

    public SchemaAttributeModelImpl()
    {
        attrMap = new LinkedHashMap();
        wcSet = null;
        wcProcess = NONE;
    }

    public SchemaAttributeModelImpl(SchemaAttributeModel sam)
    {
        attrMap = new LinkedHashMap();
        if (sam == null)
        {
            wcSet = null;
            wcProcess = NONE;
        }
        else
        {
            SchemaLocalAttribute[] attrs = sam.getAttributes();
            for (int i = 0; i < attrs.length; i++)
            {
                attrMap.put(attrs[i].getName(), attrs[i]);
            }

            if (sam.getWildcardProcess() != SchemaAttributeModel.NONE)
            {
                wcSet = sam.getWildcardSet();
                wcProcess = sam.getWildcardProcess();
            }
        }
    }
    
    private static final SchemaLocalAttribute[] EMPTY_SLA_ARRAY = new SchemaLocalAttribute[0];

    public SchemaLocalAttribute[] getAttributes()
    {
        return (SchemaLocalAttribute[])attrMap.values().toArray(EMPTY_SLA_ARRAY);
    }

    public SchemaLocalAttribute getAttribute(QName name)
    {
        return (SchemaLocalAttribute)attrMap.get(name);
    }

    public void addAttribute(SchemaLocalAttribute attruse)
    {
        attrMap.put(attruse.getName(), attruse);
    }
    
    public void removeProhibitedAttribute(QName name)
    {
        attrMap.remove(name);
    }

    public QNameSet getWildcardSet()
    {
        return wcSet == null ? QNameSet.EMPTY : wcSet;
    }

    public void setWildcardSet(QNameSet set)
    {
        wcSet = set;
    }

    public int getWildcardProcess()
    {
        return wcProcess;
    }

    public void setWildcardProcess(int proc)
    {
        wcProcess = proc;
    }
}
