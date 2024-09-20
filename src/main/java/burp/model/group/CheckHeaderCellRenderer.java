package burp.model.group;

import burp.utils.Group;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static burp.Main_Vuln.printErr;

public class CheckHeaderCellRenderer implements TableCellRenderer {
    GroupTableModel tableModel;
    JTableHeader tableHeader;
    JCheckBox selectBox;
    int  column_index;
    public CheckHeaderCellRenderer(JTable table,int column_index) {
        this.column_index = column_index;
        this.tableModel = (GroupTableModel) table.getModel();
        this.tableHeader = table.getTableHeader();
        selectBox = new JCheckBox(tableModel.getColumnName(column_index)); // 设置为第四列的标题
        List<GroupEntry> all = tableModel.getAllValue();
        boolean  is_select =true;
        for (GroupEntry a :all){
            if(column_index==3){
                if(!a.is_finger_poc){
                    is_select=false;
                    break;
                }
            } else if (column_index==4) {
                if(!a.is_finger_jump_poc){
                    is_select=false;
                    break;
                }
            }
        }
//        System.out.println(is_select);
        selectBox.setSelected(is_select);
        tableHeader.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 0) {
                    // 获得选中列
                    int selectColumn = tableHeader.columnAtPoint(e.getPoint());
                    if (selectColumn == column_index) { // 检查是否点击的是第四列
                        boolean value = !selectBox.isSelected();
                        selectBox.setSelected(value);
                        tableModel.selectAllOrNull(value,column_index); // 假设这个方法存在于 GroupTableModel 中
                        tableHeader.repaint();
                    }
                }
            }
        });
    }


    public void setSelectBox(boolean value) {
        selectBox.setSelected(value);
        tableHeader.repaint();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        String valueStr = (String) value;
        JLabel label = new JLabel(valueStr);
        label.setHorizontalAlignment(SwingConstants.CENTER); // 表头标签居中
        selectBox.setHorizontalAlignment(SwingConstants.CENTER); // 表头标签居中
        selectBox.setBorderPainted(true);
        JComponent component = (column == column_index) ? selectBox : label; // 检查是否为第四列
        component.setForeground(tableHeader.getForeground());
        component.setBackground(tableHeader.getBackground());
        component.setFont(tableHeader.getFont());
        component.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
        return component;
    }
}