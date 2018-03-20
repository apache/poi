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

package org.apache.xmlbeans.impl.xquery.saxon;

import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.ListIterator;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Node;

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;

import org.apache.xmlbeans.XmlRuntimeException;
import org.apache.xmlbeans.XmlTokenSource;
import org.apache.xmlbeans.impl.store.QueryDelegate;

public class XBeansXQuery
        implements QueryDelegate.QueryInterface
{
    private XQueryExpression xquery;
    private String contextVar;
    private Configuration config;

    /**
     * Construct given an XQuery expression string.
     * @param query The XQuery expression
     * @param contextVar The name of the context variable
     * @param boundary The offset of the end of the prolog
     */
    public XBeansXQuery(String query, String contextVar, Integer boundary)
    {
        config = new Configuration();
        config.setDOMLevel(2);
        config.setTreeModel(net.sf.saxon.event.Builder.STANDARD_TREE);
        StaticQueryContext sc = new StaticQueryContext(config);
        this.contextVar = contextVar;
        int bdry = boundary.intValue();
        //Saxon requires external variables at the end of the prolog...
        query = (bdry == 0) ?
                "declare variable $" +
                contextVar + " external;" + query :
                query.substring(0, bdry) +
                "declare variable $" +
                contextVar + " external;" +
                query.substring(bdry);
        try
        {
            xquery = sc.compileQuery(query);
        }
        catch (TransformerException e)
        {
            throw new XmlRuntimeException(e);
        }
    }

    public List execQuery(Object node, Map variableBindings)
    {
        try
        {
            Node contextNode = (Node)node;
            NodeInfo contextItem = 
                config.buildDocument(new DOMSource(contextNode));
                //config.unravel(new DOMSource(contextNode));
            DynamicQueryContext dc = new DynamicQueryContext(config);
            dc.setContextItem(contextItem);
            dc.setParameter(contextVar, contextItem);
            // Set the other variables
            if (variableBindings != null)
            {
                for (Iterator it = variableBindings.entrySet().iterator();
                    it.hasNext(); )
                {
                    Map.Entry entry = (Map.Entry)it.next();
                    String key = (String)entry.getKey();
                    Object value = entry.getValue();
                    if (value instanceof XmlTokenSource)
                    {
                        Node paramObject = ((XmlTokenSource)value).getDomNode();
                        dc.setParameter(key, paramObject);
                    }
                    else if (value instanceof String)
                        dc.setParameter(key, value);
                }
            }

            List saxonNodes = xquery.evaluate(dc);
            for (ListIterator it = saxonNodes.listIterator(); it.hasNext(); )
            {
                Object o = it.next();
                if(o instanceof NodeInfo)
                {
                    Node n = NodeOverNodeInfo.wrap((NodeInfo)o);
                    it.set(n);
                }
            }
            return saxonNodes;
        }
        catch (TransformerException e)
        {
            throw new RuntimeException("Error binding " + contextVar, e);
        }
    }
}
