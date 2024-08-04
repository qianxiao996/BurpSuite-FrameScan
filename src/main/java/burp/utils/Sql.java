package burp.utils;

import java.sql.*;
import java.util.*;

import static burp.Main_Vuln.SQL_DB_PATH;
import static burp.Main_Vuln.printErr;

public class Sql {
    /**
     * 从数据库中选择所有 POC 数据并加载到模型中。
     *
     * @return
     */
    public static List<Map<String, Object>> Select(String sql) {
        try {
            List<Map<String, Object>> ret_result = new ArrayList<>();
            // 加载 SQLite JDBC 驱动
            Class.forName("org.sqlite.JDBC");
            // 获取数据库连接
            try (Connection connection = getConnection();
                 Statement statement = connection.createStatement()) {
                // 执行查询
//                String sql = "SELECT * FROM poc";
                ResultSet rs = statement.executeQuery(sql);
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                // 处理查询结果
                while (rs.next()) {
                    Map<String, Object> rowData = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        int columnType = metaData.getColumnType(i);
                        switch (columnType) {
                            case java.sql.Types.INTEGER:
                                rowData.put(columnName, rs.getInt(i));
                                break;
                            case java.sql.Types.VARCHAR:
                            case java.sql.Types.LONGVARCHAR:
                                rowData.put(columnName, rs.getString(i));
                                break;
                            // 添加其他数据类型的处理
                            default:
                                // 如果有其他类型，你可以在这里处理
                                rowData.put(columnName, rs.getObject(i));
                                break;
                        }
                    }
                    ret_result.add(rowData);
                }
            } catch (SQLException e) {
                printErr(e.getMessage());
                printErr(Arrays.toString(e.getStackTrace()));
            }
            return ret_result;
        } catch (ClassNotFoundException e) {
            // 打印类未找到异常
            printErr(e.getMessage());
            printErr(Arrays.toString(e.getStackTrace()));
        }
        return new ArrayList<>();
    }
    public static String Delete(String sql) {
        try{
            // 加载 SQLite JDBC 驱动
            Class.forName("org.sqlite.JDBC");
            // 获取数据库连接
            try (Connection connection = getConnection();
                Statement statement = connection.createStatement()) {
                int deletedRows = statement.executeUpdate(sql);
                statement.close();
                if(deletedRows>0){
                    return "success";
                }else{
                    return "删除失败!";
                }
            } catch (SQLException e) {
                // 打印异常堆栈跟踪
                printErr(Arrays.toString(e.getStackTrace()));
            }
        } catch (ClassNotFoundException e) {
            // 打印类未找到异常
            printErr(e.getMessage());
            return e.getMessage();
        }
        return "";
    }

    /**
     * 获取数据库连接。
     *
     * @return 数据库连接
     * @throws SQLException 如果连接失败
     */
    private static Connection getConnection() throws SQLException {
        String databaseUrl = "jdbc:sqlite:" + SQL_DB_PATH;
        return DriverManager.getConnection(databaseUrl);
    }
}