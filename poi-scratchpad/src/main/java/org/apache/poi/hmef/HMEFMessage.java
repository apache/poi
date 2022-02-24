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

package org.apache.poi.hmef;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.poi.hmef.attribute.MAPIAttribute;
import org.apache.poi.hmef.attribute.MAPIStringAttribute;
import org.apache.poi.hmef.attribute.TNEFAttribute;
import org.apache.poi.hmef.attribute.TNEFMAPIAttribute;
import org.apache.poi.hmef.attribute.TNEFProperty;
import org.apache.poi.hsmf.datatypes.MAPIProperty;
import org.apache.poi.util.LittleEndian;

/**
 * HMEF - Implementation of the Microsoft TNEF message
 *  encoding format (aka winmail.dat)
 * See:
 *   http://support.microsoft.com/kb/241538
 *   http://en.wikipedia.org/wiki/Transport_Neutral_Encapsulation_Format
 *   http://search.cpan.org/dist/Convert-TNEF/
 */
public final class HMEFMessage {
    public static final int HEADER_SIGNATURE = 0x223e9f78;

    @SuppressWarnings("unused")
    private int fileId;
    private final List<TNEFAttribute> messageAttributes = new ArrayList<>();
    private final List<MAPIAttribute> mapiAttributes = new ArrayList<>();
    private final List<Attachment> attachments = new ArrayList<>();

    /**
     * @param inp input stream
     * @throws IOException If reading data from the stream fails
     * @throws RuntimeException a number of runtime exceptions can be thrown, especially if there are problems with the
     * input format
     */
    public HMEFMessage(InputStream inp) throws IOException {
        try {
            // Check the signature matches
            int sig = LittleEndian.readInt(inp);
            if (sig != HEADER_SIGNATURE) {
                throw new IllegalArgumentException(
                        "TNEF signature not detected in file, " +
                        "expected " + HEADER_SIGNATURE + " but got " + sig
                );
            }

            // Read the File ID
            fileId = LittleEndian.readUShort(inp);

            // Now begin processing the contents
            process(inp);
        } finally {
            inp.close();
        }
    }

    private void process(InputStream inp) throws IOException {
       int level;
       do {
           // Fetch the level
           level = inp.read();

           // Decide what to attach it to, based on the levels and IDs
           switch (level) {
               case TNEFProperty.LEVEL_MESSAGE:
                   processMessage(inp);
                   break;
                case TNEFProperty.LEVEL_ATTACHMENT:
                   processAttachment(inp);
                   break;
               // ignore trailing newline
                case '\r':
                case '\n':
                case TNEFProperty.LEVEL_END_OF_FILE:
                    break;
                default:
                    throw new IllegalStateException("Unhandled level " + level);
            }
        } while (level != TNEFProperty.LEVEL_END_OF_FILE);
    }

    void processMessage(InputStream inp) throws IOException {
        // Build the attribute
        TNEFAttribute attr = TNEFAttribute.create(inp);

        messageAttributes.add(attr);

        if (attr instanceof TNEFMAPIAttribute) {
            TNEFMAPIAttribute tnefMAPI = (TNEFMAPIAttribute) attr;
            mapiAttributes.addAll(tnefMAPI.getMAPIAttributes());
        }
    }

    void processAttachment(InputStream inp) throws IOException {
        // Build the attribute
        TNEFAttribute attr = TNEFAttribute.create(inp);

        // Previous attachment or a new one?
        if (attachments.isEmpty()
                || attr.getProperty() == TNEFProperty.ID_ATTACHRENDERDATA) {
            attachments.add(new Attachment());
        }

        // Save the attribute for it
        Attachment attach = attachments.get(attachments.size() - 1);
        attach.addAttribute(attr);
    }

    /**
     * Returns all HMEF/TNEF attributes of the message.
     * Note - In a typical message, most of the interesting properties
     *  are stored as {@link MAPIAttribute}s - see {@link #getMessageMAPIAttributes()}
     */
    public List<TNEFAttribute> getMessageAttributes() {
        return Collections.unmodifiableList(messageAttributes);
    }

    /**
     * Returns all MAPI attributes of the message.
     * Note - A small number of HMEF/TNEF specific attributes normally
     *  apply to most messages, see {@link #getMessageAttributes()}
     */
    public List<MAPIAttribute> getMessageMAPIAttributes() {
        return Collections.unmodifiableList(mapiAttributes);
    }

    /**
     * Returns all the Attachments of the message.
     */
    public List<Attachment> getAttachments() {
        return Collections.unmodifiableList(attachments);
    }

    /**
     * Return the message attribute with the given ID,
     *  or null if there isn't one.
     */
    public TNEFAttribute getMessageAttribute(TNEFProperty id) {
        for (TNEFAttribute attr : messageAttributes) {
            if (attr.getProperty() == id) {
                return attr;
            }
        }
        return null;
    }

    /**
     * Return the message MAPI Attribute with the given ID,
     *  or null if there isn't one.
     */
    public MAPIAttribute getMessageMAPIAttribute(MAPIProperty id) {
        for (MAPIAttribute attr : mapiAttributes) {
            // Because of custom properties, match on ID not literal property object
            if (attr.getProperty().id == id.id) {
                return attr;
            }
        }
        return null;
    }

    /**
     * Return the string value of the mapi property, or null
     *  if it isn't set
     */
    private String getString(MAPIProperty id) {
        return MAPIStringAttribute.getAsString( getMessageMAPIAttribute(id) );
    }

    /**
     * Returns the Message Subject, or null if the mapi property
     *  for this isn't set
     */
    public String getSubject() {
        return getString(MAPIProperty.CONVERSATION_TOPIC);
    }

    /**
     * Returns the Message Body, as RTF, or null if the mapi property
     *  for this isn't set
     */
    public String getBody() {
        return getString(MAPIProperty.RTF_COMPRESSED);
    }
}
