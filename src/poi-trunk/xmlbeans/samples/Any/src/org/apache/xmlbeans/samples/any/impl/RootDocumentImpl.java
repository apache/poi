/*
 * An XML document type.
 * Localname: root
 * Namespace: http://xmlbeans.apache.org/samples/any
 * Java type: org.apache.xmlbeans.samples.any.RootDocument
 *
 * Automatically generated - do not modify.
 */
package org.apache.xmlbeans.samples.any.impl;
/**
 * A document containing one root(@http://xmlbeans.apache.org/samples/any) element.
 *
 * This is a complex type.
 */
public class RootDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.apache.xmlbeans.samples.any.RootDocument
{
    
    public RootDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ROOT$0 = 
        new javax.xml.namespace.QName("http://xmlbeans.apache.org/samples/any", "root");
    
    
    /**
     * Gets the "root" element
     */
    public org.apache.xmlbeans.samples.any.RootDocument.Root getRoot()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.samples.any.RootDocument.Root target = null;
            target = (org.apache.xmlbeans.samples.any.RootDocument.Root)get_store().find_element_user(ROOT$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "root" element
     */
    public void setRoot(org.apache.xmlbeans.samples.any.RootDocument.Root root)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.samples.any.RootDocument.Root target = null;
            target = (org.apache.xmlbeans.samples.any.RootDocument.Root)get_store().find_element_user(ROOT$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.samples.any.RootDocument.Root)get_store().add_element_user(ROOT$0);
            }
            target.set(root);
        }
    }
    
    /**
     * Appends and returns a new empty "root" element
     */
    public org.apache.xmlbeans.samples.any.RootDocument.Root addNewRoot()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.samples.any.RootDocument.Root target = null;
            target = (org.apache.xmlbeans.samples.any.RootDocument.Root)get_store().add_element_user(ROOT$0);
            return target;
        }
    }
    /**
     * An XML root(@http://xmlbeans.apache.org/samples/any).
     *
     * This is a complex type.
     */
    public static class RootImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.apache.xmlbeans.samples.any.RootDocument.Root
    {
        
        public RootImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType);
        }
        
        private static final javax.xml.namespace.QName STRINGELEMENT$0 = 
            new javax.xml.namespace.QName("http://xmlbeans.apache.org/samples/any", "stringelement");
        private static final javax.xml.namespace.QName ARRAYOFANY$2 = 
            new javax.xml.namespace.QName("http://xmlbeans.apache.org/samples/any", "arrayofany");
        
        
        /**
         * Gets the "stringelement" element
         */
        public java.lang.String getStringelement()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(STRINGELEMENT$0, 0);
                if (target == null)
                {
                    return null;
                }
                return target.getStringValue();
            }
        }
        
        /**
         * Gets (as xml) the "stringelement" element
         */
        public org.apache.xmlbeans.XmlString xgetStringelement()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlString target = null;
                target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(STRINGELEMENT$0, 0);
                return target;
            }
        }
        
        /**
         * Sets the "stringelement" element
         */
        public void setStringelement(java.lang.String stringelement)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(STRINGELEMENT$0, 0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(STRINGELEMENT$0);
                }
                target.setStringValue(stringelement);
            }
        }
        
        /**
         * Sets (as xml) the "stringelement" element
         */
        public void xsetStringelement(org.apache.xmlbeans.XmlString stringelement)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlString target = null;
                target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(STRINGELEMENT$0, 0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(STRINGELEMENT$0);
                }
                target.set(stringelement);
            }
        }
        
        /**
         * Gets the "arrayofany" element
         */
        public org.apache.xmlbeans.samples.any.RootDocument.Root.Arrayofany getArrayofany()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.samples.any.RootDocument.Root.Arrayofany target = null;
                target = (org.apache.xmlbeans.samples.any.RootDocument.Root.Arrayofany)get_store().find_element_user(ARRAYOFANY$2, 0);
                if (target == null)
                {
                    return null;
                }
                return target;
            }
        }
        
        /**
         * Sets the "arrayofany" element
         */
        public void setArrayofany(org.apache.xmlbeans.samples.any.RootDocument.Root.Arrayofany arrayofany)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.samples.any.RootDocument.Root.Arrayofany target = null;
                target = (org.apache.xmlbeans.samples.any.RootDocument.Root.Arrayofany)get_store().find_element_user(ARRAYOFANY$2, 0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.samples.any.RootDocument.Root.Arrayofany)get_store().add_element_user(ARRAYOFANY$2);
                }
                target.set(arrayofany);
            }
        }
        
        /**
         * Appends and returns a new empty "arrayofany" element
         */
        public org.apache.xmlbeans.samples.any.RootDocument.Root.Arrayofany addNewArrayofany()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.samples.any.RootDocument.Root.Arrayofany target = null;
                target = (org.apache.xmlbeans.samples.any.RootDocument.Root.Arrayofany)get_store().add_element_user(ARRAYOFANY$2);
                return target;
            }
        }
        /**
         * An XML arrayofany(@http://xmlbeans.apache.org/samples/any).
         *
         * This is a complex type.
         */
        public static class ArrayofanyImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.apache.xmlbeans.samples.any.RootDocument.Root.Arrayofany
        {
            
            public ArrayofanyImpl(org.apache.xmlbeans.SchemaType sType)
            {
                super(sType);
            }
            
            
        }
    }
}
