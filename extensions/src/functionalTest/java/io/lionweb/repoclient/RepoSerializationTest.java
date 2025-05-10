package io.lionweb.repoclient;

import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.model.ClassifierInstanceUtils;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.impl.DynamicNode;
import io.lionweb.repoclient.api.HistorySupport;
import io.lionweb.repoclient.api.RepositoryConfiguration;
import io.lionweb.repoclient.testing.AbstractRepoClientFunctionalTest;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
public class RepoSerializationTest extends AbstractRepoClientFunctionalTest {

  public RepoSerializationTest() {
    super(LionWebVersion.v2023_1, true);
  }

  private void storePropertiesPartitions(String repositoryName) throws IOException {
    ExtendedLionWebRepoClient client =
            new ExtendedLionWebRepoClient(
                    LionWebVersion.v2023_1, "localhost", getModelRepoPort(), repositoryName);
    client.createRepository(
            new RepositoryConfiguration(
                    repositoryName, LionWebVersion.v2023_1, HistorySupport.DISABLED));

    int nPartitions = 100;
    int nFiles = 10;
    for (int i = 0; i < nPartitions; i++) {
      DynamicNode partition = new DynamicNode("p-" + i, PropertiesLanguage.propertiesPartition);
      client.createPartition(partition);

      for (int j = 0; j < nFiles; j++) {
        DynamicNode file =
                new DynamicNode("p-" + i + "-file-" + j, PropertiesLanguage.propertiesFile);
        ClassifierInstanceUtils.setPropertyValueByName(file, "path", "path-" + j + ".json");

        DynamicNode propertyA =
                new DynamicNode("p-" + i + "-file-" + j + "-a", PropertiesLanguage.property);
        ClassifierInstanceUtils.setPropertyValueByName(propertyA, "name", "A");
        ClassifierInstanceUtils.addChild(file, "properties", propertyA);

        DynamicNode propertyB =
                new DynamicNode("p-" + i + "-file-" + j + "-b", PropertiesLanguage.property);
        ClassifierInstanceUtils.setPropertyValueByName(propertyB, "name", "B");
        ClassifierInstanceUtils.addChild(file, "properties", propertyB);

        DynamicNode propertyC =
                new DynamicNode("p-" + i + "-file-" + j + "-c", PropertiesLanguage.property);
        ClassifierInstanceUtils.setPropertyValueByName(propertyC, "name", "C");
        ClassifierInstanceUtils.addChild(file, "properties", propertyC);

        ClassifierInstanceUtils.addChild(partition, "files", file);
      }
      client.store(partition);
    }

    Set<String> actualPartitionsIDs = new HashSet<>(client.listPartitionsIDs());
    Set<String> expectedPartitionsIDs = IntStream.range(0, 100)
            .mapToObj(i -> "p-" + i)
            .collect(Collectors.toSet());
    assertEquals(expectedPartitionsIDs, actualPartitionsIDs);

    Node p20 = client.retrieve("p-20");
    assertEquals(nFiles, ClassifierInstanceUtils.getChildrenByContainmentName(p20, "files").size());
  }

  @Test
  public void uploadAndDownloadPartitions() throws IOException {
    String repositoryName = "uploadAndDownloadPartitions";
    storePropertiesPartitions(repositoryName);

    ExtendedLionWebRepoClient client =
            new ExtendedLionWebRepoClient(
                    LionWebVersion.v2023_1, "localhost", getModelRepoPort(), repositoryName);
    File zip = new File("/Users/ftomassetti/repos/lionweb-java/pippo.zip");
    RepoSerialization repoSerialization = new RepoSerialization();
    client.getJsonSerialization().registerLanguage(PropertiesLanguage.propertiesLanguage);
    repoSerialization.downloadRepoAsZip(client, zip);

    ExtendedLionWebRepoClient clientCopy1 =
            new ExtendedLionWebRepoClient(
                    LionWebVersion.v2023_1, "localhost", getModelRepoPort(), repositoryName+"-copy1");
    clientCopy1.createRepository(
            new RepositoryConfiguration(
                    repositoryName+"-copy1", LionWebVersion.v2023_1, HistorySupport.DISABLED));
    long t0 = System.currentTimeMillis();
    repoSerialization.simpleUploadZipToRepo(clientCopy1, zip);
    long t1 = System.currentTimeMillis();
    System.out.println("Simple upload took " + (t1-t0) + "ms");

    ExtendedLionWebRepoClient clientCopy2 =
            new ExtendedLionWebRepoClient(
                    LionWebVersion.v2023_1, "localhost", getModelRepoPort(), repositoryName+"-copy2");
    clientCopy2.createRepository(
            new RepositoryConfiguration(
                    repositoryName+"-copy2", LionWebVersion.v2023_1, HistorySupport.DISABLED));
    long t2 = System.currentTimeMillis();
    repoSerialization.bulkUploadZipToRepo(clientCopy2, zip);
    long t3 = System.currentTimeMillis();
    System.out.println("Bulk upload took " + (t3-t2) + "ms");
  }
}
