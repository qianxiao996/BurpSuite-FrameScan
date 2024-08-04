package burp.poc;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public  class PocTableModel extends AbstractTableModel {
    private final String[] columnNames = {"序号","漏洞分组","漏洞分类","漏洞名称", "目录层级","插件类型"};
    public static final List<PocEntry> all_poc_data = new ArrayList<>();//用于展现poc

    @Override
    public int getRowCount()
    {
        return all_poc_data.size();

    }

    @Override
    public int getColumnCount()
    {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= all_poc_data.size() || columnIndex < 0 || columnIndex >= columnNames.length) {
            return "";
        }
        PocEntry pocEntry = all_poc_data.get(rowIndex);
        switch (columnIndex)
        {
            case 0:
                return pocEntry.id;
            case 1:
                return pocEntry.group;
            case 2:
                return pocEntry.type;
            case 3:
                return pocEntry.name;
            case 4:
                return pocEntry.dir_count;
            case 5:
                return pocEntry.plugin_type;
            default:
                return "";
        }
    }

    public Object getValueRow(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= all_poc_data.size()) {
            return "";
        }
        PocEntry pocEntry = all_poc_data.get(rowIndex);
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
                return Integer.class;
            case 5:
                return String.class;
            default:
                return String.class;
        }
    }
    public List<PocEntry> getAllValue(){
        return  all_poc_data;
    }

    public  void ClearData() {
        all_poc_data.clear();
//            int rowCount = logTable.getRowCount();
//            model.fireTableRowsDeleted(0, rowCount);
        fireTableDataChanged();
//
    }

    public   void  addValueAt(PocEntry value)
    {
        all_poc_data.add(value);
        fireTableDataChanged();
    }

}
