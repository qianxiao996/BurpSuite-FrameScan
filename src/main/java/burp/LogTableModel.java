package burp;

import javax.swing.table.AbstractTableModel;
import java.awt.event.ActionEvent;

public class LogTableModel  extends AbstractTableModel {
    private final String[] columnNames = {"序号", "来源", "URL", "返回包长度", "状态码","精确扫描结果"};
    @Override
    public int getRowCount()
    {
        return BurpExtender.table_log_data.size();

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
            default:
                return String.class;
        }
    }
    public  void ClearData() {
            BurpExtender.table_log_data.clear();
//            int rowCount = logTable.getRowCount();
//            model.fireTableRowsDeleted(0, rowCount);
            fireTableDataChanged();
//
    }
    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        if (rowIndex < 0 || rowIndex >= BurpExtender.table_log_data.size() || columnIndex < 0 || columnIndex >= columnNames.length) {
            return "";
        }
        LogEntry logEntry = BurpExtender.table_log_data.get(rowIndex);
        switch (columnIndex)
        {
            case 0:
                return logEntry.id;
            case 1:
                return BurpExtender.callbacks.getToolName(logEntry.tool);
            case 2:
                return logEntry.url.toString();
            case 3:
                return logEntry.requestResponse.getResponse().length;//返回响应包的长度
            case 4:
                return logEntry.response_code;
            case 5:
                return logEntry.scan_result;
            default:
                return "";
        }
    }


}