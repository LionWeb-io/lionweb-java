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

public class PropertyTests_Integer_Required extends DynamicNodeTestsBase
{
//    #region Single

    @Test
    public void Reflective()
    {
        AbstractClassifierInstance parent = newCircle("od");
        int value = 10;
        parent.setPropertyValue(Circle_r(), value);
        Assert.assertEquals(10, parent.getPropertyValue(Circle_r()));
    }

    @Test
    public void Get_Reflective()
    {
        AbstractClassifierInstance parent = newCircle("od");
        parent.setPropertyValue(Circle_r(), 10);
        Assert.assertEquals(10, parent.getPropertyValue(Circle_r()));
    }

    @Test
    public void Long_Reflective()
    {
        AbstractClassifierInstance parent = newCircle("od");
        long value = 10L;
        Assert.assertThrows(IllegalArgumentException.class, () -> parent.setPropertyValue(Circle_r(), value));
        Assert.assertThrows(IllegalArgumentException.class, () -> parent.getPropertyValue(Circle_r()));
    }

    @Test
    public void String_Reflective()
    {
        AbstractClassifierInstance parent = newCircle("od");
        String value = "10";
        Assert.assertThrows(IllegalArgumentException.class, () -> parent.setPropertyValue(Circle_r(), value));
        Assert.assertThrows(IllegalArgumentException.class, () -> parent.getPropertyValue(Circle_r()));
    }

    @Test
    public void Boolean_Reflective()
    {
        AbstractClassifierInstance parent = newCircle("od");
        boolean value = true;
        Assert.assertThrows(IllegalArgumentException.class, () -> parent.setPropertyValue(Circle_r(), value));
        Assert.assertThrows(IllegalArgumentException.class, () -> parent.getPropertyValue(Circle_r()));
    }

//    #endregion

//    #region Null

    @Test
    public void Null_Reflective()
    {
        AbstractClassifierInstance parent = newCircle("od");
        Object value = null;
        Assert.assertThrows(IllegalArgumentException.class, 
            () -> parent.setPropertyValue(Circle_r(), value));
        Assert.assertThrows(IllegalArgumentException.class, () -> parent.getPropertyValue(Circle_r()));
    }

    @Test
    public void Null_Get_Reflective()
    {
        AbstractClassifierInstance parent = newCircle("od");
        Assert.assertThrows(IllegalArgumentException.class, () -> parent.getPropertyValue(Circle_r()));
    }

//    #endregion
}