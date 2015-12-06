package io.dropwizard.migrations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import net.jcip.annotations.NotThreadSafe;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
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
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@NotThreadSafe
public class DbDumpCommandTest extends AbstractMigrationTest {

    private static DocumentBuilder xmlParser;
    private static List<String> attributeNames;

    private final DbDumpCommand<TestMigrationConfiguration> dumpCommand =
            new DbDumpCommand<>(new TestMigrationDatabaseConfiguration(), TestMigrationConfiguration.class);
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private TestMigrationConfiguration existedDbConf;

    @BeforeClass
    public static void initXmlParser() throws Exception {
        xmlParser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        attributeNames = ImmutableList.of("columns", "foreign-keys", "indexes", "primary-keys", "sequences",
                "tables", "unique-constraints", "views");
    }

    @Before
    public void setUp() throws Exception {
        final String existedDbPath = new File(Resources.getResource("test-db.mv.db").toURI()).getAbsolutePath();
        final String existedDbUrl = "jdbc:h2:" + StringUtils.removeEnd(existedDbPath, ".mv.db");
        existedDbConf = createConfiguration(existedDbUrl);
        dumpCommand.setOutputStream(new PrintStream(baos));
    }

    @Test
    public void testDumpSchema() throws Exception {
        final Map<String, Object> attributes = new HashMap<>();
        for (String name : attributeNames) {
            attributes.put(name, true);
        }
        dumpCommand.run(null, new Namespace(attributes), existedDbConf);

        final Element changeSet = getFirstElement(toXmlDocument(baos).getDocumentElement(), "changeSet");
        assertCreateTable(changeSet);
    }

    @Test
    public void testDumpSchemaAndData() throws Exception {
        final Map<String, Object> attributes = new HashMap<>();
        for (String name : Iterables.concat(attributeNames, ImmutableList.of("data"))) {
            attributes.put(name, true);
        }
        dumpCommand.run(null, new Namespace(attributes), existedDbConf);

        final NodeList changeSets = toXmlDocument(baos).getDocumentElement().getElementsByTagName("changeSet");
        assertCreateTable((Element) changeSets.item(0));
        assertInsertData((Element) changeSets.item(1));
    }

    @Test
    public void testDumpOnlyData() throws Exception {
        dumpCommand.run(null, new Namespace(ImmutableMap.of("data", (Object) true)), existedDbConf);

        final Element changeSet = getFirstElement(toXmlDocument(baos).getDocumentElement(), "changeSet");
        assertInsertData(changeSet);
    }

    @Test
    public void testWriteToFile() throws Exception {
        final File file = File.createTempFile("migration", ".xml");
        final Map<String, Object> attributes = ImmutableMap.of("output", (Object) file.getAbsolutePath());
        dumpCommand.run(null, new Namespace(attributes), existedDbConf);
        // Check that file is exist, and has some XML content (no reason to make a full-blown XML assertion)
        assertThat(Files.toString(file, StandardCharsets.UTF_8)).startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
    }

    @Test
    public void testHelpPage() throws Exception {
        createSubparser(dumpCommand).printHelp(new PrintWriter(baos, true));
        assertThat(baos.toString("UTF-8")).isEqualTo(String.format(
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
                        "optional arguments:%n" +
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

        final Element idColumn = (Element) columns.item(0);
        assertThat(idColumn.getAttribute("autoIncrement")).isEqualTo("true");
        assertThat(idColumn.getAttribute("name")).isEqualTo("ID");
        assertThat(idColumn.getAttribute("type")).isEqualTo("INT(10)");
        final Element idColumnConstraints = getFirstElement(idColumn, "constraints");
        assertThat(idColumnConstraints.getAttribute("primaryKey")).isEqualTo("true");
        assertThat(idColumnConstraints.getAttribute("primaryKeyName")).isEqualTo("PK_PERSONS");

        final Element nameColumn = (Element) columns.item(1);
        assertThat(nameColumn.getAttribute("name")).isEqualTo("NAME");
        assertThat(nameColumn.getAttribute("type")).isEqualTo("VARCHAR(256)");
        final Element nameColumnConstraints = getFirstElement(nameColumn, "constraints");
        assertThat(nameColumnConstraints.getAttribute("nullable")).isEqualTo("false");

        final Element emailColumn = (Element) columns.item(2);
        assertThat(emailColumn.getAttribute("name")).isEqualTo("EMAIL");
        assertThat(emailColumn.getAttribute("type")).isEqualTo("VARCHAR(128)");
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

        final Element idColumn = (Element) columns.item(0);
        assertThat(idColumn.getAttribute("name")).isEqualTo("ID");
        assertThat(idColumn.getAttribute("valueNumeric")).isEqualTo("1");

        final Element nameColumn = (Element) columns.item(1);
        assertThat(nameColumn.getAttribute("name")).isEqualTo("NAME");
        assertThat(nameColumn.getAttribute("value")).isEqualTo("Bill Smith");

        final Element emailColumn = (Element) columns.item(2);
        assertThat(emailColumn.getAttribute("name")).isEqualTo("EMAIL");
        assertThat(emailColumn.getAttribute("value")).isEqualTo("bill@smith.me");
    }

    private static Element getFirstElement(Element root, String tagName) {
        return (Element) root.getElementsByTagName(tagName).item(0);
    }
}
