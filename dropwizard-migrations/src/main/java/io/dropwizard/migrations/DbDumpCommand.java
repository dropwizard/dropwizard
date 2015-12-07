package io.dropwizard.migrations;

import com.google.common.annotations.VisibleForTesting;
import io.dropwizard.Configuration;
import io.dropwizard.db.DatabaseConfiguration;
import liquibase.CatalogAndSchema;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Data;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.Index;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Sequence;
import liquibase.structure.core.Table;
import liquibase.structure.core.UniqueConstraint;
import liquibase.structure.core.View;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class DbDumpCommand<T extends Configuration> extends AbstractLiquibaseCommand<T> {

    private PrintStream outputStream = System.out;

    @VisibleForTesting
    void setOutputStream(PrintStream outputStream) {
        this.outputStream = outputStream;
    }

    public DbDumpCommand(DatabaseConfiguration<T> strategy, Class<T> configurationClass) {
        super("dump",
              "Generate a dump of the existing database state.",
              strategy,
              configurationClass);
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        subparser.addArgument("-o", "--output")
                 .dest("output")
                 .help("Write output to <file> instead of stdout");

        final ArgumentGroup tables = subparser.addArgumentGroup("Tables");
        tables.addArgument("--tables")
              .action(Arguments.storeTrue())
              .dest("tables")
              .help("Check for added or removed tables (default)");
        tables.addArgument("--ignore-tables")
              .action(Arguments.storeFalse())
              .dest("tables")
              .help("Ignore tables");

        final ArgumentGroup columns = subparser.addArgumentGroup("Columns");
        columns.addArgument("--columns")
               .action(Arguments.storeTrue())
               .dest("columns")
               .help("Check for added, removed, or modified columns (default)");
        columns.addArgument("--ignore-columns")
               .action(Arguments.storeFalse())
               .dest("columns")
               .help("Ignore columns");

        final ArgumentGroup views = subparser.addArgumentGroup("Views");
        views.addArgument("--views")
             .action(Arguments.storeTrue())
             .dest("views")
             .help("Check for added, removed, or modified views (default)");
        views.addArgument("--ignore-views")
             .action(Arguments.storeFalse())
             .dest("views")
             .help("Ignore views");

        final ArgumentGroup primaryKeys = subparser.addArgumentGroup("Primary Keys");
        primaryKeys.addArgument("--primary-keys")
                   .action(Arguments.storeTrue())
                   .dest("primary-keys")
                   .help("Check for changed primary keys (default)");
        primaryKeys.addArgument("--ignore-primary-keys")
                   .action(Arguments.storeFalse())
                   .dest("primary-keys")
                   .help("Ignore primary keys");

        final ArgumentGroup uniqueConstraints = subparser.addArgumentGroup("Unique Constraints");
        uniqueConstraints.addArgument("--unique-constraints")
                         .action(Arguments.storeTrue())
                         .dest("unique-constraints")
                         .help("Check for changed unique constraints (default)");
        uniqueConstraints.addArgument("--ignore-unique-constraints")
                         .action(Arguments.storeFalse())
                         .dest("unique-constraints")
                         .help("Ignore unique constraints");

        final ArgumentGroup indexes = subparser.addArgumentGroup("Indexes");
        indexes.addArgument("--indexes")
               .action(Arguments.storeTrue())
               .dest("indexes")
               .help("Check for changed indexes (default)");
        indexes.addArgument("--ignore-indexes")
               .action(Arguments.storeFalse())
               .dest("indexes")
               .help("Ignore indexes");

        final ArgumentGroup foreignKeys = subparser.addArgumentGroup("Foreign Keys");
        foreignKeys.addArgument("--foreign-keys")
                   .action(Arguments.storeTrue())
                   .dest("foreign-keys")
                   .help("Check for changed foreign keys (default)");
        foreignKeys.addArgument("--ignore-foreign-keys")
                   .action(Arguments.storeFalse())
                   .dest("foreign-keys")
                   .help("Ignore foreign keys");

        final ArgumentGroup sequences = subparser.addArgumentGroup("Sequences");
        sequences.addArgument("--sequences")
                 .action(Arguments.storeTrue())
                 .dest("sequences")
                 .help("Check for changed sequences (default)");
        sequences.addArgument("--ignore-sequences")
                 .action(Arguments.storeFalse())
                 .dest("sequences")
                 .help("Ignore sequences");

        final ArgumentGroup data = subparser.addArgumentGroup("Data");
        data.addArgument("--data")
            .action(Arguments.storeTrue())
            .dest("data")
            .help("Check for changed data")
            .setDefault(Boolean.FALSE);
        data.addArgument("--ignore-data")
            .action(Arguments.storeFalse())
            .dest("data")
            .help("Ignore data (default)")
            .setDefault(Boolean.FALSE);
    }

    @Override
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public void run(Namespace namespace, Liquibase liquibase) throws Exception {
        final Set<Class<? extends DatabaseObject>> compareTypes = new HashSet<>();

        if (isTrue(namespace.getBoolean("columns"))) {
            compareTypes.add(Column.class);
        }
        if (isTrue(namespace.getBoolean("data"))) {
            compareTypes.add(Data.class);
        }
        if (isTrue(namespace.getBoolean("foreign-keys"))) {
            compareTypes.add(ForeignKey.class);
        }
        if (isTrue(namespace.getBoolean("indexes"))) {
            compareTypes.add(Index.class);
        }
        if (isTrue(namespace.getBoolean("primary-keys"))) {
            compareTypes.add(PrimaryKey.class);
        }
        if (isTrue(namespace.getBoolean("sequences"))) {
            compareTypes.add(Sequence.class);
        }
        if (isTrue(namespace.getBoolean("tables"))) {
            compareTypes.add(Table.class);
        }
        if (isTrue(namespace.getBoolean("unique-constraints"))) {
            compareTypes.add(UniqueConstraint.class);
        }
        if (isTrue(namespace.getBoolean("views"))) {
            compareTypes.add(View.class);
        }

        final DiffToChangeLog diffToChangeLog = new DiffToChangeLog(new DiffOutputControl());
        final Database database = liquibase.getDatabase();

        final String filename = namespace.getString("output");
        if (filename != null) {
            try (PrintStream file = new PrintStream(filename, StandardCharsets.UTF_8.name())) {
                generateChangeLog(database, database.getDefaultSchema(), diffToChangeLog, file, compareTypes);
            }
        } else {
            generateChangeLog(database, database.getDefaultSchema(), diffToChangeLog, outputStream, compareTypes);
        }
    }

    private void generateChangeLog(final Database database, final CatalogAndSchema catalogAndSchema,
                                   final DiffToChangeLog changeLogWriter, PrintStream outputStream,
                                   final Set<Class<? extends DatabaseObject>> compareTypes)
            throws DatabaseException, IOException, ParserConfigurationException {
        @SuppressWarnings({"unchecked", "rawtypes"})
        final SnapshotControl snapshotControl = new SnapshotControl(database,
                compareTypes.toArray(new Class[compareTypes.size()]));
        final CompareControl compareControl = new CompareControl(new CompareControl.SchemaComparison[]{
                new CompareControl.SchemaComparison(catalogAndSchema, catalogAndSchema)}, compareTypes);
        final CatalogAndSchema[] compareControlSchemas = compareControl
                .getSchemas(CompareControl.DatabaseRole.REFERENCE);

        try {
            final DatabaseSnapshot referenceSnapshot = SnapshotGeneratorFactory.getInstance()
                    .createSnapshot(compareControlSchemas, database, snapshotControl);
            final DatabaseSnapshot comparisonSnapshot = SnapshotGeneratorFactory.getInstance()
                    .createSnapshot(compareControlSchemas, null, snapshotControl);
            final DiffResult diffResult = DiffGeneratorFactory.getInstance()
                    .compare(referenceSnapshot, comparisonSnapshot, compareControl);

            changeLogWriter.setDiffResult(diffResult);
            changeLogWriter.print(outputStream);
        } catch (InvalidExampleException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    private static boolean isTrue(Boolean nullableCondition) {
        return nullableCondition != null && nullableCondition;
    }
}
