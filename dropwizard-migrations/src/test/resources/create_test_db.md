### Creating the `test-db.mv.db`

If a major H2 upgrade has to be performed, the storage format of the `test-db.mv.db` file may change.
Then a new db file has to be created.
This can be done with the H2 `RunScript` command. This command creates a db from an SQL backup file:
```shell
java -cp <path_to_h2.jar> org.h2.tools.RunScript -user sa -url jdbc:h2:file:./test-db -script ./test-db-backup.sql
```
The creation of the DB file is automated since Dropwizard 2.1.8 / 3.0.2 / 4.0.2 with the `exec-maven-plugin`.

If Liquibase changes its changelog, the `test-db-backup.sql` file may have to be updated.
This can be done with
 1. Migrating a clean database with the Liquibase changelog
 2. Using the H2 `Script` tool:
    ```shell
    java -cp <path_to_h2.jar> org.h2.tools.Script -user sa -url jdbc:h2:file:./new-migrated-db -script ./test-db-backup.sql
    ```
