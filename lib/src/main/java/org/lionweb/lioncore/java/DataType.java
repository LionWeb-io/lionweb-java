/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.lionweb.lioncore.java;

/**
 * A type of value which has not a relevant identity in the context of a model.
 *
 * A Currency or a Date type are possible DataTypes.
 *
 * It is similar to Ecore's EDataType. It is also similar to MPS' DataTypeDeclaration.
 */
public abstract class DataType extends MetamodelElement {
    public DataType(Metamodel metamodel, String simpleName) {
        super(metamodel, simpleName);
    }
}
