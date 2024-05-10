package burp;

import javax.swing.table.AbstractTableModel;

public  class PocTableModel extends AbstractTableModel {
    private final String[] columnNames = {"序号", "漏洞名称", "URL", "匹配位置","匹配方式","匹配值","匹配状态码"};

    @Override
    public int getRowCount()
    {
        return BurpExtender.all_poc_data.size();

    }

    @Override
    public int getColumnCount()
    {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= BurpExtender.all_poc_data.size() || columnIndex < 0 || columnIndex >= columnNames.length) {
            return "";
        }
        PocEntry pocEntry = BurpExtender.all_poc_data.get(rowIndex);
        switch (columnIndex)
        {
            case 0:
                return pocEntry.id;
            case 1:
                return pocEntry.name;
            case 2:
                return pocEntry.url;
            case 3:
                return pocEntry.scope;
            case 4:
                return pocEntry.match_method;
            case 5:
                return pocEntry.match_value;
            case 6:
                return pocEntry.status_code;
            default:
                return "";
        }
    }

    public Object getValueRow(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= BurpExtender.all_poc_data.size()) {
            return "";
        }
        PocEntry pocEntry = BurpExtender.all_poc_data.get(rowIndex);
        return pocEntry;
    }

    @Override
    public String getColumnName(int columnIndex)
    {
        return columnNames[columnIndex];
    }
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
                return String.class;
            case 4:
                return String.class;
            case 5:
                return String.class;
            case 6:
                return Integer.class;
            default:
                return String.class;
        }
    }

}
