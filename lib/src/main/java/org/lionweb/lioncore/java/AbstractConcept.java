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

import java.util.LinkedList;
import java.util.List;

/**
 * This represents a group of elements that shares some characteristics.
 *
 * For example, Dated and Invoice could be both AbstractConcepts, while having different levels of tightness in the
 * groups.
 *
 * AbstractConcept is similar to EClass in Ecore (which is used both for classes and interfaces) and to
 * AbstractConcept in MPS.
 */
public abstract class AbstractConcept extends MetamodelElement implements NamespaceProvider, FeaturesContainer {
    private List<Feature> features = new LinkedList<>();

    public AbstractConcept(Metamodel metamodel, String simpleName) {
        super(metamodel, simpleName);
    }

    public List<Feature> allFeatures() {
        // TODO Should this return features which are overriden?
        // TODO Should features be returned in a particular order?
        throw new UnsupportedOperationException();
    }

    // TODO should this expose an immutable list to force users to use methods on this class
    //      to modify the collection?
    @Override
    public List<Feature> getFeatures() {
        return this.features;
    }

    @Override
    public String namespaceQualifier() {
        return this.qualifiedName();
    }
}
