<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- @author <a href="mailto:barozzi@nicolaken.com">Nicola Ken Barozzi</a> -->

  <xsl:output method="text"/>

  <xsl:template match="/">-----------------------------------------------------------
         This mail is generated automatically using
         Jakarta Ant. Contents are automatically
         downloaded from Apache's Bugzilla.
-----------------------------------------------------------
         Please do not reply to this mail.
-----------------------------------------------------------

***********************************************************
            COCOON PATCH QUEUE UPDATE
 
            patches in queue:  <xsl:value-of select="count(patch-queue/bug)"/> 
***********************************************************

    <xsl:for-each select="patch-queue/bug">
-----------------------------------------------------------
<xsl:value-of select="@id"/>:<xsl:value-of select="@summary"/>
-----------------------------------------------------------
<xsl:value-of select="@url"/>

REVIEWER:    <xsl:value-of select="@owner"/>
RESOLUTION:  <xsl:value-of select="@resolution"/>
STATUS:      <xsl:value-of select="@status"/>

    </xsl:for-each>

*************************that's it!************************

------------------------patch HOWTO------------------------

Send patches to http://nagoya.apache.org/bugzilla/
specifying [PATCH] in the summary.
Bugzilla sends a mail automatically to this list.
Reviewers will mark it FIXED there when applied.
Patches not sent to Bugzilla will not be reviewed.
-----------------------------------------------------------
This file is generated and updated automatically at least
once a week, and the data is taken from Bugzilla.
If you don't find the patch you submitted to bugzilla
after one week, please notify cocoon-dev@xml.apache.org
for assistance.
-----------------------------------------------------------
There is usually a HEAD branch and a previous-version
branch that are maintained. Where will the patch go?
1. If it is a bug fix it should go to both branches
2. If something is totally new it goes into HEAD scratchpad.
3. Something in between, but does not break backward
   compatibility _may_ go into both (and may not)
4. For everything else, a vote is required  so
   first it may go into HEAD, and then be VOTEd in order
   to sync this into branch.
Please note that structural changes have to be VOTEd first.
  </xsl:template>

  <xsl:template match="@*|*|text()|processing-instruction()">
      <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
  </xsl:template>

</xsl:stylesheet>
