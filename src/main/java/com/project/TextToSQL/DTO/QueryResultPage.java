package com.project.TextToSQL.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class QueryResultPage {
    private List<String> columns;
    private List<Map<String,Object>>rows;
    private int page;
    private int pageSize;
    private boolean hasNext;
}
