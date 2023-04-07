package org.lionweb.lioncore.java.serialization.data;

import java.util.Objects;
import javax.annotation.Nonnull;
import org.lionweb.lioncore.java.metamodel.Metamodel;

/** The pair Metamodel Key and Metamodel Version identify a specific version of a metamodel. */
public class MetamodelKeyVersion {
  private String key;
  private String version;

  public MetamodelKeyVersion() {}

  public MetamodelKeyVersion(String key, String version) {
    this.key = key;
    this.version = version;
  }

  public static MetamodelKeyVersion fromMetamodel(@Nonnull Metamodel metamodel) {
    Objects.requireNonNull(metamodel, "Metamodel parameter should not be null");
    Objects.requireNonNull(metamodel.getVersion(), "Metamodel version should not be null");
    return new MetamodelKeyVersion(metamodel.getKey(), metamodel.getVersion());
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MetamodelKeyVersion)) return false;
    MetamodelKeyVersion that = (MetamodelKeyVersion) o;
    return Objects.equals(key, that.key) && Objects.equals(version, that.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, version);
  }
}
