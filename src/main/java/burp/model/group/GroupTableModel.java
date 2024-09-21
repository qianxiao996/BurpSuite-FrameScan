package burp.model.group;

import burp.utils.Group;
import burp.utils.Poc;

import javax.swing.table.AbstractTableModel;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public  class GroupTableModel extends AbstractTableModel {
    private final String[] columnNames = {"序号","分组名称","分组类型","指纹匹配后启用的POC", "直接进行扫描的POC"};
    public static List<burp.model.group.GroupEntry> all_group_data = new ArrayList<>();

    @Override
    public int getRowCount()
    {
        return all_group_data.size();

    }

    @Override
    public int getColumnCount()
    {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= all_group_data.size() || columnIndex < 0 || columnIndex >= columnNames.length) {
            return "";
        }
        burp.model.group.GroupEntry group = all_group_data.get(rowIndex);

        switch (columnIndex)
        {
            case 0:
                return group.id;
            case 1:
                return group.name;
            case 2:
                return group.type;
            case 3:
                return group.is_finger_poc;
            case 4:
                return group.is_finger_jump_poc;
            default:
                return "";
        }
    }

    public Object getValueRow(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= all_group_data.size()) {
            return "";
        }
        return all_group_data.get(rowIndex);
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
            case 3:
            case 4:
                return Boolean.class;
            case 1:
            case 2:
            default:
                return String.class;
        }
    }
    public List<burp.model.group.GroupEntry> getAllValue(){
        return  all_group_data;
    }


    public  void ClearData() {
        all_group_data.clear();
        fireTableDataChanged();
    }

    public   void  addValueAt(burp.model.group.GroupEntry value)
    {
        all_group_data.add(value);
        fireTableDataChanged();
    }

    public burp.model.group.GroupEntry getValueByGroupid(int id){
        for(burp.model.group.GroupEntry i :all_group_data){
            if(i.id==id){
                return i;
            }
        }
        return  null;
    }
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 3 || columnIndex == 4;
    }
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        // 点击事件的处理逻辑
        if (aValue instanceof Boolean) {
            GroupEntry temp  = (GroupEntry) getValueRow(rowIndex);
            if(columnIndex==3){
                temp.is_finger_poc  = (boolean) aValue;
            } else if (columnIndex==4) {
                temp.is_finger_jump_poc  = (boolean) aValue;
            }
            for (int i = 0; i < all_group_data.size(); i++) {
                GroupEntry group = all_group_data.get(i);
                if (group.id == temp.id) {
                    all_group_data.set(i, temp);
                    break;
                }
            }
            Group.Save_Group_Data(temp);
            Poc.reload_poc();
        }
    }
    public void selectAllOrNull(boolean value,int colunmn_index) {
        for (int i = 0; i < all_group_data.size(); i++) {
            GroupEntry group = all_group_data.get(i);
            if(colunmn_index==3){
                group.is_finger_poc=value;
            } else if (colunmn_index==4) {
                group.is_finger_jump_poc=value;
            }
            all_group_data.set(i, group);
            Group.Save_Group_Data(group);
        }
        Poc.reload_poc();
    }
    public static void save_data_to_db() throws SQLException {
        for (GroupEntry i : all_group_data){
            Group.Save_Group_Data(i);
        }
        Group.reload_read_group_Data();
    }
}
