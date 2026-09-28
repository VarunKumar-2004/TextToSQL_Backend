package com.project.TextToSQL.Service;

import com.project.TextToSQL.DTO.ColumnSchema;
import com.project.TextToSQL.DTO.SchemaResponse;
import com.project.TextToSQL.DTO.TableSchema;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SchemaMapper {

    public SchemaResponse toResponse(String schemaContext) {

        List<TableSchema> tables = new ArrayList<>();

        // Split tables section from relationships section
        String[] sections = schemaContext.split("RELATIONSHIPS");

        String tablesSection = sections[0];

        Map<String, TableSchema> tableMap = new LinkedHashMap<>();

        // Split individual tables
        String[] tableBlocks = tablesSection.split("TABLE:");

        for (String block : tableBlocks) {

            if (block.isBlank()) {
                continue;
            }

            String[] lines = block.trim().split("\\R");

            String tableName = lines[0].trim();
            if (tableName.equalsIgnoreCase("DATABASE SCHEMA")) {
                continue;
            }

            List<ColumnSchema> columns = new ArrayList<>();

            for (int i = 1; i < lines.length; i++) {

                String line = lines[i].trim();

                if (line.isBlank()) {
                    continue;
                }

                // Primary key line
                if (line.startsWith("PRIMARY KEY:")) {

                    String primaryKeys = line
                            .substring("PRIMARY KEY:".length())
                            .trim();

                    for (String primaryKey : primaryKeys.split(",")) {

                        String pk = primaryKey.trim();

                        for (ColumnSchema column : columns) {
                            if (column.getColumnName().equals(pk)) {
                                column.setPrimaryKey(true);
                            }
                        }
                    }

                    continue;
                }

                // Column line
                String[] parts = line.split("\\s+", 2);

                if (parts.length < 2) {
                    continue;
                }

                String columnName = parts[0];
                String dataType = parts[1];

                columns.add(
                        new ColumnSchema(
                                columnName,
                                dataType,
                                false,
                                false,
                                null,
                                null
                        )
                );
            }

            TableSchema tableSchema =
                    new TableSchema(tableName, columns);

            tableMap.put(tableName, tableSchema);
        }

        // Parse relationships
        if (sections.length > 1) {

            String relationshipsSection = sections[1];

            String[] relationships =
                    relationshipsSection.trim().split("\\R");

            for (String relationship : relationships) {

                relationship = relationship.trim();

                if (relationship.isBlank()) {
                    continue;
                }

                String[] parts = relationship.split("->");

                if (parts.length != 2) {
                    continue;
                }

                String[] source = parts[0].trim().split("\\.");

                String[] target = parts[1].trim().split("\\.");

                if (source.length != 2 || target.length != 2) {
                    continue;
                }

                String sourceTable = source[0];
                String sourceColumn = source[1];

                String targetTable = target[0];
                String targetColumn = target[1];

                TableSchema table = tableMap.get(sourceTable);

                if (table == null) {
                    continue;
                }

                for (ColumnSchema column : table.getColumns()) {

                    if (column.getColumnName().equals(sourceColumn)) {

                        column.setForeignKey(true);
                        column.setReferencesTable(targetTable);
                        column.setReferencesColumn(targetColumn);

                        break;
                    }
                }
            }
        }

        return new SchemaResponse(
                new ArrayList<>(tableMap.values())
        );
    }
}