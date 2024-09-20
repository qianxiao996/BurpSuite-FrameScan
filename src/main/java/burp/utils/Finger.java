package burp.utils;
import burp.IHttpRequestResponse;
import burp.IResponseInfo;
import burp.Main_Vuln;
import burp.model.finger.FingerEntry;
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

public class Finger {

    public static String delete_finger_data(FingerEntry temp) {
        String sql =  "delete  from finger where id = "+temp.id;
        return Sql.Delete(sql);
    }

    public static void reload_read_finger_Data() throws SQLException {
        model_finger.ClearData();
        model_finger.fireTableDataChanged();
        String sql = "select * from finger";
        List<Map<String, Object>> sql_result = Sql.Select(sql);
        for (Map<String, Object> item : sql_result) {
            int id =  (int) item.get("id");
            String cms= (String) item.get("cms");
            String method = (String) item.get("method");
            String location=  (String) item.get("location");
            String keyword=  (String) item.get("keyword");
            int isImportant= (int) item.get("isImportant");
            String type=  (String) item.get("type");
            FingerEntry temp = new FingerEntry(id,cms,method,location,keyword,isImportant,type);
            model_finger.addValueAt(temp);
        }
        printMsg("Finger Data Load Suceess！");
    }

    public static String Save_Finger_Data(FingerEntry fingerdata) throws SQLException {
        Group.Add_Group(fingerdata.cms,"指纹POC");
        String url = "jdbc:sqlite:/"+ Main_Vuln.SQL_DB_PATH;
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement pstmt = connection.prepareStatement(
                     "INSERT OR REPLACE INTO finger (`id`,`cms`,`method`,`location`,`keyword`,`isImportant`,`type`) VALUES (?,?,?,?, ?, ?,?)")) {
            pstmt.setInt(1, fingerdata.id);
            pstmt.setString(2, fingerdata.cms);
            pstmt.setString(3, fingerdata.method);
            pstmt.setString(4, fingerdata.location);
            pstmt.setString(5, fingerdata.keyword);
            pstmt.setInt(6, fingerdata.isImportant);
            pstmt.setString(7, fingerdata.type);
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
    public static void Save_Finger_Data_All(List<FingerEntry> groupEntries) {
        String url = "jdbc:sqlite:" + Main_Vuln.SQL_DB_PATH;
        try (Connection connection = DriverManager.getConnection(url)) {
            // 开启事务
            connection.setAutoCommit(false);
            String sql = "INSERT OR REPLACE INTO finger (`id`,`cms`,`method`,`location`,`keyword`,`isImportant`,`type`) VALUES (?,?,?,?, ?, ?,?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                for (FingerEntry fingerdata : groupEntries) {
                    pstmt.setInt(1, fingerdata.id);
                    pstmt.setString(2, fingerdata.cms);
                    pstmt.setString(3, fingerdata.method);
                    pstmt.setString(4, fingerdata.location);
                    pstmt.setString(5, fingerdata.keyword);
                    pstmt.setInt(6, fingerdata.isImportant);
                    pstmt.setString(7, fingerdata.type);
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
    public static void finger_edit(FingerEntry finger_data) {
        mainFrame_Finger = new JFrame("编辑指纹");
        mainFrame_Finger.setLayout(new BorderLayout());
        JLabel label_vuln_type = new JLabel("漏洞分类：");
//        label_vuln_type.setPreferredSize(new Dimension(70, 20));
        String[] items = get_all_finger_cms("");
        JComboBox<String> comboBox_cms = new JComboBox<>(items);
//        comboBox_cms.setPreferredSize(new Dimension(200,20));
        // 设置ComboBox为可编辑
        comboBox_cms.setEditable(true);
        comboBox_cms.setSelectedItem(finger_data.cms);


        comboBox_cms.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String keyword = ((JTextField) comboBox_cms.getEditor().getEditorComponent()).getText();
                    String[] items = get_all_finger_cms(keyword);
                    comboBox_cms.setModel(new DefaultComboBoxModel<>(items));
                    comboBox_cms.setSelectedItem(keyword);

                }
            }
        });
        JLabel label_vuln_method = new JLabel("匹配方法：");
//        label_vuln_method.setPreferredSize(new Dimension(70, 20));
//
        String[] method_List = { "keyword","faviconhash"};
        JComboBox<String> comBox_vuln_method = new JComboBox<>(method_List);
//        comBox_vuln_method.setSelectedIndex(2); // 方法一：列表下标，从0开始
        comBox_vuln_method.setSelectedItem(finger_data.method); // 方法二：指定列表值
//

        JLabel label_vuln_scope = new JLabel("匹配位置：");
//        label_vuln_scope.setPreferredSize(new Dimension(70, 20));
        String[] strList = { "body", "header", "title"};
        JComboBox<String> comBox_vuln_location = new JComboBox<>(strList);
//        comBox.setSelectedIndex(2); // 方法一：列表下标，从0开始
        comBox_vuln_location.setSelectedItem(finger_data.location); // 方法二：指定列表值
//        JTextField text_vuln_scope = new JTextField(finger_data.scope);


        JLabel label_vuln_reg = new JLabel("匹配值：");
//        label_vuln_reg.setPreferredSize(new Dimension(70, 20));
        JTextField text_vuln_value = new JTextField(finger_data.keyword);


        JLabel label_vuln_important = new JLabel("重要程度：");
//        label_vuln_important.setPreferredSize(new Dimension(70, 20));
        Integer[] dir_List = { 0,1,2,3,4,5};
        JComboBox<Integer> comBox_vuln_important = new JComboBox<>(dir_List);



        JLabel label_vuln_group = new JLabel("CMS分类：");
        String[] items_group = get_all_finger_type();
        JComboBox<String> comboBox_type = new JComboBox<>(items_group);
//        comboBox_type.setPreferredSize(new Dimension(200,20));
        // 设置ComboBox为可编辑
        comboBox_type.setEditable(true);
        comboBox_type.setSelectedItem(finger_data.type);

        JButton finger_button_go = new JButton("确定");
        JButton finger_button_cancal = new JButton("取消");

        finger_button_go.addActionListener(e -> {
            finger_data.cms = (String) comboBox_cms.getSelectedItem();
            finger_data.method = (String) comBox_vuln_method.getSelectedItem();
            finger_data.location = (String) comBox_vuln_location.getSelectedItem();
            finger_data.keyword = text_vuln_value.getText();
            try{
                if( comBox_vuln_important.getSelectedItem()!=null){
                    finger_data.isImportant = (int) comBox_vuln_important.getSelectedItem();
                }else{
                    finger_data.isImportant=1;
                }
            }catch (Exception  a){
                finger_data.isImportant=1;
            }

            finger_data.type = (String) comboBox_type.getSelectedItem();
            String message = null;
            try {
                message = Save_Finger_Data(finger_data);
            } catch (SQLException ex) {
                printErr(ex.getMessage());
                printErr(Arrays.toString(ex.getStackTrace()));
            }
            try {
                reload_read_finger_Data();
            } catch (SQLException ex) {
                printErr(ex.getMessage());
                printErr(Arrays.toString(ex.getStackTrace()));
            }
            JOptionPane.showMessageDialog(null, message, "提示", JOptionPane.INFORMATION_MESSAGE);
            mainFrame_Finger.setVisible(false);
            try {
                Finger.reload_read_finger_Data();
                Group.reload_read_group_Data();
            } catch (SQLException ex) {
                printErr(String.valueOf(ex));
            }
        });


        finger_button_cancal.addActionListener(e -> mainFrame_Finger.setVisible(false));

        JPanel jpanel_finger_ = new JPanel();
        GridBagLayout gbl=new GridBagLayout();//创建网格包布局管理器
        GridBagConstraints gbc=new GridBagConstraints();//GridBagConstraints对象来给出每个组件的大小和摆放位置
        jpanel_finger_.setLayout(gbl);//设置容器布局为网格包布局类型
        gbc.fill=GridBagConstraints.BOTH;//组件填充显示区域，当格子有剩余空间时，填充空间
        gbc.insets = new Insets(5, 5, 5, 5); // 边距
        gbc.anchor = GridBagConstraints.NORTHWEST; // 对齐方式
//        gridx 和 gridy：指定组件放置在网格中的起始位置。
//        gridwidth 和 gridheight：指定组件跨越的网格单元数量。
//        fill：指定组件如何填充其网格单元。
//        weightx 和 weighty：指定组件在可伸缩空间中的权重

        jpanel_finger_.add(Add_Component(gbl,label_vuln_group,gbc,0,0,1,1,0,0));
        jpanel_finger_.add(Add_Component(gbl,comboBox_type,gbc,1,0,1,2,1,0));

        jpanel_finger_.add(Add_Component(gbl,label_vuln_type,gbc,0,1,1,1,0,0));
        jpanel_finger_.add(Add_Component(gbl,comboBox_cms,gbc,1,1,1,2,1,0));

        jpanel_finger_.add(Add_Component(gbl,label_vuln_important,gbc,0,2,1,1,0,0));
        jpanel_finger_.add(Add_Component(gbl,comBox_vuln_important,gbc,1,2,1,2,1,0));

        jpanel_finger_.add(Add_Component(gbl,label_vuln_method,gbc,0,3,1,1,0,0));
        jpanel_finger_.add(Add_Component(gbl,comBox_vuln_method,gbc,1,3,1,2,1,0));

        jpanel_finger_.add(Add_Component(gbl,label_vuln_scope,gbc,0,4,1,1,0,0));
        jpanel_finger_.add(Add_Component(gbl,comBox_vuln_location,gbc,1,4,1,2,1,0));

        jpanel_finger_.add(Add_Component(gbl,label_vuln_reg,gbc,0,5,1,1,0,0));
        jpanel_finger_.add(Add_Component(gbl,text_vuln_value,gbc,1,5,1,2,1,0));


        //按钮行
        gbc.fill=GridBagConstraints.NORTH;//组件填充显示区域，当格子有剩余空间时，填充空间
        gbc.anchor = GridBagConstraints.CENTER; // 对齐方式
        jpanel_finger_.add(Add_Component(gbl,finger_button_go,gbc,1,12,1,1,0,0));
        jpanel_finger_.add(Add_Component(gbl,finger_button_cancal,gbc,2,12,1,1,0,0));

        jpanel_finger_.setBorder(new EmptyBorder(10, 10, 10, 10)); // 设置外边距为10
        mainFrame_Finger.getContentPane().add(jpanel_finger_);
        mainFrame_Finger.setSize(500, 400);
        mainFrame_Finger.setLocationRelativeTo(null);
        mainFrame_Finger.setVisible(true);
    }
    public static String[] get_all_finger_type(){
        String sql =  "select DISTINCT `type` from finger";
        List<Map<String, Object>> sql_result = Sql.Select(sql);
        String[] resultArray = new String[sql_result.size()];
        int i=0;
        for (Map<String, Object> item : sql_result) {
            resultArray[i] = (String) item.get("type");
            i++;
        }
        return resultArray;
    }
    public static String[] get_all_finger_cms(String value){
        String sql =  "select DISTINCT `cms` from finger";
        if(!Objects.equals(value, "")) {
            sql = "select DISTINCT `cms` from finger where cms like '%" + value + "%'";
        }
        List<Map<String, Object>> sql_result = Sql.Select(sql);
        String[] resultArray = new String[sql_result.size()];
        int i=0;
        for (Map<String, Object> item : sql_result) {
            resultArray[i] = (String) item.get("cms");
            i++;
        }
        return resultArray;
    }
    public static String get_host_ico(String host_url, IHttpRequestResponse requestResponse) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        String temp_url;
        if(is_burp){
             temp_url = String.valueOf(helpers.analyzeRequest(requestResponse).getUrl());
        }else{
            temp_url = get_requests_url(requestResponse,true);
        }
        URL url  = new URL(temp_url);
        String path = url.getPath(); //获取的路径
        List<String> list = countString(path,"/"); // 获取路径中所有路径的组合
        //得到返回body
        String response_body;
        if(is_burp){
            String response_data = new String(requestResponse.getResponse());
            IResponseInfo analyzeResponse = helpers.analyzeResponse(requestResponse.getResponse());
            int bodyOffset = analyzeResponse.getBodyOffset();
             response_body = response_data.substring(bodyOffset);
        }else{
            Map head_body   = get_requests_response_head_body(requestResponse.getResponse());
            response_body = (String) head_body.get("body");
        }
        String iconpath;
        if (!response_body.isEmpty()){
            // 定义正则表达式
            Pattern pattern = Pattern.compile("<link[^>]*rel=\"(?:shortcut )?icon\"[^>]*href=\"([^\"]*)\"");
            Matcher matcher = pattern.matcher(response_body);
            // 查找匹配项
            if (matcher.find()) {
                iconpath =  matcher.group(1); // 返回找到的第一个图标路径
                list.add(0,iconpath);
            }
        }

        for(String urlpath :list){
            if(urlpath.endsWith("/")){
                urlpath = urlpath+"favicon.ico";
            }else{
                urlpath = urlpath+"/favicon.ico";
            }
            String requests_url= (host_url+urlpath);
//            requests_url =requests_url.replaceAll("://+","$!#!#!$").replaceAll("//+","/").replaceAll("\\$!#!#!\\$","://");
//            String requests_url= url.getProtocol()+"://"+url_dir;
            String http_response =  get_host_ico_http_requests(requests_url);
            try{
                if (!http_response.isEmpty()){
                    int murmu = Hashing
                            .murmur3_32_fixed().hashString(http_response.replaceAll("\r","" )+"\n", StandardCharsets.UTF_8)
                            .asInt();
                    return  String.valueOf(murmu);
                }
            }catch (Exception ex){
                printErr(Arrays.toString(ex.getStackTrace()));
            }
        }
        return "";
    }
    public static String Match_keyword_finger(IHttpRequestResponse requestResponse, FingerEntry finger) {
        String match_source_data = "";
        String ret_url;
        if(is_burp){
            ret_url = String.valueOf(helpers.analyzeRequest(requestResponse).getUrl());
        }else{
            ret_url = get_requests_url(requestResponse,true);
        }

        if(Objects.equals(finger.location, "body")){
            if(is_burp){
                String response_data = new String(requestResponse.getResponse());
                IResponseInfo analyzeResponse = helpers.analyzeResponse(requestResponse.getResponse());
                int bodyOffset = analyzeResponse.getBodyOffset();
                match_source_data = response_data.substring(bodyOffset);
            }else{
                Map head_body   = get_requests_response_head_body(requestResponse.getResponse());
                match_source_data = (String) head_body.get("body");
            }
        }else if (Objects.equals(finger.location, "header")){
            if(is_burp) {
                match_source_data = helpers.analyzeResponse(requestResponse.getResponse()).getHeaders().toString();
            }else{
                Map head_body   = get_requests_response_head_body(requestResponse.getResponse());
                match_source_data =String.join("\n",(List)head_body.get("header"));
            }
        }else if (Objects.equals(finger.location, "title")){
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
        List<String> keyword_list=null;
        try{
            Gson gson = new Gson();
            Type listType = new TypeToken<List<String>>(){}.getType();
            keyword_list = gson.fromJson(finger.keyword, listType);
        }catch (Exception ex){
            printErr(finger.cms+" "+finger.keyword+"Conversion to JSON error:"+ex.getMessage());
        }
        if(keyword_list !=null){
            String   is_success = finger.cms;
            for (String element : keyword_list) {
                if (!match_source_data.contains(element)) {
                    is_success = "";
                    break;
                }
            }
            if(!is_success.isEmpty()){
                printDebug("【Match】"+ret_url+"【Finger】"+finger.cms+"【Keyword】"+keyword_list+"【Success】");
            }else{
                printDebug("【Match】"+ret_url+"【Finger】"+finger.cms+"【Keyword】"+finger.keyword+"【Fail】");
            }
            return is_success;

        } else if ( match_source_data.contains(finger.keyword)) {
            printDebug("【Match】"+ret_url+"【Finger】"+finger.cms+"【Keyword】"+finger.keyword+"【Success】");
            return  finger.cms;
        }
//        printDebug("【Match】"+helpers.analyzeRequest(requestResponse).getUrl()+"【Finger】"+finger.cms+"【Keyword】"+finger.keyword+"【Fail】");
        return  "";
    }
    public static String Match_faviconhash_finger(IHttpRequestResponse requestResponse, FingerEntry finger, String host) {
        String ret_url;
        if(is_burp){
            ret_url = String.valueOf(helpers.analyzeRequest(requestResponse).getUrl());
        }else{
            ret_url = get_requests_url(requestResponse,true);
        }
        if(finger_ico_hash_map.get(host)!=null && !Objects.equals(finger_ico_hash_map.get(host), "")) {
            List<String> keyword_list=null;
            String icon_hash = finger_ico_hash_map.get(host);
            try{
                Gson gson = new Gson();
                Type listType = new TypeToken<List<String>>(){}.getType();
                keyword_list = gson.fromJson(finger.keyword, listType);
            }catch (Exception ex){
                printErr(finger.cms+" "+finger.keyword+"Conversion to JSON error:"+ex.getMessage());
            }
            if(keyword_list !=null){
                for(String poc :keyword_list){
                    if(Objects.equals(icon_hash, poc)){
                        printDebug("【Match】"+ret_url+"【Finger】"+finger.cms+"【Keyword】"+finger.keyword+" Success");
                        return  finger.cms;
                    }
                }
            } else if (Objects.equals(icon_hash, finger.keyword)) {
                printDebug("【Match】"+ret_url+"【Finger】"+finger.cms+"【Keyword】"+finger.keyword+" Success");
                return  finger.cms;
            }
        }
        printDebug("【Match】"+ret_url+"【Finger】"+finger.cms+"【Keyword】"+finger.keyword+"【Fail】");
        return  "";
    }
}
