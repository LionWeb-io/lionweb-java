package io.lionweb.lioncore.java.serialization;

/**
 * When deserializing a tree, we either extract an entire partitions (and perhaps all the referred
 * partitions) or we will get references to nodes (for example, parents or ancestors) outside of the
 * scope of the tree extracted. This policy specifies what we do with such references.
 */
public enum UnavailableNodePolicy {
  NULL_REFERENCES,
  THROW_ERROR,
  PROXY_NODES
}
