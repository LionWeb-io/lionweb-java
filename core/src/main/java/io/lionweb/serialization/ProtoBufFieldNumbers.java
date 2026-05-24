package io.lionweb.serialization;

/** Protobuf field numbers for the LionWeb binary serialization format. */
final class ProtoBufFieldNumbers {
  private ProtoBufFieldNumbers() {}

  // PBChunk top-level fields
  static final int CHUNK_SERIALIZATION_FORMAT_VERSION = 1;
  static final int CHUNK_INTERNED_STRINGS = 2;
  static final int CHUNK_INTERNED_META_POINTERS = 3;
  static final int CHUNK_INTERNED_LANGUAGES = 4;
  static final int CHUNK_NODES = 5;

  // PBNode fields
  static final int NODE_SI_ID = 1;
  static final int NODE_MPI_CLASSIFIER = 2;
  static final int NODE_PROPERTIES = 3;
  static final int NODE_CONTAINMENTS = 4;
  static final int NODE_REFERENCES = 5;
  static final int NODE_SI_ANNOTATIONS = 6;
  static final int NODE_SI_PARENT = 7;

  // PBMetaPointer fields
  static final int META_POINTER_LI_LANGUAGE = 1;
  static final int META_POINTER_SI_KEY = 2;

  // PBLanguage fields
  static final int LANGUAGE_SI_KEY = 1;
  static final int LANGUAGE_SI_VERSION = 2;

  // PBProperty fields
  static final int PROPERTY_MPI = 1;
  static final int PROPERTY_SI_VALUE = 2;

  // PBContainment fields
  static final int CONTAINMENT_MPI = 1;
  static final int CONTAINMENT_SI_CHILDREN = 2;

  // PBReference fields
  static final int REFERENCE_MPI = 1;
  static final int REFERENCE_ENTRIES = 2;

  // PBReferenceValue fields
  static final int REFERENCE_VALUE_SI_RESOLVE_INFO = 1;
  static final int REFERENCE_VALUE_SI_REFERRED = 2;
}
