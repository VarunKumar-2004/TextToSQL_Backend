package com.project.TextToSQL.Service;

import com.project.TextToSQL.DTO.QueryResultPage;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.sql.*;
import java.util.*;
@Service
public class SQLiteQueryExecutor {
    public List<Map<String, Object>> execute(
            Path databasePath,
            String sql
    ) throws SQLException {

        String url = "jdbc:sqlite:" + databasePath.toAbsolutePath();

        List<Map<String, Object>> results = new ArrayList<>();

        try (
                Connection connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {

            ResultSetMetaData metadata =
                    resultSet.getMetaData();

            int columnCount =
                    metadata.getColumnCount();

            while (resultSet.next()) {

                Map<String, Object> row =
                        new LinkedHashMap<>();

                for (int i = 1; i <= columnCount; i++) {

                    String columnName =
                            metadata.getColumnLabel(i);

                    Object value =
                            resultSet.getObject(i);

                    row.put(columnName, value);
                }

                results.add(row);
            }
        }
        return results;
    }
    public QueryResultPage executePage(Path databasePath,String sql,int page,int pageSize) throws SQLException{
        int offSet=page*pageSize;
        String cleanSql=sql.trim();
        if(cleanSql.endsWith(";")){
            cleanSql=cleanSql.substring(0,cleanSql.length()-1).trim();
        }
        String query="select * from ("+cleanSql+") As query_result "+"limit "+(pageSize+1)+" offset "+offSet;
        List<Map<String,Object>>result=new ArrayList<>();
        List<String>columns=new ArrayList<>();
        String url = "jdbc:sqlite:" + databasePath.toAbsolutePath();
        try(Connection conn=DriverManager.getConnection(url);
                PreparedStatement ps=conn.prepareStatement(query);
                ResultSet resultSet=ps.executeQuery()
                ){
             ResultSetMetaData resultSetMetaData=resultSet.getMetaData();
             int columnCOunt=resultSetMetaData.getColumnCount();
             for(int i=1;i<=columnCOunt;i++){
                 columns.add(resultSetMetaData.getColumnLabel(i));
             }
             while(resultSet.next()){
                 Map<String,Object>row=new LinkedHashMap<>();
                 for(int i=1;i<=columnCOunt;i++){
                     String column=resultSetMetaData.getColumnLabel(i);
                     Object value=resultSet.getObject(i);
                     row.put(column,value);
                 }
                 result.add(row);
             }
        }
        boolean hasNext=result.size()>pageSize;
        if(hasNext){
            result=result.subList(0,pageSize);
        }
        return new QueryResultPage(
                columns,
                result,
                page,
                pageSize,
                hasNext
        );
    }
}
