package burp.utils;
import burp.Main_Vuln;
import burp.model.finger.FingerEntry;
import burp.model.finger.FingerTableModel;
import burp.model.group.GroupEntry;
import burp.model.group.GroupTableModel;
import burp.model.poc.PocEntry;
import burp.model.poc.PocTableModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static burp.Main_Vuln.*;
import static burp.model.poc.PocTableModel.all_poc_data;
import static burp.utils.Config.save_config;
import static burp.utils.Poc.Add_Component;

public class Plugins {
    private static String  lastDirectory;
    public static void plugins_edit(String type_) {

        mainFrame_Plugins = new JFrame("插件导入/导出");
        mainFrame_Plugins.setLayout(new BorderLayout());
        JCheckBox  vuln_poc_checkbox  = new JCheckBox("漏洞POC");
        vuln_poc_checkbox.setSelected(true);
        JCheckBox  plugins_poc_checkbox  = new JCheckBox("指纹POC");
        JCheckBox  catrgory_checkbox  = new JCheckBox("所有分组");
        JCheckBox  config_checkbox  = new JCheckBox("配置文件");
        JButton plugins_button_go = new JButton("确定");

        plugins_button_go.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(Objects.equals(type_, "export")){
                    try {
                        plugin_export(vuln_poc_checkbox.isSelected(),plugins_poc_checkbox.isSelected(),catrgory_checkbox.isSelected(),config_checkbox.isSelected());
                    } catch (JsonProcessingException ex) {
                        printErr(ex.getMessage());
                        printErr(Arrays.toString(ex.getStackTrace()));
                    }
                } else if (Objects.equals(type_, "import")) {
                    try {
                        plugin_import(vuln_poc_checkbox.isSelected(),plugins_poc_checkbox.isSelected(),catrgory_checkbox.isSelected(),config_checkbox.isSelected());
                    } catch (IOException | SQLException ex) {
                        printErr(ex.getMessage());
                        printErr(Arrays.toString(ex.getStackTrace()));

                    }
                }else{
                    JOptionPane.showMessageDialog(null, "导入导出类型错误！", "提示", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        JButton plugins_button_cancal = new JButton("取消");
        plugins_button_cancal.addActionListener(e -> mainFrame_Plugins.setVisible(false));
        JPanel jpanel_plugins_ = new JPanel();
        GridBagLayout gbl=new GridBagLayout();//创建网格包布局管理器
        GridBagConstraints gbc=new GridBagConstraints();//GridBagConstraints对象来给出每个组件的大小和摆放位置
        jpanel_plugins_.setLayout(gbl);//设置容器布局为网格包布局类型
        gbc.fill=GridBagConstraints.BOTH;//组件填充显示区域，当格子有剩余空间时，填充空间
        gbc.insets = new Insets(5, 5, 5, 5); // 边距
        gbc.anchor = GridBagConstraints.NORTHWEST; // 对齐方式

        jpanel_plugins_.add(Add_Component(gbl,vuln_poc_checkbox,gbc,0,0,1,1,1,0));
        jpanel_plugins_.add(Add_Component(gbl,plugins_poc_checkbox,gbc,1,0,1,1,1,0));

        jpanel_plugins_.add(Add_Component(gbl,catrgory_checkbox,gbc,0,1,1,1,1,0));
        jpanel_plugins_.add(Add_Component(gbl,config_checkbox,gbc,1,1,1,1,1,0));

//        按钮行
        gbc.fill=GridBagConstraints.NORTH;//组件填充显示区域，当格子有剩余空间时，填充空间
        gbc.anchor = GridBagConstraints.CENTER; // 对齐方式
        jpanel_plugins_.add(Add_Component(gbl,plugins_button_go,gbc,0,2,1,1,0,0));
        jpanel_plugins_.add(Add_Component(gbl,plugins_button_cancal,gbc,1,2,1,1,0,0));
//
        jpanel_plugins_.setBorder(new EmptyBorder(10, 10, 10, 10)); // 设置外边距为10
        mainFrame_Plugins.getContentPane().add(jpanel_plugins_);
        mainFrame_Plugins.setSize(300, 180);
        mainFrame_Plugins.setLocationRelativeTo(null);
        mainFrame_Plugins.setVisible(true);
    }

    private static void plugin_export(boolean vuln_poc, boolean finger_poc, boolean group, boolean config) throws JsonProcessingException {
        Map<String,Object> data = new HashMap<>();
        if (vuln_poc){
            List<PocEntry> all_poc = model_poc.getAllValue();
//            List<Map> all_poc_map  = new ArrayList<>();
//            // 遍历 PocEntry 列表
//            for (PocEntry poc : all_poc) {
//                // 创建一个新的 HashMap 来存放当前 PocEntry 的属性
//                HashMap<String, Object> temp = new HashMap<>();
//                // 获取 PocEntry 类的所有公共字段
//                Field[] fields = PocEntry.class.getDeclaredFields();
//                // 遍历字段
//                for (Field field : fields) {
//                    // 获取字段名称
//                    String fieldName = field.getName();
//                    // 获取字段值
//                    try {
//                        field.setAccessible(true); // 允许访问私有字段
//                        Object fieldValue = field.get(poc);
//                        temp.put(fieldName, fieldValue);
//                    } catch (IllegalAccessException e) {
//                        printErr(e.getMessage());
//                        printErr(Arrays.toString(e.getStackTrace()));
//                    }
//                }
//                // 将 temp Map 添加到 allPocMap 列表中
//                all_poc_map.add(temp);
//            }
            data.put("vuln_poc",all_poc);
        }
        if (finger_poc){
            List<FingerEntry> all_poc = model_finger.getAllValue();
//            List<Map> all_poc_map  = new ArrayList<>();
//            // 遍历 PocEntry 列表
//            for (FingerEntry poc : all_poc) {
//                // 创建一个新的 HashMap 来存放当前 PocEntry 的属性
//                HashMap<String, Object> temp = new HashMap<>();
//                // 获取 PocEntry 类的所有公共字段
//                Field[] fields = FingerEntry.class.getDeclaredFields();
//                // 遍历字段
//                for (Field field : fields) {
//                    // 获取字段名称
//                    String fieldName = field.getName();
//                    // 获取字段值
//                    try {
//                        field.setAccessible(true); // 允许访问私有字段
//                        Object fieldValue = field.get(poc);
//                        temp.put(fieldName, fieldValue);
//                    } catch (IllegalAccessException e) {
//                        printErr(e.getMessage());
//                        printErr(Arrays.toString(e.getStackTrace()));
//                    }
//                }
                // 将 temp Map 添加到 allPocMap 列表中
//                all_poc_map.add(temp);
//            }
            data.put("finger_poc",all_poc);
        }
        if (group){
            List<GroupEntry> all_group = model_group.getAllValue();
//            List<Map> all_poc_map  = new ArrayList<>();
//            // 遍历 PocEntry 列表
//            for (GroupEntry poc : all_group) {
//                // 创建一个新的 HashMap 来存放当前 PocEntry 的属性
//                HashMap<String, Object> temp = new HashMap<>();
//                // 获取 PocEntry 类的所有公共字段
//                Field[] fields = GroupEntry.class.getDeclaredFields();
//                // 遍历字段
//                for (Field field : fields) {
//                    // 获取字段名称
//                    String fieldName = field.getName();
//                    // 获取字段值
//                    try {
//                        field.setAccessible(true); // 允许访问私有字段
//                        Object fieldValue = field.get(poc);
//                        temp.put(fieldName, fieldValue);
//                    } catch (IllegalAccessException e) {
//                        printErr(e.getMessage());
//                        printErr(Arrays.toString(e.getStackTrace()));
//                    }
//                }
//                // 将 temp Map 添加到 allPocMap 列表中
//                all_poc_map.add(temp);
//            }
            data.put("group",all_group);
        }
        if (config){
            data.put("config",  Main_Vuln.Global_Config);
        }
        String filepath = fileChooser_save();
        if(filepath.isEmpty()){
            JOptionPane.showMessageDialog(null, "请选择一个Json文件", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
//        String jsonOutput = mapper.writeValueAsString(data);
        String jsonOutput = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
//
//        Yaml yaml = new Yaml(options);
//        String yamlString = yaml.dump(data);
        try (Writer out = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(Paths.get(filepath)), StandardCharsets.UTF_8))) {
            out.write(jsonOutput);
            mainFrame_Plugins.setVisible(false);
            JOptionPane.showMessageDialog(null, "导出成功！文件位置:"+filepath, "提示", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            printErr(Arrays.toString(e.getStackTrace()));
            JOptionPane.showMessageDialog(null, e.getMessage(), "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static void plugin_import(boolean vuln_poc, boolean finger_poc, boolean group, boolean config) throws IOException, SQLException {
        ObjectMapper mapper = new ObjectMapper();
        String filepath = fileChooser_open();
        Map data;
        if(filepath.isEmpty()){
            JOptionPane.showMessageDialog(null, "请选择一个Json文件", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            data = mapper.readValue(new File(filepath), Map.class);
        }catch (Exception ex){
            JOptionPane.showMessageDialog(null, "文件加载失败！Error:"+ex.getMessage(), "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (vuln_poc){
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> PocList = (List<Map<String, Object>>) data.get("vuln_poc");
            // 将 List<Map> 转换为 List<PocEntry>
            List<PocEntry> pocEntries = PocList.stream()
                    .map(map -> mapper.convertValue(map, PocEntry.class))
                    .collect(Collectors.toList());
            all_poc_data = pocEntries;
            PocTableModel.save_data_to_db();

        }
        if (finger_poc){
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> PocList = (List<Map<String, Object>>) data.get("finger_poc");
            // 将 List<Map> 转换为 List<PocEntry>
            List<FingerEntry> pocEntries = PocList.stream()
                    .map(map -> mapper.convertValue(map, FingerEntry.class))
                    .collect(Collectors.toList());

            FingerTableModel.all_finger_data = pocEntries;
            System.out.println("777");

            FingerTableModel.save_data_to_db();
            System.out.println("222");
        }
        if (group){
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> PocList = (List<Map<String, Object>>) data.get("group");
            // 将 List<Map> 转换为 List<PocEntry>
            List<GroupEntry> pocEntries = PocList.stream()
                    .map(map -> mapper.convertValue(map, GroupEntry.class))
                    .collect(Collectors.toList());
            GroupTableModel.all_group_data = pocEntries;
            GroupTableModel.save_data_to_db();
        }
        if (config){
            Global_Config = (Map<String, Object>)data.get("config");
            save_config();
        }
        mainFrame_Plugins.setVisible(false);
        JOptionPane.showMessageDialog(null, "导入成功!", "提示", JOptionPane.INFORMATION_MESSAGE);

    }

    private  static String fileChooser_open() {
        JFileChooser fileChooser = new JFileChooser();
        // 创建一个新的JFrame实例
        JFrame frame = new JFrame("导入配置");
        fileChooser.setSelectedFile(new File("data.json"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Json文件(*.json)", "json"));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // 创建一个JFileChooser实例
        // 设置默认显示目录
        if (lastDirectory != null) {
            fileChooser.setCurrentDirectory(new File(lastDirectory));
        } else {
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        }

        // 显示打开文件对话框
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            // 更新 lastDirectory 变量
            lastDirectory = String.valueOf(selectedFile.getParentFile());
            // 在这里你可以添加读取文件的逻辑
            return  selectedFile.getAbsolutePath();
        } else {
            return "";
        }
    }
    private  static String fileChooser_save(){
        // 创建一个JFileChooser实例
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("导出配置");
        fileChooser.setSelectedFile(new File("data.json"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Json文件(*.json)", "json"));
        // 设置默认显示目录
        if (lastDirectory != null) {
            fileChooser.setCurrentDirectory(new File(lastDirectory));
        } else {
            // 设置默认显示目录
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        }
        // 显示保存文件对话框
        int result = fileChooser.showSaveDialog(mainFrame_Plugins);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            // 如果文件名不包含扩展名，你可以添加一个
            if (!selectedFile.getName().endsWith(".json")) {
                selectedFile = new File(selectedFile.getPath() + ".json");
            }
            lastDirectory = String.valueOf(selectedFile.getParentFile());
            return  selectedFile.getAbsolutePath();
            // 在这里你可以添加保存文件的逻辑
        } else {
            return  "";
        }
    }
}

