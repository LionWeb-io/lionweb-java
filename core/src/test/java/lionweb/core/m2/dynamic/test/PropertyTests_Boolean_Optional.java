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

public class PropertyTests_Boolean_Optional extends DynamicNodeTestsBase
{
//    #region Single

    @Test
    public void Reflective()
    {
        AbstractClassifierInstance parent = newDocumentation("od");
        boolean value = true;
        parent.setPropertyValue(Documentation_technical(), value);
        Assert.assertEquals(true, parent.getPropertyValue(Documentation_technical()));
    }

    @Test
    public void Get_Reflective()
    {
        AbstractClassifierInstance parent = newDocumentation("od");
        parent.setPropertyValue(Documentation_technical(), true);
        Assert.assertEquals(true, parent.getPropertyValue(Documentation_technical()));
    }

    @Test
    public void False_Reflective()
    {
        AbstractClassifierInstance parent = newDocumentation("od");
        boolean value = false;
        parent.setPropertyValue(Documentation_technical(), value);
        Assert.assertEquals(false, parent.getPropertyValue(Documentation_technical()));
    }

    @Test
    public void String_Reflective()
    {
        AbstractClassifierInstance parent = newDocumentation("od");
        String value = "10";
        Assert.assertThrows(IllegalArgumentException.class, () ->
            parent.setPropertyValue(Documentation_technical(), value));
        Assert.assertEquals(null, parent.getPropertyValue(Documentation_technical()));
    }

    @Test
    public void Integer_Reflective()
    {
        AbstractClassifierInstance parent = newDocumentation("od");
        int value = 10;
        Assert.assertThrows(IllegalArgumentException.class, () ->
            parent.setPropertyValue(Documentation_technical(), value));
        Assert.assertEquals(null, parent.getPropertyValue(Documentation_technical()));
    }

//    #endregion

//    #region Null

    @Test
    public void Null_Reflective()
    {
        AbstractClassifierInstance parent = newDocumentation("od");
        Object value = null;
        parent.setPropertyValue(Documentation_technical(), null);
        Assert.assertEquals(null, parent.getPropertyValue(Documentation_technical()));
    }

    @Test
    public void Null_Get_Reflective()
    {
        AbstractClassifierInstance parent = newDocumentation("od");
        Assert.assertEquals(null, parent.getPropertyValue(Documentation_technical()));
    }

//    #endregion
}