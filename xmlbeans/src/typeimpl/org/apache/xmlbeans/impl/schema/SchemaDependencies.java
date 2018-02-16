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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SchemaDependencies
{
    // This class is NOT synchronized

    void registerDependency(String source, String target)
    {
        Set depSet = (Set) _dependencies.get(target);
        if (depSet == null)
        {
            depSet = new HashSet();
            _dependencies.put(target, depSet);
        }
        depSet.add(source);
    }

    

    /**
     * Records anti-dependencies. Keys are namespaces and values are
     * the lists of namespaces that depend on each key
     */
    private Map/*<String,Set<String>>*/ _dependencies;

    Set computeTransitiveClosure(List modifiedNamespaces)
    {
        List nsList = new ArrayList(modifiedNamespaces);
        Set result = new HashSet(modifiedNamespaces);
        for (int i = 0; i < nsList.size(); i++)
        {
            Set deps = (Set) _dependencies.get(nsList.get(i));
            if (deps == null)
                continue;
            for (Iterator it = deps.iterator(); it.hasNext(); )
            {
                String ns = (String) it.next();
                if (!result.contains(ns))
                {
                    nsList.add(ns);
                    result.add(ns);
                }
            }
        }
        return result;
    }

    SchemaDependencies()
    {
        _dependencies = new HashMap();
        _contributions = new HashMap();
    }

    SchemaDependencies(SchemaDependencies base, Set updatedNs)
    {
        _dependencies = new HashMap();
        _contributions = new HashMap();
        for (Iterator it = base._dependencies.keySet().iterator(); it.hasNext(); )
        {
            String target = (String) it.next();
            if (updatedNs.contains(target))
                continue;
            Set depSet = new HashSet();
            _dependencies.put(target, depSet);
            Set baseDepSet = (Set) base._dependencies.get(target);
            for (Iterator it2 = baseDepSet.iterator(); it2.hasNext(); )
            {
                String source = (String) it2.next();
                if (updatedNs.contains(source))
                    continue;
                depSet.add(source);
            }
        }
        for (Iterator it = base._contributions.keySet().iterator(); it.hasNext(); )
        {
            String ns = (String) it.next();
            if (updatedNs.contains(ns))
                continue;
            List fileList = new ArrayList();
            _contributions.put(ns, fileList);
            List baseFileList = (List) base._contributions.get(ns);
            for (Iterator it2 = baseFileList.iterator(); it2.hasNext(); )
                fileList.add(it2.next());
        }
    }

    /**
     * Records the list of files associated to each namespace.
     * This is needed so that we can return a list of files that
     * need to be compiled once we get a set of altered namespaces
     */
    private Map/*<String,List<String>>*/ _contributions;

    void registerContribution(String ns, String fileURL)
    {
        List fileList = (List) _contributions.get(ns);
        if (fileList == null)
        {
            fileList = new ArrayList();
            _contributions.put(ns, fileList);
        }
        fileList.add(fileURL);
    }

    boolean isFileRepresented(String fileURL)
    {
        for (Iterator it = _contributions.values().iterator(); it.hasNext(); )
        {
            List fileList = (List) it.next();
            if (fileList.contains(fileURL))
                return true;
        }
        return false;
    }

    List getFilesTouched(Set updatedNs)
    {
        List result = new ArrayList();
        for (Iterator it = updatedNs.iterator(); it.hasNext(); )
        {
            result.addAll((List) _contributions.get(it.next()));
        }
        return result;
    }

    List getNamespacesTouched(Set modifiedFiles)
    {
        List result = new ArrayList();
        for (Iterator it = _contributions.keySet().iterator(); it.hasNext(); )
        {
            String ns = (String) it.next();
            List files = (List) _contributions.get(ns);
            for (int i = 0; i < files.size(); i++)
                if (modifiedFiles.contains(files.get(i)))
                    result.add(ns);
        }
        return result;
    }
}
