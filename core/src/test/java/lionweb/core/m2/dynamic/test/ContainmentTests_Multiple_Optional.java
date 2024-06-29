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

public class ContainmentTests_Multiple_Optional extends DynamicNodeTestsBase
{
////    #region Single

    @Test
    public void Single_Reflective()
    {
        AbstractClassifierInstance parent = newGeometry("g");
        Node line = newLine("myId");
//        Assert.assertThrows(IllegalArgumentException.class, () -> parent.addChild(Geometry_shapes(), line));
//        Assert.assertSame(null, line.getParent());
//        Assert.assertFalse((parent.getChildren(Geometry_shapes())).contains(line));
        parent.addChild(Geometry_shapes(), line);
        Assert.assertSame(parent, line.getParent());
        Assert.assertTrue((parent.getChildren(Geometry_shapes())).contains(line));
    }

////    #endregion

////    #region Null

    @Test
    public void Null_Reflective()
    {
        AbstractClassifierInstance parent = newGeometry("g");
        Assert.assertThrows(IllegalArgumentException.class, () -> parent.addChild(Geometry_shapes(), null));
    }

////    #endregion

////    #region EmptyCollection

//    @Test
//    public void EmptyArray_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        Node[] values = new Node[0];
//        parent.addChild(Geometry_shapes(), values);
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).size() == 0);
//    }

//    @Test
//    public void EmptyUntypedList_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        ArrayList values = new ArrayList();
//        parent.addChild(Geometry_shapes(), values);
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).size() == 0);
//    }

//    @Test
//    public void EmptyListMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AbstractClassifierInstance values = new List<DynamicNode>();
//        parent.addChild(Geometry_shapes(), values);
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).size() == 0);
//    }

//    @Test
//    public void EmptyListSubtype_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AbstractClassifierInstance values = new List<INode>();
//        parent.addChild(Geometry_shapes(), values);
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).size() == 0);
//    }

//    @Test
//    public void EmptySet_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AbstractClassifierInstance values = new HashSet<DynamicNode>();
//        parent.addChild(Geometry_shapes(), values);
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).size() == 0);
//    }

//    @Test
//    public void EmptyListNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AbstractClassifierInstance values = new List<string>();
//        parent.addChild(Geometry_shapes(), values);
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).size() == 0);
//    }

//    @Test
//    public void EmptyList_Reset_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        parent.addChild(Geometry_shapes(), new List<DynamicNode> { newCircle("myId") });
//        AbstractClassifierInstance values = new List<DynamicNode>();
//        parent.addChild(Geometry_shapes(), values);
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).size() == 0);
//    }

////    #endregion

////    #region NullCollection

//    @Test
//    public void NullArray_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AbstractClassifierInstance values = new DynamicNode[] { null };
//        Assert.assertThrows(IllegalArgumentException.class,
//            () -> parent.addChild(Geometry_shapes(), values));
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).size() == 0);
//    }

//    @Test
//    public void NullUntypedList_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AbstractClassifierInstance values = new ArrayList() { null };
//        Assert.assertThrows(IllegalArgumentException.class,
//            () -> parent.addChild(Geometry_shapes(), values));
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).size() == 0);
//    }

//    @Test
//    public void NullListMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AbstractClassifierInstance values = new List<DynamicNode>() { null };
//        Assert.assertThrows(IllegalArgumentException.class,
//            () -> parent.addChild(Geometry_shapes(), values));
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).size() == 0);
//    }

//    @Test
//    public void NullListSubtype_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AbstractClassifierInstance values = new List<INode>() { null };
//        Assert.assertThrows(IllegalArgumentException.class,
//            () -> parent.addChild(Geometry_shapes(), values));
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).size() == 0);
//    }

//    @Test
//    public void NullListNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AbstractClassifierInstance values = new List<string>() { null };
//        Assert.assertThrows(IllegalArgumentException.class,
//            () -> parent.addChild(Geometry_shapes(), values));
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).size() == 0);
//    }

//    @Test
//    public void NullSet_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AbstractClassifierInstance values = new HashSet<DynamicNode>() { null };
//        Assert.assertThrows(IllegalArgumentException.class,
//            () -> parent.addChild(Geometry_shapes(), values));
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).size() == 0);
//    }

////    #endregion

////    #region SingleCollection

//    @Test
//    public void SingleArray_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AbstractClassifierInstance value = newLine("s");
//        AbstractClassifierInstance values = new DynamicNode[] { value };
//        parent.addChild(Geometry_shapes(), values);
//        Assert.assertSame(parent, value.getParent());
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).contains(value));
//    }

//    @Test
//    public void SingleArray_Existing_Reflective()
//    {
//        AbstractClassifierInstance circle = newCircle("cc");
//        AbstractClassifierInstance parent = newGeometry("g");
//        parent.addChild(Geometry_shapes(), new DynamicNode[] { circle });
//        AbstractClassifierInstance value = newLine("s");
//        AbstractClassifierInstance values = new DynamicNode[] { value };
//        parent.addChild(Geometry_shapes(), values);
//        Assert.assertNull(circle.getParent());
//        Assert.assertSame(parent, value.getParent());
//        CollectionAssert.assertEquals(new List<DynamicNode> { value },
//            (parent.getChildren(Geometry_shapes())).ToList());
//    }

//    @Test
//    public void SingleUntypedArray_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AbstractClassifierInstance value = newLine("s");
//        AbstractClassifierInstance values = new object[] { value };
//        parent.addChild(Geometry_shapes(), values);
//        Assert.assertSame(parent, value.getParent());
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).contains(value));
//    }

//    @Test
//    public void SingleUntypedList_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AbstractClassifierInstance value = newLine("s");
//        AbstractClassifierInstance values = new ArrayList() { value };
//        parent.addChild(Geometry_shapes(), values);
//        Assert.assertSame(parent, value.getParent());
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).contains(value));
//    }

//    @Test
//    public void SingleListMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AbstractClassifierInstance value = newLine("s");
//        AbstractClassifierInstance values = new List<DynamicNode>() { value };
//        parent.addChild(Geometry_shapes(), values);
//        Assert.assertSame(parent, value.getParent());
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).contains(value));
//    }

//    @Test
//    public void SingleListSubtype_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AbstractClassifierInstance value = newLine("s");
//        AbstractClassifierInstance values = new List<INode>() { value };
//        parent.addChild(Geometry_shapes(), values);
//        Assert.assertSame(parent, value.getParent());
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).contains(value));
//    }

//    @Test
//    public void SingleSet_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AbstractClassifierInstance value = newLine("s");
//        AbstractClassifierInstance values = new HashSet<DynamicNode>() { value };
//        parent.addChild(Geometry_shapes(), values);
//        Assert.assertSame(parent, value.getParent());
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).contains(value));
//    }

//    @Test
//    public void SingleListNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AbstractClassifierInstance value = newCoord("c");
//        AbstractClassifierInstance values = new List<DynamicNode>() { value };
//        Assert.assertThrows(IllegalArgumentException.class,
//            () -> parent.addChild(Geometry_shapes(), values));
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).size() == 0);
//    }

//    @Test
//    public void SingleUntypedListNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AbstractClassifierInstance value = newCoord("c");
//        AbstractClassifierInstance values = new ArrayList() { value };
//        Assert.assertThrows(IllegalArgumentException.class,
//            () -> parent.addChild(Geometry_shapes(), values));
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).size() == 0);
//    }

//    @Test
//    public void SingleUntypedArrayNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AbstractClassifierInstance value = newCoord("c");
//        AbstractClassifierInstance values = new object[] { value };
//        Assert.assertThrows(IllegalArgumentException.class,
//            () -> parent.addChild(Geometry_shapes(), values));
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).size() == 0);
//    }

////    #endregion

////    #region MultipleCollection

//    @Test
//    public void MultipleArray_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AbstractClassifierInstance valueA = newLine("sA");
//        AbstractClassifierInstance valueB = newLine("sB");
//        AbstractClassifierInstance values = new DynamicNode[] { valueA, valueB };
//        parent.addChild(Geometry_shapes(), values);
//        Assert.assertSame(parent, valueA.getParent());
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).contains(valueA));
//        Assert.assertSame(parent, valueB.getParent());
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).contains(valueB));
//    }

//    @Test
//    public void MultipleUntypedArray_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AbstractClassifierInstance valueA = newLine("sA");
//        AbstractClassifierInstance valueB = newLine("sB");
//        AbstractClassifierInstance values = new object[] { valueA, valueB };
//        parent.addChild(Geometry_shapes(), values);
//        Assert.assertSame(parent, valueA.getParent());
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).contains(valueA));
//        Assert.assertSame(parent, valueB.getParent());
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).contains(valueB));
//    }

//    @Test
//    public void MultipleUntypedList_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AbstractClassifierInstance valueA = newLine("sA");
//        AbstractClassifierInstance valueB = newLine("sB");
//        AbstractClassifierInstance values = new ArrayList() { valueA, valueB };
//        parent.addChild(Geometry_shapes(), values);
//        Assert.assertSame(parent, valueA.getParent());
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).contains(valueA));
//        Assert.assertSame(parent, valueB.getParent());
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).contains(valueB));
//    }

//    @Test
//    public void MultipleListMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AbstractClassifierInstance valueA = newLine("sA");
//        AbstractClassifierInstance valueB = newLine("sB");
//        AbstractClassifierInstance values = new List<DynamicNode>() { valueA, valueB };
//        parent.addChild(Geometry_shapes(), values);
//        Assert.assertSame(parent, valueA.getParent());
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).contains(valueA));
//        Assert.assertSame(parent, valueB.getParent());
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).contains(valueB));
//    }

//    @Test
//    public void MultipleListSubtype_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AbstractClassifierInstance valueA = newLine("sA");
//        AbstractClassifierInstance valueB = newLine("sB");
//        AbstractClassifierInstance values = new List<DynamicNode>() { valueA, valueB };
//        parent.addChild(Geometry_shapes(), values);
//        Assert.assertSame(parent, valueA.getParent());
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).contains(valueA));
//        Assert.assertSame(parent, valueB.getParent());
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).contains(valueB));
//    }

//    @Test
//    public void MultipleSet_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AbstractClassifierInstance valueA = newLine("sA");
//        AbstractClassifierInstance valueB = newLine("sB");
//        AbstractClassifierInstance values = new HashSet<DynamicNode>() { valueA, valueB };
//        parent.addChild(Geometry_shapes(), values);
//        Assert.assertSame(parent, valueA.getParent());
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).contains(valueA));
//        Assert.assertSame(parent, valueB.getParent());
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).contains(valueB));
//    }

//    @Test
//    public void MultipleSingleEnumerable_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AbstractClassifierInstance valueA = newLine("sA");
//        AbstractClassifierInstance valueB = newLine("sB");
//        AbstractClassifierInstance values = new SingleEnumerable<DynamicNode> { valueA, valueB };
//        parent.addChild(Geometry_shapes(), values);
//        Assert.assertSame(parent, valueA.getParent());
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).contains(valueA));
//        Assert.assertSame(parent, valueB.getParent());
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).contains(valueB));
//    }

//    @Test
//    public void MultipleListNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AbstractClassifierInstance valueA = newCoord("cA");
//        AbstractClassifierInstance valueB = newCoord("cB");
//        AbstractClassifierInstance values = new List<DynamicNode>() { valueA, valueB };
//        Assert.assertThrows(IllegalArgumentException.class,
//            () -> parent.addChild(Geometry_shapes(), values));
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).size() == 0);
//    }

//    @Test
//    public void MultipleUntypedListNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AbstractClassifierInstance valueA = newCoord("cA");
//        AbstractClassifierInstance valueB = newCoord("cB");
//        AbstractClassifierInstance values = new ArrayList() { valueA, valueB };
//        Assert.assertThrows(IllegalArgumentException.class,
//            () -> parent.addChild(Geometry_shapes(), values));
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).size() == 0);
//    }

//    @Test
//    public void MultipleUntypedArrayNonMatchingType_Reflective()
//    {
//        AbstractClassifierInstance parent = newGeometry("g");
//        AbstractClassifierInstance valueA = newCoord("cA");
//        AbstractClassifierInstance valueB = newCoord("cB");
//        AbstractClassifierInstance values = new object[] { valueA, valueB };
//        Assert.assertThrows(IllegalArgumentException.class,
//            () -> parent.addChild(Geometry_shapes(), values));
//        Assert.assertTrue((parent.getChildren(Geometry_shapes())).size() == 0);
//    }

    @Test
    public void ResultUnmodifiable_Set()
    {
        AbstractClassifierInstance parent = newGeometry("g");
        Node valueA = newLine("sA");
        Node valueB = newLine("sB");
        parent.addChild(Geometry_shapes(), valueA);
        parent.addChild(Geometry_shapes(), valueB);
        List result = parent.getChildren(Geometry_shapes());
        Assert.assertThrows(UnsupportedOperationException.class, () -> result.add(valueA));
    }

    @Test
    public void ResultUnmodifiable_Unset()
    {
        Node valueA = newLine("sA");
        AbstractClassifierInstance parent = newGeometry("g");
        List result = parent.getChildren(Geometry_shapes());
        Assert.assertThrows(UnsupportedOperationException.class, () -> result.add(valueA));
    }

////    #endregion
}