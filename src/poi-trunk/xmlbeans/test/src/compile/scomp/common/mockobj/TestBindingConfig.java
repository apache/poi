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
package compile.scomp.common.mockobj;

import org.apache.xmlbeans.BindingConfig;
import org.apache.xmlbeans.InterfaceExtension;
import org.apache.xmlbeans.PrePostExtension;
import org.apache.xmlbeans.impl.config.BindingConfigImpl;

import org.apache.xmlbeans.impl.xb.xmlconfig.ConfigDocument.Config;
import org.apache.xmlbeans.impl.xb.xmlconfig.Extensionconfig;
import org.apache.xmlbeans.impl.xb.xmlconfig.Nsconfig;
import org.apache.xmlbeans.impl.xb.xmlconfig.Qnameconfig;
import org.apache.xmlbeans.impl.xb.xmlconfig.ConfigDocument;
import org.apache.xmlbeans.BindingConfig;

import javax.xml.namespace.QName;
import java.io.File;

/**
 *
 * 
 */
public class TestBindingConfig extends BindingConfig
{
    BindingConfig bindingConfig;
    private boolean islookupPackageForNamespace;
    private boolean islookupPrefixForNamespace;
    private boolean islookupSuffixForNamespace;
    private boolean islookupJavanameForQName;
    private boolean isgetInterfaceExtensions;
    private boolean isgetInterfaceExtensionsString;
    private boolean isgetPrePostExtensions;
    private boolean isgetPrePostExtensionsString;

    public TestBindingConfig(ConfigDocument.Config[] configs, File[] javaFiles, File[] classpath)
    {

        bindingConfig = BindingConfigImpl.forConfigDocuments(configs, javaFiles, classpath);
        islookupPackageForNamespace = false;
        islookupPrefixForNamespace = false;
        islookupSuffixForNamespace = false;
        islookupJavanameForQName = false;
        isgetInterfaceExtensions = false;
        isgetInterfaceExtensionsString = false;
        isgetPrePostExtensions = false;
        isgetPrePostExtensionsString = false;
    }

    public boolean isIslookupPackageForNamespace()
    {
        return islookupPackageForNamespace;
    }
                                                         
    public boolean isIslookupPrefixForNamespace()
    {
        return islookupPrefixForNamespace;
    }

    public boolean isIslookupSuffixForNamespace()
    {
        return islookupSuffixForNamespace;
    }

    public boolean isIslookupJavanameForQName()
    {
        return islookupJavanameForQName;
    }

    public boolean isIsgetInterfaceExtensions()
    {
        return isgetInterfaceExtensions;
    }

    public boolean isIsgetInterfaceExtensionsString()
    {
        return isgetInterfaceExtensionsString;
    }

    public boolean isIsgetPrePostExtensions()
    {
        return isgetPrePostExtensions;
    }

    public boolean isIsgetPrePostExtensionsString()
    {
        return isgetPrePostExtensionsString;
    }

    public String lookupPackageForNamespace(String s)
    {
        System.out.println("lookupPackageForNamespace: "+s);
        islookupPackageForNamespace = true;
        return bindingConfig.lookupPackageForNamespace(s);
    }

    public String lookupPrefixForNamespace(String s)
    {
        System.out.println("lookupPrefixForNamespace: "+s);
        islookupPrefixForNamespace = true;
        return bindingConfig.lookupPrefixForNamespace(s);
    }

    public String lookupSuffixForNamespace(String s)
    {
        System.out.println("lookupSuffixForNamespace: "+s);
        islookupSuffixForNamespace = true;
        return bindingConfig.lookupSuffixForNamespace(s);
    }

    /** @deprecated */
    public String lookupJavanameForQName(QName qName)
    {
        System.out.println("lookupJavanameForQName: "+qName);
        islookupJavanameForQName = true;
        return bindingConfig.lookupJavanameForQName(qName);
    }

    public String lookupJavanameForQName(QName qName, int kind)
    {
        System.out.println("lookupJavanameForQName: "+qName);
        islookupJavanameForQName = true;
        return bindingConfig.lookupJavanameForQName(qName, kind);
    }

    public InterfaceExtension[] getInterfaceExtensions()
    {
        System.out.println("getInterfaceExtensions ");
        isgetInterfaceExtensions = true;
        return bindingConfig.getInterfaceExtensions();
    }

    public InterfaceExtension[] getInterfaceExtensions(String s)
    {
        System.out.println("getInterfaceExtensions: "+s);
        isgetInterfaceExtensionsString = true;
        return bindingConfig.getInterfaceExtensions(s);
    }

    public PrePostExtension[] getPrePostExtensions()
    {
        System.out.println("getPrePostExtensions");
        isgetPrePostExtensions = true;
        return bindingConfig.getPrePostExtensions();
    }

    public PrePostExtension getPrePostExtension(String s)
    {
        System.out.println("getPrePostExtension: "+s);
        isgetPrePostExtensionsString = true;
        return bindingConfig.getPrePostExtension(s);
    }
}
