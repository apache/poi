<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method = "html" encoding="Windows-1252" />
	
	<xsl:template match="status">	
			 <h1>Project Status</h1>	

		 	
			 <h2>Developers</h2>			 	
			 <table>
			 <xsl:for-each select = "developers/person">
			 <tr><td><xsl:value-of select="@name" /></td>
			     <td><xsl:value-of select="@email" /></td>
			     <td>[<xsl:value-of select="@id" />]</td></tr>			 
			 </xsl:for-each>
			 </table>


			 <h2>To do</h2>			 	
			 <xsl:for-each select = "todo/actions">
			 <h3><xsl:value-of select = "@priority"/>&#160;priority</h3>
			 <table>
			 <tr><th>context</th><th>what</th><th>assigned to</th></tr>
			 <xsl:for-each select = "action">
			 <tr><td><xsl:value-of select="@context" /></td>
			     <td><xsl:value-of select="." /></td>
			     <td>[<xsl:value-of select="@assigned-to" />]</td></tr>			 
			 </xsl:for-each>
			 </table>
			 </xsl:for-each>

			 <h2>Changes</h2>			 	
			 <xsl:for-each select = "changes/release">
			 <h3>release&#160;<xsl:value-of select = "@version"/>&#160;
			     of date&#160;<xsl:value-of select = "@date"/></h3>
			 <table>
			 <tr><th>type</th><th>what</th><th>developer</th></tr>
			 <xsl:for-each select = "action">
			 <tr><td><xsl:value-of select="@type" /></td>
			     <td><xsl:value-of select="." /></td>
			     <td>[<xsl:value-of select="@dev" />]</td></tr>			 
			 </xsl:for-each>
			 </table>
			 </xsl:for-each>	
	
		</xsl:template>
</xsl:stylesheet>