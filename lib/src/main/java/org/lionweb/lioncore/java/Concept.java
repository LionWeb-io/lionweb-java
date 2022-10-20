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
 * A Concept represents a category of entities sharing the same structure.
 *
 * For example, Invoice would be a Concept. Single entities could be Concept instances, such as Invoice #1/2022.
 *
 * A Concept in LionWeb will be roughly equivalent to an EClass (with the isInterface flag set to false) or an MPS’s
 * Concept.
 */
public class Concept extends AbstractConcept {
    private boolean isAbstract;
    private Concept extended;
    private List<ConceptInterface> implemented = new LinkedList<>();

    public Concept(Metamodel metamodel, String simpleName) {
        super(metamodel, simpleName);
    }

    public boolean isAbstract() {
        return this.isAbstract;
    }

    public void setAbstract(boolean anAbstract) {
        isAbstract = anAbstract;
    }

    // TODO should this return BaseConcept when extended is equal null?
    public Concept getExtendedConcept() {
        return this.extended;
    }

    public List<ConceptInterface> getImplemented() {
        return this.implemented;
    }

    // TODO should we verify the Concept does not extend itself, even indirectly?
    public void setExtendedConcept(Concept extended) {
        this.extended = extended;
    }


}
