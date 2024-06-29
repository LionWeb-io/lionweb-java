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

public class PropertyTests_Enum_Optional extends DynamicNodeTestsBase
{
//    #region Single

    @Test
    public void Reflective()
    {
        AbstractClassifierInstance parent = newMaterialGroup("od");
        Enum value = MatterState_Liquid();
        parent.setPropertyValue(MaterialGroup_matterState(), value);
        Assert.assertEquals(MatterState_Liquid(), parent.getPropertyValue(MaterialGroup_matterState()));
    }

    @Test
    public void Get_Reflective()
    {
        AbstractClassifierInstance parent = newMaterialGroup("od");
        parent.setPropertyValue(MaterialGroup_matterState(), MatterState_Liquid());
        Assert.assertEquals(MatterState_Liquid(), parent.getPropertyValue(MaterialGroup_matterState()));
    }

    @Test
    public void Gas_Reflective()
    {
        AbstractClassifierInstance parent = newMaterialGroup("od");
        Enum value = MatterState_Gas();
        parent.setPropertyValue(MaterialGroup_matterState(), value);
        Assert.assertEquals(MatterState_Gas(), parent.getPropertyValue(MaterialGroup_matterState()));
    }

    @Test
    public void Boolean_Reflective()
    {
        AbstractClassifierInstance parent = newMaterialGroup("od");
        boolean value = true;
        Assert.assertThrows(IllegalArgumentException.class, () -> parent.setPropertyValue(MaterialGroup_matterState(), value));
        Assert.assertEquals(null, parent.getPropertyValue(MaterialGroup_matterState()));
    }

    private enum TestEnum
    {
        a,
        solid,
        gas
    }

    @Test
    public void OtherEnum_Reflective()
    {
        AbstractClassifierInstance parent = newMaterialGroup("od");
        TestEnum value = TestEnum.a;
        Assert.assertThrows(IllegalArgumentException.class, () -> parent.setPropertyValue(MaterialGroup_matterState(), value));
        Assert.assertEquals(null, parent.getPropertyValue(MaterialGroup_matterState()));
    }

    @Test
    public void SimilarEnum_Reflective()
    {
        AbstractClassifierInstance parent = newMaterialGroup("od");
        TestEnum value = TestEnum.solid;
        Assert.assertThrows(IllegalArgumentException.class, () -> parent.setPropertyValue(MaterialGroup_matterState(), value));
        Assert.assertEquals(null, parent.getPropertyValue(MaterialGroup_matterState()));
    }

    @Test
    public void VerySimilarEnum_Reflective()
    {
        AbstractClassifierInstance parent = newMaterialGroup("od");
        TestEnum value = TestEnum.gas;
        Assert.assertThrows(IllegalArgumentException.class, () -> parent.setPropertyValue(MaterialGroup_matterState(), value));
        Assert.assertEquals(null, parent.getPropertyValue(MaterialGroup_matterState()));
    }

    @Test
    public void Integer_Reflective()
    {
        AbstractClassifierInstance parent = newMaterialGroup("od");
        int value = 10;
        Assert.assertThrows(IllegalArgumentException.class, () -> parent.setPropertyValue(MaterialGroup_matterState(), value));
        Assert.assertEquals(null, parent.getPropertyValue(MaterialGroup_matterState()));
    }

//    #endregion

//    #region Null

    @Test
    public void Null_Reflective()
    {
        AbstractClassifierInstance parent = newMaterialGroup("od");
        Object value = null;
        parent.setPropertyValue(MaterialGroup_matterState(), null);
        Assert.assertEquals(null, parent.getPropertyValue(MaterialGroup_matterState()));
    }

    @Test
    public void Null_Get_Reflective()
    {
        AbstractClassifierInstance parent = newMaterialGroup("od");
        Assert.assertEquals(null, parent.getPropertyValue(MaterialGroup_matterState()));
    }

//    #endregion
}