/*
 * XML Type:  itemType
 * Namespace: http://xmlbeans.apache.org/samples/validation/todolist
 * Java type: org.apache.xmlbeans.samples.validation.todolist.ItemType
 *
 * Automatically generated - do not modify.
 */
package org.apache.xmlbeans.samples.validation.todolist;


/**
 * An XML itemType(@http://xmlbeans.apache.org/samples/validation/todolist).
 *
 * This is a complex type.
 */
public interface ItemType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)schema.system.s59A663BF38731BA9F8026B121E40FDD3.TypeSystemHolder.typeSystem.resolveHandle("itemtypeb663type");
    
    /**
     * Gets the "name" element
     */
    java.lang.String getName();
    
    /**
     * Gets (as xml) the "name" element
     */
    org.apache.xmlbeans.XmlString xgetName();
    
    /**
     * Sets the "name" element
     */
    void setName(java.lang.String name);
    
    /**
     * Sets (as xml) the "name" element
     */
    void xsetName(org.apache.xmlbeans.XmlString name);
    
    /**
     * Gets the "description" element
     */
    java.lang.String getDescription();
    
    /**
     * Gets (as xml) the "description" element
     */
    org.apache.xmlbeans.XmlString xgetDescription();
    
    /**
     * True if has "description" element
     */
    boolean isSetDescription();
    
    /**
     * Sets the "description" element
     */
    void setDescription(java.lang.String description);
    
    /**
     * Sets (as xml) the "description" element
     */
    void xsetDescription(org.apache.xmlbeans.XmlString description);
    
    /**
     * Unsets the "description" element
     */
    void unsetDescription();
    
    /**
     * Gets the "due_by" element
     */
    java.util.Calendar getDueBy();
    
    /**
     * Gets (as xml) the "due_by" element
     */
    org.apache.xmlbeans.XmlDateTime xgetDueBy();
    
    /**
     * True if has "due_by" element
     */
    boolean isSetDueBy();
    
    /**
     * Sets the "due_by" element
     */
    void setDueBy(java.util.Calendar dueBy);
    
    /**
     * Sets (as xml) the "due_by" element
     */
    void xsetDueBy(org.apache.xmlbeans.XmlDateTime dueBy);
    
    /**
     * Unsets the "due_by" element
     */
    void unsetDueBy();
    
    /**
     * Gets the "action" element
     */
    org.apache.xmlbeans.samples.validation.todolist.ActionType.Enum getAction();
    
    /**
     * Gets (as xml) the "action" element
     */
    org.apache.xmlbeans.samples.validation.todolist.ActionType xgetAction();
    
    /**
     * Sets the "action" element
     */
    void setAction(org.apache.xmlbeans.samples.validation.todolist.ActionType.Enum action);
    
    /**
     * Sets (as xml) the "action" element
     */
    void xsetAction(org.apache.xmlbeans.samples.validation.todolist.ActionType action);
    
    /**
     * Gets the "id" attribute
     */
    int getId();
    
    /**
     * Gets (as xml) the "id" attribute
     */
    org.apache.xmlbeans.samples.validation.todolist.IdType xgetId();
    
    /**
     * True if has "id" attribute
     */
    boolean isSetId();
    
    /**
     * Sets the "id" attribute
     */
    void setId(int id);
    
    /**
     * Sets (as xml) the "id" attribute
     */
    void xsetId(org.apache.xmlbeans.samples.validation.todolist.IdType id);
    
    /**
     * Unsets the "id" attribute
     */
    void unsetId();
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static org.apache.xmlbeans.samples.validation.todolist.ItemType newInstance() {
          return (org.apache.xmlbeans.samples.validation.todolist.ItemType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static org.apache.xmlbeans.samples.validation.todolist.ItemType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (org.apache.xmlbeans.samples.validation.todolist.ItemType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static org.apache.xmlbeans.samples.validation.todolist.ItemType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (org.apache.xmlbeans.samples.validation.todolist.ItemType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static org.apache.xmlbeans.samples.validation.todolist.ItemType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.apache.xmlbeans.samples.validation.todolist.ItemType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static org.apache.xmlbeans.samples.validation.todolist.ItemType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.xmlbeans.samples.validation.todolist.ItemType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static org.apache.xmlbeans.samples.validation.todolist.ItemType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.xmlbeans.samples.validation.todolist.ItemType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static org.apache.xmlbeans.samples.validation.todolist.ItemType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.xmlbeans.samples.validation.todolist.ItemType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static org.apache.xmlbeans.samples.validation.todolist.ItemType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.xmlbeans.samples.validation.todolist.ItemType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static org.apache.xmlbeans.samples.validation.todolist.ItemType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.xmlbeans.samples.validation.todolist.ItemType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static org.apache.xmlbeans.samples.validation.todolist.ItemType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.xmlbeans.samples.validation.todolist.ItemType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static org.apache.xmlbeans.samples.validation.todolist.ItemType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.xmlbeans.samples.validation.todolist.ItemType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static org.apache.xmlbeans.samples.validation.todolist.ItemType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.xmlbeans.samples.validation.todolist.ItemType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static org.apache.xmlbeans.samples.validation.todolist.ItemType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (org.apache.xmlbeans.samples.validation.todolist.ItemType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static org.apache.xmlbeans.samples.validation.todolist.ItemType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.apache.xmlbeans.samples.validation.todolist.ItemType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static org.apache.xmlbeans.samples.validation.todolist.ItemType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (org.apache.xmlbeans.samples.validation.todolist.ItemType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static org.apache.xmlbeans.samples.validation.todolist.ItemType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.apache.xmlbeans.samples.validation.todolist.ItemType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.apache.xmlbeans.samples.validation.todolist.ItemType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (org.apache.xmlbeans.samples.validation.todolist.ItemType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.apache.xmlbeans.samples.validation.todolist.ItemType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (org.apache.xmlbeans.samples.validation.todolist.ItemType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
