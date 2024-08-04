package burp.vuln;


import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class LogTableModel  extends AbstractTableModel {


    private final String[] columnNames = {"序号", "来源", "URL", "返回包长度", "状态码","指纹信息","漏洞扫描结果"};
    public static final List<LogEntry> table_log_data = new ArrayList<>();//用于展现结果

    @Override
    public int getRowCount()
    {
        return table_log_data.size();

    }

    @Override
    public int getColumnCount()
    {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int columnIndex)
    {
        return columnNames[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
        switch (columnIndex)
        {
            case 0:
                return Integer.class;
            case 1:
                return String.class;
            case 2:
                return String.class;
            case 3:
                return Integer.class;//返回响应包的长度
            case 4:
                return Integer.class;
            case 5:
                return String.class;
            case 6:
                return String.class;
            default:
                return String.class;
        }
    }
    public  void ClearData() {
            table_log_data.clear();
//            int rowCount = logTable.getRowCount();
//            model.fireTableRowsDeleted(0, rowCount);
//            fireTableDataChanged();
//
    }
    public   void  addValueAt(LogEntry value)
    {
        table_log_data.add(value);
//        fireTableDataChanged();
    }
    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        if (rowIndex < 0 || rowIndex >= table_log_data.size() || columnIndex < 0 || columnIndex >= columnNames.length) {
            return "";
        }
        LogEntry logEntry = table_log_data.get(rowIndex);
        switch (columnIndex)
        {
            case 0:
                return logEntry.id;
            case 1:
                return  logEntry.tool;
//                return BurpExtender.callbacks.getToolName(logEntry.tool);
            case 2:
                return logEntry.url.toString();
            case 3:
                return logEntry.requestResponse.getResponse().length;//返回响应包的长度
            case 4:
                return logEntry.response_code;
            case 5:
                return logEntry.finger_scan_result;
            case 6:
                return logEntry.vuln_scan_result;
            default:
                return "";
        }
    }
    public List<LogEntry> getAllValue(){
        return  table_log_data;
    }




}