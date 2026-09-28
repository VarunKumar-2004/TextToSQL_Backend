package com.project.TextToSQL.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SchemaResponse {
    private List<TableSchema> tables;
}
