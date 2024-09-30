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

import io.lionweb.lioncore.java.model.impl.AbstractClassifierInstance;
import org.junit.Assert;
import org.junit.Test;

public class ReferenceTests_Multiple_Optional extends DynamicNodeTestsBase
{
////    #region Single
//
//    @Test
//    public void Single_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance line = newLine("myId");
//        Assert.assertThrows(IllegalArgumentException.class, () ->
//            parent.addReferenceValue(ReferenceGeometry_shapes(), line));
//        Assert.assertNull(line.getParent());
//        Assert.assertFalse((parent.getReferenceValues(ReferenceGeometry_shapes()).contains(line));
//    }
//
////    #endregion
//
////    #region Null
//
//    @Test
//    public void Null_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        Assert.assertThrows(IllegalArgumentException.class, () ->
//            parent.addReferenceValue(ReferenceGeometry_shapes(), null));
//    }
//
////    #endregion
//
////    #region EmptyCollection
//
//    @Test
//    public void EmptyArray_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance values = new DynamicNode[0];
//        parent.addReferenceValue(ReferenceGeometry_shapes(), values);
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).size() == 0);
//    }
//
//    @Test
//    public void EmptyUntypedList_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance values = new ArrayList();
//        parent.addReferenceValue(ReferenceGeometry_shapes(), values);
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).size() == 0);
//    }
//
//    @Test
//    public void EmptyListMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance values = new List<DynamicNode>();
//        parent.addReferenceValue(ReferenceGeometry_shapes(), values);
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).size() == 0);
//    }
//
//    @Test
//    public void EmptyListSubtype_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance values = new List<DynamicNode>();
//        parent.addReferenceValue(ReferenceGeometry_shapes(), values);
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).size() == 0);
//    }
//
//    @Test
//    public void EmptySet_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance values = new HashSet<DynamicNode>();
//        parent.addReferenceValue(ReferenceGeometry_shapes(), values);
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).size() == 0);
//    }
//
//    @Test
//    public void EmptyListNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance values = new List<string>();
//        parent.addReferenceValue(ReferenceGeometry_shapes(), values);
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).size() == 0);
//    }
//
//    @Test
//    public void EmptyList_Reset_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        parent.addReferenceValue(ReferenceGeometry_shapes(), new List<DynamicNode> { newLine("myId") });
//        AbstractClassifierInstance values = new List<DynamicNode>();
//        parent.addReferenceValue(ReferenceGeometry_shapes(), values);
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).size() == 0);
//    }
//
////    #endregion
//
////    #region NullCollection
//
//    @Test
//    public void NullArray_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance values = new DynamicNode[] { null };
//        Assert.assertThrows(IllegalArgumentException.class,
//            () -> parent.addReferenceValue(ReferenceGeometry_shapes(), values));
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).size() == 0);
//    }
//
//    @Test
//    public void NullUntypedList_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance values = new ArrayList() { null };
//        Assert.assertThrows(IllegalArgumentException.class,
//            () -> parent.addReferenceValue(ReferenceGeometry_shapes(), values));
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).size() == 0);
//    }
//
//    @Test
//    public void NullListMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance values = new List<DynamicNode>() { null };
//        Assert.assertThrows(IllegalArgumentException.class,
//            () -> parent.addReferenceValue(ReferenceGeometry_shapes(), values));
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).size() == 0);
//    }
//
//    @Test
//    public void NullListSubtype_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance values = new List<DynamicNode>() { null };
//        Assert.assertThrows(IllegalArgumentException.class,
//            () -> parent.addReferenceValue(ReferenceGeometry_shapes(), values));
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).size() == 0);
//    }
//
//    @Test
//    public void NullListNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance values = new List<string>() { null };
//        Assert.assertThrows(IllegalArgumentException.class,
//            () -> parent.addReferenceValue(ReferenceGeometry_shapes(), values));
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).size() == 0);
//    }
//
//    @Test
//    public void NullSet_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance values = new HashSet<DynamicNode>() { null };
//        Assert.assertThrows(IllegalArgumentException.class,
//            () -> parent.addReferenceValue(ReferenceGeometry_shapes(), values));
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).size() == 0);
//    }
//
////    #endregion
//
////    #region SingleCollection
//
//    @Test
//    public void SingleArray_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance value = newLine("s");
//        AbstractClassifierInstance values = new DynamicNode[] { value };
//        parent.addReferenceValue(ReferenceGeometry_shapes(), values);
//        Assert.assertNull(value.getParent());
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).contains(value));
//    }
//
//    @Test
//    public void SingleUntypedArray_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance value = newLine("s");
//        AbstractClassifierInstance values = new object[] { value };
//        parent.addReferenceValue(ReferenceGeometry_shapes(), values);
//        Assert.assertNull(value.getParent());
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).contains(value));
//    }
//
//    @Test
//    public void SingleUntypedList_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance value = newLine("s");
//        AbstractClassifierInstance values = new ArrayList() { value };
//        parent.addReferenceValue(ReferenceGeometry_shapes(), values);
//        Assert.assertNull(value.getParent());
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).contains(value));
//    }
//
//    @Test
//    public void SingleListMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance value = newLine("s");
//        AbstractClassifierInstance values = new List<DynamicNode>() { value };
//        parent.addReferenceValue(ReferenceGeometry_shapes(), values);
//        Assert.assertNull(value.getParent());
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).contains(value));
//    }
//
//    @Test
//    public void SingleListSubtype_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance value = newLine("s");
//        AbstractClassifierInstance values = new List<DynamicNode>() { value };
//        parent.addReferenceValue(ReferenceGeometry_shapes(), values);
//        Assert.assertNull(value.getParent());
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).contains(value));
//    }
//
//    @Test
//    public void SingleSet_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance value = newLine("s");
//        AbstractClassifierInstance values = new HashSet<DynamicNode>() { value };
//        parent.addReferenceValue(ReferenceGeometry_shapes(), values);
//        Assert.assertNull(value.getParent());
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).contains(value));
//    }
//
//    @Test
//    public void SingleListNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance value = newCoord("c");
//        AbstractClassifierInstance values = new List<DynamicNode>() { value };
//        Assert.assertThrows(IllegalArgumentException.class,
//            () -> parent.addReferenceValue(ReferenceGeometry_shapes(), values));
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).size() == 0);
//    }
//
//    @Test
//    public void SingleUntypedListNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance value = newCoord("c");
//        AbstractClassifierInstance values = new ArrayList() { value };
//        Assert.assertThrows(IllegalArgumentException.class,
//            () -> parent.addReferenceValue(ReferenceGeometry_shapes(), values));
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).size() == 0);
//    }
//
//    @Test
//    public void SingleUntypedArrayNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance value = newCoord("c");
//        AbstractClassifierInstance values = new object[] { value };
//        Assert.assertThrows(IllegalArgumentException.class,
//            () -> parent.addReferenceValue(ReferenceGeometry_shapes(), values));
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).size() == 0);
//    }
//
////    #endregion
//
////    #region MultipleCollection
//
//    @Test
//    public void MultipleArray_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance valueA = newLine("sA");
//        AbstractClassifierInstance valueB = newLine("sB");
//        AbstractClassifierInstance values = new DynamicNode[] { valueA, valueB };
//        parent.addReferenceValue(ReferenceGeometry_shapes(), values);
//        Assert.assertNull(valueA.getParent());
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).contains(valueA));
//        Assert.assertNull(valueB.getParent());
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).contains(valueB));
//    }
//
//    @Test
//    public void MultipleUntypedArray_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance valueA = newLine("sA");
//        AbstractClassifierInstance valueB = newLine("sB");
//        AbstractClassifierInstance values = new object[] { valueA, valueB };
//        parent.addReferenceValue(ReferenceGeometry_shapes(), values);
//        Assert.assertNull(valueA.getParent());
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).contains(valueA));
//        Assert.assertNull(valueB.getParent());
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).contains(valueB));
//    }
//
//    @Test
//    public void MultipleUntypedList_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance valueA = newLine("sA");
//        AbstractClassifierInstance valueB = newLine("sB");
//        AbstractClassifierInstance values = new ArrayList() { valueA, valueB };
//        parent.addReferenceValue(ReferenceGeometry_shapes(), values);
//        Assert.assertNull(valueA.getParent());
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).contains(valueA));
//        Assert.assertNull(valueB.getParent());
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).contains(valueB));
//    }
//
//    @Test
//    public void MultipleListMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance valueA = newLine("sA");
//        AbstractClassifierInstance valueB = newLine("sB");
//        AbstractClassifierInstance values = new List<DynamicNode>() { valueA, valueB };
//        parent.addReferenceValue(ReferenceGeometry_shapes(), values);
//        Assert.assertNull(valueA.getParent());
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).contains(valueA));
//        Assert.assertNull(valueB.getParent());
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).contains(valueB));
//    }
//
//    @Test
//    public void MultipleListSubtype_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance valueA = newLine("sA");
//        AbstractClassifierInstance valueB = newLine("sB");
//        AbstractClassifierInstance values = new List<DynamicNode>() { valueA, valueB };
//        parent.addReferenceValue(ReferenceGeometry_shapes(), values);
//        Assert.assertNull(valueA.getParent());
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).contains(valueA));
//        Assert.assertNull(valueB.getParent());
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).contains(valueB));
//    }
//
//    @Test
//    public void MultipleSet_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance valueA = newLine("sA");
//        AbstractClassifierInstance valueB = newLine("sB");
//        AbstractClassifierInstance values = new HashSet<DynamicNode>() { valueA, valueB };
//        parent.addReferenceValue(ReferenceGeometry_shapes(), values);
//        Assert.assertNull(valueA.getParent());
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).contains(valueA));
//        Assert.assertNull(valueB.getParent());
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).contains(valueB));
//    }
//
//    @Test
//    public void MultipleSingleEnumerable_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance valueA = newLine("sA");
//        AbstractClassifierInstance valueB = newLine("sB");
//        AbstractClassifierInstance values = new SingleEnumerable<DynamicNode>() { valueA, valueB };
//        parent.addReferenceValue(ReferenceGeometry_shapes(), values);
//        Assert.assertNull(valueA.getParent());
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).contains(valueA));
//        Assert.assertNull(valueB.getParent());
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).contains(valueB));
//    }
//
//    @Test
//    public void MultipleListNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance valueA = newCoord("cA");
//        AbstractClassifierInstance valueB = newCoord("cB");
//        AbstractClassifierInstance values = new List<DynamicNode>() { valueA, valueB };
//        Assert.assertThrows(IllegalArgumentException.class,
//            () -> parent.addReferenceValue(ReferenceGeometry_shapes(), values));
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).size() == 0);
//    }
//
//    @Test
//    public void MultipleUntypedListNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance valueA = newCoord("cA");
//        AbstractClassifierInstance valueB = newCoord("cB");
//        AbstractClassifierInstance values = new ArrayList() { valueA, valueB };
//        Assert.assertThrows(IllegalArgumentException.class,
//            () -> parent.addReferenceValue(ReferenceGeometry_shapes(), values));
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).size() == 0);
//    }
//
//    @Test
//    public void MultipleUntypedArrayNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance valueA = newCoord("cA");
//        AbstractClassifierInstance valueB = newCoord("cB");
//        AbstractClassifierInstance values = new object[] { valueA, valueB };
//        Assert.assertThrows(IllegalArgumentException.class,
//            () -> parent.addReferenceValue(ReferenceGeometry_shapes(), values));
//        Assert.assertTrue((parent.getReferenceValues(ReferenceGeometry_shapes()).size() == 0);
//    }
//
//    @Test
//    public void ResultUnmodifiable_Set()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance valueA = newLine("sA");
//        AbstractClassifierInstance valueB = newLine("sB");
//        AbstractClassifierInstance values = new DynamicNode[] { valueA, valueB };
//        parent.addReferenceValue(ReferenceGeometry_shapes(), values);
//        AbstractClassifierInstance result = parent.getReferenceValues(ReferenceGeometry_shapes());
//        Assert.IsInstanceOfType<IReadOnlyList<INode>>(result);
//    }
//
//    @Test
//    public void ResultUnmodifiable_Unset()
//    {
//        AbstractClassifierInstance parent = newReferenceGeometry("g");
//        AbstractClassifierInstance result = parent.getReferenceValues(ReferenceGeometry_shapes());
//        Assert.IsInstanceOfType<IReadOnlyList<INode>>(result);
//    }
//
////    #endregion
}