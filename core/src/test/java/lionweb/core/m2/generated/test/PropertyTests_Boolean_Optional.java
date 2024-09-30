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

package lionweb.core.m2.generated.test;

import examples.shapes.m2.Documentation;
import org.junit.Assert;
import org.junit.Test;

public class PropertyTests_Boolean_Optional
{
//    #region Single

    @Test
    public void Setter()
    {
        Documentation parent = new Documentation("od", false);
        boolean value = true;
        parent.setTechnical(value);
        Assert.assertEquals(true, parent.getTechnical());
    }

//    @Test
//    public void Reflective()
//    {
//        var parent = new Documentation("od");
//        var value = true;
//        parent.Set(ShapesLanguage.Instance.Documentation_technical, value);
//        Assert.AreEqual(true, parent.Technical);
//    }
//
//    @Test
//    public void Get_Reflective()
//    {
//        var parent = new Documentation("od") { Technical = true };
//        Assert.AreEqual(true, parent.Get(ShapesLanguage.Instance.Documentation_technical));
//    }
//
//    @Test
//    public void False_Reflective()
//    {
//        var parent = new Documentation("od");
//        var value = false;
//        parent.Set(ShapesLanguage.Instance.Documentation_technical, value);
//        Assert.AreEqual(false, parent.Technical);
//    }
//
//    @Test
//    public void String_Reflective()
//    {
//        var parent = new Documentation("od");
//        var value = "10";
//        Assert.ThrowsException<InvalidValueException>(() =>
//            parent.Set(ShapesLanguage.Instance.Documentation_technical, value));
//        Assert.AreEqual(null, parent.Technical);
//    }
//
//    @Test
//    public void Integer_Reflective()
//    {
//        var parent = new Documentation("od");
//        var value = 10;
//        Assert.ThrowsException<InvalidValueException>(() =>
//            parent.Set(ShapesLanguage.Instance.Documentation_technical, value));
//        Assert.AreEqual(null, parent.Technical);
//    }
//
//    @Test
//    public void Constructor()
//    {
//        var parent = new Documentation("myId") { Technical = true };
//        Assert.AreEqual(true, parent.Technical);
//    }
//
////    #endregion
//
////    #region Null
//
//    @Test
//    public void Null()
//    {
//        var parent = new Documentation("od");
//        object value = null;
//        parent.Technical = (bool?)value;
//        Assert.AreEqual(null, parent.Technical);
//    }
//
//    @Test
//    public void Null_Setter()
//    {
//        var parent = new Documentation("od");
//        object value = null;
//        parent.SetTechnical((bool?)value);
//        Assert.AreEqual(null, parent.Technical);
//    }
//
//    @Test
//    public void Null_Reflective()
//    {
//        var parent = new Documentation("od");
//        object value = null;
//        parent.Set(ShapesLanguage.Instance.Documentation_technical, null);
//        Assert.AreEqual(null, parent.Technical);
//    }
//
//    @Test
//    public void Null_Get()
//    {
//        var parent = new Documentation("od");
//        Assert.AreEqual(null, parent.Technical);
//    }
//
//    @Test
//    public void Null_Get_Reflective()
//    {
//        var parent = new Documentation("od");
//        Assert.AreEqual(null, parent.Get(ShapesLanguage.Instance.Documentation_technical));
//    }
//
//    @Test
//    public void Null_Constructor()
//    {
//        object value = null;
//        var parent = new Documentation("od") { Technical = (bool?)value };
//        Assert.AreEqual(null, parent.Text);
//    }

//    #endregion
}