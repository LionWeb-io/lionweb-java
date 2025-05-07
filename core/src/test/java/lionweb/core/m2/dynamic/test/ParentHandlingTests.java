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
import org.junit.Assert;
import org.junit.Test;

public class ParentHandlingTests extends DynamicNodeTestsBase
{
//    #region single

//    #region optional

//    #region SameInOtherInstance

//    @Test
//    public void SingleOptional_SameInOtherInstance_Reflective()
//    {
//        Node child = newDocumentation("myId");
//        AbstractClassifierInstance source = newGeometry("src");
//        source.addChild(Geometry_documentation(), child);
//        AbstractClassifierInstance target = newGeometry("tgt");
//
//        target.addChild(Geometry_documentation(), child);
//
//        Assert.assertSame(target, child.getParent());
//        Assert.assertSame(child, target.getChildren(Geometry_documentation()));
//        Assert.assertNull(source.getChildren(Geometry_documentation()));
//    }

//    @Test
//    public void SingleOptional_SameInOtherInstance_detach_Reflective()
//    {
//        Node child = newDocumentation("myId");
//        AbstractClassifierInstance source = newGeometry("src");
//        source.addChild(Geometry_documentation(), child);
//        Node orphan = newDocumentation("o");
//        AbstractClassifierInstance target = newGeometry("tgt");
//        target.addChild(Geometry_documentation(), orphan);
//
//        target.addChild(Geometry_documentation(), child);
//
//        Assert.assertSame(target, child.getParent());
//        Assert.assertSame(child, target.getChildren(Geometry_documentation()));
//        Assert.assertNull(source.getChildren(Geometry_documentation()));
//        Assert.assertNull(orphan.getParent());
//    }

//    #endregion

//    #region other

//    @Test
//    public void SingleOptional_Other_Reflective()
//    {
//        Node child = newDocumentation("myId");
//        AbstractClassifierInstance source = newGeometry("src");
//        source.addChild(Geometry_documentation(), child);
//        AbstractClassifierInstance target = newOffsetDuplicate("tgt");
//
//        target.addChild(OffsetDuplicate_docs(), child);
//
//        Assert.assertSame(target, child.getParent());
//        Assert.assertSame(child, target.getChildren(OffsetDuplicate_docs()));
//        Assert.assertNull(source.getChildren(Geometry_documentation()));
//    }

//    @Test
//    public void SingleOptional_Other_detach_Reflective()
//    {
//        Node child = newDocumentation("myId");
//        AbstractClassifierInstance source = newGeometry("src");
//        source.addChild(Geometry_documentation(), child);
//        Node orphan = newDocumentation("o");
//        AbstractClassifierInstance target = newOffsetDuplicate("tgt");
//        target.addChild(OffsetDuplicate_docs(), orphan);
//
//        target.addChild(OffsetDuplicate_docs(), child);
//
//        Assert.assertSame(target, child.getParent());
//        Assert.assertSame(child, target.getChildren(OffsetDuplicate_docs()));
//        Assert.assertNull(source.getChildren(Geometry_documentation()));
//        Assert.assertNull(orphan.getParent());
//    }

//    #endregion

//    #region otherInSameInstance

//    @Test
//    public void SingleOptional_OtherInSameInstance_Reflective()
//    {
//        Node child = newDocumentation("myId");
//        AbstractClassifierInstance parent = newOffsetDuplicate("src");
//        parent.addChild(OffsetDuplicate_docs(), child);
//
//        parent.addChild(OffsetDuplicate_secretDocs(), child);
//
//        Assert.assertSame(parent, child.getParent());
//        Assert.assertSame(child, parent.getChildren(OffsetDuplicate_secretDocs()));
//        Assert.assertNull(parent.getChildren(OffsetDuplicate_docs()));
//    }

//    @Test
//    public void SingleOptional_OtherInSameInstance_detach_Reflective()
//    {
//        Node child = newDocumentation("myId");
//        Node orphan = newDocumentation("o");
//        AbstractClassifierInstance parent = newOffsetDuplicate("src");
//        parent.addChild(OffsetDuplicate_docs(), child);
//        parent.addChild(OffsetDuplicate_secretDocs(), orphan);
//
//        parent.addChild(OffsetDuplicate_secretDocs(), child);
//
//        Assert.assertSame(parent, child.getParent());
//        Assert.assertSame(child, parent.getChildren(OffsetDuplicate_secretDocs()));
//        Assert.assertNull(parent.getChildren(OffsetDuplicate_docs()));
//        Assert.assertNull(orphan.getParent());
//    }

//    #endregion

//    #region sameInSameInstance

//    @Test
//    public void SingleOptional_SameInSameInstance_Reflective()
//    {
//        Node child = newDocumentation("myId");
//        AbstractClassifierInstance parent = newOffsetDuplicate("src");
//        parent.addChild(OffsetDuplicate_docs(), child);
//
//        parent.addChild(OffsetDuplicate_docs(), child);
//
//        Assert.assertSame(parent, child.getParent());
//        Assert.assertSame(child, parent.getChildren(OffsetDuplicate_docs()));
//    }

//    #endregion

//    #region annotation

//    @Test
//    public void SingleOptional_ToAnnotation()
//    {
//        Node child = newDocumentation("myId");
//        AbstractClassifierInstance source = newGeometry("src");
//        source.addChild(Geometry_documentation(), child);
//        AbstractClassifierInstance target = newLine("tgt");
//
//        target.addAnnotation(child);
//
//        Assert.assertSame(target, child.getParent());
//        Assert.assertTrue(target.getAnnotations().contains(child));
//        Assert.assertFalse(source.getAnnotations().contains(child));
//    }

//    @Test
//    public void SingleOptional_ToAnnotation_Reflective()
//    {
//        Node child = newDocumentation("myId");
//        AbstractClassifierInstance source = newGeometry("src");
//        source.addChild(Geometry_documentation(), child);
//        AbstractClassifierInstance target = newLine("tgt");
//
//        target.addChild(null, child);
//
//        Assert.assertSame(target, child.getParent());
//        Assert.assertTrue(target.getAnnotations().contains(child));
//        Assert.assertFalse(source.getAnnotations().contains(child));
//    }

//    @Test
//    public void Annotation_ToSingleOptional_Reflective()
//    {
//        Node child = newDocumentation("myId");
//        AbstractClassifierInstance source = newLine("src");
//        source.addAnnotation(child);
//        AbstractClassifierInstance target = newGeometry("tgt");
//
//        target.addChild(Geometry_documentation(), child);
//
//        Assert.assertSame(target, child.getParent());
//        Assert.assertFalse(source.getAnnotations().contains(child));
//        Assert.assertSame(child, target.getChildren(Geometry_documentation()));
//    }

//    @Test
//    public void Annotation_ToSingleOptional_detach_Reflective()
//    {
//        Node child = newDocumentation("myId");
//        Node orphan = newDocumentation("o");
//        AbstractClassifierInstance source = newLine("src");
//        source.addAnnotation(child);
//        AbstractClassifierInstance target = newGeometry("tgt");
//        target.addChild(Geometry_documentation(), orphan);
//
//        target.addChild(Geometry_documentation(), child);
//
//        Assert.assertSame(target, child.getParent());
//        Assert.assertFalse(source.getAnnotations().contains(child));
//        Assert.assertSame(child, target.getChildren(Geometry_documentation()));
//        Assert.assertNull(orphan.getParent());
//    }

//    #region sameInstance

//    @Test
//    public void SingleOptional_ToAnnotation_SameInstance()
//    {
//        Node child = newDocumentation("myId");
//        AbstractClassifierInstance parent = newOffsetDuplicate("src");
//        parent.addChild(OffsetDuplicate_docs(), child);
//
//        parent.addAnnotation(child);
//
//        Assert.assertSame(parent, child.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(child));
//        Assert.assertNull(parent.getChildren(OffsetDuplicate_docs()));
//    }

//    @Test
//    public void SingleOptional_ToAnnotation_SameInstance_Reflective()
//    {
//        Node child = newDocumentation("myId");
//        AbstractClassifierInstance parent = newOffsetDuplicate("src");
//        parent.addChild(OffsetDuplicate_docs(), child);
//
//        parent.addChild(null, child);
//
//        Assert.assertSame(parent, child.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(child));
//        Assert.assertNull(parent.getChildren(OffsetDuplicate_docs()));
//    }

//    @Test
//    public void Annotation_ToSingleOptional_SameInstance_Reflective()
//    {
//        Node child = newDocumentation("myId");
//        AbstractClassifierInstance parent = newOffsetDuplicate("src");
//        parent.addAnnotation(child);
//
//        parent.addChild(OffsetDuplicate_docs(), child);
//
//        Assert.assertSame(parent, child.getParent());
//        Assert.assertFalse(parent.getAnnotations().contains(child));
//        Assert.assertSame(child, parent.getChildren(OffsetDuplicate_docs()));
//    }

//    @Test
//    public void Annotation_ToSingleOptional_SameInstance_detach_Reflective()
//    {
//        Node child = newDocumentation("myId");
//        Node orphan = newDocumentation("o");
//        AbstractClassifierInstance parent = newOffsetDuplicate("src");
//        parent.addChild(OffsetDuplicate_docs(), orphan);
//        parent.addAnnotation(child);
//
//        parent.addChild(OffsetDuplicate_docs(), child);
//
//        Assert.assertSame(parent, child.getParent());
//        Assert.assertFalse(parent.getAnnotations().contains(child));
//        Assert.assertSame(child, parent.getChildren(OffsetDuplicate_docs()));
//        Assert.assertNull(orphan.getParent());
//    }

//    #endregion

//    #endregion

//    #endregion

//    #region required

//    #region SameInOtherInstance

    @Test
    public void SingleRequired_SameInOtherInstance_Reflective()
    {
        Node child = newCoord("myId");
        AbstractClassifierInstance source = newOffsetDuplicate("src");
        source.addChild(OffsetDuplicate_offset(), child);
        AbstractClassifierInstance target = newOffsetDuplicate("tgt");

        target.addChild(OffsetDuplicate_offset(), child);

        Assert.assertSame(target, child.getParent());
        Assert.assertSame(child, target.getChildren(OffsetDuplicate_offset()));
        Assert.assertThrows(IllegalArgumentException.class, () -> source.getChildren(OffsetDuplicate_offset()));
    }

    @Test
    public void SingleRequired_SameInOtherInstance_detach_Reflective()
    {
        Node child = newCoord("myId");
        Node orphan = newCoord("o");
        AbstractClassifierInstance source = newOffsetDuplicate("src");
        source.addChild(OffsetDuplicate_offset(), child);
        AbstractClassifierInstance target = newOffsetDuplicate("tgt");
        target.addChild(OffsetDuplicate_offset(), orphan);

        target.addChild(OffsetDuplicate_offset(), child);

        Assert.assertSame(target, child.getParent());
        Assert.assertSame(child, target.getChildren(OffsetDuplicate_offset()));
        Assert.assertThrows(IllegalArgumentException.class, () -> source.getChildren(OffsetDuplicate_offset()));
        Assert.assertNull(orphan.getParent());
    }

//    #endregion

//    #region other

    @Test
    public void SingleRequired_Other_Reflective()
    {
        Node child = newCoord("myId");
        AbstractClassifierInstance source = newOffsetDuplicate("src");
        source.addChild(OffsetDuplicate_offset(), child);
        AbstractClassifierInstance target = newCircle("tgt");

        target.addChild(Circle_center(), child);

        Assert.assertSame(target, child.getParent());
        Assert.assertSame(child, target.getChildren(Circle_center()));
        Assert.assertThrows(IllegalArgumentException.class, () -> source.getChildren(OffsetDuplicate_offset()));
    }

    @Test
    public void SingleRequired_Other_detach_Reflective()
    {
        Node child = newCoord("myId");
        Node orphan = newCoord("o");
        AbstractClassifierInstance source = newOffsetDuplicate("src");
        source.addChild(OffsetDuplicate_offset(), child);
        AbstractClassifierInstance target = newCircle("tgt");
        target.addChild(Circle_center(), orphan);

        target.addChild(Circle_center(), child);

        Assert.assertSame(target, child.getParent());
        Assert.assertSame(child, target.getChildren(Circle_center()));
        Assert.assertThrows(IllegalArgumentException.class, () -> source.getChildren(OffsetDuplicate_offset()));
        Assert.assertNull(orphan.getParent());
    }

//    #endregion

//    #region SameInSameInstance

    @Test
    public void SingleRequired_SameInSameInstance_Reflective()
    {
        Node child = newCoord("myId");
        AbstractClassifierInstance parent = newLine("src");
        parent.addChild(Line_start(), child);

        parent.addChild(Line_start(), child);

        Assert.assertSame(parent, child.getParent());
        Assert.assertSame(child, parent.getChildren(Line_start()));
    }

//    #endregion

//    #region OtherInSameInstance

    @Test
    public void SingleRequired_OtherInSameInstance_Reflective()
    {
        Node child = newCoord("myId");
        AbstractClassifierInstance parent = newLine("src");
        parent.addChild(Line_start(), child);

        parent.addChild(Line_end(), child);

        Assert.assertSame(parent, child.getParent());
        Assert.assertSame(child, parent.getChildren(Line_end()));
        Assert.assertThrows(IllegalArgumentException.class, () -> parent.getChildren(Line_start()));
    }

    @Test
    public void SingleRequired_OtherInSameInstance_detach_Reflective()
    {
        Node child = newCoord("myId");
        Node orphan = newCoord("o");
        AbstractClassifierInstance parent = newLine("src");
        parent.addChild(Line_start(), child);
        parent.addChild(Line_end(), orphan);

        parent.addChild(Line_end(), child);

        Assert.assertSame(parent, child.getParent());
        Assert.assertSame(child, parent.getChildren(Line_end()));
        Assert.assertThrows(IllegalArgumentException.class, () -> parent.getChildren(Line_start()));
        Assert.assertNull(orphan.getParent());
    }

//    #endregion

//    #endregion

//    #endregion

//    #region multiple

//    #region optional

//    #region singleEntry

//    #region SameInOtherInstance

    @Test
    public void MultipleOptional_Single_SameInOtherInstance_Reflective()
    {
        Node child = newLine("myId");
        AbstractClassifierInstance source = newGeometry("src");
//        source.addChild(Geometry_shapes(), Lists.newArrayList( child });
        source.addChild(Geometry_shapes(), child);
        AbstractClassifierInstance target = newGeometry("tgt");

//        target.addChild(Geometry_shapes(), Lists.newArrayList( child });
        target.addChild(Geometry_shapes(), child);

        Assert.assertSame(target, child.getParent());
        CollectionAssert.AreEqual(Lists.newArrayList( child), target.getChildren(Geometry_shapes()));
        Assert.assertEquals(0, (source.getChildren(Geometry_shapes())).size());
    }

//    #endregion

//    #region Other

//    @Test
//    public void MultipleOptional_Single_Other_Reflective()
//    {
//        Node child = newLine("myId");
//        AbstractClassifierInstance source = newGeometry("src");
//        source.addChild(Geometry_shapes(), Lists.newArrayList( child });
//        AbstractClassifierInstance target = newCompositeShape("tgt");
//
//        target.addChild(CompositeShape_parts(), Lists.newArrayList( child });
//
//        Assert.assertSame(target, child.getParent());
//        CollectionAssert.AreEqual(Lists.newArrayList( child }, target.getChildren(CompositeShape_parts()));
//        Assert.assertEquals(0, (source.getChildren(Geometry_shapes())).size());
//    }

//    #endregion

//    #region OtherInSameInstance

    @Test
    public void MultipleOptional_Single_OtherInSameInstance_Reflective()
    {
        Node child = newMaterialGroup("myId");
        AbstractClassifierInstance parent = newBillOfMaterials("src");
        parent.addChild(BillOfMaterials_groups(),  child);

        parent.addChild(BillOfMaterials_altGroups(), child);

        Assert.assertSame(parent, child.getParent());
        CollectionAssert.AreEqual(Lists.newArrayList( child ), parent.getChildren(BillOfMaterials_altGroups()));
        Assert.assertEquals(0, (parent.getChildren(BillOfMaterials_groups())).size());
    }

//    #endregion

//    #region SameInSameInstance

    @Test
    public void MultipleOptional_Single_SameInSameInstance_Reflective()
    {
        Node child = newMaterialGroup("myId");
        AbstractClassifierInstance parent = newBillOfMaterials("src");
        parent.addChild(BillOfMaterials_groups(), child);

        parent.addChild(BillOfMaterials_groups(), child);

        Assert.assertSame(parent, child.getParent());
        CollectionAssert.AreEqual(Lists.newArrayList( child ), parent.getChildren(BillOfMaterials_groups()));
    }

//    #endregion

//    #endregion

//    #region someEntries

//    #region SameInOtherInstance

    @Test
    public void MultipleOptional_Partial_SameInOtherInstance_Reflective()
    {
        Node childA = newLine("a");
        Node childB = newLine("b");
        AbstractClassifierInstance source = newGeometry("src");
//        source.addChild(Geometry_shapes(), Lists.newArrayList( childA,childB });
        source.addChild(Geometry_shapes(), childA);
        source.addChild(Geometry_shapes(), childB);
        AbstractClassifierInstance target = newGeometry("tgt");

        target.addChild(Geometry_shapes(), childA);

        Assert.assertSame(target, childA.getParent());
        CollectionAssert.AreEqual(Lists.newArrayList( childA ), target.getChildren(Geometry_shapes()));
        CollectionAssert.AreEqual(Lists.newArrayList( childB ), source.getChildren(Geometry_shapes()));
    }

//    #endregion

//    #region Other

    @Test
    public void MultipleOptional_Partial_Other_Reflective()
    {
        Node childA = newLine("a");
        Node childB = newLine("b");
        AbstractClassifierInstance source = newGeometry("src");
//        source.addChild(Geometry_shapes(), Lists.newArrayList( childA,childB });
        source.addChild(Geometry_shapes(),childA);
        source.addChild(Geometry_shapes(),childB);
        AbstractClassifierInstance target = newCompositeShape("tgt");

        target.addChild(CompositeShape_parts(),  childA);

        Assert.assertSame(target, childA.getParent());
        CollectionAssert.AreEqual(Lists.newArrayList( childA ), target.getChildren(CompositeShape_parts()));
        CollectionAssert.AreEqual(Lists.newArrayList( childB ), source.getChildren(Geometry_shapes()));
    }

//    #endregion

//    #region OtherInSameInstance

    @Test
    public void MultipleOptional_Partial_OtherInSameInstance_Reflective()
    {
        Node childA = newMaterialGroup("a");
        Node childB = newMaterialGroup("b");
        AbstractClassifierInstance parent = newBillOfMaterials("src");
//        parent.addChild(BillOfMaterials_groups(), Lists.newArrayList( childA, childB });
        parent.addChild(BillOfMaterials_groups(), childA);
        parent.addChild(BillOfMaterials_groups(), childB);

        parent.addChild(BillOfMaterials_altGroups(), childA);

        Assert.assertSame(parent, childA.getParent());
        CollectionAssert.AreEqual(Lists.newArrayList( childA ), parent.getChildren(BillOfMaterials_altGroups()));
        CollectionAssert.AreEqual(Lists.newArrayList( childB ), parent.getChildren(BillOfMaterials_groups()));
    }

//    #endregion

//    #region SameInSameInstance

    @Test
    public void MultipleOptional_Partial_SameInSameInstance_Reflective()
    {
        Node childA = newMaterialGroup("myId");
        Node childB = newMaterialGroup("b");
        AbstractClassifierInstance parent = newBillOfMaterials("src");
//        parent.addChild(BillOfMaterials_groups(), Lists.newArrayList( childA, childA });
        parent.addChild(BillOfMaterials_groups(), childA);
        parent.addChild(BillOfMaterials_groups(), childA);

        parent.addChild(BillOfMaterials_groups(), childA);

        Assert.assertSame(parent, childA.getParent());
        CollectionAssert.AreEqual(Lists.newArrayList( childA ), parent.getChildren(BillOfMaterials_groups()));
    }

//    #endregion

//    #endregion

//    #region FromSingle

//    #region Other

    @Test
    public void MultipleOptional_FromSingle_Other_Reflective()
    {
        Node child = newLine("myId");
        AbstractClassifierInstance source = newMaterialGroup("src");
        source.addChild(MaterialGroup_defaultShape(), child);
        AbstractClassifierInstance target = newGeometry("tgt");

        target.addChild(Geometry_shapes(), child);

        Assert.assertSame(target, child.getParent());
        CollectionAssert.AreEqual(Lists.newArrayList( child ), target.getChildren(Geometry_shapes()));
        Assert.assertNull(source.getChildren(MaterialGroup_defaultShape()));
    }

//    #endregion

//    #region OtherInSameInstance

    @Test
    public void MultipleOptional_FromSingle_OtherInSameInstance_Reflective()
    {
        Node child = newMaterialGroup("myId");
        AbstractClassifierInstance parent = newBillOfMaterials("src");
        parent.addChild(BillOfMaterials_defaultGroup(), child);

        parent.addChild(BillOfMaterials_altGroups(), child);

        Assert.assertSame(parent, child.getParent());
        CollectionAssert.AreEqual(Lists.newArrayList( child ), parent.getChildren(BillOfMaterials_altGroups()));
        Assert.assertNull(parent.getChildren(BillOfMaterials_defaultGroup()));
    }

//    #endregion

//    #endregion

//    #region ToSingle

//    #region Other

    @Test
    public void MultipleOptional_ToSingle_Other_Reflective()
    {
        Node child = newLine("myId");
        AbstractClassifierInstance source = newGeometry("src");
        source.addChild(Geometry_shapes(), child);
        AbstractClassifierInstance target = newMaterialGroup("tgt");

        target.addChild(MaterialGroup_defaultShape(), child);

        Assert.assertSame(target, child.getParent());
        Assert.assertSame(child, target.getChildren(MaterialGroup_defaultShape()));
        Assert.assertEquals(0, (source.getChildren(Geometry_shapes())).size());
    }

//    #endregion

//    #region OtherInSameInstance

    @Test
    public void MultipleOptional_ToSingle_OtherInSameInstance_Reflective()
    {
        Node child = newMaterialGroup("myId");
        AbstractClassifierInstance parent = newBillOfMaterials("src");
        parent.addChild(BillOfMaterials_altGroups(), child);

        parent.addChild(BillOfMaterials_defaultGroup(), child);

        Assert.assertSame(parent, child.getParent());
        Assert.assertSame(child, parent.getChildren(BillOfMaterials_defaultGroup()));
        Assert.assertEquals(0, (parent.getChildren(BillOfMaterials_altGroups())).size());
    }

//    #endregion

//    #endregion

//    #endregion

//    #region required

//    #region singleEntry

//    #region SameInOtherInstance

    @Test
    public void MultipleRequired_Single_SameInOtherInstance_Reflective()
    {
        Node child = newLine("myId");
        AbstractClassifierInstance source = newCompositeShape("src");
        source.addChild(CompositeShape_parts(), child);
        AbstractClassifierInstance target = newCompositeShape("tgt");

        target.addChild(CompositeShape_parts(), child);

        Assert.assertSame(target, child.getParent());
        CollectionAssert.AreEqual(Lists.newArrayList( child ), target.getChildren(CompositeShape_parts()));
        Assert.assertThrows(IllegalArgumentException.class, () -> (source.getChildren(CompositeShape_parts())).size());
    }

//    #endregion

//    #region Other

    @Test
    public void MultipleRequired_Single_Other_Reflective()
    {
        Node child = newLine("myId");
        AbstractClassifierInstance source = newCompositeShape("src");
        source.addChild(CompositeShape_parts(), child);
        AbstractClassifierInstance target = newGeometry("tgt");

        target.addChild(Geometry_shapes(), child);

        Assert.assertSame(target, child.getParent());
        CollectionAssert.AreEqual(Lists.newArrayList( child ), target.getChildren(Geometry_shapes()));
        Assert.assertThrows(IllegalArgumentException.class, () -> (source.getChildren(CompositeShape_parts())).size());
    }

//    #endregion

//    #region OtherInSameInstance

    @Test
    public void MultipleRequired_Single_OtherInSameInstance_Reflective()
    {
        Node child = newLine("myId");
        AbstractClassifierInstance parent = newCompositeShape("src");
        parent.addChild(CompositeShape_parts(), child);

        parent.addChild(CompositeShape_disabledParts(), child);

        Assert.assertSame(parent, child.getParent());
        CollectionAssert.AreEqual(Lists.newArrayList( child ), parent.getChildren(CompositeShape_disabledParts()));
        Assert.assertThrows(IllegalArgumentException.class, () -> (parent.getChildren(CompositeShape_parts())).size());
    }

//    #endregion

//    #region SameInSameInstance

    @Test
    public void MultipleRequired_Single_SameInSameInstance_Reflective()
    {
        Node child = newLine("myId");
        AbstractClassifierInstance parent = newCompositeShape("src");
        parent.addChild(CompositeShape_parts(), child);

        parent.addChild(CompositeShape_parts(), child);

        Assert.assertSame(parent, child.getParent());
        CollectionAssert.AreEqual(Lists.newArrayList( child ), parent.getChildren(CompositeShape_parts()));
    }

//    #endregion

//    #endregion

//    #region someEntries

//    #region SameInOtherInstance

    @Test
    public void MultipleRequired_Partial_SameInOtherInstance_Reflective()
    {
        Node childA = newLine("a");
        Node childB = newLine("b");
        AbstractClassifierInstance source = newCompositeShape("src");
//        source.addChild(CompositeShape_parts(), Lists.newArrayList( childA, childB });
        source.addChild(CompositeShape_parts(), childA);
        source.addChild(CompositeShape_parts(), childB);
        AbstractClassifierInstance target = newCompositeShape("tgt");

        target.addChild(CompositeShape_parts(), childA);

        Assert.assertSame(target, childA.getParent());
        CollectionAssert.AreEqual(Lists.newArrayList( childA ), target.getChildren(CompositeShape_parts()));
        CollectionAssert.AreEqual(Lists.newArrayList( childB ), source.getChildren(CompositeShape_parts()));
    }

//    #endregion

//    #region Other

    @Test
    public void MultipleRequired_Partial_Other_Reflective()
    {
        Node childA = newLine("a");
        Node childB = newLine("b");
        AbstractClassifierInstance source = newCompositeShape("src");
//        source.addChild(CompositeShape_parts(), Lists.newArrayList( childA,childB });
        source.addChild(CompositeShape_parts(), childA);
        source.addChild(CompositeShape_parts(), childB);
        AbstractClassifierInstance target = newGeometry("tgt");

        target.addChild(Geometry_shapes(), childA);

        Assert.assertSame(target, childA.getParent());
        CollectionAssert.AreEqual(Lists.newArrayList( childA ), target.getChildren(Geometry_shapes()));
        CollectionAssert.AreEqual(Lists.newArrayList( childB ), source.getChildren(CompositeShape_parts()));
    }

//    #endregion

//    #region OtherInSameInstance

    @Test
    public void MultipleRequired_Partial_OtherInSameInstance_Reflective()
    {
        Node childA = newLine("a");
        Node childB = newLine("b");
        AbstractClassifierInstance parent = newCompositeShape("src");
//        parent.addChild(CompositeShape_parts(), Lists.newArrayList( childA,childB });
        parent.addChild(CompositeShape_parts(), childA);
        parent.addChild(CompositeShape_parts(), childB);

        parent.addChild(CompositeShape_disabledParts(), childA);

        Assert.assertSame(parent, childA.getParent());
        CollectionAssert.AreEqual(Lists.newArrayList( childA ), parent.getChildren(CompositeShape_disabledParts()));
        CollectionAssert.AreEqual(Lists.newArrayList( childB ), parent.getChildren(CompositeShape_parts()));
    }

//    #endregion

//    #region SameInSameInstance

    @Test
    public void MultipleRequired_Partial_SameInSameInstance_Reflective()
    {
        Node childA = newLine("myId");
        Node childB = newLine("b");
        AbstractClassifierInstance parent = newCompositeShape("src");
//        parent.addChild(CompositeShape_parts(), Lists.newArrayList( childA, childB });
        parent.addChild(CompositeShape_parts(), childA);
        parent.addChild(CompositeShape_parts(), childB);

        parent.addChild(CompositeShape_parts(), childA);

        Assert.assertSame(parent, childA.getParent());
        CollectionAssert.AreEqual(Lists.newArrayList( childA ), parent.getChildren(CompositeShape_parts()));
    }

//    #endregion

//    #endregion

//    #region FromSingle

//    #region Other

    @Test
    public void MultipleRequired_FromSingle_Other_Reflective()
    {
        Node child = newLine("myId");
        AbstractClassifierInstance source = newCompositeShape("src");
        source.addChild(CompositeShape_evilPart(), child);
        AbstractClassifierInstance target = newGeometry("tgt");

        target.addChild(Geometry_shapes(), child);

        Assert.assertSame(target, child.getParent());
        CollectionAssert.AreEqual(Lists.newArrayList( child ), target.getChildren(Geometry_shapes()));
        Assert.assertThrows(IllegalArgumentException.class, () -> source.getChildren(CompositeShape_evilPart()));
    }

//    #endregion

//    #region OtherInSameInstance

    @Test
    public void MultipleRequired_FromSingle_OtherInSameInstance_Reflective()
    {
        Node child = newLine("myId");
        AbstractClassifierInstance parent = newCompositeShape("src");
        parent.addChild(CompositeShape_evilPart(), child);

        parent.addChild(CompositeShape_parts(), child);

        Assert.assertSame(parent, child.getParent());
        CollectionAssert.AreEqual(Lists.newArrayList( child ), parent.getChildren(CompositeShape_parts()));
        Assert.assertThrows(IllegalArgumentException.class, () -> parent.getChildren(CompositeShape_evilPart()));
    }

//    #endregion

//    #endregion

//    #region ToSingle

//    #region Other

    @Test
    public void MultipleRequired_ToSingle_Other_Reflective()
    {
        Node child = newLine("myId");
        AbstractClassifierInstance source = newGeometry("src");
        source.addChild(Geometry_shapes(), child);
        AbstractClassifierInstance target = newCompositeShape("tgt");

        target.addChild(CompositeShape_evilPart(), child);

        Assert.assertSame(target, child.getParent());
        Assert.assertSame(child, target.getChildren(CompositeShape_evilPart()));
        Assert.assertEquals(0, (source.getChildren(Geometry_shapes())).size());
    }

//    #endregion

//    #region OtherInSameInstance

    @Test
    public void MultipleRequired_ToSingle_OtherInSameInstance_Reflective()
    {
        Node child = newLine("myId");
        AbstractClassifierInstance parent = newCompositeShape("src");
        parent.addChild(CompositeShape_parts(), child);

        parent.addChild(CompositeShape_evilPart(), child);

        Assert.assertSame(parent, child.getParent());
        Assert.assertSame(child, parent.getChildren(CompositeShape_evilPart()));
        Assert.assertThrows(IllegalArgumentException.class, () -> parent.getChildren(CompositeShape_parts()));
    }

//    #endregion

//    #endregion

//    #endregion

//    #endregion
}