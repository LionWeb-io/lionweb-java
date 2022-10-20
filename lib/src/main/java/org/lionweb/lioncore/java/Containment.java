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
 * Represents a relation between a containing FeaturesContainer and a contained AbstractConcept.
 *
 * Between an IfStatement and its condition there is a Containment relation.
 *
 * A Containment is similar to an ECore’s EReference with the containment flag set to true.
 * Differently from an EReference there is no container flag and resolveProxies flag.
 * A Containment is similar to an MPS’s LinkDeclaration with metaClass having value aggregation.
 */
public class Containment extends Link {
    private Containment specialized;

    public Containment(String simpleName, FeaturesContainer container) {
        // TODO verify that the container is also a NamespaceProvider
        super(simpleName, container);
    }

    public Containment getSpecialized() {
        return specialized;
    }

    public void setSpecialized(Containment specialized) {
        // TODO check which limitations there are: should have the same name? Should it belong
        //      to an ancestor of the FeaturesContainer holding this Containment?
        this.specialized = specialized;
    }

}
