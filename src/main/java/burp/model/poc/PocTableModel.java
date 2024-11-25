package burp.model.poc;

import burp.utils.Poc;

import javax.swing.table.AbstractTableModel;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static burp.Main_Vuln.printErr;
import static burp.utils.Poc.reload_read_poc_Data;

public  class PocTableModel extends AbstractTableModel {
    private final String[] columnNames = {"序号","漏洞分组","漏洞分类","漏洞名称", "目录层级","插件类型"};
    public static List<burp.model.poc.PocEntry> all_poc_data = new ArrayList<>();//用于展现poc

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
        burp.model.poc.PocEntry PocEntry = all_poc_data.get(rowIndex);
        switch (columnIndex)
        {
            case 0:
                return PocEntry.id;
            case 1:
                return PocEntry.group;
            case 2:
                return PocEntry.type;
            case 3:
                return PocEntry.name;
            case 4:
                return PocEntry.dir_count;
            case 5:
                return PocEntry.plugin_type;
            default:
                return "";
        }
    }

    public Object getValueRow(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= all_poc_data.size()) {
            return "";
        }
        return all_poc_data.get(rowIndex);
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
            case 4:
                return Integer.class;
            case 1:
            case 3:
            case 2:
            case 5:
            default:
                return String.class;
        }
    }
    public List<burp.model.poc.PocEntry> getAllValue(){
        return  all_poc_data;
    }


    public  void ClearData() {
        all_poc_data.clear();
        fireTableDataChanged();
    }

    public   void  addValueAt(burp.model.poc.PocEntry value)
    {
        all_poc_data.add(value);
    }

    public burp.model.poc.PocEntry getValueByPocid(int id){
        for(burp.model.poc.PocEntry i :all_poc_data){
            if(i.id==id){
                return i;
            }
        }
        return  null;
    }
    public static void save_data_to_db() throws SQLException {
        Poc.Save_Poc_Data_All(all_poc_data);
        reload_read_poc_Data();
        Poc.reload_poc();

    }
}
