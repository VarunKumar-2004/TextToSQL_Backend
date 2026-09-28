package com.project.TextToSQL.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ColumnSchema {
    private String columnName;
    private String dataType;
    private boolean primaryKey;
    private boolean foreignKey;
    private String referencesTable;
    private String referencesColumn;
}
