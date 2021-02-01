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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Collection of convenience chunks for standard parts of the MSG file.
 *
 * Not all of these will be present in any given file.
 *
 * A partial list is available at:
 * http://msdn.microsoft.com/en-us/library/ms526356%28v=exchg.10%29.aspx
 *
 * TODO Deprecate the public Chunks in favour of Property Lookups
 */
public final class Chunks implements ChunkGroupWithProperties {
    private static final Logger LOG = LogManager.getLogger(Chunks.class);

    /**
     * Holds all the chunks that were found, indexed by their MAPIProperty.
     * Normally a property will have zero chunks (fixed sized) or one chunk
     * (variable size), but in some cases (eg Unknown) you may get more.
     */
    private final Map<MAPIProperty, List<Chunk>> allChunks = new HashMap<>();

    /**
     * Holds all the unknown properties that were found, indexed by their property id and property type.
     * All unknown properties have a custom properties instance.
     */
    private final Map<Long, MAPIProperty> unknownProperties = new HashMap<>();

    /** Type of message that the MSG represents (ie. IPM.Note) */
    private StringChunk messageClass;
    /** BODY Chunk, for plain/text messages */
    private StringChunk textBodyChunk;
    /** BODY Html Chunk, for html messages */
    private StringChunk htmlBodyChunkString;
    private ByteChunk htmlBodyChunkBinary;
    /** BODY Rtf Chunk, for Rtf (Rich) messages */
    private ByteChunk rtfBodyChunk;
    /** Subject link chunk, in plain/text */
    private StringChunk subjectChunk;
    /**
     * Value that is in the TO field (not actually the addresses as they are
     * stored in recip directory nodes
     */
    private StringChunk displayToChunk;
    /** Value that is in the FROM field */
    private StringChunk displayFromChunk;
    /** value that shows in the CC field */
    private StringChunk displayCCChunk;
    /** Value that shows in the BCC field */
    private StringChunk displayBCCChunk;
    /** Sort of like the subject line, but without the RE: and FWD: parts. */
    private StringChunk conversationTopic;
    /** Type of server that the message originated from (SMTP, etc). */
    private StringChunk sentByServerType;
    /** The email headers */
    private StringChunk messageHeaders;
    /** TODO */
    private MessageSubmissionChunk submissionChunk;
    /** TODO */
    private StringChunk emailFromChunk;
    /** The message ID */
    private StringChunk messageId;
    /** The message properties */
    private MessagePropertiesChunk messageProperties;

    @Override
    public Map<MAPIProperty, List<PropertyValue>> getProperties() {
        if (messageProperties != null) {
            return messageProperties.getProperties();
        } else {
            return Collections.emptyMap();
        }
    }

    public Map<MAPIProperty, PropertyValue> getRawProperties() {
        if (messageProperties != null) {
            return messageProperties.getRawProperties();
        } else {
            return Collections.emptyMap();
        }
    }

    public Map<MAPIProperty, List<Chunk>> getAll() {
        return allChunks;
    }

    @Override
    public Chunk[] getChunks() {
        ArrayList<Chunk> chunks = new ArrayList<>(allChunks.size());
        for (List<Chunk> c : allChunks.values()) {
            chunks.addAll(c);
        }
        return chunks.toArray(new Chunk[0]);
    }

    public StringChunk getMessageClass() {
        return messageClass;
    }

    public StringChunk getTextBodyChunk() {
        return textBodyChunk;
    }

    public StringChunk getHtmlBodyChunkString() {
        return htmlBodyChunkString;
    }

    public ByteChunk getHtmlBodyChunkBinary() {
        return htmlBodyChunkBinary;
    }

    public ByteChunk getRtfBodyChunk() {
        return rtfBodyChunk;
    }

    public StringChunk getSubjectChunk() {
        return subjectChunk;
    }

    public StringChunk getDisplayToChunk() {
        return displayToChunk;
    }

    public StringChunk getDisplayFromChunk() {
        return displayFromChunk;
    }

    public StringChunk getDisplayCCChunk() {
        return displayCCChunk;
    }

    public StringChunk getDisplayBCCChunk() {
        return displayBCCChunk;
    }

    public StringChunk getConversationTopic() {
        return conversationTopic;
    }

    public StringChunk getSentByServerType() {
        return sentByServerType;
    }

    public StringChunk getMessageHeaders() {
        return messageHeaders;
    }

    public MessageSubmissionChunk getSubmissionChunk() {
        return submissionChunk;
    }

    public StringChunk getEmailFromChunk() {
        return emailFromChunk;
    }

    public StringChunk getMessageId() {
        return messageId;
    }

    public MessagePropertiesChunk getMessageProperties() {
        return messageProperties;
    }

    /**
     * Called by the parser whenever a chunk is found.
     */
    @Override
    public void record(Chunk chunk) {
        // Work out what MAPIProperty this corresponds to
        MAPIProperty prop = MAPIProperty.get(chunk.getChunkId());
        if (prop == MAPIProperty.UNKNOWN) {
            long id = (chunk.getChunkId() << 16) + (long)chunk.getType().getId();
            prop = unknownProperties.get(id);
            if (prop == null) {
                prop = MAPIProperty.createCustom(chunk.getChunkId(), chunk.getType(), chunk.getEntryName());
                unknownProperties.put(id, prop);
            }
        }

        // Assign it for easy lookup, as best we can
        if (prop == MAPIProperty.MESSAGE_CLASS) {
            messageClass = (StringChunk) chunk;
        } else if (prop == MAPIProperty.INTERNET_MESSAGE_ID) {
            messageId = (StringChunk) chunk;
        } else if (prop == MAPIProperty.MESSAGE_SUBMISSION_ID) {
            // TODO - parse
            submissionChunk = (MessageSubmissionChunk) chunk;
        } else if (prop == MAPIProperty.RECEIVED_BY_ADDRTYPE) {
            sentByServerType = (StringChunk) chunk;
        } else if (prop == MAPIProperty.TRANSPORT_MESSAGE_HEADERS) {
            messageHeaders = (StringChunk) chunk;
        }

        else if (prop == MAPIProperty.CONVERSATION_TOPIC) {
            conversationTopic = (StringChunk) chunk;
        } else if (prop == MAPIProperty.SUBJECT) {
            subjectChunk = (StringChunk) chunk;
        } /*else if (prop == MAPIProperty.ORIGINAL_SUBJECT) {
            // TODO
        }*/

        else if (prop == MAPIProperty.DISPLAY_TO) {
            displayToChunk = (StringChunk) chunk;
        } else if (prop == MAPIProperty.DISPLAY_CC) {
            displayCCChunk = (StringChunk) chunk;
        } else if (prop == MAPIProperty.DISPLAY_BCC) {
            displayBCCChunk = (StringChunk) chunk;
        }

        else if (prop == MAPIProperty.SENDER_EMAIL_ADDRESS) {
            emailFromChunk = (StringChunk) chunk;
        } else if (prop == MAPIProperty.SENDER_NAME) {
            displayFromChunk = (StringChunk) chunk;
        } else if (prop == MAPIProperty.BODY) {
            textBodyChunk = (StringChunk) chunk;
        } else if (prop == MAPIProperty.BODY_HTML) {
            if (chunk instanceof StringChunk) {
                htmlBodyChunkString = (StringChunk) chunk;
            }
            if (chunk instanceof ByteChunk) {
                htmlBodyChunkBinary = (ByteChunk) chunk;
            }
        } else if (prop == MAPIProperty.RTF_COMPRESSED) {
            rtfBodyChunk = (ByteChunk) chunk;
        } else if (chunk instanceof MessagePropertiesChunk) {
            messageProperties = (MessagePropertiesChunk) chunk;
        }

        // And add to the main list
        allChunks.computeIfAbsent(prop, k -> new ArrayList<>());
        allChunks.get(prop).add(chunk);
    }

    @Override
    public void chunksComplete() {
        if (messageProperties != null) {
            messageProperties.matchVariableSizedPropertiesToChunks();
        } else {
            LOG.atWarn().log("Message didn't contain a root list of properties!");
        }
    }
}
