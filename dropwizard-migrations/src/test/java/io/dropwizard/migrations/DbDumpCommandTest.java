package io.dropwizard.migrations;

import net.jcip.annotations.NotThreadSafe;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@NotThreadSafe
class DbDumpCommandTest {

    private static final List<String> ATTRIBUTE_NAMES = Arrays.asList("columns", "foreign-keys", "indexes",
            "primary-keys", "sequences", "tables", "unique-constraints", "views");
    private static DocumentBuilder xmlParser;

    private final DbDumpCommand<TestMigrationConfiguration> dumpCommand =
            new DbDumpCommand<>(new TestMigrationDatabaseConfiguration(), TestMigrationConfiguration.class, "migrations.xml");
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private TestMigrationConfiguration existedDbConf;

    @BeforeAll
    public static void initXmlParser() throws Exception {
        xmlParser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }

    @BeforeEach
    void setUp() throws Exception {
        final URI existedDbPathUri = Objects.requireNonNull(getClass().getResource("/test-db.mv.db")).toURI();
        final String existedDbPath = Paths.get(existedDbPathUri).toString();
        final String existedDbUrl = "jdbc:h2:" + existedDbPath.substring(0, existedDbPath.length() - ".mv.db".length());
        existedDbConf = MigrationTestSupport.createConfiguration(existedDbUrl);
        dumpCommand.setOutputStream(new PrintStream(baos));
    }

    @Test
    void testDumpSchema() throws Exception {
        dumpCommand.run(null, new Namespace(ATTRIBUTE_NAMES.stream()
            .collect(Collectors.toMap(a -> a, b -> true))), existedDbConf);

        final Element changeSet = getFirstElement(toXmlDocument(baos).getDocumentElement(), "changeSet");
        assertCreateTable(changeSet);
    }

    @Test
    void testDumpSchemaAndData() throws Exception {
        dumpCommand.run(null, new Namespace(Stream.concat(ATTRIBUTE_NAMES.stream(), Stream.of("data"))
            .collect(Collectors.toMap(a -> a, b -> true))), existedDbConf);

        final NodeList changeSets = toXmlDocument(baos).getDocumentElement().getElementsByTagName("changeSet");
        assertCreateTable((Element) changeSets.item(0));
        assertInsertData((Element) changeSets.item(1));
    }

    @Test
    void testDumpOnlyData() throws Exception {
        dumpCommand.run(null, new Namespace(Collections.singletonMap("data", true)), existedDbConf);

        final Element changeSet = getFirstElement(toXmlDocument(baos).getDocumentElement(), "changeSet");
        assertInsertData(changeSet);
    }

    @Test
    void testWriteToFile() throws Exception {
        final File file = File.createTempFile("migration", ".xml");
        dumpCommand.run(null, new Namespace(Collections.singletonMap("output", file.getAbsolutePath())), existedDbConf);
        // Check that file is exist, and has some XML content (no reason to make a full-blown XML assertion)
        assertThat(file).content(UTF_8)
            .startsWith("<?xml version=\"1.1\" encoding=\"UTF-8\" standalone=\"no\"?>");
    }

    @Test
    void testHelpPage() {
        MigrationTestSupport.createSubparser(dumpCommand).printHelp(new PrintWriter(new OutputStreamWriter(baos, UTF_8), true));
        assertThat(baos.toString(UTF_8)).isEqualTo(String.format(
                "usage: db dump [-h] [--migrations MIGRATIONS-FILE] [--catalog CATALOG]%n" +
                        "          [--schema SCHEMA] [-o OUTPUT] [--tables] [--ignore-tables]%n" +
                        "          [--columns] [--ignore-columns] [--views] [--ignore-views]%n" +
                        "          [--primary-keys] [--ignore-primary-keys] [--unique-constraints]%n" +
                        "          [--ignore-unique-constraints] [--indexes] [--ignore-indexes]%n" +
                        "          [--foreign-keys] [--ignore-foreign-keys] [--sequences]%n" +
                        "          [--ignore-sequences] [--data] [--ignore-data] [file]%n" +
                        "%n" +
                        "Generate a dump of the existing database state.%n" +
                        "%n" +
                        "positional arguments:%n" +
                        "  file                   application configuration file%n" +
                        "%n" +
                        "named arguments:%n" +
                        "  -h, --help             show this help message and exit%n" +
                        "  --migrations MIGRATIONS-FILE%n" +
                        "                         the file containing  the  Liquibase migrations for%n" +
                        "                         the application%n" +
                        "  --catalog CATALOG      Specify  the   database   catalog   (use  database%n" +
                        "                         default if omitted)%n" +
                        "  --schema SCHEMA        Specify the database schema  (use database default%n" +
                        "                         if omitted)%n" +
                        "  -o OUTPUT, --output OUTPUT%n" +
                        "                         Write output to <file> instead of stdout%n" +
                        "%n" +
                        "Tables:%n" +
                        "  --tables               Check for added or removed tables (default)%n" +
                        "  --ignore-tables        Ignore tables%n" +
                        "%n" +
                        "Columns:%n" +
                        "  --columns              Check for  added,  removed,  or  modified  columns%n" +
                        "                         (default)%n" +
                        "  --ignore-columns       Ignore columns%n" +
                        "%n" +
                        "Views:%n" +
                        "  --views                Check  for  added,  removed,   or  modified  views%n" +
                        "                         (default)%n" +
                        "  --ignore-views         Ignore views%n" +
                        "%n" +
                        "Primary Keys:%n" +
                        "  --primary-keys         Check for changed primary keys (default)%n" +
                        "  --ignore-primary-keys  Ignore primary keys%n" +
                        "%n" +
                        "Unique Constraints:%n" +
                        "  --unique-constraints   Check for changed unique constraints (default)%n" +
                        "  --ignore-unique-constraints%n" +
                        "                         Ignore unique constraints%n" +
                        "%n" +
                        "Indexes:%n" +
                        "  --indexes              Check for changed indexes (default)%n" +
                        "  --ignore-indexes       Ignore indexes%n" +
                        "%n" +
                        "Foreign Keys:%n" +
                        "  --foreign-keys         Check for changed foreign keys (default)%n" +
                        "  --ignore-foreign-keys  Ignore foreign keys%n" +
                        "%n" +
                        "Sequences:%n" +
                        "  --sequences            Check for changed sequences (default)%n" +
                        "  --ignore-sequences     Ignore sequences%n" +
                        "%n" +
                        "Data:%n" +
                        "  --data                 Check for changed data%n" +
                        "  --ignore-data          Ignore data (default)%n"));
    }


    private static Document toXmlDocument(ByteArrayOutputStream baos) throws SAXException, IOException {
        return xmlParser.parse(new ByteArrayInputStream(baos.toByteArray()));
    }

    /**
     * Assert correctness of a change set with creation of a table
     *
     * @param changeSet actual XML element
     */
    private static void assertCreateTable(Element changeSet) {
        final Element createTable = getFirstElement(changeSet, "createTable");

        assertThat(createTable.getAttribute("catalogName")).isEqualTo("TEST-DB");
        assertThat(createTable.getAttribute("schemaName")).isEqualTo("PUBLIC");
        assertThat(createTable.getAttribute("tableName")).isEqualTo("PERSONS");

        final NodeList columns = createTable.getElementsByTagName("column");

        assertThat(columns.item(0))
            .isInstanceOfSatisfying(Element.class, column -> assertThat(column)
                .satisfies(idColumn -> assertThat(idColumn.getAttribute("autoIncrement")).isEqualTo("true"))
                .satisfies(idColumn -> assertThat(idColumn.getAttribute("name")).isEqualTo("ID"))
                .satisfies(idColumn -> assertThat(idColumn.getAttribute("type")).isEqualTo("INT"))
                .extracting(idColumn -> getFirstElement(idColumn, "constraints"))
                .satisfies(idColumnConstraints -> assertThat(idColumnConstraints.getAttribute("primaryKey")).isEqualTo("true"))
                .satisfies(idColumnConstraints -> assertThat(idColumnConstraints.getAttribute("primaryKeyName")).isEqualTo("PK_PERSONS")));

        assertThat(columns.item(1))
            .isInstanceOfSatisfying(Element.class, column -> assertThat(column)
                .satisfies(nameColumn -> assertThat(nameColumn.getAttribute("name")).isEqualTo("NAME"))
                .satisfies(nameColumn -> assertThat(nameColumn.getAttribute("type")).isEqualTo("VARCHAR(256)"))
                .extracting(nameColumn -> getFirstElement(nameColumn, "constraints"))
                .satisfies(nameColumnConstraints -> assertThat(nameColumnConstraints.getAttribute("nullable")).isEqualTo("false")));

        assertThat(columns.item(2))
            .isInstanceOfSatisfying(Element.class, column -> assertThat(column)
                .satisfies(emailColumn -> assertThat(emailColumn.getAttribute("name")).isEqualTo("EMAIL"))
                .satisfies(emailColumn -> assertThat(emailColumn.getAttribute("type")).isEqualTo("VARCHAR(128)")));
    }

    /**
     * Assert a correctness of a change set with insertion data into a table
     *
     * @param changeSet actual XML element
     */
    private static void assertInsertData(Element changeSet) {
        final Element insert = getFirstElement(changeSet, "insert");

        assertThat(insert.getAttribute("catalogName")).isEqualTo("TEST-DB");
        assertThat(insert.getAttribute("schemaName")).isEqualTo("PUBLIC");
        assertThat(insert.getAttribute("tableName")).isEqualTo("PERSONS");

        final NodeList columns = insert.getElementsByTagName("column");

        assertThat(columns.item(0))
            .isInstanceOfSatisfying(Element.class, column -> assertThat(column)
                .satisfies(idColumn -> assertThat(idColumn.getAttribute("name")).isEqualTo("ID"))
                .satisfies(idColumn -> assertThat(idColumn.getAttribute("valueNumeric")).isEqualTo("1")));

        assertThat(columns.item(1))
            .isInstanceOfSatisfying(Element.class, column -> assertThat(column)
                .satisfies(nameColumn -> assertThat(nameColumn.getAttribute("name")).isEqualTo("NAME"))
                .satisfies(nameColumn -> assertThat(nameColumn.getAttribute("value")).isEqualTo("Bill Smith")));

        assertThat(columns.item(2))
            .isInstanceOfSatisfying(Element.class, column -> assertThat(column)
                .satisfies(nameColumn -> assertThat(nameColumn.getAttribute("name")).isEqualTo("EMAIL"))
                .satisfies(nameColumn -> assertThat(nameColumn.getAttribute("value")).isEqualTo("bill@smith.me")));
    }

    private static Element getFirstElement(Element root, String tagName) {
        return (Element) root.getElementsByTagName(tagName).item(0);
    }
}
