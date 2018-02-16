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

package org.apache.xmlbeans.impl.xpath.saxon;

import java.util.List;
import java.util.Map;
import java.util.ListIterator;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Node;

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.NodeWrapper;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.VirtualNode;
import net.sf.saxon.om.Item;
import net.sf.saxon.value.Value;
import net.sf.saxon.sxpath.XPathEvaluator;
import net.sf.saxon.sxpath.XPathExpression;
import net.sf.saxon.sxpath.IndependentContext;
import net.sf.saxon.sxpath.XPathDynamicContext;
import net.sf.saxon.sxpath.XPathVariable;

import org.apache.xmlbeans.impl.store.PathDelegate;

public class XBeansXPath
        implements PathDelegate.SelectPathInterface
{
    private Object[] namespaceMap;
    private String path;
    private String contextVar;
    private String defaultNS;

    /**
     * Construct given an XPath expression string.
     * @param path The XPath expression
     * @param contextVar The name of the context variable
     * @param namespaceMap a map of prefix/uri bindings for NS support
     * @param defaultNS the uri for the default element NS, if any
     */
    public XBeansXPath(String path, String contextVar,
                       Map namespaceMap, String defaultNS)
    {
        this.path = path;
        this.contextVar = contextVar;
        this.defaultNS = defaultNS;
        this.namespaceMap = namespaceMap.entrySet().toArray();
    }

    /**
     * Select all nodes that are selectable by this XPath
     * expression. If multiple nodes match, multiple nodes
     * will be returned.
     * <p/>
     * <p/>
     * <b>NOTE:</b> In most cases, nodes will be returned
     * in document-order, as defined by the XML Canonicalization
     * specification.  The exception occurs when using XPath
     * expressions involving the <code>union</code> operator
     * (denoted with the pipe '|' character).
     * </p>
     * <p/>
     * <p/>
     * <b>NOTE:</b> Param node must be a DOM node which will be used
     * during the xpath execution and iteration through the results. 
     * A call of node.dispose() must be done after reading all results.
     * </p>
     *
     * @param node The node, nodeset or Context object for evaluation.
     * This value can be null.
     * @return The <code>List</code> of all items selected
     *         by this XPath expression.
     */
    public List selectNodes(Object node)
    {
        try
        {
            Node contextNode = (Node)node;
            XPathEvaluator xpe = new XPathEvaluator();
            Configuration config = new Configuration();
            config.setDOMLevel(2);
            config.setTreeModel(net.sf.saxon.event.Builder.STANDARD_TREE);
            IndependentContext sc = new IndependentContext(config);
            // Declare ns bindings
            if (defaultNS != null)
                sc.setDefaultElementNamespace(defaultNS);

            for (int i = 0; i < namespaceMap.length; i++)
            {
                Map.Entry entry = (Map.Entry) namespaceMap[i];
                sc.declareNamespace((String) entry.getKey(),
                        (String) entry.getValue());
            }
            xpe.setStaticContext(sc);
            XPathVariable thisVar = xpe.declareVariable("", contextVar);
            XPathExpression xpath = xpe.createExpression(path);
            NodeInfo contextItem = 
                //config.buildDocument(new DOMSource(contextNode));
                config.unravel(new DOMSource(contextNode));
            XPathDynamicContext dc = xpath.createDynamicContext(null);
            dc.setContextItem(contextItem);
            dc.setVariable(thisVar, contextItem);

            List saxonNodes = xpath.evaluate(dc);
            for (ListIterator it = saxonNodes.listIterator(); it.hasNext(); )
            {
                Object o = it.next();
                if (o instanceof NodeInfo)
                {
                    if (o instanceof NodeWrapper)
                    {
                        Node n = getUnderlyingNode((NodeWrapper)o);
                        it.set(n);
                    }
                    else
                    {
                        it.set(((NodeInfo)o).getStringValue());
                    }
                }
                else if (o instanceof Item)
                    it.set(Value.convertToJava((Item)o));
            }
            return saxonNodes;
        }
        catch (TransformerException e)
        {
            throw new RuntimeException(e);
        }
    }

    public List selectPath(Object node)
    {
        return selectNodes(node);
    }

    /**
     * According to the Saxon javadoc: 
     * <code>getUnderlyingNode</code> in <code>NodeWrapper</code> implements 
     * the method specified in the interface <code>VirtualNode</code>, and
     * the specification of the latter says that it may return another
     * <code>VirtualNode</code>, and you may have to drill down through
     * several layers of wrapping.
     * To be safe, this method is provided to drill down through multiple
     * layers of wrapping.
     * @param v The <code>VirtualNode</code>
     * @return The underlying node
     */
    private static Node getUnderlyingNode(VirtualNode v)
    {
        Object o = v;
        while (o instanceof VirtualNode)
        {
            o = ((VirtualNode)o).getUnderlyingNode();
        }
        return (Node)o;
    }

}
