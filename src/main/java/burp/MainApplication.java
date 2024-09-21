package burp;
import com.formdev.flatlaf.*;
import com.formdev.flatlaf.intellijthemes.*;
import com.formdev.flatlaf.intellijthemes.FlatArcDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatDraculaIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatMonokaiProIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatSolarizedDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatSolarizedLightIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.*;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
public class MainApplication extends JFrame {
    public MainApplication() {
        // 初始化窗口
        this.initUI();
    }

    private void initUI() {
        setTitle(Main_Vuln.app_title);
        //插件物理路径
        //配置文件
        String currentDir = System.getProperty("user.dir");
        String Config_PATH  = String.format("%s/%s", currentDir, Main_Vuln.Config_FILE);
        Main_Vuln.load_config_bypath(Config_PATH);
        try
        {

            String themes = (String) Main_Vuln.Global_Config.get("Themes");
//            String themes = "FlatIntelliJLaf";
//            String themes = "FlatArcIJTheme";
//            String themes = "FlatArcOrangeIJTheme";
            switch (themes){
                case "FlatDarkLaf":
                    FlatDarkLaf.setup();
                    break;
                case  "FlatIntelliJLaf":
                    FlatIntelliJLaf.setup();
                    break;
                case  "FlatLightLaf":
                    FlatLightLaf.setup();
                    break;
                case  "FlatCyanLightIJTheme":
                    FlatCyanLightIJTheme.setup();
                    break;
                case  "FlatArcIJTheme":
                    FlatArcIJTheme.setup();
                    break;
                case  "FlatArcOrangeIJTheme":
                    FlatArcOrangeIJTheme.setup();
                    break;
                case  "FlatLightFlatIJTheme":
                    FlatLightFlatIJTheme.setup();
                    break;
                case  "FlatSolarizedLightIJTheme":
                    FlatSolarizedLightIJTheme.setup();
                    break;
                case  "FlatGitHubDarkIJTheme":
                    FlatGitHubDarkIJTheme.setup();
                    break;
                case  "FlatSpacegrayIJTheme":
                    FlatSpacegrayIJTheme.setup();
                    break;
                case  "FlatVuesionIJTheme":
                    FlatVuesionIJTheme.setup();
                    break;
                case  "FlatXcodeDarkIJTheme":
                    FlatXcodeDarkIJTheme.setup();
                    break;
                case  "FlatAtomOneDarkIJTheme":
                    FlatAtomOneDarkIJTheme.setup();
                    break;
                case  "FlatGrayIJTheme":
                    FlatGrayIJTheme.setup();
                    break;
                case  "FlatArcDarkIJTheme":
                    FlatArcDarkIJTheme.setup();
                    break;
                case  "FlatArcDarkOrangeIJTheme":
                    FlatArcDarkOrangeIJTheme.setup();
                    break;
                case  "FlatCarbonIJTheme":
                    FlatCarbonIJTheme.setup();
                    break;
                case  "FlatCobalt2IJTheme":
                    FlatCobalt2IJTheme.setup();
                    break;
                case  "FlatHighContrastIJTheme":
                    FlatHighContrastIJTheme.setup();
                    break;
                case  "FlatDarkFlatIJTheme":
                    FlatDarkFlatIJTheme.setup();
                    break;
                case  "FlatDarkPurpleIJTheme":
                    FlatDarkPurpleIJTheme.setup();
                    break;
                case  "FlatDraculaIJTheme":
                    FlatDraculaIJTheme.setup();
                    break;
                case  "FlatGradiantoDarkFuchsiaIJTheme":
                    FlatGradiantoDarkFuchsiaIJTheme.setup();
                    break;
                case  "FlatGradiantoDeepOceanIJTheme":
                    FlatGradiantoDeepOceanIJTheme.setup();
                    break;
                case  "FlatGradiantoMidnightBlueIJTheme":
                    FlatGradiantoMidnightBlueIJTheme.setup();
                    break;
                case  "FlatGradiantoNatureGreenIJTheme":
                    FlatGradiantoNatureGreenIJTheme.setup();
                    break;
                case  "FlatGruvboxDarkHardIJTheme":
                    FlatGruvboxDarkHardIJTheme.setup();
                    break;
                case  "FlatGruvboxDarkSoftIJTheme":
                    FlatGruvboxDarkSoftIJTheme.setup();
                    break;
                case  "FlatHiberbeeDarkIJTheme":
                    FlatHiberbeeDarkIJTheme.setup();
                    break;
                case  "FlatMaterialDesignDarkIJTheme":
                    FlatMaterialDesignDarkIJTheme.setup();
                    break;
                case  "FlatMonocaiIJTheme":
                    FlatMonocaiIJTheme.setup();
                    break;
                case  "FlatMonokaiProIJTheme":
                    FlatMonokaiProIJTheme.setup();
                    break;
                case  "FlatGruvboxDarkMediumIJTheme":
                    FlatGruvboxDarkMediumIJTheme.setup();
                    break;
                case  "FlatNordIJTheme":
                    FlatNordIJTheme.setup();
                    break;
                case  "FlatOneDarkIJTheme":
                    FlatOneDarkIJTheme.setup();
                    break;
                case  "FlatSolarizedDarkIJTheme":
                    FlatSolarizedDarkIJTheme.setup();
                    break;
            }
        }
        catch(Exception e)
        {
            //TODO exception
        }

        Main_Vuln.is_burp=false;
        //sql数据库物理路径
        Main_Vuln.SQL_DB_PATH = Paths.get(currentDir, Main_Vuln.SQL_DB_FILE).toString();
        JTabbedPane tab = new JTabbedPane();
        try {
            Main_Vuln.load_plugins(MainApplication.this,tab);
        } catch (SQLException | ClassNotFoundException | IOException e) {
            System.out.println(e.getMessage());
        }
        add(tab);
        // 设置窗口的默认关闭操作
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // 设置窗口的大小
        setSize(1300, 800);
        // 设置窗口的位置
        setLocationRelativeTo(null); // 居中显示
        // 设置窗口可见
        setVisible(true);
        Main_Vuln.splitPane_request_response.setDividerLocation(0.5);//设置分割的大小

    }

    public static void main(String[] args) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainApplication();
            }
        });
    }
//    public JTabbedPane load_scan_ui(JTabbedPane  tab){
//        JLabel  aa =new JLabel("ddd");
//        JPanel tab_index = new JPanel();
//        tab_index.setLayout(new GridLayout(1,1,5,5));
//        tab_index.add(aa);
//        tab.addTab("扫描目标",tab_index);
//        return tab;
//    }
}
