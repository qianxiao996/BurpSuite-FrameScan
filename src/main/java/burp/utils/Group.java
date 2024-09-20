package burp.utils;
import burp.IHttpRequestResponse;
import burp.IResponseInfo;
import burp.Main_Vuln;
import burp.model.group.GroupEntry;
import burp.model.poc.PocEntry;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static burp.BurpExtender.helpers;
import static burp.Main_Vuln.*;
import static burp.utils.Conn.get_requests_response_head_body;
import static burp.utils.Conn.get_requests_url;
import static burp.utils.CustHttpService.get_host_ico_http_requests;
import static burp.utils.Poc.Add_Component;
import static burp.utils.Poc.countString;

public class Group {
    public static void Add_Group(String  name,String type) {
        int last_id;
        // 获取最后一个元素
        if (model_group.getRowCount()>0) {
            List<GroupEntry>  all_value = model_group.getAllValue();
            last_id = all_value.get(all_value.size() - 1).id+1;
        } else {
            last_id=1;
        }
        GroupEntry _Data = new GroupEntry(last_id,name,type,true,false);
        Group.Save_Group_Data(_Data);

    }



    public static String delete_Group_data(GroupEntry temp) {
        String sql =  "delete  from `group` where id = "+temp.id;
        return Sql.Delete(sql);
    }

    public static void reload_read_group_Data() throws SQLException {
        model_group.ClearData();
        model_group.fireTableDataChanged();
        String sql = "select * from `group`";
        List<Map<String, Object>> sql_result = Sql.Select(sql);
        for (Map<String, Object> item : sql_result) {
            int id =  (int) item.get("id");
            String name= (String) item.get("name");
            String type = (String) item.get("type");
            int is_finger_poc_int = (int) item.get("is_finger_poc");
            Boolean is_finger_poc = (is_finger_poc_int != 0) ? Boolean.TRUE : Boolean.FALSE;
            int is_finger_jump_poc_bool=  (int) item.get("is_finger_jump_poc");
            Boolean is_finger_jump_poc = (is_finger_jump_poc_bool != 0) ? Boolean.TRUE : Boolean.FALSE;
            GroupEntry temp = new GroupEntry(id,name,type,is_finger_poc,is_finger_jump_poc);
            model_group.addValueAt(temp);
        }
        printMsg("Group Data Load Suceess！");
    }
    public static String Save_Group_Data(GroupEntry Groupdata) {
        String url = "jdbc:sqlite:/"+ Main_Vuln.SQL_DB_PATH;
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement pstmt = connection.prepareStatement(
                     "INSERT OR REPLACE INTO `group` (`id`,`name`,`type`,`is_finger_poc`,`is_finger_jump_poc`) VALUES (?,?,?, ?,?)")) {
            pstmt.setInt(1, Groupdata.id);
            pstmt.setString(2, Groupdata.name);
            pstmt.setString(3, Groupdata.type);
            if(Groupdata.is_finger_poc){
                pstmt.setInt(4, 1);
            }else{
                pstmt.setInt(4, 0);
            }
            if(Groupdata.is_finger_jump_poc){
                pstmt.setInt(5, 1);
            }else{
                pstmt.setInt(5, 0);
            }
            // 执行更新或插入操作
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                return "保存成功!";

            } else {
                return "保存失败!";
            }

        } catch (SQLException e) {
            printErr(Arrays.toString(e.getStackTrace()));
//                e.printStackTrace();
            return Arrays.toString(e.getStackTrace());
        }
    }
    public static void Save_Group_Data_All(List<GroupEntry> Entries) {
        String url = "jdbc:sqlite:" + Main_Vuln.SQL_DB_PATH;
        try (Connection connection = DriverManager.getConnection(url)) {
            // 开启事务
            connection.setAutoCommit(false);
            String sql = "INSERT OR REPLACE INTO `group` (`id`,`name`,`type`,`is_finger_poc`,`is_finger_jump_poc`) VALUES (?,?,?, ?,?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                for (GroupEntry Groupdata : Entries) {
                    pstmt.setInt(1, Groupdata.id);
                    pstmt.setString(2, Groupdata.name);
                    pstmt.setString(3, Groupdata.type);
                    if(Groupdata.is_finger_poc){
                        pstmt.setInt(4, 1);
                    }else{
                        pstmt.setInt(4, 0);
                    }
                    if(Groupdata.is_finger_jump_poc){
                        pstmt.setInt(5, 1);
                    }else{
                        pstmt.setInt(5, 0);
                    }
                    pstmt.addBatch(); // 添加到批处理中
                }
                // 执行批处理
                int[] rowsAffected = pstmt.executeBatch();
                // 提交事务
                connection.commit();
                // 检查是否有数据被影响
                boolean anySuccess = false;
                for (int rows : rowsAffected) {
                    if (rows > 0) {
                        anySuccess = true;
                        break;
                    }
                }
                if (anySuccess) {
                } else {
                }
            } catch (SQLException e) {
                // 回滚事务
                connection.rollback();
                printErr(e.getMessage());
                printErr(Arrays.toString(e.getStackTrace()));
            }
        } catch (SQLException e) {
            printErr(e.getMessage());
            printErr(Arrays.toString(e.getStackTrace()));
        }
    }
    public static void Group_edit(GroupEntry Group_data) {
        mainFrame_Group = new JFrame("编辑分组");
        mainFrame_Group.setLayout(new BorderLayout());

        JLabel label_group_name = new JLabel("分组名称：");
        JTextField  text_group_name= new JTextField(Group_data.name);


        JLabel label_vuln_type = new JLabel("分组类型：");
        String[] group_list = {"漏洞POC","指纹POC"};
        JComboBox<String> comboBox_type = new JComboBox<>(group_list);
        comboBox_type.setSelectedItem(Group_data.type);

        String[] is_items = {"是","否"};
        JLabel label_finger_enable = new JLabel("指纹匹配插件：");
        JComboBox<String> comboBox_finger_enable = new JComboBox<>(is_items);
        if(!Group_data.is_finger_poc){
            comboBox_finger_enable.setSelectedItem("否");
        }else{
            comboBox_finger_enable.setSelectedItem("是");
        }

        JLabel label_is_poc = new JLabel("直接进行扫描：");
        JComboBox<String> comboBox_is_poc = new JComboBox<>(is_items);
        if(Group_data.is_finger_jump_poc){
            comboBox_is_poc.setSelectedItem("是");
        }else{
            comboBox_is_poc.setSelectedItem("否");
        }
        JButton Group_button_go = new JButton("确定");
        JButton Group_button_cancal = new JButton("取消");

        Group_button_go.addActionListener(e -> {
            Group_data.name = (String) text_group_name.getText();
            Group_data.type = (String) comboBox_type.getSelectedItem();
            String is_true_finger_enable  = (String) comboBox_finger_enable.getSelectedItem();
            if(Objects.equals(is_true_finger_enable, "是")){
                Group_data.is_finger_poc=true;
            }else{
                Group_data.is_finger_poc=false;
            }
            String is_true_is_poc  = (String) comboBox_is_poc.getSelectedItem();
            if(Objects.equals(is_true_is_poc, "是")){
                Group_data.is_finger_jump_poc=true;
            }else{
                Group_data.is_finger_jump_poc=false;
            }
            String message = Save_Group_Data(Group_data);
            try {
                reload_read_group_Data();
            } catch (SQLException ex) {
                printErr(ex.getMessage());
                printErr(Arrays.toString(ex.getStackTrace()));
            }
            JOptionPane.showMessageDialog(null, message, "提示", JOptionPane.INFORMATION_MESSAGE);
            mainFrame_Group.setVisible(false);
            try {
                Group.reload_read_group_Data();
            } catch (SQLException ex) {
                printErr(String.valueOf(ex));
            }
        });
        Group_button_cancal.addActionListener(e -> mainFrame_Group.setVisible(false));
        JPanel jpanel_Group_ = new JPanel();
        GridBagLayout gbl=new GridBagLayout();//创建网格包布局管理器
        GridBagConstraints gbc=new GridBagConstraints();//GridBagConstraints对象来给出每个组件的大小和摆放位置
        jpanel_Group_.setLayout(gbl);//设置容器布局为网格包布局类型
        gbc.fill=GridBagConstraints.BOTH;//组件填充显示区域，当格子有剩余空间时，填充空间
        gbc.insets = new Insets(5, 5, 5, 5); // 边距
        gbc.anchor = GridBagConstraints.NORTHWEST; // 对齐方式
//        gridx 和 gridy：指定组件放置在网格中的起始位置。
//        gridwidth 和 gridheight：指定组件跨越的网格单元数量。
//        fill：指定组件如何填充其网格单元。
//        weightx 和 weighty：指定组件在可伸缩空间中的权重

        jpanel_Group_.add(Add_Component(gbl,label_group_name,gbc,0,0,1,1,0,0));
        jpanel_Group_.add(Add_Component(gbl,text_group_name,gbc,1,0,1,2,1,0));

        jpanel_Group_.add(Add_Component(gbl,label_vuln_type,gbc,0,1,1,1,0,0));
        jpanel_Group_.add(Add_Component(gbl,comboBox_type,gbc,1,1,1,2,1,0));

        jpanel_Group_.add(Add_Component(gbl,label_finger_enable,gbc,0,2,1,1,0,0));
        jpanel_Group_.add(Add_Component(gbl,comboBox_finger_enable,gbc,1,2,1,2,1,0));

        jpanel_Group_.add(Add_Component(gbl,label_is_poc,gbc,0,3,1,1,0,0));
        jpanel_Group_.add(Add_Component(gbl,comboBox_is_poc,gbc,1,3,1,2,1,0));


        //按钮行
        gbc.fill=GridBagConstraints.NORTH;//组件填充显示区域，当格子有剩余空间时，填充空间
        gbc.anchor = GridBagConstraints.CENTER; // 对齐方式
        jpanel_Group_.add(Add_Component(gbl,Group_button_go,gbc,1,4,1,1,0,0));
        jpanel_Group_.add(Add_Component(gbl,Group_button_cancal,gbc,2,4,1,1,0,0));
        jpanel_Group_.setBorder(new EmptyBorder(10, 10, 10, 10)); // 设置外边距为10
        mainFrame_Group.getContentPane().add(jpanel_Group_);
        mainFrame_Group.setSize(500, 300);
        mainFrame_Group.setLocationRelativeTo(null);
        mainFrame_Group.setVisible(true);
    }

    public static String[] get_all_group(){
        String sql =  "select DISTINCT `name` from `group`";
        List<Map<String, Object>> sql_result = Sql.Select(sql);
        String[] resultArray = new String[sql_result.size()];
        int i=0;
        for (Map<String, Object> item : sql_result) {
            resultArray[i] = (String) item.get("name");
            i++;
        }
        return resultArray;
    }

    public static String[] get_group_by_type(String type){
        String sql;
        if(Objects.equals(type, "vuln")){
             sql =  "select DISTINCT `name` from `group` where  is_finger_poc >0 ";
        } else if (Objects.equals(type, "finger")) {
             sql =  "select DISTINCT `name` from `group` where  is_finger_jump_poc>0";
        }else {
             sql =  "select DISTINCT `name` from `group`";
        }
        List<Map<String, Object>> sql_result = Sql.Select(sql);
        String[] resultArray = new String[sql_result.size()];
        int i=0;
        for (Map<String, Object> item : sql_result) {
            resultArray[i] = (String) item.get("name");
            i++;
        }
        return resultArray;
    }




}
