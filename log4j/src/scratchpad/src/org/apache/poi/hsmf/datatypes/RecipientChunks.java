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

package org.apache.poi.hsmf.datatypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Collection of convenience chunks for the Recip(ient) part of an outlook file.
 *
 * If a message has multiple recipients, there will be several of these.
 */
public final class RecipientChunks implements ChunkGroupWithProperties {
    private static final POILogger LOG = POILogFactory.getLogger(RecipientChunks.class);

    public static final String PREFIX = "__recip_version1.0_#";

    public static final MAPIProperty RECIPIENT_NAME = MAPIProperty.DISPLAY_NAME;
    public static final MAPIProperty DELIVERY_TYPE = MAPIProperty.ADDRTYPE;
    public static final MAPIProperty RECIPIENT_EMAIL_ADDRESS = MAPIProperty.EMAIL_ADDRESS;
    public static final MAPIProperty RECIPIENT_SEARCH = MAPIProperty.SEARCH_KEY;
    public static final MAPIProperty RECIPIENT_SMTP_ADDRESS = MAPIProperty.SMTP_ADDRESS;
    public static final MAPIProperty RECIPIENT_DISPLAY_NAME = MAPIProperty.RECIPIENT_DISPLAY_NAME;

    /** Our 0 based position in the list of recipients */
    private int recipientNumber;

    /** TODO */
    private ByteChunk recipientSearchChunk;
    /**
     * The "name", which could be their name if an internal person, or their
     * email address if an external person
     */
    private StringChunk recipientNameChunk;
    /**
     * The email address of the recipient, which could be in SMTP or SEARCH
     * format, but isn't always present...
     */
    private StringChunk recipientEmailChunk;
    /**
     * The smtp destination email address of the recipient, but isn't always
     * present...
     */
    private StringChunk recipientSMTPChunk;
    /**
     * Normally EX or SMTP. Will generally affect where the email address ends
     * up.
     */
    private StringChunk deliveryTypeChunk;
    /**
     * The display name of the recipient. Normally seems to hold the same value
     * as in recipientNameChunk
     */
    private StringChunk recipientDisplayNameChunk;
    /**
     * Holds the fixed sized properties, and the pointers to the data of
     * variable sized ones
     */
    private PropertiesChunk recipientProperties;

    public RecipientChunks(String name) {
        recipientNumber = -1;
        int splitAt = name.lastIndexOf('#');
        if (splitAt > -1) {
            String number = name.substring(splitAt + 1);
            try {
                recipientNumber = Integer.parseInt(number, 16);
            } catch (NumberFormatException e) {
                LOG.log(POILogger.ERROR, "Invalid recipient number in name ", name);
            }
        }
    }

    public int getRecipientNumber() {
        return recipientNumber;
    }

    public ByteChunk getRecipientSearchChunk() {
        return recipientSearchChunk;
    }

    public StringChunk getRecipientNameChunk() {
        return recipientNameChunk;
    }

    public StringChunk getRecipientEmailChunk() {
        return recipientEmailChunk;
    }

    public StringChunk getRecipientSMTPChunk() {
        return recipientSMTPChunk;
    }

    public StringChunk getDeliveryTypeChunk() {
        return deliveryTypeChunk;
    }

    public StringChunk getRecipientDisplayNameChunk() {
        return recipientDisplayNameChunk;
    }

    /**
     * Tries to find their name, in whichever chunk holds it.
     */
    public String getRecipientName() {
        if (recipientNameChunk != null) {
            return recipientNameChunk.getValue();
        }
        if (recipientDisplayNameChunk != null) {
            return recipientDisplayNameChunk.getValue();
        }

        // Can't find it
        return null;
    }

    /**
     * Tries to find their email address, in whichever chunk holds it given the
     * delivery type.
     */
    public String getRecipientEmailAddress() {
        // If we have this, it really has the email
        if (recipientSMTPChunk != null) {
            return recipientSMTPChunk.getValue();
        }

        // This might be a real email, or might be
        // in CN=... format
        if (recipientEmailChunk != null) {
            String email = recipientEmailChunk.getValue();
            int cne = email.indexOf("/CN=");
            if (cne < 0) {
                // Normal smtp address
                return email;
            } else {
                // /O=..../CN=em@ail
                return email.substring(cne + 4);
            }
        }

        // Might be in the name field, check there
        if (recipientNameChunk != null) {
            String name = recipientNameChunk.getValue();
            if (name.contains("@")) {
                // Strip leading and trailing quotes if needed
                if (name.startsWith("'") && name.endsWith("'")) {
                    return name.substring(1, name.length() - 1);
                }
                return name;
            }
        }

        // Check the search chunk, see if it's
        // encoded as a SMTP destination in there.
        if (recipientSearchChunk != null) {
            String search = recipientSearchChunk.getAs7bitString();
            int idx = search.indexOf("SMTP:");
            if (idx >= 0) {
                return search.substring(idx + 5);
            }
        }

        // Can't find it
        return null;
    }

    /** Holds all the chunks that were found. */
    private List<Chunk> allChunks = new ArrayList<>();

    @Override
    public Map<MAPIProperty, List<PropertyValue>> getProperties() {
        if (recipientProperties != null) {
            return recipientProperties.getProperties();
        } else {
            return Collections.emptyMap();
        }
    }

    public Chunk[] getAll() {
        return allChunks.toArray(new Chunk[0]);
    }

    @Override
    public Chunk[] getChunks() {
        return getAll();
    }

    /**
     * Called by the parser whenever a chunk is found.
     */
    @Override
    public void record(Chunk chunk) {
        if (chunk.getChunkId() == RECIPIENT_SEARCH.id) {
            // TODO - parse
            recipientSearchChunk = (ByteChunk) chunk;
        } else if (chunk.getChunkId() == RECIPIENT_NAME.id) {
            recipientDisplayNameChunk = (StringChunk) chunk;
        } else if (chunk.getChunkId() == RECIPIENT_DISPLAY_NAME.id) {
            recipientNameChunk = (StringChunk) chunk;
        } else if (chunk.getChunkId() == RECIPIENT_EMAIL_ADDRESS.id) {
            recipientEmailChunk = (StringChunk) chunk;
        } else if (chunk.getChunkId() == RECIPIENT_SMTP_ADDRESS.id) {
            recipientSMTPChunk = (StringChunk) chunk;
        } else if (chunk.getChunkId() == DELIVERY_TYPE.id) {
            deliveryTypeChunk = (StringChunk) chunk;
        } else if (chunk instanceof PropertiesChunk) {
            recipientProperties = (PropertiesChunk) chunk;
        }

        // And add to the main list
        allChunks.add(chunk);
    }

    @Override
    public void chunksComplete() {
        if (recipientProperties != null) {
            recipientProperties.matchVariableSizedPropertiesToChunks();
        } else {
            LOG.log(POILogger.WARN, "Recipeints Chunk didn't contain a list of properties!");
        }
    }

    /**
     * Orders by the recipient number.
     */
    public static class RecipientChunksSorter
            implements Comparator<RecipientChunks>, Serializable {
        @Override
        public int compare(RecipientChunks a, RecipientChunks b) {
            return Integer.compare(a.recipientNumber, b.recipientNumber);
        }
    }
}
