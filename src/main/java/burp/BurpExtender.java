package burp;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


public class BurpExtender  implements IBurpExtender, ITab, IHttpListener,IScannerCheck, IMessageEditorController,IContextMenuFactory
{
    public static IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;
    private JSplitPane splitPane;
    private IMessageEditor requestViewer;
    private IMessageEditor responseViewer;
    private  JTextArea out_log; //日志输出框
    private  JFrame  mainFrame; //子窗口
    public JTable vuln_poc_table; //表格poc
    public static final List<LogEntry> table_log_data = new ArrayList<>();//用于展现结果
    public static final List<PocEntry> all_poc_data = new ArrayList<>();//用于展现poc
    private IHttpRequestResponse currentlyDisplayedItem;

    private final LogTableModel model = new LogTableModel();
    private final PocTableModel model_poc = new PocTableModel();



    public PrintWriter stdout;
    boolean switchs = true; //开关
    boolean clicks_Repeater=true;//是否启用repeater
    boolean clicks_Proxy=true;//是否启用proxy
    int count = 0; //记录条数
    JTable logTable; //第一个表格框
    String white_URL = ""; //白名单地址

    String black_URL = "";
    int white_switchs = 0;//白名单开关

    List<Object> logList = new ArrayList<>();

    JComboBox<Integer> comboBox_cengji = new JComboBox<>(); //层级
    JButton btn3_white = new JButton(); // 是否启用白名单
    JTextField textField_white = new JTextField("");//白名单文本框

    JTextField textField_black = new JTextField("");//黑名单文本框

    String codeStatus = "200,301,302,405";
    JTextField statusCodetextField = new JTextField(codeStatus);//白名单文本框

    String Tools_Name ="FrameScan";
    String CONFIGVERSION = "v2.0";
    //
    // implement IBurpExtender
    //

    @Override
    public void registerExtenderCallbacks(final IBurpExtenderCallbacks callbacks)
    {
        //输出
        this.stdout = new PrintWriter(callbacks.getStdout(), true);
        this.stdout.println("Loading..");

        // keep a reference to our callbacks object
        BurpExtender.callbacks = callbacks;

        // obtain an extension helpers object
        helpers = callbacks.getHelpers();

        // set our extension name
        callbacks.setExtensionName(Tools_Name+" "+CONFIGVERSION);

        // create our UI
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
//                textField.setMaximumSize(new Dimension(300, 30));
//                comboBox_cengji.setMaximumSize(new Dimension(300, 30));

                // main split pane
                splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
                JSplitPane splitPanes = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
                JPanel jp=new JPanel();
                // table of log entries
                logTable = new JTable(model);
                logTable.setAutoCreateRowSorter(true);
                logTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
//                logTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                logTable.getColumnModel().getColumn(0).setMaxWidth(100);
                logTable.getColumnModel().getColumn(1).setMaxWidth(100);
                logTable.getColumnModel().getColumn(3).setMaxWidth(100);
                logTable.getColumnModel().getColumn(4).setMaxWidth(100);
//                logTable.getColumnModel().getColumn(5).setPreferredWidth(300);
                logTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        int row = logTable.getSelectedRow();
                        int id = (int)logTable.getValueAt(row,0);
                        LogEntry logEntry = null;
                        for(LogEntry i : table_log_data){
                            if(i.id==id) {
                                logEntry = i;
                                break;
                            }
                        }
                        if(logEntry!=null)
                        {
                            requestViewer.setMessage(logEntry.requestResponse.getRequest(), true);
                            responseViewer.setMessage(logEntry.requestResponse.getResponse(), false);
                            currentlyDisplayedItem = logEntry.requestResponse;

                        }
                    }
                  });
                JScrollPane scrollPane = new JScrollPane(logTable); //给列表添加滚动条

                jp.add(scrollPane);    //将表格加到面板
                jp.setLayout(new GridLayout(1,1)); // 设置布局为1行1列

                //侧边复选框
                JPanel config_jpanel=new JPanel();


                config_jpanel.setLayout(new BoxLayout(config_jpanel, BoxLayout.Y_AXIS));
//                config_jpanel.setLayout(new GridLayout(6, 1,1,1)); //六行一列
                JCheckBox chkbox1=new JCheckBox("启动插件", true);    //创建指定文本和状态的复选框
                JCheckBox chkbox_repeater=new JCheckBox("监控Repeater");    //创建指定文本的复选框
                JCheckBox chkbox_proxy=new JCheckBox("监控Proxy");    //创建指定文本的复选框 //
                JPanel jpStart = new JPanel();
                JButton btn1=new JButton("清空表格");    //创建JButton对象
                JButton btn_clear_log=new JButton("清空日志");    //创建JButton对象
                jpStart.setLayout(new FlowLayout(FlowLayout.LEFT));
                jpStart.add(chkbox1);
                jpStart.add(chkbox_repeater);
                jpStart.add(chkbox_proxy);
                jpStart.add(btn1);
                jpStart.add(btn_clear_log);
                JLabel cengjiJLabel = new JLabel("目录层级:");
                JLabel cengjiJLabel_dest = new JLabel("0 表示所有目录, 1 表示根目录,2 表示一级目录 以此类推");

                for (int x=0;x<5;x++)
                {
                    comboBox_cengji.addItem(x);
                }
//                comboBox_cengji.setMaximumSize(new Dimension(100, 30));
                JPanel cengji_jpanel=new JPanel();
                cengji_jpanel.setLayout(new BoxLayout(cengji_jpanel, BoxLayout.X_AXIS));
                cengji_jpanel.add(cengjiJLabel);
                cengji_jpanel.add(Box.createHorizontalStrut(10) );

                cengji_jpanel.add(comboBox_cengji);
                cengji_jpanel.add(Box.createHorizontalStrut(10) );

                cengji_jpanel.add(cengjiJLabel_dest);
                cengji_jpanel.add(Box.createHorizontalStrut(10) );




                chkbox_proxy.setSelected(clicks_Proxy);
                chkbox_repeater.setSelected(clicks_Repeater);
                JLabel label_domain_white = new JLabel("白名单:");

                btn3_white=new JButton("当前白名单状态:关闭");    //处理白名单
                white_URL = textField_white.getText();
                white_switchs = 1;
                textField_white .setEditable(false);
                textField_white .setForeground(Color.GRAY);//设置组件的背景色
                textField_white.setToolTipText("多个域名加白请用,隔开");
//                textField_white.setMaximumSize(new Dimension(400, 30));

                JPanel white_jpanel=new JPanel();
                white_jpanel.setLayout(new BoxLayout(white_jpanel, BoxLayout.X_AXIS));
                white_jpanel.add(label_domain_white);
                white_jpanel.add(Box.createHorizontalStrut(10) );

                white_jpanel.add(textField_white);
                white_jpanel.add(Box.createHorizontalStrut(10) );
                white_jpanel.add(btn3_white);


                JLabel label_domain_black = new JLabel("黑名单:");
//                white_URL = textField_black.getText();
                textField_black .setForeground(Color.GRAY);//设置组件的背景色
                textField_black.setToolTipText("多个域名加白请用,隔开");
                JButton btn3_black  = new JButton("确定");    //处理白名单
                btn3_black.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        black_URL = textField_black.getText();
                    }
                });



                JPanel black_jpanel=new JPanel();
                black_jpanel.setLayout(new BoxLayout(black_jpanel, BoxLayout.X_AXIS));
                black_jpanel.add(label_domain_black);
                black_jpanel.add(Box.createHorizontalStrut(10) );
                black_jpanel.add(textField_black);
                black_jpanel.add(Box.createHorizontalStrut(10) );
                black_jpanel.add(btn3_black);


                reload_read_poc_Data();
                try {
                    load_config();
                } catch (IOException e) {
                    stdout.println(e.getStackTrace());
//                    throw new RuntimeException(e);
                }
                //添加复选框监听事件
                chkbox1.addItemListener(new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        if(chkbox1.isSelected()){
                            stdout.println("插件启动");
                            switchs = true;
                        }else {
                            stdout.println("插件关闭");
                            switchs = false;
                        }

                    }
                });
                chkbox_repeater.addItemListener(new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        if (chkbox_repeater.isSelected()){
                            stdout.println("启动 监控Repeater");
                            chkbox_repeater.setSelected(true);
                            clicks_Repeater  = true;
                        }else {
                            stdout.println("关闭 监控Repeater");
                            chkbox_repeater.setSelected(false);
                            clicks_Repeater  = false;
                        }
                    }
                });
                chkbox_proxy.addItemListener(new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        if(chkbox_proxy.isSelected()) {
                            stdout.println("启动 监控Proxy");
                            chkbox_proxy.setSelected(true);
                            clicks_Proxy  = true;
                        }else {
                            stdout.println("关闭 监控Proxy");
                            chkbox_proxy.setSelected(false);
                            clicks_Proxy  = false;
                        }
                    }
                });
                btn1.addActionListener(new ActionListener() {//清空列表
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        table_log_data.clear();
                        model.fireTableDataChanged();
                    }
                });

                btn_clear_log.addActionListener(new ActionListener() {//清空列表
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        out_log.setText(""); // 清楚logList的内容
                    }
                });
                btn3_white.addActionListener(new ActionListener() {//加载自定义payload
                    @Override
                    public void actionPerformed(ActionEvent e) {
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
                    }
                });


                JPanel jnStatusCode = new JPanel();

                JLabel tempJLabel = new JLabel("仅显示状态码:");
                jnStatusCode.setLayout(new BoxLayout(jnStatusCode, BoxLayout.X_AXIS));


//                jnStatusCode.setLayout(new FlowLayout(FlowLayout.LEFT));
                jnStatusCode.add(tempJLabel);
                jnStatusCode.add(Box.createHorizontalStrut(10) );

//                statusCodetextField.setMaximumSize(new Dimension (400,30));
                jnStatusCode.add(statusCodetextField);
                jnStatusCode.add(Box.createHorizontalStrut(10) );
                JButton jtb = new JButton("确定");
                jtb.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // TODO Auto-generated method stub
                        codeStatus = statusCodetextField.getText().trim();
                    }
                });


                JPanel config_button_jpanel=new JPanel();
                config_button_jpanel.setLayout(new BoxLayout(config_button_jpanel, BoxLayout.X_AXIS));
                JButton reload_config_button = new JButton("重载配置");
                JButton save_config_button = new JButton("保存配置");
                reload_config_button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            load_config();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                });
                save_config_button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        save_config();
                    }
                });
                config_button_jpanel.add(reload_config_button);

                config_button_jpanel.add(Box.createHorizontalStrut(10) );

                config_button_jpanel.add(save_config_button);



                jnStatusCode.add(jtb);
                config_jpanel.add(cengji_jpanel);

                config_jpanel.add(Box.createVerticalStrut(10));
                config_jpanel.add(white_jpanel);
                config_jpanel.add(Box.createVerticalStrut(10));
                config_jpanel.add(black_jpanel);
                config_jpanel.add(Box.createVerticalStrut(10));
                config_jpanel.add(jnStatusCode);
                config_jpanel.add(Box.createVerticalStrut(10));
                config_jpanel.add(config_button_jpanel);

                JTabbedPane tabs = new JTabbedPane();
                requestViewer = callbacks.createMessageEditor(BurpExtender.this, false);
                responseViewer = callbacks.createMessageEditor(BurpExtender.this, false);
                tabs.addTab("Request", requestViewer.getComponent());
                tabs.addTab("Response", responseViewer.getComponent());

                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

                panel.add(jpStart);
                panel.add(new JLabel("日志"));

                out_log = new JTextArea(60,1);
                out_log.setLineWrap(true);
                JScrollPane out_log_=new JScrollPane(out_log);    //将文本域放入滚动窗口
                out_log_.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                panel.add(out_log_);



                //左边
                splitPanes.setLeftComponent(jp);//上面
                splitPanes.setRightComponent(tabs);//下面

                JSplitPane splitPane_index_ = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
                splitPane_index_.setLeftComponent(splitPanes);//添加在左面
                splitPane_index_.setRightComponent(panel);//添加在右面
                splitPane_index_.setDividerLocation(1100);//设置分割的大小

                JPanel tab_index = new JPanel();
                tab_index.setLayout(new GridLayout(1,1,5,5));
                tab_index.add(splitPane_index_);

                //漏洞poc
                JPanel vuln_button_panel = new JPanel();
                vuln_button_panel.setLayout(new BoxLayout(vuln_button_panel, BoxLayout.Y_AXIS));
                JButton vuln_poc_button_add  = new JButton("ADD");


                vuln_poc_button_add.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int last_id;
                        // 获取最后一个元素
                        if (!all_poc_data.isEmpty()) {
                            last_id = all_poc_data.get(all_poc_data.size() - 1).id+1;
                        } else {
                            last_id=1;
                        }
//                        JOptionPane.showMessageDialog(null, last_id, "提示", JOptionPane.INFORMATION_MESSAGE);
                        reload_read_poc_Data(); //可有可无??
                        PocEntry null_Data =  new PocEntry(last_id,"","/","response","");
                        poc_edit(null_Data);
                    }
                });


                JButton vuln_poc_button_edit  = new JButton("Edit");

                vuln_poc_button_edit.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int[] id = vuln_poc_table.getSelectedRows();
                        if (id.length>0){
                            PocEntry temp = (PocEntry) model_poc.getValueRow(id[0]);
                            poc_edit(temp);

                        }else{
                            JOptionPane.showMessageDialog(null, "请选择一条数据!", "警告", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                });
                JButton vuln_poc_button_delete  = new JButton("Delete");

                vuln_poc_button_delete.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int[] id = vuln_poc_table.getSelectedRows();
                        if (id.length>0){
                            PocEntry temp = (PocEntry) model_poc.getValueRow(id[0]);
                            String message = delete_poc_data(temp);
                            JOptionPane.showMessageDialog(null, message, "提示", JOptionPane.INFORMATION_MESSAGE);
                            reload_read_poc_Data();

                        }else{
                            JOptionPane.showMessageDialog(null, "请选择一条数据!", "警告", JOptionPane.WARNING_MESSAGE);
                        }

                    }
                });
                JButton vuln_poc_button_reload  = new JButton("Reload");
                vuln_poc_button_reload.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        reload_read_poc_Data();
                    }
                });
                vuln_button_panel.add(vuln_poc_button_add);
                vuln_button_panel.add(Box.createVerticalStrut(10) );
                vuln_button_panel.add(vuln_poc_button_edit);
                vuln_button_panel.add(Box.createVerticalStrut(10) ); //间隔
                vuln_button_panel.add(vuln_poc_button_delete);
                vuln_button_panel.add(Box.createVerticalStrut(10) ); //间隔
                vuln_button_panel.add(vuln_poc_button_reload);

                vuln_poc_table = new JTable(model_poc);
                vuln_poc_table.setAutoCreateRowSorter(true);
                vuln_poc_table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
                JScrollPane poc_scrollPane = new JScrollPane(vuln_poc_table); //给列表添加滚动条

                JSplitPane splitPanes_vuln_poc = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
                splitPanes_vuln_poc.setLeftComponent(vuln_button_panel);//上面
                splitPanes_vuln_poc.setRightComponent(poc_scrollPane);//下面
                splitPanes_vuln_poc.setEnabled(false);
                splitPanes_vuln_poc.setBorder(new EmptyBorder(10, 10, 10, 10)); // 设置外边距为10

                JPanel config_tab =new JPanel();
                config_tab.setLayout(new FlowLayout(FlowLayout.CENTER));
                config_tab.add(config_jpanel);

                JTabbedPane tab = new JTabbedPane();
                tab.addTab("首页",tab_index);
                tab.addTab("POC",splitPanes_vuln_poc);
                tab.addTab("配置",config_tab);
                splitPane.setRightComponent(tab);




                // customize our UI components
                callbacks.customizeUiComponent(splitPane);
                callbacks.customizeUiComponent(vuln_poc_table);
                callbacks.customizeUiComponent(logTable);
                callbacks.customizeUiComponent(scrollPane);
                callbacks.customizeUiComponent(config_jpanel);
                callbacks.customizeUiComponent(jp);
                callbacks.customizeUiComponent(tabs);

                // add the custom tab to Burp's UI
                callbacks.addSuiteTab(BurpExtender.this);

                // register ourselves as an HTTP listener
                callbacks.registerHttpListener(BurpExtender.this);
                callbacks.registerScannerCheck(BurpExtender.this);
                callbacks.registerContextMenuFactory(BurpExtender.this);


            }

        });
        this.stdout.println("Success..");
        this.stdout.println(Tools_Name+" "+CONFIGVERSION+" by qianxiao996!\nGithub:https://github.com/qianxiao996");
    }

    private void save_config() {
//        out_log.append("配置保存...");
        String BurpConfigPath = callbacks.getExtensionFilename().substring(0, (callbacks.getExtensionFilename().lastIndexOf(File.separator))) + File.separator;
        String ConfigPath =  String.format("%s/%s", BurpConfigPath, "config.yml");
        Map<String, Object> data = new HashMap<>();
        data.put("DirCengji", (Integer)Objects.requireNonNull(comboBox_cengji.getSelectedItem())) ;

        if(Objects.equals(btn3_white.getText(), "当前白名单状态:启用")){
            data.put("WhiteEnable", true) ;
        }else{
            data.put("WhiteEnable",false) ;
        }
        data.put("WhiteList", textField_white.getText()) ;
        data.put("BlackList", textField_black.getText()) ;
        data.put("ShowStatusCode", (String) statusCodetextField.getText()) ;

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);//通常使用的yaml格式
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);//标量格式

        Yaml yaml = new Yaml(options);
        String yamlString = yaml.dump(data);
        //写入ini配置文件
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(ConfigPath));
            out.write(yamlString);
            out.close();
        } catch (IOException exception) {
            stdout.println(Arrays.toString(exception.getStackTrace()));
        }
//        out_log.append("配置保存成功！");
        JOptionPane.showMessageDialog(null, "配置保存成功！", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    private void load_config() throws IOException {
        String BurpConfigPath = callbacks.getExtensionFilename().substring(0, (callbacks.getExtensionFilename().lastIndexOf(File.separator))) + File.separator;
        //配置文件路径
        String ConfigPath =  String.format("%s/%s", BurpConfigPath, "config.yml");
        BufferedReader in = new BufferedReader(new FileReader(ConfigPath));
        String str,str_data="";
        while ((str = in.readLine()) != null) {
            str_data += str+"\n";
        }
        Yaml yaml = new Yaml();
        Map<String, Object> map = yaml.load(str_data);
        comboBox_cengji.setSelectedItem((Integer)map.get("DirCengji"));
//        stdout.println(map.get("WhiteEnable"));

        white_URL = (String) map.get("WhiteList");
        textField_white.setText( (String)map.get("WhiteList"));


        if((Boolean) map.get("WhiteEnable")){
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
        black_URL = (String)map.get("BlackList");
        textField_black.setText( (String)map.get("BlackList"));
        statusCodetextField.setText( (String)map.get("ShowStatusCode"));
        stdout.println("Config Load Success！");
    }

    private String delete_poc_data(PocEntry temp) {
        try{
            Connection c;
            Class.forName("org.sqlite.JDBC");
            String BurpConfigPath = callbacks.getExtensionFilename().substring(0, (callbacks.getExtensionFilename().lastIndexOf(File.separator))) + File.separator;
    //                    //配置文件路径
            String ConfigPath =  String.format("%s/%s", BurpConfigPath, "data.db");
            c = DriverManager.getConnection("jdbc:sqlite:"+ConfigPath);
    //                    System.out.println("Opened database successfully");
            Statement statement = c.createStatement();
    //                    String sql = "CREATE TABLE COMPANY " + "(ID INT PRIMARY KEY     NOT NULL,"
    //                            + " NAME           TEXT    NOT NULL, " + " AGE            INT     NOT NULL, "
    //                            + " ADDRESS        CHAR(50), " + " SALARY         REAL)";
            String sql =  "delete  from data where id = "+temp.id;
            int deletedRows = statement.executeUpdate(sql);
            statement.close();
            c.close();
            if(deletedRows>0){
                return "success";
            }else{
                return "删除失败!";
            }
        } catch (SQLException | ClassNotFoundException e) {
            return Arrays.toString(e.getStackTrace());
        }
    }
    //
    // implement ITab
    //
    public void reload_read_poc_Data(){
        try {
            all_poc_data.clear();
            Connection c;
            Class.forName("org.sqlite.JDBC");
            String BurpConfigPath = callbacks.getExtensionFilename().substring(0, (callbacks.getExtensionFilename().lastIndexOf(File.separator))) + File.separator;
//                    //配置文件路径
            String ConfigPath =  String.format("%s/%s", BurpConfigPath, "data.db");
            c = DriverManager.getConnection("jdbc:sqlite:"+ConfigPath);

//                    System.out.println("Opened database successfully");
            Statement statement = c.createStatement();
//                    String sql = "CREATE TABLE COMPANY " + "(ID INT PRIMARY KEY     NOT NULL,"
//                            + " NAME           TEXT    NOT NULL, " + " AGE            INT     NOT NULL, "
//                            + " ADDRESS        CHAR(50), " + " SALARY         REAL)";
            String sql =  "select * from data";
            ResultSet rs = statement.executeQuery(sql);
            while(rs.next()){
                int id = Integer.parseInt(rs.getString(1));
                String name= rs.getString(2);
                String url= rs.getString(3);
                String scope= rs.getString(4);
                String regex= rs.getString(5);
                all_poc_data.add(new PocEntry(id,name,url,scope,regex));
            }
            model_poc.fireTableDataChanged();
            statement.close();
            c.close();
            stdout.println("POC Data Load Suceess！");
        } catch (SQLException | ClassNotFoundException e) {
            stdout.println(Arrays.toString(e.getStackTrace()));
//            out_log.append(Arrays.toString(e.getStackTrace()));
            throw new RuntimeException(e);
        }

    }
    public void poc_edit(PocEntry poc_data) {
        mainFrame = new JFrame("编辑POC");
        mainFrame.setLayout(new BorderLayout());

        JPanel jpanel_poc_name = new JPanel();
        jpanel_poc_name.setLayout(new BoxLayout(jpanel_poc_name, BoxLayout.X_AXIS));
        JLabel label_vuln_name = new JLabel("漏洞名称：");
        JTextField text_vuln_name = new JTextField(poc_data.name);
        jpanel_poc_name.add(label_vuln_name);
        jpanel_poc_name.add(text_vuln_name);
        jpanel_poc_name.setMaximumSize(new Dimension(300, 30));

        JPanel jpanel_poc_url = new JPanel();
        jpanel_poc_url.setLayout(new BoxLayout(jpanel_poc_url, BoxLayout.X_AXIS));
        JLabel label_vuln_url = new JLabel("URL路径：");
        JTextField text_vuln_url = new JTextField(poc_data.url);
        jpanel_poc_url.add(label_vuln_url);
        jpanel_poc_url.add(text_vuln_url);
        jpanel_poc_url.setMaximumSize(new Dimension(300, 30));


        JPanel jpanel_poc_scope = new JPanel();
        jpanel_poc_scope.setLayout(new BoxLayout(jpanel_poc_scope, BoxLayout.X_AXIS));
        JLabel label_vuln_scope = new JLabel("匹配位置：");

        String[] strList = { "response", "response header", "response body"};
        JComboBox<String> comBox_vuln_scope = new JComboBox<String>(strList);
//        comBox.setSelectedIndex(2); // 方法一：列表下标，从0开始
        comBox_vuln_scope.setSelectedItem(poc_data.scope); // 方法二：指定列表值

//        JTextField text_vuln_scope = new JTextField(poc_data.scope);
        jpanel_poc_scope.add(label_vuln_scope);
        jpanel_poc_scope.add(comBox_vuln_scope);
        jpanel_poc_scope.setMaximumSize(new Dimension(300, 30));



        JPanel jpanel_poc_reg = new JPanel();
        jpanel_poc_reg.setLayout(new BoxLayout(jpanel_poc_reg, BoxLayout.X_AXIS));
        JLabel label_vuln_reg = new JLabel("匹配正则：");
        JTextField text_vuln_reg = new JTextField(poc_data.regex);
        jpanel_poc_reg.add(label_vuln_reg);
        jpanel_poc_reg.add(text_vuln_reg);
        jpanel_poc_reg.setMaximumSize(new Dimension(300, 30));


        JPanel jpanel_poc_button = new JPanel();
        jpanel_poc_button.setLayout(new BoxLayout(jpanel_poc_button, BoxLayout.X_AXIS));
        JButton poc_button_go = new JButton("确定");
        JButton poc_button_cancal = new JButton("取消");
        jpanel_poc_button.add(poc_button_go);
        jpanel_poc_button.add(Box.createHorizontalStrut(10)); // 垂直间隔10像素
        jpanel_poc_button.add(poc_button_cancal);

        poc_button_go.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                poc_data.name = text_vuln_name.getText();
                poc_data.url = text_vuln_url.getText();
                poc_data.scope = comBox_vuln_scope.getSelectedItem().toString();
                poc_data.regex = text_vuln_reg.getText();
                String message = Save_Poc_Data(poc_data);
                JOptionPane.showMessageDialog(null, message, "提示", JOptionPane.INFORMATION_MESSAGE);
                mainFrame.setVisible(false);
                reload_read_poc_Data();
            }
        });


        poc_button_cancal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainFrame.setVisible(false);
            }
        });

        JPanel jpanel_poc_ = new JPanel();
        jpanel_poc_.setLayout(new BoxLayout(jpanel_poc_, BoxLayout.Y_AXIS));
        jpanel_poc_.add(jpanel_poc_name);
        jpanel_poc_.add(Box.createVerticalStrut(10)); // 水平间隔10像素
        jpanel_poc_.add(jpanel_poc_url);
        jpanel_poc_.add(Box.createVerticalStrut(10)); // 水平间隔10像素
        jpanel_poc_.add(jpanel_poc_scope);
        jpanel_poc_.add(Box.createVerticalStrut(10)); // 水平间隔10像素
        jpanel_poc_.add(jpanel_poc_reg);
        jpanel_poc_.add(Box.createVerticalStrut(10)); // 水平间隔10像素
        jpanel_poc_.add(jpanel_poc_button);
        jpanel_poc_.setBorder(new EmptyBorder(10, 10, 10, 10)); // 设置外边距为10
        mainFrame.getContentPane().add(jpanel_poc_);
        mainFrame.setSize(350, 220);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    private String Save_Poc_Data(PocEntry pocData) {
        String BurpConfigPath = callbacks.getExtensionFilename().substring(0, (callbacks.getExtensionFilename().lastIndexOf(File.separator))) + File.separator;
        String ConfigPath =  String.format("%s/%s", BurpConfigPath, "data.db");
        String url = "jdbc:sqlite:"+ConfigPath;
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement pstmt = connection.prepareStatement(
            "INSERT OR REPLACE INTO data (id,name,url,scope,regex) VALUES (?, ?, ?,?,?)")) {
            pstmt.setInt(1, pocData.id);
            pstmt.setString(2, pocData.name);
            pstmt.setString(3, pocData.url);
            pstmt.setString(4, pocData.scope);
            pstmt.setString(5, pocData.regex);
            // 执行更新或插入操作
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                return "success";

            } else {
                return "保存失败!";
            }

        } catch (SQLException e) {
            stdout.println(Arrays.toString(e.getStackTrace()));
//                e.printStackTrace();
            return Arrays.toString(e.getStackTrace());
        }
    }


    @Override
    public String getTabCaption()
    {
        return Tools_Name;
    }

    @Override
    public Component getUiComponent()
    {
        return splitPane;
    }

    //
    // implement IHttpListener
    //




    @Override
    public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo)
    {
        if(switchs){//插件开关
            if((toolFlag ==  callbacks.TOOL_PROXY && clicks_Proxy) || (toolFlag ==  callbacks.TOOL_REPEATER  && clicks_Repeater)){//监听Repeater
                // only process responses
                if (!messageIsRequest)
                {
                    // create a new log entry with the message details
                    synchronized(table_log_data)
                    {
                        //BurpExtender.this.checkVul(messageInfo,toolFlag);
                        Thread thread = new Thread(new Runnable() {
                            public void run() {
                                try {
                                    BurpExtender.this.checkVul(messageInfo,toolFlag);
                                } catch (Exception ex) {
//                                    ex.printStackTrace();
//                                    BurpExtender.this.stdout.println(ex);
                                    BurpExtender.this.stdout.println(Arrays.toString(ex.getStackTrace()));
                                }
                            }
                        });
                        thread.start();
                    }
                }
            }

        }

    }

    @Override
    public List<IScanIssue> doPassiveScan(IHttpRequestResponse baseRequestResponse) {
        return null;
    }

    @Override // 这个功能可以不需要
    public List<JMenuItem> createMenuItems(final IContextMenuInvocation invocation) {
        //右键发送按钮功能

        List<JMenuItem> listMenuItems = new ArrayList<JMenuItem>(1);
        if(invocation.getToolFlag() == IBurpExtenderCallbacks.TOOL_REPEATER || invocation.getToolFlag() == IBurpExtenderCallbacks.TOOL_PROXY){
            //父级菜单
            IHttpRequestResponse[] responses = invocation.getSelectedMessages();
            JMenuItem jMenu = new JMenuItem("Send to "+Tools_Name);

            jMenu.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(switchs) {
                        //不应在Swing事件调度线程中发出HTTP请求，所以需要创建一个Runnable并在 run() 方法中完成工作，后调用 new Thread(runnable).start() 来启动线程
                        Thread thread = new Thread(new Runnable() {
                            public void run() {
                                try {
                                    BurpExtender.this.checkVul(responses[0], 1024);
                                } catch (Exception ex) {
//                                    ex.printStackTrace();
//                                    BurpExtender.this.stdout.println(ex);
                                    BurpExtender.this.stdout.println(Arrays.toString(ex.getStackTrace()));
                                }
                            }
                        });
                        thread.start();
                    }else {
                        BurpExtender.this.stdout.println("插件关闭");
                    }

                }
            });

            listMenuItems.add(jMenu);


        }
        //BurpExtender.this.checkVul(responses,4);
        return listMenuItems;
    }

    private void checkVul(IHttpRequestResponse baseRequestResponse, int toolFlag) throws Exception {

        String temp_data = String.valueOf(helpers.analyzeRequest(baseRequestResponse).getUrl());//url
        URL url  = new URL(temp_data);
        String paths = url.getProtocol()+"://"+url.getAuthority();
        String path = url.getPath(); //获取的路径

        if(textField_white.isEditable()) { // 判断是否启用白名单
            String[] whiteList = textField_white .getText().split(",");
            boolean b = false; // 判断是否是白名单
            for(String wl:whiteList) {
                if(paths.contains(wl)) { // 如果在白名单里面
                    b = true;
                    break;
                }
            }
            if(!b) { // 如果不是白名单 那么退出
                out_log.append("【不在白名单内】path:"+paths+"\n");
                return;

            }

        }
        //判断黑名单
        if(!black_URL.isEmpty()){
            String[] black_data = black_URL.split(",");
            for (String wl : black_data) {
                if (paths.contains(wl)) { // 如果在黑名单里面
                    out_log.append("【匹配到黑名单】path:" + paths + " 域名:" + wl + "\n");
                    return;
                }
            }
        }

        if(path.endsWith("/")) { // 判断是否以/ 结尾
            path = path+"{ChangeTemps}";

        }

        List list = countString(path,"/"); // 获取路径中所有路径的组合
//        stdout.println("path:"+list);


        List<String> headers = helpers.analyzeRequest(baseRequestResponse).getHeaders();
        int conboBox = (Integer)comboBox_cengji.getSelectedItem();
        if(conboBox ==0) {
            conboBox = 100;
        }

        for(int urlLength=0;urlLength<conboBox&&urlLength<list.size();urlLength++) { // 匹配层级
            String logsPahtTemp = paths+ list.get(urlLength);// 获取路径
            if(logList.contains(logsPahtTemp)) { // 判断是否包含在list中
                System.out.println("在列表中");
                continue; // 继续执行 存在历史 则不进行处理
            }
            System.out.println("不在列表中");
            logList.add(logsPahtTemp);
            String method = helpers.analyzeRequest(baseRequestResponse).getMethod(); // 获取提交方法
            String headUrl = headers.get(0);
            // 替换所有的方法类型为GET类型
            if(method.toUpperCase().startsWith("POST")||method.toUpperCase().startsWith("HEAD")||method.toUpperCase().startsWith("DELETE")||method.toUpperCase().startsWith("OPTIONS")||method.toUpperCase().startsWith("TRACE")) {
                method  = method.replace(method.split(" ")[0], "GET");
                headers.set(0,method );
            }
//            System.out.println(this.getClass().getName()+""+headers.get(0));
            //遍历所有poc
            for (PocEntry s : all_poc_data) { //遍历字典
                String scan_result="";
                String urls =s.url;
                String[] e = headUrl.split(" ");
                e[1] = list.get(urlLength) + "" + urls;
                e[1] = e[1].replace("//", "/");
                headers.set(0, e[0] + " " + e[1] + " " + e[2]);

                byte[] bodyBytes = "".getBytes();
                byte[] new_Requests = helpers.buildHttpMessage(headers, bodyBytes); //关键方法
                IHttpService iHttpService = baseRequestResponse.getHttpService();
                IHttpRequestResponse requestResponse = callbacks.makeHttpRequest(iHttpService, new_Requests);//发送请求
                int aaa = out_log.getLineCount();
                if (aaa >= 50000) {
                    out_log.setText("");
                }
                out_log.append(helpers.analyzeRequest(requestResponse).getUrl() + " " + String.valueOf(helpers.analyzeResponse(requestResponse.getResponse()).getStatusCode()));
                out_log.append("\n");

//                stdout.println(helpers.analyzeRequest(requestResponse).getUrl()+" "+String.valueOf(helpers.analyzeResponse(requestResponse.getResponse()).getStatusCode()));

                String[] codeStatuss = codeStatus.split(",");

                for (String ss : codeStatuss) { //判断状态码是否一致
                    if (!(String.valueOf(helpers.analyzeResponse(requestResponse.getResponse()).getStatusCode()).equals(ss))) {
                        continue; // 如果状态码不一致 那么直接跳出当次循环
                    }
                    String regex_source_data;
                    if(Objects.equals(s.scope, "response")){
                        String response_data = new String(requestResponse.getResponse());
                        regex_source_data = response_data;

                    }else if (Objects.equals(s.scope, "response header")){
                        String response_headers = helpers.analyzeResponse(requestResponse.getResponse()).getHeaders().toString();
                        regex_source_data = response_headers;
                    }else if (Objects.equals(s.scope, "response body")){
                        String response_data = new String(requestResponse.getResponse());
                        IResponseInfo analyzeResponse = helpers.analyzeResponse(requestResponse.getResponse());
                        int bodyOffset = analyzeResponse.getBodyOffset();
                        String response_body = response_data.substring(bodyOffset);
                        regex_source_data = response_body;
                    }else{
                        regex_source_data="";
                    }
                    try{
                        if (!regex_source_data.isEmpty() && s.regex!=null &&  !s.regex.isEmpty()){
                            Pattern pattern = Pattern.compile(s.regex);
                            Matcher matcher = pattern.matcher(regex_source_data);
                            // 查找匹配项
                            if (matcher.find()) {
                                scan_result = s.name;
                                // 如果找到匹配项，输出匹配成功的信息
                            }
                        }

                    }catch (Exception ex){
                        stdout.println(Arrays.toString(ex.getStackTrace()));
                    }
                    if(!scan_result.isEmpty()){
                        out_log.append("【匹配成功】URL:"+helpers.analyzeRequest(requestResponse).getUrl()+"POC:"+s.name+"匹配条件:"+s.scope+"reg:"+s.regex+"\n");
                    }
//                    out_log.append(response_body);
//                    JOptionPane.showMessageDialog(null, scan_result, "提示", JOptionPane.INFORMATION_MESSAGE);
                    count++;
                    LogEntry result_Data = new LogEntry(count, toolFlag, callbacks.saveBuffersToTempFiles(requestResponse), helpers.analyzeRequest(requestResponse).getUrl(), "", "", "", 0, (int)(helpers.analyzeResponse(requestResponse.getResponse()).getStatusCode()), scan_result);
                    table_log_data.add(result_Data);
                    model.fireTableRowsInserted(table_log_data.size() - 1, table_log_data.size() - 1);
                }
            }
            logList.add(logsPahtTemp);
        }
    }
    /**
     * 统计某个字符串在指定字符串中出现的个数
     * @param str
     * @param s
     */
    public List countString(String str,String s) {
//        int count = 0,len = str.length();
        List<String> list = new ArrayList<>();
        String ss = str;
        while(str.contains(s)) {
            str = str.substring(str.indexOf(s) + 1,str.length());
            list.add(ss.replace(str, s).replace("//","/"));
            count++;
        }
        return list;
    }


    @Override
    public List<IScanIssue> doActiveScan(IHttpRequestResponse baseRequestResponse, IScannerInsertionPoint insertionPoint) {
        return null;
    }

    @Override
    public int consolidateDuplicateIssues(IScanIssue existingIssue, IScanIssue newIssue) {
        if (existingIssue.getIssueName().equals(newIssue.getIssueName()))
            return -1;
        else return 0;
    }
    //
    // extend AbstractTableModel
    //





    //
    // implement IMessageEditorController
    // this allows our request/response viewers to obtain details about the messages being displayed
    //

    @Override
    public byte[] getRequest()
    {
        return currentlyDisplayedItem.getRequest();
    }

    @Override
    public byte[] getResponse()
    {
        return currentlyDisplayedItem.getResponse();
    }

    @Override
    public IHttpService getHttpService()
    {
        return currentlyDisplayedItem.getHttpService();
    }





//    public static String MD5(String key) {
//        char hexDigits[] = {
//                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
//        };
//        try {
//            byte[] btInput = key.getBytes();
//            // 获得MD5摘要算法的 MessageDigest 对象
//            MessageDigest mdInst = MessageDigest.getInstance("MD5");
//            // 使用指定的字节更新摘要
//            mdInst.update(btInput);
//            // 获得密文
//            byte[] md = mdInst.digest();
//            // 把密文转换成十六进制的字符串形式
//            int j = md.length;
//            char str[] = new char[j * 2];
//            int k = 0;
//            for (int i = 0; i < j; i++) {
//                byte byte0 = md[i];
//                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
//                str[k++] = hexDigits[byte0 & 0xf];
//            }
//            return new String(str);
//        } catch (Exception e) {
//            return null;
//        }
//    }


}