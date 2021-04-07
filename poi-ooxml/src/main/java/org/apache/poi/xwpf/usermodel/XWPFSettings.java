/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package org.apache.poi.xwpf.usermodel;

import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.xml.namespace.QName;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.util.POIXMLUnits;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.poifs.crypt.CryptoFunctions;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.STAlgClass;
import org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.STAlgType;
import org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.STCryptProv;
import org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.STOnOff1;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDocProtect;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTOnOff;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSettings;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTZoom;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STDocProtect;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.SettingsDocument;

public class XWPFSettings extends POIXMLDocumentPart {

    private CTSettings ctSettings;

    /**
     * @since POI 3.14-Beta1
     */
    public XWPFSettings(PackagePart part) throws IOException {
        super(part);
    }

    public XWPFSettings() {
        super();
        ctSettings = CTSettings.Factory.newInstance();
    }

    @Override
    protected void onDocumentRead() throws IOException {
        super.onDocumentRead();
        readFrom(getPackagePart().getInputStream());
    }

    /**
     * Set zoom.<br>
     * In the zoom tag inside settings.xml file <br>
     * it sets the value of zoom
     * <br>
     * sample snippet from settings.xml
     * <pre>
     *    &lt;w:zoom w:percent="50" /&gt;
     * <pre>
     *
     * @return percentage as an integer of zoom level
     */
    public long getZoomPercent() {
        CTZoom zoom;
        if (!ctSettings.isSetZoom()) {
            zoom = ctSettings.addNewZoom();
        } else {
            zoom = ctSettings.getZoom();
        }


        return (zoom.getPercent() == null) ? 100 : POIXMLUnits.parsePercent(zoom.xgetPercent()) / 1000;
    }

    /**
     * Set zoom.<br>
     * In the zoom tag inside settings.xml file <br>
     * it sets the value of zoom
     * <br>
     * sample snippet from settings.xml
     * <pre>
     *    &lt;w:zoom w:percent="50" /&gt;
     * <pre>
     */
    public void setZoomPercent(long zoomPercent) {
        if (!ctSettings.isSetZoom()) {
            ctSettings.addNewZoom();
        }
        CTZoom zoom = ctSettings.getZoom();
        zoom.setPercent(BigInteger.valueOf(zoomPercent));
    }

	/**
     * Verifies the documentProtection tag inside settings.xml file <br>
     * if the protection is enforced (w:enforcement="1") <br>
     *  <p>
     * <br>
     * sample snippet from settings.xml
     * <pre>
     *     &lt;w:settings  ... &gt;
     *         &lt;w:documentProtection w:edit=&quot;readOnly&quot; w:enforcement=&quot;1&quot;/&gt;
     * </pre>
     *
     * @return true if documentProtection is enforced with option any
     */
	public boolean isEnforcedWith() {
        CTDocProtect ctDocProtect = ctSettings.getDocumentProtection();
        return ctDocProtect != null && POIXMLUnits.parseOnOff(ctDocProtect.xgetEnforcement());

    }

    /**
     * Verifies the documentProtection tag inside settings.xml file <br>
     * if the protection is enforced (w:enforcement="1") <br>
     * and if the kind of protection equals to passed (STDocProtect.Enum editValue) <br>
     * <p>
     * <br>
     * sample snippet from settings.xml
     * <pre>
     *     &lt;w:settings  ... &gt;
     *         &lt;w:documentProtection w:edit=&quot;readOnly&quot; w:enforcement=&quot;1&quot;/&gt;
     * </pre>
     *
     * @return true if documentProtection is enforced with option readOnly
     */
    public boolean isEnforcedWith(STDocProtect.Enum editValue) {
        CTDocProtect ctDocProtect = ctSettings.getDocumentProtection();
        return ctDocProtect != null && POIXMLUnits.parseOnOff(ctDocProtect.xgetEnforcement()) && ctDocProtect.getEdit().equals(editValue);

    }

    /**
     * Enforces the protection with the option specified by passed editValue.<br>
     * <br>
     * In the documentProtection tag inside settings.xml file <br>
     * it sets the value of enforcement to "1" (w:enforcement="1") <br>
     * and the value of edit to the passed editValue (w:edit="[passed editValue]")<br>
     * <br>
     * sample snippet from settings.xml
     * <pre>
     *     &lt;w:settings  ... &gt;
     *         &lt;w:documentProtection w:edit=&quot;[passed editValue]&quot; w:enforcement=&quot;1&quot;/&gt;
     * </pre>
     */
    public void setEnforcementEditValue(org.openxmlformats.schemas.wordprocessingml.x2006.main.STDocProtect.Enum editValue) {
        safeGetDocumentProtection().setEnforcement(STOnOff1.ON);
        safeGetDocumentProtection().setEdit(editValue);
    }

    /**
     * Enforces the protection with the option specified by passed editValue and password.<br>
     * <br>
     * sample snippet from settings.xml
     * <pre>
     *   &lt;w:documentProtection w:edit=&quot;[passed editValue]&quot; w:enforcement=&quot;1&quot;
     *       w:cryptProviderType=&quot;rsaAES&quot; w:cryptAlgorithmClass=&quot;hash&quot;
     *       w:cryptAlgorithmType=&quot;typeAny&quot; w:cryptAlgorithmSid=&quot;14&quot;
     *       w:cryptSpinCount=&quot;100000&quot; w:hash=&quot;...&quot; w:salt=&quot;....&quot;
     *   /&gt;
     * </pre>
     *
     * @param editValue the protection type
     * @param password  the plaintext password, if null no password will be applied
     * @param hashAlgo  the hash algorithm - only md2, m5, sha1, sha256, sha384 and sha512 are supported.
     *                  if null, it will default default to sha1
     */
    public void setEnforcementEditValue(org.openxmlformats.schemas.wordprocessingml.x2006.main.STDocProtect.Enum editValue,
                                        String password, HashAlgorithm hashAlgo) {
        safeGetDocumentProtection().setEnforcement(STOnOff1.ON);
        safeGetDocumentProtection().setEdit(editValue);

        if (password == null) {
            if (safeGetDocumentProtection().isSetCryptProviderType()) {
                safeGetDocumentProtection().unsetCryptProviderType();
            }

            if (safeGetDocumentProtection().isSetCryptAlgorithmClass()) {
                safeGetDocumentProtection().unsetCryptAlgorithmClass();
            }

            if (safeGetDocumentProtection().isSetCryptAlgorithmType()) {
                safeGetDocumentProtection().unsetCryptAlgorithmType();
            }

            if (safeGetDocumentProtection().isSetCryptAlgorithmSid()) {
                safeGetDocumentProtection().unsetCryptAlgorithmSid();
            }

            if (safeGetDocumentProtection().isSetSalt()) {
                safeGetDocumentProtection().unsetSalt();
            }

            if (safeGetDocumentProtection().isSetCryptSpinCount()) {
                safeGetDocumentProtection().unsetCryptSpinCount();
            }

            if (safeGetDocumentProtection().isSetHash()) {
                safeGetDocumentProtection().unsetHash();
            }
        } else {
            final STCryptProv.Enum providerType;
            final int sid;
            if (hashAlgo == null) {
                hashAlgo = HashAlgorithm.sha1;
            }

            switch (hashAlgo) {
                case md2:
                    providerType = STCryptProv.RSA_FULL;
                    sid = 1;
                    break;
                case md4:
                    providerType = STCryptProv.RSA_FULL;
                    sid = 2;
                    break;
                case md5:
                    providerType = STCryptProv.RSA_FULL;
                    sid = 3;
                    break;
                case sha1:
                    providerType = STCryptProv.RSA_FULL;
                    sid = 4;
                    break;
                case sha256:
                    providerType = STCryptProv.RSA_AES;
                    sid = 12;
                    break;
                case sha384:
                    providerType = STCryptProv.RSA_AES;
                    sid = 13;
                    break;
                case sha512:
                    providerType = STCryptProv.RSA_AES;
                    sid = 14;
                    break;
                default:
                    throw new EncryptedDocumentException
                            ("Hash algorithm '" + hashAlgo + "' is not supported for document write protection.");
            }


            SecureRandom random = new SecureRandom();
            byte[] salt = random.generateSeed(16);

            // Iterations specifies the number of times the hashing function shall be iteratively run (using each
            // iteration's result as the input for the next iteration).
            int spinCount = 100000;

            String legacyHash = CryptoFunctions.xorHashPasswordReversed(password);
            // Implementation Notes List:
            // --> In this third stage, the reversed byte order legacy hash from the second stage shall
            //     be converted to Unicode hex string representation
            byte[] hash = CryptoFunctions.hashPassword(legacyHash, hashAlgo, salt, spinCount, false);

            safeGetDocumentProtection().setSalt(salt);
            safeGetDocumentProtection().setHash(hash);
            safeGetDocumentProtection().setCryptSpinCount(BigInteger.valueOf(spinCount));
            safeGetDocumentProtection().setCryptAlgorithmType(STAlgType.TYPE_ANY);
            safeGetDocumentProtection().setCryptAlgorithmClass(STAlgClass.HASH);
            safeGetDocumentProtection().setCryptProviderType(providerType);
            safeGetDocumentProtection().setCryptAlgorithmSid(BigInteger.valueOf(sid));
        }
    }

    /**
     * Validates the existing password
     *
     * @param password the password
     * @return true, only if password was set and equals, false otherwise
     */
    public boolean validateProtectionPassword(String password) {
        BigInteger sid = safeGetDocumentProtection().getCryptAlgorithmSid();
        byte[] hash = safeGetDocumentProtection().getHash();
        byte[] salt = safeGetDocumentProtection().getSalt();
        BigInteger spinCount = safeGetDocumentProtection().getCryptSpinCount();

        if (sid == null || hash == null || salt == null || spinCount == null) return false;

        HashAlgorithm hashAlgo;
        switch (sid.intValue()) {
            case 1:
                hashAlgo = HashAlgorithm.md2;
                break;
            case 2:
                hashAlgo = HashAlgorithm.md4;
                break;
            case 3:
                hashAlgo = HashAlgorithm.md5;
                break;
            case 4:
                hashAlgo = HashAlgorithm.sha1;
                break;
            case 12:
                hashAlgo = HashAlgorithm.sha256;
                break;
            case 13:
                hashAlgo = HashAlgorithm.sha384;
                break;
            case 14:
                hashAlgo = HashAlgorithm.sha512;
                break;
            default:
                return false;
        }

        String legacyHash = CryptoFunctions.xorHashPasswordReversed(password);
        // Implementation Notes List:
        // --> In this third stage, the reversed byte order legacy hash from the second stage shall
        //     be converted to Unicode hex string representation
        byte[] hash2 = CryptoFunctions.hashPassword(legacyHash, hashAlgo, salt, spinCount.intValue(), false);

        return Arrays.equals(hash, hash2);
    }

    /**
     * Removes protection enforcement.<br>
     * In the documentProtection tag inside settings.xml file <br>
     * it sets the value of enforcement to "0" (w:enforcement="0") <br>
     */
    public void removeEnforcement() {
        safeGetDocumentProtection().setEnforcement(STOnOff1.OFF);
    }

    /**
     * Enforces fields update on document open (in Word).
     * In the settings.xml file <br>
     * sets the updateSettings value to true (w:updateSettings w:val="true")
     * <p>
     * NOTICES:
     * <ul>
     * <li>Causing Word to ask on open: "This document contains fields that may refer to other files. Do you want to update the fields in this document?"
     * (if "Update automatic links at open" is enabled)</li>
     * <li>Flag is removed after saving with changes in Word </li>
     * </ul>
     */
    public void setUpdateFields() {
        CTOnOff onOff = CTOnOff.Factory.newInstance();
        onOff.setVal(STOnOff1.ON);
        ctSettings.setUpdateFields(onOff);
    }

    boolean isUpdateFields() {
        return ctSettings.isSetUpdateFields() && POIXMLUnits.parseOnOff(ctSettings.getUpdateFields().xgetVal());
    }

    /**
     * Check if revision tracking is turned on.
     *
     * @return <code>true</code> if revision tracking is turned on
     */
    public boolean isTrackRevisions() {
        return ctSettings.isSetTrackRevisions();
    }

    /**
     * Enable or disable revision tracking.
     *
     * @param enable <code>true</code> to  turn on revision tracking, <code>false</code> to turn off revision tracking
     */
    public void setTrackRevisions(boolean enable) {
        if (enable) {
            if (!ctSettings.isSetTrackRevisions()) {
                ctSettings.addNewTrackRevisions();
            }
        } else {
            if (ctSettings.isSetTrackRevisions()) {
                ctSettings.unsetTrackRevisions();
            }
        }
    }

    @Override
    protected void commit() throws IOException {
        if (ctSettings == null) {
            throw new IllegalStateException("Unable to write out settings that were never read in!");
        }

        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        xmlOptions.setSaveSyntheticDocumentElement(new QName(CTSettings.type.getName().getNamespaceURI(), "settings"));

        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        ctSettings.save(out, xmlOptions);
        out.close();
    }

    private CTDocProtect safeGetDocumentProtection() {
        CTDocProtect documentProtection = ctSettings.getDocumentProtection();
        if (documentProtection == null) {
            documentProtection = CTDocProtect.Factory.newInstance();
            ctSettings.setDocumentProtection(documentProtection);
        }
        return ctSettings.getDocumentProtection();
    }

    private void readFrom(InputStream inputStream) {
        try {
            ctSettings = SettingsDocument.Factory.parse(inputStream, DEFAULT_XML_OPTIONS).getSettings();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Check if separate even and odd headings is turned on.
     *
     * @return True if even and odd headings is turned on.
     */
    public boolean getEvenAndOddHeadings() {
        return ctSettings.isSetEvenAndOddHeaders();
    }

    /**
     * Turn separate even-and-odd headings on or off
     *
     * @param enable <code>true</code> to turn on separate even and odd headings,
     * <code>false</code> to turn off even and odd headings.
     */
    public void setEvenAndOddHeadings(boolean enable) {
        CTOnOff onOff = CTOnOff.Factory.newInstance();
        onOff.setVal(enable ? STOnOff1.ON : STOnOff1.OFF);
        ctSettings.setEvenAndOddHeaders(onOff);
    }

    /**
     * Check if mirrored margins is turned on
     *
     * @return True if mirrored margins is turned on.
     */
    public boolean getMirrorMargins() {
        return ctSettings.isSetMirrorMargins();
    }

    /**
     * Turn mirrored margins on or off
     *
     * @param enable <code>true</code> to turn on mirrored margins,
     * <code>false</code> to turn off mirrored marginss.
     */
    public void setMirrorMargins(boolean enable) {
        CTOnOff onOff = CTOnOff.Factory.newInstance();
        onOff.setVal(enable ? STOnOff1.ON : STOnOff1.OFF);
        ctSettings.setMirrorMargins(onOff);
    }


}
