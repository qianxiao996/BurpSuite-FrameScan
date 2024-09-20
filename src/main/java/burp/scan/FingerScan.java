package burp.scan;

import burp.IHttpRequestResponse;
import burp.IHttpService;
import burp.model.finger.FingerEntry;
import burp.model.log.LogEntry;

import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static burp.BurpExtender.callbacks;
import static burp.BurpExtender.helpers;
import static burp.Main_Vuln.*;
import static burp.Main_Vuln.count;
import static burp.utils.Conn.*;
import static burp.utils.Finger.*;

public class FingerScan {
    public static LogEntry Check_Finger(int toolFlag, IHttpRequestResponse messageInfo) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        List<FingerEntry> all_finger = model_finger.getAllValue();
        if(stop_finger_scan || all_finger.isEmpty()){
            return  null;
        }
        IHttpService httpService = messageInfo.getHttpService();
        // 获取主机名
        String host_url = get_requests_url(messageInfo,false);

        String host = httpService.getHost();
        printDebug("Start Finger Scan Host:"+host_url);
        if(finger_ico_hash_map.isEmpty() || finger_ico_hash_map.get(host)==null || Objects.equals(finger_ico_hash_map.get(host), "")){
            printDebug("【GET ICO】"+host_url);
            String iconhash= get_host_ico(host_url,messageInfo);
            if(!iconhash.isEmpty()){
                printDebug("【GET ICO】"+host_url+"【Hash】"+iconhash);
                finger_ico_hash_map.put(host,iconhash);
            }else{
                printDebug("【GET ICO】"+host_url+"【Ico Hash】null, all faviconhash plugins is close!");
            }
        }
        printDebug("【Finger num】"+all_finger.size());
        List<String> all_finger_result=new ArrayList<>();
        for(FingerEntry item :all_finger){
            count++;
            String mathc_result="";
            if(Objects.equals(item.method, "keyword")){
                mathc_result = Match_keyword_finger(messageInfo,item);

            } else if (Objects.equals(item.method, "faviconhash")) {
                mathc_result = Match_faviconhash_finger(messageInfo,item,host);
            }
            if (mathc_result!=null && !mathc_result.isEmpty()){
                all_finger_result.add(mathc_result);
            }
        }
        String tool_name="Others";
        URL reuslt_url;
        String[] head_list = get_requests_head_line(messageInfo);
        if(is_burp){
            tool_name = callbacks.getToolName(toolFlag);
            reuslt_url = helpers.analyzeRequest(messageInfo).getUrl();
        }else{
            String req_url;
            if (head_list != null) {
                req_url = get_requests_url(messageInfo,false)+head_list[1];
            }else{
                req_url = get_requests_url(messageInfo,false);
            }
            reuslt_url = new URL(req_url);
        }

        String enable_poc_str = String.join("$$$", all_finger_result);
        printDebug("【Match End】"+reuslt_url+"【Enable_poc_str】"+enable_poc_str);
        if (!enable_poc_str.isEmpty()){
            return new LogEntry(count, tool_name, messageInfo, reuslt_url, "", "", "", 0, GetMessageStatusCode(messageInfo), enable_poc_str, "",-1);
//            return new LogEntry(count, tool_name, callbacks.saveBuffersToTempFiles(messageInfo), helpers.analyzeRequest(messageInfo).getUrl(), "", "", "", 0, (int) (helpers.analyzeResponse(messageInfo.getResponse()).getStatusCode()), enable_poc_str, "");
        }else{
            return  null;
        }
    }

}
