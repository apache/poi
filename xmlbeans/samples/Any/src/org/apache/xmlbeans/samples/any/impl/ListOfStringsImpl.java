/*
 * XML Type:  ListOfStrings
 * Namespace: http://xmlbeans.apache.org/samples/any
 * Java type: org.apache.xmlbeans.samples.any.ListOfStrings
 *
 * Automatically generated - do not modify.
 */
package org.apache.xmlbeans.samples.any.impl;
/**
 * An XML ListOfStrings(@http://xmlbeans.apache.org/samples/any).
 *
 * This is a complex type.
 */
public class ListOfStringsImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.apache.xmlbeans.samples.any.ListOfStrings
{
    
    public ListOfStringsImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName STRINGELEMENT$0 = 
        new javax.xml.namespace.QName("http://xmlbeans.apache.org/samples/any", "stringelement");
    private static final javax.xml.namespace.QName ID$2 = 
        new javax.xml.namespace.QName("", "id");
    
    
    /**
     * Gets array of all "stringelement" elements
     */
    public java.lang.String[] getStringelementArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(STRINGELEMENT$0, targetList);
            java.lang.String[] result = new java.lang.String[targetList.size()];
            for (int i = 0, len = targetList.size() ; i < len ; i++)
                result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getStringValue();
            return result;
        }
    }
    
    /**
     * Gets ith "stringelement" element
     */
    public java.lang.String getStringelementArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(STRINGELEMENT$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) array of all "stringelement" elements
     */
    public org.apache.xmlbeans.XmlString[] xgetStringelementArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(STRINGELEMENT$0, targetList);
            org.apache.xmlbeans.XmlString[] result = new org.apache.xmlbeans.XmlString[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets (as xml) ith "stringelement" element
     */
    public org.apache.xmlbeans.XmlString xgetStringelementArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(STRINGELEMENT$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return (org.apache.xmlbeans.XmlString)target;
        }
    }
    
    /**
     * Returns number of "stringelement" element
     */
    public int sizeOfStringelementArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(STRINGELEMENT$0);
        }
    }
    
    /**
     * Sets array of all "stringelement" element
     */
    public void setStringelementArray(java.lang.String[] stringelementArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(stringelementArray, STRINGELEMENT$0);
        }
    }
    
    /**
     * Sets ith "stringelement" element
     */
    public void setStringelementArray(int i, java.lang.String stringelement)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(STRINGELEMENT$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.setStringValue(stringelement);
        }
    }
    
    /**
     * Sets (as xml) array of all "stringelement" element
     */
    public void xsetStringelementArray(org.apache.xmlbeans.XmlString[]stringelementArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(stringelementArray, STRINGELEMENT$0);
        }
    }
    
    /**
     * Sets (as xml) ith "stringelement" element
     */
    public void xsetStringelementArray(int i, org.apache.xmlbeans.XmlString stringelement)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(STRINGELEMENT$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(stringelement);
        }
    }
    
    /**
     * Inserts the value as the ith "stringelement" element
     */
    public void insertStringelement(int i, java.lang.String stringelement)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = 
                (org.apache.xmlbeans.SimpleValue)get_store().insert_element_user(STRINGELEMENT$0, i);
            target.setStringValue(stringelement);
        }
    }
    
    /**
     * Appends the value as the last "stringelement" element
     */
    public void addStringelement(java.lang.String stringelement)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(STRINGELEMENT$0);
            target.setStringValue(stringelement);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "stringelement" element
     */
    public org.apache.xmlbeans.XmlString insertNewStringelement(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().insert_element_user(STRINGELEMENT$0, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "stringelement" element
     */
    public org.apache.xmlbeans.XmlString addNewStringelement()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(STRINGELEMENT$0);
            return target;
        }
    }
    
    /**
     * Removes the ith "stringelement" element
     */
    public void removeStringelement(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(STRINGELEMENT$0, i);
        }
    }
    
    /**
     * Gets the "id" attribute
     */
    public java.lang.String getId()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ID$2);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "id" attribute
     */
    public org.apache.xmlbeans.XmlString xgetId()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(ID$2);
            return target;
        }
    }
    
    /**
     * True if has "id" attribute
     */
    public boolean isSetId()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(ID$2) != null;
        }
    }
    
    /**
     * Sets the "id" attribute
     */
    public void setId(java.lang.String id)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ID$2);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(ID$2);
            }
            target.setStringValue(id);
        }
    }
    
    /**
     * Sets (as xml) the "id" attribute
     */
    public void xsetId(org.apache.xmlbeans.XmlString id)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(ID$2);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_attribute_user(ID$2);
            }
            target.set(id);
        }
    }
    
    /**
     * Unsets the "id" attribute
     */
    public void unsetId()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(ID$2);
        }
    }
}
