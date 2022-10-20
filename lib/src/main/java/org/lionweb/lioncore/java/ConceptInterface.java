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
 * A ConceptInterface represents a category of entities sharing some similar characteristics.
 *
 * For example, Named would be a ConceptInterface.
 *
 * A ConceptInterface in LionWeb will be roughly equivalent to an EClass (with the isInterface flag set to true) or
 * an MPS’s ConceptInterface.
 */
public class ConceptInterface extends AbstractConcept {
    private List<ConceptInterface> extended = new LinkedList<>();

    public ConceptInterface(Metamodel metamodel, String simpleName) {
        super(metamodel, simpleName);
    }

    public List<ConceptInterface> getExtendedInterface() {
        return this.extended;
    }
}
