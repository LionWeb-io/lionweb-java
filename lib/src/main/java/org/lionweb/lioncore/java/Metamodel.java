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

import org.lionweb.lioncore.java.utils.Naming;

import java.util.LinkedList;
import java.util.List;

/**
 * A Metamodel will provide the Concepts necessary to describe data in a particular domain together with supporting
 * elements necessary for the definition of those Concepts.
 *
 * It also represents the namespace within which Concepts and other supporting elements are organized.
 * For example, a Metamodel for accounting could collect several Concepts such as Invoice, Customer, InvoiceLine,
 * Product. It could also contain related elements necessary for the definitions of the concepts. For example, a
 * DataType named Currency.
 *
 * A Metamodel in LionWeb will be roughly equivalent to an EPackage or the contents of the structure aspect of an MPS
 * Language.
 */
public class Metamodel implements NamespaceProvider {
    private String qualifiedName;
    private List<Metamodel> dependsOn = new LinkedList<>();
    private List<MetamodelElement> elements = new LinkedList<>();

    public Metamodel(String qualifiedName) {
        Naming.validateQualifiedName(qualifiedName);
        this.qualifiedName = qualifiedName;
    }

    @Override
    public String namespaceQualifier() {
        return qualifiedName;
    }

    public List<Metamodel> dependsOn() {
        return this.dependsOn;
    }
    public List<MetamodelElement> getElements() {
        return this.elements;
    }

    public String getQualifiedName() {
        return this.qualifiedName;
    }
}
