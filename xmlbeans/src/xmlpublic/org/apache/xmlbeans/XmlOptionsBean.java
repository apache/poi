package org.apache.xmlbeans;

import org.xml.sax.XMLReader;
import org.xml.sax.EntityResolver;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.Set;

/**
 * Same as {@link XmlOptions} but adhering to JavaBean conventions
 */
public class XmlOptionsBean extends XmlOptions
{
    /**
     * Construct a new blank XmlOptions.
     */
    public XmlOptionsBean ( ) { }

    /**
     * Construct a new XmlOptions, copying the options.
     * @param other the source <code>XmlOptions</code> object
     */
    public XmlOptionsBean (XmlOptions other) {
        super( other );
    }


    public void setSaveNamespacesFirst(boolean b)
    {
        if (b)
            super.setSaveNamespacesFirst();
        else
            remove( SAVE_NAMESPACES_FIRST );
    }

    public boolean isSaveNamespacesFirst()
    {
        return hasOption( SAVE_NAMESPACES_FIRST );
    }

    public void setSavePrettyPrint(boolean b)
    {
        if (b)
            super.setSavePrettyPrint();
        else
            remove( SAVE_PRETTY_PRINT );
    }

    public boolean isSavePrettyPrint()
    {
        return hasOption( SAVE_PRETTY_PRINT );
    }

    public Integer getSavePrettyPrintIndent()
    {
        return (Integer) get( SAVE_PRETTY_PRINT_INDENT );
    }

    public Integer getSavePrettyPrintOffset()
    {
        return (Integer) get( SAVE_PRETTY_PRINT_OFFSET );
    }

    public String getCharacterEncoding()
    {
        return (String) get( CHARACTER_ENCODING );
    }

    public SchemaType getDocumentType()
    {
        return (SchemaType) get ( DOCUMENT_TYPE );
    }

    public void setSaveAggressiveNamespaces(boolean b)
    {
        if (b)
            super.setSaveAggressiveNamespaces();
        else
            remove( SAVE_AGGRESSIVE_NAMESPACES );
    }

    public boolean isSaveAggressiveNamespaces()
    {
        return hasOption( SAVE_AGGRESSIVE_NAMESPACES );
    }

    public QName getSaveSyntheticDocumentElement()
    {
        return (QName) get( SAVE_SYNTHETIC_DOCUMENT_ELEMENT );
    }

    public void setUseDefaultNamespace(boolean b)
    {
        if (b)
            super.setUseDefaultNamespace();
        else
            remove( SAVE_USE_DEFAULT_NAMESPACE );
    }

    public boolean isUseDefaultNamespace()
    {
        return hasOption( SAVE_USE_DEFAULT_NAMESPACE );
    }

    public Map getSaveImplicitNamespaces()
    {
        return (Map) get( SAVE_IMPLICIT_NAMESPACES );
    }

    public Map getSaveSuggestedPrefixes()
    {
        return (Map) get( SAVE_SUGGESTED_PREFIXES );
    }

    public String getSaveFilterProcinst()
    {
        return (String) get( SAVE_FILTER_PROCINST );
    }

    public XmlOptionCharEscapeMap getSaveSubstituteCharacters()
    {
        return (XmlOptionCharEscapeMap) get( SAVE_SUBSTITUTE_CHARACTERS );
    }

    public void setSaveUseOpenFrag(boolean b)
    {
        if (b)
            super.setSaveUseOpenFrag();
        else
            remove( SAVE_USE_OPEN_FRAGMENT );
    }

    public boolean isSaveUseOpenFrag()
    {
        return hasOption( SAVE_USE_OPEN_FRAGMENT );
    }

    public void setSaveOuter(boolean b)
    {
        if (b)
            super.setSaveOuter();
        else
            remove( SAVE_OUTER );
    }

    public boolean isSaveOuter()
    {
        return hasOption( SAVE_OUTER );
    }

    public void setSaveInner(boolean b)
    {
        if (b)
            super.setSaveInner();
        else
            remove( SAVE_INNER );
    }

    public boolean isSaveInner()
    {
        return hasOption( SAVE_INNER );
    }

    public void setSaveNoXmlDecl(boolean b)
    {
        if (b)
            super.setSaveNoXmlDecl();
        else
            remove( SAVE_NO_XML_DECL );
    }

    public boolean isSaveNoXmlDecl()
    {
        return hasOption( SAVE_NO_XML_DECL );
    }

    public Integer getSaveCDataLengthThreshold()
    {
        return (Integer) get( SAVE_CDATA_LENGTH_THRESHOLD );
    }

    public Integer getSaveCDataEntityCountThreshold()
    {
        return (Integer) get( SAVE_CDATA_ENTITY_COUNT_THRESHOLD );
    }

    public void setSaveSaxNoNSDeclsInAttributes(boolean b)
    {
        if (b)
            super.setSaveSaxNoNSDeclsInAttributes();
        else
            remove( SAVE_SAX_NO_NSDECLS_IN_ATTRIBUTES );
    }

    public boolean isSaveSaxNoNSDeclsInAttributes()
    {
        return hasOption( SAVE_SAX_NO_NSDECLS_IN_ATTRIBUTES );
    }

    public QName getLoadReplaceDocumentElement()
    {
        return (QName) get( LOAD_REPLACE_DOCUMENT_ELEMENT );
    }

    public void setLoadStripWhitespace(boolean b)
    {
        if (b)
            super.setLoadStripWhitespace();
        else
            remove( LOAD_STRIP_WHITESPACE );
    }

    public boolean isSetLoadStripWhitespace()
    {
        return hasOption( LOAD_STRIP_WHITESPACE );
    }

    public void setLoadStripComments(boolean b)
    {
        if (b)
            super.setLoadStripComments();
        else
            remove( LOAD_STRIP_COMMENTS );
    }

    public boolean isLoadStripComments()
    {
        return hasOption( LOAD_STRIP_COMMENTS );
    }

    public void setLoadStripProcinsts(boolean b)
    {
        if (b)
            super.setLoadStripProcinsts();
        else
            remove( LOAD_STRIP_PROCINSTS );
    }

    public boolean isLoadStripProcinsts()
    {
        return hasOption( LOAD_STRIP_PROCINSTS );
    }

    public void setLoadLineNumbers(boolean b)
    {
        if (b)
            super.setLoadLineNumbers();
        else
            remove( LOAD_LINE_NUMBERS );
    }

    public boolean isLoadLineNumbers()
    {
        return hasOption( LOAD_LINE_NUMBERS );
    }

    public Map getLoadSubstituteNamespaces()
    {
        return (Map) get( LOAD_SUBSTITUTE_NAMESPACES );
    }

    public void setLoadTrimTextBuffer(boolean b)
    {
        if (b)
            super.setLoadTrimTextBuffer();
        else
            remove( LOAD_TRIM_TEXT_BUFFER );
    }

    public boolean isLoadTrimTextBuffer()
    {
        return hasOption( LOAD_TRIM_TEXT_BUFFER );
    }

    public Map getLoadAdditionalNamespaces()
    {
        return (Map) get( LOAD_ADDITIONAL_NAMESPACES );
    }

    public void setLoadMessageDigest(boolean b)
    {
        if (b)
            super.setLoadMessageDigest();
        else
            remove( LOAD_MESSAGE_DIGEST );
    }

    public boolean isLoadMessageDigest()
    {
        return hasOption( LOAD_MESSAGE_DIGEST );
    }

    public void setLoadUseDefaultResolver(boolean b)
    {
        if (b)
            super.setLoadUseDefaultResolver();
        else
            remove( LOAD_USE_DEFAULT_RESOLVER );
    }

    public boolean isLoadUseDefaultResolver()
    {
        return hasOption( LOAD_USE_DEFAULT_RESOLVER );
    }

    public String getXqueryCurrentNodeVar()
    {
        return (String) get( XQUERY_CURRENT_NODE_VAR );
    }

    public Map getXqueryVariables()
    {
        return (Map) get( XQUERY_VARIABLE_MAP );
    }

    public String getDocumentSourceName()
    {
        return (String) get( DOCUMENT_SOURCE_NAME );
    }

    public Map getCompileSubstituteNames()
    {
        return (Map) get( COMPILE_SUBSTITUTE_NAMES );
    }

    public void setCompileNoUpaRule(boolean b)
    {
        if (b)
            super.setCompileNoUpaRule();
        else
            remove( COMPILE_NO_UPA_RULE );
    }

    public boolean isCompileNoUpaRule()
    {
        return hasOption( COMPILE_NO_UPA_RULE );
    }

    public void setCompileNoPvrRule(boolean b)
    {
        if (b)
            super.setCompileNoPvrRule();
        else
            remove( COMPILE_NO_PVR_RULE );
    }

    public boolean isCompileNoPvrRule()
    {
        return hasOption( COMPILE_NO_PVR_RULE );
    }

    public void setCompileNoAnnotations(boolean b)
    {
        if (b)
            super.setCompileNoAnnotations();
        else
            remove( COMPILE_NO_ANNOTATIONS );
    }

    public boolean isCompileNoAnnotations()
    {
        return hasOption( COMPILE_NO_ANNOTATIONS );
    }

    public void setCompileDownloadUrls(boolean b)
    {
        if (b)
            super.setCompileDownloadUrls();
        else
            remove( COMPILE_DOWNLOAD_URLS );
    }

    public boolean isCompileDownloadUrls()
    {
        return hasOption( COMPILE_DOWNLOAD_URLS );
    }

    public Set getCompileMdefNamespaces()
    {
        return (Set) get( COMPILE_MDEF_NAMESPACES );
    }

    public void setValidateOnSet(boolean b)
    {
        if (b)
            super.setValidateOnSet();
        else
            remove( VALIDATE_ON_SET );
    }

    public boolean isValidateOnSet()
    {
        return hasOption( VALIDATE_ON_SET );
    }

    public void setValidateTreatLaxAsSkip(boolean b)
    {
        if (b)
            super.setValidateTreatLaxAsSkip();
        else
            remove( VALIDATE_TREAT_LAX_AS_SKIP );
    }

    public boolean isValidateTreatLaxAsSkip()
    {
        return hasOption( VALIDATE_TREAT_LAX_AS_SKIP );
    }

    public void setValidateStrict(boolean b)
    {
        if (b)
            super.setValidateStrict();
        else
            remove( VALIDATE_STRICT );
    }

    public boolean isValidateStrict()
    {
        return hasOption( VALIDATE_STRICT );
    }

    public void setUnsynchronized(boolean b)
    {
        if (b)
            super.setUnsynchronized();
        else
            remove( UNSYNCHRONIZED );
    }

    public boolean isUnsynchronized()
    {
        return hasOption( UNSYNCHRONIZED );
    }

    public EntityResolver getEntityResolver()
    {
        return (EntityResolver) get( ENTITY_RESOLVER );
    }

    public String getGenerateJavaVersion()
    {
        return (String) get( GENERATE_JAVA_VERSION );
    }
}
