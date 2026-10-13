package io.lionweb.archive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.HistorySupport;
import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.client.inmemory.InMemoryServer;
import io.lionweb.language.Concept;
import io.lionweb.language.Language;
import io.lionweb.language.assigners.CommonIDAssigners;
import io.lionweb.language.assigners.CommonKeyAssigners;
import io.lionweb.model.Node;
import io.lionweb.model.impl.DynamicNode;
import io.lionweb.serialization.Instantiator;
import io.lionweb.serialization.ProtoBufSerialization;
import io.lionweb.serialization.SerializationProvider;
import io.lionweb.serialization.data.SerializationChunk;
import io.lionweb.utils.ModelComparator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.Test;

public class LionWebArchiveTest {

  private Company createAcmeCompany() {
    Company acme = new Company("c-1", "Acme Inc.");

    Role ceo = acme.addRole("r-1", "CEO");
    ceo.setSalaryBand(50_000, 200_000);

    Role cfo = acme.addRole("r-2", "CFO");
    cfo.setSalaryBand(40_000, 150_000);

    Role developer = acme.addRole("r-3", "Developer");
    developer.setSalaryBand(30_000, 70_000);

    Role salesman = acme.addRole("r-4", "Salesman");
    salesman.setSalaryBand(30_000, 80_000);

    Department marketing = acme.addDepartment("d-1", "Marketing");
    Department sales = acme.addDepartment("d-2", "Sales");
    Department production = acme.addDepartment("d-3", "Production");

    Employee employee1 = acme.addEmployee("e-1", "Gino", "De pinis");
    employee1.addRole(ceo);

    Employee employee2 = acme.addEmployee("e-2", "Ginetto", "De pinis");
    employee2.addRole(cfo);

    Employee employee3 = acme.addEmployee("e-3", "Tizio", "Caius");
    employee3.addRole(salesman);
    employee3.addDepartment(sales);
    return acme;
  }

  @Test
  public void storeAndLoadArchiveFromRepository() throws IOException {
    InMemoryServer server = new InMemoryServer();
    server.createRepository(
        new RepositoryConfiguration(
            "CompaniesRepo", LionWebVersion.v2023_1, HistorySupport.DISABLED));

    ProtoBufSerialization serialization =
        SerializationProvider.getStandardProtoBufSerialization(LionWebVersion.v2023_1);
    serialization.registerLanguage(CompanyLanguage.getLanguage());
    SerializationChunk chunk = serialization.serializeTreeToSerializationChunk(createAcmeCompany());
    server.createPartitionFromChunk("CompaniesRepo", chunk.getClassifierInstances());

    File archiveFile = Files.createTempFile("lionweb-archive", ".zip").toFile();
    RepositoryStorage.store(archiveFile, server, "CompaniesRepo");

    server.createRepository(
        new RepositoryConfiguration(
            "ReplicatedCompaniesRepo", LionWebVersion.v2023_1, HistorySupport.DISABLED));
    RepositoryStorage.load(archiveFile, server, "ReplicatedCompaniesRepo", true);
  }

  @Test
  public void loadArchiveAsNodes() throws IOException {
    InMemoryServer server = new InMemoryServer();
    server.createRepository(
        new RepositoryConfiguration(
            "CompaniesRepo", LionWebVersion.v2023_1, HistorySupport.DISABLED));

    ProtoBufSerialization serialization =
        SerializationProvider.getStandardProtoBufSerialization(LionWebVersion.v2023_1);
    serialization.registerLanguage(CompanyLanguage.getLanguage());
    Company acmeOriginal = createAcmeCompany();
    SerializationChunk chunk = serialization.serializeTreeToSerializationChunk(acmeOriginal);
    server.createPartitionFromChunk("CompaniesRepo", chunk.getClassifierInstances());

    File archiveFile = Files.createTempFile("lionweb-archive", ".zip").toFile();
    RepositoryStorage.store(archiveFile, server, "CompaniesRepo");

    ProtoBufSerialization protoBufSerialization =
        SerializationProvider.getStandardProtoBufSerialization(LionWebVersion.v2023_1);
    protoBufSerialization
        .getInstantiator()
        .registerCustomDeserializer(
            CompanyLanguage.getCompany().getID(),
            (Instantiator.ClassifierSpecificInstantiator<Company>)
                (classifier,
                    serializedClassifierInstance,
                    deserializedNodesByID,
                    propertiesValues) -> new Company(serializedClassifierInstance.getID()));
    protoBufSerialization
        .getInstantiator()
        .registerCustomDeserializer(
            CompanyLanguage.getRole().getID(),
            (Instantiator.ClassifierSpecificInstantiator<Role>)
                (classifier,
                    serializedClassifierInstance,
                    deserializedNodesByID,
                    propertiesValues) -> new Role(serializedClassifierInstance.getID()));
    protoBufSerialization
        .getInstantiator()
        .registerCustomDeserializer(
            CompanyLanguage.getDepartment().getID(),
            (Instantiator.ClassifierSpecificInstantiator<Department>)
                (classifier,
                    serializedClassifierInstance,
                    deserializedNodesByID,
                    propertiesValues) -> new Department(serializedClassifierInstance.getID()));
    protoBufSerialization
        .getInstantiator()
        .registerCustomDeserializer(
            CompanyLanguage.getEmployee().getID(),
            (Instantiator.ClassifierSpecificInstantiator<Employee>)
                (classifier,
                    serializedClassifierInstance,
                    deserializedNodesByID,
                    propertiesValues) -> new Employee(serializedClassifierInstance.getID()));
    protoBufSerialization.registerLanguage(CompanyLanguage.getLanguage());

    List<Node> nodes = LionWebArchive.loadNodes(archiveFile, protoBufSerialization);
    assertEquals(1, nodes.size());
    assertTrue(nodes.get(0) instanceof Company);
    assertTrue(ModelComparator.areEquivalent(acmeOriginal, (Company) nodes.get(0)));
  }

  @Test
  public void loadArchiveAsNodesWithPreLoadingLanguages() throws IOException {
    InMemoryServer server = new InMemoryServer();
    server.createRepository(
        new RepositoryConfiguration(
            "CompaniesRepo", LionWebVersion.v2023_1, HistorySupport.DISABLED));

    ProtoBufSerialization serialization =
        SerializationProvider.getStandardProtoBufSerialization(LionWebVersion.v2023_1);
    serialization.registerLanguage(CompanyLanguage.getLanguage());
    Company acmeOriginal = createAcmeCompany();
    SerializationChunk chunk = serialization.serializeTreeToSerializationChunk(acmeOriginal);
    server.createPartitionFromChunk("CompaniesRepo", chunk.getClassifierInstances());

    File archiveFile = Files.createTempFile("lionweb-archive", ".zip").toFile();
    RepositoryStorage.store(archiveFile, server, "CompaniesRepo");

    ProtoBufSerialization protoBufSerialization =
        SerializationProvider.getStandardProtoBufSerialization(LionWebVersion.v2023_1);
    protoBufSerialization.enableDynamicNodes();
    protoBufSerialization.registerLanguage(CompanyLanguage.getLanguage());

    List<Node> nodes = LionWebArchive.loadNodes(archiveFile, protoBufSerialization);
    assertEquals(1, nodes.size());
    Node loadedCompany = nodes.stream().filter(n -> !(n instanceof Language)).findFirst().get();
    assertTrue(loadedCompany instanceof DynamicNode);
    assertTrue(ModelComparator.areEquivalent(acmeOriginal, loadedCompany));
  }

  @Test
  public void loadArchiveAsNodesWithSelfLoadingLanguages() throws IOException {
    InMemoryServer server = new InMemoryServer();
    server.createRepository(
        new RepositoryConfiguration(
            "CompaniesRepo", LionWebVersion.v2023_1, HistorySupport.DISABLED));

    ProtoBufSerialization serialization =
        SerializationProvider.getStandardProtoBufSerialization(LionWebVersion.v2023_1);
    serialization.registerLanguage(CompanyLanguage.getLanguage());
    Company acmeOriginal = createAcmeCompany();
    SerializationChunk chunk = serialization.serializeTreeToSerializationChunk(acmeOriginal);
    server.createPartitionFromChunk("CompaniesRepo", chunk.getClassifierInstances());
    SerializationChunk languageChunk =
        serialization.serializeTreeToSerializationChunk(CompanyLanguage.getLanguage());
    server.createPartitionFromChunk("CompaniesRepo", languageChunk.getClassifierInstances());

    File archiveFile = Files.createTempFile("lionweb-archive", ".zip").toFile();
    RepositoryStorage.store(archiveFile, server, "CompaniesRepo");

    ProtoBufSerialization protoBufSerialization =
        SerializationProvider.getStandardProtoBufSerialization(LionWebVersion.v2023_1);
    protoBufSerialization.enableDynamicNodes();

    List<Node> nodes = LionWebArchive.loadNodes(archiveFile, protoBufSerialization);
    assertEquals(1, nodes.size());
    Node loadedCompany = nodes.stream().filter(n -> !(n instanceof Language)).findFirst().get();
    assertTrue(loadedCompany instanceof DynamicNode);
    assertTrue(ModelComparator.areEquivalent(acmeOriginal, loadedCompany));
  }

  private static List<SerializationChunk> createCompanyChunks(int numCompanies) {
    ProtoBufSerialization serialization =
        SerializationProvider.getStandardProtoBufSerialization(LionWebVersion.v2023_1);
    Random rnd = new Random(42);
    List<SerializationChunk> chunks = new ArrayList<>();
    for (int i = 0; i < numCompanies; i++) {
      chunks.add(
          serialization.serializeTreeToSerializationChunk(
              ArchivePerformanceTest.createCompany(rnd, 5, 3, 50)));
    }
    return chunks;
  }

  private static String rootId(SerializationChunk chunk) {
    return chunk.getClassifierInstances().stream()
        .filter(n -> n.getParentNodeID() == null)
        .findFirst()
        .get()
        .getID();
  }

  private static class CollectingLoader implements LionWebArchive.Loader {
    LionWebVersion lionWebVersion;
    final List<SerializationChunk> languageChunks = new ArrayList<>();
    final List<SerializationChunk> partitionChunks = new ArrayList<>();
    boolean languagesLoaded = false;
    boolean partitionsLoaded = false;

    @Override
    public void setLwVersion(LionWebVersion lionWebVersion) {
      this.lionWebVersion = lionWebVersion;
    }

    @Override
    public void addLanguageChunk(SerializationChunk chunk) {
      assertTrue(partitionChunks.isEmpty());
      languageChunks.add(chunk);
    }

    @Override
    public void languagesLoaded() {
      languagesLoaded = true;
    }

    @Override
    public void addPartitionChunk(SerializationChunk chunk) {
      assertTrue(languagesLoaded);
      partitionChunks.add(chunk);
    }

    @Override
    public void partitionsLoaded() {
      partitionsLoaded = true;
    }
  }

  @Test
  public void writerStoresChunksIncrementallyAndLoadPreservesOrder() throws IOException {
    ProtoBufSerialization serialization =
        SerializationProvider.getStandardProtoBufSerialization(LionWebVersion.v2023_1);
    SerializationChunk languageChunk =
        serialization.serializeTreeToSerializationChunk(CompanyLanguage.getLanguage());
    List<SerializationChunk> partitionChunks = createCompanyChunks(60);

    File archiveFile = Files.createTempFile("lionweb-archive", ".lwa").toFile();
    try (LionWebArchive.Writer writer =
        new LionWebArchive.Writer(archiveFile, LionWebVersion.v2023_1)) {
      // Languages and partitions can be interleaved
      writer.addPartitionChunk(partitionChunks.get(0));
      writer.addLanguageChunk(languageChunk);
      for (SerializationChunk chunk : partitionChunks.subList(1, partitionChunks.size())) {
        writer.addPartitionChunk(chunk);
      }
    }

    CollectingLoader loader = new CollectingLoader();
    LionWebArchive.load(archiveFile, loader);
    assertEquals(LionWebVersion.v2023_1, loader.lionWebVersion);
    assertTrue(loader.languagesLoaded);
    assertTrue(loader.partitionsLoaded);
    assertEquals(1, loader.languageChunks.size());
    assertEquals(rootId(languageChunk), rootId(loader.languageChunks.get(0)));
    assertEquals(
        partitionChunks.stream().map(LionWebArchiveTest::rootId).collect(Collectors.toList()),
        loader.partitionChunks.stream()
            .map(LionWebArchiveTest::rootId)
            .collect(Collectors.toList()));
    for (int i = 0; i < partitionChunks.size(); i++) {
      assertEquals(
          partitionChunks.get(i).getClassifierInstances().size(),
          loader.partitionChunks.get(i).getClassifierInstances().size());
    }
  }

  @Test
  public void writerCannotBeUsedAfterClose() throws IOException {
    File archiveFile = Files.createTempFile("lionweb-archive", ".lwa").toFile();
    LionWebArchive.Writer writer = new LionWebArchive.Writer(archiveFile, LionWebVersion.v2023_1);
    writer.close();
    // Closing twice is allowed
    writer.close();
    SerializationChunk chunk = createCompanyChunks(1).get(0);
    assertThrows(IllegalStateException.class, () -> writer.addPartitionChunk(chunk));

    CollectingLoader loader = new CollectingLoader();
    LionWebArchive.load(archiveFile, loader);
    assertTrue(loader.languageChunks.isEmpty());
    assertTrue(loader.partitionChunks.isEmpty());
  }

  @Test
  public void storeWithCompressionLevel() throws IOException {
    List<SerializationChunk> partitionChunks = createCompanyChunks(10);

    File uncompressed = Files.createTempFile("lionweb-archive", ".lwa").toFile();
    LionWebArchive.store(uncompressed, LionWebVersion.v2023_1, null, partitionChunks);
    File compressed = Files.createTempFile("lionweb-archive", ".lwa").toFile();
    LionWebArchive.store(compressed, LionWebVersion.v2023_1, null, partitionChunks, 9);

    assertTrue(compressed.length() < uncompressed.length());

    CollectingLoader loader = new CollectingLoader();
    LionWebArchive.load(compressed, loader);
    assertEquals(partitionChunks.size(), loader.partitionChunks.size());

    assertThrows(
        IllegalArgumentException.class,
        () -> LionWebArchive.store(compressed, LionWebVersion.v2023_1, null, null, 10));
    assertThrows(
        IllegalArgumentException.class,
        () -> new LionWebArchive.Writer(compressed, LionWebVersion.v2023_1, -2));
  }

  @Test
  public void archiveFollowsPublicFormatConstants() throws IOException {
    ProtoBufSerialization serialization =
        SerializationProvider.getStandardProtoBufSerialization(LionWebVersion.v2023_1);
    SerializationChunk languageChunk =
        serialization.serializeTreeToSerializationChunk(CompanyLanguage.getLanguage());
    SerializationChunk partitionChunk = createCompanyChunks(1).get(0);

    File archiveFile =
        Files.createTempFile("lionweb-archive", "." + LionWebArchive.STANDARD_EXTENSION).toFile();
    LionWebArchive.store(
        archiveFile,
        LionWebVersion.v2023_1,
        Collections.singletonList(languageChunk),
        Collections.singletonList(partitionChunk));

    try (ZipFile zipFile = new ZipFile(archiveFile)) {
      assertNotNull(zipFile.getEntry(LionWebArchive.METADATA_PATH));
      assertNotNull(
          zipFile.getEntry(
              LionWebArchive.LANGUAGES_DIRECTORY
                  + rootId(languageChunk)
                  + LionWebArchive.CHUNK_EXTENSION));
      ZipEntry partitionEntry =
          zipFile.getEntry(
              LionWebArchive.PARTITIONS_DIRECTORY
                  + rootId(partitionChunk)
                  + LionWebArchive.CHUNK_EXTENSION);
      assertNotNull(partitionEntry);
      assertEquals(3, zipFile.size());
    }
  }

  private static Language createLanguageDependingOnCompanyLanguage() {
    Language language = new Language(LionWebVersion.v2023_1, "ExtendedCompanyLanguage");
    language.setVersion("v1.0");
    language.addDependency(CompanyLanguage.getLanguage());
    Concept startup = new Concept(LionWebVersion.v2023_1, "Startup");
    startup.setExtendedConcept(CompanyLanguage.getCompany());
    startup.setPartition(true);
    language.addElement(startup);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(language);
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(language);
    return language;
  }

  @Test
  public void loadNodesWithLanguageDependingOnLanguageNotInArchive() throws IOException {
    ProtoBufSerialization serialization =
        SerializationProvider.getStandardProtoBufSerialization(LionWebVersion.v2023_1);
    serialization.registerLanguage(CompanyLanguage.getLanguage());
    SerializationChunk extendedLanguageChunk =
        serialization.serializeTreeToSerializationChunk(createLanguageDependingOnCompanyLanguage());
    Company acmeOriginal = createAcmeCompany();
    SerializationChunk partitionChunk =
        serialization.serializeTreeToSerializationChunk(acmeOriginal);

    File archiveFile = Files.createTempFile("lionweb-archive", ".lwa").toFile();
    LionWebArchive.store(
        archiveFile,
        LionWebVersion.v2023_1,
        Collections.singletonList(extendedLanguageChunk),
        Collections.singletonList(partitionChunk));

    // The language depended upon is neither in the archive nor registered
    ProtoBufSerialization unawareSerialization =
        SerializationProvider.getStandardProtoBufSerialization(LionWebVersion.v2023_1);
    unawareSerialization.enableDynamicNodes();
    IllegalStateException e =
        assertThrows(
            IllegalStateException.class,
            () -> LionWebArchive.loadNodes(archiveFile, unawareSerialization));
    assertTrue(e.getMessage().contains("Unknown dependency"));

    // The language depended upon is registered in the serialization
    ProtoBufSerialization awareSerialization =
        SerializationProvider.getStandardProtoBufSerialization(LionWebVersion.v2023_1);
    awareSerialization.enableDynamicNodes();
    awareSerialization.registerLanguage(CompanyLanguage.getLanguage());
    List<Node> nodes = LionWebArchive.loadNodes(archiveFile, awareSerialization);
    assertEquals(1, nodes.size());
    assertTrue(ModelComparator.areEquivalent(acmeOriginal, nodes.get(0)));

    // Unknown dependencies are explicitly ignored
    ProtoBufSerialization ignoringSerialization =
        SerializationProvider.getStandardProtoBufSerialization(LionWebVersion.v2023_1);
    ignoringSerialization.enableDynamicNodes();
    ignoringSerialization.registerLanguage(CompanyLanguage.getLanguage());
    List<Node> nodesIgnoring = new ArrayList<>();
    LionWebArchive.loadNodes(archiveFile, ignoringSerialization, nodesIgnoring::add, true);
    assertEquals(1, nodesIgnoring.size());
    assertTrue(ModelComparator.areEquivalent(acmeOriginal, nodesIgnoring.get(0)));
  }
}
