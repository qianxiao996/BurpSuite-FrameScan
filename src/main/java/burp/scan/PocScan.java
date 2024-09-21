package burp.scan;

import burp.IHttpRequestResponse;
import burp.IHttpService;
import burp.model.poc.PocEntry;
import burp.rpc.Poc_Request;
import burp.rpc.Poc_Response;
import burp.utils.Poc;
import burp.model.log.LogEntry;
import org.yaml.snakeyaml.Yaml;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static burp.BurpExtender.callbacks;
import static burp.BurpExtender.helpers;
import static burp.Main_Vuln.*;
import static burp.utils.Conn.*;
import static burp.utils.CustHttpService.GetHttpRequestResponse;
import static burp.utils.Poc.*;

public class PocScan {
    public static LogEntry Scan_Advanced_HTTP_Request(IHttpRequestResponse messageInfo, int toolFlag, String dir_url, PocEntry poc) throws MalformedURLException {
        IHttpRequestResponse souce_requestResponse =  messageInfo;
        Yaml yaml = new Yaml(options);
        Map<String, Object> yamlString = yaml.load(poc.plugins_data);
        String  requests_raw = (String) yamlString.get("requests_raw");
        Map<String, Object> expression = (Map<String,Object>) yamlString.get("expression");

        requests_raw = Replace_Var_Request(messageInfo,requests_raw);
        // 分割原始请求字符串为头部和消息体
        Map head_body = get_requests_response_head_body(requests_raw.getBytes());
//        if (parts.length==1){
//            parts = requests_raw.split("\r\n\r\n", 2);
//        }
        List<String> httpHeaders = (List<String>) head_body.get("header");
        String bodyPart = (String) head_body.get("body");
        // 将头部字符串转换为List<String>
//        List<String> httpHeaders = new ArrayList<>(Arrays.asList(headersPart.split("\r\n")));
        String headUrl = httpHeaders.get(0);
//        //修改请求路径
        String[]  head_list = (headUrl.split(" "));
        if (head_list.length>=2){
            head_list[1] =  (dir_url+head_list[1]).replaceAll("//+","/");
        }
        headUrl =String.join(" ",head_list);
        httpHeaders.set(0,headUrl);
        // 如果消息体为空字符串，则不需要转换为byte[]
        byte[] new_bodyBytes = bodyPart.isEmpty() ? new byte[0] : bodyPart.getBytes();
        // 使用buildHttpMessage方法重建HTTP请求Headers:
        printDebug("【Scan_Advanced_HTTP_Request】【Request】\n"+ String.join("\n", httpHeaders) +"\n\n"+ new String(new_bodyBytes, StandardCharsets.UTF_8));
        URL reuslt_url;
        IHttpService iHttpService = messageInfo.getHttpService();
        IHttpRequestResponse requestResponse;
        if(is_burp){
            byte[] newRequest = helpers.buildHttpMessage(httpHeaders, new_bodyBytes);
            requestResponse = callbacks.makeHttpRequest(iHttpService, newRequest);//发送请求
            reuslt_url = helpers.analyzeRequest(requestResponse).getUrl();
        }else{
            String req_url;
            String temp_url  = get_requests_url(messageInfo,false);
            req_url = temp_url+("/"+dir_url+"/"+head_list[1]).replaceAll("//+","/");
            requestResponse =  GetHttpRequestResponse(req_url,httpHeaders,new String(new_bodyBytes));
            reuslt_url = new URL(req_url);
        }

        printDebug("【Scan_Advanced_HTTP_Request】【Response】"+reuslt_url + " " + GetMessageStatusCode(requestResponse)+"\n");
        if (requestResponse != null) {
            printDebug("【Scan_Advanced_HTTP_Request】【Response】\n" + new String(requestResponse.getResponse()) + "\n");
        }
        String tool_name="Others";
        if(is_burp){
            tool_name = callbacks.getToolName(toolFlag);
        }
        Boolean vuln_scan_result = Match_poc_data(requestResponse,expression);
        if(vuln_scan_result){
            printMsg("【Scan_Advanced_HTTP_Request】【Match Success】URL:"+reuslt_url+"【POC】"+poc.name+"【Match loaction】"+expression.get("scope")+"【Match Method】"+expression.get("match_method")+"【Match Value】"+expression.get("match_value"));
            return new LogEntry(count, tool_name, souce_requestResponse, reuslt_url, "", "", "", 0,GetMessageStatusCode(requestResponse), "", poc.name, poc.id,requestResponse);
        }
        return  null;
    }
    public static LogEntry Scan_Python_Poc(IHttpRequestResponse requestResponse, int toolFlag, String dir_url, PocEntry poc) throws MalformedURLException {
        IHttpService httpService = requestResponse.getHttpService();
        String host_url = get_requests_url(requestResponse,false)+dir_url;
        host_url =host_url.replaceAll("//+","/");
        URL result_url =new URL(host_url);
        try{
            if(host_url==null || poc==null || poc.plugins_data==null) return null;
            Poc_Request requests = Poc_Request.newBuilder()
                    .setPocType("Python")
                    .setUrl(host_url)
                    .setPoc(poc.plugins_data)
                    .build();
            Poc_Response response =Grpc_Send(requests);
            printDebug("【Scan_Python_Poc】【Request】"+poc.name);
            if (response!=null && response.getIsSuccess()) {
                IHttpRequestResponse result_rr = new IHttpRequestResponse() {
                    @Override
                    public byte[] getRequest() {
                        return ("URL: " + dir_url + "\n" + poc.plugins_data).getBytes(StandardCharsets.UTF_8);
                    }
                    @Override
                    public void setRequest(byte[] bytes) {

                    }

                    @Override
                    public byte[] getResponse() {
                        return ("return flag:" + response.getIsSuccess() + "\nreturn result:" + response.getResult()).getBytes(StandardCharsets.UTF_8);
                    }

                    @Override
                    public void setResponse(byte[] bytes) {

                    }

                    @Override
                    public String getComment() {
                        return null;
                    }

                    @Override
                    public void setComment(String s) {

                    }

                    @Override
                    public String getHighlight() {
                        return null;
                    }

                    @Override
                    public void setHighlight(String s) {

                    }

                    @Override
                    public IHttpService getHttpService() {
                        return httpService;
                    }

                    @Override
                    public void setHttpService(IHttpService iHttpService) {

                    }
                };
                String tool_name="Others";
                if(is_burp){
                    tool_name = callbacks.getToolName(toolFlag);
                }
                printMsg("【Scan_Python_Poc】【Match Success】URL:"+result_url+"【POC】"+poc.name+"【Response_result】"+response.getResult());
                return new LogEntry(count, tool_name, requestResponse, result_url, "", "", "", 0,0, "", poc.name,poc.id,result_rr);
            }
            if (response != null) {
                printDebug("【Scan_Python_Poc】【Response】【"+poc.name+"】【"+result_url+"】"+ false +" "+response.getResult());
            }
        }catch (Exception ex){
            printErr(Arrays.toString(ex.getStackTrace()));
            printErr(ex.getMessage());
        }
        return  null;
    }
    public static LogEntry Scan_Yaml_Poc(IHttpRequestResponse requestResponse, int toolFlag, String dir_url, PocEntry poc) throws MalformedURLException {
        Yaml yaml = new Yaml(options);
        Map<String, Object> yamlString = yaml.load(poc.plugins_data);
        Map<String,Map> rules = (Map<String,Map>) yamlString.get("rules");
        try{
            for (Map.Entry<String, Map> ruleEntry : rules.entrySet()) {
                Map<String,Map> ruleDetails = ruleEntry.getValue();
                Map<String,String> requests = ruleDetails.get("request");
                String path = requests.get("path");
                String replace_path;
                if(!Objects.equals(dir_url, "/")){
                    replace_path = dir_url+path;
                    replace_path = replace_path.replaceAll("//+","/");
                }else {
                    replace_path = path;
                }
                poc.plugins_data = poc.plugins_data.replaceAll(path,replace_path);
            }
        }catch (Exception ex){
            printErr("Yaml Replace String Fail "+Arrays.toString(ex.getStackTrace()));
        }
        IHttpService httpService = requestResponse.getHttpService();
        String host_url =get_requests_url(requestResponse,false);
        String aaa_url = host_url+dir_url;
        URL result_url =new URL(aaa_url);
        try{
            if(host_url==null || poc==null || poc.plugins_data==null) return null;
            Poc_Request requests = Poc_Request.newBuilder()
                    .setPocType("Yaml")
                    .setUrl(host_url)
                    .setPoc(poc.plugins_data)
                    .build();
            Poc_Response response =Grpc_Send(requests);
            printDebug("【Scan_Yaml_Poc】【Request】"+poc.name);
//        System.out.println("Response: " + response.getIsSuccess() + ", " + response.getResult());
            if (response!=null && response.getIsSuccess()) {
                String tool_name="Others";
                if(is_burp){
                    tool_name = callbacks.getToolName(toolFlag);
                }
                IHttpRequestResponse result_rr =new IHttpRequestResponse() {
                    @Override
                    public byte[] getRequest() {
                        return  ("URL: " + dir_url + "\n" +poc.plugins_data).getBytes(StandardCharsets.UTF_8);
                    }

                    @Override
                    public void setRequest(byte[] bytes) {

                    }

                    @Override
                    public byte[] getResponse() {
                        return ("return flag:"+response.getIsSuccess()+"\nreturn result:"+response.getResult()).getBytes(StandardCharsets.UTF_8);
                    }

                    @Override
                    public void setResponse(byte[] bytes) {

                    }

                    @Override
                    public String getComment() {
                        return null;
                    }

                    @Override
                    public void setComment(String s) {

                    }

                    @Override
                    public String getHighlight() {
                        return null;
                    }

                    @Override
                    public void setHighlight(String s) {

                    }

                    @Override
                    public IHttpService getHttpService() {
                        return httpService;
                    }

                    @Override
                    public void setHttpService(IHttpService iHttpService) {

                    }
                };
                printMsg("【Scan_Yaml_Poc】【Match Success】URL:"+result_url+"【POC】"+poc.name+"【Response_result】"+response.getResult());
                return new LogEntry(count, tool_name, requestResponse, result_url, "", "", "", 0, 0, "", poc.name,poc.id,result_rr);
            }
            printDebug("【Scan_Yaml_Poc】【Response】【"+poc.name+"】【"+result_url+"】"+ false +" "+response.getResult());
//        channel.shutdownNow();
        }catch (Exception ex){
            printErr(Arrays.toString(ex.getStackTrace()));
            printErr(ex.getMessage());
        }
        return  null;
    }

    public static LogEntry Scan_Simple_HTTP_Request(IHttpRequestResponse messageInfo, int toolFlag, String dir_url, PocEntry poc) throws MalformedURLException {
        //遍历目录 对每个poc
        Yaml yaml = new Yaml(options);
        Map<String, Object> yamlString = yaml.load(poc.plugins_data);
        Map<String,Object> requests = (Map<String,Object>) yamlString.get("requests");
        Map<String, Object> expression = (Map<String,Object>) yamlString.get("expression");
        String url;
        String tihuan_url="/" ;
        String method=null;
        String body="";
        Map<String,String> headers = new HashMap<>();
        byte[] bodyBytes = "".getBytes();
        if(requests!=null && requests.size()>0){
            headers  = (Map<String, String>) requests.get("headers");
            body  = (String) requests.get("body");
            method  = (String) requests.get("method");
            url  = (String) requests.get("url");
            if(url!=null){
                tihuan_url = dir_url+"/"+url;

            }else{
                String[] headurl_list = get_requests_head_line(messageInfo);
                if (headurl_list != null) {
                    tihuan_url = headurl_list[1];
                }
            }
            if(body==null ){
                if(is_burp){
                    byte[] requestBytes = messageInfo.getRequest();
                    // 分析请求以获取请求体的偏移量
                    int bodyOffset = helpers.analyzeRequest(messageInfo).getBodyOffset();
                    // 获取请求体
                    bodyBytes = new byte[requestBytes.length - bodyOffset];
                    System.arraycopy(requestBytes, bodyOffset, bodyBytes, 0, requestBytes.length - bodyOffset);
                }
                else{
                    bodyBytes = (byte[]) get_requests_response_head_body(messageInfo.getRequest()).get("body");
                }
            }
        }else{
            String[] headurl_list = get_requests_head_line(messageInfo);
            if (headurl_list != null) {
                tihuan_url = headurl_list[1];
            }
        }
        List<String> http_headers_info  = Set_Header_Requests(messageInfo,method,tihuan_url,headers);
        printDebug("【Scan_Simple_HTTP_Request】【Request】\n"+ String.join("\n", http_headers_info) +"\n\n"+ new String(bodyBytes, StandardCharsets.UTF_8));
        IHttpRequestResponse requestResponse;
        IHttpService iHttpService = messageInfo.getHttpService();
        URL reuslt_url ;
        String tool_name="Others";
        if (is_burp){
            tool_name = callbacks.getToolName(toolFlag);
            byte[] new_Requests = helpers.buildHttpMessage(http_headers_info, bodyBytes); //关键方法
            requestResponse = callbacks.makeHttpRequest(iHttpService, new_Requests);//发送请求
            reuslt_url = helpers.analyzeRequest(requestResponse).getUrl();
        }else{
            String req_url;
            String temp_url = get_requests_url(messageInfo,false);
            req_url = temp_url+("/"+tihuan_url).replaceAll("//+","/");
            requestResponse =  GetHttpRequestResponse(req_url,http_headers_info,new String(bodyBytes));
            reuslt_url = new URL(req_url);
        }
        if (requestResponse != null) {
            printDebug("【Scan_Simple_HTTP_Request】【Response】\n"+ new String(requestResponse.getResponse())+"\n");
        }else{
            return null;
        }
//        printDebug("【Scan_Simple_HTTP_Request】"+helpers.analyzeRequest(requestResponse).getUrl() + " " + String.valueOf(helpers.analyzeResponse(requestResponse.getResponse()).getStatusCode())+"\n");
        Boolean vuln_scan_result = Match_poc_data(requestResponse,expression);
        if(vuln_scan_result){
            printMsg("【Scan_Simple_HTTP_Request】【Match Success】URL:"+reuslt_url+"【POC】"+poc.name+"【Match loaction】"+expression.get("scope")+"【Match Method】"+expression.get("match_method")+"【Match Value】"+expression.get("match_value"));
            return new LogEntry(count, tool_name, messageInfo, reuslt_url, "", "", "", 0, GetMessageStatusCode(requestResponse), "", poc.name,poc.id,requestResponse);
        }
        return null;
    }

    public static void Check_Vuln(int toolFlag, IHttpRequestResponse messageInfo, LogEntry finger_scan_result) throws MalformedURLException {
        //得到所有的poc
        IHttpService httpService = messageInfo.getHttpService();
        // 获取主机名
        String host = httpService.getHost();
        printDebug("Start Poc Scan Host:"+host);
        String finger;
        if(finger_scan_result!=null && finger_scan_result.finger_scan_result!=null){
            finger = finger_scan_result.finger_scan_result;
        }else{
            finger="";
        }
        List<PocEntry> match_poc_list = Poc.get_mathch_poc(finger);

        finger =finger.replaceAll("\\$\\$\\$",",");
        if(finger.equals(",")){
            finger="";
        }
        if(stop_poc_scan || match_poc_list.isEmpty()){
            return;
        }
        printDebug("【Host】"+host+" 【Poc num】"+match_poc_list.size());
        //url
        String wnazheng_url  =get_requests_url(messageInfo,true);
        URL url  = new URL(wnazheng_url);
        String path = url.getPath(); //获取的路径
        String paths = url.getProtocol()+"://"+url.getAuthority();
        boolean is_vuln =false;
        //黑白名单检测
        if(is_white_black(url)){
            printDebug("【Host】"+host+"【White_Black bypass】");
            if(path.endsWith("/")) { // 判断是否以/ 结尾
                path = path+"{ChangeTemps}";

            }
            List<String> list = countString(path,"/"); // 获取路径中所有路径的组合
            for (PocEntry poc : match_poc_list) { //遍历字典
                printDebug("【"+poc.plugin_type+"】【Host】"+host+"【Poc】"+poc.name);
                //目录层级
                int conboBox = poc.dir_count;
                if(conboBox ==0) {
                    conboBox = 100;
                }
                for(int urlLength=0;urlLength<conboBox&&urlLength<list.size();urlLength++) { // 匹配层级
                    printDebug("【"+poc.plugin_type+"】【Host】"+host+list.get(urlLength)+"【Poc Scan】"+poc.name);
                    count++;
                    String logsPahtTemp = paths + list.get(urlLength) + "$$$" + poc.id;// 获取路径
                    if(is_burp){
                        if (logList.contains(logsPahtTemp)) { // 判断是否包含在list中
                            printDebug("【"+poc.plugin_type+"】【Host】"+host+list.get(urlLength)+"【Poc Scan】This URL has been scanned and will not be scanned this time");
                            continue; // 继续执行 存在历史 则不进行处理
                        }
                        logList.add(logsPahtTemp);
                    }

                    LogEntry temp = null;
                    if (Objects.equals(poc.plugin_type, "简单请求替换")) {
                        temp = PocScan.Scan_Simple_HTTP_Request(messageInfo, toolFlag, list.get(urlLength), poc);
                    }else if (Objects.equals(poc.plugin_type, "高级HTTP请求")) {
                        temp = PocScan.Scan_Advanced_HTTP_Request(messageInfo, toolFlag, list.get(urlLength), poc);
                    }else if (Objects.equals(poc.plugin_type, "Yaml Poc")) {
                        temp = PocScan.Scan_Yaml_Poc(messageInfo, toolFlag, list.get(urlLength), poc);
                    }else if (Objects.equals(poc.plugin_type, "Python Poc")) {
                        temp = PocScan.Scan_Python_Poc(messageInfo, toolFlag, list.get(urlLength), poc);
                    }
                    if (temp != null){
                        if(!temp.vuln_scan_result.isEmpty()){
                            printDebug("【"+poc.plugin_type+"】【Host】"+host+list.get(urlLength)+"【Poc Scan】"+poc.name+"【Result】"+temp.vuln_scan_result);
                            temp.finger_scan_result = finger;
                            temp.vuln_scan_result = poc.name;
                            model.addValueAt(temp);
                            model.fireTableDataChanged();
                            is_vuln=true;
                        }
                    } else{
                        printDebug("【"+poc.plugin_type+"】【Host】"+host+list.get(urlLength)+"【Poc Scan】"+poc.name+"【Result】False");
                    }
                }
            }
            if(!is_vuln){
                if ( !finger.isEmpty() && finger_scan_result!=null) {
                    printMsg("【No Vuln  Finger Output】【Host】" + url + "【Finger】" +finger);
                    model.addValueAt(finger_scan_result);
                    model.fireTableDataChanged();
                }else{
                    printMsg("【No Vuln  Finger Output】【Host】" + url + "【Finger】" +finger);
                }
            }
        }
    }




}
