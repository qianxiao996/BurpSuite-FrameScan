package burp.utils;

import burp.IHttpRequestResponse;
import burp.IHttpService;
import burp.IResponseInfo;
import burp.Main_Vuln;
import burp.rpc.Poc_Request;
import burp.rpc.Poc_Response;
import burp.rpc.pocGrpc;
import com.sun.net.httpserver.HttpServer;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import burp.finger.FingerEntry;
import burp.poc.PocEntry;
import burp.vuln.LogEntry;
import com.google.common.hash.Hashing;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static burp.BurpExtender.*;
import static burp.Main_Vuln.*;

public class Scan {
    private static final List<String> userAgents = new ArrayList<>();
    static {
        // 初始化User-Agent列表
        userAgents.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36");
        userAgents.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36");
        userAgents.add("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36");
        userAgents.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:90.0) Gecko/20100101 Firefox/90.0");
        userAgents.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:90.0) Gecko/20100101 Firefox/90.0");
        userAgents.add("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:90.0) Gecko/20100101 Firefox/90.0");
        userAgents.add("Mozilla/5.0 (iPhone; CPU iPhone OS 14_7 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.1 Mobile/15E148 Safari/604.1");
        userAgents.add("Mozilla/5.0 (iPad; CPU OS 14_7 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.1 Mobile/15E148 Safari/604.1");
    }
    public static Boolean is_white_black(URL url)
    {

        String paths = url.getProtocol()+"://"+url.getAuthority();
        String black_text = (String) Global_Config.get("BlackList");
        //判断黑名单
        if(!black_text.isEmpty()){
            String[] blackList = black_text.split(",");
            for (String wl : blackList) {
                if (paths.contains(wl)) { // 如果在黑名单里面
                    printDebug("【匹配到黑名单】path:" + paths + " 域名:" + wl );
                    return false;
                }
            }
        }

        //是否继续往下执行
        boolean b = false;
        Boolean  is_enable_white = (Boolean) Global_Config.get("WhiteEnable");
        if(is_enable_white) { // 判断是否启用白名单
            String white_text = (String) Global_Config.get("WhiteList");
            String[] whiteList = white_text.split(",");
            for(String wl:whiteList) {
                if(paths.contains(wl)) { // 如果在白名单里面
                    b = true;
                    break;
                }
            }
            if(!b) { // 如果不是白名单 那么退出
                printDebug("【不在白名单内】path:"+paths);
            }

        }else{
            b= true;
        }
        return b;
    }


    public static void Check_Vuln(int toolFlag, IHttpRequestResponse messageInfo, LogEntry finger_scan_result) throws MalformedURLException {
        //得到所有的poc
        IHttpService httpService = messageInfo.getHttpService();
        // 获取主机名
        String host = httpService.getHost();

        String finger;
        if(finger_scan_result!=null && finger_scan_result.finger_scan_result!=null){
            finger = finger_scan_result.finger_scan_result;
        }else{
            finger="";
        }
        List<PocEntry> match_poc_list = Poc.get_mathch_poc(finger);

        finger =finger.replaceAll("$$$",",");
        if(finger==","){
            finger="";
        }
        if(stop_poc_scan || match_poc_list.isEmpty()){
            return;
        }
        printDebug("【Host】"+host+" 【Poc num】"+match_poc_list.size());
        //url
        String temp_url = String.valueOf(helpers.analyzeRequest(messageInfo).getUrl());
        URL url  = new URL(temp_url);
        String path = url.getPath(); //获取的路径
        String paths = url.getProtocol()+"://"+url.getAuthority();
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
                    if (logList.contains(logsPahtTemp)) { // 判断是否包含在list中
                        continue; // 继续执行 存在历史 则不进行处理
                    }
                    logList.add(logsPahtTemp);
                    LogEntry temp = null;
                    if (Objects.equals(poc.plugin_type, "简单请求替换")) {
                        temp = Scan_Simple_HTTP_Request(messageInfo, toolFlag, list.get(urlLength), poc);
                    }else if (Objects.equals(poc.plugin_type, "高级HTTP请求")) {
                        temp = Scan_Advanced_HTTP_Request(messageInfo, toolFlag, list.get(urlLength), poc);
                    }else if (Objects.equals(poc.plugin_type, "Yaml Poc")) {
                        temp = Scan_Yaml_Poc(messageInfo, toolFlag, list.get(urlLength), poc);
                    }else if (Objects.equals(poc.plugin_type, "Python Poc")) {
                        temp = Scan_Python_Poc(messageInfo, toolFlag, list.get(urlLength), poc);
                    }
                    //添加结果
                    if (temp != null && !temp.vuln_scan_result.isEmpty()) {
                        printDebug("【"+poc.plugin_type+"】【Host】"+list.get(urlLength)+"【Poc Scan】"+poc.name+"【Result】"+temp.vuln_scan_result);
                        temp.finger_scan_result = finger;
                        temp.vuln_scan_result = poc.name;
                        model.addValueAt(temp);
                        model.fireTableDataChanged();
                    }else if(!finger.isEmpty()){
                        if (finger_scan_result != null) {
                            finger_scan_result.finger_scan_result = finger;
                            printDebug("【" + poc.plugin_type + "】【Host】" + list.get(urlLength) + "【Poc Scan】" + poc.name + "【Result】" + finger_scan_result.finger_scan_result);
                        }
                        model.addValueAt(finger_scan_result);
                        model.fireTableDataChanged();
                    }else{
                        printDebug("【"+poc.plugin_type+"】【Host】"+list.get(urlLength)+"【Poc Scan】"+poc.name+"【Result】False");
                    }
                }
            }

        }
    }
    public static String Get_Random_UserAgent() {
        Random random = new Random();
        int index = random.nextInt(userAgents.size());
        return userAgents.get(index);
    }

    public static LogEntry Scan_Advanced_HTTP_Request(IHttpRequestResponse messageInfo, int toolFlag,String dir_url,PocEntry poc) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);//通常使用的yaml格式
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);//标量格式
        Yaml yaml = new Yaml(options);
        Map<String, Object> yamlString = yaml.load(poc.plugins_data);
        String  requests_raw = (String) yamlString.get("requests_raw");
        Map<String, Object> expression = (Map<String,Object>) yamlString.get("expression");
        //得到所有的poc
        IHttpService httpService = messageInfo.getHttpService();
        // 获取主机名
        String host = httpService.getHost();
        requests_raw = requests_raw.replaceAll("\\{\\{Host\\}\\}",host);
        //getbody
        byte[] requestBytes = messageInfo.getRequest();
        // 分析请求以获取请求体的偏移量
        int bodyOffset = helpers.analyzeRequest(messageInfo).getBodyOffset();
        // 获取请求体
        byte[] bodyBytes = new byte[requestBytes.length - bodyOffset];
        System.arraycopy(requestBytes, bodyOffset, bodyBytes, 0, requestBytes.length - bodyOffset);
        String body =  new String(bodyBytes, StandardCharsets.UTF_8);
        requests_raw = requests_raw.replaceAll("\\{\\{Body\\}\\}",body);
        requests_raw = requests_raw.replaceAll("\\{\\{Random_UserAgent\\}\\}",Get_Random_UserAgent());
        //修改请求头
        List<String> source_headers = helpers.analyzeRequest(messageInfo).getHeaders();
        for (String item : source_headers) {
            String[] item_list = item.split(" ");
            if(item_list.length>1){
                String key = item_list[0];
                requests_raw = requests_raw.replaceAll("\\{\\{"+key+"\\}\\}",item_list[1]);
            }
        }

        // 分割原始请求字符串为头部和消息体
        String[] parts = requests_raw.split("\n\n", 2);
        if (parts.length==1){
            parts = requests_raw.split("\r\n\r\n", 2);
        }
        String headersPart = parts[0];
        String bodyPart = parts.length > 1 ? parts[1] : "";
        // 将头部字符串转换为List<String>
        List<String> httpHeaders = new ArrayList<>(Arrays.asList(headersPart.split("\r\n")));
        String headUrl = httpHeaders.get(0);
        //修改请求路径
        String[]  head_list = (headUrl.split(" "));
        if (head_list.length==3){
            head_list[1] =  dir_url+head_list[1];
            head_list[1] = headUrl.replaceAll("//", "/");
        }
        headUrl = Arrays.toString(head_list);
        httpHeaders.set(0,headUrl);
        // 如果消息体为空字符串，则不需要转换为byte[]
        byte[] new_bodyBytes = bodyPart.isEmpty() ? new byte[0] : bodyPart.getBytes();
        // 使用buildHttpMessage方法重建HTTP请求Headers:
        printDebug("【Scan_Advanced_HTTP_Request】【Request】\n"+ String.join("\n", httpHeaders) +"\n\n"+ new String(new_bodyBytes, StandardCharsets.UTF_8));
        byte[] newRequest = helpers.buildHttpMessage(httpHeaders, new_bodyBytes);
        IHttpService iHttpService = messageInfo.getHttpService();
        IHttpRequestResponse requestResponse = callbacks.makeHttpRequest(iHttpService, newRequest);//发送请求
//        printDebug("【Scan_Advanced_HTTP_Request】【Response】"+helpers.analyzeRequest(requestResponse).getUrl() + " " + String.valueOf(helpers.analyzeResponse(requestResponse.getResponse()).getStatusCode())+"\n");
        printDebug("【Scan_Advanced_HTTP_Request】【Response】\n"+ new String(requestResponse.getResponse())+"\n");
        String tool_name = callbacks.getToolName(toolFlag);
        Boolean vuln_scan_result = Match_poc_data(requestResponse,expression);
        if(vuln_scan_result){
            printMsg("【Scan_Advanced_HTTP_Request】【Match Success】URL:"+helpers.analyzeRequest(requestResponse).getUrl()+"【POC】"+poc.name+"【Match loaction】"+expression.get("scope")+"【Match Method】"+expression.get("match_method")+"【Match Value】"+expression.get("match_value"));
            return new LogEntry(count, tool_name, callbacks.saveBuffersToTempFiles(requestResponse), helpers.analyzeRequest(requestResponse).getUrl(), "", "", "", 0, (int) (helpers.analyzeResponse(requestResponse.getResponse()).getStatusCode()), "", poc.name);
        }
        return  null;
    }
    public static LogEntry Scan_Python_Poc(IHttpRequestResponse requestResponse, int toolFlag,String dir_url,PocEntry poc) throws MalformedURLException {
        IHttpService httpService = requestResponse.getHttpService();
        String host = httpService.getHost();
        String Protocol = httpService.getProtocol();
        String port = String.valueOf(httpService.getPort());
        String host_url =Protocol+"://"+ host+":"+port+"/"+dir_url;
        host_url =host_url.replaceAll("//","/");
        URL result_url =new URL(host_url);
        try{
            Poc_Request requests = Poc_Request.newBuilder()
                    .setPocType("Python")
                    .setUrl(host_url)
                    .setPoc((String) poc.plugins_data)
                    .build();
            Poc_Response response = client.pocscan(requests);
            printDebug("【Scan_Python_Poc】【Request】"+poc.name);
            if (response.getIsSuccess()) {
                IHttpRequestResponse result_rr = new IHttpRequestResponse() {
                    @Override
                    public byte[] getRequest() {
                        return ("METHOD " + dir_url + " HTTP/1.1\n" + poc.plugins_data).getBytes(StandardCharsets.UTF_8);
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
                String tool_name = callbacks.getToolName(toolFlag);
                printMsg("【Scan_Python_Poc】【Match Success】URL:"+result_url+"【POC】"+poc.name+"【Response_result】"+response.getResult());
                return new LogEntry(count, tool_name, callbacks.saveBuffersToTempFiles(result_rr), result_url, "", "", "", 0,0, "", poc.name);
            }
            printDebug("【Scan_Python_Poc】【Response】【"+poc.name+"】【"+result_url+"】"+ false +" "+response.getResult());
        }catch (Exception ex){
            printErr(Arrays.toString(ex.getStackTrace()));
            printErr(ex.getMessage());
        }
        return  null;
    }
    public static LogEntry Scan_Yaml_Poc(IHttpRequestResponse requestResponse, int toolFlag,String dir_url,PocEntry poc) throws MalformedURLException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);//通常使用的yaml格式
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);//标量格式
        Yaml yaml = new Yaml(options);
        Map<String, Object> yamlString = yaml.load(poc.plugins_data);
        Map<String,Map> rules = (Map<String,Map>) yamlString.get("rules");
        try{
            for (Map.Entry<String, Map> ruleEntry : rules.entrySet()) {
                Map<String,Map> ruleDetails = ruleEntry.getValue();
                Map<String,String> requests = ruleDetails.get("request");
                String path = (String) requests.get("path");
                String replace_path="";
                if(dir_url!="/"){
                    replace_path = dir_url+path;
                    replace_path = replace_path.replaceAll("//","/");
                }else {
                    replace_path = path;
                }
                poc.plugins_data = poc.plugins_data.replaceAll(path,replace_path);
            }
        }catch (Exception ex){
            printErr("Yaml Replace String Fail "+Arrays.toString(ex.getStackTrace()));
        }
        IHttpService httpService = requestResponse.getHttpService();
        String host = httpService.getHost();
        String Protocol = httpService.getProtocol();
        String port = String.valueOf(httpService.getPort());
        String host_url =Protocol+"://"+ host+":"+port;
        String aaa_url = Protocol+"://"+(host+":"+port+"/"+dir_url).replaceAll("//","/");
        URL result_url =new URL(aaa_url);
        try{
            Poc_Request requests = Poc_Request.newBuilder()
                    .setPocType("Yaml")
                    .setUrl(host_url)
                    .setPoc((String) poc.plugins_data)
                    .build();
            Poc_Response response = client.pocscan(requests);
            printDebug("【Scan_Yaml_Poc】【Request】"+poc.name);
//        System.out.println("Response: " + response.getIsSuccess() + ", " + response.getResult());
            if (response.getIsSuccess()) {
                String tool_name = callbacks.getToolName(toolFlag);
                IHttpRequestResponse result_rr =new IHttpRequestResponse() {
                    @Override
                    public byte[] getRequest() {
                        return  ("METHOD "+dir_url+" HTTP/1.1\n"+poc.plugins_data).getBytes(StandardCharsets.UTF_8);
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
//                result_rr.setRequest(poc.plugins_data.getBytes());
//                result_rr.setResponse(("【return flag】"+response.getIsSuccess()+"\n【return result】"+response.getResult()).getBytes());
                printMsg("【Scan_Yaml_Poc】【Match Success】URL:"+result_url+"【POC】"+poc.name+"【Response_result】"+response.getResult());
                return new LogEntry(count, tool_name, callbacks.saveBuffersToTempFiles(result_rr), result_url, "", "", "", 0, 0, "", poc.name);
            }
            printDebug("【Scan_Yaml_Poc】【Response】【"+poc.name+"】【"+result_url+"】"+ false +" "+response.getResult());
//        channel.shutdownNow();
        }catch (Exception ex){
            printErr(Arrays.toString(ex.getStackTrace()));
            printErr(ex.getMessage());
        }
        return  null;
    }

    public static LogEntry Scan_Simple_HTTP_Request(IHttpRequestResponse messageInfo, int toolFlag,String dir_url,PocEntry poc) {
        //遍历目录 对每个poc
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);//通常使用的yaml格式
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);//标量格式
        Yaml yaml = new Yaml(options);
        Map<String, Object> yamlString = yaml.load(poc.plugins_data);
        Map<String,Object> requests = (Map<String,Object>) yamlString.get("requests");
        Map<String, Object> expression = (Map<String,Object>) yamlString.get("expression");
        String url  = (String) requests.get("url");
        String method  = (String) requests.get("method");
        Map<String,String> headers  = (Map<String, String>) requests.get("headers");
        List<String> http_headers_info  = Set_Header_Requests(messageInfo,method,dir_url+"/"+url,headers);
        String body  = (String) requests.get("body");
        byte[] bodyBytes = "".getBytes();
        if(body==null ){
            byte[] requestBytes = messageInfo.getRequest();
            // 分析请求以获取请求体的偏移量
            int bodyOffset = helpers.analyzeRequest(messageInfo).getBodyOffset();
            // 获取请求体
            bodyBytes = new byte[requestBytes.length - bodyOffset];
            System.arraycopy(requestBytes, bodyOffset, bodyBytes, 0, requestBytes.length - bodyOffset);
        }
        printDebug("【Scan_Simple_HTTP_Request】【Request】\n"+ String.join("\n", http_headers_info) +"\n\n"+ new String(bodyBytes, StandardCharsets.UTF_8));
        byte[] new_Requests = helpers.buildHttpMessage(http_headers_info, bodyBytes); //关键方法
        IHttpService iHttpService = messageInfo.getHttpService();
        IHttpRequestResponse requestResponse = callbacks.makeHttpRequest(iHttpService, new_Requests);//发送请求
        printDebug("【Scan_Simple_HTTP_Request】【Response】\n"+ new String(requestResponse.getResponse())+"\n");
//        printDebug("【Scan_Simple_HTTP_Request】"+helpers.analyzeRequest(requestResponse).getUrl() + " " + String.valueOf(helpers.analyzeResponse(requestResponse.getResponse()).getStatusCode())+"\n");
        String tool_name = callbacks.getToolName(toolFlag);
        Boolean vuln_scan_result = Match_poc_data(requestResponse,expression);
        if(vuln_scan_result){
            printMsg("【Scan_Simple_HTTP_Request】【Match Success】URL:"+helpers.analyzeRequest(requestResponse).getUrl()+"【POC】"+poc.name+"【Match loaction】"+expression.get("scope")+"【Match Method】"+expression.get("match_method")+"【Match Value】"+expression.get("match_value"));
            return new LogEntry(count, tool_name, callbacks.saveBuffersToTempFiles(requestResponse), helpers.analyzeRequest(requestResponse).getUrl(), "", "", "", 0, (int) (helpers.analyzeResponse(requestResponse.getResponse()).getStatusCode()), "", poc.name);
        }
        return null;
    }
    public static List<String> Set_Header_Requests(IHttpRequestResponse requestResponse, String method,String url,Map<String,String> headers){
        String source_method = helpers.analyzeRequest(requestResponse).getMethod(); // 获取提交方法
        if( method ==null || method.isEmpty()){
            method=source_method;
        }
        List<String> source_headers = helpers.analyzeRequest(requestResponse).getHeaders();
        String headUrl = source_headers.get(0);
        List<String> keyValueStrings;
        if(headers !=null && !headers.isEmpty()){
            source_headers.subList(1, source_headers.size());
            //将heads转换为key、value的形式
            Map<String,String> Source_heads_map  = new HashMap<>();
            for (String item : source_headers) {
                String[] item_list = item.split(" ");
                String key = item_list[0];
                String value="";
                if(item_list.length>1){
                    value = item_list[1];
                }
                Source_heads_map.put(key,value);
            }
            Source_heads_map.putAll(headers);
            Source_heads_map.put("Cookie",Source_heads_map.get("Cookie").replaceAll("\\{\\{Random_UserAgent\\}\\}",Get_Random_UserAgent()));

            // 遍历 Map 的键值对
            keyValueStrings = new ArrayList<>();
            for (Map.Entry<String, String> entry : Source_heads_map.entrySet()) {
                // 将键和值转换为字符串，并用空格分隔
                String keyValueString = entry.getKey() + " " + entry.getValue();
                keyValueStrings.add(keyValueString);
            }
        }else{
            keyValueStrings = source_headers;
        }
        String[] e = headUrl.split(" ");
        url = url.replaceAll("//","/");
        String result_headUrl = method.toUpperCase()+" "+url+" "+e[2];
        keyValueStrings.set(0,result_headUrl);
        return  keyValueStrings;
    }

    public static Boolean Match_poc_data(IHttpRequestResponse requestResponse, Map<String,Object> poc){
        if((int)poc.get("status_code") !=0  && (helpers.analyzeResponse(requestResponse.getResponse()).getStatusCode())!=(int)poc.get("status_code")){
            return  false;
        }
        boolean scan_result = false;
        String match_source_data = "";
        if(Objects.equals(poc.get("scope"), "response")){
            String response_data = new String(requestResponse.getResponse());
            match_source_data = response_data;
        }else if (Objects.equals(poc.get("scope"), "response header")){
            String response_headers = helpers.analyzeResponse(requestResponse.getResponse()).getHeaders().toString();
            match_source_data = response_headers;
        }else if (Objects.equals(poc.get("scope"), "response body")){
            String response_data = new String(requestResponse.getResponse());
            IResponseInfo analyzeResponse = helpers.analyzeResponse(requestResponse.getResponse());
            int bodyOffset = analyzeResponse.getBodyOffset();
            String response_body = response_data.substring(bodyOffset);
            match_source_data = response_body;
        }else if (Objects.equals(poc.get("scope"), "response title")){
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
        //开始进行匹配   ”Contain","Equal", "Regex"}
        if(!Objects.equals(poc.get("match_method"), "") && !Objects.equals(poc.get("match_value"), "") && !Objects.equals(match_source_data, "")){
            if(Objects.equals(poc.get("match_method"), "Contain") && match_source_data.contains((String) poc.get("match_value"))) {
                scan_result = true;
            }else if(Objects.equals(poc.get("match_method"), "Equal") && match_source_data.equals(poc.get("match_value"))) {
                scan_result = true;
            }else if(Objects.equals(poc.get("match_method"), "Regex")) {
                try{
                    if (!match_source_data.isEmpty()){
                        Pattern pattern = Pattern.compile((String) poc.get("match_value"));
                        Matcher matcher = pattern.matcher(match_source_data);
                        // 查找匹配项
                        if (matcher.find()) {
                            scan_result = true;
                        }
                    }
                }catch (Exception ex){
                    printErr(Arrays.toString(ex.getStackTrace()));
                }
            }

        }
        return scan_result;
    }

    /**
     * 统计某个字符串在指定字符串中出现的个数
     */
    public static List<String> countString(String str, String s) {
//        int count = 0,len = str.length();
        List<String> list = new ArrayList<>();
        String ss = str;
        while(str.contains(s)) {
            str = str.substring(str.indexOf(s) + 1);
            list.add(ss.replace(str, s).replaceAll("//","/"));
            count++;
        }
        Collections.reverse(list);
        return list;
    }


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
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    IHttpService httpService = messageInfo.getHttpService();
                    // 获取主机名
                    String host = httpService.getHost();
                    printDebug("Start Poc Scan Host:"+host);
                    Scan.Check_Vuln(toolFlag,messageInfo,null);
                } catch (Exception ex) {
                    printErr(ex.getMessage());
                    printErr(Arrays.toString(ex.getStackTrace()));
                }
            }
        });
        thread.start();
    }

    public static void finger_scan(int toolFlag, IHttpRequestResponse messageInfo,Boolean enable_poc){
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    IHttpService httpService = messageInfo.getHttpService();
                    // 获取主机名
                    String host = httpService.getHost();
                    printDebug("Start Finger Scan Host:"+host);
                    LogEntry finger_info = Scan.Check_Finger(toolFlag, messageInfo);
                    if (finger_info != null && !finger_info.finger_scan_result.isEmpty()) {
                        if (enable_poc && switchs_poc) {
                            printDebug("Start Poc Scan Host:" + host);
                            Scan.Check_Vuln(toolFlag, messageInfo, finger_info);
                        } else {
                            finger_info.finger_scan_result = finger_info.finger_scan_result.replaceAll("$$$", ",");
                            model.addValueAt(finger_info);
                            model.fireTableDataChanged();
                        }
                    }
                } catch (Exception ex) {
                    printErr(ex.getMessage());
                    printErr(Arrays.toString(ex.getStackTrace()));
                }
            }
        });
        thread.start();
    }

    public static String get_host_ico(String host_url,IHttpRequestResponse requestResponse) throws IOException {
        String temp_url = String.valueOf(helpers.analyzeRequest(requestResponse).getUrl());
        URL url  = new URL(temp_url);
        String path = url.getPath(); //获取的路径
        List<String> list = countString(path,"/"); // 获取路径中所有路径的组合
        //得到返回body
        String response_data = new String(requestResponse.getResponse());
        IResponseInfo analyzeResponse = helpers.analyzeResponse(requestResponse.getResponse());
        int bodyOffset = analyzeResponse.getBodyOffset();
        String response_body = response_data.substring(bodyOffset);
        String iconpath="";

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
                urlpath = urlpath+"/favicon.ico";
            }
            String url_dir= (host_url+urlpath);
            url_dir =url_dir.replaceAll("//","/");
            String requests_url= url.getProtocol()+"://"+url_dir;
            String http_response =  send_http_get(requests_url);
            try{
                if (!http_response.isEmpty()){
                    int murmu = Hashing
                            .murmur3_32().hashString(http_response.replaceAll("\r","" )+"\n", StandardCharsets.UTF_8)
                            .asInt();
                    return  String.valueOf(murmu);
                }
            }catch (Exception ex){
                printErr(Arrays.toString(ex.getStackTrace()));
            }
        }
        return "";
    }
    public static String send_http_get(String urlpath) throws IOException {
        // 创建URL对象
        URL url = new URL(urlpath);
        // 打开连接
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // 设置请求方法为GET
        connection.setRequestMethod("GET");
        // 设置是否允许输入输出流
        connection.setDoInput(true);
        connection.setDoOutput(false);
        int responseCode = 0;
        try{
             responseCode = connection.getResponseCode();
        }catch (Exception ex) {
            printErr("send_http_get Error:"+urlpath+" "+ex.getMessage());
            return  "";
        }
        StringBuilder content = new StringBuilder("");
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // 读取响应
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
             content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
        }
        connection.disconnect();
        return content.toString();
    }
    public static LogEntry Check_Finger(int toolFlag, IHttpRequestResponse messageInfo) throws IOException {
        List<FingerEntry> all_finger = model_finger.getAllValue();
        if(stop_finger_scan || all_finger.isEmpty()){
            return  null;
        }
        IHttpService httpService = messageInfo.getHttpService();
        // 获取主机名
        String protocol = httpService.getProtocol();

        String host = httpService.getHost();
        String port = String.valueOf(httpService.getPort());
        String host_url = host+":"+port;
        if(finger_ico_hash_map.isEmpty() || finger_ico_hash_map.get(host)==null || Objects.equals((String) finger_ico_hash_map.get(host), "")){
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
                mathc_result = Match_keyword_finger(toolFlag,messageInfo,item);

            } else if (Objects.equals(item.method, "faviconhash")) {
                mathc_result = Match_faviconhash_finger(toolFlag,messageInfo,item,host);
            }
            if (mathc_result!=null && !mathc_result.isEmpty()){
                all_finger_result.add(mathc_result);
            }
        }
        String tool_name = callbacks.getToolName(toolFlag);
        String enable_poc_str = String.join("$$$", all_finger_result);
        printDebug("【Match End】"+helpers.analyzeRequest(messageInfo).getUrl()+"【Enable_poc_str】"+enable_poc_str);
        if (!enable_poc_str.isEmpty()){
            return new LogEntry(count, tool_name, callbacks.saveBuffersToTempFiles(messageInfo), helpers.analyzeRequest(messageInfo).getUrl(), "", "", "", 0, (int) (helpers.analyzeResponse(messageInfo.getResponse()).getStatusCode()), enable_poc_str, "");
        }else{
            return  null;
        }
//        Main_Vuln.count++;
//        String tool_name = callbacks.getToolName(toolFlag);
//        LogEntry result_Data = new LogEntry(Main_Vuln.count, tool_name, callbacks.saveBuffersToTempFiles(messageInfo), helpers.analyzeRequest(messageInfo).getUrl(), "", "", "", 0, (int)(helpers.analyzeResponse(messageInfo.getResponse()).getStatusCode()), "","");
//        return result_Data;
    }
    public static String Match_keyword_finger(int toolFlag, IHttpRequestResponse requestResponse, FingerEntry finger) throws IOException {
        String match_source_data = "";
        if(Objects.equals(finger.location, "body")){
            String response_data = new String(requestResponse.getResponse());
            IResponseInfo analyzeResponse = helpers.analyzeResponse(requestResponse.getResponse());
            int bodyOffset = analyzeResponse.getBodyOffset();
            String response_body = response_data.substring(bodyOffset);
            match_source_data = response_body;
        }else if (Objects.equals(finger.location, "header")){
            String response_headers = helpers.analyzeResponse(requestResponse.getResponse()).getHeaders().toString();
            match_source_data = response_headers;
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
                    is_success =  "";
                }
            }
            if(!is_success.isEmpty()){
                printDebug("【Match】"+helpers.analyzeRequest(requestResponse).getUrl()+"【Finger】"+finger.cms+"【Keyword】"+keyword_list+"【Success】");
            }else{
                printDebug("【Match】"+helpers.analyzeRequest(requestResponse).getUrl()+"【Finger】"+finger.cms+"【Keyword】"+finger.keyword+"【Fail】");
            }
            return is_success;
//            for(String poc :keyword_list){
//                if(match_source_data.contains(poc)){
//                    printDebug("【Match】"+helpers.analyzeRequest(requestResponse).getUrl()+"【Finger】"+finger.cms+"【Keyword】"+keyword_list+"【Success】");
//                    return  finger.cms;
//                }
//            }
        } else if ( match_source_data.contains(finger.keyword)) {
            printDebug("【Match】"+helpers.analyzeRequest(requestResponse).getUrl()+"【Finger】"+finger.cms+"【Keyword】"+finger.keyword+"【Success】");
            return  finger.cms;
        }
//        printDebug("【Match】"+helpers.analyzeRequest(requestResponse).getUrl()+"【Finger】"+finger.cms+"【Keyword】"+finger.keyword+"【Fail】");
        return  "";
    }
    public static String Match_faviconhash_finger(int toolFlag, IHttpRequestResponse requestResponse, FingerEntry finger, String host) throws IOException {
        if(finger_ico_hash_map.get(host)!=null && finger_ico_hash_map.get(host)!="") {
            String tool_name = callbacks.getToolName(toolFlag);
            List<String> keyword_list=null;
            String icon_hash = (String) finger_ico_hash_map.get(host);
            try{
                Gson gson = new Gson();
                Type listType = new TypeToken<List<String>>(){}.getType();
                keyword_list = gson.fromJson(finger.keyword, listType);
            }catch (Exception ex){
                printErr(finger.cms+" "+finger.keyword+"Conversion to JSON error:"+ex.getMessage());
            }
            if(keyword_list !=null){
                for(String poc :keyword_list){
                    if(icon_hash==poc){
                        printDebug("【Match】"+helpers.analyzeRequest(requestResponse).getUrl()+"【Finger】"+finger.cms+"【Keyword】"+finger.keyword+" Success");
                        return  finger.cms;
                    }
                }
            } else if ( icon_hash==finger.keyword) {
                printDebug("【Match】"+helpers.analyzeRequest(requestResponse).getUrl()+"【Finger】"+finger.cms+"【Keyword】"+finger.keyword+" Success");
                return  finger.cms;
            }
        }
        printDebug("【Match】"+helpers.analyzeRequest(requestResponse).getUrl()+"【Finger】"+finger.cms+"【Keyword】"+finger.keyword+"【Fail】");
        return  "";
    }
}
