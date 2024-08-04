package burp;

import burp.utils.Config;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Map;
public class MainApplication extends JFrame {
    public static String SQL_DB_FILE="data.db";
    public static String Config_FILE="config.yml";


    public MainApplication() {
        // 初始化窗口
        this.initUI();
    }

    private void initUI() {
        setTitle("FrameScan");
        JPanel MainPane = new JPanel();
        MainPane.setPreferredSize(new Dimension(1000, 500));
        MainPane.setLayout(new BoxLayout(MainPane, BoxLayout.X_AXIS));
        //插件物理路径
        //配置文件
        String currentDir = System.getProperty("user.dir");
        try {
            String Config_PATH  = String.format("%s/%s", currentDir, Config_FILE);
            Map<String, Object> temp_config = Config.read_config(Config_PATH);
            Main_Vuln.Global_Config = temp_config;
            Main_Vuln.Config_PATH = Config_PATH;
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        Main_Vuln.is_burp=false;
        //sql数据库物理路径
        Main_Vuln.SQL_DB_PATH = Paths.get(currentDir, SQL_DB_FILE).toString();
        JTabbedPane tab = new JTabbedPane();
        try {
            Main_Vuln.load_plugins(MainApplication.this,tab);
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        add(tab);
        // 设置窗口的默认关闭操作
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // 设置窗口的大小
        setSize(1300, 700);
        // 设置窗口的位置
        setLocationRelativeTo(null); // 居中显示
        // 设置窗口可见
        setVisible(true);
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainApplication();
            }
        });
    }
}
