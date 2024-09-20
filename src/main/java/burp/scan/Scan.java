package burp.scan;
import burp.IHttpRequestResponse;
import burp.model.log.LogEntry;
import java.util.Arrays;

import static burp.BurpExtender.callbacks;
import static burp.Main_Vuln.*;
import static burp.scan.PocScan.Check_Vuln;
public class Scan {
    public static void accept_Http_Info(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo){
        if (!messageIsRequest) {
            if((toolFlag ==  callbacks.TOOL_PROXY && clicks_Proxy)|| (toolFlag ==  callbacks.TOOL_REPEATER && clicks_Repeater)){
                //指纹扫描
                if(switchs_finger){
                    finger_scan(toolFlag,messageInfo,true);
                }
                if(switchs_poc) {
                    poc_scan(toolFlag,messageInfo);
                }

            }
        }
    }
    public static void poc_scan(int toolFlag, IHttpRequestResponse messageInfo){
        Thread thread = new Thread(() -> {
            try {
                Check_Vuln(toolFlag,messageInfo,null);
            } catch (Exception ex) {
                printErr(ex.getMessage());
                printErr(Arrays.toString(ex.getStackTrace()));
            }
        });
        thread.start();
    }

    public static void finger_scan(int toolFlag, IHttpRequestResponse messageInfo,Boolean enable_poc){
        Thread thread = new Thread(() -> {
            try {
                LogEntry finger_info = FingerScan.Check_Finger(toolFlag, messageInfo);
                if (finger_info != null && !finger_info.finger_scan_result.isEmpty()) {
                    if (enable_poc && switchs_poc) {
                        Check_Vuln(toolFlag, messageInfo, finger_info);
                    } else {
                        finger_info.finger_scan_result = finger_info.finger_scan_result.replaceAll("\\$\\$\\$", ",");
                        model.addValueAt(finger_info);
                        model.fireTableDataChanged();
                    }
                }
            } catch (Exception ex) {
                printErr(ex.getMessage());
                printErr(Arrays.toString(ex.getStackTrace()));
            }
        });
        thread.start();
    }

    public static void myappscan(){
        Thread thread = new Thread(() -> {
            for(String url_str :all_scan_url){
                try {
                    IHttpRequestResponse messageInfo =  createHttpRequestResponse(url_str);
                    if(messageInfo!=null && messageInfo.getResponse().length>0&& switchs_finger){
                        finger_scan(1024,messageInfo,true);
                    }
                    if(messageInfo!=null &&  messageInfo.getResponse().length>0 &&  switchs_poc) {
                        poc_scan(1024,messageInfo);
                    }
                } catch (Exception ex) {
                    printErr(ex.getMessage());
                    printErr(Arrays.toString(ex.getStackTrace()));
                }
            }
        });
        thread.start();
    }
}
