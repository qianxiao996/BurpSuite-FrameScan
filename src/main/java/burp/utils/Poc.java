package burp.utils;

import burp.IHttpRequestResponse;
import burp.IResponseInfo;
import burp.poc.PocEntry;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static burp.BurpExtender.helpers;
import static burp.Main_Vuln.*;
import static burp.utils.Conn.*;

public class Poc {

    public static void reload_read_poc_Data() throws SQLException {
        model_poc.ClearData();
        model_poc.fireTableDataChanged();
        String sql = "select * from poc";
        List<Map<String, Object>> sql_result = Sql.Select(sql);
//        printDebug(String.valueOf(sql_result.size()));
        for (Map<String, Object> item : sql_result) {
            int id = (int) item.get("id");
            String group = (String) item.get("group");
            String type = (String) item.get("type");
            String name = (String) item.get("name");
            int dir_count = (int) item.get("dir_count");
            String plugin_type = (String) item.get("plugin_type");
            String plugins_data = (String) item.get("plugins_data");
            String plugins_description = (String) item.get("description");
            model_poc.addValueAt(new PocEntry(id,group,type,name,dir_count,plugin_type,plugins_data,plugins_description));
        }
        printMsg("POC Data Load Suceess！");

    }
    public static String delete_poc_data(PocEntry temp) {
        String sql =  "delete  from poc where id = "+temp.id;
        return Sql.Delete(sql);
    }
    public static List<PocEntry> Get_Poc(String groupstr) {
        //逗号分隔成列表进行查询
        String[] parts = groupstr.split(",");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            sb.append("'").append(parts[i]).append("'");
            if (i < parts.length - 1) {
                sb.append(",");
            }
        }
        List<PocEntry> resultList = new ArrayList<>();
        String sql = "SELECT * FROM poc WHERE `group` IN ("+ sb +")";
//        Main_Vuln.printDebug(sql);
        List<Map<String, Object>> sql_result = Sql.Select(sql);
        for (Map<String, Object> item : sql_result) {
            int id = (int)item.get("id");
            String group= (String) item.get("group");
            String type=(String) item.get("type");
            String name= (String) item.get("name");
            int dir_count= (int)item.get("dir_count");
            String plugin_type=(String) item.get("plugin_type");
            String plugins_data= (String) item.get("plugins_data");
            String plugins_description=(String) item.get("description");
            resultList.add(new PocEntry(id,group,type,name,dir_count,plugin_type,plugins_data,plugins_description));
        }
        return resultList;
    }
    public static String[] get_all_poc_type(String value){
        String sql =  "select DISTINCT `type` from poc";
        if(!Objects.equals(value, "")){
            sql =  "select DISTINCT `type` from poc where type like '%"+value+"%'";
        }
        List<Map<String, Object>> sql_result = Sql.Select(sql);
        String[] resultArray = new String[sql_result.size()];
        int i=0;
        for (Map<String, Object> item : sql_result) {
            resultArray[i] = (String) item.get("type");
            i++;
        }
        return resultArray;
    }
    public static String[] get_all_poc_group(){
        String sql =  "select DISTINCT `group` from poc";
        List<Map<String, Object>> sql_result = Sql.Select(sql);
        String[] resultArray = new String[sql_result.size()];
        int i=0;
        for (Map<String, Object> item : sql_result) {
            resultArray[i] = (String) item.get("group");
            i++;
        }
        return resultArray;
    }
    private static String Save_Poc_Data(PocEntry pocData) {
        String url = "jdbc:sqlite:/"+ SQL_DB_PATH;
//        printDebug(pocData.toString());
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement pstmt = connection.prepareStatement(
                     "INSERT OR REPLACE INTO poc (`id`,`group`,`type`,`name`,`dir_count`,`plugin_type`,`plugins_data`,`description`) VALUES (?,?,?,?,?, ?, ?,?)")) {
            pstmt.setInt(1, pocData.id);
            pstmt.setString(2, pocData.group);
            pstmt.setString(3, pocData.type);
            pstmt.setString(4, pocData.name);
            pstmt.setInt(5, pocData.dir_count);
            pstmt.setString(6, pocData.plugin_type);
            pstmt.setString(7, pocData.plugins_data);
            pstmt.setString(8, pocData.description);
            // 执行更新或插入操作
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                return "保存成功!";

            } else {
                return "保存失败!";
            }

        } catch (SQLException e) {
            printErr(e.getMessage());
            printErr( Arrays.toString(e.getStackTrace()));
            return Arrays.toString(e.getStackTrace());
        }
    }
    public static void poc_edit(PocEntry poc_data) {

        mainFrame = new JFrame("编辑POC");
        mainFrame.setLayout(new BorderLayout());

        JLabel label_vuln_type = new JLabel("漏洞分类：");
        String[] items = get_all_poc_type("");
        JComboBox<String> comboBox_type= new JComboBox<>(items);

        JLabel label_vuln_name = new JLabel("漏洞名称：");
        JTextField  text_vuln_name= new JTextField(poc_data.name);
        // 设置ComboBox为可编辑
        comboBox_type.setEditable(true);
        comboBox_type.setSelectedItem(poc_data.type);

        comboBox_type.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String keyword = ((JTextField) comboBox_type.getEditor().getEditorComponent()).getText();
                    String[] items = get_all_poc_type(keyword);
                    comboBox_type.setModel(new DefaultComboBoxModel<>(items));
                    comboBox_type.setSelectedItem(keyword);

                }
            }
        });


        JLabel label_vuln_group = new JLabel("漏洞分组：");
        String[] items_group = get_all_poc_group();
        JComboBox<String> comboBox_group = new JComboBox<>(items_group);
        // 设置ComboBox为可编辑
        comboBox_group.setEditable(true);
        comboBox_group.setSelectedItem(poc_data.group);

        JLabel label_vuln_dir = new JLabel("目录层级：");
        String[] dir_List = { "0","1", "2","3","4","5"};
        JComboBox<String> comBox_vuln_dir = new JComboBox<>(dir_List);

        if(poc_data.dir_count>=0){
            comBox_vuln_dir.setSelectedIndex(poc_data.dir_count);
        }else{
            comBox_vuln_dir.setSelectedIndex(1);
        }

        //请求
        JLabel lable_reques_type = new JLabel("插件类型：");
        String[] combox_request_List = { "简单请求替换","高级HTTP请求","Yaml Poc","Python Poc"};
        JComboBox<String> combox_request = new JComboBox<>(combox_request_List);
        combox_request.setSelectedItem(poc_data.plugin_type);

        RSyntaxTextArea textArea_request = new RSyntaxTextArea();
        textArea_request.setEditable(true);
        textArea_request.setLineWrap(true);
        textArea_request.setCodeFoldingEnabled(true);

        RTextScrollPane scrollPane_request = new RTextScrollPane(textArea_request);
        JLabel label_vuln_scope = new JLabel("匹配位置：");
        String[] strList = { "response", "response header", "response body","response title"};
        JComboBox<String> comBox_vuln_scope = new JComboBox<>(strList);

        JLabel label_vuln_method = new JLabel("匹配方法：");
        String[] method_List = { "Contain","Equal", "Regex"};
        JComboBox<String> comBox_vuln_method = new JComboBox<>(method_List);

        JLabel label_vuln_reg = new JLabel("匹配值：");
        JTextField text_vuln_value = new JTextField();
        JLabel label_vuln_status_code = new JLabel("状态码：");
        JTextField text_vuln_status_code = new JTextField();


        List<JLabel> is_enable_label_list = new ArrayList<>();
        is_enable_label_list.add(label_vuln_scope);
        is_enable_label_list.add(label_vuln_method);
        is_enable_label_list.add(label_vuln_reg);
        is_enable_label_list.add(label_vuln_status_code);


        combox_request.addItemListener(e -> {
            //用if判断是否选中，如果不加判断，选中或取消两种状l.;'.;''o
            // 态同时出现，可能影响正确的取值
            if (e.getStateChange() == ItemEvent.SELECTED) {
                JComboBox<String> cmb = (JComboBox<String>) e.getSource();
                String str = Objects.requireNonNull(cmb.getSelectedItem()).toString();    //得到选中的内容
                Update_Poo(is_enable_label_list,text_vuln_value,text_vuln_status_code,comBox_vuln_scope,comBox_vuln_method,textArea_request,str,poc_data);
            }
        });



        JLabel label_poc_description = new JLabel("描述信息：");
        JTextField text_poc_description= new JTextField();
        text_poc_description.setText(poc_data.description);

        JButton poc_button_go = new JButton("保存");
        JButton poc_button_cancal = new JButton("取消");


        poc_button_go.addActionListener(e -> {
            //分组
            poc_data.group = (String) comboBox_group.getSelectedItem();
            //插件类型
            poc_data.plugin_type = (String) combox_request.getSelectedItem();
            //分类
            poc_data.type = (String) comboBox_type.getSelectedItem();
            //名称
            poc_data.name = text_vuln_name.getText();
            if(Objects.equals(poc_data.group, "") || Objects.equals(poc_data.type, "") || Objects.equals(poc_data.name, "")){
                JOptionPane.showMessageDialog(null, "必填值为空！", "提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String scope = Objects.requireNonNull(comBox_vuln_scope.getSelectedItem()).toString();
            String match_method = Objects.requireNonNull(comBox_vuln_method.getSelectedItem()).toString();
            String match_value = text_vuln_value.getText();
            int status_code = 0;
            try {
                status_code = (Integer.parseInt((text_vuln_status_code.getText())));
            } catch (Exception ex) {
//                JOptionPane.showMessageDialog(null, "状态码请输入数字！", "提示", JOptionPane.INFORMATION_MESSAGE);
//                return;
            }
//                DumperOptions options = new DumperOptions();
//                options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);//通常使用的yaml格式
//                options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);//标量格式

            Yaml yaml = new Yaml(options);
            if(combox_request.getSelectedItem()=="简单请求替换") {
                try{
                    Map<String, Object> request_data;
                    request_data = yaml.load(textArea_request.getText());
                    Map<String, Object> temp = new HashMap<>();
                    temp.put("requests", request_data);
                    Map<String, Object> temp_expression = new HashMap<>();
                    temp_expression.put("scope", scope);
                    temp_expression.put("match_method", match_method);
                    temp_expression.put("match_value", match_value);
                    temp_expression.put("status_code", status_code);
                    temp.put("expression", temp_expression);
                    poc_data.plugins_data = yaml.dump(temp);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Yaml格式错误！", "提示", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
            } else if (combox_request.getSelectedItem()=="高级HTTP请求"  ) {
                String poc_requests_raw =  textArea_request.getText();
                Map<String, Object> temp = new HashMap<>();
                temp.put("requests_raw", poc_requests_raw);
                Map<String, Object> temp_expression = new HashMap<>();
                temp_expression.put("scope", scope);
                temp_expression.put("match_method", match_method);
                temp_expression.put("match_value", match_value);
                temp_expression.put("status_code", status_code);
                temp.put("expression", temp_expression);
                poc_data.plugins_data = yaml.dump(temp);
            } else if (combox_request.getSelectedItem()=="Yaml Poc"  ) {
                Map<String, Object> _data;
                _data = yaml.load(textArea_request.getText());
                Map<String,Object> detail = (Map<String, Object>) _data.get("detail");
                detail.put("name", text_vuln_name.getText());
                detail.put("category", comboBox_type.getSelectedItem());
                detail.put("group", comboBox_group.getSelectedItem());
                detail.put("description", text_poc_description.getText());
                _data.put("detail", detail);
                poc_data.plugins_data = yaml.dump(_data);
//                    poc_data.plugins_data = String.valueOf(textArea_request.getText());
            } else if (combox_request.getSelectedItem()=="Python Poc"  ) {
                poc_data.plugins_data = textArea_request.getText();
            }
            poc_data.description = text_poc_description.getText();
            String message = Save_Poc_Data(poc_data);
            try {
                reload_read_poc_Data();
                String[] items1 = Poc.get_all_poc_group();
                vuln_poc_combox.removeAllItems();
                vuln_disenable_finger_poc_combox.removeAllItems();
                for(String i : items1){
                    vuln_poc_combox.addItem(i);
                    vuln_disenable_finger_poc_combox.addItem(i);
                }
//                    vuln_poc_combox.setModel(new DefaultComboBoxModel<String>(items));
//                    vuln_disenable_finger_poc_combox.setModel(new DefaultComboBoxModel<String>(items));

            } catch (SQLException ex) {
                printErr(ex.getMessage());
                printErr(Arrays.toString(ex.getStackTrace()));
            }
            JOptionPane.showMessageDialog(null, message, "提示", JOptionPane.INFORMATION_MESSAGE);
            if(message.contains("保存成功")){
                mainFrame.setVisible(false);
                try {
                    Poc.reload_read_poc_Data();
                } catch (SQLException ex) {
                    printErr(String.valueOf(ex));
                }
            }
        });
        poc_button_cancal.addActionListener(e -> mainFrame.setVisible(false));
        JPanel jpanel_poc_ = new JPanel();
        GridBagLayout gbl=new GridBagLayout();//创建网格包布局管理器
        GridBagConstraints gbc=new GridBagConstraints();//GridBagConstraints对象来给出每个组件的大小和摆放位置
        jpanel_poc_.setLayout(gbl);//设置容器布局为网格包布局类型
        gbc.fill=GridBagConstraints.BOTH;//组件填充显示区域，当格子有剩余空间时，填充空间
        gbc.insets = new Insets(5, 5, 5, 5); // 边距
        gbc.anchor = GridBagConstraints.NORTHWEST; // 对齐方式
//        gridx 和 gridy：指定组件放置在网格中的起始位置。
//        gridwidth 和 gridheight：指定组件跨越的网格单元数量。
//        fill：指定组件如何填充其网格单元。
//        weightx 和 weighty：指定组件在可伸缩空间中的权重

        //第一行
        jpanel_poc_.add(Add_Component(gbl,label_vuln_type,gbc,0,0,1,1,0,0));
        jpanel_poc_.add(Add_Component(gbl,comboBox_type,gbc,1,0,1,2,1,0));
        jpanel_poc_.add(Add_Component(gbl,label_vuln_name,gbc,3,0,1,1,0,0));
        jpanel_poc_.add(Add_Component(gbl,text_vuln_name,gbc,4,0,1,2,1,0));

        //第二行
        jpanel_poc_.add(Add_Component(gbl,label_vuln_group,gbc,0,1,1,1,0,0));
        jpanel_poc_.add(Add_Component(gbl,comboBox_group,gbc,1,1,1,2,1,0));
        jpanel_poc_.add(Add_Component(gbl,label_vuln_dir,gbc,3,1,1,1,0,0));
        jpanel_poc_.add(Add_Component(gbl,comBox_vuln_dir,gbc,4,1,1,2,1,0));

        //第三行
        jpanel_poc_.add(Add_Component(gbl,label_vuln_scope,gbc,0,2,1,1,0,0));
        jpanel_poc_.add(Add_Component(gbl,comBox_vuln_scope,gbc,1,2,1,2,1,0));
        jpanel_poc_.add(Add_Component(gbl,label_vuln_method,gbc,3,2,1,1,0,0));
        jpanel_poc_.add(Add_Component(gbl,comBox_vuln_method,gbc,4,2,1,2,1,0));

        //第四 行
        jpanel_poc_.add(Add_Component(gbl,label_vuln_reg,gbc,0,3,1,1,0,0));
        jpanel_poc_.add(Add_Component(gbl,text_vuln_value,gbc,1,3,1,2,1,0));
        jpanel_poc_.add(Add_Component(gbl,label_vuln_status_code,gbc,3,3,1,1,0,0));
        jpanel_poc_.add(Add_Component(gbl,text_vuln_status_code,gbc,4,3,1,2,1,0));

        //描述信息
        jpanel_poc_.add(Add_Component(gbl,label_poc_description,gbc,0,4,1,1,0,0));
        jpanel_poc_.add(Add_Component(gbl,text_poc_description,gbc,1,4,1,5,1,0));

        //
        jpanel_poc_.add(Add_Component(gbl,lable_reques_type,gbc,0,5,1,1,0,0));
        jpanel_poc_.add(Add_Component(gbl,combox_request,gbc,1,5,1,5,1,0));

        //请求
        jpanel_poc_.add(Add_Component(gbl,scrollPane_request,gbc,0,6,5,6,1,1));
        //按钮行
        gbc.fill=GridBagConstraints.NORTH;//组件填充显示区域，当格子有剩余空间时，填充空间
        gbc.anchor = GridBagConstraints.CENTER; // 对齐方式

        jpanel_poc_.add(Add_Component(gbl,poc_button_go,gbc,2,12,1,1,0,0));
        jpanel_poc_.add(Add_Component(gbl,poc_button_cancal,gbc,3,12,1,1,0,0));

        Update_Poo(is_enable_label_list,text_vuln_value,text_vuln_status_code,comBox_vuln_scope,comBox_vuln_method,textArea_request,poc_data.plugin_type,poc_data);
//        jpanel_poc_.setBorder(new EmptyBorder(10, 10, 10, 10)); // 设置外边距为10
        mainFrame.getContentPane().add(jpanel_poc_);
        mainFrame.setSize(1000, 1100);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }
    public static Component Add_Component(GridBagLayout gbl,Component comp,GridBagConstraints gbc,int gridx,int gridy,int gridheight,int gridwidth,int weight_x,int weight_y)
    {
        gbc.weightx=weight_x;
        gbc.weighty=weight_y;
        gbc.gridheight=gridheight;
        gbc.gridwidth=gridwidth;
        gbc.gridx=gridx;
        gbc.gridy=gridy;
        gbl.setConstraints(comp, gbc);
        return comp;
    }
    public static void Update_Poo(List<JLabel> is_enable_label_list,JTextField text_vuln_value,JTextField text_vuln_status_code,JComboBox comBox_vuln_scope,JComboBox comBox_vuln_method,RSyntaxTextArea textArea_request,String yuyan,PocEntry poc_data){
//        DumperOptions options = new DumperOptions();
//        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);//通常使用的yaml格式
//        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);//标量格式
        if(yuyan.equals("简单请求替换") || yuyan.equals("高级HTTP请求")){
            for (JLabel item :is_enable_label_list){
                item.setEnabled(true);
            }
            comBox_vuln_scope.setEnabled(true);
            comBox_vuln_method.setEnabled(true);
            text_vuln_value.setEnabled(true);
            text_vuln_status_code.setEnabled(true);
        }else{
            for (JLabel item :is_enable_label_list){
                item.setEnabled(false);
            }
            comBox_vuln_scope.setEnabled(false);
            comBox_vuln_method.setEnabled(false);
            text_vuln_value.setEnabled(false);
            text_vuln_status_code.setEnabled(false);
        }

        switch (yuyan) {
            case "简单请求替换":
                textArea_request.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_YAML);
                try {
                    if (!Objects.equals(poc_data.name, "")) {
                        Yaml yaml = new Yaml(options);
                        Map<String, Object> temp_data = yaml.load(poc_data.plugins_data);
                        Map temp_requests = (Map) temp_data.get("requests");
                        String requests_yamlString = yaml.dump(temp_requests);
                        textArea_request.setText(requests_yamlString);
                        HashMap temp_expression = (HashMap) temp_data.get("expression");
                        text_vuln_value.setText((String) temp_expression.get("match_value"));
                        int status_code = (int) temp_expression.get("status_code");
                        text_vuln_status_code.setText(String.valueOf(status_code));
                        comBox_vuln_scope.setSelectedItem((String) temp_expression.get("scope"));
                        comBox_vuln_method.setSelectedItem((String) temp_expression.get("match_method")); // 方法二：指定列表值
                    }
                } catch (Exception e) {
                    textArea_request.setText((String) poc_data.plugins_data);
                    printErr(String.valueOf(e.getMessage()));
                    printErr(Arrays.toString(e.getStackTrace()));
                }
                if (Objects.equals(poc_data.plugins_data, "")) {
                    textArea_request.setText((String) Global_Config.get("Simple_HTTP_Request"));
                }
                break;
            case "高级HTTP请求":
                textArea_request.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
                try {
                    if (!Objects.equals(poc_data.name, "")) {
                        Yaml yaml = new Yaml(options);
                        Map<String, Object> temp_data = yaml.load(poc_data.plugins_data);
                        textArea_request.setText((String) temp_data.get("requests_raw"));
                        HashMap temp_expression = (HashMap) temp_data.get("expression");
                        text_vuln_value.setText((String) temp_expression.get("match_value"));
                        int status_code = (int) temp_expression.get("status_code");
                        text_vuln_status_code.setText(String.valueOf(status_code));
                        comBox_vuln_scope.setSelectedItem(temp_expression.get("scope"));
                        comBox_vuln_method.setSelectedItem(temp_expression.get("match_method")); // 方法二：指定列表值
                    }
                } catch (Exception e) {
                    textArea_request.setText(poc_data.plugins_data);
                    printErr(String.valueOf(e));
                }
                if (Objects.equals(poc_data.plugins_data, "")) {
                    textArea_request.setText((String) Global_Config.get("Advanced_HTTP_Request"));
                }
                break;
            case "Yaml Poc":
                textArea_request.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_YAML);
                textArea_request.setText(poc_data.plugins_data);
                if (Objects.equals(poc_data.plugins_data, "")) {
                    textArea_request.setText((String) Global_Config.get("Yaml_Poc"));
                }
                break;
            case "Python Poc":
                textArea_request.setText(poc_data.plugins_data);
                textArea_request.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PYTHON);
                if (Objects.equals(poc_data.plugins_data, "")) {
                    textArea_request.setText((String) Global_Config.get("Python_Poc"));
                }
                break;
            default:
                textArea_request.setText(poc_data.plugins_data);
                break;
        }
    }

    public static List<PocEntry> get_mathch_poc(String findr_str) {
        List<String> finger_list = Arrays.asList(findr_str.split("\\$\\$\\$"));
        List<PocEntry> all_enale_poc = new ArrayList<>();
        for (PocEntry poc : enable_poc_list) {
            if (finger_list.contains(poc.type) || Objects.equals(poc.type, "ALL")) {
                all_enale_poc.add(poc);
            }
        }
        return mergeAndRemoveDuplicates(jump_finger_poc_list,all_enale_poc);
    }

    public static  String Replace_Var_Request(IHttpRequestResponse messageInfo,String requests_raw) throws MalformedURLException {
        URL wanzheng_url;
        if(is_burp){
            wanzheng_url = helpers.analyzeRequest(messageInfo).getUrl();
        }else{
            String url  = get_requests_url(messageInfo,true);
            wanzheng_url = new URL(url);
        }
        // 获取整个路径
        String base_url_path = wanzheng_url.getPath();
        // 如果你想去掉可能存在的查询参数
        if (base_url_path.contains("?")) {
            base_url_path = base_url_path.substring(0, base_url_path.indexOf('?'));
        }
        base_url_path = base_url_path.startsWith("/") ? base_url_path : "/" + base_url_path;
        // 获取主机名
//        String host = wanzheng_url.getHost()+":";
        String base_path;
        if (base_url_path.endsWith("/")) {
            base_path = base_url_path;
        }else{
            // 查找最后一个斜杠的位置
            int lastSlashIndex = base_url_path.lastIndexOf('/');
            // 如果找到了斜杠，保留从开始到斜杠的位置
            if (lastSlashIndex != -1) {
                base_path =  base_url_path.substring(0, lastSlashIndex + 1);
            } else {
                // 如果路径中没有斜杠，返回空字符串或其他错误提示
                base_path =  "/";
            }
        }
        requests_raw = requests_raw.replaceAll("\\{\\{Host\\}\\}",wanzheng_url.getAuthority());
        requests_raw = requests_raw.replaceAll("\\{\\{Base_Url\\}\\}",base_url_path);
        requests_raw = requests_raw.replaceAll("\\{\\{Base_Path\\}\\}",base_path);
        requests_raw = requests_raw.replaceAll("\\{\\{Url\\}\\}",wanzheng_url.toString());
        requests_raw = requests_raw.replaceAll("\\{\\{Random_UserAgent\\}\\}",Get_Random_UserAgent());
        //getbody
        byte[] requestBytes = messageInfo.getRequest();
        String body;
        Map<String, Object> head_body = null;
        if(is_burp){
            // 分析请求以获取请求体的偏移量
            int bodyOffset = helpers.analyzeRequest(messageInfo).getBodyOffset();
            // 获取请求体
            byte[] bodyBytes = new byte[requestBytes.length - bodyOffset];
            System.arraycopy(requestBytes, bodyOffset, bodyBytes, 0, requestBytes.length - bodyOffset);
            body =  new String(bodyBytes, StandardCharsets.UTF_8);
        }else{
            head_body =  get_requests_response_head_body(messageInfo.getRequest());
            body = (String) head_body.get("body");
        }
        requests_raw = requests_raw.replaceAll("\\{\\{Body\\}\\}",body);
        //修改请求头
        List<String> source_headers;
        if(is_burp){
            source_headers = helpers.analyzeRequest(messageInfo).getHeaders();
        }else{
            source_headers = (List<String>) head_body.get("header");
        }
        if(source_headers!=null && !source_headers.isEmpty()){
            for (String item : source_headers) {
                String[] item_list = item.split(" ");
                if(item_list.length>1){
                    String key = item_list[0];
                    requests_raw = requests_raw.replaceAll("\\{\\{"+key+"\\}\\}",item_list[1]);
                }
            }
        }
        return requests_raw;
    }




    public static List<String> Set_Header_Requests(IHttpRequestResponse requestResponse, String method,String url,Map<String,String> headers){
        String source_method;
        List<String> source_headers;
        if(is_burp){
            source_method = helpers.analyzeRequest(requestResponse).getMethod(); // 获取提交方法
            source_headers = helpers.analyzeRequest(requestResponse).getHeaders();

        }else{
            String[] head_list = get_requests_head_line(requestResponse);
            source_method = head_list[0]; // 获取提交方法
            source_headers = (List<String>) get_requests_response_head_body(requestResponse.getRequest()).get("header");
        }
        if( method ==null || method.isEmpty()){
            method=source_method;
        }
        String headUrl = source_headers.get(0);
        List<String> keyValueStrings;
        if(headers !=null && !headers.isEmpty()){
            source_headers.subList(1, source_headers.size());
            //将heads转换为key、value的形式
            Map<String,String> Source_heads_map  = new HashMap<>();
            for (String item : source_headers) {
                String[] item_list = item.split(" ");
                String key = item_list[0];
                String value="";
                if(item_list.length>1){
                    value = item_list[1];
                }
                Source_heads_map.put(key,value);
            }
            Source_heads_map.putAll(headers);
            Source_heads_map.put("Cookie",Source_heads_map.get("Cookie").replaceAll("\\{\\{Random_UserAgent\\}\\}",Get_Random_UserAgent()));
            // 遍历 Map 的键值对
            keyValueStrings = new ArrayList<>();
            for (Map.Entry<String, String> entry : Source_heads_map.entrySet()) {
                // 将键和值转换为字符串，并用空格分隔
                String keyValueString = entry.getKey() + " " + entry.getValue();
                keyValueStrings.add(keyValueString);
            }
        }else{
            keyValueStrings = source_headers;
        }
        String[] e = headUrl.split(" ");
        url = url.replaceAll("//+","/");
        String result_headUrl = method.toUpperCase()+" "+url+" "+e[2];
        keyValueStrings.set(0,result_headUrl);
        return  keyValueStrings;
    }

    public static Boolean Match_poc_data(IHttpRequestResponse requestResponse, Map<String,Object> poc){
        if((int)poc.get("status_code") !=0  && (GetMessageStatusCode(requestResponse)!=(int)poc.get("status_code"))){
            return  false;
        }
        boolean scan_result = false;
        String match_source_data = "";
        if(Objects.equals(poc.get("scope"), "response")){
            match_source_data = new String(requestResponse.getResponse());
        }else if (Objects.equals(poc.get("scope"), "response header")){
            String response_headers="";
            if (is_burp){
                response_headers = helpers.analyzeResponse(requestResponse.getResponse()).getHeaders().toString();
            }else{
                Map<String, Object> head_body = get_requests_response_head_body(requestResponse.getResponse());
                response_headers = (String) head_body.get("body");
            }
            match_source_data = response_headers;
        }else if (Objects.equals(poc.get("scope"), "response body")){
            if(is_burp){
                String response_data = new String(requestResponse.getResponse());
                IResponseInfo analyzeResponse = helpers.analyzeResponse(requestResponse.getResponse());
                int bodyOffset = analyzeResponse.getBodyOffset();
                match_source_data = response_data.substring(bodyOffset);
            }else{
                Map<String, Object> body_map = get_requests_response_head_body(requestResponse.getResponse());
                match_source_data= (String) body_map.get("body");
            }

        }else if (Objects.equals(poc.get("scope"), "response title")){
            String response_data = new String(requestResponse.getResponse());
            Pattern titlePattern = Pattern.compile("<title>(.*?)</title>", Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
            Matcher matcher = titlePattern.matcher(response_data);
            if (matcher.find()) {
                match_source_data =  matcher.group(1).trim(); // 返回title标签内容
            }
        }
        else{
            match_source_data="";
        }
        //开始进行匹配   ”Contain","Equal", "Regex"}
        if(!Objects.equals(poc.get("match_method"), "") && !Objects.equals(poc.get("match_value"), "") && !Objects.equals(match_source_data, "")){
            if(Objects.equals(poc.get("match_method"), "Contain") && match_source_data.contains((String) poc.get("match_value"))) {
                scan_result = true;
            }else if(Objects.equals(poc.get("match_method"), "Equal") && match_source_data.equals(poc.get("match_value"))) {
                scan_result = true;
            }else if(Objects.equals(poc.get("match_method"), "Regex")) {
                try{
                    if (!match_source_data.isEmpty()){
                        Pattern pattern = Pattern.compile((String) poc.get("match_value"));
                        Matcher matcher = pattern.matcher(match_source_data);
                        // 查找匹配项
                        if (matcher.find()) {
                            scan_result = true;
                        }
                    }
                }catch (Exception ex){
                    printErr(Arrays.toString(ex.getStackTrace()));
                }
            }

        }
        return scan_result;
    }

    /**
     * 统计某个字符串在指定字符串中出现的个数
     */
    public static List<String> countString(String str, String s) {
//        int count = 0,len = str.length();
        List<String> list = new ArrayList<>();
        String ss = str;
        while(str.contains(s)) {
            str = str.substring(str.indexOf(s) + 1);
            list.add(ss.replace(str, s).replaceAll("//+","/"));
            count++;
        }
//        Collections.reverse(list);
        return list;
    }

}
