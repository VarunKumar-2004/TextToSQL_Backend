package com.project.TextToSQL.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TableSchema {
    private String tableName;
    private List<ColumnSchema> columns;
}
