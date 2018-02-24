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

package org.apache.xmlbeans;

import org.w3c.dom.Node;
import org.w3c.dom.DOMImplementation;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.ref.SoftReference;

import javax.xml.stream.XMLStreamReader;

import org.apache.xmlbeans.xml.stream.XMLInputStream;
import org.apache.xmlbeans.xml.stream.XMLStreamException;

/**
 * A hook for the XML Bean Factory mechanism.
 * Provided for advanced users who wish to provide their own
 * implementation of the Factory.parse methods. This is used, for example,
 * to defer reading XML streams until needed.
 * <p>
 * To use the hook, call XmlFactoryHook.ThreadContext.setHook(), passing
 * your own XmlFactoryHook implementation.  Then every call to a Factory
 * method will be delgated to your hook.
 *
 * <pre>
 * MyHook hook = new MyHook();
 * XmlFactoryHook.ThreadContext.setHook(hook);
 * // this results in a call to hook.parse(...)
 * XmlObject.Factory.parse(new File("test.xml"));
 * </pre>
 * 
 * If the hook needs to turn around and invoke the built-in parsers, then
 * it should do so by calling the appropriate method on the passed
 * SchemaTypeLoader.  Since SchemaTypeLoader.parse() methods delegate
 * to the registered hook, a hook that wishes to actually invoke the
 * default parser without having itself called back again should
 * unregister itself before calling loader.parse(), and then re-register
 * itself again after the call.
 * <pre>
 * void parse(SchemaTypeLoader loader, ...)
 * {
 *     XmlFactoryHook remember = XmlFactoryHook.ThreadContext.getHook();
 *     XmlFactoryHook.ThreadContext.setHook(null);
 *     loader.parse(...); // isn't hooked.
 *     XmlFactoryHook.ThreadContext.setHook(remember);
 * }
 * </pre>
 */
public interface XmlFactoryHook
{
    /** Hooks Factory.newInstance calls */
    public XmlObject newInstance ( SchemaTypeLoader loader, SchemaType type, XmlOptions options );
    /** Hooks Factory.parse calls */
    public XmlObject parse ( SchemaTypeLoader loader, String xmlText, SchemaType type, XmlOptions options ) throws XmlException;
    /** Hooks Factory.parse calls */
    public XmlObject parse ( SchemaTypeLoader loader, InputStream jiois, SchemaType type, XmlOptions options ) throws XmlException, IOException;
    /** Hooks Factory.parse calls */
    public XmlObject parse ( SchemaTypeLoader loader, XMLStreamReader xsr, SchemaType type, XmlOptions options ) throws XmlException;
    /** Hooks Factory.parse calls */
    public XmlObject parse ( SchemaTypeLoader loader, Reader jior, SchemaType type, XmlOptions options ) throws XmlException, IOException;
    /** Hooks Factory.parse calls */
    public XmlObject parse ( SchemaTypeLoader loader, Node node, SchemaType type, XmlOptions options ) throws XmlException;
    /** Hooks Factory.parse calls
      * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
      */
    public XmlObject parse ( SchemaTypeLoader loader, XMLInputStream xis, SchemaType type, XmlOptions options ) throws XmlException, XMLStreamException;
    /** Hooks Factory.newXmlSaxHandler calls */
    public XmlSaxHandler newXmlSaxHandler ( SchemaTypeLoader loader, SchemaType type, XmlOptions options );
    /** Hooks Factory.newDomImplementation calls */
    public DOMImplementation newDomImplementation ( SchemaTypeLoader loader, XmlOptions options );

    /**
     * Used to manage the XmlFactoryHook for the current thread.
     */ 
    public final static class ThreadContext
    {
        private static ThreadLocal threadHook = new ThreadLocal();

        /**
         * Returns the current thread's hook, or null if none.
         */ 
        public static XmlFactoryHook getHook()
        {
            SoftReference softRef = (SoftReference)threadHook.get();
            return softRef==null ? null : (XmlFactoryHook)softRef.get();
        }

        /**
         * Sets the hook for the current thread.
         */ 
        public static void setHook(XmlFactoryHook hook)
        {
            threadHook.set(new SoftReference(hook));
        }

        // provided to prevent unwanted construction
        private ThreadContext()
        {
        }
    }
}
