package burp;

import burp.finger.FingerEntry;
import burp.finger.FingerTableModel;

import burp.rpc.pocGrpc;
import burp.utils.Config;
import burp.utils.Poc;
import burp.log.LogEntry;
import burp.log.LogTableModel;
import burp.poc.PocEntry;
import burp.poc.PocTableModel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.yaml.snakeyaml.DumperOptions;
import burp.utils.Finger;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static burp.BurpExtender.*;
import static burp.utils.Config.save_config;
import static burp.utils.Conn.Get_Random_UserAgent;
import static burp.utils.CustHttpService.GetHttpRequestResponse;
import static burp.utils.Poc.Add_Component;
import static burp.scan.Scan.*;

public class Main_Vuln{
    public static String SQL_DB_FILE="data.db";

    public static String Config_FILE="config.yml";
    public static  String  app_name =  "FrameScan";
    public static  String  app_version =  "v2.4";
    public static  String  app_title =  app_name+ " "+ app_version+" by 浅笑996";
    public static DumperOptions options = new DumperOptions();
    public static List<String> all_scan_url = new ArrayList<>();
    public static Thread grpc_server_thread;
    public static boolean grpc_server_thread_stop_flag=false;
    public static Map<String, Object> Global_Config;

    public static String grpc_host = "localhost";
//    public static String grpc_host = ;
    public static int grpc_port = 23333;

    public static ManagedChannel channel = ManagedChannelBuilder.forAddress(grpc_host, grpc_port).usePlaintext().build();
    public static pocGrpc.pocBlockingStub client = pocGrpc.newBlockingStub(channel);

    public static String Config_PATH;

    public static String SQL_DB_PATH;

    public static boolean is_burp;
    public static Boolean stop_poc_scan=false;
    public static Boolean stop_finger_scan=false;
//    public static boolean is_debug;

    public static JSplitPane splitPane_request_response;
    public static JTextArea app_log; //日志输出框
    public static JFrame  mainFrame; //子窗口

    public static JFrame  mainFrame_Finger; //子窗口

    public static JTable vuln_poc_table; //表格poc
    public static Map<String,String> finger_ico_hash_map=new HashMap<>();
    public static JTable finger_poc_table; //表格poc

    public static JCheckBox chkbox_poc;
    public static JCheckBox chkbox_finger;

    public static JCheckBox chkbox_is_debug;
    public static JCheckBox chkbox_repeater;
    public static JCheckBox chkbox_proxy;
    public static IMessageEditor requestViewer;
    public static IMessageEditor responseViewer;

    public static RSyntaxTextArea responseViewer_1 = new RSyntaxTextArea();
    public static RSyntaxTextArea requestViewer_1 = new RSyntaxTextArea();

    public static final LogTableModel model = new LogTableModel();
    public static final PocTableModel model_poc = new PocTableModel();

    public static final FingerTableModel model_finger = new FingerTableModel();


    public static boolean switchs_poc = true; //开关
    public static boolean switchs_finger = true; //开关
    public static boolean clicks_Repeater=true;//是否启用repeater
    public static boolean clicks_Proxy=true;//是否启用proxy
    public static int count = 0; //记录条数

    static int white_switchs = 0;//白名单开关

    public static List<Object> logList = new ArrayList<>();

    public static JButton btn3_white = new JButton(); // 是否启用白名单
    public static JTextField textField_white;//白名单文本框

    public static JTextField textField_black;//黑名单文本框

    public static String enable_poc_str="";


    public static String jump_finger_poc_str="";

    //启用的poc
    public static List<PocEntry> enable_poc_list = new ArrayList<>();

    //跳过指纹的poc
    public static List<PocEntry>  jump_finger_poc_list = new ArrayList<>();

//    static JTextField statusCodetextField = new JTextField(codeStatus);//白名单文本框
    public static JComboBox<String> vuln_poc_combox;

    //不根据指纹来执行的poc
    public static JComboBox<String> vuln_disenable_finger_poc_combox;


    static JTable logTable = new JTable(model); //第一个表格框
    static String white_URL = ""; //白名单地址
    static String black_URL = "";
    public static void load_config_bypath(String Config_PATH) {
        try {
            Main_Vuln.Global_Config = Config.read_config(Config_PATH);
            Main_Vuln.Config_PATH = Config_PATH;
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void printErr(String msgData) {
        app_log.append("【Error】"+msgData+"\r\n");
        if(is_burp){
            callbacks.printError("【Error】"+msgData);
        }else{
            System.out.println("【Error】"+msgData);
        }
    }
    public static void printMsg(String msgData) {
        app_log.append("【INFO】"+msgData+"\r\n");
        if(is_burp){
            callbacks.printOutput(msgData);
        }else{
            System.out.println("【INFO】"+msgData);
        }
    }
    public static void printDebug(String msgData) {
        if ((Boolean) Global_Config.get("Is_Debug")) {
            app_log.append("【Debug】"+msgData+"\r\n");
            if(is_burp){
                callbacks.printOutput("【Debug】"+msgData);
            }else{
                System.out.println("【Debug】"+msgData);
            }
        }

    }
    public static void load_plugins(Object burpExtendergui,JTabbedPane  tab) throws SQLException, ClassNotFoundException {
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);//通常使用的yaml格式
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);//标量格式
        jump_finger_poc_str=(String) Global_Config.get("DisenableFingerPocList");
        enable_poc_str=(String) Global_Config.get("EnablePocList");

        textField_white = new JTextField();//白名单文本框
        textField_black = new JTextField();//黑名单文本框


        JPopupMenu popupMenu_finger = new JPopupMenu();
        JMenuItem item_finger_add = new JMenuItem("Add");
        JMenuItem item_finger_edit = new JMenuItem("Edit");
        JMenuItem item_finger_delete = new JMenuItem("Delete");
        JMenuItem item_finger_relaod = new JMenuItem("Reload");
        popupMenu_finger.add(item_finger_add);
        popupMenu_finger.add(item_finger_edit);
        popupMenu_finger.add(item_finger_delete);
        popupMenu_finger.add(item_finger_relaod);



        JPopupMenu popupMenu_poc = new JPopupMenu();
        JMenuItem item_poc_add = new JMenuItem("Add");
        JMenuItem item_poc_edit = new JMenuItem("Edit");
        JMenuItem item_poc_delete = new JMenuItem("Delete");
        JMenuItem item_poc_relaod = new JMenuItem("Reload");
        popupMenu_poc.add(item_poc_add);
        popupMenu_poc.add(item_poc_edit);
        popupMenu_poc.add(item_poc_delete);
        popupMenu_poc.add(item_poc_relaod);

        // 创建右键菜单
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem clear_table = new JMenuItem("清空表格");
        JMenuItem clear_huancun = new JMenuItem("清空缓存");
        JMenuItem clear_all = new JMenuItem("一键清空");
        JMenuItem stop_scan_item = new JMenuItem("关闭扫描");
        popupMenu.add(clear_table);
        if(is_burp){
            popupMenu.add(clear_huancun);
        }
        popupMenu.add(clear_all);
        popupMenu.add(stop_scan_item);

        JSplitPane splitPanes = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JPanel jp=new JPanel();
        // table of log entries

        logTable.setAutoCreateRowSorter(true);
        logTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
//                logTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        logTable.getColumnModel().getColumn(0).setMaxWidth(100);
        logTable.getColumnModel().getColumn(1).setMaxWidth(100);
        logTable.getColumnModel().getColumn(3).setMaxWidth(100);
        logTable.getColumnModel().getColumn(4).setMaxWidth(100);
//                logTable.getColumnModel().getColumn(5).setPreferredWidth(300);
        // 为JTable添加鼠标监听器
        logTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) { // 如果是鼠标右键
                    popupMenu.show(logTable, e.getX(), e.getY()); // 显示右键菜单
                }
            }
        });
        clear_table.addActionListener(e -> {
            if(is_burp){
                responseViewer.setMessage("".getBytes(),true);
                requestViewer.setMessage("".getBytes(),true);
            }else{
                responseViewer_1.setText("");
                requestViewer_1.setText("");
            }

            model.ClearData();
//            model.fireTableDataChanged();
        });
        clear_all.addActionListener(e -> {
            if(is_burp){
                responseViewer.setMessage("".getBytes(),true);
                requestViewer.setMessage("".getBytes(),true);
            }else{
                responseViewer_1.setText("");
                requestViewer_1.setText("");
            }
//                out_log.setText(""); // 清楚logList的内容
            logList.clear();
            model.ClearData();
//            model.fireTableDataChanged();
        });
        clear_huancun.addActionListener(e -> logList.clear());
        stop_scan_item.addActionListener(e -> {
            stop_poc_scan = true;
            stop_finger_scan = true;
            chkbox_poc.setSelected(false);
            chkbox_finger.setSelected(false);
            switchs_poc=false;
            switchs_finger =false;
        });



        logTable.getSelectionModel().addListSelectionListener(e -> {

            splitPane_request_response.setDividerLocation(0.5);
            List<LogEntry>  all_table_Data= model.getAllValue();
            int row = logTable.getSelectedRow();
            if(!(row >= 0 && row < all_table_Data.size())){
                return;
            }
            int id = (int)logTable.getValueAt(row,0);
            LogEntry logEntry = null;
            for(LogEntry i : all_table_Data){
                if(i.id==id) {
                    logEntry = i;
                    break;
                }
            }
            if(logEntry!=null)
            {
                if (is_burp){
                    requestViewer.setMessage(logEntry.requestResponse.getRequest(), true);
                    responseViewer.setMessage(logEntry.requestResponse.getResponse(), false);
                }else{
                    PocEntry poc = model_poc.getValueByPocid(logEntry.vuln_poc_id);
                    if(poc!=null){
                        if(Objects.equals(poc.plugin_type, "Yaml Poc")){
                            requestViewer_1.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_YAML);
                        } else if (Objects.equals(poc.plugin_type, "Python Poc")) {
                            requestViewer_1.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PYTHON);
                        }else{
                            requestViewer_1.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
                        }
                    }else{
                        requestViewer_1.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
                        responseViewer_1.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
                    }
                    requestViewer_1.setText(new String(logEntry.requestResponse.getRequest()));
                    responseViewer_1.setText(new String(logEntry.requestResponse.getResponse()));
                }

                currentlyDisplayedItem = logEntry.requestResponse;
            }
        });
        JScrollPane scrollPane = new JScrollPane(logTable); //给列表添加滚动条
        jp.add(scrollPane);    //将表格加到面板
        jp.setLayout(new GridLayout(1,1)); // 设置布局为1行1列

        scrollPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) { // 如果是鼠标右键
                    popupMenu.show(scrollPane, e.getX(), e.getY()); // 显示右键菜单
                }
            }
        });

        chkbox_poc=new JCheckBox("启动POC插件");    //创建指定文本和状态的复选框
        chkbox_finger=new JCheckBox("启动指纹插件");    //创建指定文本和状态的复选框
        chkbox_repeater=new JCheckBox("监控Repeater");    //创建指定文本的复选框
        chkbox_proxy=new JCheckBox("监控Proxy");    //创建指定文本的复选框 //
        chkbox_is_debug=new JCheckBox("Debug");    //创建指定文本的复选框 //
        JLabel label_domain_white = new JLabel("白名单列表:");

        btn3_white=new JButton("当前白名单状态:关闭");    //处理白名单
        white_URL = textField_white.getText();
        textField_white .setEditable(false);
        textField_white .setForeground(Color.GRAY);//设置组件的背景色
        textField_white.setToolTipText("多个域名请用,隔开");
        JLabel label_domain_black = new JLabel("黑名单列表:");
        textField_black .setForeground(Color.GRAY);//设置组件的背景色
        textField_black.setToolTipText("多个域名请用,隔开");
        chkbox_is_debug.addItemListener(e -> {
            printMsg("Debug is "+chkbox_is_debug.isSelected());
            Global_Config.put("Is_Debug",chkbox_is_debug.isSelected());
        });
        //添加复选框监听事件
        chkbox_poc.addItemListener(e -> {
            if(chkbox_poc.isSelected()){
                printDebug("POC Plugins Start");
                switchs_poc = true;
                stop_poc_scan = false;
            }else {
                printDebug("POC Plugins Stop");
                switchs_poc = false;
                stop_poc_scan = true;
            }

        });

        chkbox_finger.addItemListener(e -> {
            if(chkbox_finger.isSelected()){
                printDebug("Finger Plugins Start");
                switchs_finger = true;
                stop_finger_scan = false;

            }else {
                printDebug("Finger Plugins Stop");
                switchs_finger = false;
                stop_finger_scan = true;
            }

        });

        chkbox_repeater.addItemListener(e -> {
            if (chkbox_repeater.isSelected()){
                printDebug("Start Repeater");
                clicks_Repeater  = true;
            }else {
                printDebug("Stop Repeater");
                clicks_Repeater  = false;
            }
        });
        chkbox_proxy.addItemListener(e -> {
            if(chkbox_proxy.isSelected()) {
                printDebug("Start Proxy");
                clicks_Proxy  = true;
            }else {
                printDebug("Stop Proxy");
                clicks_Proxy  = false;
            }
        });

        //加载自定义payload
        btn3_white.addActionListener(e -> {
            if(btn3_white.getText().equals("当前白名单状态:启用")){
                btn3_white.setText("当前白名单状态:关闭");
                white_URL = textField_white .getText();
                white_switchs = 1;
                textField_white .setEditable(false);
                textField_white .setForeground(Color.GRAY);//设置组件的背景色
            }else {
                btn3_white.setText("当前白名单状态:启用");
                white_switchs = 0;
                textField_white .setEditable(true);
                textField_white .setForeground(Color.BLACK);
            }
        });
        JLabel tempJLabel = new JLabel("启用漏洞POC:");
        String[] items = Poc.get_all_poc_group();
//        String[] dir_List = { "0","1", "2","3","4","5"};
        vuln_poc_combox = new JComboBox<>(items);

        //判断是否所有的items都在数据库分类中
        List<String> enable_poc_temp_list = Arrays.stream(enable_poc_str.split(","))
                .collect(Collectors.toList());
        List<String>  enable_poc_temp=new ArrayList<>();
        for(String aa :enable_poc_temp_list){
            boolean baohan_flag = Arrays.asList(items).contains(aa);
            if (baohan_flag){
                enable_poc_temp.add(aa);
            }
        }
        enable_poc_str = enable_poc_temp.stream()
                .filter(s -> s != null && !s.isEmpty()) // 过滤掉 null 和空字符串
                .collect(Collectors.joining(",")); // 使用 "," 连接列表中的元素
        //end

        vuln_poc_combox.setEditable(true);
        vuln_poc_combox.setSelectedItem(enable_poc_str);
        vuln_poc_combox.setEditable(false);


//        printDebug(String.valueOf(enable_poc_list.size()));
        // 添加 ActionListener
        vuln_poc_combox.addActionListener(e -> {
            String selectedValue = (String) vuln_poc_combox.getSelectedItem();
            // 使用 Stream API 将字符串分割成 List
            List<String> enable_poc_split = Arrays.stream(enable_poc_str.split(","))
                    .collect(Collectors.toList());
            if (enable_poc_split.contains(selectedValue)) {
                enable_poc_split.remove(selectedValue);
            } else {
                enable_poc_split.add(selectedValue);
            }
            // 使用 Stream API 过滤掉空字符串和 null 值
            enable_poc_str = enable_poc_split.stream()
                    .filter(s -> s != null && !s.isEmpty()) // 过滤掉 null 和空字符串
                    .collect(Collectors.joining(",")); // 使用 "," 连接列表中的元素

            vuln_poc_combox.setEditable(true);
            vuln_poc_combox.setSelectedItem(enable_poc_str);
            vuln_poc_combox.setEditable(false);
        });

        JLabel dislabel = new JLabel("跳过指纹POC:");
        vuln_disenable_finger_poc_combox = new JComboBox<>(items);

        //判断是否所有的items都在数据库分类中
        List<String> disenable_poc_temp_list = Arrays.stream(jump_finger_poc_str.split(","))
                .collect(Collectors.toList());
        List<String>  jump_finger_poc_temp=new ArrayList<>();
        for(String aa :disenable_poc_temp_list){
            boolean baohan_flag = Arrays.asList(items).contains(aa);
            if (baohan_flag){
                jump_finger_poc_temp.add(aa);
            }
        }
        jump_finger_poc_str = jump_finger_poc_temp.stream()
                .filter(s -> s != null && !s.isEmpty()) // 过滤掉 null 和空字符串
                .collect(Collectors.joining(",")); // 使用 "," 连接列表中的元素
        //end

        vuln_disenable_finger_poc_combox.setEditable(true);
        vuln_disenable_finger_poc_combox.setSelectedItem(jump_finger_poc_str);
        vuln_disenable_finger_poc_combox.setEditable(false);

        // 添加 ActionListener
        vuln_disenable_finger_poc_combox.addActionListener(e -> {
            String selectedValue = (String) vuln_disenable_finger_poc_combox.getSelectedItem();
            // 使用 Stream API 将字符串分割成 List
            List<String> disenable_poc_list = Arrays.stream(jump_finger_poc_str.split(","))
                    .collect(Collectors.toList());
            if (disenable_poc_list.contains(selectedValue)) {
                disenable_poc_list.remove(selectedValue);
            } else {
                disenable_poc_list.add(selectedValue);
            }
            jump_finger_poc_str = disenable_poc_list.stream()
                    .filter(s -> s != null && !s.isEmpty()) // 过滤掉 null 和空字符串
                    .collect(Collectors.joining(",")); // 使用 "," 连接列表中的元素

            vuln_disenable_finger_poc_combox.setEditable(true);
            vuln_disenable_finger_poc_combox.setSelectedItem(jump_finger_poc_str);
            vuln_disenable_finger_poc_combox.setEditable(false);

        });
        JButton reload_config_button = new JButton("重载配置");
        JButton save_config_button = new JButton("保存配置");
        reload_config_button.addActionListener(e -> {
            try {
                load_config();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        save_config_button.addActionListener(e -> save_config());
        splitPane_request_response = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        if(is_burp){
            requestViewer =  callbacks.createMessageEditor((IMessageEditorController) burpExtendergui, false);
            responseViewer =  callbacks.createMessageEditor((IMessageEditorController) burpExtendergui, false);
            splitPane_request_response.setLeftComponent(requestViewer.getComponent());//添加在左面
            splitPane_request_response.setRightComponent(responseViewer.getComponent());//添加在右面
        }else{
            JTabbedPane request_messageeditor = new JTabbedPane();
            JPanel  tab_requests = new JPanel();
            tab_requests.setLayout(new GridLayout(1,1,5,5));
            requestViewer_1.setEditable(false);
            requestViewer_1.setLineWrap(true);
            requestViewer_1.setCodeFoldingEnabled(true);
            responseViewer_1.setEditable(false);
            responseViewer_1.setLineWrap(true);
            responseViewer_1.setCodeFoldingEnabled(true);

            requestViewer_1.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
            responseViewer_1.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);

            RTextScrollPane jscroll_requestViewer = new RTextScrollPane(requestViewer_1);
            RTextScrollPane jscroll_responseViewer = new RTextScrollPane(responseViewer_1);
            //设置 垂直滚动条需要时显示
            jscroll_requestViewer.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            jscroll_responseViewer.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            jscroll_requestViewer.setBorder(null);
            jscroll_responseViewer.setBorder(null);
            requestViewer_1.setBorder(null);
            responseViewer_1.setBorder(null);
//            responseViewer_1.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

            tab_requests.add(jscroll_requestViewer);
            request_messageeditor.addTab("Requests",tab_requests);

            JTabbedPane response_messageeditor = new JTabbedPane();
            JPanel  tab_response = new JPanel();
            tab_response.setLayout(new GridLayout(1,1,5,5));
            tab_response.add(jscroll_responseViewer);
            response_messageeditor.addTab("Response",tab_response);
            splitPane_request_response.setLeftComponent(request_messageeditor);//添加在左面
            splitPane_request_response.setRightComponent(response_messageeditor);//添加在右面
        }

        splitPane_request_response.setDividerLocation(0.5);//设置分割的大小

        app_log =  new JTextArea();
        // 设置文本区域的大小
        app_log.setColumns(1); // 设置列数
        app_log.setRows(20);    // 设置行数
        app_log.setLineWrap(true);
        // 创建 JScrollPane 来包含 JTextArea，以便在文本过长时提供滚动条
        JScrollPane app_log_scrollPane = new JScrollPane(app_log);

        JPanel jpanel_config_ = new JPanel();
        jpanel_config_.setMaximumSize(new Dimension(1000,1000));
        GridBagLayout gbl=new GridBagLayout();//创建网格包布局管理器
        GridBagConstraints gbc=new GridBagConstraints();//GridBagConstraints对象来给出每个组件的大小和摆放位置
        jpanel_config_.setLayout(gbl);//设置容器布局为网格包布局类型
        gbc.fill=GridBagConstraints.BOTH;//组件填充显示区域，当格子有剩余空间时，填充空间
        gbc.insets = new Insets(5, 5, 5, 5); // 边距
        gbc.anchor = GridBagConstraints.NORTH; // 对齐方式
//        gridx 和 gridy：指定组件放置在网格中的起始位置。
//        gridwidth 和 gridheight：指定组件跨越的网格单元数量。
//        fill：指定组件如何填充其网格单元。
//        weightx 和 weighty：指定组件在可伸缩空间中的权重

        //第一行
        jpanel_config_.add(Add_Component(gbl,chkbox_poc,gbc,0,0,1,1,1,0));
        jpanel_config_.add(Add_Component(gbl,chkbox_finger,gbc,1,0,1,1,1,0));
        jpanel_config_.add(Add_Component(gbl,chkbox_repeater,gbc,2,0,1,1,1,0));
        jpanel_config_.add(Add_Component(gbl,chkbox_proxy,gbc,3,0,1,1,0,0));
        jpanel_config_.add(Add_Component(gbl,chkbox_is_debug,gbc,4,0,1,1,0,0));


        //白名单
        jpanel_config_.add(Add_Component(gbl,label_domain_white,gbc,0,1,1,1,0,0));
        jpanel_config_.add(Add_Component(gbl,textField_white,gbc,1,1,1,2,1,0));
        jpanel_config_.add(Add_Component(gbl,btn3_white,gbc,3,1,1,2,0,0));

        //黑名单
        jpanel_config_.add(Add_Component(gbl,label_domain_black,gbc,0,2,1,1,0,0));
        jpanel_config_.add(Add_Component(gbl,textField_black,gbc,1,2,1,4,1,0));

        //启用poc
        jpanel_config_.add(Add_Component(gbl,tempJLabel,gbc,0,3,1,1,0,0));
        jpanel_config_.add(Add_Component(gbl,vuln_poc_combox,gbc,1,3,1,4,1,0));

        //跳过poc
        jpanel_config_.add(Add_Component(gbl,dislabel,gbc,0,4,1,1,0,0));
        jpanel_config_.add(Add_Component(gbl,vuln_disenable_finger_poc_combox,gbc,1,4,1,4,1,0));


        //按钮行
//        gbc.fill=GridBagConstraints.NORTH;//组件填充显示区域，当格子有剩余空间时，填充空间
//        gbc.anchor = GridBagConstraints.CENTER; // 对齐方式

        jpanel_config_.add(Add_Component(gbl,reload_config_button,gbc,1,5,1,1,0,0));
        jpanel_config_.add(Add_Component(gbl,save_config_button,gbc,3,5,1,1,0,0));




        //漏洞poc




//        jpanel_config_.add(Add_Component(gbl,app_log_label,gbc,0,6,1,5,1,0));
//
//
//        jpanel_config_.add(Add_Component(gbl,app_log_scrollPane,gbc,0,7,1,5,1,1));


        JPanel setting_split_11 = new JPanel();
        setting_split_11.setLayout(new BoxLayout(setting_split_11, BoxLayout.Y_AXIS));
        setting_split_11.add(app_log_scrollPane);

        splitPanes.setBorder(null);
        splitPanes.setLeftComponent(jp);//上面
        splitPanes.setRightComponent(splitPane_request_response);//下面
        //得到首选大小


        JPanel tab_index = new JPanel();
        GridBagLayout gbl_index=new GridBagLayout();//创建网格包布局管理器
        GridBagConstraints gbc_index=new GridBagConstraints();//GridBagConstraints对象来给出每个组件的大小和摆放位置
        tab_index.setLayout(gbl_index);//设置容器布局为网格包布局类型
        gbc_index.fill=GridBagConstraints.BOTH;//组件填充显示区域，当格子有剩余空间时，填充空间
        gbc_index.insets = new Insets(5, 5, 5, 5); // 边距
        gbc_index.anchor = GridBagConstraints.NORTHWEST; // 对齐方式
//        JPanel tab_index = new JPanel();
//        tab_index.setLayout(new GridLayout(1,1,5,5));
        Poc.reload_read_poc_Data();

        Finger.reload_read_finger_Data();

        try {
            load_config();
        } catch (IOException e) {
            printErr(Arrays.toString(e.getStackTrace()));
        }
        //漏洞poc
//        JPanel vuln_button_panel = new JPanel();
//        vuln_button_panel.setLayout(new BoxLayout(vuln_button_panel, BoxLayout.Y_AXIS));
//        JButton vuln_poc_button_add  = new JButton("ADD");


        item_poc_add.addActionListener(e -> {
            int last_id;
            // 获取最后一个元素
            if (model_poc.getRowCount()>0) {
                List<PocEntry>  all_value = model_poc.getAllValue();
                last_id = all_value.get(all_value.size() - 1).id+1;
            } else {
                last_id=1;
            }
            PocEntry null_Data = new PocEntry(last_id,"默认分组","","",1,"简单请求替换", "","");
            Poc.poc_edit(null_Data);
        });




//        JButton vuln_poc_button_edit  = new JButton("Edit");
        item_poc_edit.addActionListener(e -> {
            int[] id = vuln_poc_table.getSelectedRows();
            if (id.length>0){
                PocEntry temp = (PocEntry) model_poc.getValueRow(id[0]);
                Poc.poc_edit(temp);

            }else{
                JOptionPane.showMessageDialog(null, "请选择一条数据!", "警告", JOptionPane.WARNING_MESSAGE);
            }
        });
//        JButton vuln_poc_button_delete  = new JButton("Delete");

        item_poc_delete.addActionListener(e -> {
            int[] id = vuln_poc_table.getSelectedRows();
            if (id.length>0){
                PocEntry temp = (PocEntry) model_poc.getValueRow(id[0]);
                String message = Poc.delete_poc_data(temp);
                JOptionPane.showMessageDialog(null, message, "提示", JOptionPane.INFORMATION_MESSAGE);
                try {
                    Poc.reload_read_poc_Data();
                } catch (SQLException ex) {
                    printErr(String.valueOf(ex));
                }

            }else{
                JOptionPane.showMessageDialog(null, "请选择一条数据!", "警告", JOptionPane.WARNING_MESSAGE);
            }

        });
//        JButton vuln_poc_button_reload  = new JButton("Reload");
        item_poc_relaod.addActionListener(e -> {
            try {
                Poc.reload_read_poc_Data();
            } catch (SQLException ex) {
                printErr(String.valueOf(ex));
            }
        });
//        vuln_button_panel.add(vuln_poc_button_add);
//        vuln_button_panel.add(Box.createVerticalStrut(10) );
//        vuln_button_panel.add(vuln_poc_button_edit);
//        vuln_button_panel.add(Box.createVerticalStrut(10) ); //间隔
//        vuln_button_panel.add(vuln_poc_button_delete);
//        vuln_button_panel.add(Box.createVerticalStrut(10) ); //间隔
//        vuln_button_panel.add(vuln_poc_button_reload);



        vuln_poc_table = new JTable(model_poc);
        vuln_poc_table.setAutoCreateRowSorter(true);
        vuln_poc_table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        vuln_poc_table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) { // 如果是鼠标右键
                    popupMenu_poc.show(vuln_poc_table, e.getX(), e.getY()); // 显示右键菜单
                }
            }
        });


        JScrollPane poc_scrollPane = new JScrollPane(vuln_poc_table); //给列表添加滚动条
        poc_scrollPane.setBorder(BorderFactory.createEmptyBorder());
        poc_scrollPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) { // 如果是鼠标右键
                    popupMenu_poc.show(poc_scrollPane, e.getX(), e.getY()); // 显示右键菜单
                }
            }
        });



        JSplitPane splitPanes_vuln_poc = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
//        splitPanes_vuln_poc.setLeftComponent(vuln_button_panel);//上面
        splitPanes_vuln_poc.setRightComponent(poc_scrollPane);//下面
        splitPanes_vuln_poc.setEnabled(false);
        splitPanes_vuln_poc.setBorder(new EmptyBorder(10, 10, 10, 10)); // 设置外边距为10
        //指纹poc
//        JPanel finger_button_panel = new JPanel();
//        finger_button_panel.setLayout(new BoxLayout(finger_button_panel, BoxLayout.Y_AXIS));
//        JButton finger_poc_button_add  = new JButton("ADD");
        item_finger_add.addActionListener(e -> {
            int last_id;
            // 获取最后一个元素
            if (model_finger.getRowCount()>0) {
                List<FingerEntry>  all_value = model_finger.getAllValue();
                last_id = all_value.get(all_value.size() - 1).id+1;
            } else {
                last_id=1;
            }
            FingerEntry null_Data = new FingerEntry(last_id,"","keyword","body","",0,"");
            Finger.finger_edit(null_Data);
        });
//        JButton finger_poc_button_edit  = new JButton("Edit");

        item_finger_edit.addActionListener(e -> {
            int[] id = finger_poc_table.getSelectedRows();
            if (id.length>0){
                FingerEntry temp = (FingerEntry) model_finger.getValueRow(id[0]);
                Finger.finger_edit(temp);

            }else{
                JOptionPane.showMessageDialog(null, "请选择一条数据!", "警告", JOptionPane.WARNING_MESSAGE);
            }
        });
//        JButton finger_poc_button_delete  = new JButton("Delete");

        item_finger_delete.addActionListener(e -> {
            int[] id = finger_poc_table.getSelectedRows();
            if (id.length>0){
                FingerEntry temp = (FingerEntry) model_finger.getValueRow(id[0]);
                String message = Finger.delete_finger_data(temp);
                JOptionPane.showMessageDialog(null, message, "提示", JOptionPane.INFORMATION_MESSAGE);
                try {
                    Finger.reload_read_finger_Data();
                } catch (SQLException ex) {
                    printDebug(String.valueOf(ex));
                }

            }else{
                JOptionPane.showMessageDialog(null, "请选择一条数据!", "警告", JOptionPane.WARNING_MESSAGE);
            }

        });
//        JButton finger_poc_button_reload  = new JButton("Reload");
        item_finger_relaod.addActionListener(e -> {
            try {
                Finger.reload_read_finger_Data();
            } catch (SQLException ex) {
                printErr(String.valueOf(ex));
            }
        });
//        finger_button_panel.add(finger_poc_button_add);
//        finger_button_panel.add(Box.createVerticalStrut(10) );
//        finger_button_panel.add(finger_poc_button_edit);
//        finger_button_panel.add(Box.createVerticalStrut(10) ); //间隔
//        finger_button_panel.add(finger_poc_button_delete);
//        finger_button_panel.add(Box.createVerticalStrut(10) ); //间隔
//        finger_button_panel.add(finger_poc_button_reload);

        finger_poc_table = new JTable(model_finger);
        finger_poc_table.setAutoCreateRowSorter(true);
        finger_poc_table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        JScrollPane finger_scrollPane = new JScrollPane(finger_poc_table); //给列表添加滚动条
        finger_scrollPane.setBorder(BorderFactory.createEmptyBorder());
        finger_poc_table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) { // 如果是鼠标右键
                    popupMenu_finger.show(finger_poc_table, e.getX(), e.getY()); // 显示右键菜单
                }
            }
        });

        finger_scrollPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) { // 如果是鼠标右键
                    popupMenu_finger.show(finger_scrollPane, e.getX(), e.getY()); // 显示右键菜单
                }
            }
        });


//        JSplitPane splitPanes_finger_poc = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
//        splitPanes_finger_poc.setLeftComponent(finger_button_panel);//上面
//        splitPanes_finger_poc.setRightComponent(finger_scrollPane);//下面
//        splitPanes_finger_poc.setEnabled(false);
//        splitPanes_finger_poc.setBorder(new EmptyBorder(10, 10, 10, 10)); // 设置外边距为10


        //插件设置
        JPanel setting = new JPanel();
//        setting.setMaximumSize();
        setting.setLayout(new BoxLayout(setting, BoxLayout.X_AXIS));
        setting.add(setting_split_11);
        setting.add(jpanel_config_);
        if(!is_burp){
            JLabel  label_index_scan_url =new JLabel("目标地址:");
            JTextField jtext_index_scan_url  = new JTextField();
            jtext_index_scan_url.setToolTipText("请输入URL,例如：http://baidu.com");
            JButton button_index_scan_file = new JButton("导入地址");
            button_index_scan_file.addActionListener(e -> {
                JFileChooser chooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                        "TXT files",  "txt");
                chooser.setMultiSelectionEnabled(false);//允许文件多选
                chooser.setFileFilter(filter);
                int returnVal = chooser.showOpenDialog(chooser);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    String filePathname=chooser.getSelectedFile().getPath();
                    jtext_index_scan_url.setText(filePathname);
                    all_scan_url  =  load_all_all(filePathname);
                    String alert_message = "加载"+all_scan_url.size()+"条URL地址";
                    printMsg(alert_message);
                    if(all_scan_url.isEmpty()){
                        JOptionPane.showMessageDialog(null, "未获取到URL地址", "提示", JOptionPane.INFORMATION_MESSAGE);
                    }else{
                        JOptionPane.showMessageDialog(null, alert_message, "提示", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            });
            JButton button_index_scan_start = new JButton("开始扫描");
            button_index_scan_start.addActionListener(e -> {
                if(jtext_index_scan_url.getText().startsWith("http://")||jtext_index_scan_url.getText().startsWith("https://")){
                    all_scan_url.clear();
                    all_scan_url.add(jtext_index_scan_url.getText());
                }
                if(all_scan_url.isEmpty()){
                    JOptionPane.showMessageDialog(null, "未获取到URL地址", "提示", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                myappscan();
            });
//            JButton button_index_scanclear_huancun = new JButton("清空缓存");
//            button_index_scanclear_huancun.setEnabled(true);
//            button_index_scanclear_huancun.addActionListener(e -> logList.clear());
            //        gridx 和 gridy：指定组件放置在网格中的起始位置。
            //        gridwidth 和 gridheight：指定组件跨越的网格单元数量。
            //        fill：指定组件如何填充其网格单元。
            //        weightx 和 weighty：指定组件在可伸缩空间中的权重
            //第一行
            tab_index.add(Add_Component(gbl_index,label_index_scan_url,gbc_index,0,0,1,1,0,0));
            tab_index.add(Add_Component(gbl_index,jtext_index_scan_url,gbc_index,1,0,1,1,1,0));
            tab_index.add(Add_Component(gbl_index,button_index_scan_file,gbc_index,2,0,1,1,0,0));
            tab_index.add(Add_Component(gbl_index,button_index_scan_start,gbc_index,3,0,1,1,0,0));
//            tab_index.add(Add_Component(gbl_index,button_index_scanclear_huancun,gbc_index,4,0,1,1,0,0));
            //白名单
            tab_index.add(Add_Component(gbl_index,splitPanes,gbc_index,0,1,1,4,1,1));
        }else{
            tab_index.add(Add_Component(gbl_index,splitPanes,gbc_index,0,0,1,1,1,1));
        }
        tab.addTab("漏洞探测",tab_index);
        tab.addTab("漏洞POC",poc_scrollPane);
        tab.addTab("指纹POC",finger_scrollPane);
        tab.addTab("插件设置",setting);
    }
    public static IHttpRequestResponse createHttpRequestResponse(String url_str) throws Exception {
        // 创建 URL 对象
        URL targetUrl = new URL(url_str);
        // 构建请求头
        List<String> headers =  new ArrayList<>();
        String url_path;
        if(Objects.equals(targetUrl.getPath(), "") || targetUrl.getPath().isEmpty()){
            url_path = "/";
        }else{
            url_path = targetUrl.getPath();
        }
        headers.add("GET " + url_path + " HTTP/1.1");
        headers.add("Host: " + targetUrl.getHost());
        headers.add("User-Agent: "+Get_Random_UserAgent());
        headers.add("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8");
        headers.add("Accept-Language: en-US,en;q=0.5");
        headers.add("Connection: close");
        // 构建请求消息
        return GetHttpRequestResponse(url_str,headers,"");
    }

    public static void load_config() throws IOException {
        try{
            if(Global_Config.get("Grpc_Host") !=null && !((String) Global_Config.get("Grpc_Host")).isEmpty()){
                grpc_host = (String) Global_Config.get("Grpc_Host");
            }
            if(Global_Config.get("Grpc_Port")!=null && (int) Global_Config.get("Grpc_Port")!=0 ){
                grpc_port =(int) Global_Config.get("Grpc_Port");
            }
            channel = ManagedChannelBuilder.forAddress(grpc_host, grpc_port).usePlaintext().build();
            client = pocGrpc.newBlockingStub(channel);

        } catch (Exception e) {
            printErr(Arrays.toString(e.getStackTrace()));
            printErr(e.getMessage());
        }

        try{
            grpc_server_thread = new Thread(() -> {
                try {
                    // 获取路径对象
                    Path path = Paths.get(Config_PATH);
                    String tool_path = String.valueOf(path.getParent());
                    // 创建 BufferedWriter 以写入文件
                    String osName = System.getProperty("os.name").toLowerCase();
                    String architecture = System.getProperty("os.arch");
                    boolean isOpen = isPortOpen(grpc_host, grpc_port, 20);
                    if (isOpen){
                        printDebug(grpc_host+":"+grpc_port+" already Open");
                        return;
                    }
                    String platform = "" ;
                    if(Main_Vuln.Global_Config.get("Platform")!=null){
                        platform = (String)  Main_Vuln.Global_Config.get("Platform");
                    }

                    String rpc_server;
                    if (platform.contains("windows") || osName.contains("windows")) {
                        rpc_server = tool_path+"\\rpc_server_windows_x64.exe "+grpc_host+":"+grpc_port;
                    } else if (platform.contains("linux") || platform.contains("mac os x") || osName.contains("linux") || osName.contains("mac os x")) {
                        if (platform.contains("amd64")||platform.contains("x86_64") || architecture.equals("amd64") || architecture.equals("x86_64")) {
                            rpc_server = tool_path+"/rpc_server_linux_x64 "+grpc_host+":"+grpc_port;
                        } else if (platform.contains("arm")||platform.contains("aarch64")|| architecture.startsWith("arm") || architecture.equals("aarch64")) {
                            rpc_server = tool_path+"/rpc_server_linux_arm64 "+grpc_host+":"+grpc_port;
                        } else {
                            printErr("请手动开启GRPC Server");
                            return;
                        }
                    } else {
                        printErr("请手动开启GRPC Server");
                        return;
                    }
                    ProcessBuilder pb = new ProcessBuilder(rpc_server.split(" "));
                    pb.redirectErrorStream(true); // 将错误输出重定向到标准输出
                    try {
                        Process process = pb.start();
                        // 创建 PrintWriter 以写入文件
                        Path outputFile = Paths.get(tool_path+"\\output.txt");
                        try (OutputStream outputStream = new FileOutputStream(outputFile.toFile(), true); // 追加模式
                             InputStream inputStream = process.getInputStream()) {
                            // 读取每一行输出
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                if (grpc_server_thread_stop_flag) {
                                    break;
                                }
                                outputStream.write(buffer, 0, bytesRead);
                            }

                        } catch (IOException e) {
                            printErr(e.getMessage());
                            printErr(Arrays.toString(e.getStackTrace()));
                        }
                    } catch (IOException e) {
                        printErr(e.getMessage());
                    }
                    if(platform.isEmpty()){
                        Global_Config.put("Platform",osName+":"+architecture);
                        Yaml yaml = new Yaml(options);
                        String yamlString = yaml.dump(Global_Config);
                        try (Writer out = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(Paths.get(Config_PATH)), StandardCharsets.UTF_8))) {
                            out.write(yamlString);
                        } catch (IOException e) {
                            printErr(Arrays.toString(e.getStackTrace()));
                        }
                    }
                } catch (Exception ex) {
                    printErr(ex.getMessage());
                    printErr(Arrays.toString(ex.getStackTrace()));
                }
            });
            grpc_server_thread.start();
            printMsg("Start GRPC Server:"+ grpc_host+":"+grpc_port);
        } catch (Exception e) {
            printErr("开启GRPC Server失败！"+e.getMessage());
        }
        printMsg("Set GRPC:"+ grpc_host+":"+grpc_port);
        //配置文件路径
        white_URL = (String) Global_Config.get("WhiteList");
        textField_white.setText( (String)Global_Config.get("WhiteList"));
        chkbox_poc.setSelected((Boolean) Global_Config.get("Enable_Poc"));
        chkbox_is_debug.setSelected((Boolean) Global_Config.get("Is_Debug"));
//        is_debug = (boolean) Global_Config.get("Is_Debug");
        chkbox_finger.setSelected((Boolean) Global_Config.get("Enable_Finger"));
        switchs_poc= (boolean) Global_Config.get("Enable_Poc");
        switchs_finger= (boolean) Global_Config.get("Enable_Finger");
        chkbox_proxy.setSelected((Boolean) Global_Config.get("Is_Proxy"));
        clicks_Proxy = (boolean) Global_Config.get("Is_Proxy");
        chkbox_repeater.setSelected((Boolean) Global_Config.get("Is_Repeater"));
        clicks_Repeater = (boolean) Global_Config.get("Is_Repeater");

        if((Boolean) Global_Config.get("WhiteEnable")){
            btn3_white.setText("当前白名单状态:启用");
            white_switchs = 1;
            textField_white .setEditable(true);
            textField_white .setForeground(Color.BLACK);//设置组件的背景色
        }else {
            btn3_white.setText("当前白名单状态:关闭");
            white_switchs = 0;
            textField_white.setEditable(false);
            textField_white.setForeground(Color.GRAY);
        }
        black_URL = (String)Global_Config.get("BlackList");
        textField_black.setText( (String)Global_Config.get("BlackList"));
        String[] items = Poc.get_all_poc_group();
        vuln_poc_combox.removeAllItems();
        vuln_disenable_finger_poc_combox.removeAllItems();
        for(String i :items){
            vuln_poc_combox.addItem(i);
            vuln_disenable_finger_poc_combox.addItem(i);
        }
        enable_poc_list.clear();
        enable_poc_list =  Poc.Get_Poc(enable_poc_str);
        jump_finger_poc_list.clear();
        jump_finger_poc_list =  Poc.Get_Poc(jump_finger_poc_str);
        printMsg("Config Load Success！");

    }

    /**
     * 检查指定端口是否开放。
     *
     * @param host 主机名或 IP 地址
     * @param port 端口号
     * @param timeout 连接尝试的超时时间（毫秒）
     * @return 如果端口开放返回 true，否则返回 false
     */
    public static boolean isPortOpen(String host, int port, long timeout) {
        try (Socket socket = new Socket()) {
            SocketAddress socketAddress = new InetSocketAddress(host, port);
            socket.connect(socketAddress, (int) timeout);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static List<JMenuItem>  returnMenuItems(IContextMenuInvocation invocation){
        List<JMenuItem>  menu_list= new ArrayList<>();
        //父级菜单
        IHttpRequestResponse[] responses = invocation.getSelectedMessages();
        JMenuItem jMenu_scan = new JMenuItem("Send to Scan");
        JMenuItem jMenu_poc = new JMenuItem("Send to PocScan");
        JMenuItem jMenu_finger = new JMenuItem("Send to FingerScan");
        jMenu_scan.addActionListener(e -> {
            if(Main_Vuln.switchs_poc) {
                finger_scan(1024,responses[0],true);
            }else {
                printDebug("插件关闭");
            }

        });
        jMenu_poc.addActionListener(e -> {
            if(Main_Vuln.switchs_poc) {
                poc_scan(1024,responses[0]);
            }else {
                printDebug("插件关闭");
            }

        });
        jMenu_finger.addActionListener(e -> {
            if(Main_Vuln.switchs_finger) {
                finger_scan(1024,responses[0],false);
            }else {
                printDebug("插件关闭");
            }
        });
        menu_list.add(jMenu_scan);
        menu_list.add(jMenu_poc);
        menu_list.add(jMenu_finger);
        return menu_list;
    }

    public static List<PocEntry> mergeAndRemoveDuplicates(List<PocEntry> list1, List<PocEntry> list2) {
        return Stream.concat(list1.stream(), list2.stream())
                .distinct()
                .collect(Collectors.toList());
    }
    public static List<String> load_all_all(String file_path){
        List<String>  all_url  = new ArrayList<>();
        Path filePath = Paths.get(file_path); // 替换为你的文件路径
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("http://") || line.startsWith("https://")) {
                    all_url.add(line);
                }
            }
        } catch (IOException e) {
            printErr(e.getMessage());
            printErr(Arrays.toString(e.getStackTrace()));
        }
        return all_url;
    }
}
