/*
 * An XML document type.
 * Localname: todolist
 * Namespace: http://xmlbeans.apache.org/samples/validation/todolist
 * Java type: org.apache.xmlbeans.samples.validation.todolist.TodolistDocument
 *
 * Automatically generated - do not modify.
 */
package org.apache.xmlbeans.samples.validation.todolist.impl;
/**
 * A document containing one todolist(@http://xmlbeans.apache.org/samples/validation/todolist) element.
 *
 * This is a complex type.
 */
public class TodolistDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.apache.xmlbeans.samples.validation.todolist.TodolistDocument
{
    
    public TodolistDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName TODOLIST$0 = 
        new javax.xml.namespace.QName("http://xmlbeans.apache.org/samples/validation/todolist", "todolist");
    
    
    /**
     * Gets the "todolist" element
     */
    public org.apache.xmlbeans.samples.validation.todolist.TodolistDocument.Todolist getTodolist()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.samples.validation.todolist.TodolistDocument.Todolist target = null;
            target = (org.apache.xmlbeans.samples.validation.todolist.TodolistDocument.Todolist)get_store().find_element_user(TODOLIST$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "todolist" element
     */
    public void setTodolist(org.apache.xmlbeans.samples.validation.todolist.TodolistDocument.Todolist todolist)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.samples.validation.todolist.TodolistDocument.Todolist target = null;
            target = (org.apache.xmlbeans.samples.validation.todolist.TodolistDocument.Todolist)get_store().find_element_user(TODOLIST$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.samples.validation.todolist.TodolistDocument.Todolist)get_store().add_element_user(TODOLIST$0);
            }
            target.set(todolist);
        }
    }
    
    /**
     * Appends and returns a new empty "todolist" element
     */
    public org.apache.xmlbeans.samples.validation.todolist.TodolistDocument.Todolist addNewTodolist()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.samples.validation.todolist.TodolistDocument.Todolist target = null;
            target = (org.apache.xmlbeans.samples.validation.todolist.TodolistDocument.Todolist)get_store().add_element_user(TODOLIST$0);
            return target;
        }
    }
    /**
     * An XML todolist(@http://xmlbeans.apache.org/samples/validation/todolist).
     *
     * This is a complex type.
     */
    public static class TodolistImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.apache.xmlbeans.samples.validation.todolist.TodolistDocument.Todolist
    {
        
        public TodolistImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType);
        }
        
        private static final javax.xml.namespace.QName ITEM$0 = 
            new javax.xml.namespace.QName("http://xmlbeans.apache.org/samples/validation/todolist", "item");
        
        
        /**
         * Gets array of all "item" elements
         */
        public org.apache.xmlbeans.samples.validation.todolist.ItemType[] getItemArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                java.util.List targetList = new java.util.ArrayList();
                get_store().find_all_element_users(ITEM$0, targetList);
                org.apache.xmlbeans.samples.validation.todolist.ItemType[] result = new org.apache.xmlbeans.samples.validation.todolist.ItemType[targetList.size()];
                targetList.toArray(result);
                return result;
            }
        }
        
        /**
         * Gets ith "item" element
         */
        public org.apache.xmlbeans.samples.validation.todolist.ItemType getItemArray(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.samples.validation.todolist.ItemType target = null;
                target = (org.apache.xmlbeans.samples.validation.todolist.ItemType)get_store().find_element_user(ITEM$0, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                return target;
            }
        }
        
        /**
         * Returns number of "item" element
         */
        public int sizeOfItemArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().count_elements(ITEM$0);
            }
        }
        
        /**
         * Sets array of all "item" element
         */
        public void setItemArray(org.apache.xmlbeans.samples.validation.todolist.ItemType[] itemArray)
        {
            synchronized (monitor())
            {
                check_orphaned();
                arraySetterHelper(itemArray, ITEM$0);
            }
        }
        
        /**
         * Sets ith "item" element
         */
        public void setItemArray(int i, org.apache.xmlbeans.samples.validation.todolist.ItemType item)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.samples.validation.todolist.ItemType target = null;
                target = (org.apache.xmlbeans.samples.validation.todolist.ItemType)get_store().find_element_user(ITEM$0, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                target.set(item);
            }
        }
        
        /**
         * Inserts and returns a new empty value (as xml) as the ith "item" element
         */
        public org.apache.xmlbeans.samples.validation.todolist.ItemType insertNewItem(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.samples.validation.todolist.ItemType target = null;
                target = (org.apache.xmlbeans.samples.validation.todolist.ItemType)get_store().insert_element_user(ITEM$0, i);
                return target;
            }
        }
        
        /**
         * Appends and returns a new empty value (as xml) as the last "item" element
         */
        public org.apache.xmlbeans.samples.validation.todolist.ItemType addNewItem()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.samples.validation.todolist.ItemType target = null;
                target = (org.apache.xmlbeans.samples.validation.todolist.ItemType)get_store().add_element_user(ITEM$0);
                return target;
            }
        }
        
        /**
         * Removes the ith "item" element
         */
        public void removeItem(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_element(ITEM$0, i);
            }
        }
    }
}
