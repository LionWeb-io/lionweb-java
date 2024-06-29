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

import examples.shapes.dynamic.ShapesDynamic;
import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.model.impl.DynamicAnnotationInstance;
import io.lionweb.lioncore.java.model.impl.DynamicClassifierInstance;
import io.lionweb.lioncore.java.model.impl.DynamicNode;
import io.lionweb.lioncore.java.serialization.Instantiator;
import org.junit.Before;

import java.util.List;

public abstract class DynamicNodeTestsBase {
    protected Language _lang;
    protected Instantiator instantiator;

    @Before
    public void LoadLanguage() {
        _lang = ShapesDynamic.Language;
        instantiator = new Instantiator().enableDynamicNodes();
    }
    
    protected <T extends Classifier<T>> DynamicClassifierInstance<T> CreateNode(String id, T classifier) {
        if (classifier instanceof Concept) {
            return (DynamicClassifierInstance<T>) new DynamicNode(id, (Concept) classifier);
        }
        if(classifier instanceof Annotation) {
            return (DynamicClassifierInstance<T>) new DynamicAnnotationInstance(id, (Annotation) classifier);
        }
        throw new IllegalStateException();
    }
    
    protected Classifier getClassifierByKey(String key) {
        return _lang.getElements()
                .stream()
                .filter(e -> e instanceof Classifier)
                .map(e -> (Classifier) e)
                .filter(e -> e.getKey().equals(key))
                .findFirst()
                .orElse(null);
    }

    protected Feature getFeatureByKey(Classifier classifier, String key) {
        return ((List <Feature>) classifier.allFeatures())
                .stream()
                .filter(f -> f.getKey().equals(key))
                .findFirst()
                .orElse(null);
    }

    protected DynamicNode newReferenceGeometry(String id) {
        DynamicNode node = (DynamicNode) CreateNode(id, getClassifierByKey("key-ReferenceGeometry"));
        if (node == null) {
            throw new AssertionError();
        }
        return node;
    }

    protected DynamicNode newLine(String id) {
        DynamicNode node = (DynamicNode) CreateNode(id, getClassifierByKey("key-Line"));
        if (node == null) {
            throw new AssertionError();
        }
        return node;
    }

    protected DynamicNode newCoord(String id) {
        DynamicNode node = (DynamicNode) CreateNode(id, getClassifierByKey("key-Coord"));
        if (node == null) {
            throw new AssertionError();
        }
        return node;
    }

    protected DynamicAnnotationInstance newBillOfMaterials(String id) {
        DynamicAnnotationInstance node = (DynamicAnnotationInstance) CreateNode(id, getClassifierByKey("key-BillOfMaterials"));
        if (node == null) {
            throw new AssertionError();
        }
        return node;
    }

    protected DynamicAnnotationInstance newDocumentation(String id) {
        DynamicAnnotationInstance node = (DynamicAnnotationInstance) CreateNode(id, getClassifierByKey("key-Documentation"));
        if (node == null) {
            throw new AssertionError();
        }
        return node;
    }

    protected DynamicNode newGeometry(String id) {
        DynamicNode node = (DynamicNode) CreateNode(id, getClassifierByKey("key-Geometry"));
        if (node == null) {
            throw new AssertionError();
        }
        return node;
    }

    protected DynamicNode newCircle(String id) {
        DynamicNode node = (DynamicNode) CreateNode(id, getClassifierByKey("key-Circle"));
        if (node == null) {
            throw new AssertionError();
        }
        return node;
    }

    protected DynamicNode newCompositeShape(String id) {
        DynamicNode node = (DynamicNode) CreateNode(id, getClassifierByKey("key-CompositeShape"));
        if (node == null) {
            throw new AssertionError();
        }
        return node;
    }

    protected DynamicNode newOffsetDuplicate(String id) {
        DynamicNode node = (DynamicNode) CreateNode(id, getClassifierByKey("key-OffsetDuplicate"));
        if (node == null) {
            throw new AssertionError();
        }
        return node;
    }

    protected DynamicNode newMaterialGroup(String id) {
        DynamicNode node = (DynamicNode) CreateNode(id, getClassifierByKey("key-MaterialGroup"));
        if (node == null) {
            throw new AssertionError();
        }
        return node;
    }
    
    protected Feature ReferenceGeometry_shapes() {
        return getFeatureByKey(getClassifierByKey("key-ReferenceGeometry"), "key-shapes-references");
    }

    protected Feature Geometry_shapes() {
        return getFeatureByKey(getClassifierByKey("key-Geometry"), "key-shapes");
    }

    protected Feature CompositeShape_parts() {
        return getFeatureByKey(getClassifierByKey("key-CompositeShape"), "key-parts");
    }

    protected Feature CompositeShape_disabledParts() {
        return getFeatureByKey(getClassifierByKey("key-CompositeShape"), "key-disabled-parts");
    }

    protected Feature CompositeShape_evilPart() {
        return getFeatureByKey(getClassifierByKey("key-CompositeShape"), "key-evil-part");
    }

    protected Feature Geometry_documentation() {
        return getFeatureByKey(getClassifierByKey("key-Geometry"), "key-documentation");
    }

    protected Feature OffsetDuplicate_offset() {
        return getFeatureByKey(getClassifierByKey("key-OffsetDuplicate"), "key-offset");
    }

    protected Feature Documentation_technical()
    {
        return getFeatureByKey(getClassifierByKey("key-Documentation"), "key-technical");
    }

    protected Feature MaterialGroup_matterState()
    {
        return getFeatureByKey(getClassifierByKey("key-MaterialGroup"), "key-matter-state");
    }

    protected Feature MaterialGroup_defaultShape()
    {
        return getFeatureByKey(getClassifierByKey("key-MaterialGroup"), "key-default-shape");
    }

    protected Feature Circle_r()
    {
        return getFeatureByKey(getClassifierByKey("key-Circle"), "key-r");
    }

    protected Feature Circle_center()
    {
        return getFeatureByKey(getClassifierByKey("key-Circle"), "key-center");
    }

    protected Feature Line_start()
    {
        return getFeatureByKey(getClassifierByKey("key-Line"), "key-start");
    }

    protected Feature Line_end()
    {
        return getFeatureByKey(getClassifierByKey("key-Line"), "key-end");
    }

    protected Feature Documentation_text()
    {
        return getFeatureByKey(getClassifierByKey("key-Documentation"), "key-text");
    }

    protected Feature MaterialGroup_materials()
    {
        return getFeatureByKey(getClassifierByKey("key-MaterialGroup"), "key-group-materials");
    }

    protected Feature OffsetDuplicate_altSource()
    {
        return getFeatureByKey(getClassifierByKey("key-OffsetDuplicate"), "key-alt-source");
    }

    protected Feature OffsetDuplicate_source()
    {
        return getFeatureByKey(getClassifierByKey("key-OffsetDuplicate"), "key-source");
    }

    protected Feature OffsetDuplicate_docs()
    {
        return getFeatureByKey(getClassifierByKey("key-OffsetDuplicate"), "key-docs");
    }

    protected Feature OffsetDuplicate_secretDocs()
    {
        return getFeatureByKey(getClassifierByKey("key-OffsetDuplicate"), "key-secret-docs");
    }

    protected Feature BillOfMaterials_groups()
    {
        return getFeatureByKey(getClassifierByKey("key-BillOfMaterials"), "key-groups");
    }

    protected Feature BillOfMaterials_altGroups()
    {
        return getFeatureByKey(getClassifierByKey("key-BillOfMaterials"), "key-alt-groups");
    }

    protected Feature BillOfMaterials_defaultGroup()
    {
        return getFeatureByKey(getClassifierByKey("key-BillOfMaterials"), "key-default-group");
    }

    protected Feature Shape_shapeDocs()
    {
        return getFeatureByKey(getClassifierByKey("key-Shape"), "key-shape-docs");
    }

//    protected Enum MatterState_Gas()
//    {
//        return getGetFactory().GetEnumerationLiteral(
//            MatterState
//                    .Literals
//                    .First(l => l.Key == "key-gas")
//        );
//    }
//
//    protected Enum MatterState_Liquid()
//    {
//        return getGetFactory().GetEnumerationLiteral(
//            MatterState
//                    .Literals
//                    .First(l => l.Key == "key-liquid")
//        );
//    }
//
//    private Enumeration MatterState() {
//        _lang
//                .Enumerations()
//                .First(e = > e.Key == "key-MatterState");
//    }

}

