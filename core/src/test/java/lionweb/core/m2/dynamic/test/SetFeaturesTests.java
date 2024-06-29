// Copyright 2024 TRUMPF Laser SE and other contributors
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//     http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// 
// SPDX-FileCopyrightText: 2024 TRUMPF Laser SE and other contributors
// SPDX-License-Identifier: Apache-2.0

package lionweb.core.m2.dynamic.test;

import com.google.common.collect.Lists;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.impl.AbstractClassifierInstance;
import lionweb.utils.tests.CollectionAssert;
import org.junit.Test;

import java.util.Collections;

public class SetFeaturesTests extends DynamicNodeTestsBase
{
////    #region property
//
////    #region string
//
//    @Test
//    public void String_Init()
//    {
//        AbstractClassifierInstance parent = newDocumentation("od");
//        CollectionAssert.AreEqual(Collections.emptyList(), parent.CollectAllSetFeatures().ToList());
//    }
//
//    @Test
//    public void String_Set_Reflective()
//    {
//        AbstractClassifierInstance parent = newDocumentation("od");
//        parent.setPropertyValue(Documentation_text(), "hello");
//        CollectionAssert.AreEqual(Lists.newArrayList(Documentation_text()),
//            parent.CollectAllSetFeatures().ToList());
//    }
//
//    @Test
//    public void String_Unset_Reflective()
//    {
//        AbstractClassifierInstance parent = newDocumentation("od");
//        parent.setPropertyValue(Documentation_text(), "hello");
//        parent.setPropertyValue(Documentation_text(), null);
//        CollectionAssert.AreEqual(Collections.emptyList(), parent.CollectAllSetFeatures().ToList());
//    }
//
////    #endregion
//
////    #region integer
//
//    @Test
//    public void Integer_Init()
//    {
//        AbstractClassifierInstance parent = newCircle("od");
//        CollectionAssert.AreEqual(Collections.emptyList(), parent.CollectAllSetFeatures().ToList());
//    }
//
//    @Test
//    public void Integer_Set_Reflective()
//    {
//        AbstractClassifierInstance parent = newCircle("od");
//        parent.setPropertyValue(Circle_r(), 10);
//        CollectionAssert.AreEqual(Lists.newArrayList(Circle_r()),
//            parent.CollectAllSetFeatures().ToList());
//    }
//
////    #endregion
//
////    #region boolean
//
//    @Test
//    public void Boolean_Init()
//    {
//        AbstractClassifierInstance parent = newDocumentation("od");
//        CollectionAssert.AreEqual(Collections.emptyList(), parent.CollectAllSetFeatures().ToList());
//    }
//
//    @Test
//    public void Boolean_Set_Reflective()
//    {
//        AbstractClassifierInstance parent = newDocumentation("od");
//        parent.setPropertyValue(Documentation_technical(), true);
//        CollectionAssert.AreEqual(Lists.newArrayList(Documentation_technical()),
//            parent.CollectAllSetFeatures().ToList());
//    }
//
//    @Test
//    public void Boolean_Unset_Reflective()
//    {
//        AbstractClassifierInstance parent = newDocumentation("od");
//        parent.setPropertyValue(Documentation_technical(), true);
//        parent.setPropertyValue(Documentation_technical(), null);
//        CollectionAssert.AreEqual(Collections.emptyList(), parent.CollectAllSetFeatures().ToList());
//    }
//
////    #endregion
//
////    #region enum
//
//    @Test
//    public void Enum_Init()
//    {
//        AbstractClassifierInstance parent = newMaterialGroup("od");
//        CollectionAssert.AreEqual(Collections.emptyList(), parent.CollectAllSetFeatures().ToList());
//    }
//
//    @Test
//    public void Enum_Set_Reflective()
//    {
//        AbstractClassifierInstance parent = newMaterialGroup("od");
//        parent.setPropertyValue(MaterialGroup_matterState(), MatterState_Gas());
//        CollectionAssert.AreEqual(Lists.newArrayList( MaterialGroup_matterState() ),
//            parent.CollectAllSetFeatures().ToList());
//    }
//
//    @Test
//    public void Enum_Unset_Reflective()
//    {
//        AbstractClassifierInstance parent = newMaterialGroup("od");
//        parent.setPropertyValue(MaterialGroup_matterState(), MatterState_Gas());
//        parent.setPropertyValue(MaterialGroup_matterState(), null);
//        CollectionAssert.AreEqual(Collections.emptyList(), parent.CollectAllSetFeatures().ToList());
//    }
//
////    #endregion
//
////    #endregion
//
////    #region containment
//
////    #region single
//
////    #region optional
//
//    @Test
//    public void ContainmentSingleOptional_Init()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        CollectionAssert.AreEqual(Collections.emptyList(), parent.CollectAllSetFeatures().ToList());
//    }
//
//    @Test
//    public void ContainmentSingleOptional_Set_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        Node doc = newDocumentation("myId");
//        parent.addChild(Geometry_documentation(), doc);
//        CollectionAssert.AreEqual(Lists.newArrayList( Geometry_documentation() ),
//            parent.CollectAllSetFeatures().ToList());
//    }
//
//    @Test
//    public void ContainmentSingleOptional_Unset_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        Node doc = newDocumentation("myId");
//        parent.addChild(Geometry_documentation(), doc);
//        parent.addChild(Geometry_documentation(), null);
//        CollectionAssert.AreEqual(Collections.emptyList(), parent.CollectAllSetFeatures().ToList());
//    }
//
////    #endregion
//
////    #region required
//
//    @Test
//    public void ContainmentSingleRequired_Init()
//    {
//        AbstractClassifierInstance parent = newOffsetDuplicate("g");
//        CollectionAssert.AreEqual(Collections.emptyList(), parent.CollectAllSetFeatures().ToList());
//    }
//
//    @Test
//    public void ContainmentSingleRequired_Set_Reflective()
//    {
//        AbstractClassifierInstance parent = newOffsetDuplicate("g");
//        Node coord = newCoord("myId");
//        parent.addChild(OffsetDuplicate_offset(), coord);
//        CollectionAssert.AreEqual(Lists.newArrayList( OffsetDuplicate_offset() ),
//            parent.CollectAllSetFeatures().ToList());
//    }
//
////    #endregion
//
////    #endregion
//
////    #region multiple
//
////    #region optional
//
//    @Test
//    public void ContainmentMultipleOptional_Init()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        CollectionAssert.AreEqual(Collections.emptyList(), parent.CollectAllSetFeatures().ToList());
//    }
//
//    @Test
//    public void ContainmentMultipleOptional_Set_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        parent.addChild(Geometry_shapes(), newCircle("myId"));
//        CollectionAssert.AreEqual(Lists.newArrayList( Geometry_shapes() ),
//            parent.CollectAllSetFeatures().ToList());
//    }
//
//    @Test
//    public void ContainmentMultipleOptional_Reset_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AbstractClassifierInstance value = newCircle("myId");
//        parent.addChild(Geometry_shapes(), value);
//        parent.addChild(Geometry_shapes(), null);
//        CollectionAssert.AreEqual(Collections.emptyList(),
//            parent.CollectAllSetFeatures().ToList());
//    }
//
//    @Test
//    public void ContainmentMultipleOptional_Overwrite_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AbstractClassifierInstance value = newCircle("myA");
//        parent.addChild(Geometry_shapes(), value);
//        parent.addChild(Geometry_shapes(), newCircle("myB"));
//        CollectionAssert.AreEqual(Lists.newArrayList( Geometry_shapes() ),
//            parent.CollectAllSetFeatures().ToList());
//    }
//
////    #endregion
//
////    #region required
//
//    @Test
//    public void ContainmentMultipleRequired_Init()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        CollectionAssert.AreEqual(Collections.emptyList(), parent.CollectAllSetFeatures().ToList());
//    }
//
//    @Test
//    public void ContainmentMultipleRequired_Set_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        parent.addChild(Geometry_shapes(), newCircle("myId"));
//        CollectionAssert.AreEqual(Lists.newArrayList( Geometry_shapes() ),
//            parent.CollectAllSetFeatures().ToList());
//    }
//
//    @Test
//    public void ContainmentMultipleRequired_Overwrite_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        Node valueA = newCircle("myA");
//        parent.addChild(Geometry_shapes(), valueA );
//        Node valueB = newCircle("myB");
//        parent.addChild(Geometry_shapes(),  valueB);
//        CollectionAssert.AreEqual(Lists.newArrayList( Geometry_shapes() ),
//            parent.CollectAllSetFeatures().ToList());
//    }
//
////    #endregion
//
////    #endregion
//
////    #endregion
//
////    #region reference
//
////    #region single
//
////    #region optional
//
//    @Test
//    public void ReferenceSingleOptional_Init()
//    {
//        AbstractClassifierInstance parent = newOffsetDuplicate("g");
//        CollectionAssert.AreEqual(Collections.emptyList(), parent.CollectAllSetFeatures().ToList());
//    }
//
//    @Test
//    public void ReferenceSingleOptional_Set_Reflective()
//    {
//        AbstractClassifierInstance parent = newOffsetDuplicate("g");
//        parent.addReferenceValue(OffsetDuplicate_altSource(), newLine("myId"));
//        CollectionAssert.AreEqual(Lists.newArrayList( OffsetDuplicate_altSource() ),
//            parent.CollectAllSetFeatures().ToList());
//    }
//
//    @Test
//    public void ReferenceSingleOptional_Unset_Reflective()
//    {
//        AbstractClassifierInstance parent = newOffsetDuplicate("g");
//        parent.addReferenceValue(OffsetDuplicate_altSource(), newLine("myId"));
//        parent.addReferenceValue(OffsetDuplicate_altSource(), null);
//        CollectionAssert.AreEqual(Collections.emptyList(), parent.CollectAllSetFeatures().ToList());
//    }
//
////    #endregion
//
////    #region required
//
//    @Test
//    public void ReferenceSingleRequired_Init()
//    {
//        AbstractClassifierInstance parent = newOffsetDuplicate("g");
//        CollectionAssert.AreEqual(Collections.emptyList(), parent.CollectAllSetFeatures().ToList());
//    }
//
//    @Test
//    public void ReferenceSingleRequired_Set_Reflective()
//    {
//        AbstractClassifierInstance parent = newOffsetDuplicate("g");
//        parent.addReferenceValue(OffsetDuplicate_source(), newLine("myId"));
//        CollectionAssert.AreEqual(Lists.newArrayList( OffsetDuplicate_source() ),
//            parent.CollectAllSetFeatures().ToList());
//    }
//
////    #endregion
//
////    #endregion
//
////    #region multiple
//
////    #region optional
//
//    @Test
//    public void ReferenceMultipleOptional_Init()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        CollectionAssert.AreEqual(Collections.emptyList(), parent.CollectAllSetFeatures().ToList());
//    }
//
//    @Test
//    public void ReferenceMultipleOptional_Set_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        parent.addReferenceValue(ReferenceGeometry_shapes(), newCircle("myId") );
//        CollectionAssert.AreEqual(Lists.newArrayList( ReferenceGeometry_shapes() ),
//            parent.CollectAllSetFeatures().ToList());
//    }
//
//    @Test
//    public void ReferenceMultipleOptional_Reset_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance value = newCircle("myId");
//        parent.addReferenceValue(ReferenceGeometry_shapes(), value);
//        parent.addReferenceValue(ReferenceGeometry_shapes(), null);
//        CollectionAssert.AreEqual(Collections.emptyList(),
//            parent.CollectAllSetFeatures().ToList());
//    }
//
//    @Test
//    public void ReferenceMultipleOptional_Overwrite_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance value = newCircle("myA");
//        parent.addReferenceValue(ReferenceGeometry_shapes(),value);
//        parent.addReferenceValue(ReferenceGeometry_shapes(), newCircle("myB"));
//        CollectionAssert.AreEqual(Lists.newArrayList( ReferenceGeometry_shapes()),
//            parent.CollectAllSetFeatures().ToList());
//    }
//
////    #endregion
//
////    #region required
//
//    @Test
//    public void ReferenceMultipleRequired_Init()
//    {
//        AbstractClassifierInstance parent = newMaterialGroup("g");
//        CollectionAssert.AreEqual(Collections.emptyList(), parent.CollectAllSetFeatures().ToList());
//    }
//
//    @Test
//    public void ReferenceMultipleRequired_Set_Reflective()
//    {
//        AbstractClassifierInstance parent = newMaterialGroup("g");
//        parent.addReferenceValue(MaterialGroup_materials(), newCircle("myId") );
//        CollectionAssert.AreEqual(Lists.newArrayList( MaterialGroup_materials() ),
//            parent.CollectAllSetFeatures().ToList());
//    }
//
//    @Test
//    public void ReferenceMultipleRequired_Overwrite_Reflective()
//    {
//        AbstractClassifierInstance parent = newMaterialGroup("g");
//        AbstractClassifierInstance valueA = newCircle("myA");
//        parent.addReferenceValue(MaterialGroup_materials(), valueA );
//        AbstractClassifierInstance valueB = newCircle("myB");
//        parent.addReferenceValue(MaterialGroup_materials(),  valueB );
//        CollectionAssert.AreEqual(Lists.newArrayList( MaterialGroup_materials() ),
//            parent.CollectAllSetFeatures().ToList());
//    }
//
////    #endregion
//
////    #endregion
//
////    #endregion
}