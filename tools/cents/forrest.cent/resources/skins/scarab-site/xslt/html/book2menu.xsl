<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:NetUtils="org.apache.cocoon.util.NetUtils"
                version="1.0">

  <xsl:param name="resource"/>

  <xsl:template match="book">
    <menu>
     <div id="navcolumn">
      <xsl:apply-templates/>
     </div> 
  <div id="helptext">
		 <table border="0" cellspacing="0" cellpadding="3" width="100%">
		 <tr>
		 <th>How do I...?</th>
		 </tr>
		 <tr>
		 <td>
		  <div>Learn more about this project? </div> 
		  <div>Print this page...</div>
		  <div>Troubleshoot...</div>
		</td>
		</tr>
		</table>
   </div>     
    </menu>
  </xsl:template>

  <xsl:template match="project">
  </xsl:template>

  <xsl:template match="menu[position()=1]">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="menu">
     <!-- Encode label to escape any reserved characters such as space -->
     <xsl:variable name="encLabel" select="NetUtils:encodePath(@label)"/>
    <div>
      <small><strong><xsl:value-of select="@label"/></strong></small>
     <xsl:apply-templates/>
     </div> 
  </xsl:template>


  <xsl:template match="menu-item">
    <xsl:if test="not(@type) or @type!='hidden'">
     <xsl:variable name="encLabel" select="NetUtils:encodePath(@label)"/>
       <xsl:choose>
         <xsl:when test="@href=$resource"><!-- selected  -->
           <div><small><xsl:value-of select="@label"/></small></div>
         </xsl:when>
         <xsl:otherwise>
           <div><small><a href="{@href}"><xsl:value-of select="@label"/></a></small></div>
         </xsl:otherwise>
       </xsl:choose>
    </xsl:if>
  </xsl:template>

  <xsl:template match="external">
    <xsl:if test="not(@type) or @type!='hidden'">
     <xsl:variable name="encLabel" select="NetUtils:encodePath(@label)"/>
      <div><small><a href="{@href}" target="new" ><xsl:value-of select="@label"/></a></small></div>
   </xsl:if>
  </xsl:template>

  <xsl:template match="node()|@*" priority="-1"/>
</xsl:stylesheet>

