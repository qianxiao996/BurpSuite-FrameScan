package burp;
import burp.utils.Scan;
import org.yaml.snakeyaml.Yaml;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import javax.swing.*;
import burp.utils.Config;

import static burp.Main_Vuln.channel;
import static burp.Main_Vuln.load_config;


public class BurpExtender  implements IBurpExtender, ITab, IHttpListener,IScannerCheck, IMessageEditorController,IContextMenuFactory,IExtensionStateListener
{
    public static JPanel MainPane;
    public static IBurpExtenderCallbacks callbacks;
    public static IExtensionHelpers helpers;
    public static IHttpRequestResponse currentlyDisplayedItem;

    public static String BurpConfigPath ;
    public static PrintWriter stdout;
    String Tools_Name ="FrameScan";
    String CONFIGVERSION = "v2.2";
    //
    public static String SQL_DB_FILE="data.db";

    public static String Config_FILE="config.yml";


    // implement IBurpExtender
    //
    @Override
    public void registerExtenderCallbacks(final IBurpExtenderCallbacks callbacks)
    {
        //输出
        stdout = new PrintWriter(callbacks.getStdout(), true);
        stdout.println("Loading..");

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
                MainPane = new JPanel();
                MainPane.setPreferredSize(new Dimension(1000, 500));
                MainPane.setLayout(new BoxLayout(MainPane, BoxLayout.X_AXIS));
                //插件物理路径
                BurpConfigPath = callbacks.getExtensionFilename().substring(0, (callbacks.getExtensionFilename().lastIndexOf(File.separator))) + File.separator;
                // main split pane
                //配置文件
                try {
                    String Config_PATH = String.format("%s/%s", BurpExtender.BurpConfigPath, Config_FILE);
                    Map<String, Object> temp_config = Config.read_config(Config_PATH);
                    Main_Vuln.Config_PATH = Config_PATH;
                    Main_Vuln.Global_Config = temp_config;
                } catch (IOException e) {
                    stdout.println(Arrays.toString(e.getStackTrace()));
                }
                //sql数据库物理路径
                Main_Vuln.SQL_DB_PATH = Paths.get(BurpConfigPath, SQL_DB_FILE).toString();

                //tab
                JTabbedPane tab = new JTabbedPane();
                try {
                    Main_Vuln.is_burp = true;
                    Main_Vuln.load_plugins(BurpExtender.this,tab);
                } catch (SQLException | ClassNotFoundException e) {
                    stdout.println(e);
                }

                MainPane.add(tab);

                // add the custom tab to Burp's UI
                callbacks.addSuiteTab(BurpExtender.this);

                // register ourselves as an HTTP listener
                callbacks.registerHttpListener(BurpExtender.this);
                callbacks.registerScannerCheck(BurpExtender.this);
                callbacks.registerContextMenuFactory(BurpExtender.this);


            }

        });
        stdout.println("Success..");
        stdout.println(Tools_Name+" "+CONFIGVERSION+" by qianxiao996!\nGithub:https://github.com/qianxiao996");
    }



    @Override
    public String getTabCaption()
    {
        return Tools_Name;
    }

    @Override
    public Component getUiComponent()
    {
        return MainPane;
    }

    //
    // implement IHttpListener
    //




    @Override
    public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo)
    {
        //漏洞扫描
        Scan.accept_Http_Info(toolFlag,messageIsRequest,messageInfo);
    }

    @Override
    public List<IScanIssue> doPassiveScan(IHttpRequestResponse baseRequestResponse) {
        return null;
    }

    @Override
    public List<JMenuItem> createMenuItems(final IContextMenuInvocation invocation) {
        //右键发送按钮功能
        if(invocation.getToolFlag() == IBurpExtenderCallbacks.TOOL_REPEATER || invocation.getToolFlag() == IBurpExtenderCallbacks.TOOL_PROXY){
            return  Main_Vuln.returnMenuItems(invocation);
//            listMenuItems.add(jMenu);
        }
        return new ArrayList<>();
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

    @Override
    public IHttpService getHttpService() {
//        return null;
        return currentlyDisplayedItem.getHttpService();

    }

    @Override
    public byte[] getRequest() {
        return currentlyDisplayedItem.getRequest();

//        return new byte[0];
    }
    @Override
    public void extensionUnloaded() {
        // 插件被卸载时执行的操作
        System.out.println("Extension unloaded. Cleaning up resources...");
        channel.shutdown();
        Main_Vuln.grpc_server_thread_stop_flag=false;

        Main_Vuln.grpc_server_thread.stop();
    }

    @Override
    public byte[] getResponse() {
        return currentlyDisplayedItem.getResponse();

//        return new byte[0];
    }



//    @Override
//    public byte[] getRequest()
//    {
//        return Main_Vuln.currentlyDisplayedItem.getRequest();
//    }
//
//    @Override
//    public byte[] getResponse()
//    {
//        return Main_Vuln.currentlyDisplayedItem.getResponse();
//    }
//
//    @Override
//    public IHttpService getHttpService()
//    {
//        return Main_Vuln.currentlyDisplayedItem.getHttpService();
//    }


}