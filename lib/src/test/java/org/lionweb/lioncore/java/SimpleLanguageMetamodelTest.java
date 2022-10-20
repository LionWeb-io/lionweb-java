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

import org.junit.Test;
import static org.junit.Assert.*;

public class SimpleLanguageMetamodelTest {

    @Test public void emptyMetamodelDefinition() {
        Metamodel metamodel = new Metamodel("SimpleLanguage");
        assertEquals("SimpleLanguage", metamodel.getQualifiedName());
        assertEquals("SimpleLanguage", metamodel.namespaceQualifier());
        assertEquals(0, metamodel.dependsOn().size());
        assertEquals(0, metamodel.getElements().size());
    }

    @Test public void emptyConceptDefinition() {
        Metamodel metamodel = new Metamodel("SimpleLanguage");
        Concept expression = new Concept(metamodel, "Expression");
        assertEquals("Expression", expression.getSimpleName());
        assertSame(metamodel, expression.getContainer());
        assertSame(metamodel, expression.getMetamodel());
        assertEquals("SimpleLanguage.Expression", expression.qualifiedName());
        assertEquals("SimpleLanguage.Expression", expression.namespaceQualifier());
        assertNull(expression.getExtendedConcept());
        assertEquals(0, expression.getImplemented().size());
        assertEquals(0, expression.getFeatures().size());
        assertFalse(expression.isAbstract());
    }

    @Test public void emptyConceptInterfaceDefinition() {
        Metamodel metamodel = new Metamodel("SimpleLanguage");
        ConceptInterface deprecated = new ConceptInterface(metamodel, "Deprecated");
        assertEquals("Deprecated", deprecated.getSimpleName());
        assertSame(metamodel, deprecated.getContainer());
        assertSame(metamodel, deprecated.getMetamodel());
        assertEquals("SimpleLanguage.Deprecated", deprecated.qualifiedName());
        assertEquals("SimpleLanguage.Deprecated", deprecated.namespaceQualifier());
        assertEquals(0, deprecated.getExtendedInterface().size());
        assertEquals(0, deprecated.getFeatures().size());
    }
}
