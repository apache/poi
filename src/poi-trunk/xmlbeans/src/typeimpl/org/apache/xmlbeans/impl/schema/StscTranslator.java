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

import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.QNameSetBuilder;
import org.apache.xmlbeans.SchemaAttributeModel;
import org.apache.xmlbeans.SchemaBookmark;
import org.apache.xmlbeans.SchemaGlobalAttribute;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaIdentityConstraint;
import org.apache.xmlbeans.SchemaLocalAttribute;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlNonNegativeInteger;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlPositiveInteger;
import org.apache.xmlbeans.impl.common.QNameHelper;
import org.apache.xmlbeans.impl.common.XMLChar;
import org.apache.xmlbeans.impl.common.XPath;
import org.apache.xmlbeans.impl.values.NamespaceContext;
import org.apache.xmlbeans.impl.values.XmlNonNegativeIntegerImpl;
import org.apache.xmlbeans.impl.values.XmlPositiveIntegerImpl;
import org.apache.xmlbeans.impl.values.XmlValueOutOfRangeException;
import org.apache.xmlbeans.impl.xb.xsdschema.Annotated;
import org.apache.xmlbeans.impl.xb.xsdschema.AnnotationDocument;
import org.apache.xmlbeans.impl.xb.xsdschema.Attribute;
import org.apache.xmlbeans.impl.xb.xsdschema.AttributeGroup;
import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.apache.xmlbeans.impl.xb.xsdschema.FieldDocument;
import org.apache.xmlbeans.impl.xb.xsdschema.FormChoice;
import org.apache.xmlbeans.impl.xb.xsdschema.Keybase;
import org.apache.xmlbeans.impl.xb.xsdschema.KeyrefDocument;
import org.apache.xmlbeans.impl.xb.xsdschema.LocalElement;
import org.apache.xmlbeans.impl.xb.xsdschema.LocalSimpleType;
import org.apache.xmlbeans.impl.xb.xsdschema.NamedAttributeGroup;
import org.apache.xmlbeans.impl.xb.xsdschema.NamedGroup;
import org.apache.xmlbeans.impl.xb.xsdschema.RedefineDocument.Redefine;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument.Schema;
import org.apache.xmlbeans.impl.xb.xsdschema.SimpleType;
import org.apache.xmlbeans.impl.xb.xsdschema.TopLevelAttribute;
import org.apache.xmlbeans.impl.xb.xsdschema.TopLevelComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.TopLevelElement;
import org.apache.xmlbeans.impl.xb.xsdschema.TopLevelSimpleType;
import org.apache.xmlbeans.soap.SOAPArrayType;

import javax.xml.namespace.QName;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StscTranslator
{
    private static final QName WSDL_ARRAYTYPE_NAME =
        QNameHelper.forLNS("arrayType", "http://schemas.xmlsoap.org/wsdl/");
    private static final String FORM_QUALIFIED = "qualified";

    public static void addAllDefinitions(StscImporter.SchemaToProcess[] schemasAndChameleons)
    {
        // Build all redefine objects
        List redefinitions = new ArrayList();
        for (int i = 0; i < schemasAndChameleons.length; i++)
        {
            List redefines = schemasAndChameleons[i].getRedefines();
            if (redefines != null)
            {
                List redefineObjects = schemasAndChameleons[i].getRedefineObjects();
                Iterator it = redefines.iterator();
                Iterator ito = redefineObjects.iterator();
                for (; it.hasNext(); )
                {
                    assert ito.hasNext() :
                    "The array of redefines and redefine objects have to have the same length";
                    redefinitions.add(new RedefinitionHolder(
                            (StscImporter.SchemaToProcess) it.next(),
                            (Redefine) ito.next()));
                }
            }
        }
        RedefinitionMaster globalRedefinitions = new RedefinitionMaster((RedefinitionHolder[])
            redefinitions.toArray(new RedefinitionHolder[redefinitions.size()]));

        StscState state = StscState.get();
        for (int j = 0; j < schemasAndChameleons.length; j++)
        {
            Schema schema = schemasAndChameleons[j].getSchema();
            String givenTargetNamespace = schemasAndChameleons[j].getChameleonNamespace();

        // quick check for a few unsupported features

        if (schema.sizeOfNotationArray() > 0)
        {
            state.warning("Schema <notation> is not yet supported for this release.", XmlErrorCodes.UNSUPPORTED_FEATURE, schema.getNotationArray(0));
        }

        // figure namespace (taking into account chameleons)
        String targetNamespace = schema.getTargetNamespace();
        boolean chameleon = false;
        if (givenTargetNamespace != null && targetNamespace == null)
        {
            targetNamespace = givenTargetNamespace;
            chameleon = true;
        }
        if (targetNamespace == null)
            targetNamespace = "";

        //SchemaContainer container = null;
        if (targetNamespace.length() > 0 || !isEmptySchema(schema))
        {
            state.registerContribution(targetNamespace, schema.documentProperties().getSourceName());
            state.addNewContainer(targetNamespace);
            //container = state.getContainer(targetNamespace);
        }

        List redefChain = new ArrayList();
        TopLevelComplexType[] complexTypes = schema.getComplexTypeArray();
        for (int i = 0; i < complexTypes.length; i++)
        {
            TopLevelComplexType type = complexTypes[i];
            TopLevelComplexType redef;
            // 1. Traverse the list of redefining Schemas putting all redefinitions
            // of this type in a List
            RedefinitionHolder[] rhArray = globalRedefinitions.getComplexTypeRedefinitions(
                type.getName(), schemasAndChameleons[j]);
            for (int k = 0; k < rhArray.length; k++)
            {
                // In error cases, some redefinitions were nulled out in the list
                // which is why we need to perform this check
                if (rhArray[k] != null)
                {
                    redef = rhArray[k].redefineComplexType(type.getName());
                    assert redef != null; // This was already checked
                    redefChain.add(type);
                    type = redef;
                }
            }

            SchemaTypeImpl t = translateGlobalComplexType(type, targetNamespace, chameleon, redefChain.size() > 0);
            state.addGlobalType(t, null);
            SchemaTypeImpl r;
            // 2. Traverse the List built in step 1 in reverse and add all the
            // types in it to the list of redefined types
            for (int k = redefChain.size() - 1; k >= 0; k--)
            {
                redef = (TopLevelComplexType) redefChain.remove(k);
                r = translateGlobalComplexType(redef, targetNamespace, chameleon, k > 0);
                state.addGlobalType(r, t);
                t = r;
            }
        }

        TopLevelSimpleType[] simpleTypes = schema.getSimpleTypeArray();
        for (int i = 0; i < simpleTypes.length; i++)
        {
            TopLevelSimpleType type = simpleTypes[i];
            TopLevelSimpleType redef;
            RedefinitionHolder[] rhArray = globalRedefinitions.getSimpleTypeRedefinitions(
                type.getName(), schemasAndChameleons[j]);
            for (int k = 0; k < rhArray.length; k++)
            {
                // In error cases, some redefinitions were nulled out in the list
                // which is why we need to perform this check
                if (rhArray[k] != null)
                {
                    redef = rhArray[k].redefineSimpleType(type.getName());
                    assert redef != null; // This was already checked
                    redefChain.add(type);
                    type = redef;
                }
            }

            SchemaTypeImpl t = translateGlobalSimpleType(type, targetNamespace, chameleon,redefChain.size() > 0);
            state.addGlobalType(t, null);
            SchemaTypeImpl r;
            for (int k = redefChain.size()-1; k >= 0; k--)
            {
                redef = (TopLevelSimpleType) redefChain.remove(k);
                r = translateGlobalSimpleType(redef, targetNamespace, chameleon, k > 0);
                state.addGlobalType(r, t);
                t = r;
            }
        }

        TopLevelElement[] elements = schema.getElementArray();
        for (int i = 0; i < elements.length; i++)
        {
            TopLevelElement element = elements[i];
            state.addDocumentType(translateDocumentType(element, targetNamespace, chameleon), QNameHelper.forLNS(element.getName(), targetNamespace));
        }

        TopLevelAttribute[] attributes = schema.getAttributeArray();
        for (int i = 0; i < attributes.length ; i++)
        {
            TopLevelAttribute attribute = attributes[i];
            state.addAttributeType(translateAttributeType(attribute, targetNamespace, chameleon), QNameHelper.forLNS(attribute.getName(), targetNamespace));
        }

        NamedGroup[] modelgroups = schema.getGroupArray();
        for (int i = 0; i < modelgroups.length; i++)
        {
            NamedGroup group = modelgroups[i];
            NamedGroup redef;
            RedefinitionHolder[] rhArray = globalRedefinitions.getModelGroupRedefinitions(
                group.getName(), schemasAndChameleons[j]);
            for (int k = 0; k < rhArray.length; k++)
            {
                // In error cases, some redefinitions were nulled out in the list
                // which is why we need to perform this check
                if (rhArray[k] != null)
                {
                    redef = rhArray[k].redefineModelGroup(group.getName());
                    assert redef != null; // This was already checked
                    redefChain.add(group);
                    group = redef;
                }
            }

            SchemaModelGroupImpl g = translateModelGroup(group, targetNamespace, chameleon, redefChain.size() > 0);
            state.addModelGroup(g, null);
            SchemaModelGroupImpl r;
            for (int k = redefChain.size()-1; k >= 0; k--)
            {
                redef = (NamedGroup) redefChain.remove(k);
                r = translateModelGroup(redef, targetNamespace, chameleon, k > 0);
                state.addModelGroup(r, g);
                g = r;
            }
        }

        NamedAttributeGroup[] attrgroups = schema.getAttributeGroupArray();
        for (int i = 0; i < attrgroups.length; i++)
        {
            NamedAttributeGroup group = attrgroups[i];
            NamedAttributeGroup redef;
            RedefinitionHolder[] rhArray = globalRedefinitions.getAttributeGroupRedefinitions(
                group.getName(), schemasAndChameleons[j]);
            for (int k = 0; k < rhArray.length; k++)
            {
                // In error cases, some redefinitions were nulled out in the list
                // which is why we need to perform this check
                if (rhArray[k] != null)
                {
                    redef = rhArray[k].redefineAttributeGroup(group.getName());
                    assert redef != null; // This was already checked
                    redefChain.add(group);
                    group = redef;
                }
            }

            SchemaAttributeGroupImpl g = translateAttributeGroup(group, targetNamespace, chameleon, redefChain.size() > 0);
            state.addAttributeGroup(g, null);
            SchemaAttributeGroupImpl r;
            for (int k = redefChain.size()-1; k >= 0; k--)
            {
                redef = (NamedAttributeGroup) redefChain.remove(k);
                r = translateAttributeGroup(redef, targetNamespace, chameleon, k > 0);
                state.addAttributeGroup(r, g);
                g = r;
            }
        }

        AnnotationDocument.Annotation[] annotations = schema.getAnnotationArray();
        for (int i = 0; i < annotations.length; i++)
            state.addAnnotation(SchemaAnnotationImpl.getAnnotation(state.getContainer(targetNamespace), schema, annotations[i]), targetNamespace);
        }

        for (int i = 0; i < redefinitions.size(); i++)
            ((RedefinitionHolder) redefinitions.get(i)).complainAboutMissingDefinitions();
    }

    private static class RedefinitionHolder
    {
        // record redefinitions
        private Map stRedefinitions = Collections.EMPTY_MAP;
        private Map ctRedefinitions = Collections.EMPTY_MAP;
        private Map agRedefinitions = Collections.EMPTY_MAP;
        private Map mgRedefinitions = Collections.EMPTY_MAP;
        private String schemaLocation = "";
        private StscImporter.SchemaToProcess schemaRedefined;

        // first build set of redefined components
        RedefinitionHolder(StscImporter.SchemaToProcess schemaToProcess, Redefine redefine)
        {
            schemaRedefined = schemaToProcess;
            if (redefine != null)
            {
                StscState state = StscState.get();

                stRedefinitions = new HashMap();
                ctRedefinitions = new HashMap();
                agRedefinitions = new HashMap();
                mgRedefinitions = new HashMap();
                if (redefine.getSchemaLocation() != null)
                    schemaLocation = redefine.getSchemaLocation();

                TopLevelComplexType[] complexTypes = redefine.getComplexTypeArray();
                for (int i = 0; i < complexTypes.length; i++)
                {
                    if (complexTypes[i].getName() != null)
                    {
                        // KHK: which rule? sch-props-correct.2?
                        if (ctRedefinitions.containsKey(complexTypes[i].getName()))
                            state.error("Duplicate type redefinition: " + complexTypes[i].getName(), XmlErrorCodes.DUPLICATE_GLOBAL_TYPE, null);
                        else
                            ctRedefinitions.put(complexTypes[i].getName(), complexTypes[i]);
                    }
                }

                TopLevelSimpleType[] simpleTypes = redefine.getSimpleTypeArray();
                for (int i = 0; i < simpleTypes.length; i++)
                {
                    if (simpleTypes[i].getName() != null)
                    {
                        if (stRedefinitions.containsKey(simpleTypes[i].getName()))
                            state.error("Duplicate type redefinition: " + simpleTypes[i].getName(), XmlErrorCodes.DUPLICATE_GLOBAL_TYPE, null);
                        else
                            stRedefinitions.put(simpleTypes[i].getName(), simpleTypes[i]);
                    }
                }

                NamedGroup[] modelgroups = redefine.getGroupArray();
                for (int i = 0; i < modelgroups.length; i++)
                {
                    if (modelgroups[i].getName() != null)
                    {
                        if (mgRedefinitions.containsKey(modelgroups[i].getName()))
                            state.error("Duplicate type redefinition: " + modelgroups[i].getName(), XmlErrorCodes.DUPLICATE_GLOBAL_TYPE, null);
                        else
                            mgRedefinitions.put(modelgroups[i].getName(), modelgroups[i]);
                    }
                }

                NamedAttributeGroup[] attrgroups = redefine.getAttributeGroupArray();
                for (int i = 0; i < attrgroups.length; i++)
                {
                    if (attrgroups[i].getName() != null)
                    {
                        if (agRedefinitions.containsKey(attrgroups[i].getName()))
                            state.error("Duplicate type redefinition: " + attrgroups[i].getName(), XmlErrorCodes.DUPLICATE_GLOBAL_TYPE, null);
                        else
                            agRedefinitions.put(attrgroups[i].getName(), attrgroups[i]);
                    }
                }
            }
        }

        public TopLevelSimpleType redefineSimpleType(String name)
        {
            if (name == null || !stRedefinitions.containsKey(name))
                return null;
            return (TopLevelSimpleType)stRedefinitions.remove(name);
        }

        public TopLevelComplexType redefineComplexType(String name)
        {
            if (name == null || !ctRedefinitions.containsKey(name))
                return null;
            return (TopLevelComplexType)ctRedefinitions.remove(name);
        }

        public NamedGroup redefineModelGroup(String name)
        {
            if (name == null || !mgRedefinitions.containsKey(name))
                return null;
            return (NamedGroup)mgRedefinitions.remove(name);
        }

        public NamedAttributeGroup redefineAttributeGroup(String name)
        {
            if (name == null || !agRedefinitions.containsKey(name))
                return null;
            return (NamedAttributeGroup)agRedefinitions.remove(name);
        }

        public void complainAboutMissingDefinitions()
        {
            if (stRedefinitions.isEmpty() && ctRedefinitions.isEmpty() &&
                    agRedefinitions.isEmpty() && mgRedefinitions.isEmpty())
                return;

            StscState state = StscState.get();

            for (Iterator i = stRedefinitions.keySet().iterator(); i.hasNext(); )
            {
                String name = (String)i.next();
                state.error("Redefined simple type " + name + " not found in " + schemaLocation, XmlErrorCodes.GENERIC_ERROR, (XmlObject)stRedefinitions.get(name));
            }

            for (Iterator i = ctRedefinitions.keySet().iterator(); i.hasNext(); )
            {
                String name = (String)i.next();
                state.error("Redefined complex type " + name + " not found in " + schemaLocation, XmlErrorCodes.GENERIC_ERROR, (XmlObject)ctRedefinitions.get(name));
            }

            for (Iterator i = agRedefinitions.keySet().iterator(); i.hasNext(); )
            {
                String name = (String)i.next();
                state.error("Redefined attribute group " + name + " not found in " + schemaLocation, XmlErrorCodes.GENERIC_ERROR, (XmlObject)agRedefinitions.get(name));
            }

            for (Iterator i = mgRedefinitions.keySet().iterator(); i.hasNext(); )
            {
                String name = (String)i.next();
                state.error("Redefined model group " + name + " not found in " + schemaLocation, XmlErrorCodes.GENERIC_ERROR, (XmlObject)mgRedefinitions.get(name));
            }
        }
    }

    /**
     * This is used to aggregate all redefinitions for a specific component name.
     * The idea is to record the list of &lt;redefine%gt; sections that could
     * potentially redefine this component. When the list of actual redefinitions
     * is requested, the potential redefinitions are first filtered based on
     * accessibilty of the schema currently being processed from the redefining Schemas
     * and then topologically sorted based on the inclusion relationship to
     * ensure that redefinitions are applied in the right order.
     */
    private static class RedefinitionMaster
    {
        // record redefinitions
        private Map stRedefinitions = Collections.EMPTY_MAP;
        private Map ctRedefinitions = Collections.EMPTY_MAP;
        private Map agRedefinitions = Collections.EMPTY_MAP;
        private Map mgRedefinitions = Collections.EMPTY_MAP;
        private static final RedefinitionHolder[] EMPTY_REDEFINTION_HOLDER_ARRAY =
            new RedefinitionHolder[0];

        RedefinitionMaster(RedefinitionHolder[] redefHolders)
        {
            if (redefHolders.length > 0)
            {
                stRedefinitions = new HashMap();
                ctRedefinitions = new HashMap();
                agRedefinitions = new HashMap();
                mgRedefinitions = new HashMap();

                for (int i = 0; i < redefHolders.length; i++)
                {
                    RedefinitionHolder redefHolder = redefHolders[i];
                    for (Iterator it = redefHolder.stRedefinitions.keySet().iterator(); it.hasNext();)
                    {
                        Object key = it.next();
                        List redefinedIn = (List) stRedefinitions.get(key);
                        if (redefinedIn == null)
                        {
                            redefinedIn = new ArrayList();
                            stRedefinitions.put(key, redefinedIn);
                        }
                        redefinedIn.add(redefHolders[i]);
                    }
                    for (Iterator it = redefHolder.ctRedefinitions.keySet().iterator(); it.hasNext();)
                    {
                        Object key = it.next();
                        List redefinedIn = (List) ctRedefinitions.get(key);
                        if (redefinedIn == null)
                        {
                            redefinedIn = new ArrayList();
                            ctRedefinitions.put(key, redefinedIn);
                        }
                        redefinedIn.add(redefHolders[i]);
                    }
                    for (Iterator it = redefHolder.agRedefinitions.keySet().iterator(); it.hasNext();)
                    {
                        Object key = it.next();
                        List redefinedIn = (List) agRedefinitions.get(key);
                        if (redefinedIn == null)
                        {
                            redefinedIn = new ArrayList();
                            agRedefinitions.put(key, redefinedIn);
                        }
                        redefinedIn.add(redefHolders[i]);
                    }
                    for (Iterator it = redefHolder.mgRedefinitions.keySet().iterator(); it.hasNext();)
                    {
                        Object key = it.next();
                        List redefinedIn = (List) mgRedefinitions.get(key);
                        if (redefinedIn == null)
                        {
                            redefinedIn = new ArrayList();
                            mgRedefinitions.put(key, redefinedIn);
                        }
                        redefinedIn.add(redefHolders[i]);
                    }
                }
            }
        }

        RedefinitionHolder[] getSimpleTypeRedefinitions(String name,
            StscImporter.SchemaToProcess schema)
        {
            List redefines = (List) stRedefinitions.get(name);
            if (redefines == null)
                return EMPTY_REDEFINTION_HOLDER_ARRAY;
            return doTopologicalSort(redefines, schema, name, SIMPLE_TYPE);
        }

        RedefinitionHolder[] getComplexTypeRedefinitions(String name,
            StscImporter.SchemaToProcess schema)
        {
            List redefines = (List) ctRedefinitions.get(name);
            if (redefines == null)
                return EMPTY_REDEFINTION_HOLDER_ARRAY;
            return doTopologicalSort(redefines, schema, name, COMPLEX_TYPE);
        }

        RedefinitionHolder[] getAttributeGroupRedefinitions(String name,
            StscImporter.SchemaToProcess schema)
        {
            List redefines = (List) agRedefinitions.get(name);
            if (redefines == null)
                return EMPTY_REDEFINTION_HOLDER_ARRAY;
            return doTopologicalSort(redefines, schema, name, ATTRIBUTE_GROUP);
        }

        RedefinitionHolder[] getModelGroupRedefinitions(String name,
            StscImporter.SchemaToProcess schema)
        {
            List redefines = (List) mgRedefinitions.get(name);
            if (redefines == null)
                return EMPTY_REDEFINTION_HOLDER_ARRAY;
            return doTopologicalSort(redefines, schema, name, MODEL_GROUP);
        }

        private final static short SIMPLE_TYPE = 1;
        private final static short COMPLEX_TYPE = 2;
        private final static short MODEL_GROUP = 3;
        private final static short ATTRIBUTE_GROUP = 4;

        private RedefinitionHolder[] doTopologicalSort(List genericRedefines,
            StscImporter.SchemaToProcess schema, String name, short componentType)
        {
            // We have a list of files that redefine this name
            // Filter out the ones that don't redefine this file in particular
            RedefinitionHolder[] specificRedefines = new RedefinitionHolder[genericRedefines.size()];
            int n = 0;
            for (int i = 0; i < genericRedefines.size(); i++)
            {
                RedefinitionHolder h = (RedefinitionHolder) genericRedefines.get(i);
                if (h.schemaRedefined == schema ||
                    h.schemaRedefined.indirectIncludes(schema))
                    specificRedefines[n++] = h;
            }
            // Now we have the list of files that specifically redefine the
            // name in the file that we are looking for
            // Sort this list into topological order to get the right order
            // and figure out if there are multiple redefinitions involved
            RedefinitionHolder[] sortedRedefines = new RedefinitionHolder[n];
            int[] numberOfIncludes = new int[n];
            // Just count the number of inclusions for each redefinition
            for (int i = 0; i < n-1; i++)
            {
                RedefinitionHolder current = specificRedefines[i];
                for (int j = i + 1; j < n; j++)
                {
                    if (current.schemaRedefined.indirectIncludes(specificRedefines[j].schemaRedefined))
                        numberOfIncludes[i]++;
                    if (specificRedefines[j].schemaRedefined.indirectIncludes(current.schemaRedefined))
                        numberOfIncludes[j]++;
                }
            }
            // Eliminate members one by one, according to the number of schemas
            // that they include, to complete the sort
            int position = 0;
            boolean errorReported = false;
            while (position < n)
            {
                int index = -1;
                for (int i = 0; i < numberOfIncludes.length; i++)
                    if (numberOfIncludes[i] == 0)
                    {
                        if (index < 0)
                            index = i;
                    }
                if (index < 0)
                {
                    // Error! Circular redefinition
                    if (!errorReported)
                    {
                        StringBuffer fileNameList = new StringBuffer();
                        XmlObject location = null;
                        for (int i = 0; i < n; i++)
                            if (specificRedefines[i] != null)
                            {
                                fileNameList.append(specificRedefines[i].schemaLocation).
                                    append(',').append(' ');
                                if (location == null)
                                    location = locationFromRedefinitionAndCode(
                                        specificRedefines[i], name, componentType);
                            }
                        StscState.get().error("Detected circular redefinition of " +
                            componentNameFromCode(componentType) + " \"" + name +
                            "\"; Files involved: " + fileNameList.toString(),
                            XmlErrorCodes.GENERIC_ERROR, location);
                        errorReported = true;
                    }
                    int min = n;
                    for (int i = 0; i < n; i++)
                        if (numberOfIncludes[i] > 0 && numberOfIncludes[i] < min)
                        {
                            min = numberOfIncludes[i];
                            index = i;
                        }
                    numberOfIncludes[index]--;
                }
                else
                {
                    assert specificRedefines[index] != null;
                    sortedRedefines[position++] = specificRedefines[index];
                    for (int i = 0; i < n; i++)
                        if (specificRedefines[i] != null &&
                            specificRedefines[i].schemaRedefined.
                                indirectIncludes(specificRedefines[index].
                                    schemaRedefined))
                            numberOfIncludes[i]--;
                    specificRedefines[index] = null;
                    numberOfIncludes[index]--;
                }
            }
            // Nice. We now have all the redefinitions of this name in the list
            // Each one has to transitively redefine the one before, otherwise
            // it means we are attepting two different redefinitions for the same
            // component
            for (int i = 1; i < n; i++)
            {
                // Find the previous index with non-null Schema
                // Since i is never 0, such index always exists
                int j;
                for (j = i-1; j >= 0; j--)
                    if (sortedRedefines[j] != null)
                        break;

                if (!sortedRedefines[i].schemaRedefined.indirectIncludes(
                        sortedRedefines[j].schemaRedefined))
                {
                    StscState.get().error("Detected multiple redefinitions of " +
                        componentNameFromCode(componentType) +
                        " \"" + name + "\"; Files involved: " +
                        sortedRedefines[j].schemaRedefined.getSourceName() + ", " +
                        sortedRedefines[i].schemaRedefined.getSourceName(),
                        XmlErrorCodes.DUPLICATE_GLOBAL_TYPE,
                        locationFromRedefinitionAndCode(sortedRedefines[i], name, componentType));
                    // Ignore this redefinition
                    switch (componentType)
                    {
                    case SIMPLE_TYPE:
                        sortedRedefines[i].redefineSimpleType(name); break;
                    case COMPLEX_TYPE:
                        sortedRedefines[i].redefineComplexType(name); break;
                    case ATTRIBUTE_GROUP:
                        sortedRedefines[i].redefineAttributeGroup(name); break;
                    case MODEL_GROUP:
                        sortedRedefines[i].redefineModelGroup(name); break;
                    }
                    sortedRedefines[i] = null;
                }
            }

            return sortedRedefines;
        }

        private String componentNameFromCode(short code)
        {
            String componentName;
            switch (code)
            {
            case SIMPLE_TYPE: componentName = "simple type"; break;
            case COMPLEX_TYPE: componentName = "complex type"; break;
            case MODEL_GROUP: componentName = "model group"; break;
            case ATTRIBUTE_GROUP: componentName = "attribute group"; break;
            default: componentName = "";
            }
            return componentName;
        }

        private XmlObject locationFromRedefinitionAndCode(RedefinitionHolder redefinition,
            String name, short code)
        {
            XmlObject location;
            switch (code)
            {
            case SIMPLE_TYPE:
                location = (XmlObject) redefinition.stRedefinitions.get(name);
                break;
            case COMPLEX_TYPE:
                location = (XmlObject) redefinition.ctRedefinitions.get(name);
                break;
            case MODEL_GROUP:
                location = (XmlObject) redefinition.mgRedefinitions.get(name);
                break;
            case ATTRIBUTE_GROUP:
                location = (XmlObject) redefinition.agRedefinitions.get(name);
                break;
            default:
                location = null;
            }
            return location;
        }
    }

    private static String findFilename(XmlObject xobj)
    {
        return StscState.get().sourceNameForUri(xobj.documentProperties().getSourceName());
    }

    private static SchemaTypeImpl translateDocumentType ( TopLevelElement xsdType, String targetNamespace, boolean chameleon )
    {
        SchemaTypeImpl sType = new SchemaTypeImpl( StscState.get().getContainer(targetNamespace) );

        sType.setDocumentType(true);
        sType.setParseContext( xsdType, targetNamespace, chameleon, null, null, false);
        sType.setFilename( findFilename( xsdType ) );

        return sType;
    }

    private static SchemaTypeImpl translateAttributeType ( TopLevelAttribute xsdType, String targetNamespace, boolean chameleon )
    {
        SchemaTypeImpl sType = new SchemaTypeImpl( StscState.get().getContainer(targetNamespace) );

        sType.setAttributeType(true);
        sType.setParseContext( xsdType, targetNamespace, chameleon, null, null, false);
        sType.setFilename( findFilename( xsdType ) );

        return sType;
    }

    private static SchemaTypeImpl translateGlobalComplexType(TopLevelComplexType xsdType, String targetNamespace, boolean chameleon, boolean redefinition)
    {
        StscState state = StscState.get();

        String localname = xsdType.getName();
        if (localname == null)
        {
            state.error(XmlErrorCodes.MISSING_NAME, new Object[] { "global type" }, xsdType);
            // recovery: ignore unnamed types.
            return null;
        }
        if (!XMLChar.isValidNCName(localname))
        {
            state.error(XmlErrorCodes.INVALID_VALUE, new Object[] { localname, "name" }, xsdType.xgetName());
            // recovery: let the name go through anyway.
        }

        QName name = QNameHelper.forLNS(localname, targetNamespace);

        if (isReservedTypeName(name))
        {
            state.warning(XmlErrorCodes.RESERVED_TYPE_NAME, new Object[] { QNameHelper.pretty(name) }, xsdType);
            return null;
        }
        // System.err.println("Recording type " + QNameHelper.pretty(name));

        SchemaTypeImpl sType = new SchemaTypeImpl(state.getContainer(targetNamespace));
        sType.setParseContext(xsdType, targetNamespace, chameleon, null, null, redefinition);
        sType.setFilename(findFilename(xsdType));
        sType.setName(QNameHelper.forLNS(localname, targetNamespace));
        sType.setAnnotation(SchemaAnnotationImpl.getAnnotation(state.getContainer(targetNamespace), xsdType));
        sType.setUserData(getUserData(xsdType));
        return sType;
    }

    private static SchemaTypeImpl translateGlobalSimpleType(TopLevelSimpleType xsdType, String targetNamespace, boolean chameleon, boolean redefinition)
    {
        StscState state = StscState.get();

        String localname = xsdType.getName();
        if (localname == null)
        {
            state.error(XmlErrorCodes.MISSING_NAME, new Object[] { "global type" }, xsdType);
            // recovery: ignore unnamed types.
            return null;
        }
        if (!XMLChar.isValidNCName(localname))
        {
            state.error(XmlErrorCodes.INVALID_VALUE, new Object[] { localname, "name" }, xsdType.xgetName());
            // recovery: let the name go through anyway.
        }

        QName name = QNameHelper.forLNS(localname, targetNamespace);

        if (isReservedTypeName(name))
        {
            state.warning(XmlErrorCodes.RESERVED_TYPE_NAME, new Object[] { QNameHelper.pretty(name) }, xsdType);
            return null;
        }
        // System.err.println("Recording type " + QNameHelper.pretty(name));

        SchemaTypeImpl sType = new SchemaTypeImpl(state.getContainer(targetNamespace));
        sType.setSimpleType(true);
        sType.setParseContext(xsdType, targetNamespace, chameleon, null, null, redefinition);
        sType.setFilename(findFilename(xsdType));
        sType.setName(name);
        sType.setAnnotation(SchemaAnnotationImpl.getAnnotation(state.getContainer(targetNamespace), xsdType));
        sType.setUserData(getUserData(xsdType));
        return sType;
    }

    /*package*/ static SchemaTypeImpl translateAnonymousSimpleType(SimpleType typedef,
        String targetNamespace, boolean chameleon, String elemFormDefault,
        String attFormDefault, List anonymousTypes, SchemaType outerType)
    {
        StscState state = StscState.get();
        SchemaTypeImpl sType = new SchemaTypeImpl(state.getContainer(targetNamespace));
        sType.setSimpleType(true);
        sType.setParseContext(typedef, targetNamespace, chameleon,
            elemFormDefault, attFormDefault, false);
        sType.setOuterSchemaTypeRef(outerType.getRef());
        sType.setAnnotation(SchemaAnnotationImpl.getAnnotation(state.getContainer(targetNamespace), typedef));
        sType.setUserData(getUserData(typedef));
        anonymousTypes.add(sType);
        return sType;
    }

    static FormChoice findElementFormDefault(XmlObject obj)
    {
        XmlCursor cur = obj.newCursor();
        while (cur.getObject().schemaType() != Schema.type)
            if (!cur.toParent())
                return null;
        return ((Schema)cur.getObject()).xgetElementFormDefault();
    }

    public static boolean uriMatch(String s1, String s2)
    {
        if (s1 == null)
            return s2 == null || s2.equals("");
        if (s2 == null)
            return s1.equals("");
        return s1.equals(s2);
    }

    public static void copyGlobalElementToLocalElement(SchemaGlobalElement referenced, SchemaLocalElementImpl target )
    {

        target.setNameAndTypeRef(referenced.getName(), referenced.getType().getRef());
        target.setNillable(referenced.isNillable());
        target.setDefault(referenced.getDefaultText(), referenced.isFixed(), ((SchemaGlobalElementImpl)referenced).getParseObject());
        target.setIdentityConstraints(((SchemaLocalElementImpl)referenced).getIdentityConstraintRefs());
        target.setBlock(referenced.blockExtension(),  referenced.blockRestriction(),  referenced.blockSubstitution());
        target.setAbstract(referenced.isAbstract());
        target.setTransitionRules(((SchemaParticle)referenced).acceptedStartNames(),
            ((SchemaParticle)referenced).isSkippable());
        target.setAnnotation(referenced.getAnnotation());
    }

    public static void copyGlobalAttributeToLocalAttribute(SchemaGlobalAttributeImpl referenced, SchemaLocalAttributeImpl target )
    {
        target.init(
            referenced.getName(), referenced.getTypeRef(), referenced.getUse(),
            referenced.getDefaultText(),
                referenced.getParseObject(), referenced._defaultValue,
            referenced.isFixed(),
            referenced.getWSDLArrayType(),
            referenced.getAnnotation(), null);
    }

    /**
     * Translates a local or global schema element.
     */
    // check rule 3.3.3
    // http://www.w3c.org/TR/#section-Constraints-on-XML-Representations-of-Element-Declarations
    public static SchemaLocalElementImpl translateElement(
        Element xsdElt, String targetNamespace, boolean chameleon,
        String elemFormDefault, String attFormDefault,
        List anonymousTypes, SchemaType outerType)
    {
        StscState state = StscState.get();

        SchemaTypeImpl sgHead = null;

        // translate sg head
        if (xsdElt.isSetSubstitutionGroup())
        {
            sgHead = state.findDocumentType(xsdElt.getSubstitutionGroup(),
                ((SchemaTypeImpl)outerType).getChameleonNamespace(), targetNamespace);

            if (sgHead != null)
                StscResolver.resolveType(sgHead);
        }

        String name = xsdElt.getName();
        QName ref = xsdElt.getRef();


        if (ref != null && name != null)
        {
            // if (name.equals(ref.getLocalPart()) && uriMatch(targetNamespace, ref.getNamespaceURI()))
            //     state.warning("Element " + name + " specifies both a ref and a name", XmlErrorCodes.ELEMENT_EXTRA_REF, xsdElt.xgetRef());
            // else
                state.error(XmlErrorCodes.SCHEMA_ELEM$REF_OR_NAME_HAS_BOTH, new Object[] { name }, xsdElt.xgetRef());
            // ignore name
            name = null;
        }
        if (ref == null && name == null)
        {
            state.error(XmlErrorCodes.SCHEMA_ELEM$REF_OR_NAME_HAS_NEITHER, null, xsdElt);
            // recovery: ignore this element
            return null;
        }
        if (name != null && !XMLChar.isValidNCName(name))
        {
            state.error(XmlErrorCodes.INVALID_VALUE, new Object[] { name, "name" }, xsdElt.xgetName());
            // recovery: let the name go through anyway.
        }

        if (ref != null)
        {
            if (xsdElt.getType() != null)
            {
                state.error(XmlErrorCodes.SCHEMA_ELEM$REF_FEATURES, new Object[] { "type" }, xsdElt.xgetType());
                // recovery: let the name go through anyway.
            }

            if (xsdElt.getSimpleType() != null)
            {
                state.error(XmlErrorCodes.SCHEMA_ELEM$REF_FEATURES, new Object[] { "<simpleType>" }, xsdElt.getSimpleType());
                // recovery: let the name go through anyway.
            }

            if (xsdElt.getComplexType() != null)
            {
                state.error(XmlErrorCodes.SCHEMA_ELEM$REF_FEATURES, new Object[] { "<complexType>" }, xsdElt.getComplexType());
                // recovery: let the name go through anyway.
            }

            if (xsdElt.getForm() != null)
            {
                state.error(XmlErrorCodes.SCHEMA_ELEM$REF_FEATURES, new Object[] { "form" }, xsdElt.xgetForm());
                // recovery: let the name go through anyway.
            }

            if (xsdElt.sizeOfKeyArray() > 0)
            {
                state.warning(XmlErrorCodes.SCHEMA_ELEM$REF_FEATURES, new Object[] { "<key>" }, xsdElt);
                // recovery: ignore
            }

            if (xsdElt.sizeOfKeyrefArray() > 0)
            {
                state.warning(XmlErrorCodes.SCHEMA_ELEM$REF_FEATURES, new Object[] { "<keyref>" }, xsdElt);
                // recovery: ignore
            }

            if (xsdElt.sizeOfUniqueArray() > 0)
            {
                state.warning(XmlErrorCodes.SCHEMA_ELEM$REF_FEATURES, new Object[] { "<unique>" }, xsdElt);
                // recovery: ignore
            }

            if (xsdElt.isSetDefault())
            {
                state.warning(XmlErrorCodes.SCHEMA_ELEM$REF_FEATURES, new Object[] { "default" }, xsdElt.xgetDefault());
                // recovery: ignore
            }

            if (xsdElt.isSetFixed())
            {
                state.warning(XmlErrorCodes.SCHEMA_ELEM$REF_FEATURES, new Object[] { "fixed" }, xsdElt.xgetFixed());
                // recovery: ignore
            }

            if (xsdElt.isSetBlock())
            {
                state.warning(XmlErrorCodes.SCHEMA_ELEM$REF_FEATURES, new Object[] { "block" }, xsdElt.xgetBlock());
                // recovery: ignore
            }

            if (xsdElt.isSetNillable())
            {
                state.warning(XmlErrorCodes.SCHEMA_ELEM$REF_FEATURES, new Object[] { "nillable" }, xsdElt.xgetNillable());
                // recovery: ignore
            }

            assert(xsdElt instanceof LocalElement);
            SchemaGlobalElement referenced = state.findGlobalElement(ref, chameleon ? targetNamespace : null, targetNamespace);
            if (referenced == null)
            {
                state.notFoundError(ref, SchemaType.ELEMENT, xsdElt.xgetRef(), true);
                // recovery: ignore this element
                return null;
            }
            SchemaLocalElementImpl target = new SchemaLocalElementImpl();
            target.setParticleType(SchemaParticle.ELEMENT);
            target.setUserData(getUserData(xsdElt));
            copyGlobalElementToLocalElement( referenced, target );
            return target;
        }

        QName qname;
        SchemaLocalElementImpl impl;
        SchemaType sType = null;

        if (xsdElt instanceof LocalElement)
        {
            impl = new SchemaLocalElementImpl();
            boolean qualified = false; // default
            FormChoice form = xsdElt.xgetForm();
            if (form != null)
                qualified = form.getStringValue().equals(FORM_QUALIFIED);
            else if (elemFormDefault != null)
                qualified = elemFormDefault.equals(FORM_QUALIFIED);
            else
            {
                form = findElementFormDefault(xsdElt);
                qualified = form != null && form.getStringValue().equals(FORM_QUALIFIED);
            }

            qname = qualified ? QNameHelper.forLNS(name, targetNamespace) : QNameHelper.forLN(name);
        }
        else
        {
            SchemaGlobalElementImpl gelt = new SchemaGlobalElementImpl(state.getContainer(targetNamespace));
            impl = gelt;

            // Set subst group head
            if (sgHead != null)
            {
                SchemaGlobalElementImpl head = state.findGlobalElement(xsdElt.getSubstitutionGroup(), chameleon ? targetNamespace : null, targetNamespace);
                if (head != null)
                    gelt.setSubstitutionGroup(head.getRef());
            }

            // Set subst group members
            qname = QNameHelper.forLNS(name, targetNamespace);
            SchemaTypeImpl docType = (SchemaTypeImpl)outerType;

            QName[] sgMembers = docType.getSubstitutionGroupMembers();
            QNameSetBuilder transitionRules = new QNameSetBuilder();
            transitionRules.add(qname);

            for (int i = 0 ; i < sgMembers.length ; i++)
            {
                gelt.addSubstitutionGroupMember(sgMembers[i]);
                transitionRules.add(sgMembers[i]);
            }

            impl.setTransitionRules(QNameSet.forSpecification(transitionRules), false);
            impl.setTransitionNotes(QNameSet.EMPTY, true);

            boolean finalExt = false;
            boolean finalRest = false;
            Object ds = xsdElt.getFinal();
            if (ds != null)
            {
                if (ds instanceof String && ds.equals("#all"))
                {
                    // #ALL value
                    finalExt = finalRest = true;
                }
                else if (ds instanceof List)
                {
                    if (((List)ds).contains("extension"))
                        finalExt = true;
                    if (((List)ds).contains("restriction"))
                        finalRest = true;
                }
            }

            gelt.setFinal(finalExt, finalRest);
            gelt.setAbstract(xsdElt.getAbstract());
            gelt.setFilename(findFilename(xsdElt));
            gelt.setParseContext(xsdElt, targetNamespace, chameleon);
        }

        SchemaAnnotationImpl ann = SchemaAnnotationImpl.getAnnotation(state.getContainer(targetNamespace), xsdElt);
        impl.setAnnotation(ann);
        impl.setUserData(getUserData(xsdElt));
        if (xsdElt.getType() != null)
        {
            sType = state.findGlobalType(xsdElt.getType(), chameleon ? targetNamespace : null, targetNamespace );
            if (sType == null)
                state.notFoundError(xsdElt.getType(), SchemaType.TYPE, xsdElt.xgetType(), true);
        }

        boolean simpleTypedef = false;
        Annotated typedef = xsdElt.getComplexType();
        if (typedef == null)
        {
            typedef = xsdElt.getSimpleType();
            simpleTypedef = true;
        }

        if ((sType != null) && typedef != null)
        {
            state.error(XmlErrorCodes.SCHEMA_ELEM$TYPE_ATTR_OR_NESTED_TYPE, null, typedef);
            typedef = null;
        }

        if (typedef != null)
        {
            Object[] grps = state.getCurrentProcessing();
            QName[] context = new QName[grps.length];
            for (int i = 0; i < context.length; i++)
                if (grps[i] instanceof SchemaModelGroupImpl)
                    context[i] = ((SchemaModelGroupImpl ) grps[i]).getName();
            SchemaType repeat = checkRecursiveGroupReference(context, qname, (SchemaTypeImpl)outerType);
            if (repeat != null)
                sType = repeat;
            else
            {
            SchemaTypeImpl sTypeImpl = new SchemaTypeImpl(state.getContainer(targetNamespace));
            sType = sTypeImpl;
            sTypeImpl.setContainerField(impl);
            sTypeImpl.setOuterSchemaTypeRef(outerType == null ? null : outerType.getRef());
            sTypeImpl.setGroupReferenceContext(context);
            // leave the anonymous type unresolved: it will be resolved later.
            anonymousTypes.add(sType);
            sTypeImpl.setSimpleType(simpleTypedef);
            sTypeImpl.setParseContext(typedef, targetNamespace, chameleon,
                 elemFormDefault, attFormDefault, false);
            sTypeImpl.setAnnotation(SchemaAnnotationImpl.getAnnotation(state.getContainer(targetNamespace), typedef));
            sTypeImpl.setUserData(getUserData(typedef));
            }
        }

        if (sType == null)
        {
            // type may inherit from substitution group head
            if (sgHead != null)
            {
                SchemaGlobalElement head = state.findGlobalElement(xsdElt.getSubstitutionGroup(), chameleon ? targetNamespace : null, targetNamespace);

                // Bug - Do I need to copy the type if it's anonymous?
                // If element does not exist, error has already been reported
                if (head != null)
                    sType = head.getType();
            }

        }



        if (sType == null)
            sType = BuiltinSchemaTypeSystem.ST_ANY_TYPE;

        SOAPArrayType wat = null;
        XmlCursor c = xsdElt.newCursor();
        String arrayType = c.getAttributeText(WSDL_ARRAYTYPE_NAME);
        c.dispose();
        if (arrayType != null)
        {
            try
            {
                wat = new SOAPArrayType(arrayType, new NamespaceContext(xsdElt));
            }
            catch (XmlValueOutOfRangeException e)
            {
                state.error(XmlErrorCodes.SOAPARRAY, new Object[] {arrayType}, xsdElt);
            }
        }
        impl.setWsdlArrayType(wat);

        boolean isFixed = xsdElt.isSetFixed();
        if (xsdElt.isSetDefault() && isFixed)
        {
            state.error(XmlErrorCodes.SCHEMA_ELEM$DEFAULT_OR_FIXED, null, xsdElt.xgetFixed());
            // recovery: ignore fixed
            isFixed = false;
        }
        impl.setParticleType(SchemaParticle.ELEMENT);
        impl.setNameAndTypeRef(qname, sType.getRef());
        impl.setNillable(xsdElt.getNillable());
        impl.setDefault(isFixed ? xsdElt.getFixed() : xsdElt.getDefault(), isFixed, xsdElt);

        Object block = xsdElt.getBlock();
        boolean blockExt = false;
        boolean blockRest = false;
        boolean blockSubst = false;

        if (block != null)
        {
            if (block instanceof String && block.equals("#all"))
            {
                // #ALL value
                blockExt = blockRest = blockSubst = true;
            }
            else if (block instanceof List)
            {
                if (((List)block).contains("extension"))
                    blockExt = true;
                if (((List)block).contains("restriction"))
                    blockRest = true;
                if (((List)block).contains("substitution"))
                    blockSubst = true;
            }
        }

        impl.setBlock(blockExt, blockRest, blockSubst);

        boolean constraintFailed = false;

        // Translate Identity constraints

        int length = xsdElt.sizeOfKeyArray() + xsdElt.sizeOfKeyrefArray() + xsdElt.sizeOfUniqueArray();
        SchemaIdentityConstraintImpl[] constraints = new SchemaIdentityConstraintImpl[length];
        int cur = 0;

        // Handle key constraints
        Keybase[] keys = xsdElt.getKeyArray();
        for (int i = 0 ; i < keys.length ; i++, cur++) {
            constraints[cur] = translateIdentityConstraint(keys[i], targetNamespace, chameleon);
            if (constraints[cur] != null)
                constraints[cur].setConstraintCategory(SchemaIdentityConstraint.CC_KEY);
            else
                constraintFailed = true;
        }

        // Handle unique constraints
        Keybase[] uc = xsdElt.getUniqueArray();
        for (int i = 0 ; i < uc.length ; i++, cur++) {
            constraints[cur] = translateIdentityConstraint(uc[i], targetNamespace, chameleon);
            if (constraints[cur] != null)
                constraints[cur].setConstraintCategory(SchemaIdentityConstraint.CC_UNIQUE);
            else
                constraintFailed = true;
        }

        // Handle keyref constraints
        KeyrefDocument.Keyref[] krs = xsdElt.getKeyrefArray();
        for (int i = 0 ; i < krs.length ; i++, cur++) {
            constraints[cur] = translateIdentityConstraint(krs[i], targetNamespace, chameleon);
            if (constraints[cur] != null)
                constraints[cur].setConstraintCategory(SchemaIdentityConstraint.CC_KEYREF);
            else
                constraintFailed = true;
        }

        if (!constraintFailed)
        {
            SchemaIdentityConstraint.Ref[] refs = new SchemaIdentityConstraint.Ref[length];
            for (int i = 0 ; i < refs.length ; i++)
                refs[i] = constraints[i].getRef();

            impl.setIdentityConstraints(refs);
        }

        return impl;
    }

    /**
     * We need to do this because of the following kind of Schemas:
     * <xs:group name="e">
     *     <xs:sequence>
     *         <xs:element name="error">
     *             <xs:complexType>
     *                 <xs:group ref="e"/>
     *             </xs:complexType>
     *         </xs:element>
     *     </xs:sequence>
     * </xs:group>
     * (see JIRA bug XMLBEANS-35)
     * Even though this should not be allowed because it produces an infinite
     * number of anonymous types and local elements nested within each other,
     * the de facto consensus among Schema processors is that this should be
     * valid, therefore we have to detect this situation and "patch up" the
     * Schema object model so that instead of creating a new anonymous type,
     * we refer to the one that was already created earlier.
     * In order to accomplish that, we store inside every anonymous type the
     * list of groups that were dereferenced at the moment the type was created
     * and if the same pattern is about to repeat, it means that we are in a
     * case similar to the above.
     */
    private static SchemaType checkRecursiveGroupReference(QName[] context, QName containingElement, SchemaTypeImpl outerType)
    {
        if (context.length < 1)
            return null;
        SchemaTypeImpl type = outerType;

        while (type != null)
        {
            if (type.getName() != null || type.isDocumentType())
                return null; // not anonymous
            if (containingElement.equals(type.getContainerField().getName()))
            {
                QName[] outerContext = type.getGroupReferenceContext();
                if (outerContext != null && outerContext.length == context.length)
                {
                    // Smells like trouble
                    boolean equal = true;
                    for (int i = 0; i < context.length; i++)
                        if (!(context[i] == null && outerContext[i] == null ||
                              context[i] != null && context[i].equals(outerContext[i])))
                        {
                            equal = false;
                            break;
                        }
                    if (equal)
                        return type;
                }
            }
            type = (SchemaTypeImpl) type.getOuterType();
        }
        return null;
    }

    private static String removeWhitespace(String xpath)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < xpath.length(); i++)
        {
            char ch = xpath.charAt(i);
            if (XMLChar.isSpace(ch))
                continue;
            sb.append(ch);
        }
        return sb.toString();
    }

    public static final org.apache.xmlbeans.impl.regex.RegularExpression XPATH_REGEXP = new org.apache.xmlbeans.impl.regex.RegularExpression("(\\.//)?((((child::)?((\\i\\c*:)?(\\i\\c*|\\*)))|\\.)/)*((((child::)?((\\i\\c*:)?(\\i\\c*|\\*)))|\\.)|((attribute::|@)((\\i\\c*:)?(\\i\\c*|\\*))))(\\|(\\.//)?((((child::)?((\\i\\c*:)?(\\i\\c*|\\*)))|\\.)/)*((((child::)?((\\i\\c*:)?(\\i\\c*|\\*)))|\\.)|((attribute::|@)((\\i\\c*:)?(\\i\\c*|\\*)))))*", "X");

    private static boolean checkXPathSyntax(String xpath)
    {
        if (xpath == null)
            return false;

        // strip whitespace from xpath
        xpath = removeWhitespace(xpath);

        // apply regexp
        synchronized (XPATH_REGEXP)
        {
            return (XPATH_REGEXP.matches(xpath));
        }
    }

    private static SchemaIdentityConstraintImpl translateIdentityConstraint(Keybase parseIC,
        String targetNamespace, boolean chameleon)
    {
        StscState state = StscState.get();

        // first do some checking
        String selector = parseIC.getSelector() == null ? null : parseIC.getSelector().getXpath();
        if (!checkXPathSyntax(selector))
        {
            state.error(XmlErrorCodes.SELECTOR_XPATH, new Object[] { selector }, parseIC.getSelector().xgetXpath());
            return null;
        }

        FieldDocument.Field[] fieldElts = parseIC.getFieldArray();
        for (int j = 0; j < fieldElts.length; j++)
        {
            if (!checkXPathSyntax(fieldElts[j].getXpath()))
            {
                state.error(XmlErrorCodes.FIELDS_XPATH, new Object[] { fieldElts[j].getXpath() }, fieldElts[j].xgetXpath());
                return null;
            }
        }

        // then translate.
        SchemaIdentityConstraintImpl ic = new SchemaIdentityConstraintImpl(state.getContainer(targetNamespace));
        ic.setName(QNameHelper.forLNS(parseIC.getName(), targetNamespace));
        ic.setSelector(parseIC.getSelector().getXpath());
        ic.setParseContext(parseIC, targetNamespace, chameleon);
        SchemaAnnotationImpl ann = SchemaAnnotationImpl.getAnnotation(state.getContainer(targetNamespace), parseIC);
        ic.setAnnotation(ann);
        ic.setUserData(getUserData(parseIC));

        // Set the ns map
        XmlCursor c = parseIC.newCursor();
        Map nsMap = new HashMap();

        c.getAllNamespaces(nsMap);
        nsMap.remove(""); // Remove the default mapping. This cannot be used by the xpath expressions.
        ic.setNSMap(nsMap);
        c.dispose();

        String[] fields = new String[fieldElts.length];
        for (int j = 0 ; j < fields.length ; j++)
            fields[j] = fieldElts[j].getXpath();
        ic.setFields(fields);

        try {
            ic.buildPaths();
        }
        catch (XPath.XPathCompileException e) {
            state.error(XmlErrorCodes.INVALID_XPATH, new Object[] { e.getMessage() }, parseIC);
            return null;
        }

        state.addIdConstraint(ic);
        ic.setFilename(findFilename(parseIC));

        return state.findIdConstraint(ic.getName(), targetNamespace, null);

    }

    public static SchemaModelGroupImpl translateModelGroup(NamedGroup namedGroup, String targetNamespace, boolean chameleon, boolean redefinition)
    {
        String name = namedGroup.getName();
        if (name == null)
        {
            StscState.get().error(XmlErrorCodes.MISSING_NAME, new Object[] { "model group" }, namedGroup);
            return null;
        }
        SchemaContainer c = StscState.get().getContainer(targetNamespace);
        SchemaModelGroupImpl result = new SchemaModelGroupImpl(c);
        SchemaAnnotationImpl ann = SchemaAnnotationImpl.getAnnotation(c, namedGroup);
        FormChoice elemFormDefault = findElementFormDefault(namedGroup);
        FormChoice attFormDefault = findAttributeFormDefault(namedGroup);
        result.init(QNameHelper.forLNS(name, targetNamespace), targetNamespace, chameleon,
            elemFormDefault == null ? null : elemFormDefault.getStringValue(),
            attFormDefault == null ? null : attFormDefault.getStringValue(),
            redefinition, namedGroup, ann, getUserData(namedGroup));
        result.setFilename(findFilename(namedGroup));
        return result;
    }

    public static SchemaAttributeGroupImpl translateAttributeGroup(AttributeGroup attrGroup, String targetNamespace, boolean chameleon, boolean redefinition)
    {
        String name = attrGroup.getName();
        if (name == null)
        {
            StscState.get().error(XmlErrorCodes.MISSING_NAME, new Object[] { "attribute group" }, attrGroup);
            return null;
        }
        SchemaContainer c = StscState.get().getContainer(targetNamespace);
        SchemaAttributeGroupImpl result = new SchemaAttributeGroupImpl(c);
        SchemaAnnotationImpl ann = SchemaAnnotationImpl.getAnnotation(c, attrGroup);
        FormChoice formDefault = findAttributeFormDefault(attrGroup);
        result.init(QNameHelper.forLNS(name, targetNamespace), targetNamespace, chameleon,
            formDefault == null ? null : formDefault.getStringValue(),
            redefinition, attrGroup, ann, getUserData(attrGroup));
        result.setFilename(findFilename(attrGroup));
        return result;
    }

    static FormChoice findAttributeFormDefault(XmlObject obj)
    {
        XmlCursor cur = obj.newCursor();
        while (cur.getObject().schemaType() != Schema.type)
            if (!cur.toParent())
                return null;
        return ((Schema)cur.getObject()).xgetAttributeFormDefault();
    }

    static SchemaLocalAttributeImpl translateAttribute(
        Attribute xsdAttr, String targetNamespace, String formDefault, boolean chameleon,
        List anonymousTypes, SchemaType outerType, SchemaAttributeModel baseModel,
        boolean local)
    {
        StscState state = StscState.get();

        String name = xsdAttr.getName();
        QName ref = xsdAttr.getRef();

        if (ref != null && name != null)
        {
            if (name.equals(ref.getLocalPart()) && uriMatch(targetNamespace, ref.getNamespaceURI()))
                state.warning(XmlErrorCodes.SCHEMA_ATTR$REF_OR_NAME_HAS_BOTH, new Object[] { name }, xsdAttr.xgetRef());
            else
                state.error(XmlErrorCodes.SCHEMA_ATTR$REF_OR_NAME_HAS_BOTH, new Object[] { name }, xsdAttr.xgetRef());
            // ignore name
            name = null;
        }
        if (ref == null && name == null)
        {
            state.error(XmlErrorCodes.SCHEMA_ATTR$REF_OR_NAME_HAS_NEITHER, null, xsdAttr);
            // recovery: ignore this element
            return null;
        }
        if (name != null && !XMLChar.isValidNCName(name))
        {
            state.error(XmlErrorCodes.INVALID_VALUE, new Object[] { name, "name" }, xsdAttr.xgetName());
            // recovery: let the name go through anyway.
        }

        boolean isFixed = false;
        String deftext = null;
        String fmrfixedtext = null;
        QName qname;
        SchemaLocalAttributeImpl sAttr;
        SchemaType sType = null;
        int use = SchemaLocalAttribute.OPTIONAL;

        if (local)
            sAttr = new SchemaLocalAttributeImpl();
        else
        {
            sAttr = new SchemaGlobalAttributeImpl(state.get().getContainer(targetNamespace));
            ((SchemaGlobalAttributeImpl)sAttr).setParseContext(xsdAttr, targetNamespace, chameleon);
        }

        if (ref != null)
        {
            if (xsdAttr.getType() != null)
            {
                state.error(XmlErrorCodes.SCHEMA_ATTR$REF_FEATURES, new Object[] { "type" }, xsdAttr.xgetType());
                // recovery: ignore type, simpleType
            }

            if (xsdAttr.getSimpleType() != null)
            {
                state.error(XmlErrorCodes.SCHEMA_ATTR$REF_FEATURES, new Object[] { "<simpleType>" }, xsdAttr.getSimpleType());
                // recovery: ignore type, simpleType
            }

            if (xsdAttr.getForm() != null)
            {
                state.error(XmlErrorCodes.SCHEMA_ATTR$REF_FEATURES, new Object[] { "form" }, xsdAttr.xgetForm());
                // recovery: ignore form
            }

            SchemaGlobalAttribute referenced = state.findGlobalAttribute(ref, chameleon ? targetNamespace : null, targetNamespace);
            if (referenced == null)
            {
                state.notFoundError(ref, SchemaType.ATTRIBUTE, xsdAttr.xgetRef(), true);
                // recovery: ignore this element
                return null;
            }

            qname = ref;
            use = referenced.getUse();
            sType = referenced.getType();
            deftext = referenced.getDefaultText();
            if (deftext != null)
            {
                isFixed = referenced.isFixed();
                if (isFixed)
                    fmrfixedtext = deftext;
            }
        }
        else
        {
            if (local)
            {
                boolean qualified = false; // default
                FormChoice form = xsdAttr.xgetForm();
                if (form != null)
                    qualified = form.getStringValue().equals(FORM_QUALIFIED);
                else if (formDefault != null)
                    qualified = formDefault.equals(FORM_QUALIFIED);
                else
                {
                    form = findAttributeFormDefault(xsdAttr);
                    qualified = form != null && form.getStringValue().equals(FORM_QUALIFIED);
                }

                qname = qualified ? QNameHelper.forLNS(name, targetNamespace) : QNameHelper.forLN(name);
            }
            else
            {
                qname = QNameHelper.forLNS(name, targetNamespace);
            }

            if (xsdAttr.getType() != null)
            {
                sType = state.findGlobalType(xsdAttr.getType(), chameleon ? targetNamespace : null, targetNamespace );
                if (sType == null)
                    state.notFoundError(xsdAttr.getType(), SchemaType.TYPE, xsdAttr.xgetType(), true);
            }

            if (qname.getNamespaceURI().equals("http://www.w3.org/2001/XMLSchema-instance"))
            {
                state.error(XmlErrorCodes.NO_XSI, new Object[] { "http://www.w3.org/2001/XMLSchema-instance" }, xsdAttr.xgetName());
            }

            if (qname.getNamespaceURI().length() == 0 && qname.getLocalPart().equals("xmlns"))
            {
                state.error(XmlErrorCodes.NO_XMLNS, null, xsdAttr.xgetName());
            }

            LocalSimpleType typedef = xsdAttr.getSimpleType();

            if ((sType != null) && typedef != null)
            {
                state.error(XmlErrorCodes.SCHEMA_ATTR$TYPE_ATTR_OR_NESTED_TYPE, null, typedef);
                typedef = null;
            }

            if (typedef != null)
            {
                SchemaTypeImpl sTypeImpl = new SchemaTypeImpl(state.getContainer(targetNamespace));
                sType = sTypeImpl;
                sTypeImpl.setContainerField(sAttr);
                sTypeImpl.setOuterSchemaTypeRef(outerType == null ? null : outerType.getRef());
                // leave the anonymous type unresolved: it will be resolved later.
                anonymousTypes.add(sType);
                sTypeImpl.setSimpleType(true);
                sTypeImpl.setParseContext(typedef, targetNamespace, chameleon, null, null, false);
                sTypeImpl.setAnnotation(SchemaAnnotationImpl.getAnnotation(state.getContainer(targetNamespace), typedef));
                sTypeImpl.setUserData(getUserData(typedef));
            }

            if (sType == null && baseModel != null && baseModel.getAttribute(qname) != null)
                sType = baseModel.getAttribute(qname).getType();
        }

        if (sType == null)
            sType = BuiltinSchemaTypeSystem.ST_ANY_SIMPLE;

        if (!sType.isSimpleType())
        {
            // KHK: which rule? could use #a-simple_type_definition
            state.error("Attributes must have a simple type (not complex).", XmlErrorCodes.INVALID_SCHEMA, xsdAttr);
            // recovery: switch to the any-type
            sType = BuiltinSchemaTypeSystem.ST_ANY_SIMPLE;
        }

        if (xsdAttr.isSetUse())
        {
            use = translateUseCode(xsdAttr.xgetUse());

            // ignore referenced default if no longer optional
            if (use != SchemaLocalAttribute.OPTIONAL && !isFixed)
                deftext = null;
        }

        if (xsdAttr.isSetDefault() || xsdAttr.isSetFixed())
        {
            if (isFixed && !xsdAttr.isSetFixed())
                state.error("A use of a fixed attribute definition must also be fixed", XmlErrorCodes.REDUNDANT_DEFAULT_FIXED, xsdAttr.xgetFixed());

            isFixed = xsdAttr.isSetFixed();

            if (xsdAttr.isSetDefault() && isFixed)
            {
                state.error(XmlErrorCodes.SCHEMA_ATTR$DEFAULT_OR_FIXED, null, xsdAttr.xgetFixed());
                // recovery: ignore fixed
                isFixed = false;
            }
            deftext = isFixed ? xsdAttr.getFixed() : xsdAttr.getDefault();
            // BUGBUG(radup) this is not good, since they should be compared by value
            // in StscChecker; but there we don't have yet access to the referred attr
            if (fmrfixedtext != null && !fmrfixedtext.equals(deftext))
            {
                state.error(XmlErrorCodes.SCHEMA_ATTR$FIXED_NOT_MATCH, null, xsdAttr.xgetFixed());
                // recovery: reset fixed to the original value
                deftext = fmrfixedtext;
            }
        }

        if (!local)
        {
            ((SchemaGlobalAttributeImpl)sAttr).setFilename(findFilename(xsdAttr));
        }

        SOAPArrayType wat = null;
        XmlCursor c = xsdAttr.newCursor();
        String arrayType = c.getAttributeText(WSDL_ARRAYTYPE_NAME);
        c.dispose();
        if (arrayType != null)
        {
            try
            {
                wat = new SOAPArrayType(arrayType, new NamespaceContext(xsdAttr));
            }
            catch (XmlValueOutOfRangeException e)
            {
                state.error(XmlErrorCodes.SOAPARRAY, new Object[] {arrayType}, xsdAttr);
            }
        }

        SchemaAnnotationImpl ann = SchemaAnnotationImpl.getAnnotation(state.getContainer(targetNamespace), xsdAttr);
        sAttr.init(
            qname,
            sType.getRef(),
            use,
            deftext, xsdAttr, null, isFixed,
            wat, ann, getUserData(xsdAttr));

        return sAttr;
    }

    static int translateUseCode(Attribute.Use attruse)
    {
        if (attruse == null)
            return SchemaLocalAttribute.OPTIONAL;

        String val = attruse.getStringValue();
        if (val.equals("optional"))
            return SchemaLocalAttribute.OPTIONAL;
        if (val.equals("required"))
            return SchemaLocalAttribute.REQUIRED;
        if (val.equals("prohibited"))
            return SchemaLocalAttribute.PROHIBITED;
        return SchemaLocalAttribute.OPTIONAL;
    }

    static BigInteger buildBigInt(XmlAnySimpleType value)
    {
        if (value == null)
            return null;
        String text = value.getStringValue();
        BigInteger bigInt;
        try
        {
            bigInt = new BigInteger(text);
        }
        catch (NumberFormatException e)
        {
            StscState.get().error(XmlErrorCodes.INVALID_VALUE_DETAIL, new Object[] { text, "nonNegativeInteger", e.getMessage() }, value);
            return null;
        }

        if (bigInt.signum() < 0)
        {
            StscState.get().error(XmlErrorCodes.INVALID_VALUE, new Object[] { text, "nonNegativeInteger" }, value);
            return null;
        }

        return bigInt;
    }


    static XmlNonNegativeInteger buildNnInteger(XmlAnySimpleType value)
    {
        BigInteger bigInt = buildBigInt(value);
        try
        {
            XmlNonNegativeIntegerImpl i = new XmlNonNegativeIntegerImpl();
            i.set(bigInt);
            i.setImmutable();
            return i;
        }
        catch (XmlValueOutOfRangeException e)
        {
            StscState.get().error("Internal error processing number", XmlErrorCodes.MALFORMED_NUMBER, value);
            return null;
        }
    }

    static XmlPositiveInteger buildPosInteger(XmlAnySimpleType value)
    {
        BigInteger bigInt = buildBigInt(value);
        try
        {
            XmlPositiveIntegerImpl i = new XmlPositiveIntegerImpl();
            i.set(bigInt);
            i.setImmutable();
            return i;
        }
        catch (XmlValueOutOfRangeException e)
        {
            StscState.get().error("Internal error processing number", XmlErrorCodes.MALFORMED_NUMBER, value);
            return null;
        }
    }


    private static Object getUserData(XmlObject pos)
    {
        XmlCursor.XmlBookmark b = pos.newCursor().getBookmark(SchemaBookmark.class);
        if (b != null && b instanceof SchemaBookmark)
            return ((SchemaBookmark) b).getValue();
        else
            return null;
    }

    private static boolean isEmptySchema(Schema schema)
    {
        XmlCursor cursor = schema.newCursor();
        boolean result = !cursor.toFirstChild();
        cursor.dispose();
        return result;
    }

    private static boolean isReservedTypeName(QName name)
    {
        return (BuiltinSchemaTypeSystem.get().findType(name) != null);
    }
}
