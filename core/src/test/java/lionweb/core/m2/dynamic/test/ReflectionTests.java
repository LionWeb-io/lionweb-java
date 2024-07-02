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

import examples.shapes.dynamic.ShapesDynamic;
import io.lionweb.lioncore.java.language.LionCoreBuiltins;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.impl.AbstractClassifierInstance;
import lionweb.utils.tests.CollectionAssert;
import org.junit.Assert;
import org.junit.Test;

public class ReflectionTests extends DynamicNodeTestsBase
{
//    @Test
//    public void GetClassifier()
//    {
//        AbstractClassifierInstance node = newCircle("id");
//        Assert.assertEquals(getClassifierByKey("key-Circle"), node.getClassifier(),
//            new LanguageEntityIdentityComparer());
//    }

//    #region inherited

//    #region property

    @Test
    public void SetGetInheritedProperty()
    {
        AbstractClassifierInstance node = newCircle("id");
        node.setPropertyValue(LionCoreBuiltins.getINamed().getPropertyByID("LionCore-builtins-INamed-name"), "hi");
        Assert.assertEquals("hi", node.getPropertyValue(LionCoreBuiltins.getINamed().getPropertyByID("LionCore-builtins-INamed-name")));
    }

    @Test
    public void GetInheritedProperty_Unset()
    {
        AbstractClassifierInstance node = newCircle("id");
        Assert.assertThrows(IllegalArgumentException.class, () -> node.getPropertyValue(LionCoreBuiltins.getINamed().getPropertyByID("LionCore-builtins-INamed-name")));
    }

//    #endregion

//    #region containment

//    @Test
//    public void SetGetInheritedContainment()
//    {
//        Node child = newDocumentation("c");
//        AbstractClassifierInstance parent = newCircle("id");
//        parent.addChild(Shape_shapeDocs(), child);
//        Assert.assertSame(child, parent.getChildren(Shape_shapeDocs()));
//    }

    @Test
    public void GetInheritedContainment_Unset()
    {
        AbstractClassifierInstance parent = newCircle("id");
        Assert.assertSame(null, parent.getChildren(Shape_shapeDocs()));
    }

//    @Test
//    public void InheritedContainment_DetachChild()
//    {
//        Node child = newDocumentation("c");
//        AbstractClassifierInstance parent = newCircle("id");
//        parent.addChild(Shape_shapeDocs(), child);
//        parent.removeChild(child);
//        Assert.assertNull(child.getParent());
//        Assert.assertNull(parent.getChildren(Shape_shapeDocs()));
//    }

//    @Test
//    public void InheritedContainment_GetContainmentOf()
//    {
//        Node child = newDocumentation("c");
//        AbstractClassifierInstance parent = newCircle("id");
//        parent.addChild(Shape_shapeDocs(), child);
//        Assert.assertEquals(Shape_shapeDocs(), parent.GetContainmentOf(child));
//    }

//    @Test
//    public void InheritedContainment_CollectAllSetFeatures()
//    {
//        Node child = newDocumentation("c");
//        AbstractClassifierInstance parent = newCircle("id");
//        parent.addChild(Shape_shapeDocs(), child);
//        CollectionAssert.assertEquals(new List<Feature> { Shape_shapeDocs },
//            parent.CollectAllSetFeatures().ToList());
//    }

//    #endregion

//    #endregion
}