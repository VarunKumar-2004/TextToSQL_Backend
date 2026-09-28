package com.project.TextToSQL.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

@Service
public class DatabaseSchemaService {

    public String extractSchema(MultipartFile file) throws IOException {

        Path tempFile = Files.createTempFile(
                "texttosql-",
                getExtension(file.getOriginalFilename())
        );

        try {
            file.transferTo(tempFile.toFile());

            return extractSchemaFromFile(tempFile);

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private String extractSchemaFromFile(Path databaseFile) {

        String jdbcUrl = "jdbc:sqlite:" + databaseFile.toAbsolutePath();

        StringBuilder schema = new StringBuilder();

        try (Connection connection = DriverManager.getConnection(jdbcUrl)) {

            DatabaseMetaData metadata = connection.getMetaData();

            schema.append("DATABASE SCHEMA\n\n");

            try (ResultSet tables = metadata.getTables(
                    null,
                    null,
                    "%",
                    new String[]{"TABLE"}
            )) {

                while (tables.next()) {

                    String tableName = tables.getString("TABLE_NAME");

                    schema.append("TABLE: ")
                            .append(tableName)
                            .append("\n");

                    extractColumns(
                            metadata,
                            tableName,
                            schema
                    );

                    extractPrimaryKeys(
                            metadata,
                            tableName,
                            schema
                    );

                    schema.append("\n");
                }
            }

            extractForeignKeys(
                    metadata,
                    schema
            );

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to read SQLite database schema",
                    e
            );
        }

        return schema.toString();
    }

    private void extractColumns(
            DatabaseMetaData metadata,
            String tableName,
            StringBuilder schema
    ) throws SQLException {

        try (ResultSet columns = metadata.getColumns(
                null,
                null,
                tableName,
                null
        )) {

            while (columns.next()) {

                String columnName =
                        columns.getString("COLUMN_NAME");

                String dataType =
                        columns.getString("TYPE_NAME");

                schema.append("  ")
                        .append(columnName)
                        .append(" ")
                        .append(dataType)
                        .append("\n");
            }
        }
    }

    private void extractPrimaryKeys(
            DatabaseMetaData metadata,
            String tableName,
            StringBuilder schema
    ) throws SQLException {

        try (ResultSet primaryKeys = metadata.getPrimaryKeys(
                null,
                null,
                tableName
        )) {

            boolean found = false;

            while (primaryKeys.next()) {

                if (!found) {
                    schema.append("  PRIMARY KEY: ");
                    found = true;
                } else {
                    schema.append(", ");
                }

                schema.append(
                        primaryKeys.getString("COLUMN_NAME")
                );
            }

            if (found) {
                schema.append("\n");
            }
        }
    }

    private void extractForeignKeys(
            DatabaseMetaData metadata,
            StringBuilder schema
    ) throws SQLException {

        schema.append("\nRELATIONSHIPS\n");

        try (ResultSet tables = metadata.getTables(
                null,
                null,
                "%",
                new String[]{"TABLE"}
        )) {

            while (tables.next()) {

                String tableName =
                        tables.getString("TABLE_NAME");

                try (ResultSet foreignKeys =
                             metadata.getImportedKeys(
                                     null,
                                     null,
                                     tableName
                             )) {

                    while (foreignKeys.next()) {

                        String foreignColumn =
                                foreignKeys.getString(
                                        "FKCOLUMN_NAME"
                                );

                        String referencedTable =
                                foreignKeys.getString(
                                        "PKTABLE_NAME"
                                );

                        String referencedColumn =
                                foreignKeys.getString(
                                        "PKCOLUMN_NAME"
                                );

                        schema.append("  ")
                                .append(tableName)
                                .append(".")
                                .append(foreignColumn)
                                .append(" -> ")
                                .append(referencedTable)
                                .append(".")
                                .append(referencedColumn)
                                .append("\n");
                    }
                }
            }
        }
    }

    private String getExtension(String filename) {

        if (filename == null || !filename.contains(".")) {
            return ".db";
        }

        return filename.substring(
                filename.lastIndexOf(".")
        );
    }
}