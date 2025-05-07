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
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.impl.DynamicAnnotationInstance;
import io.lionweb.lioncore.java.model.impl.DynamicClassifierInstance;
import io.lionweb.lioncore.java.model.impl.DynamicNode;
import io.lionweb.lioncore.java.serialization.Instantiator;
import org.junit.Before;

import java.sql.Ref;
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
    
    protected Reference ReferenceGeometry_shapes() {
        return (Reference) getFeatureByKey(getClassifierByKey("key-ReferenceGeometry"), "key-shapes-references");
    }

    protected Containment Geometry_shapes() {
        return (Containment) getFeatureByKey(getClassifierByKey("key-Geometry"), "key-shapes");
    }

    protected Containment CompositeShape_parts() {
        return (Containment) getFeatureByKey(getClassifierByKey("key-CompositeShape"), "key-parts");
    }

    protected Containment CompositeShape_disabledParts() {
        return (Containment) getFeatureByKey(getClassifierByKey("key-CompositeShape"), "key-disabled-parts");
    }

    protected Containment CompositeShape_evilPart() {
        return (Containment) getFeatureByKey(getClassifierByKey("key-CompositeShape"), "key-evil-part");
    }

    protected Containment Geometry_documentation() {
        return (Containment) getFeatureByKey(getClassifierByKey("key-Geometry"), "key-documentation");
    }

    protected Containment OffsetDuplicate_offset() {
        return (Containment) getFeatureByKey(getClassifierByKey("key-OffsetDuplicate"), "key-offset");
    }

    protected Property Documentation_technical()
    {
        return (Property) getFeatureByKey(getClassifierByKey("key-Documentation"), "key-technical");
    }

    protected Property MaterialGroup_matterState()
    {
        return (Property) getFeatureByKey(getClassifierByKey("key-MaterialGroup"), "key-matter-state");
    }

    protected Containment MaterialGroup_defaultShape()
    {
        return (Containment) getFeatureByKey(getClassifierByKey("key-MaterialGroup"), "key-default-shape");
    }

    protected Property Circle_r()
    {
        return (Property) getFeatureByKey(getClassifierByKey("key-Circle"), "key-r");
    }

    protected Containment Circle_center()
    {
        return (Containment) getFeatureByKey(getClassifierByKey("key-Circle"), "key-center");
    }

    protected Containment Line_start()
    {
        return (Containment) getFeatureByKey(getClassifierByKey("key-Line"), "key-start");
    }

    protected Containment Line_end()
    {
        return (Containment) getFeatureByKey(getClassifierByKey("key-Line"), "key-end");
    }

    protected Property Documentation_text()
    {
        return (Property) getFeatureByKey(getClassifierByKey("key-Documentation"), "key-text");
    }

    protected Reference MaterialGroup_materials()
    {
        return (Reference) getFeatureByKey(getClassifierByKey("key-MaterialGroup"), "key-group-materials");
    }

    protected Reference OffsetDuplicate_altSource()
    {
        return (Reference) getFeatureByKey(getClassifierByKey("key-OffsetDuplicate"), "key-alt-source");
    }

    protected Reference OffsetDuplicate_source()
    {
        return (Reference) getFeatureByKey(getClassifierByKey("key-OffsetDuplicate"), "key-source");
    }

    protected Containment OffsetDuplicate_docs()
    {
        return (Containment) getFeatureByKey(getClassifierByKey("key-OffsetDuplicate"), "key-docs");
    }

    protected Containment OffsetDuplicate_secretDocs()
    {
        return (Containment) getFeatureByKey(getClassifierByKey("key-OffsetDuplicate"), "key-secret-docs");
    }

    protected Containment BillOfMaterials_groups()
    {
        return (Containment) getFeatureByKey(getClassifierByKey("key-BillOfMaterials"), "key-groups");
    }

    protected Containment BillOfMaterials_altGroups()
    {
        return (Containment) getFeatureByKey(getClassifierByKey("key-BillOfMaterials"), "key-alt-groups");
    }

    protected Containment BillOfMaterials_defaultGroup()
    {
        return (Containment) getFeatureByKey(getClassifierByKey("key-BillOfMaterials"), "key-default-group");
    }

    protected Containment Shape_shapeDocs()
    {
        return (Containment) getFeatureByKey(getClassifierByKey("key-Shape"), "key-shape-docs");
    }

    protected Enum MatterState_Gas()
    {
        return null;
//        return getGetFactory().GetEnumerationLiteral(
//            MatterState
//                    .Literals
//                    .First(l => l.Key == "key-gas")
//        );
    }

    protected Enum MatterState_Liquid()
    {
        return null;
//        return getGetFactory().GetEnumerationLiteral(
//            MatterState
//                    .Literals
//                    .First(l => l.Key == "key-liquid")
//        );
    }

    private Enumeration MatterState() {
        return _lang
                .getElements()
                .stream()
                .filter(e -> e instanceof Enumeration)
                .map(e -> (Enumeration) e)
                .filter(e -> e.getKey().equals("key-MatterState"))
                .findFirst()
                .orElse(null);
    }

}

