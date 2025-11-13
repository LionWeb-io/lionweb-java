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

public class ContainmentTests_Single_Required extends DynamicNodeTestsBase
{
//    #region Single

    @Test
    public void Single_Reflective()
    {
        AbstractClassifierInstance parent = newOffsetDuplicate("od");
        Node coord = newCoord("myId");
        parent.addChild(OffsetDuplicate_offset(), coord);
        Assert.assertSame(parent, coord.getParent());
        Assert.assertSame(coord, parent.getChildren(OffsetDuplicate_offset()));
    }

    @Test
    public void Existing_Reflective()
    {
        Node oldCoord = newCoord("old");
        AbstractClassifierInstance parent = newOffsetDuplicate("g");
        parent.addChild(OffsetDuplicate_offset(), oldCoord);
        Node coord = newCoord("myId");
        parent.addChild(OffsetDuplicate_offset(), coord);
        Assert.assertNull(oldCoord.getParent());
        Assert.assertSame(parent, coord.getParent());
        Assert.assertSame(coord, parent.getChildren(OffsetDuplicate_offset()));
    }

//    #endregion

//    #region Null

    @Test
    public void Null_Reflective()
    {
        AbstractClassifierInstance parent = newOffsetDuplicate("od");
        Assert.assertThrows(IllegalArgumentException.class, 
            () -> parent.addChild(OffsetDuplicate_offset(), null));
        Assert.assertThrows(IllegalArgumentException.class, () -> parent.getChildren(OffsetDuplicate_offset()));
    }

//    #endregion

//    #region EmptyCollection

//    @Test
//    public void EmptyArray_Reflective()
//    {
//        AbstractClassifierInstance parent = newOffsetDuplicate("od");
//        AbstractClassifierInstance values = new DynamicNode[0];
//        Assert.assertThrows(IllegalArgumentException.class, () =>
//            parent.addChild(OffsetDuplicate_offset(), values));
//    }
//
//    @Test
//    public void EmptyUntypedList_Reflective()
//    {
//        AbstractClassifierInstance parent = newOffsetDuplicate("od");
//        AbstractClassifierInstance values = new ArrayList();
//        Assert.assertThrows(IllegalArgumentException.class, () =>
//            parent.addChild(OffsetDuplicate_offset(), values));
//    }
//
//    @Test
//    public void EmptyListMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newOffsetDuplicate("od");
//        AbstractClassifierInstance values = new List<DynamicNode>();
//        Assert.assertThrows(IllegalArgumentException.class, () =>
//            parent.addChild(OffsetDuplicate_offset(), values));
//    }
//
//    @Test
//    public void EmptySet_Reflective()
//    {
//        AbstractClassifierInstance parent = newOffsetDuplicate("od");
//        AbstractClassifierInstance values = new HashSet<DynamicNode>();
//        Assert.assertThrows(IllegalArgumentException.class, () =>
//            parent.addChild(OffsetDuplicate_offset(), values));
//    }
//
//    @Test
//    public void EmptyListNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newOffsetDuplicate("od");
//        AbstractClassifierInstance values = new List<string>();
//        Assert.assertThrows(IllegalArgumentException.class, () =>
//            parent.addChild(OffsetDuplicate_offset(), values));
//    }

//    #endregion

//    #region NullCollection

//    @Test
//    public void NullArray_Reflective()
//    {
//        AbstractClassifierInstance parent = newOffsetDuplicate("od");
//        AbstractClassifierInstance values = new DynamicNode[] { null };
//        Assert.assertThrows(IllegalArgumentException.class, () =>
//            parent.addChild(OffsetDuplicate_offset(), values));
//    }
//
//    @Test
//    public void NullUntypedList_Reflective()
//    {
//        AbstractClassifierInstance parent = newOffsetDuplicate("od");
//        AbstractClassifierInstance values = new ArrayList() { null };
//        Assert.assertThrows(IllegalArgumentException.class,
//            () -> parent.addChild(OffsetDuplicate_offset(), values));
//    }
//
//    @Test
//    public void NullListMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newOffsetDuplicate("od");
//        AbstractClassifierInstance values = new List<DynamicNode>() { null };
//        Assert.assertThrows(IllegalArgumentException.class,
//            () -> parent.addChild(OffsetDuplicate_offset(), values));
//    }
//
//    @Test
//    public void NullListNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newOffsetDuplicate("od");
//        AbstractClassifierInstance values = new List<DynamicNode>() { null };
//        Assert.assertThrows(IllegalArgumentException.class,
//            () -> parent.addChild(OffsetDuplicate_offset(), values));
//    }
//
//    @Test
//    public void NullSet_Reflective()
//    {
//        AbstractClassifierInstance parent = newOffsetDuplicate("od");
//        AbstractClassifierInstance values = new HashSet<DynamicNode>() { null };
//        Assert.assertThrows(IllegalArgumentException.class, () =>
//            parent.addChild(OffsetDuplicate_offset(), values));
//    }

//    #endregion

//    #region SingleCollection

//    @Test
//    public void SingleArray_Reflective()
//    {
//        AbstractClassifierInstance parent = newOffsetDuplicate("od");
//        AbstractClassifierInstance value = newCoord("s");
//        AbstractClassifierInstance values = new DynamicNode[] { value };
//
//        Assert.assertThrows(IllegalArgumentException.class, () =>
//            parent.addChild(OffsetDuplicate_offset(), values));
//        Assert.assertThrows(IllegalArgumentException.class, () -> parent.getChildren(OffsetDuplicate_offset()));
//        Assert.assertNull(value.getParent());
//    }
//
//    @Test
//    public void SingleUntypedArray_Reflective()
//    {
//        AbstractClassifierInstance parent = newOffsetDuplicate("od");
//        AbstractClassifierInstance value = newCoord("s");
//        AbstractClassifierInstance values = new object[] { value };
//        Assert.assertThrows(IllegalArgumentException.class, () =>
//            parent.addChild(OffsetDuplicate_offset(), values));
//        Assert.assertThrows(IllegalArgumentException.class, () -> parent.getChildren(OffsetDuplicate_offset()));
//        Assert.assertNull(value.getParent());
//    }
//
//    @Test
//    public void SingleUntypedList_Reflective()
//    {
//        AbstractClassifierInstance parent = newOffsetDuplicate("od");
//        AbstractClassifierInstance value = newCoord("s");
//        AbstractClassifierInstance values = new ArrayList() { value };
//        Assert.assertThrows(IllegalArgumentException.class, () =>
//            parent.addChild(OffsetDuplicate_offset(), values));
//        Assert.assertThrows(IllegalArgumentException.class, () -> parent.getChildren(OffsetDuplicate_offset()));
//        Assert.assertNull(value.getParent());
//    }
//
//    @Test
//    public void SingleListMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newOffsetDuplicate("od");
//        AbstractClassifierInstance value = newCoord("s");
//        AbstractClassifierInstance values = new List<DynamicNode>() { value };
//        Assert.assertThrows(IllegalArgumentException.class, () =>
//            parent.addChild(OffsetDuplicate_offset(), values));
//        Assert.assertThrows(IllegalArgumentException.class, () -> parent.getChildren(OffsetDuplicate_offset()));
//        Assert.assertNull(value.getParent());
//    }
//
//    @Test
//    public void SingleSet_Reflective()
//    {
//        AbstractClassifierInstance parent = newOffsetDuplicate("od");
//        AbstractClassifierInstance value = newCoord("s");
//        AbstractClassifierInstance values = new HashSet<DynamicNode>() { value };
//        Assert.assertThrows(IllegalArgumentException.class, () =>
//            parent.addChild(OffsetDuplicate_offset(), values));
//        Assert.assertThrows(IllegalArgumentException.class, () -> parent.getChildren(OffsetDuplicate_offset()));
//        Assert.assertNull(value.getParent());
//    }
//
//    @Test
//    public void SingleListNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newOffsetDuplicate("od");
//        AbstractClassifierInstance value = newLine("c");
//        AbstractClassifierInstance values = new List<DynamicNode>() { value };
//        Assert.assertThrows(IllegalArgumentException.class,
//            () -> parent.addChild(OffsetDuplicate_offset(), values));
//    }
//
//    @Test
//    public void SingleUntypedListNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newOffsetDuplicate("od");
//        AbstractClassifierInstance value = newCoord("c");
//        AbstractClassifierInstance values = new ArrayList() { value };
//        Assert.assertThrows(IllegalArgumentException.class,
//            () -> parent.addChild(OffsetDuplicate_offset(), values));
//    }
//
//    @Test
//    public void SingleUntypedArrayNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newOffsetDuplicate("od");
//        AbstractClassifierInstance value = newCoord("c");
//        AbstractClassifierInstance values = new object[] { value };
//        Assert.assertThrows(IllegalArgumentException.class,
//            () -> parent.addChild(OffsetDuplicate_offset(), values));
//    }

//    #endregion

//    #region MultipleCollection

//    @Test
//    public void MultipleArray_Reflective()
//    {
//        AbstractClassifierInstance parent = newOffsetDuplicate("od");
//        AbstractClassifierInstance valueA = newCoord("sA");
//        AbstractClassifierInstance valueB = newCoord("sB");
//        AbstractClassifierInstance values = new DynamicNode[] { valueA, valueB };
//        Assert.assertThrows(IllegalArgumentException.class, () =>
//            parent.addChild(OffsetDuplicate_offset(), values));
//        Assert.assertThrows(IllegalArgumentException.class, () -> parent.getChildren(OffsetDuplicate_offset()));
//        Assert.assertNull(valueA.getParent());
//        Assert.assertNull(valueB.getParent());
//    }
//
//    @Test
//    public void MultipleUntypedArray_Reflective()
//    {
//        AbstractClassifierInstance parent = newOffsetDuplicate("od");
//        AbstractClassifierInstance valueA = newCoord("sA");
//        AbstractClassifierInstance valueB = newCoord("sB");
//        AbstractClassifierInstance values = new object[] { valueA, valueB };
//        Assert.assertThrows(IllegalArgumentException.class, () =>
//            parent.addChild(OffsetDuplicate_offset(), values));
//        Assert.assertThrows(IllegalArgumentException.class, () -> parent.getChildren(OffsetDuplicate_offset()));
//        Assert.assertNull(valueA.getParent());
//        Assert.assertNull(valueB.getParent());
//    }
//
//    @Test
//    public void MultipleUntypedList_Reflective()
//    {
//        AbstractClassifierInstance parent = newOffsetDuplicate("od");
//        AbstractClassifierInstance valueA = newCoord("sA");
//        AbstractClassifierInstance valueB = newCoord("sB");
//        AbstractClassifierInstance values = new ArrayList() { valueA, valueB };
//        Assert.assertThrows(IllegalArgumentException.class, () =>
//            parent.addChild(OffsetDuplicate_offset(), values));
//        Assert.assertThrows(IllegalArgumentException.class, () -> parent.getChildren(OffsetDuplicate_offset()));
//        Assert.assertNull(valueA.getParent());
//        Assert.assertNull(valueB.getParent());
//    }
//
//    @Test
//    public void MultipleListMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newOffsetDuplicate("od");
//        AbstractClassifierInstance valueA = newCoord("sA");
//        AbstractClassifierInstance valueB = newCoord("sB");
//        AbstractClassifierInstance values = new List<DynamicNode>() { valueA, valueB };
//        Assert.assertThrows(IllegalArgumentException.class, () =>
//            parent.addChild(OffsetDuplicate_offset(), values));
//        Assert.assertThrows(IllegalArgumentException.class, () -> parent.getChildren(OffsetDuplicate_offset()));
//        Assert.assertNull(valueA.getParent());
//        Assert.assertNull(valueB.getParent());
//    }
//
//    @Test
//    public void MultipleSet_Reflective()
//    {
//        AbstractClassifierInstance parent = newOffsetDuplicate("od");
//        AbstractClassifierInstance valueA = newCoord("sA");
//        AbstractClassifierInstance valueB = newCoord("sB");
//        AbstractClassifierInstance values = new HashSet<DynamicNode>() { valueA, valueB };
//        Assert.assertThrows(IllegalArgumentException.class, () =>
//            parent.addChild(OffsetDuplicate_offset(), values));
//        Assert.assertThrows(IllegalArgumentException.class, () -> parent.getChildren(OffsetDuplicate_offset()));
//        Assert.assertNull(valueA.getParent());
//        Assert.assertNull(valueB.getParent());
//    }
//
//    @Test
//    public void MultipleListNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newOffsetDuplicate("od");
//        AbstractClassifierInstance valueA = newLine("cA");
//        AbstractClassifierInstance valueB = newLine("cB");
//        AbstractClassifierInstance values = new List<DynamicNode>() { valueA, valueB };
//        Assert.assertThrows(IllegalArgumentException.class,
//            () -> parent.addChild(OffsetDuplicate_offset(), values));
//        Assert.assertThrows(IllegalArgumentException.class, () -> parent.getChildren(OffsetDuplicate_offset()));
//        Assert.assertNull(valueA.getParent());
//        Assert.assertNull(valueB.getParent());
//    }
//
//    @Test
//    public void MultipleUntypedListNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newOffsetDuplicate("od");
//        AbstractClassifierInstance valueA = newLine("cA");
//        AbstractClassifierInstance valueB = newLine("cB");
//        AbstractClassifierInstance values = new ArrayList() { valueA, valueB };
//        Assert.assertThrows(IllegalArgumentException.class,
//            () -> parent.addChild(OffsetDuplicate_offset(), values));
//        Assert.assertThrows(IllegalArgumentException.class, () -> parent.getChildren(OffsetDuplicate_offset()));
//        Assert.assertNull(valueA.getParent());
//        Assert.assertNull(valueB.getParent());
//    }
//
//    @Test
//    public void MultipleUntypedArrayNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newOffsetDuplicate("od");
//        AbstractClassifierInstance valueA = newLine("cA");
//        AbstractClassifierInstance valueB = newLine("cB");
//        AbstractClassifierInstance values = new object[] { valueA, valueB };
//        Assert.assertThrows(IllegalArgumentException.class,
//            () -> parent.addChild(OffsetDuplicate_offset(), values));
//        Assert.assertThrows(IllegalArgumentException.class, () -> parent.getChildren(OffsetDuplicate_offset()));
//        Assert.assertNull(valueA.getParent());
//        Assert.assertNull(valueB.getParent());
//    }

//    #endregion
}