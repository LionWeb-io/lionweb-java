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

import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.impl.AbstractClassifierInstance;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class ContainmentTests_Multiple_Required extends DynamicNodeTestsBase
{
//    #region Single

    @Test
    public void Single_Reflective()
    {
        AbstractClassifierInstance parent = newCompositeShape("cs");
        Node line = newLine("myId");
        Assert.assertThrows(IllegalArgumentException.class, () -> parent.addChild(CompositeShape_parts(), line));
        Assert.assertSame(null, line.getParent());
        Assert.assertThrows(IllegalArgumentException.class, () -> parent.getChildren(CompositeShape_parts()).contains(line));
    }

//    #endregion

//    #region Null

    @Test
    public void Null_Reflective()
    {
        AbstractClassifierInstance parent = newCompositeShape("cs");
        Assert.assertThrows(IllegalArgumentException.class, () -> parent.addChild(CompositeShape_parts(), null));
    }

//    #endregion

//    #region EmptyCollection

//    @Test
//    public void EmptyArray_Reflective()
//    {
//        AbstractClassifierInstance parent = newCompositeShape("cs");
//        AbstractClassifierInstance values = new DynamicNode[0];
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> parent.addChild(CompositeShape_parts(), values));
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> (parent.Get(CompositeShape_parts()).size() == 0);
//    }
//
//    @Test
//    public void EmptyUntypedList_Reflective()
//    {
//        AbstractClassifierInstance parent = newCompositeShape("cs");
//        AbstractClassifierInstance values = new ArrayList();
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> parent.addChild(CompositeShape_parts(), values));
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> (parent.Get(CompositeShape_parts()).size() == 0);
//    }
//
//    @Test
//    public void EmptyListMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newCompositeShape("cs");
//        AbstractClassifierInstance values = new List<DynamicNode>();
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> parent.addChild(CompositeShape_parts(), values));
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> (parent.Get(CompositeShape_parts()).size() == 0);
//    }
//
//    @Test
//    public void EmptyListSubtype_Reflective()
//    {
//        AbstractClassifierInstance parent = newCompositeShape("cs");
//        AbstractClassifierInstance values = new List<INode>();
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> parent.addChild(CompositeShape_parts(), values));
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> (parent.Get(CompositeShape_parts()).size() == 0);
//    }
//
//    @Test
//    public void EmptySet_Reflective()
//    {
//        AbstractClassifierInstance parent = newCompositeShape("cs");
//        AbstractClassifierInstance values = new HashSet<DynamicNode>();
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> parent.addChild(CompositeShape_parts(), values));
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> (parent.Get(CompositeShape_parts()).size() == 0);
//    }
//
//    @Test
//    public void EmptyListNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newCompositeShape("cs");
//        AbstractClassifierInstance values = new List<string>();
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> parent.addChild(CompositeShape_parts(), values));
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> (parent.Get(CompositeShape_parts()).size() == 0);
//    }
//
//    @Test
//    public void EmptyList_Reset_Reflective()
//    {
//        AbstractClassifierInstance parent = newCompositeShape("cs");
//        AbstractClassifierInstance value = newCircle("myId");
//        parent.addChild(CompositeShape_parts(), new List<DynamicNode> { value });
//        AbstractClassifierInstance values = new List<DynamicNode>();
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> parent.addChild(CompositeShape_parts(), values));
//        CollectionAssert.assertEquals(new List<DynamicNode> { value },
//            (parent.Get(CompositeShape_parts()).ToList());
//    }

//    #endregion

//    #region NullCollection

//    @Test
//    public void NullArray_Reflective()
//    {
//        AbstractClassifierInstance parent = newCompositeShape("cs");
//        AbstractClassifierInstance values = new DynamicNode[] { null };
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> parent.addChild(CompositeShape_parts(), values));
//        Assert.assertThrows(IllegalArgumentException.class, () =>
//            (parent.Get(CompositeShape_parts()).size() == 0);
//    }
//
//    @Test
//    public void NullUntypedList_Reflective()
//    {
//        AbstractClassifierInstance parent = newCompositeShape("cs");
//        AbstractClassifierInstance values = new ArrayList() { null };
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> parent.addChild(CompositeShape_parts(), values));
//        Assert.assertThrows(IllegalArgumentException.class, () =>
//            (parent.Get(CompositeShape_parts()).size() == 0);
//    }
//
//    @Test
//    public void NullListMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newCompositeShape("cs");
//        AbstractClassifierInstance values = new List<DynamicNode>() { null };
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> parent.addChild(CompositeShape_parts(), values));
//        Assert.assertThrows(IllegalArgumentException.class, () =>
//            (parent.Get(CompositeShape_parts()).size() == 0);
//    }
//
//    @Test
//    public void NullListSubtype_Reflective()
//    {
//        AbstractClassifierInstance parent = newCompositeShape("cs");
//        AbstractClassifierInstance values = new List<INode>() { null };
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> parent.addChild(CompositeShape_parts(), values));
//        Assert.assertThrows(IllegalArgumentException.class, () =>
//            (parent.Get(CompositeShape_parts()).size() == 0);
//    }
//
//    @Test
//    public void NullListNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newCompositeShape("cs");
//        AbstractClassifierInstance values = new List<string>() { null };
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> parent.addChild(CompositeShape_parts(), values));
//        Assert.assertThrows(IllegalArgumentException.class, () =>
//            (parent.Get(CompositeShape_parts()).size() == 0);
//    }
//
//    @Test
//    public void NullSet_Reflective()
//    {
//        AbstractClassifierInstance parent = newCompositeShape("cs");
//        AbstractClassifierInstance values = new HashSet<DynamicNode>() { null };
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> parent.addChild(CompositeShape_parts(), values));
//        Assert.assertThrows(IllegalArgumentException.class, () =>
//            (parent.Get(CompositeShape_parts()).size() == 0);
//    }

//    #endregion

//    #region SingleCollection

//    @Test
//    public void SingleArray_Reflective()
//    {
//        AbstractClassifierInstance parent = newCompositeShape("cs");
//        AbstractClassifierInstance value = newLine("s");
//        AbstractClassifierInstance values = new DynamicNode[] { value };
//        parent.addChild(CompositeShape_parts(), values);
//        Assert.assertSame(parent, value.getParent());
//        Assert.assertTrue((parent.Get(CompositeShape_parts()).contains(value));
//    }
//
//    @Test
//    public void SingleArray_Existing_Reflective()
//    {
//        AbstractClassifierInstance circle = newCircle("cc");
//        AbstractClassifierInstance parent = newCompositeShape("g");
//        parent.addChild(CompositeShape_parts(), new DynamicNode[] { circle });
//        AbstractClassifierInstance value = newLine("s");
//        AbstractClassifierInstance values = new DynamicNode[] { value };
//        parent.addChild(CompositeShape_parts(), values);
//        Assert.assertNull(circle.getParent());
//        Assert.assertSame(parent, value.getParent());
//        CollectionAssert.assertEquals(new List<DynamicNode> { value },
//            (parent.Get(CompositeShape_parts()).ToList());
//    }
//
//    @Test
//    public void SingleUntypedArray_Reflective()
//    {
//        AbstractClassifierInstance parent = newCompositeShape("cs");
//        AbstractClassifierInstance value = newLine("s");
//        AbstractClassifierInstance values = new object[] { value };
//        parent.addChild(CompositeShape_parts(), values);
//        Assert.assertSame(parent, value.getParent());
//        Assert.assertTrue((parent.Get(CompositeShape_parts()).contains(value));
//    }
//
//    @Test
//    public void SingleUntypedList_Reflective()
//    {
//        AbstractClassifierInstance parent = newCompositeShape("cs");
//        AbstractClassifierInstance value = newLine("s");
//        AbstractClassifierInstance values = new ArrayList() { value };
//        parent.addChild(CompositeShape_parts(), values);
//        Assert.assertSame(parent, value.getParent());
//        Assert.assertTrue((parent.Get(CompositeShape_parts()).contains(value));
//    }
//
//    @Test
//    public void SingleListMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newCompositeShape("cs");
//        AbstractClassifierInstance value = newLine("s");
//        AbstractClassifierInstance values = new List<DynamicNode>() { value };
//        parent.addChild(CompositeShape_parts(), values);
//        Assert.assertSame(parent, value.getParent());
//        Assert.assertTrue((parent.Get(CompositeShape_parts()).contains(value));
//    }
//
//    @Test
//    public void SingleListSubtype_Reflective()
//    {
//        AbstractClassifierInstance parent = newCompositeShape("cs");
//        AbstractClassifierInstance value = newLine("s");
//        AbstractClassifierInstance values = new List<INode>() { value };
//        parent.addChild(CompositeShape_parts(), values);
//        Assert.assertSame(parent, value.getParent());
//        Assert.assertTrue((parent.Get(CompositeShape_parts()).contains(value));
//    }
//
//    @Test
//    public void SingleSet_Reflective()
//    {
//        AbstractClassifierInstance parent = newCompositeShape("cs");
//        AbstractClassifierInstance value = newLine("s");
//        AbstractClassifierInstance values = new HashSet<DynamicNode>() { value };
//        parent.addChild(CompositeShape_parts(), values);
//        Assert.assertSame(parent, value.getParent());
//        Assert.assertTrue((parent.Get(CompositeShape_parts()).contains(value));
//    }
//
//    @Test
//    public void SingleListNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newCompositeShape("cs");
//        AbstractClassifierInstance value = "c";
//        AbstractClassifierInstance values = new List<string>() { value };
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> parent.addChild(CompositeShape_parts(), values));
//        Assert.assertThrows(IllegalArgumentException.class, () =>
//            (parent.Get(CompositeShape_parts()).size() == 0);
//    }
//
//    @Test
//    public void SingleUntypedListNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newCompositeShape("cs");
//        AbstractClassifierInstance value = newCoord("c");
//        AbstractClassifierInstance values = new ArrayList() { value };
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> parent.addChild(CompositeShape_parts(), values));
//        Assert.assertThrows(IllegalArgumentException.class, () =>
//            (parent.Get(CompositeShape_parts()).size() == 0);
//    }
//
//    @Test
//    public void SingleUntypedArrayNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newCompositeShape("cs");
//        AbstractClassifierInstance value = newCoord("c");
//        AbstractClassifierInstance values = new object[] { value };
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> parent.addChild(CompositeShape_parts(), values));
//        Assert.assertThrows(IllegalArgumentException.class, () =>
//            (parent.Get(CompositeShape_parts()).size() == 0);
//    }

//    #endregion

//    #region MultipleCollection

//    @Test
//    public void MultipleArray_Reflective()
//    {
//        AbstractClassifierInstance parent = newCompositeShape("cs");
//        AbstractClassifierInstance valueA = newLine("sA");
//        AbstractClassifierInstance valueB = newLine("sB");
//        AbstractClassifierInstance values = new DynamicNode[] { valueA, valueB };
//        parent.addChild(CompositeShape_parts(), values);
//        Assert.assertSame(parent, valueA.getParent());
//        Assert.assertTrue((parent.Get(CompositeShape_parts()).contains(valueA));
//        Assert.assertSame(parent, valueB.getParent());
//        Assert.assertTrue((parent.Get(CompositeShape_parts()).contains(valueB));
//    }
//
//    @Test
//    public void MultipleUntypedArray_Reflective()
//    {
//        AbstractClassifierInstance parent = newCompositeShape("cs");
//        AbstractClassifierInstance valueA = newLine("sA");
//        AbstractClassifierInstance valueB = newLine("sB");
//        AbstractClassifierInstance values = new object[] { valueA, valueB };
//        parent.addChild(CompositeShape_parts(), values);
//        Assert.assertSame(parent, valueA.getParent());
//        Assert.assertTrue((parent.Get(CompositeShape_parts()).contains(valueA));
//        Assert.assertSame(parent, valueB.getParent());
//        Assert.assertTrue((parent.Get(CompositeShape_parts()).contains(valueB));
//    }
//
//    @Test
//    public void MultipleUntypedList_Reflective()
//    {
//        AbstractClassifierInstance parent = newCompositeShape("cs");
//        AbstractClassifierInstance valueA = newLine("sA");
//        AbstractClassifierInstance valueB = newLine("sB");
//        AbstractClassifierInstance values = new ArrayList() { valueA, valueB };
//        parent.addChild(CompositeShape_parts(), values);
//        Assert.assertSame(parent, valueA.getParent());
//        Assert.assertTrue((parent.Get(CompositeShape_parts()).contains(valueA));
//        Assert.assertSame(parent, valueB.getParent());
//        Assert.assertTrue((parent.Get(CompositeShape_parts()).contains(valueB));
//    }
//
//    @Test
//    public void MultipleListMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newCompositeShape("cs");
//        AbstractClassifierInstance valueA = newLine("sA");
//        AbstractClassifierInstance valueB = newLine("sB");
//        AbstractClassifierInstance values = new List<DynamicNode>() { valueA, valueB };
//        parent.addChild(CompositeShape_parts(), values);
//        Assert.assertSame(parent, valueA.getParent());
//        Assert.assertTrue((parent.Get(CompositeShape_parts()).contains(valueA));
//        Assert.assertSame(parent, valueB.getParent());
//        Assert.assertTrue((parent.Get(CompositeShape_parts()).contains(valueB));
//    }
//
//    @Test
//    public void MultipleListSubtype_Reflective()
//    {
//        AbstractClassifierInstance parent = newCompositeShape("cs");
//        AbstractClassifierInstance valueA = newLine("sA");
//        AbstractClassifierInstance valueB = newLine("sB");
//        AbstractClassifierInstance values = new List<INode>() { valueA, valueB };
//        parent.addChild(CompositeShape_parts(), values);
//        Assert.assertSame(parent, valueA.getParent());
//        Assert.assertTrue((parent.Get(CompositeShape_parts()).contains(valueA));
//        Assert.assertSame(parent, valueB.getParent());
//        Assert.assertTrue((parent.Get(CompositeShape_parts()).contains(valueB));
//    }
//
//    @Test
//    public void MultipleSet_Reflective()
//    {
//        AbstractClassifierInstance parent = newCompositeShape("cs");
//        AbstractClassifierInstance valueA = newLine("sA");
//        AbstractClassifierInstance valueB = newLine("sB");
//        AbstractClassifierInstance values = new HashSet<DynamicNode>() { valueA, valueB };
//        parent.addChild(CompositeShape_parts(), values);
//        Assert.assertSame(parent, valueA.getParent());
//        Assert.assertTrue((parent.Get(CompositeShape_parts()).contains(valueA));
//        Assert.assertSame(parent, valueB.getParent());
//        Assert.assertTrue((parent.Get(CompositeShape_parts()).contains(valueB));
//    }
//
//    @Test
//    public void MultipleSingleEnumerable_Reflective()
//    {
//        AbstractClassifierInstance parent = newCompositeShape("cs");
//        AbstractClassifierInstance valueA = newLine("sA");
//        AbstractClassifierInstance valueB = newLine("sB");
//        AbstractClassifierInstance values = new SingleEnumerable<DynamicNode>() { valueA, valueB };
//        parent.addChild(CompositeShape_parts(), values);
//        Assert.assertSame(parent, valueA.getParent());
//        Assert.assertTrue((parent.Get(CompositeShape_parts()).contains(valueA));
//        Assert.assertSame(parent, valueB.getParent());
//        Assert.assertTrue((parent.Get(CompositeShape_parts()).contains(valueB));
//    }
//
//    @Test
//    public void MultipleListNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newCompositeShape("cs");
//        AbstractClassifierInstance valueA = "cA";
//        AbstractClassifierInstance valueB = "cB";
//        AbstractClassifierInstance values = new List<string>() { valueA, valueB };
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> parent.addChild(CompositeShape_parts(), values));
//        Assert.assertThrows(IllegalArgumentException.class, () =>
//            (parent.Get(CompositeShape_parts()).size() == 0);
//    }
//
//    @Test
//    public void MultipleUntypedListNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newCompositeShape("cs");
//        AbstractClassifierInstance valueA = newCoord("cA");
//        AbstractClassifierInstance valueB = newCoord("cB");
//        AbstractClassifierInstance values = new ArrayList() { valueA, valueB };
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> parent.addChild(CompositeShape_parts(), values));
//        Assert.assertThrows(IllegalArgumentException.class, () =>
//            (parent.Get(CompositeShape_parts()).size() == 0);
//    }
//
//    @Test
//    public void MultipleUntypedArrayNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newCompositeShape("cs");
//        AbstractClassifierInstance valueA = newCoord("cA");
//        AbstractClassifierInstance valueB = newCoord("cB");
//        AbstractClassifierInstance values = new object[] { valueA, valueB };
//        Assert.assertThrows(IllegalArgumentException.class, 
//            () -> parent.addChild(CompositeShape_parts(), values));
//        Assert.assertThrows(IllegalArgumentException.class, () =>
//            (parent.Get(CompositeShape_parts()).size() == 0);
//    }

    @Test
    public void ResultUnmodifiable_Set()
    {
        AbstractClassifierInstance parent = newCompositeShape("g");
        Node valueA = newLine("sA");
        Node valueB = newLine("sB");
        parent.addChild(CompositeShape_parts(), valueA);
        parent.addChild(CompositeShape_parts(), valueB);
        List result = parent.getChildren(CompositeShape_parts());
        Assert.assertThrows(UnsupportedOperationException.class, () -> result.add(valueA));
    }

    @Test
    public void ResultUnmodifiable_Unset()
    {
        AbstractClassifierInstance parent = newCompositeShape("g");
        Assert.assertThrows(IllegalArgumentException.class, () -> parent.getChildren(CompositeShape_parts()));
    }

//    #endregion
}