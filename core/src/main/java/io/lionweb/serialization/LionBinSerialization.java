package io.lionweb.serialization;

import io.lionweb.LionWebVersion;
import io.lionweb.language.*;
import io.lionweb.model.AnnotationInstance;
import io.lionweb.model.ClassifierInstance;
import io.lionweb.model.ClassifierInstanceUtils;
import io.lionweb.model.HasSettableParent;
import io.lionweb.model.Node;
import io.lionweb.model.ReferenceValue;
import io.lionweb.model.impl.AbstractClassifierInstance;
import io.lionweb.model.impl.ProxyNode;
import io.lionweb.serialization.data.*;
import io.lionweb.serialization.data.MetaPointer;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import javax.annotation.Nonnull;

/**
 * Binary serialization format ("LionBin") that exploits the structural regularity of LionWeb
 * nodes: every node with the same classifier always has the same set of properties, containments,
 * and references (features are fixed by the language). Annotations may vary per node.
 *
 * <p>A <em>type table</em> captures (classifier metapointer + ordered feature metapointers) exactly
 * once per distinct classifier encountered in the chunk. Each node record stores only a type-table
 * index plus its data, eliminating per-node metapointer repetition and enabling exact pre-sizing of
 * {@link SerializedClassifierInstance} collections at deserialization time.
 *
 * <h2>Binary layout (format version 2 — all integers are unsigned LEB128 varints)</h2>
 *
 * <pre>
 * HEADER
 *   magic:                    5 bytes "LWBIN"
 *   format version:           1 byte  (= 2)
 *   serialization format ver: varint(len) + UTF-8 bytes
 *
 * STRING TABLE              [count: varint]  ([len: varint] [bytes])*
 *   index 0 = null (not stored); stored entries start at index 1
 *
 * LANGUAGE TABLE            [count: varint]  ([siKey: varint] [siVersion: varint])*
 *   index 0 = null sentinel (not stored)
 *
 * METAPOINTER TABLE         [count: varint]  ([liLanguage: varint] [siKey: varint])*
 *
 * TYPE TABLE                [count: varint]  (
 *   [mpiClassifier: varint]
 *   [propertyCount: varint]    [mpiProp: varint]*
 *   [containmentCount: varint] [mpiCont: varint]*
 *   [referenceCount: varint]   [mpiRef:  varint]*
 * )*
 *
 * NODE DATA                 [count: varint]  (
 *   [typeIndex:      varint]
 *   [siId:           varint]   0 = null ID
 *   [siParent:       varint]   0 = no parent
 *   [annotationCount: varint]  written first so reader can pre-size correctly
 *   [siPropertyValue: varint]* one per property in type order; 0 = null value
 *   ( [childCount: varint] [siChild: varint]* )*     one section per containment
 *   ( [entryCount: varint] ([siReferred: varint] [siResolveInfo: varint])* )* one per reference
 *   [siAnnotation: varint]*    annotationCount entries
 * )*
 * </pre>
 */
public class LionBinSerialization extends AbstractSerialization {

  static final byte[] MAGIC = {'L', 'W', 'B', 'I', 'N'};
  static final byte FORMAT_VERSION = 2; // v2: unsigned LEB128 varint encoding

  public LionBinSerialization() {
    super();
  }

  public LionBinSerialization(@Nonnull LionWebVersion lionWebVersion) {
    super(lionWebVersion);
  }

  // ===========================================================================
  // Public serialization API
  // ===========================================================================

  public byte[] serializeTreesToByteArray(ClassifierInstance<?>... roots) {
    List<ClassifierInstance<?>> allNodes = new ArrayList<>(1024);
    Set<String> seenIDs = new HashSet<>(1024);
    for (ClassifierInstance<?> root : roots) {
      collectNonProxyNoDup(root, allNodes, seenIDs);
    }
    return serializeNodesToByteArray(allNodes);
  }

  public byte[] serializeNodesToByteArray(@Nonnull List<ClassifierInstance<?>> nodes) {
    for (int i = 0, sz = nodes.size(); i < sz; i++) {
      if (nodes.get(i) instanceof ProxyNode) {
        throw new IllegalArgumentException("Proxy nodes cannot be serialized");
      }
    }
    try {
      return serializeToBytes(nodes);
    } catch (IOException e) {
      throw new RuntimeException("Unexpected IO error during serialization", e);
    }
  }

  // ===========================================================================
  // Public deserialization API
  // ===========================================================================

  public SerializationChunk deserializeToChunk(@Nonnull byte[] bytes) throws IOException {
    return deserializeBinaryChunk(ByteBuffer.wrap(bytes));
  }

  public SerializationChunk deserializeToChunk(@Nonnull File file) throws IOException {
    return deserializeToChunk(Files.readAllBytes(file.toPath()));
  }

  public SerializationChunk deserializeToChunk(@Nonnull InputStream inputStream)
      throws IOException {
    return deserializeToChunk(readAllBytes(inputStream));
  }

  public List<Node> deserializeToNodes(@Nonnull byte[] bytes) throws IOException {
    return deserializeDirectToNodes(ByteBuffer.wrap(bytes));
  }

  public List<Node> deserializeToNodes(@Nonnull File file) throws IOException {
    return deserializeToNodes(Files.readAllBytes(file.toPath()));
  }

  public List<Node> deserializeToNodes(@Nonnull InputStream inputStream) throws IOException {
    return deserializeToNodes(readAllBytes(inputStream));
  }

  // ===========================================================================
  // Serialization internals
  // ===========================================================================

  private byte[] serializeToBytes(List<ClassifierInstance<?>> nodes) throws IOException {
    int sz = nodes.size();
    Intern intern = new Intern();

    // --- Pass 1: build type table; cache feature lists; intern all strings ---
    List<TypeEntry> types = new ArrayList<>();
    Map<MetaPointer, Integer> typeByClassifierMP = new HashMap<>();
    int[] typeIndexPerNode = new int[sz];

    for (int i = 0; i < sz; i++) {
      ClassifierInstance<?> node = nodes.get(i);
      Classifier<?> classifier = node.getClassifier();
      MetaPointer classifierMP = MetaPointer.from(classifier);

      Integer ti = typeByClassifierMP.get(classifierMP);
      if (ti == null) {
        ti = types.size();
        TypeEntry te = new TypeEntry();
        te.classifierMPI = intern.metaPointer(classifierMP);

        List<Property> props = classifier.allProperties();
        te.propertyMPIs = new int[props.size()];
        te.properties = new Property[props.size()];
        for (int j = 0; j < props.size(); j++) {
          te.propertyMPIs[j] = intern.metaPointer(MetaPointer.from(props.get(j)));
          te.properties[j] = props.get(j);
        }
        List<Containment> conts = classifier.allContainments();
        te.containmentMPIs = new int[conts.size()];
        te.containments = new Containment[conts.size()];
        for (int j = 0; j < conts.size(); j++) {
          te.containmentMPIs[j] = intern.metaPointer(MetaPointer.from(conts.get(j)));
          te.containments[j] = conts.get(j);
        }
        List<Reference> refs = classifier.allReferences();
        te.referenceMPIs = new int[refs.size()];
        te.references = new Reference[refs.size()];
        for (int j = 0; j < refs.size(); j++) {
          te.referenceMPIs[j] = intern.metaPointer(MetaPointer.from(refs.get(j)));
          te.references[j] = refs.get(j);
        }
        typeByClassifierMP.put(classifierMP, ti);
        types.add(te);
      }
      typeIndexPerNode[i] = ti;

      // Intern all strings for this node (using cached feature arrays from TypeEntry)
      TypeEntry te = types.get(ti);
      intern.string(node.getID());
      ClassifierInstance<?> parent = node.getParent();
      if (parent != null) intern.string(parent.getID());
      for (int j = 0; j < te.properties.length; j++) {
        Object val = node.getPropertyValue(te.properties[j]);
        if (val != null)
          intern.string(dataTypesValuesSerialization.serialize(te.properties[j].getType().getID(), val));
      }
      for (int j = 0; j < te.containments.length; j++) {
        List<? extends Node> children = node.getChildren(te.containments[j]);
        for (int k = 0, m = children.size(); k < m; k++) intern.string(children.get(k).getID());
      }
      for (int j = 0; j < te.references.length; j++) {
        List<ReferenceValue> rvs = node.getReferenceValues(te.references[j]);
        for (int k = 0, m = rvs.size(); k < m; k++) {
          ReferenceValue rv = rvs.get(k);
          String refID = rv.getReferred() == null ? null : rv.getReferred().getID();
          if (builtinsReferenceDangling && ClassifierInstanceUtils.isBuiltinElement(rv.getReferred()))
            refID = null;
          intern.string(refID);
          intern.string(rv.getResolveInfo());
        }
      }
      List<AnnotationInstance> anns = node.getAnnotations();
      for (int j = 0, n = anns.size(); j < n; j++) intern.string(anns.get(j).getID());
    }

    // --- Pass 2: write binary using custom Output with varint encoding ---
    Output out = new Output(Math.max(4096, sz * 64));

    // Header
    out.putBytes(MAGIC, 0, MAGIC.length);
    out.putByte(FORMAT_VERSION);
    out.putUtf8(getLionWebVersion().getVersionString());

    // String table (index 0 = null, not written; entries start at 1)
    List<String> strings = intern.strings;
    out.putVarInt(strings.size() - 1);
    for (int i = 1; i < strings.size(); i++) out.putUtf8(strings.get(i));

    // Language table (index 0 = null sentinel, not written)
    List<LanguageVersion> langs = intern.languages;
    out.putVarInt(langs.size() - 1);
    for (int i = 1; i < langs.size(); i++) {
      LanguageVersion lv = langs.get(i);
      out.putVarInt(intern.string(lv.getKey()));
      out.putVarInt(intern.string(lv.getVersion()));
    }

    // MetaPointer table
    List<MetaPointer> mps = intern.metaPointers;
    out.putVarInt(mps.size());
    for (int i = 0; i < mps.size(); i++) {
      MetaPointer mp = mps.get(i);
      out.putVarInt(intern.language(mp.getLanguageVersion()));
      out.putVarInt(intern.string(mp.getKey()));
    }

    // Type table
    out.putVarInt(types.size());
    for (int i = 0; i < types.size(); i++) {
      TypeEntry te = types.get(i);
      out.putVarInt(te.classifierMPI);
      out.putVarInt(te.propertyMPIs.length);
      for (int j = 0; j < te.propertyMPIs.length; j++) out.putVarInt(te.propertyMPIs[j]);
      out.putVarInt(te.containmentMPIs.length);
      for (int j = 0; j < te.containmentMPIs.length; j++) out.putVarInt(te.containmentMPIs[j]);
      out.putVarInt(te.referenceMPIs.length);
      for (int j = 0; j < te.referenceMPIs.length; j++) out.putVarInt(te.referenceMPIs[j]);
    }

    // Node data
    out.putVarInt(sz);
    for (int i = 0; i < sz; i++) {
      ClassifierInstance<?> node = nodes.get(i);
      TypeEntry te = types.get(typeIndexPerNode[i]);

      out.putVarInt(typeIndexPerNode[i]);
      out.putVarInt(intern.string(node.getID()));
      ClassifierInstance<?> parent = node.getParent();
      out.putVarInt(parent == null ? 0 : intern.string(parent.getID()));

      List<AnnotationInstance> anns = node.getAnnotations();
      out.putVarInt(anns.size());

      for (int j = 0; j < te.properties.length; j++) {
        Object val = node.getPropertyValue(te.properties[j]);
        String strVal =
            val == null ? null : dataTypesValuesSerialization.serialize(te.properties[j].getType().getID(), val);
        out.putVarInt(intern.string(strVal));
      }
      for (int j = 0; j < te.containments.length; j++) {
        List<? extends Node> children = node.getChildren(te.containments[j]);
        out.putVarInt(children.size());
        for (int k = 0, m = children.size(); k < m; k++)
          out.putVarInt(intern.string(children.get(k).getID()));
      }
      for (int j = 0; j < te.references.length; j++) {
        List<ReferenceValue> rvs = node.getReferenceValues(te.references[j]);
        out.putVarInt(rvs.size());
        for (int k = 0, m = rvs.size(); k < m; k++) {
          ReferenceValue rv = rvs.get(k);
          String refID = rv.getReferred() == null ? null : rv.getReferred().getID();
          if (builtinsReferenceDangling && ClassifierInstanceUtils.isBuiltinElement(rv.getReferred()))
            refID = null;
          out.putVarInt(intern.string(refID));
          out.putVarInt(intern.string(rv.getResolveInfo()));
        }
      }
      for (int j = 0, n = anns.size(); j < n; j++) out.putVarInt(intern.string(anns.get(j).getID()));
    }

    return out.toByteArray();
  }

  // ===========================================================================
  // Deserialization — chunk path (public API, for inspection/interop)
  // ===========================================================================

  private SerializationChunk deserializeBinaryChunk(ByteBuffer buf) throws IOException {
    readAndValidateHeader(buf);
    String serializationFormatVersion = readUtf8(buf);

    String[] strings = readStringTable(buf);
    LanguageVersion[] languages = readLanguageTable(buf, strings);
    MetaPointer[] metaPointers = readMetaPointerTable(buf, strings, languages);
    TypeEntry[] typeEntries = readTypeTable(buf);

    int nodeCount = getVarInt(buf);
    SerializationChunk chunk = new SerializationChunk(nodeCount);
    chunk.setSerializationFormatVersion(serializationFormatVersion);
    for (LanguageVersion lv : languages) {
      if (lv != null) chunk.addLanguage(lv);
    }

    for (int ni = 0; ni < nodeCount; ni++) {
      TypeEntry te = typeEntries[getVarInt(buf)];
      String id = strings[getVarInt(buf)];
      String parentID = strings[getVarInt(buf)];
      int annCount = getVarInt(buf);

      SerializedClassifierInstance sci =
          new SerializedClassifierInstance(
              te.propertyMPIs.length, te.containmentMPIs.length, te.referenceMPIs.length, annCount);
      sci.setID(id);
      sci.setParentNodeID(parentID);
      sci.setClassifier(metaPointers[te.classifierMPI]);

      for (int pi = 0; pi < te.propertyMPIs.length; pi++) {
        int siValue = getVarInt(buf);
        if (serializeEmptyFeatures || siValue != 0)
          sci.unsafeAppendPropertyValue(
              SerializedPropertyValue.get(metaPointers[te.propertyMPIs[pi]], strings[siValue]));
      }
      for (int ci = 0; ci < te.containmentMPIs.length; ci++) {
        int childrenCount = getVarInt(buf);
        if (childrenCount > 0 || serializeEmptyFeatures) {
          List<String> children = new ArrayList<>(childrenCount);
          for (int k = 0; k < childrenCount; k++) {
            int childIdx = getVarInt(buf);
            if (childIdx == 0) throw new DeserializationException("Null child ID");
            children.add(strings[childIdx]);
          }
          sci.unsafeAppendContainmentValue(
              new SerializedContainmentValue(metaPointers[te.containmentMPIs[ci]], children));
        }
      }
      for (int ri = 0; ri < te.referenceMPIs.length; ri++) {
        int entryCount = getVarInt(buf);
        SerializedReferenceValue srv =
            new SerializedReferenceValue(metaPointers[te.referenceMPIs[ri]]);
        for (int vi = 0; vi < entryCount; vi++) {
          SerializedReferenceValue.Entry entry = new SerializedReferenceValue.Entry();
          entry.setReference(strings[getVarInt(buf)]);
          entry.setResolveInfo(strings[getVarInt(buf)]);
          srv.addValue(entry);
        }
        if (serializeEmptyFeatures || !srv.getValue().isEmpty())
          sci.unsafeAppendReferenceValue(srv);
      }
      for (int ai = 0; ai < annCount; ai++) sci.addAnnotation(strings[getVarInt(buf)]);

      chunk.addClassifierInstance(sci);
    }
    return chunk;
  }

  // ===========================================================================
  // Deserialization — direct path (skips SerializationChunk intermediary)
  // ===========================================================================

  @SuppressWarnings("unchecked")
  private List<Node> deserializeDirectToNodes(ByteBuffer buf) throws IOException {
    readAndValidateHeader(buf);
    LionWebVersion lwv = LionWebVersion.fromValue(readUtf8(buf));

    String[] strings = readStringTable(buf);
    LanguageVersion[] languages = readLanguageTable(buf, strings);
    MetaPointer[] metaPointers = readMetaPointerTable(buf, strings, languages);
    TypeEntry[] typeEntries = readTypeTable(buf);

    // Resolve each type entry to live classifier + feature objects (once per type)
    TypeResolution[] typeRes = new TypeResolution[typeEntries.length];
    for (int ti = 0; ti < typeEntries.length; ti++) {
      TypeEntry te = typeEntries[ti];
      Classifier<?> cls = classifierResolver.resolveClassifier(metaPointers[te.classifierMPI]);
      if (cls == null)
        throw new DeserializationException("Cannot resolve classifier " + metaPointers[te.classifierMPI]);
      Property[] props = new Property[te.propertyMPIs.length];
      for (int j = 0; j < te.propertyMPIs.length; j++)
        props[j] = findProperty(cls, metaPointers[te.propertyMPIs[j]].getKey());
      Containment[] conts = new Containment[te.containmentMPIs.length];
      for (int j = 0; j < te.containmentMPIs.length; j++)
        conts[j] = findContainment(cls, metaPointers[te.containmentMPIs[j]].getKey());
      Reference[] refs = new Reference[te.referenceMPIs.length];
      for (int j = 0; j < te.referenceMPIs.length; j++)
        refs[j] = findReference(cls, metaPointers[te.referenceMPIs[j]].getKey());
      typeRes[ti] = new TypeResolution(cls, props, conts, refs);
    }

    int n = getVarInt(buf);

    // --- Pass 1: read all node data into compact parallel arrays ---
    int[] typeIdxs   = new int[n];
    String[] nodeIds  = new String[n];
    String[] parentIds = new String[n];
    Map<Property, Object>[] propValues = new Map[n];
    String[][][] childIDs   = new String[n][][]; // [nodeIdx][contIdx][childIdx]
    String[][][] refReferred = new String[n][][]; // [nodeIdx][refIdx][entryIdx]
    String[][][] refInfo     = new String[n][][]; // [nodeIdx][refIdx][entryIdx]
    String[][] annIDs        = new String[n][];

    for (int ni = 0; ni < n; ni++) {
      int ti = getVarInt(buf);
      typeIdxs[ni] = ti;
      TypeResolution tr = typeRes[ti];

      nodeIds[ni]  = strings[getVarInt(buf)];
      parentIds[ni] = strings[getVarInt(buf)];
      int annCount = getVarInt(buf);

      // Properties: deserialize directly into Map<Property, Object>
      Map<Property, Object> pv = new HashMap<>(tr.properties.length * 2);
      for (int pi = 0; pi < tr.properties.length; pi++) {
        String strVal = strings[getVarInt(buf)];
        Property prop = tr.properties[pi];
        if (strVal != null && prop != null) {
          Object val = dataTypesValuesSerialization.deserialize(prop.getType(), strVal, prop.isRequired());
          if (val != null) pv.put(prop, val);
        }
      }
      propValues[ni] = pv;

      // Containments
      String[][] contChildren = new String[tr.containments.length][];
      for (int ci = 0; ci < tr.containments.length; ci++) {
        int cc = getVarInt(buf);
        String[] cids = new String[cc];
        for (int k = 0; k < cc; k++) {
          int idx = getVarInt(buf);
          if (idx == 0) throw new DeserializationException("Null child ID");
          cids[k] = strings[idx];
        }
        contChildren[ci] = cids;
      }
      childIDs[ni] = contChildren;

      // References
      String[][] referred = new String[tr.references.length][];
      String[][] info     = new String[tr.references.length][];
      for (int ri = 0; ri < tr.references.length; ri++) {
        int ec = getVarInt(buf);
        String[] rr = new String[ec];
        String[] ri2 = new String[ec];
        for (int vi = 0; vi < ec; vi++) {
          rr[vi]  = strings[getVarInt(buf)];
          ri2[vi] = strings[getVarInt(buf)];
        }
        referred[ri] = rr;
        info[ri]     = ri2;
      }
      refReferred[ni] = referred;
      refInfo[ni]     = info;

      // Annotations
      String[] aids = new String[annCount];
      for (int ai = 0; ai < annCount; ai++) aids[ai] = strings[getVarInt(buf)];
      annIDs[ni] = aids;
    }

    // --- Pass 2: topological sort (leaves first for correct instantiation order) ---
    // Build ID→index map and compute parent index and childrenInChunk count
    Map<String, Integer> idToIdx = new HashMap<>(n * 2);
    for (int i = 0; i < n; i++) {
      if (nodeIds[i] != null) idToIdx.put(nodeIds[i], i);
    }
    int[] parentIdx      = new int[n];
    int[] childrenInChunk = new int[n];
    Arrays.fill(parentIdx, -1);
    for (int i = 0; i < n; i++) {
      String pid = parentIds[i];
      if (pid != null) {
        Integer pi = idToIdx.get(pid);
        if (pi != null) {
          parentIdx[i] = pi;
          childrenInChunk[pi]++;
        }
      }
    }
    int[] processOrder = new int[n];
    int count = 0;
    ArrayDeque<Integer> queue = new ArrayDeque<>();
    for (int i = 0; i < n; i++) {
      if (childrenInChunk[i] == 0) queue.offer(i);
    }
    while (!queue.isEmpty()) {
      int i = queue.poll();
      processOrder[count++] = i;
      int pi = parentIdx[i];
      if (pi != -1 && --childrenInChunk[pi] == 0) queue.offer(pi);
    }
    if (count < n) { // cycle or disconnected nodes — append remainder
      for (int i = 0; i < n; i++) {
        if (childrenInChunk[i] > 0) processOrder[count++] = i;
      }
    }

    // --- Pass 3: instantiate in leaf-first order ---
    ClassifierInstance<?>[] instances = new ClassifierInstance<?>[n];
    Map<String, ClassifierInstance<?>> byID = new HashMap<>(n * 2);

    for (int si = 0; si < n; si++) {
      int ni = processOrder[si];
      TypeResolution tr = typeRes[typeIdxs[ni]];
      String id = nodeIds[ni];

      // Minimal stub for the instantiator (id + classifier + non-empty containments)
      String[][] kids = childIDs[ni];
      int nonEmptyConts = 0;
      for (String[] c : kids) if (c.length > 0) nonEmptyConts++;
      SerializedClassifierInstance stub =
          new SerializedClassifierInstance(0, nonEmptyConts, 0, 0);
      stub.setID(id);
      stub.setClassifier(metaPointers[typeEntries[typeIdxs[ni]].classifierMPI]);
      for (int ci = 0; ci < tr.containments.length; ci++) {
        if (kids[ci].length > 0) {
          stub.unsafeAppendContainmentValue(
              new SerializedContainmentValue(
                  metaPointers[typeEntries[typeIdxs[ni]].containmentMPIs[ci]],
                  Arrays.asList(kids[ci])));
        }
      }

      ClassifierInstance<?> inst =
          instantiator.instantiate(tr.classifier, stub, byID, propValues[ni]);

      // Set any properties the instantiator didn't handle
      for (Map.Entry<Property, Object> e : propValues[ni].entrySet()) {
        Property prop = e.getKey();
        Object val = e.getValue();
        if (!Objects.equals(val, inst.getPropertyValue(prop)))
          inst.setPropertyValue(prop, val);
      }

      instances[ni] = inst;
      if (id != null) byID.put(id, inst);
    }

    // --- Pass 4: wire containments, references, annotations ---
    for (int ni = 0; ni < n; ni++) {
      ClassifierInstance<?> inst = instances[ni];
      TypeResolution tr = typeRes[typeIdxs[ni]];

      // Children
      String[][] kids = childIDs[ni];
      for (int ci = 0; ci < tr.containments.length; ci++) {
        for (String childId : kids[ci]) {
          ClassifierInstance<?> child;
          if (unavailableChildrenPolicy == UnavailableNodePolicy.PROXY_NODES) {
            child = byID.containsKey(childId) ? byID.get(childId) : new ProxyNode(childId);
          } else {
            child = byID.get(childId);
            if (child == null) child = instanceResolver.resolve(childId);
            if (child == null && unavailableChildrenPolicy == UnavailableNodePolicy.THROW_ERROR)
              throw new DeserializationException("Cannot resolve child " + childId);
          }
          if (child != null) ((Node) inst).addChild(tr.containments[ci], (Node) child);
        }
      }

      // References
      for (int ri = 0; ri < tr.references.length; ri++) {
        String[] rr = refReferred[ni][ri];
        String[] ri2 = refInfo[ni][ri];
        for (int vi = 0; vi < rr.length; vi++) {
          String refId = rr[vi];
          Node referred = null;
          if (refId != null) {
            referred = (Node) byID.get(refId);
            if (referred == null) referred = (Node) instanceResolver.resolve(refId);
          }
          if (referred == null && refId != null) {
            switch (unavailableReferenceTargetPolicy) {
              case PROXY_NODES:
                referred = new ProxyNode(refId);
                break;
              case THROW_ERROR:
                throw new DeserializationException("Cannot resolve reference to " + refId);
              default:
                break;
            }
          }
          inst.addReferenceValue(tr.references[ri], new ReferenceValue(referred, ri2[vi]));
        }
      }

      // Proxy parent
      String pid = parentIds[ni];
      if (pid != null && !byID.containsKey(pid)
          && unavailableParentPolicy == UnavailableNodePolicy.PROXY_NODES) {
        ProxyNode proxyParent = new ProxyNode(pid);
        if (inst instanceof HasSettableParent)
          ((HasSettableParent) inst).setParent(proxyParent);
      }

      // Annotations: if this instance is an annotation, attach it to its parent
      if (inst instanceof AnnotationInstance) {
        String parentId = parentIds[ni];
        ClassifierInstance<?> annotated = parentId != null ? byID.get(parentId) : null;
        if (annotated instanceof AbstractClassifierInstance) {
          ((AbstractClassifierInstance<?>) annotated).addAnnotation((AnnotationInstance) inst);
        }
      }
    }

    // Return nodes in original serialization order, filtering to Node instances
    List<Node> result = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      if (instances[i] instanceof Node) result.add((Node) instances[i]);
    }
    return result;
  }

  // ===========================================================================
  // Shared table-reading helpers
  // ===========================================================================

  private void readAndValidateHeader(ByteBuffer buf) {
    byte[] magic = new byte[MAGIC.length];
    buf.get(magic);
    if (!Arrays.equals(magic, MAGIC))
      throw new DeserializationException("Not a LionBin stream: bad magic bytes");
    byte fv = buf.get();
    if (fv != FORMAT_VERSION)
      throw new DeserializationException("Unsupported LionBin format version: " + fv);
  }

  private String[] readStringTable(ByteBuffer buf) {
    int count = getVarInt(buf);
    String[] strings = new String[count + 1];
    strings[0] = null;
    for (int i = 1; i <= count; i++) strings[i] = readUtf8(buf);
    return strings;
  }

  private LanguageVersion[] readLanguageTable(ByteBuffer buf, String[] strings) {
    int count = getVarInt(buf);
    LanguageVersion[] langs = new LanguageVersion[count + 1];
    langs[0] = null;
    for (int i = 1; i <= count; i++) {
      String key = strings[getVarInt(buf)];
      String ver = strings[getVarInt(buf)];
      langs[i] = LanguageVersion.of(key, ver);
    }
    return langs;
  }

  private MetaPointer[] readMetaPointerTable(
      ByteBuffer buf, String[] strings, LanguageVersion[] languages) {
    int count = getVarInt(buf);
    MetaPointer[] mps = new MetaPointer[count];
    for (int i = 0; i < count; i++) {
      LanguageVersion lv = languages[getVarInt(buf)];
      String key = strings[getVarInt(buf)];
      mps[i] = MetaPointer.get(lv.getKey(), lv.getVersion(), key);
    }
    return mps;
  }

  private TypeEntry[] readTypeTable(ByteBuffer buf) {
    int count = getVarInt(buf);
    TypeEntry[] entries = new TypeEntry[count];
    for (int i = 0; i < count; i++) {
      TypeEntry te = new TypeEntry();
      te.classifierMPI = getVarInt(buf);
      int pc = getVarInt(buf);
      te.propertyMPIs = new int[pc];
      for (int j = 0; j < pc; j++) te.propertyMPIs[j] = getVarInt(buf);
      int cc = getVarInt(buf);
      te.containmentMPIs = new int[cc];
      for (int j = 0; j < cc; j++) te.containmentMPIs[j] = getVarInt(buf);
      int rc = getVarInt(buf);
      te.referenceMPIs = new int[rc];
      for (int j = 0; j < rc; j++) te.referenceMPIs[j] = getVarInt(buf);
      entries[i] = te;
    }
    return entries;
  }

  // ===========================================================================
  // Feature lookup helpers (for type resolution)
  // ===========================================================================

  private static Property findProperty(Classifier<?> cls, String key) {
    List<Property> props = cls.allProperties();
    for (int i = 0, n = props.size(); i < n; i++) {
      if (key.equals(props.get(i).getKey())) return props.get(i);
    }
    return null;
  }

  private static Containment findContainment(Classifier<?> cls, String key) {
    List<Containment> conts = cls.allContainments();
    for (int i = 0, n = conts.size(); i < n; i++) {
      if (key.equals(conts.get(i).getKey())) return conts.get(i);
    }
    return null;
  }

  private static Reference findReference(Classifier<?> cls, String key) {
    List<Reference> refs = cls.allReferences();
    for (int i = 0, n = refs.size(); i < n; i++) {
      if (key.equals(refs.get(i).getKey())) return refs.get(i);
    }
    return null;
  }

  // ===========================================================================
  // Node collection helper (serialization)
  // ===========================================================================

  private static void collectNonProxyNoDup(
      ClassifierInstance<?> node, List<ClassifierInstance<?>> result, Set<String> seenIDs) {
    if (node instanceof ProxyNode) return;
    String id = node.getID();
    if (id != null && !seenIDs.add(id)) return;
    result.add(node);
    List<AnnotationInstance> anns = node.getAnnotations();
    for (int i = 0, n = anns.size(); i < n; i++)
      collectNonProxyNoDup(anns.get(i), result, seenIDs);
    Classifier<?> classifier = node.getClassifier();
    List<Containment> conts = classifier.allContainments();
    for (int i = 0, n = conts.size(); i < n; i++) {
      List<? extends Node> children = node.getChildren(conts.get(i));
      for (int j = 0, m = children.size(); j < m; j++)
        collectNonProxyNoDup(children.get(j), result, seenIDs);
    }
  }

  // ===========================================================================
  // I/O utilities
  // ===========================================================================

  private static int getVarInt(ByteBuffer buf) {
    int result = 0, shift = 0;
    while (true) {
      byte b = buf.get();
      result |= (b & 0x7F) << shift;
      if ((b & 0x80) == 0) return result;
      shift += 7;
    }
  }

  private static String readUtf8(ByteBuffer buf) {
    int len = getVarInt(buf);
    byte[] bytes = new byte[len];
    buf.get(bytes);
    return new String(bytes, StandardCharsets.UTF_8);
  }

  private static byte[] readAllBytes(InputStream in) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buf = new byte[8192];
    int n;
    while ((n = in.read(buf)) != -1) baos.write(buf, 0, n);
    return baos.toByteArray();
  }

  // ===========================================================================
  // Inner classes
  // ===========================================================================

  /** Custom growable byte-array writer. All integers written as unsigned LEB128 varints. */
  private static final class Output {
    private byte[] buf;
    private int pos;

    Output(int initialCapacity) {
      this.buf = new byte[Math.max(64, initialCapacity)];
    }

    void putVarInt(int v) {
      ensureCapacity(5);
      while ((v & ~0x7F) != 0) {
        buf[pos++] = (byte) ((v & 0x7F) | 0x80);
        v >>>= 7;
      }
      buf[pos++] = (byte) v;
    }

    void putByte(byte b) {
      ensureCapacity(1);
      buf[pos++] = b;
    }

    void putBytes(byte[] src, int off, int len) {
      ensureCapacity(len);
      System.arraycopy(src, off, buf, pos, len);
      pos += len;
    }

    void putUtf8(String s) {
      byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
      putVarInt(bytes.length);
      putBytes(bytes, 0, bytes.length);
    }

    byte[] toByteArray() {
      return Arrays.copyOf(buf, pos);
    }

    private void ensureCapacity(int needed) {
      if (pos + needed > buf.length)
        buf = Arrays.copyOf(buf, Math.max(pos + needed, buf.length * 2));
    }
  }

  /** Ordered type descriptor: classifier + feature metapointer indices + cached feature objects. */
  private static final class TypeEntry {
    int classifierMPI;
    int[] propertyMPIs;
    int[] containmentMPIs;
    int[] referenceMPIs;
    // Cached during serialization (null when built during deserialization)
    Property[] properties;
    Containment[] containments;
    Reference[] references;
  }

  /** Resolved type: live classifier and feature objects for direct deserialization. */
  private static final class TypeResolution {
    final Classifier<?> classifier;
    final Property[] properties;
    final Containment[] containments;
    final Reference[] references;

    TypeResolution(
        Classifier<?> classifier,
        Property[] properties,
        Containment[] containments,
        Reference[] references) {
      this.classifier = classifier;
      this.properties = properties;
      this.containments = containments;
      this.references = references;
    }
  }

  /** Intern tables for strings, languages, and metapointers during serialization. */
  private static final class Intern {
    final List<String> strings = new ArrayList<>();
    final Map<String, Integer> stringMap = new HashMap<>();
    final List<LanguageVersion> languages = new ArrayList<>();
    final Map<LanguageVersion, Integer> languageMap = new HashMap<>();
    final List<MetaPointer> metaPointers = new ArrayList<>();
    final Map<MetaPointer, Integer> metaPointerMap = new HashMap<>();

    Intern() {
      strings.add(null);
      stringMap.put(null, 0);
      languages.add(null);
      languageMap.put(null, 0);
    }

    int string(String s) {
      Integer idx = stringMap.get(s);
      if (idx != null) return idx;
      int i = strings.size();
      strings.add(s);
      stringMap.put(s, i);
      return i;
    }

    int language(LanguageVersion lv) {
      Integer idx = languageMap.get(lv);
      if (idx != null) return idx;
      string(lv.getKey());
      string(lv.getVersion());
      int i = languages.size();
      languages.add(lv);
      languageMap.put(lv, i);
      return i;
    }

    int metaPointer(MetaPointer mp) {
      Integer idx = metaPointerMap.get(mp);
      if (idx != null) return idx;
      language(mp.getLanguageVersion());
      string(mp.getKey());
      int i = metaPointers.size();
      metaPointers.add(mp);
      metaPointerMap.put(mp, i);
      return i;
    }
  }
}
