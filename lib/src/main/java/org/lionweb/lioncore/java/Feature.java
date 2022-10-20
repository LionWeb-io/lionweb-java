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

/**
 * A Feature represents a characteristic or some form of data associated with a particular concept.
 *
 * For example, an Invoice can have an associated date, a number, a connection with a customer, and it can contain
 * InvoiceLines. All of this information is represented by features.
 *
 * A Feature in LionWeb will be roughly equivalent to an EStructuralFeature or to the combination of Properties and
 * Links (both containment and reference links) in MPS.
 */
public abstract class Feature implements NamespacedEntity {
    private Multiplicity multiplicity;
    private boolean derived;
    private String simpleName;
    private FeaturesContainer container;

    public Feature(String simpleName, FeaturesContainer container) {
        // TODO verify that the container is also a NamespaceProvider
        // TODO enforce uniqueness of the name within the FeauturesContainer
        Naming.validateSimpleName(simpleName);
        this.simpleName = simpleName;
        this.container = container;
    }

    public Multiplicity getMultiplicity() {
        return multiplicity;
    }

    public void setMultiplicity(Multiplicity multiplicity) {
        this.multiplicity = multiplicity;
    }

    public boolean isDerived() {
        return derived;
    }

    public void setDerived(boolean derived) {
        this.derived = derived;
    }

    @Override
    public String getSimpleName() {
        return simpleName;
    }

    @Override
    public String qualifiedName() {
        return this.getContainer().namespaceQualifier() + "." + this.getSimpleName();
    }

    @Override
    public NamespaceProvider getContainer() {
        return (NamespaceProvider) container;
    }
}
