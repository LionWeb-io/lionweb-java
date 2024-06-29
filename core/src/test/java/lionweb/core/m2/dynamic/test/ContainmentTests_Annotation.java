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
import lionweb.utils.tests.CollectionAssert;
import io.lionweb.lioncore.java.model.AnnotationInstance;
import io.lionweb.lioncore.java.model.impl.AbstractClassifierInstance;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class ContainmentTests_Annotation extends DynamicNodeTestsBase {
////    #region Single

    @Test
    public void Single_Add() {
        AbstractClassifierInstance parent = newLine("g");
        AnnotationInstance bom = newBillOfMaterials("myId");
        parent.addAnnotation(bom);
        Assert.assertEquals(parent, bom.getParent());
        Assert.assertTrue(parent.getAnnotations().contains(bom));
    }

//    @Test
//    public void Single_Reflective()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AbstractClassifierInstance bom = newBillOfMaterials("myId");
//        Assert.assertThrows(IllegalArgumentException.class, () -> parent.Set(null, bom));
//        Assert.assertEquals(null, bom.getParent());
//        Assert.assertFalse(parent.getAnnotations().contains(bom));
//    }

////    #region Insert

//    @Test
//    public void Single_Insert_Empty()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AnnotationInstance bom = newBillOfMaterials("myId");
//        parent.InsertAnnotations(0, [bom]);
//        Assert.assertEquals(parent, bom.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(bom));
//    }

//    @Test
//    public void Single_Insert_Empty_UnderBounds()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AnnotationInstance bom = newBillOfMaterials("myId");
//        Assert.assertThrows(ArgumentOutOfRangeException>(() -> parent.InsertAnnotations(-1, [bom]));
//        Assert.assertNull(bom.getParent());
//        Assert.assertFalse(parent.getAnnotations().contains(bom));
//    }

//    @Test
//    public void Single_Insert_Empty_OverBounds()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AnnotationInstance bom = newBillOfMaterials("myId");
//        Assert.assertThrows(ArgumentOutOfRangeException>(() -> parent.InsertAnnotations(1, [bom]));
//        Assert.assertNull(bom.getParent());
//        Assert.assertFalse(parent.getAnnotations().contains(bom));
//    }

//    @Test
//    public void Single_Insert_One_Before()
//    {
//        AnnotationInstance doc = newDocumentation("cId");
//        AbstractClassifierInstance parent = newLine("g");
//        parent.addAnnotation(doc);
//        AnnotationInstance bom = newBillOfMaterials("myId");
//        parent.InsertAnnotations(0, [bom]);
//        Assert.assertEquals(parent, doc.getParent());
//        Assert.assertEquals(parent, bom.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(bom));
//        CollectionAssert.assertEquals(Lists.newArrayList( bom, doc }, parent.getAnnotations());
//    }

//    @Test
//    public void Single_Insert_One_After()
//    {
//        AnnotationInstance doc = newDocumentation("cId");
//        AbstractClassifierInstance parent = newLine("g");
//        parent.addAnnotation(doc);
//        AnnotationInstance bom = newBillOfMaterials("myId");
//        parent.InsertAnnotations(1, [bom]);
//        Assert.assertEquals(parent, doc.getParent());
//        Assert.assertEquals(parent, bom.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(bom));
//        CollectionAssert.assertEquals(Lists.newArrayList( doc, bom }, parent.getAnnotations());
//    }

//    @Test
//    public void Single_Insert_Two_Before()
//    {
//        AnnotationInstance docA = newDocumentation("cIdA");
//        AnnotationInstance docB = newDocumentation("cIdB");
//        AbstractClassifierInstance parent = newLine("g");
//        parent.addAnnotation(docA, docB);
//        AnnotationInstance bom = newBillOfMaterials("myId");
//        parent.InsertAnnotations(0, [bom]);
//        Assert.assertEquals(parent, docA.getParent());
//        Assert.assertEquals(parent, docB.getParent());
//        Assert.assertEquals(parent, bom.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(bom));
//        CollectionAssert.assertEquals(Lists.newArrayList( bom, docA, docB }, parent.getAnnotations());
//    }

//    @Test
//    public void Single_Insert_Two_Between()
//    {
//        AnnotationInstance docA = newDocumentation("cIdA");
//        AnnotationInstance docB = newDocumentation("cIdB");
//        AbstractClassifierInstance parent = newLine("g");
//        parent.addAnnotation(docA, docB]);
//        AnnotationInstance bom = newBillOfMaterials("myId");
//        parent.InsertAnnotations(1, [bom]);
//        Assert.assertEquals(parent, docA.getParent());
//        Assert.assertEquals(parent, docB.getParent());
//        Assert.assertEquals(parent, bom.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(bom));
//        CollectionAssert.assertEquals(Lists.newArrayList( docA, bom, docB }, parent.getAnnotations());
//    }

//    @Test
//    public void Single_Insert_Two_After()
//    {
//        AnnotationInstance docA = newDocumentation("cIdA");
//        AnnotationInstance docB = newDocumentation("cIdB");
//        AbstractClassifierInstance parent = newLine("g");
//        parent.addAnnotation(docA, docB]);
//        AnnotationInstance bom = newBillOfMaterials("myId");
//        parent.InsertAnnotations(2, [bom]);
//        Assert.assertEquals(parent, docA.getParent());
//        Assert.assertEquals(parent, docB.getParent());
//        Assert.assertEquals(parent, bom.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(bom));
//        CollectionAssert.assertEquals(Lists.newArrayList( docA, docB, bom }, parent.getAnnotations());
//    }

////    #endregion

////    #region Remove

    @Test
    public void Single_Remove_Empty() {
        AbstractClassifierInstance parent = newLine("g");
        AnnotationInstance bom = newBillOfMaterials("myId");
        parent.removeAnnotation(bom);
        Assert.assertNull(bom.getParent());
        Assert.assertFalse(parent.getAnnotations().contains(bom));
    }

    @Test
    public void Single_Remove_NotContained() {
        AnnotationInstance doc = newDocumentation("myC");
        AbstractClassifierInstance parent = newLine("cs");
        parent.addAnnotation(doc);
        AnnotationInstance bom = newBillOfMaterials("myId");
        parent.removeAnnotation(bom);
        Assert.assertEquals(parent, doc.getParent());
        Assert.assertNull(bom.getParent());
        Assert.assertEquals(Lists.newArrayList(doc), parent.getAnnotations());
    }

    @Test
    public void Single_Remove_Only() {
        AnnotationInstance bom = newBillOfMaterials("myId");
        AbstractClassifierInstance parent = newLine("g");
        parent.addAnnotation(bom);
        parent.removeAnnotation(bom);
        Assert.assertNull(bom.getParent());
        Assert.assertEquals(Collections.emptyList(), parent.getAnnotations());
    }

    @Test
    public void Single_Remove_First() {
        AnnotationInstance doc = newDocumentation("cId");
        AnnotationInstance bom = newBillOfMaterials("myId");
        AbstractClassifierInstance parent = newLine("g");
        parent.addAnnotation(bom);
        parent.addAnnotation(doc);
        parent.removeAnnotation(bom);
        Assert.assertEquals(parent, doc.getParent());
        Assert.assertNull(bom.getParent());
        CollectionAssert.AreEqual(Lists.newArrayList(doc), parent.getAnnotations());
    }

    @Test
    public void Single_Remove_Last() {
        AnnotationInstance doc = newDocumentation("cId");
        AnnotationInstance bom = newBillOfMaterials("myId");
        AbstractClassifierInstance parent = newLine("g");
        parent.addAnnotation(doc);
        parent.addAnnotation(bom);
        parent.removeAnnotation(bom);
        Assert.assertEquals(parent, doc.getParent());
        Assert.assertNull(bom.getParent());
        CollectionAssert.AreEqual(Lists.newArrayList(doc), parent.getAnnotations());
    }

    @Test
    public void Single_Remove_Between() {
        AnnotationInstance docA = newDocumentation("cIdA");
        AnnotationInstance docB = newDocumentation("cIdB");
        AnnotationInstance bom = newBillOfMaterials("myId");
        AbstractClassifierInstance parent = newLine("g");
        parent.addAnnotation(docA);
        parent.addAnnotation(bom);
        parent.addAnnotation(docB);
        parent.removeAnnotation(bom);
        Assert.assertEquals(parent, docA.getParent());
        Assert.assertEquals(parent, docB.getParent());
        Assert.assertNull(bom.getParent());
        CollectionAssert.AreEqual(Lists.newArrayList(docA, docB), parent.getAnnotations());
    }

////    #endregion

////    #endregion

////    #region Null

    @Test
    public void Null() {
        AbstractClassifierInstance parent = newLine("g");
        Assert.assertThrows(NullPointerException.class, () -> parent.addAnnotation(null));
    }

//    @Test
//    public void Null_Reflective()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        Assert.assertThrows(IllegalArgumentException.class, () -> parent.Set(null, null));
//    }

//    @Test
//    public void Null_Insert_Empty()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        Assert.assertThrows(IllegalArgumentException.class, () -> parent.InsertAnnotations(0, null));
//    }

//    @Test
//    public void Null_Insert_Empty_OutOfBounds()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        Assert.assertThrows(ArgumentOutOfRangeException>(() -> parent.InsertAnnotations(1, null));
//    }

    @Test
    public void Null_Remove_Empty() {
        AbstractClassifierInstance parent = newLine("g");
        Assert.assertThrows(NullPointerException.class, () -> parent.removeAnnotation(null));
    }

////    #endregion

////    #region EmptyCollection

//    @Test
//    public void EmptyArray()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AbstractClassifierInstance[] values = new AbstractClassifierInstance[0];
//        parent.addAnnotation(values);
//        Assert.assertTrue(parent.getAnnotations().size() == 0);
//    }

//    @Test
//    public void EmptyArray_Reflective()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AbstractClassifierInstance[] values = new AbstractClassifierInstance[0];
//        parent.Set(null, values);
//        Assert.assertTrue(parent.getAnnotations().size() == 0);
//    }

//    @Test
//    public void Insert_EmptyArray()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AbstractClassifierInstance[] values = new AbstractClassifierInstance[0];
//        parent.InsertAnnotations(0, values);
//        Assert.assertTrue(parent.getAnnotations().size() == 0);
//    }

//    @Test
//    public void Remove_EmptyArray()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AbstractClassifierInstance[] values = new AbstractClassifierInstance[0];
//        parent.RemoveAnnotations(values);
//        Assert.assertTrue(parent.getAnnotations().size() == 0);
//    }

//    @Test
//    public void EmptyUntypedList_Reflective()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        ArrayList values = new ArrayList();
//        parent.Set(null, values);
//        Assert.assertTrue(parent.getAnnotations().size() == 0);
//    }

//    @Test
//    public void EmptyListMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        ArrayList<INode> values = new ArrayList<INode>();
//        parent.Set(null, values);
//        Assert.assertTrue(parent.getAnnotations().size() == 0);
//    }

//    @Test
//    public void EmptySet_Reflective()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        HashSet<INode> values = new HashSet<INode>();
//        parent.Set(null, values);
//        Assert.assertTrue(parent.getAnnotations().size() == 0);
//    }

////    #endregion

////    #region NullCollection

//    @Test
//    public void NullArray()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AbstractClassifierInstance[] values = new AbstractClassifierInstance[] { null };
//        Assert.assertThrows(IllegalArgumentException.class, () -> parent.addAnnotation(values));
//        Assert.assertTrue(parent.getAnnotations().size() == 0);
//    }

//    @Test
//    public void NullArray_Reflective()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AbstractClassifierInstance[] values = new AbstractClassifierInstance[] { null };
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> parent.Set(null, values));
//        Assert.assertTrue(parent.getAnnotations().size() == 0);
//    }

//    @Test
//    public void Insert_NullArray()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AbstractClassifierInstance[] values = new AbstractClassifierInstance[] { null };
//        Assert.assertThrows(IllegalArgumentException.class, () -> parent.InsertAnnotations(0, values));
//        Assert.assertTrue(parent.getAnnotations().size() == 0);
//    }

//    @Test
//    public void Remove_NullArray()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AbstractClassifierInstance[] values = new AbstractClassifierInstance[] { null };
//        Assert.assertThrows(IllegalArgumentException.class, () -> parent.RemoveAnnotations(values));
//        Assert.assertTrue(parent.getAnnotations().size() == 0);
//    }

//    @Test
//    public void NullUntypedList_Reflective()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AbstractClassifierInstance values = new ArrayList() { null };
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> parent.Set(null, values));
//        Assert.assertTrue(parent.getAnnotations().size() == 0);
//    }

//    @Test
//    public void NullListMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AbstractClassifierInstance values = new List<INode>() { null };
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> parent.Set(null, values));
//        Assert.assertTrue(parent.getAnnotations().size() == 0);
//    }

//    @Test
//    public void NullSet_Reflective()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AbstractClassifierInstance values = new HashSet<INode>() { null };
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> parent.Set(null, values));
//        Assert.assertTrue(parent.getAnnotations().size() == 0);
//    }

////    #endregion

////    #region SingleCollection

//    @Test
//    public void SingleArray()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AnnotationInstance value = newBillOfMaterials("s");
//        AnnotationInstance[] values = new AnnotationInstance[]{ value };
//        parent.addAnnotation(values);
//        Assert.assertEquals(parent, value.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(value));
//    }

//    @Test
//    public void SingleArray_Reflective()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AnnotationInstance value = newBillOfMaterials("s");
//        AnnotationInstance[] values = new AnnotationInstance[]{ value };
//        parent.Set(null, values);
//        Assert.assertEquals(parent, value.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(value));
//    }

//    @Test
//    public void SingleArray_Existing_Reflective()
//    {
//        AnnotationInstance doc = newDocumentation("cc");
//        AbstractClassifierInstance parent = newLine("g");
//        parent.addAnnotation(]);
//        AnnotationInstance value = newBillOfMaterials("s");
//        AnnotationInstance[] values = new AnnotationInstance[]{ value };
//        parent.Set(null, values);
//        Assert.assertNull(doc.getParent());
//        Assert.assertEquals(parent, value.getParent());
//        CollectionAssert.assertEquals(Lists.newArrayList( value }, parent.getAnnotations());
//    }

//    @Test
//    public void Insert_SingleArray()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AnnotationInstance value = newBillOfMaterials("s");
//        AnnotationInstance[] values = new AnnotationInstance[]{ value };
//        parent.InsertAnnotations(0, values);
//        Assert.assertEquals(parent, value.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(value));
//    }

////    #region Remove

//    @Test
//    public void SingleArray_Remove_Empty()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AnnotationInstance bom = newBillOfMaterials("myId");
//        AnnotationInstance[] values = new AnnotationInstance[]{ bom };
//        parent.RemoveAnnotations(values);
//        Assert.assertNull(bom.getParent());
//        Assert.assertFalse(parent.getAnnotations().contains(bom));
//    }

//    @Test
//    public void SingleArray_Remove_Only()
//    {
//        AnnotationInstance bom = newBillOfMaterials("myId");
//        AbstractClassifierInstance parent = newLine("g");
//        parent.addAnnotation(bom]);
//        AnnotationInstance[] values = new AnnotationInstance[]{ bom };
//        parent.RemoveAnnotations(values);
//        Assert.assertNull(bom.getParent());
//        CollectionAssert.assertEquals(Lists.newArrayList( }, parent.getAnnotations());
//    }

//    @Test
//    public void SingleArray_Remove_First()
//    {
//        AnnotationInstance doc = newDocumentation("cId");
//        AnnotationInstance bom = newBillOfMaterials("myId");
//        AbstractClassifierInstance parent = newLine("g");
//        parent.addAnnotation(bom, doc]);
//        AnnotationInstance[] values = new AnnotationInstance[]{ bom };
//        parent.RemoveAnnotations(values);
//        Assert.assertEquals(parent, doc.getParent());
//        Assert.assertNull(bom.getParent());
//        CollectionAssert.assertEquals(Lists.newArrayList( doc }, parent.getAnnotations());
//    }

//    @Test
//    public void SingleArray_Remove_Last()
//    {
//        AnnotationInstance doc = newDocumentation("cId");
//        AnnotationInstance bom = newBillOfMaterials("myId");
//        AbstractClassifierInstance parent = newLine("g");
//        parent.addAnnotation(doc, bom]);
//        AnnotationInstance[] values = new AnnotationInstance[]{ bom };
//        parent.RemoveAnnotations(values);
//        Assert.assertEquals(parent, doc.getParent());
//        Assert.assertNull(bom.getParent());
//        CollectionAssert.assertEquals(Lists.newArrayList( doc }, parent.getAnnotations());
//    }

//    @Test
//    public void SingleArray_Remove_Between()
//    {
//        AnnotationInstance docA = newDocumentation("cIdA");
//        AnnotationInstance docB = newDocumentation("cIdB");
//        AnnotationInstance bom = newBillOfMaterials("myId");
//        AbstractClassifierInstance parent = newLine("g");
//        parent.addAnnotation(docA, bom, docB]);
//        AnnotationInstance[] values = new AnnotationInstance[]{ bom };
//        parent.RemoveAnnotations(values);
//        Assert.assertEquals(parent, docA.getParent());
//        Assert.assertEquals(parent, docB.getParent());
//        Assert.assertNull(bom.getParent());
//        CollectionAssert.assertEquals(Lists.newArrayList( docA, docB }, parent.getAnnotations());
//    }

////    #endregion

//    @Test
//    public void SingleUntypedArray_Reflective()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AnnotationInstance value = newBillOfMaterials("s");
//        Object[] values = new Object[] { value };
//        parent.Set(null, values);
//        Assert.assertEquals(parent, value.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(value));
//    }

//    @Test
//    public void SingleUntypedList_Reflective()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AnnotationInstance value = newBillOfMaterials("s");
//        AbstractClassifierInstance values = new ArrayList() { value };
//        parent.Set(null, values);
//        Assert.assertEquals(parent, value.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(value));
//    }

//    @Test
//    public void SingleListMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AnnotationInstance value = newBillOfMaterials("s");
//        AbstractClassifierInstance values = new ArrayList<INode>() { value };
//        parent.Set(null, values);
//        Assert.assertEquals(parent, value.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(value));
//    }

//    @Test
//    public void SingleSet_Reflective()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AnnotationInstance value = newBillOfMaterials("s");
//        AbstractClassifierInstance values = new HashSet<INode>() { value };
//        parent.Set(null, values);
//        Assert.assertEquals(parent, value.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(value));
//    }

//    @Test
//    public void SingleListNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AbstractClassifierInstance value = newCoord("c");
//        AbstractClassifierInstance values = new ArrayList<INode>() { value };
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> parent.Set(null, values));
//        Assert.assertTrue(parent.getAnnotations().size() == 0);
//    }

//    @Test
//    public void SingleUntypedListNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AbstractClassifierInstance value = newCoord("c");
//        AbstractClassifierInstance values = new ArrayList() { value };
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> parent.Set(null, values));
//        Assert.assertTrue(parent.getAnnotations().size() == 0);
//    }

//    @Test
//    public void SingleUntypedArrayNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AbstractClassifierInstance value = newCoord("c");
//        Object[] values = new Object[] { value };
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> parent.Set(null, values));
//        Assert.assertTrue(parent.getAnnotations().size() == 0);
//    }

////    #endregion

////    #region MultipleCollection

//    @Test
//    public void MultipleArray()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AnnotationInstance valueA = newBillOfMaterials("sA");
//        AnnotationInstance valueB = newBillOfMaterials("sB");
//        AnnotationInstance[] values = new AnnotationInstance[]{ valueA, valueB };
//        parent.addAnnotation(values);
//        Assert.assertEquals(parent, valueA.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(valueA));
//        Assert.assertEquals(parent, valueB.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(valueB));
//    }

//    @Test
//    public void MultipleArray_Reflective()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AnnotationInstance valueA = newBillOfMaterials("sA");
//        AnnotationInstance valueB = newBillOfMaterials("sB");
//        AnnotationInstance[] values = new AnnotationInstance[]{ valueA, valueB };
//        parent.Set(null, values);
//        Assert.assertEquals(parent, valueA.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(valueA));
//        Assert.assertEquals(parent, valueB.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(valueB));
//    }

////    #region Insert

//    @Test
//    public void Multiple_Insert_ListMatchingType()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AnnotationInstance valueA = newBillOfMaterials("sA");
//        AnnotationInstance valueB = newBillOfMaterials("sB");
//        AbstractClassifierInstance values = new ArrayList<INode> { valueA, valueB };
//        parent.InsertAnnotations(0, values);
//        Assert.assertEquals(parent, valueA.getParent());
//        Assert.assertEquals(parent, valueB.getParent());
//        CollectionAssert.assertEquals(Lists.newArrayList( valueA, valueB }, parent.getAnnotations());
//    }

//    @Test
//    public void Multiple_Insert_Set()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AnnotationInstance valueA = newBillOfMaterials("sA");
//        AnnotationInstance valueB = newBillOfMaterials("sB");
//        AbstractClassifierInstance values = new HashSet<INode> { valueA, valueB };
//        parent.InsertAnnotations(0, values);
//        Assert.assertEquals(parent, valueA.getParent());
//        Assert.assertEquals(parent, valueB.getParent());
//        CollectionAssert.assertEquals(Lists.newArrayList( valueA, valueB }, parent.getAnnotations());
//    }

//    @Test
//    public void Multiple_Insert_SingleEnumerable()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AnnotationInstance valueA = newBillOfMaterials("sA");
//        AnnotationInstance valueB = newBillOfMaterials("sB");
//        AbstractClassifierInstance values = new SingleEnumerable<INode> { valueA, valueB };
//        parent.InsertAnnotations(0, values);
//        Assert.assertEquals(parent, valueA.getParent());
//        Assert.assertEquals(parent, valueB.getParent());
//        CollectionAssert.assertEquals(Lists.newArrayList( valueA, valueB }, parent.getAnnotations());
//    }

//    @Test
//    public void Multiple_Insert_Empty()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AnnotationInstance valueA = newBillOfMaterials("sA");
//        AnnotationInstance valueB = newBillOfMaterials("sB");
//        AnnotationInstance[] values = new AnnotationInstance[]{ valueA, valueB };
//        parent.InsertAnnotations(0, values);
//        Assert.assertEquals(parent, valueA.getParent());
//        Assert.assertEquals(parent, valueB.getParent());
//        CollectionAssert.assertEquals(Lists.newArrayList( valueA, valueB }, parent.getAnnotations());
//    }

//    @Test
//    public void Multiple_Insert_One_Before()
//    {
//        AnnotationInstance doc = newDocumentation("cId");
//        AbstractClassifierInstance parent = newLine("g");
//        parent.addAnnotation(doc]);
//        AnnotationInstance valueA = newBillOfMaterials("sA");
//        AnnotationInstance valueB = newBillOfMaterials("sB");
//        AnnotationInstance[] values = new AnnotationInstance[]{ valueA, valueB };
//        parent.InsertAnnotations(0, values);
//        Assert.assertEquals(parent, doc.getParent());
//        Assert.assertEquals(parent, valueA.getParent());
//        Assert.assertEquals(parent, valueB.getParent());
//        CollectionAssert.assertEquals(Lists.newArrayList( valueA, valueB, doc }, parent.getAnnotations());
//    }

//    @Test
//    public void Multiple_Insert_One_After()
//    {
//        AnnotationInstance doc = newDocumentation("cId");
//        AbstractClassifierInstance parent = newLine("g");
//        parent.addAnnotation(doc]);
//        AnnotationInstance valueA = newBillOfMaterials("sA");
//        AnnotationInstance valueB = newBillOfMaterials("sB");
//        AnnotationInstance[] values = new AnnotationInstance[]{ valueA, valueB };
//        parent.InsertAnnotations(1, values);
//        Assert.assertEquals(parent, doc.getParent());
//        Assert.assertEquals(parent, valueA.getParent());
//        Assert.assertEquals(parent, valueB.getParent());
//        CollectionAssert.assertEquals(Lists.newArrayList( doc, valueA, valueB }, parent.getAnnotations());
//    }

//    @Test
//    public void Multiple_Insert_Two_Before()
//    {
//        AnnotationInstance docA = newDocumentation("cIdA");
//        AnnotationInstance docB = newDocumentation("cIdB");
//        AbstractClassifierInstance parent = newLine("g");
//        parent.addAnnotation(docA, docB]);
//        AnnotationInstance valueA = newBillOfMaterials("sA");
//        AnnotationInstance valueB = newBillOfMaterials("sB");
//        AnnotationInstance[] values = new AnnotationInstance[]{ valueA, valueB };
//        parent.InsertAnnotations(0, values);
//        Assert.assertEquals(parent, docA.getParent());
//        Assert.assertEquals(parent, docB.getParent());
//        Assert.assertEquals(parent, valueA.getParent());
//        Assert.assertEquals(parent, valueB.getParent());
//        CollectionAssert.assertEquals(Lists.newArrayList( valueA, valueB, docA, docB }, parent.getAnnotations());
//    }

//    @Test
//    public void Multiple_Insert_Two_Between()
//    {
//        AnnotationInstance docA = newDocumentation("cIdA");
//        AnnotationInstance docB = newDocumentation("cIdB");
//        AbstractClassifierInstance parent = newLine("g");
//        parent.addAnnotation(docA, docB]);
//        AnnotationInstance valueA = newBillOfMaterials("sA");
//        AnnotationInstance valueB = newBillOfMaterials("sB");
//        AnnotationInstance[] values = new AnnotationInstance[]{ valueA, valueB };
//        parent.InsertAnnotations(1, values);
//        Assert.assertEquals(parent, docA.getParent());
//        Assert.assertEquals(parent, docB.getParent());
//        Assert.assertEquals(parent, valueA.getParent());
//        Assert.assertEquals(parent, valueB.getParent());
//        CollectionAssert.assertEquals(Lists.newArrayList( docA, valueA, valueB, docB }, parent.getAnnotations());
//    }

//    @Test
//    public void Multiple_Insert_Two_After()
//    {
//        AnnotationInstance docA = newDocumentation("cIdA");
//        AnnotationInstance docB = newDocumentation("cIdB");
//        AbstractClassifierInstance parent = newLine("g");
//        parent.addAnnotation(docA, docB]);
//        AnnotationInstance valueA = newBillOfMaterials("sA");
//        AnnotationInstance valueB = newBillOfMaterials("sB");
//        AnnotationInstance[] values = new AnnotationInstance[]{ valueA, valueB };
//        parent.InsertAnnotations(2, values);
//        Assert.assertEquals(parent, docA.getParent());
//        Assert.assertEquals(parent, docB.getParent());
//        Assert.assertEquals(parent, valueA.getParent());
//        Assert.assertEquals(parent, valueB.getParent());
//        CollectionAssert.assertEquals(Lists.newArrayList( docA, docB, valueA, valueB }, parent.getAnnotations());
//    }

////    #endregion

////    #region Remove

//    @Test
//    public void Multiple_Remove_ListMatchingType()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AnnotationInstance valueA = newBillOfMaterials("sA");
//        AnnotationInstance valueB = newBillOfMaterials("sB");
//        AbstractClassifierInstance values = new ArrayList<INode>() { valueA, valueB };
//        parent.RemoveAnnotations(values);
//        Assert.assertNull(valueA.getParent());
//        Assert.assertNull(valueB.getParent());
//        Assert.assertFalse(parent.getAnnotations().contains(valueA));
//        Assert.assertFalse(parent.getAnnotations().contains(valueB));
//    }

//    @Test
//    public void Multiple_Remove_Set()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AnnotationInstance valueA = newBillOfMaterials("sA");
//        AnnotationInstance valueB = newBillOfMaterials("sB");
//        AbstractClassifierInstance values = new HashSet<INode>() { valueA, valueB };
//        parent.RemoveAnnotations(values);
//        Assert.assertNull(valueA.getParent());
//        Assert.assertNull(valueB.getParent());
//        Assert.assertFalse(parent.getAnnotations().contains(valueA));
//        Assert.assertFalse(parent.getAnnotations().contains(valueB));
//    }

//    @Test
//    public void Multiple_Remove_SingleEnumerable()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AnnotationInstance valueA = newBillOfMaterials("sA");
//        AnnotationInstance valueB = newBillOfMaterials("sB");
//        AbstractClassifierInstance values = new SingleEnumerable<INode>() { valueA, valueB };
//        parent.RemoveAnnotations(values);
//        Assert.assertNull(valueA.getParent());
//        Assert.assertNull(valueB.getParent());
//        Assert.assertFalse(parent.getAnnotations().contains(valueA));
//        Assert.assertFalse(parent.getAnnotations().contains(valueB));
//    }

//    @Test
//    public void Multiple_Remove_Empty()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AnnotationInstance valueA = newBillOfMaterials("sA");
//        AnnotationInstance valueB = newBillOfMaterials("sB");
//        AnnotationInstance[] values = new AnnotationInstance[]{ valueA, valueB };
//        parent.RemoveAnnotations(values);
//        Assert.assertNull(valueA.getParent());
//        Assert.assertNull(valueB.getParent());
//        Assert.assertFalse(parent.getAnnotations().contains(valueA));
//        Assert.assertFalse(parent.getAnnotations().contains(valueB));
//    }

//    @Test
//    public void Multiple_Remove_NonContained()
//    {
//        AnnotationInstance docA = newDocumentation("cA");
//        AnnotationInstance docB = newDocumentation("cB");
//        AbstractClassifierInstance parent = newLine("cs");
//        parent.addAnnotation(docA, docB]);
//        AnnotationInstance valueA = newBillOfMaterials("sA");
//        AnnotationInstance valueB = newBillOfMaterials("sB");
//        AnnotationInstance[] values = new AnnotationInstance[]{ valueA, valueB };
//        parent.RemoveAnnotations(values);
//        Assert.assertEquals(parent, docA.getParent());
//        Assert.assertEquals(parent, docB.getParent());
//        Assert.assertNull(valueA.getParent());
//        Assert.assertNull(valueB.getParent());
//        CollectionAssert.assertEquals(Lists.newArrayList( docA, docB }, parent.getAnnotations());
//    }

//    @Test
//    public void Multiple_Remove_HalfContained()
//    {
//        AnnotationInstance docA = newDocumentation("cA");
//        AnnotationInstance docB = newDocumentation("cB");
//        AbstractClassifierInstance parent = newLine("cs");
//        parent.addAnnotation(docA, docB]);
//        AnnotationInstance valueA = newBillOfMaterials("sA");
//        AnnotationInstance[] values = new AnnotationInstance[]{ valueA, docA };
//        parent.RemoveAnnotations(values);
//        Assert.assertEquals(parent, docB.getParent());
//        Assert.assertNull(valueA.getParent());
//        Assert.assertNull(docA.getParent());
//        CollectionAssert.assertEquals(Lists.newArrayList( docB }, parent.getAnnotations());
//    }

//    @Test
//    public void Multiple_Remove_Only()
//    {
//        AnnotationInstance valueA = newBillOfMaterials("sA");
//        AnnotationInstance valueB = newBillOfMaterials("sB");
//        AbstractClassifierInstance parent = newLine("g");
//        parent.addAnnotation(valueA, valueB]);
//        AnnotationInstance[] values = new AnnotationInstance[]{ valueA, valueB };
//        parent.RemoveAnnotations(values);
//        Assert.assertNull(valueA.getParent());
//        Assert.assertNull(valueB.getParent());
//        CollectionAssert.assertEquals(Lists.newArrayList( }, parent.getAnnotations());
//    }

//    @Test
//    public void Multiple_Remove_First()
//    {
//        AnnotationInstance doc = newDocumentation("cId");
//        AnnotationInstance valueA = newBillOfMaterials("sA");
//        AnnotationInstance valueB = newBillOfMaterials("sB");
//        AbstractClassifierInstance parent = newLine("g");
//        parent.addAnnotation(valueA, valueB, doc]);
//        AnnotationInstance[] values = new AnnotationInstance[]{ valueA, valueB };
//        parent.RemoveAnnotations(values);
//        Assert.assertEquals(parent, doc.getParent());
//        Assert.assertNull(valueA.getParent());
//        Assert.assertNull(valueB.getParent());
//        CollectionAssert.assertEquals(Lists.newArrayList( doc }, parent.getAnnotations());
//    }

//    @Test
//    public void Multiple_Remove_Last()
//    {
//        AnnotationInstance doc = newDocumentation("cId");
//        AnnotationInstance valueA = newBillOfMaterials("sA");
//        AnnotationInstance valueB = newBillOfMaterials("sB");
//        AbstractClassifierInstance parent = newLine("g");
//        parent.addAnnotation(doc, valueA, valueB]);
//        AnnotationInstance[] values = new AnnotationInstance[]{ valueA, valueB };
//        parent.RemoveAnnotations(values);
//        Assert.assertEquals(parent, doc.getParent());
//        Assert.assertNull(valueA.getParent());
//        Assert.assertNull(valueB.getParent());
//        CollectionAssert.assertEquals(Lists.newArrayList( doc }, parent.getAnnotations());
//    }

//    @Test
//    public void Multiple_Remove_Between()
//    {
//        AnnotationInstance docA = newDocumentation("cIdA");
//        AnnotationInstance docB = newDocumentation("cIdB");
//        AnnotationInstance valueA = newBillOfMaterials("sA");
//        AnnotationInstance valueB = newBillOfMaterials("sB");
//        AbstractClassifierInstance parent = newLine("g");
//        parent.addAnnotation(docA, valueA, valueB, docB]);
//        AnnotationInstance[] values = new AnnotationInstance[]{ valueA, valueB };
//        parent.RemoveAnnotations(values);
//        Assert.assertEquals(parent, docA.getParent());
//        Assert.assertEquals(parent, docB.getParent());
//        Assert.assertNull(valueA.getParent());
//        Assert.assertNull(valueB.getParent());
//        CollectionAssert.assertEquals(Lists.newArrayList( docA, docB }, parent.getAnnotations());
//    }

//    @Test
//    public void Multiple_Remove_Mixed()
//    {
//        AnnotationInstance docA = newDocumentation("cIdA");
//        AnnotationInstance docB = newDocumentation("cIdB");
//        AnnotationInstance valueA = newBillOfMaterials("sA");
//        AnnotationInstance valueB = newBillOfMaterials("sB");
//        AbstractClassifierInstance parent = newLine("g");
//        parent.addAnnotation(valueA, docA, valueB, docB]);
//        AnnotationInstance[] values = new AnnotationInstance[]{ valueA, valueB };
//        parent.RemoveAnnotations(values);
//        Assert.assertEquals(parent, docA.getParent());
//        Assert.assertEquals(parent, docB.getParent());
//        Assert.assertNull(valueA.getParent());
//        Assert.assertNull(valueB.getParent());
//        CollectionAssert.assertEquals(Lists.newArrayList( docA, docB }, parent.getAnnotations());
//    }

////    #endregion

//    @Test
//    public void MultipleUntypedArray_Reflective()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AnnotationInstance valueA = newBillOfMaterials("sA");
//        AnnotationInstance valueB = newBillOfMaterials("sB");
//        Object[] values = new Object[] { valueA, valueB };
//        parent.Set(null, values);
//        Assert.assertEquals(parent, valueA.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(valueA));
//        Assert.assertEquals(parent, valueB.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(valueB));
//    }

//    @Test
//    public void MultipleUntypedList_Reflective()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AnnotationInstance valueA = newBillOfMaterials("sA");
//        AnnotationInstance valueB = newBillOfMaterials("sB");
//        AbstractClassifierInstance values = new ArrayList() { valueA, valueB };
//        parent.Set(null, values);
//        Assert.assertEquals(parent, valueA.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(valueA));
//        Assert.assertEquals(parent, valueB.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(valueB));
//    }

//    @Test
//    public void MultipleListMatchingType()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AnnotationInstance valueA = newBillOfMaterials("sA");
//        AnnotationInstance valueB = newBillOfMaterials("sB");
//        AbstractClassifierInstance values = new ArrayList<INode>() { valueA, valueB };
//        parent.addAnnotation(values);
//        Assert.assertEquals(parent, valueA.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(valueA));
//        Assert.assertEquals(parent, valueB.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(valueB));
//    }

//    @Test
//    public void MultipleListMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AnnotationInstance valueA = newBillOfMaterials("sA");
//        AnnotationInstance valueB = newBillOfMaterials("sB");
//        AbstractClassifierInstance values = new ArrayList<INode>() { valueA, valueB };
//        parent.Set(null, values);
//        Assert.assertEquals(parent, valueA.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(valueA));
//        Assert.assertEquals(parent, valueB.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(valueB));
//    }

//    @Test
//    public void MultipleSet()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AnnotationInstance valueA = newBillOfMaterials("sA");
//        AnnotationInstance valueB = newBillOfMaterials("sB");
//        AbstractClassifierInstance values = new HashSet<INode>() { valueA, valueB };
//        parent.addAnnotation(values);
//        Assert.assertEquals(parent, valueA.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(valueA));
//        Assert.assertEquals(parent, valueB.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(valueB));
//    }

//    @Test
//    public void MultipleSet_Reflective()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AnnotationInstance valueA = newBillOfMaterials("sA");
//        AnnotationInstance valueB = newBillOfMaterials("sB");
//        AbstractClassifierInstance values = new HashSet<INode>() { valueA, valueB };
//        parent.Set(null, values);
//        Assert.assertEquals(parent, valueA.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(valueA));
//        Assert.assertEquals(parent, valueB.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(valueB));
//    }

//    @Test
//    public void MultipleSingleEnumerable()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AnnotationInstance valueA = newBillOfMaterials("sA");
//        AnnotationInstance valueB = newBillOfMaterials("sB");
//        AbstractClassifierInstance values = new SingleEnumerable<INode> { valueA, valueB };
//        parent.addAnnotation(values);
//        Assert.assertEquals(parent, valueA.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(valueA));
//        Assert.assertEquals(parent, valueB.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(valueB));
//    }

//    @Test
//    public void MultipleSingleEnumerable_Reflective()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AnnotationInstance valueA = newBillOfMaterials("sA");
//        AnnotationInstance valueB = newBillOfMaterials("sB");
//        AbstractClassifierInstance values = new SingleEnumerable<INode> { valueA, valueB };
//        parent.Set(null, values);
//        Assert.assertEquals(parent, valueA.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(valueA));
//        Assert.assertEquals(parent, valueB.getParent());
//        Assert.assertTrue(parent.getAnnotations().contains(valueB));
//    }

//    @Test
//    public void MultipleListNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AbstractClassifierInstance valueA = newCoord("cA");
//        AbstractClassifierInstance valueB = newCoord("cB");
//        AbstractClassifierInstance values = new ArrayList<INode>() { valueA, valueB };
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> parent.Set(null, values));
//        Assert.assertTrue(parent.getAnnotations().size() == 0);
//    }

//    @Test
//    public void MultipleUntypedListNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AbstractClassifierInstance valueA = newCoord("cA");
//        AbstractClassifierInstance valueB = newCoord("cB");
//        AbstractClassifierInstance values = new ArrayList() { valueA, valueB };
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> parent.Set(null, values));
//        Assert.assertTrue(parent.getAnnotations().size() == 0);
//    }

//    @Test
//    public void MultipleUntypedArrayNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AbstractClassifierInstance valueA = newCoord("cA");
//        AbstractClassifierInstance valueB = newCoord("cB");
//        Object[] values = new Object[] { valueA, valueB };
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> parent.Set(null, values));
//        Assert.assertTrue(parent.getAnnotations().size() == 0);
//    }

//    @Test
//    public void SingleList_NotAnnotating()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AnnotationInstance value = newDocumentation("sA");
//        AbstractClassifierInstance values = new ArrayList<INode>() { value };
//        Assert.assertThrows(IllegalArgumentException.class, () -> parent.addAnnotation(values));
//        Assert.assertNull(value.getParent());
//        Assert.assertFalse(parent.getAnnotations().contains(value));
//    }

//    @Test
//    public void SingleList_NotAnnotating_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AnnotationInstance value = newDocumentation("sA");
//        AbstractClassifierInstance values = new ArrayList<INode>() { value };
//        Assert.assertThrows(IllegalArgumentException.class, () -> parent.Set(null, values));
//        Assert.assertNull(value.getParent());
//        Assert.assertFalse(parent.getAnnotations().contains(value));
//    }

//    @Test
//    public void SingleList_NotAnnotating_Insert()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AnnotationInstance value = newDocumentation("sA");
//        AbstractClassifierInstance values = new ArrayList<INode>() { value };
//        Assert.assertThrows(IllegalArgumentException.class, () -> parent.InsertAnnotations(0, values));
//        Assert.assertNull(value.getParent());
//        Assert.assertFalse(parent.getAnnotations().contains(value));
//    }

//    @Test
//    public void SingleList_NotAnnotating_Remove()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AnnotationInstance value = newDocumentation("sA");
//        AbstractClassifierInstance values = new ArrayList<INode>() { value };
//        parent.RemoveAnnotations(values);
//        Assert.assertNull(value.getParent());
//        Assert.assertFalse(parent.getAnnotations().contains(value));
//    }

//    @Test
//    public void Result_Reflective()
//    {
//        AbstractClassifierInstance parent = newLine("g");
//        AnnotationInstance valueA = newBillOfMaterials("sA");
//        AnnotationInstance valueB = newBillOfMaterials("sB");
//        AnnotationInstance[] values = new AnnotationInstance[]{ valueA, valueB };
//        parent.Set(null, values);
//        AbstractClassifierInstance result = parent.Get(null);
//        CollectionAssert.assertEquals(new List<INode>() {valueA, valueB}, (result as IList<INode>).ToList());
//    }

    @Test
    public void ResultUnmodifiable_Set()
    {
        AbstractClassifierInstance parent = newLine("g");
        AnnotationInstance valueA = newBillOfMaterials("sA");
        AnnotationInstance valueB = newBillOfMaterials("sB");
        AnnotationInstance[] values = new AnnotationInstance[]{ valueA, valueB };
        parent.addAnnotation(valueA);
        parent.addAnnotation(valueB);
        List result = parent.getAnnotations();
        Assert.assertThrows(UnsupportedOperationException.class, () -> result.add(valueA));
    }

    @Test
    public void ResultUnmodifiable_Unset()
    {
        AnnotationInstance valueA = newBillOfMaterials("sA");
        AbstractClassifierInstance parent = newLine("g");
        List result = parent.getAnnotations();
        Assert.assertThrows(UnsupportedOperationException.class, () -> result.add(valueA));
    }

////    #endregion
}