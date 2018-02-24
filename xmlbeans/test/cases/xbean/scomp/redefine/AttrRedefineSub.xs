<xs:schema
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
   xmlns="http://xbean/scomp/redefine/AttrGroupRedefine"
    targetNamespace="http://xbean/scomp/redefine/AttrGroupRedefine"
    >

  <xs:redefine schemaLocation="BaseAttrGroup.xs">
   <xs:attributeGroup name="AttrGroup">
       <xs:attribute name="attr1" type="xs:string"/>
   </xs:attributeGroup>
  </xs:redefine>
</xs:schema>