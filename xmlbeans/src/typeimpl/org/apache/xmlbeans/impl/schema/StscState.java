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

import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.impl.common.QNameHelper;
import org.apache.xmlbeans.impl.common.ResolverUtil;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaComponent;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaGlobalAttribute;
import org.apache.xmlbeans.SchemaIdentityConstraint;
import org.apache.xmlbeans.SchemaAttributeGroup;
import org.apache.xmlbeans.SchemaModelGroup;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.SystemProperties;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.BindingConfig;
import org.apache.xmlbeans.impl.values.XmlStringImpl;
import org.apache.xmlbeans.impl.values.XmlValueOutOfRangeException;
import org.apache.xmlbeans.impl.util.HexBin;

import java.util.*;
import java.net.URISyntaxException;
import java.net.URI;
import java.net.URL;
import java.io.File;


import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.xml.sax.EntityResolver;

/**
 * This class represents the state of the SchemaTypeSystemCompiler as it's
 * going.
 */
public class StscState
{
    private String _givenStsName;
    private Collection _errorListener;
    private SchemaTypeSystemImpl _target;
    private BindingConfig _config;
    private Map _compatMap;
    private boolean _doingDownloads;
    private byte[] _digest = null;
    private boolean _noDigest = false;
    
    // EXPERIMENTAL: recovery from compilation errors and partial type systems
    private boolean _allowPartial = false;
    private int _recoveredErrors = 0;

    private SchemaTypeLoader _importingLoader;

    private Map _containers = new LinkedHashMap();
    private SchemaDependencies _dependencies;

    private Map _redefinedGlobalTypes        = new LinkedHashMap();
    private Map _redefinedModelGroups        = new LinkedHashMap();
    private Map _redefinedAttributeGroups    = new LinkedHashMap();

    private Map _globalTypes        = new LinkedHashMap();
    private Map _globalElements     = new LinkedHashMap();
    private Map _globalAttributes   = new LinkedHashMap();
    private Map _modelGroups        = new LinkedHashMap();
    private Map _attributeGroups    = new LinkedHashMap();
    private Map _documentTypes      = new LinkedHashMap();
    private Map _attributeTypes     = new LinkedHashMap();
    private Map _typesByClassname   = new LinkedHashMap();
    private Map _misspelledNames    = new HashMap();
    private Set _processingGroups   = new LinkedHashSet();
    private Map _idConstraints      = new LinkedHashMap();
    private Set _namespaces         = new HashSet();
    private List _annotations       = new ArrayList();
    private boolean _noUpa;
    private boolean _noPvr;
    private boolean _noAnn;
    private boolean _mdefAll;
    private Set _mdefNamespaces     = buildDefaultMdefNamespaces();
    private EntityResolver _entityResolver;
    private File _schemasDir;

    private static Set buildDefaultMdefNamespaces()
    {
        // namespaces which are known to appear in WSDLs redundantly
        return new HashSet(
                Arrays.asList( new String[] {
                    "http://www.openuri.org/2002/04/soap/conversation/",
                }));
    }

    /**
     * Used to store the new target namespace for a chameleon
     * included schema.
     */
    public static final Object CHAMELEON_INCLUDE_URI = new Object();

    /**
     * Only constructed via StscState.start().
     */
    private StscState()
    {
    }

    /**
     * Initializer for incremental compilation
     */
    public void initFromTypeSystem(SchemaTypeSystemImpl system, Set newNamespaces)
    {
//         setGivenTypeSystemName(system.getName().substring(14));

        SchemaContainer[] containers = system.containers();
        for (int i = 0; i < containers.length; i++)
        {
            if (!newNamespaces.contains(containers[i].getNamespace()))
            {
                // Copy data from the given container
                addContainer(containers[i]);
            }
        }
    }


    /* CONTAINERS ================================================================*/

    void addNewContainer(String namespace)
    {
        if (_containers.containsKey(namespace))
            return;

        SchemaContainer container = new SchemaContainer(namespace);
        container.setTypeSystem(sts());
        addNamespace(namespace);
        _containers.put(namespace, container);
    }

    private void addContainer(SchemaContainer container)
    {
        _containers.put(container.getNamespace(), container);
        List redefModelGroups = container.redefinedModelGroups();
        for (int i = 0; i < redefModelGroups.size(); i++)
        {
            QName name = ((SchemaModelGroup) redefModelGroups.get(i)).getName();
            _redefinedModelGroups.put(name, redefModelGroups.get(i));
        }

        List redefAttrGroups = container.redefinedAttributeGroups();
        for (int i = 0; i < redefAttrGroups.size(); i++)
        {
            QName name = ((SchemaAttributeGroup) redefAttrGroups.get(i)).getName();
            _redefinedAttributeGroups.put(name, redefAttrGroups.get(i));
        }

        List redefTypes = container.redefinedGlobalTypes();
        for (int i = 0; i < redefTypes.size(); i++)
        {
            QName name = ((SchemaType) redefTypes.get(i)).getName();
            _redefinedGlobalTypes.put(name, redefTypes.get(i));
        }

        List globalElems = container.globalElements();
        for (int i = 0; i < globalElems.size(); i++)
        {
            QName name = ((SchemaGlobalElement) globalElems.get(i)).getName();
            _globalElements.put(name, globalElems.get(i));
        }

        List globalAtts = container.globalAttributes();
        for (int i = 0; i < globalAtts.size(); i++)
        {
            QName name = ((SchemaGlobalAttribute) globalAtts.get(i)).getName();
            _globalAttributes.put(name, globalAtts.get(i));
        }

        List modelGroups = container.modelGroups();
        for (int i = 0; i < modelGroups.size(); i++)
        {
            QName name = ((SchemaModelGroup) modelGroups.get(i)).getName();
            _modelGroups.put(name, modelGroups.get(i));
        }

        List attrGroups = container.attributeGroups();
        for (int i = 0; i < attrGroups.size(); i++)
        {
            QName name = ((SchemaAttributeGroup) attrGroups.get(i)).getName();
            _attributeGroups.put(name, attrGroups.get(i));
        }

        List globalTypes = container.globalTypes();
        for (int i = 0; i < globalTypes.size(); i++)
        {
            SchemaType t = (SchemaType) globalTypes.get(i);
            QName name = t.getName();
            _globalTypes.put(name, t);
            if (t.getFullJavaName() != null)
                addClassname(t.getFullJavaName(), t);
        }

        List documentTypes = container.documentTypes();
        for (int i = 0; i < documentTypes.size(); i++)
        {
            SchemaType t = (SchemaType) documentTypes.get(i);
            QName name = t.getProperties()[0].getName();
            _documentTypes.put(name, t);
            if (t.getFullJavaName() != null)
                addClassname(t.getFullJavaName(), t);
        }

        List attributeTypes = container.attributeTypes();
        for (int i = 0; i < attributeTypes.size(); i++)
        {
            SchemaType t = (SchemaType) attributeTypes.get(i);
            QName name = t.getProperties()[0].getName();
            _attributeTypes.put(name, t);
            if (t.getFullJavaName() != null)
                addClassname(t.getFullJavaName(), t);
        }

        List identityConstraints = container.identityConstraints();
        for (int i = 0; i < identityConstraints.size(); i++)
        {
            QName name = ((SchemaIdentityConstraint) identityConstraints.get(i)).getName();
            _idConstraints.put(name, identityConstraints.get(i));
        }

        _annotations.addAll(container.annotations());
        _namespaces.add(container.getNamespace());
        container.unsetImmutable();
    }

    SchemaContainer getContainer(String namespace)
    {
        return (SchemaContainer) _containers.get(namespace);
    }

    Map getContainerMap()
    {
        return Collections.unmodifiableMap(_containers);
    }

    /* DEPENDENCIES ================================================================*/

    void registerDependency(String sourceNs, String targetNs)
    {
        _dependencies.registerDependency(sourceNs, targetNs);
    }

    void registerContribution(String ns, String fileUrl)
    {
        _dependencies.registerContribution(ns, fileUrl);
    }

    SchemaDependencies getDependencies()
    {
        return _dependencies;
    }

    void setDependencies(SchemaDependencies deps)
    {
        _dependencies = deps;
    }

    boolean isFileProcessed(String url)
    {
        return _dependencies.isFileRepresented(url);
    }


    /**
     * Initializer for schematypepath
     */
    public void setImportingTypeLoader(SchemaTypeLoader loader)
    {
        _importingLoader = loader;
    }

    /**
     * Initializer for error handling.
     */
    public void setErrorListener(Collection errorListener)
        { _errorListener = errorListener; }

    /**
     * Passes an error on to the current error listener.
     * KHK: remove this
     */
    public void error(String message, int code, XmlObject loc)
        { addError(_errorListener, message, code, loc); }

    /**
     * Passes an error on to the current error listener.
     */
    public void error(String code, Object[] args, XmlObject loc)
        { addError(_errorListener, code, args, loc); }
    
    /**
     * Passes a recovered error on to the current error listener.
     */
    public void recover(String code, Object[] args, XmlObject loc)
        { addError(_errorListener, code, args, loc); _recoveredErrors++; }
    
    /**
     * Passes an error on to the current error listener.
     */
    public void warning(String message, int code, XmlObject loc)
    {
        addWarning(_errorListener, message, code, loc);
    }

    /**
     * Passes an error on to the current error listener.
     */
    public void warning(String code, Object[] args, XmlObject loc)
    {
        // it's OK for XMLSchema.xsd itself to have reserved type names
        if (code == XmlErrorCodes.RESERVED_TYPE_NAME &&
            loc.documentProperties().getSourceName() != null &&
            loc.documentProperties().getSourceName().indexOf("XMLSchema.xsd") > 0)
            return;

        addWarning(_errorListener, code, args, loc);
    }

    /**
     * Passes a warning on to the current error listener.
     */
    public void info(String message)
        { addInfo(_errorListener, message); }

    /**
     * Passes a warning on to the current error listener.
     */
    public void info(String code, Object[] args)
        { addInfo(_errorListener, code, args); }

    // KHK: remove this
    public static void addError(Collection errorListener, String message, int code, XmlObject location)
    {
        XmlError err =
            XmlError.forObject(
                message,
                XmlError.SEVERITY_ERROR,
                location);
        errorListener.add(err);
    }

    public static void addError(Collection errorListener, String code, Object[] args, XmlObject location)
    {
        XmlError err =
            XmlError.forObject(
              code,
              args,
              XmlError.SEVERITY_ERROR,
              location);
        errorListener.add(err);
    }
    
    public static void addError(Collection errorListener, String code, Object[] args, File location)
    {
        XmlError err =
            XmlError.forLocation(
                code,
                args,
                XmlError.SEVERITY_ERROR,
                location.toURI().toString(), 0, 0, 0);
        errorListener.add(err);
    }

    public static void addError(Collection errorListener, String code, Object[] args, URL location)
    {
        XmlError err =
            XmlError.forLocation(
              code,
              args,
              XmlError.SEVERITY_ERROR,
              location.toString(), 0, 0, 0);
        errorListener.add(err);
    }

    // KHK: remove this
    public static void addWarning(Collection errorListener, String message, int code, XmlObject location)
    {
        XmlError err =
            XmlError.forObject(
              message,
              XmlError.SEVERITY_WARNING,
              location);
        errorListener.add(err);
    }

    public static void addWarning(Collection errorListener, String code, Object[] args, XmlObject location)
    {
        XmlError err =
            XmlError.forObject(
                code,
                args,
                XmlError.SEVERITY_WARNING,
                location);
        errorListener.add(err);
    }

    public static void addInfo(Collection errorListener, String message)
    {
        XmlError err = XmlError.forMessage(message, XmlError.SEVERITY_INFO);
        errorListener.add(err);
    }

    public static void addInfo(Collection errorListener, String code, Object[] args)
    {
        XmlError err = XmlError.forMessage(code, args, XmlError.SEVERITY_INFO);
        errorListener.add(err);
    }

    public void setGivenTypeSystemName(String name)
        { _givenStsName = name; }

    /**
     * Initializer for references to the SchemaTypeLoader
     */
    public void setTargetSchemaTypeSystem(SchemaTypeSystemImpl target)
        { _target = target; }

    /**
     * Accumulates a schema digest...
     */
    public void addSchemaDigest(byte[] digest)
    {
        if (_noDigest)
            return;

        if (digest == null)
        {
            _noDigest = true;
            _digest = null;
            return;
        }

        if (_digest == null)
            _digest = new byte[128/8]; // 128 bits.
        int len = _digest.length;
        if (digest.length < len)
            len = digest.length;
        for (int i = 0; i < len; i++)
            _digest[i] ^= digest[i];
    }

    /**
     * The SchemaTypeSystem which we're building types on behalf of.
     */
    public SchemaTypeSystemImpl sts()
    {
        if (_target != null)
            return _target;

        String name = _givenStsName;
        if (name == null && _digest != null)
            name = "s" + new String(HexBin.encode(_digest));

        _target = new SchemaTypeSystemImpl(name);
        return _target;
    }

    /**
     * True if the given URI is a local file
     */
    public boolean shouldDownloadURI(String uriString)
    {
        if (_doingDownloads)
            return true;

        if (uriString == null)
            return false;

        try
        {
            URI uri = new URI(uriString);
            if (uri.getScheme().equalsIgnoreCase("jar") ||
                uri.getScheme().equalsIgnoreCase("zip"))
            {
                // It may be local or not, depending on the embedded URI
                String s = uri.getSchemeSpecificPart();
                int i = s.lastIndexOf('!');
                return shouldDownloadURI(i > 0 ? s.substring(0, i) : s);
            }
            return uri.getScheme().equalsIgnoreCase("file");
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Initializer for compatMap.
     */
    public void setOptions(XmlOptions options)
    {
        if (options == null)
        {
            return; // defaults are all false.
        }

        _allowPartial = options.hasOption("COMPILE_PARTIAL_TYPESYSTEM");
        
        _compatMap = (Map)options.get(XmlOptions.COMPILE_SUBSTITUTE_NAMES);
        _noUpa = options.hasOption(XmlOptions.COMPILE_NO_UPA_RULE) ? true :
                !"true".equals(SystemProperties.getProperty("xmlbean.uniqueparticleattribution", "true"));
        _noPvr = options.hasOption(XmlOptions.COMPILE_NO_PVR_RULE) ? true :
                !"true".equals(SystemProperties.getProperty("xmlbean.particlerestriction", "true"));
        _noAnn = options.hasOption(XmlOptions.COMPILE_NO_ANNOTATIONS) ? true :
            !"true".equals(SystemProperties.getProperty("xmlbean.schemaannotations", "true"));
        _doingDownloads = options.hasOption(XmlOptions.COMPILE_DOWNLOAD_URLS) ? true :
                "true".equals(SystemProperties.getProperty("xmlbean.downloadurls", "false"));
        _entityResolver = (EntityResolver)options.get(XmlOptions.ENTITY_RESOLVER);

        if (_entityResolver == null)
            _entityResolver = ResolverUtil.getGlobalEntityResolver();

        if (_entityResolver != null)
            _doingDownloads = true;
        
        if (options.hasOption(XmlOptions.COMPILE_MDEF_NAMESPACES))
        {
            _mdefNamespaces.addAll((Collection)options.get(XmlOptions.COMPILE_MDEF_NAMESPACES));
            
            String local = "##local";
            String any = "##any";
            
            if (_mdefNamespaces.contains(local))
            {
                _mdefNamespaces.remove(local);
                _mdefNamespaces.add("");
            }
            if (_mdefNamespaces.contains(any))
            {
                _mdefNamespaces.remove(any);
                _mdefAll = true;
            }
        }
    }

    /**
     * May return null if there is no custom entity resolver.
     */
    public EntityResolver getEntityResolver()
    {
        return _entityResolver;
    }

    /**
     * True if no unique particle attribution option is set
     */
    public boolean noUpa()
    {
        return _noUpa;
    }

    /**
     * True if no particle valid (restriciton) option is set
     */
    public boolean noPvr()
    {
        return _noPvr;
    }

    /**
     * True if annotations should be skipped
     */
    public boolean noAnn()
    {
        return _noAnn;
    }
    
    /**
     * True if a partial SchemaTypeSystem should be produced
     */
    // EXPERIMENTAL
    public boolean allowPartial()
    {
        return _allowPartial;
    }
    
    /**
     * Get count of recovered errors. Not for public.
     */ 
    // EXPERIMENTAL
    public int getRecovered()
    {
        return _recoveredErrors;
    }
    
    /**
     * Intercepts XML names and translates them
     * through the compat map, if any.
     *
     * Also looks for a default namespace for global definitions.
     */
    private QName compatName(QName name, String chameleonNamespace)
    {
        // first check for a chameleonNamespace namespace
        if (name.getNamespaceURI().length() == 0 && chameleonNamespace != null && chameleonNamespace.length() > 0)
            name = new QName(chameleonNamespace, name.getLocalPart());

        if (_compatMap == null)
            return name;

        QName subst = (QName)_compatMap.get(name);
        if (subst == null)
            return name;
        return subst;
    }

    /**
     * Initializer for the schema config object.
     */
    public void setBindingConfig(BindingConfig config)
        throws IllegalArgumentException
    {
        _config = config;
    }

    public BindingConfig getBindingConfig()
        throws IllegalArgumentException
    {
        return _config;
    }

    /**
     * Looks up package override for a namespace URI
     */
    public String getPackageOverride(String namespace)
    {
        if (_config == null)
            return null;
        return _config.lookupPackageForNamespace(namespace);
    }

    /**
     * Looks up package override for a namespace URI
     */
    public String getJavaPrefix(String namespace)
    {
        if (_config == null)
            return null;
        return _config.lookupPrefixForNamespace(namespace);
    }

    /**
     * Looks up package override for a namespace URI
     */
    public String getJavaSuffix(String namespace)
    {
        if (_config == null)
            return null;
        return _config.lookupSuffixForNamespace(namespace);
    }

    /**
     * Looks up configured java name for the given qname.
     */
    public String getJavaname(QName qname, int kind)
    {
        if (_config == null)
            return null;
        return _config.lookupJavanameForQName(qname, kind);
    }

    /* SPELLINGS ======================================================*/

    private static String crunchName(QName name)
    {
        // lowercase, and drop namespace.
        return name.getLocalPart().toLowerCase();
    }

    void addSpelling(QName name, SchemaComponent comp)
    {
        _misspelledNames.put(crunchName(name), comp);
    }

    SchemaComponent findSpelling(QName name)
    {
        return (SchemaComponent)_misspelledNames.get(crunchName(name));
    }

    /* NAMESPACES ======================================================*/

    void addNamespace(String targetNamespace)
    {
        _namespaces.add(targetNamespace);
    }

    String[] getNamespaces()
    {
        return (String[])_namespaces.toArray(new String[_namespaces.size()]);
    }

    boolean linkerDefinesNamespace(String namespace)
    {
        return _importingLoader.isNamespaceDefined(namespace);
    }

    /* TYPES ==========================================================*/

    SchemaTypeImpl findGlobalType(QName name, String chameleonNamespace, String sourceNamespace)
    {
        name = compatName(name, chameleonNamespace);
        SchemaTypeImpl result = (SchemaTypeImpl)_globalTypes.get(name);
        boolean foundOnLoader = false;
        if (result == null)
        {
            result = (SchemaTypeImpl)_importingLoader.findType(name);
            foundOnLoader = result != null;
        }
        if (!foundOnLoader && sourceNamespace != null)
            registerDependency(sourceNamespace, name.getNamespaceURI());
        return result;
    }

    SchemaTypeImpl findRedefinedGlobalType(QName name, String chameleonNamespace, SchemaTypeImpl redefinedBy)
    {
        QName redefinedName = redefinedBy.getName();
        name = compatName(name, chameleonNamespace);
        if (name.equals(redefinedName))
        {
            return (SchemaTypeImpl)_redefinedGlobalTypes.get(redefinedBy);
            // BUGBUG: should also link against _importingLoader.findRedefinedType
        }
        SchemaTypeImpl result = (SchemaTypeImpl)_globalTypes.get(name);
        if (result == null)
            result = (SchemaTypeImpl)_importingLoader.findType(name);
        // no dependency is needed here, necause it's intra-namespace
        return result;
    }

    void addGlobalType(SchemaTypeImpl type, SchemaTypeImpl redefined)
    {
        if (type != null)
        {
            QName name = type.getName();
            SchemaContainer container = getContainer(name.getNamespaceURI());
            assert container != null && container == type.getContainer();

            if (redefined != null)
            {
                if (_redefinedGlobalTypes.containsKey(redefined))
                {
                    if (!ignoreMdef(name)) {
                        if (_mdefAll) {
                            warning(XmlErrorCodes.SCHEMA_PROPERTIES$DUPLICATE,
                                    new Object[] { "global type", QNameHelper.pretty(name), ((SchemaType) _redefinedGlobalTypes.get(redefined)).getSourceName() } ,
                                    type.getParseObject());
                        } else {
                            error(XmlErrorCodes.SCHEMA_PROPERTIES$DUPLICATE,
                                new Object[] { "global type", QNameHelper.pretty(name), ((SchemaType) _redefinedGlobalTypes.get(redefined)).getSourceName() } ,
                                type.getParseObject());
                        }
                    }
                }
                else
                {
                    _redefinedGlobalTypes.put(redefined, type);
                    container.addRedefinedType(type.getRef());
                }
            }
            else
            {
                if (_globalTypes.containsKey(name))
                {
                    if (!ignoreMdef(name)) {
                        if (_mdefAll) {
                            warning(XmlErrorCodes.SCHEMA_PROPERTIES$DUPLICATE,
                                    new Object[] { "global type", QNameHelper.pretty(name), ((SchemaType) _globalTypes.get(name)).getSourceName() },
                                    type.getParseObject());
                        } else {
                            error(XmlErrorCodes.SCHEMA_PROPERTIES$DUPLICATE,
                                new Object[] { "global type", QNameHelper.pretty(name), ((SchemaType) _globalTypes.get(name)).getSourceName() },
                                type.getParseObject());
                        }
                    }
                }
                else
                {
                    _globalTypes.put(name, type);
                    container.addGlobalType(type.getRef());
                    addSpelling(name, type);
                }
            }
        }
    }

    private boolean ignoreMdef(QName name)
    {
        return _mdefNamespaces.contains(name.getNamespaceURI());
    }

    SchemaType[] globalTypes()
        { return (SchemaType[])_globalTypes.values().toArray(new SchemaType[_globalTypes.size()]); }

    SchemaType[] redefinedGlobalTypes()
        { return (SchemaType[])_redefinedGlobalTypes.values().toArray(new SchemaType[_redefinedGlobalTypes.size()]); }

    /* DOCUMENT TYPES =================================================*/

    SchemaTypeImpl findDocumentType(QName name, String chameleonNamespace, String sourceNamespace)
    {
        name = compatName(name, chameleonNamespace);
        SchemaTypeImpl result = (SchemaTypeImpl)_documentTypes.get(name);
        boolean foundOnLoader = false;
        if (result == null)
        {
            result = (SchemaTypeImpl)_importingLoader.findDocumentType(name);
            foundOnLoader = result != null;
        }
        if (!foundOnLoader && sourceNamespace != null)
            registerDependency(sourceNamespace, name.getNamespaceURI());
        return result;
    }

    void addDocumentType(SchemaTypeImpl type, QName name)
    {
        if (_documentTypes.containsKey(name))
        {
            if (!ignoreMdef(name)) {
                if (_mdefAll) {
                    warning(XmlErrorCodes.SCHEMA_PROPERTIES$DUPLICATE,
                            new Object[] { "global element", QNameHelper.pretty(name), ((SchemaComponent) _documentTypes.get(name)).getSourceName() },
                            type.getParseObject());
                } else {                
                    error(XmlErrorCodes.SCHEMA_PROPERTIES$DUPLICATE,
                        new Object[] { "global element", QNameHelper.pretty(name), ((SchemaComponent) _documentTypes.get(name)).getSourceName() },
                        type.getParseObject());
                }
            }
        }
        else
        {
            _documentTypes.put(name, type);
            SchemaContainer container = getContainer(name.getNamespaceURI());
            assert container != null && container == type.getContainer();
            container.addDocumentType(type.getRef());
        }
    }

    SchemaType[] documentTypes()
        { return (SchemaType[])_documentTypes.values().toArray(new SchemaType[_documentTypes.size()]); }

    /* ATTRIBUTE TYPES =================================================*/

    SchemaTypeImpl findAttributeType(QName name, String chameleonNamespace, String sourceNamespace)
    {
        name = compatName(name, chameleonNamespace);
        SchemaTypeImpl result = (SchemaTypeImpl)_attributeTypes.get(name);
        boolean foundOnLoader = false;
        if (result == null)
        {
            result = (SchemaTypeImpl)_importingLoader.findAttributeType(name);
            foundOnLoader = result != null;
        }
        if (!foundOnLoader && sourceNamespace != null)
            registerDependency(sourceNamespace, name.getNamespaceURI());
        return result;
    }

    void addAttributeType(SchemaTypeImpl type, QName name)
    {
        if (_attributeTypes.containsKey(name))
        {
            if (!ignoreMdef(name)) {
                if (_mdefAll) {
                    warning(XmlErrorCodes.SCHEMA_PROPERTIES$DUPLICATE,
                        new Object[] { "global attribute", QNameHelper.pretty(name), ((SchemaComponent) _attributeTypes.get(name)).getSourceName() },
                        type.getParseObject());
                } else {
                    error(XmlErrorCodes.SCHEMA_PROPERTIES$DUPLICATE,
                        new Object[] { "global attribute", QNameHelper.pretty(name), ((SchemaComponent) _attributeTypes.get(name)).getSourceName() },
                        type.getParseObject());
                }
            }
        }
        else
        {
            _attributeTypes.put(name, type);
            SchemaContainer container = getContainer(name.getNamespaceURI());
            assert container != null && container == type.getContainer();
            container.addAttributeType(type.getRef());
        }
    }

    SchemaType[] attributeTypes()
        { return (SchemaType[])_attributeTypes.values().toArray(new SchemaType[_attributeTypes.size()]); }

    /* ATTRIBUTES =====================================================*/

    SchemaGlobalAttributeImpl findGlobalAttribute(QName name, String chameleonNamespace, String sourceNamespace)
    {
        name = compatName(name, chameleonNamespace);
        SchemaGlobalAttributeImpl result = (SchemaGlobalAttributeImpl)_globalAttributes.get(name);
        boolean foundOnLoader = false;
        if (result == null)
        {
            result = (SchemaGlobalAttributeImpl)_importingLoader.findAttribute(name);
            foundOnLoader = result != null;
        }
        if (!foundOnLoader && sourceNamespace != null)
            registerDependency(sourceNamespace, name.getNamespaceURI());
        return result;
    }

    void addGlobalAttribute(SchemaGlobalAttributeImpl attribute)
    {
        if (attribute != null)
        {
            QName name = attribute.getName();
            _globalAttributes.put(name, attribute);
            addSpelling(name, attribute);
            SchemaContainer container = getContainer(name.getNamespaceURI());
            assert container != null && container == attribute.getContainer();
            container.addGlobalAttribute(attribute.getRef());
        }
    }

    SchemaGlobalAttribute[] globalAttributes()
        { return (SchemaGlobalAttribute[])_globalAttributes.values().toArray(new SchemaGlobalAttribute[_globalAttributes.size()]); }

    /* ELEMENTS =======================================================*/

    SchemaGlobalElementImpl findGlobalElement(QName name, String chameleonNamespace, String sourceNamespace)
    {
        name = compatName(name, chameleonNamespace);
        SchemaGlobalElementImpl result = (SchemaGlobalElementImpl)_globalElements.get(name);
        boolean foundOnLoader = false;
        if (result == null)
        {
            result = (SchemaGlobalElementImpl)_importingLoader.findElement(name);
            foundOnLoader = result != null;
        }
        if (!foundOnLoader && sourceNamespace != null)
            registerDependency(sourceNamespace, name.getNamespaceURI());
        return result;
    }

    void addGlobalElement(SchemaGlobalElementImpl element)
    {
        if (element != null)
        {
            QName name = element.getName();
            _globalElements.put(name, element);
            SchemaContainer container = getContainer(name.getNamespaceURI());
            assert container != null && container == element.getContainer();
            container.addGlobalElement(element.getRef());
            addSpelling(name, element);
        }
    }

    SchemaGlobalElement[] globalElements()
        { return (SchemaGlobalElement[])_globalElements.values().toArray(new SchemaGlobalElement[_globalElements.size()]); }

    /* ATTRIBUTE GROUPS ===============================================*/

    SchemaAttributeGroupImpl findAttributeGroup(QName name, String chameleonNamespace, String sourceNamespace)
    {
        name = compatName(name, chameleonNamespace);
        SchemaAttributeGroupImpl result = (SchemaAttributeGroupImpl)_attributeGroups.get(name);
        boolean foundOnLoader = false;
        if (result == null)
        {
            result = (SchemaAttributeGroupImpl)_importingLoader.findAttributeGroup(name);
            foundOnLoader = result != null;
        }
        if (!foundOnLoader && sourceNamespace != null)
            registerDependency(sourceNamespace, name.getNamespaceURI());
        return result;
    }

    SchemaAttributeGroupImpl findRedefinedAttributeGroup(QName name, String chameleonNamespace, SchemaAttributeGroupImpl redefinedBy)
    {
        QName redefinitionFor = redefinedBy.getName();
        name = compatName(name, chameleonNamespace);
        if (name.equals(redefinitionFor))
        {
            return (SchemaAttributeGroupImpl)_redefinedAttributeGroups.get(redefinedBy);
            // BUGBUG: should also link against _importingLoader.findRedefinedAttributeGroup
        }
        SchemaAttributeGroupImpl result = (SchemaAttributeGroupImpl)_attributeGroups.get(name);
        if (result == null)
            result = (SchemaAttributeGroupImpl)_importingLoader.findAttributeGroup(name);
        return result;
    }

    void addAttributeGroup(SchemaAttributeGroupImpl attributeGroup, SchemaAttributeGroupImpl redefined)
    {
        if (attributeGroup != null)
        {
            QName name = attributeGroup.getName();
            SchemaContainer container = getContainer(name.getNamespaceURI());
            assert container != null && container == attributeGroup.getContainer();
            if (redefined != null)
            {
                if (_redefinedAttributeGroups.containsKey(redefined))
                {
                    if (!ignoreMdef(name)) {
                        if (_mdefAll) {
                            warning(XmlErrorCodes.SCHEMA_PROPERTIES$DUPLICATE,
                                new Object[] { "attribute group", QNameHelper.pretty(name), ((SchemaComponent) _redefinedAttributeGroups.get(redefined)).getSourceName() },
                                attributeGroup.getParseObject());
                        } else {
                            error(XmlErrorCodes.SCHEMA_PROPERTIES$DUPLICATE,
                                new Object[] { "attribute group", QNameHelper.pretty(name), ((SchemaComponent) _redefinedAttributeGroups.get(redefined)).getSourceName() },
                                attributeGroup.getParseObject());
                        }
                    }
                }
                else
                {
                    _redefinedAttributeGroups.put(redefined, attributeGroup);
                    container.addRedefinedAttributeGroup(attributeGroup.getRef());
                }
            }
            else
            {
                if (_attributeGroups.containsKey( name ))
                {
                    if (!ignoreMdef(name)) {
                        if (_mdefAll) {
                            warning(XmlErrorCodes.SCHEMA_PROPERTIES$DUPLICATE,
                                new Object[] { "attribute group", QNameHelper.pretty(name), ((SchemaComponent) _attributeGroups.get(name)).getSourceName() },
                                attributeGroup.getParseObject());
                        } else {
                            error(XmlErrorCodes.SCHEMA_PROPERTIES$DUPLICATE,
                                new Object[] { "attribute group", QNameHelper.pretty(name), ((SchemaComponent) _attributeGroups.get(name)).getSourceName() },
                                attributeGroup.getParseObject());
                        }
                    }
                }
                else
                {
                    _attributeGroups.put(attributeGroup.getName(), attributeGroup);
                    addSpelling(attributeGroup.getName(), attributeGroup);
                    container.addAttributeGroup(attributeGroup.getRef());
                }
            }
        }
    }

    SchemaAttributeGroup[] attributeGroups()
        { return (SchemaAttributeGroup[])_attributeGroups.values().toArray(new SchemaAttributeGroup[_attributeGroups.size()]); }

    SchemaAttributeGroup[] redefinedAttributeGroups()
        { return (SchemaAttributeGroup[])_redefinedAttributeGroups.values().toArray(new SchemaAttributeGroup[_redefinedAttributeGroups.size()]); }

    /* MODEL GROUPS ===================================================*/

    SchemaModelGroupImpl findModelGroup(QName name, String chameleonNamespace, String sourceNamespace)
    {
        name = compatName(name, chameleonNamespace);
        SchemaModelGroupImpl result = (SchemaModelGroupImpl)_modelGroups.get(name);
        boolean foundOnLoader = false;
        if (result == null)
        {
            result = (SchemaModelGroupImpl)_importingLoader.findModelGroup(name);
            foundOnLoader = result != null;
        }
        if (!foundOnLoader && sourceNamespace != null)
            registerDependency(sourceNamespace, name.getNamespaceURI());
        return result;
    }

    SchemaModelGroupImpl findRedefinedModelGroup(QName name, String chameleonNamespace, SchemaModelGroupImpl redefinedBy)
    {
        QName redefinitionFor = redefinedBy.getName();
        name = compatName(name, chameleonNamespace);
        if (name.equals(redefinitionFor))
        {
            return (SchemaModelGroupImpl)_redefinedModelGroups.get(redefinedBy);
            // BUGBUG: should also link against _importingLoader.findRedefinedModelGroup
        }
        SchemaModelGroupImpl result = (SchemaModelGroupImpl)_modelGroups.get(name);
        if (result == null)
            result = (SchemaModelGroupImpl)_importingLoader.findModelGroup(name);
        return result;
    }

    void addModelGroup(SchemaModelGroupImpl modelGroup, SchemaModelGroupImpl redefined)
    {
        if (modelGroup != null)
        {
            QName name = modelGroup.getName();
            SchemaContainer container = getContainer(name.getNamespaceURI());
            assert container != null && container == modelGroup.getContainer();
            if (redefined != null)
            {
                if (_redefinedModelGroups.containsKey(redefined))
                {
                    if (!ignoreMdef(name)) {
                        if (_mdefAll) {
                            warning(XmlErrorCodes.SCHEMA_PROPERTIES$DUPLICATE,
                                    new Object[] { "model group", QNameHelper.pretty(name), ((SchemaComponent) _redefinedModelGroups.get(redefined)).getSourceName() },
                                    modelGroup.getParseObject());
                        } else {
                            error(XmlErrorCodes.SCHEMA_PROPERTIES$DUPLICATE,
                                new Object[] { "model group", QNameHelper.pretty(name), ((SchemaComponent) _redefinedModelGroups.get(redefined)).getSourceName() },
                                modelGroup.getParseObject());
                        }
                    }
                }
                else
                {
                    _redefinedModelGroups.put(redefined, modelGroup);
                    container.addRedefinedModelGroup(modelGroup.getRef());
                }
            }
            else
            {
                if (_modelGroups.containsKey(name))
                {
                    if (!ignoreMdef(name)) {
                        if (_mdefAll) {
                            warning(XmlErrorCodes.SCHEMA_PROPERTIES$DUPLICATE,
                                    new Object[] { "model group", QNameHelper.pretty(name), ((SchemaComponent) _modelGroups.get(name)).getSourceName() },
                                    modelGroup.getParseObject());
                        } else {
                            error(XmlErrorCodes.SCHEMA_PROPERTIES$DUPLICATE,
                                new Object[] { "model group", QNameHelper.pretty(name), ((SchemaComponent) _modelGroups.get(name)).getSourceName() },
                                modelGroup.getParseObject());
                        }
                    }
                }
                else
                {
                    _modelGroups.put(modelGroup.getName(), modelGroup);
                    addSpelling(modelGroup.getName(), modelGroup);
                    container.addModelGroup(modelGroup.getRef());
                }
            }
        }
    }

    SchemaModelGroup[] modelGroups()
        { return (SchemaModelGroup[])_modelGroups.values().toArray(new SchemaModelGroup[_modelGroups.size()]); }

    SchemaModelGroup[] redefinedModelGroups()
        { return (SchemaModelGroup[])_redefinedModelGroups.values().toArray(new SchemaModelGroup[_redefinedModelGroups.size()]); }

    /* IDENTITY CONSTRAINTS ===========================================*/

    SchemaIdentityConstraintImpl findIdConstraint(QName name, String chameleonNamespace, String sourceNamespace)
    {
        name = compatName(name, chameleonNamespace);
        if (sourceNamespace != null)
            registerDependency(sourceNamespace, name.getNamespaceURI());
        return (SchemaIdentityConstraintImpl)_idConstraints.get(name);
    }

    void addIdConstraint(SchemaIdentityConstraintImpl idc)
    {
        if (idc != null)
        {
            QName name = idc.getName();
            SchemaContainer container = getContainer(name.getNamespaceURI());
            assert container != null && container == idc.getContainer();
            if (_idConstraints.containsKey(name))
            {
                if (!ignoreMdef(name))
                    warning(XmlErrorCodes.SCHEMA_PROPERTIES$DUPLICATE,
                        new Object[] { "identity constraint", QNameHelper.pretty(name), ((SchemaComponent) _idConstraints.get(name)).getSourceName() },
                        idc.getParseObject());
            }
            else
            {
                _idConstraints.put(name, idc);
                addSpelling(idc.getName(), idc);
                container.addIdentityConstraint(idc.getRef());
            }
        }
    }

    SchemaIdentityConstraintImpl[] idConstraints()
        { return (SchemaIdentityConstraintImpl[])_idConstraints.values().toArray(new SchemaIdentityConstraintImpl[_idConstraints.size()]); }

    /* ANNOTATIONS ===========================================*/

    void addAnnotation(SchemaAnnotationImpl ann, String targetNamespace)
    {
        if (ann != null)
        {
            SchemaContainer container = getContainer(targetNamespace);
            assert container != null && container == ann.getContainer();
            _annotations.add(ann);
            container.addAnnotation(ann);
        }
    }

    List annotations()
    { return _annotations; }

    /* RECURSION AVOIDANCE ============================================*/
    boolean isProcessing(Object obj)
    {
        return _processingGroups.contains(obj);
    }

    void startProcessing(Object obj)
    {
        assert(!_processingGroups.contains(obj));
        _processingGroups.add(obj);
    }

    void finishProcessing(Object obj)
    {
        assert(_processingGroups.contains(obj));
        _processingGroups.remove(obj);
    }

    Object[] getCurrentProcessing()
    {
        return _processingGroups.toArray();
    }

    /* JAVAIZATION ====================================================*/

    Map typesByClassname()
        { return Collections.unmodifiableMap(_typesByClassname); }

    void addClassname(String classname, SchemaType type)
        { _typesByClassname.put(classname, type); }



    /**
     * Stack management if (heaven help us) we ever need to do
     * nested compilation of schema type system.
     */
    private static final class StscStack
    {
        StscState current;
        ArrayList stack = new ArrayList();
        final StscState push()
        {
            stack.add(current);
            current = new StscState();
            return current;
        }
        final void pop()
        {
            current = (StscState)stack.get(stack.size() - 1);
            stack.remove(stack.size() - 1);
        }
    }

    private static ThreadLocal tl_stscStack = new ThreadLocal();

    public static StscState start()
    {
        StscStack stscStack = (StscStack) tl_stscStack.get();

        if (stscStack==null)
        {
            stscStack = new StscStack();
            tl_stscStack.set(stscStack);
        }
        return stscStack.push();
    }

    public static StscState get()
    {
        return ((StscStack) tl_stscStack.get()).current;
    }

    public static void end()
    {
        StscStack stscStack = (StscStack) tl_stscStack.get();
        stscStack.pop();
        if (stscStack.stack.size()==0)
            tl_stscStack.set(null);            // this is required to release all the references in this classloader
                                               // which will enable class unloading and avoid OOM in PermGen
    }

    private final static XmlValueRef XMLSTR_PRESERVE = buildString("preserve");
    private final static XmlValueRef XMLSTR_REPLACE = buildString("preserve");
    private final static XmlValueRef XMLSTR_COLLAPSE = buildString("preserve");

    static final SchemaType[] EMPTY_ST_ARRAY = new SchemaType[0];
    static final SchemaType.Ref[] EMPTY_STREF_ARRAY = new SchemaType.Ref[0];

    private final static XmlValueRef[] FACETS_NONE = new XmlValueRef[]
        { null, null, null, null, null, null, null, null, null,
          null, null, null };

    private final static boolean[] FIXED_FACETS_NONE = new boolean[]
        { false, false, false, false, false, false, false, false, false,
          false, false, false };

    private final static XmlValueRef[] FACETS_WS_COLLAPSE = new XmlValueRef[]
        { null, null, null, null, null, null, null, null, null,
          build_wsstring(SchemaType.WS_COLLAPSE), null, null };

    private final static boolean[] FIXED_FACETS_WS = new boolean[]
        { false, false, false, false, false, false, false, false, false,
          true, false, false };

    final static XmlValueRef[] FACETS_UNION = FACETS_NONE;
    final static boolean[] FIXED_FACETS_UNION = FIXED_FACETS_NONE;
    final static XmlValueRef[] FACETS_LIST = FACETS_WS_COLLAPSE;
    final static boolean[] FIXED_FACETS_LIST = FIXED_FACETS_WS;

    static XmlValueRef build_wsstring(int wsr)
    {
        switch (wsr)
        {
            case SchemaType.WS_PRESERVE:
                return XMLSTR_PRESERVE;
            case SchemaType.WS_REPLACE:
                return XMLSTR_REPLACE;
            case SchemaType.WS_COLLAPSE:
                return XMLSTR_COLLAPSE;
        }
        return null;
    }

    static XmlValueRef buildString(String str)
    {
        if (str == null)
            return null;

        try
        {
            XmlStringImpl i = new XmlStringImpl();
            i.set(str);
            i.setImmutable();
            return new XmlValueRef(i);
        }
        catch (XmlValueOutOfRangeException e)
        {
            return null;
        }
    }

    public void notFoundError(QName itemName, int code, XmlObject loc, boolean recovered)
    {
        String expected;
        String expectedName = QNameHelper.pretty(itemName);
        String found = null;
        String foundName = null;
        String sourceName = null;
        
        if (recovered)
            _recoveredErrors++;

        switch (code)
        {
            case SchemaType.TYPE:
                expected = "type";
                break;
            case SchemaType.ELEMENT:
                expected = "element";
                break;
            case SchemaType.ATTRIBUTE:
                expected = "attribute";
                break;
            case SchemaType.MODEL_GROUP:
                expected = "model group";
                break;
            case SchemaType.ATTRIBUTE_GROUP:
                expected = "attribute group";
                break;
            case SchemaType.IDENTITY_CONSTRAINT:
                expected = "identity constraint";
                break;
            default:
                assert(false);
                expected = "definition";
                break;
        }

        SchemaComponent foundComponent = findSpelling(itemName);
        QName name;
        if (foundComponent != null)
        {
            name = foundComponent.getName();
            if (name != null)
            {
                switch (foundComponent.getComponentType())
                {
                    case SchemaComponent.TYPE:
                        found = "type";
                        sourceName = ((SchemaType)foundComponent).getSourceName();
                        break;
                    case SchemaComponent.ELEMENT:
                        found = "element";
                        sourceName = ((SchemaGlobalElement)foundComponent).getSourceName();
                        break;
                    case SchemaComponent.ATTRIBUTE:
                        found = "attribute";
                        sourceName = ((SchemaGlobalAttribute)foundComponent).getSourceName();
                        break;
                    case SchemaComponent.ATTRIBUTE_GROUP:
                        found = "attribute group";
                        break;
                    case SchemaComponent.MODEL_GROUP:
                        found = "model group";
                        break;
                }

                if (sourceName != null)
                {
                    sourceName = sourceName.substring(sourceName.lastIndexOf('/') + 1);
                }

                if (!name.equals(itemName))
                {
                    foundName = QNameHelper.pretty(name);
                }
            }
        }

        if (found == null)
        {
            // error with no help
            error(XmlErrorCodes.SCHEMA_QNAME_RESOLVE,
                new Object[] { expected, expectedName }, loc);
        }
        else {
            // error with help
            error(XmlErrorCodes.SCHEMA_QNAME_RESOLVE$HELP,
                new Object[] {
                    expected,
                    expectedName,
                    found,
                    (foundName == null ? new Integer(0) : new Integer(1)),
                    foundName,
                    (sourceName == null ? new Integer(0) : new Integer(1)),
                    sourceName
                },
                loc);
        }
    }


    /**
     * Produces the "sourceName" (to be used within the schema project
     * source file copies) from the URI of the original source.
     *
     * Returns null if none.
     */
    public String sourceNameForUri(String uri)
    {
        return (String)_sourceForUri.get(uri);
    }

    /**
     * Returns the whole sourceCopyMap, mapping URI's that have
     * been read to "sourceName" local names that have been used
     * to tag the types.
     */
    public Map sourceCopyMap()
    {
        return Collections.unmodifiableMap(_sourceForUri);
    }

    /**
     * The base URI to use for nice filenames when saving sources.
     */
    public void setBaseUri(URI uri)
    {
        _baseURI = uri;
    }

    private final static String PROJECT_URL_PREFIX = "project://local";

    public String relativize(String uri)
    {
        return relativize(uri, false);
    }

    public String computeSavedFilename(String uri)
    {
        return relativize(uri, true);
    }

    private String relativize(String uri, boolean forSavedFilename)
    {
        if (uri == null)
            return null;

        // deal with things that do not look like absolute uris
        if (uri.startsWith("/"))
        {
            uri = PROJECT_URL_PREFIX + uri.replace('\\', '/');
        }
        else
        {
            // looks like a URL?
            int colon = uri.indexOf(':');
            if (colon <= 1 || !uri.substring(0, colon).matches("^\\w+$"))
                uri = PROJECT_URL_PREFIX + "/" + uri.replace('\\', '/');
        }

        // now relativize against that...
        if (_baseURI != null)
        {
            try
            {
                URI relative = _baseURI.relativize(new URI(uri));
                if (!relative.isAbsolute())
                    return relative.toString();
                else
                    uri = relative.toString();
            }
            catch (URISyntaxException e)
            {
            }
        }

        if (!forSavedFilename)
            return uri;

        int lastslash = uri.lastIndexOf('/');
        String dir = QNameHelper.hexsafe(lastslash == -1 ? "" : uri.substring(0, lastslash));

        int question = uri.indexOf('?', lastslash + 1);
        if (question == -1)
            return dir + "/" + uri.substring(lastslash + 1);

        String query = QNameHelper.hexsafe(question == -1 ? "" : uri.substring(question));

        // if encoded query part is longer than 64 characters, just drop it
        if (query.startsWith(QNameHelper.URI_SHA1_PREFIX))
            return dir + "/" + uri.substring(lastslash + 1, question);
        else
            return dir + "/" + uri.substring(lastslash + 1, question) + query;
    }

    /**
     * Notes another URI that has been consumed during compilation
     * (this is the URI that is in the document .NAME property)
     */
    public void addSourceUri(String uri, String nameToUse)
    {
        if (uri == null)
            return;

        if (nameToUse == null)
            nameToUse = computeSavedFilename(uri);

        _sourceForUri.put(uri, nameToUse);
    }

    /**
     * Returns the error listener being filled in during this compilation
     */
    public Collection getErrorListener()
    {
        return _errorListener;
    }

    /**
     * Returns the schema type loader to use for processing s4s
     */
    public SchemaTypeLoader getS4SLoader()
    {
        return _s4sloader;
    }

    Map _sourceForUri = new HashMap();
    URI _baseURI = URI.create(PROJECT_URL_PREFIX + "/");
    SchemaTypeLoader _s4sloader = XmlBeans.typeLoaderForClassLoader(SchemaDocument.class.getClassLoader());

    public File getSchemasDir()
    {
        return _schemasDir;
    }

    public void setSchemasDir(File _schemasDir)
    {
        this._schemasDir = _schemasDir;
    }
}
