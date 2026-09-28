package com.project.TextToSQL.Service;

import org.springframework.stereotype.Service;

@Service
public class SQLValidationService {
    public void validateSql(String sql){
        if(sql==null||sql.isBlank()){
            throw new IllegalArgumentException("Generated SQL is Empty");
        }
        String normalizedSql=sql.trim().toLowerCase();
        if(!normalizedSql.startsWith("select")
                &&!normalizedSql.startsWith("with")
                &&!normalizedSql.startsWith("select\n")
                &&!normalizedSql.startsWith("with\n")){
            throw new IllegalArgumentException("only select queries are acceptable");
        }
        String sqlWithoutSemiColon=normalizedSql.endsWith(";")?normalizedSql.substring(0,normalizedSql.length()-1):normalizedSql;
        if(sqlWithoutSemiColon.contains(";")){
            throw new IllegalArgumentException("Only single queries are accepted");
        }
        String[] forbiddenKeywords = {
                "insert",
                "update",
                "delete",
                "drop",
                "alter",
                "create",
                "attach",
                "detach",
                "pragma",
                "vacuum",
                "reindex",
                "replace"
        };
        for(String keyWord:forbiddenKeywords){
            if(containsSqlKeyword(sqlWithoutSemiColon,keyWord)){
                throw new IllegalArgumentException("only select queries are accepted");
            }
        }
    }
    private boolean containsSqlKeyword(
            String sql,
            String keyword
    ) {

        String pattern =
                "(?i).*\\b"
                        + keyword
                        + "\\b.*";

        return sql.matches(pattern);
    }
}
