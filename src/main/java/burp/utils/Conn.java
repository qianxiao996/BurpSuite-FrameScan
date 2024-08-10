package burp.utils;

import burp.IHttpRequestResponse;
import burp.IHttpService;
import burp.rpc.Poc_Request;
import burp.rpc.Poc_Response;
import io.grpc.StatusRuntimeException;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static burp.BurpExtender.helpers;
import static burp.Main_Vuln.*;

public class Conn {
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
    public static String Get_Random_UserAgent() {
        Random random = new Random();
        int index = random.nextInt(userAgents.size());
        return userAgents.get(index);
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
    //得到请求方法 路径 HTTP版本
    public static String[] get_requests_head_line(IHttpRequestResponse messageInfo) {
        String[] return_result = new String[3];
        String headersPart = new String(messageInfo.getRequest(), StandardCharsets.UTF_8);
        List<String> httpHeaders = new ArrayList<>(Arrays.asList(headersPart.split("\r\n")));
        if(httpHeaders.isEmpty()){
            return null;
        }
        String headUrl = httpHeaders.get(0);
        String[] head_list = headUrl.split(" ");
        if(head_list.length>=3){
            return  head_list;
        } else if (head_list.length==2) {
            return_result[0] = head_list[0];
            return_result[1] = head_list[1];
            return_result[2] ="HTTP/1.1";
        } else if (head_list.length==1) {
            return_result[0] = head_list[0];
            return_result[1] = "/";
            return_result[2] ="HTTP/1.1";
        }else{
            return_result[0] = "GET";
            return_result[1] = "/";
            return_result[2] ="HTTP/1.1";
        }
        return  return_result;
    }
    public static Map<String,Object> get_requests_response_head_body(byte[] req_resp_byte) {
        Map<String,Object> return_sult = new HashMap<>();
        String headersPart = new String(req_resp_byte, StandardCharsets.UTF_8);
        String[] http_body_head = headersPart.split("\\R\\R");
        List<String> header_list = Arrays.asList(http_body_head[0].split("\\R"));
        return_sult.put("header",header_list);
        if(http_body_head.length>=2){
            return_sult.put("body",http_body_head[1]);
        }else{
            return_sult.put("body","");
        }
        return return_sult;
    }

    public static int GetMessageStatusCode(IHttpRequestResponse requestResponse){
        if(is_burp){
            return helpers.analyzeResponse(requestResponse.getResponse()).getStatusCode();
        }else{

            String response = new String(requestResponse.getResponse());
            String[] response_ =response.split("\\R");
            String head_line_str= response_[0];
            if(head_line_str.startsWith("HTTP/")){
                String[] head_line_list = head_line_str.split(" ");
                if(head_line_list.length>=2){
                    return Integer.parseInt(head_line_list[1]);
                }else{
                    return -1;
                }
            }else{
                return  -1;
            }
        }
    }
    public static String get_requests_url(IHttpRequestResponse messageInfo,boolean  is_path){
        IHttpService iHttpService = messageInfo.getHttpService();
        String host_url;
        if(iHttpService.getPort()!=-1){
            host_url = iHttpService.getProtocol()+"://"+iHttpService.getHost()+":"+iHttpService.getPort();
        }else{
            host_url = iHttpService.getProtocol()+"://"+iHttpService.getHost();
        }
        if(is_path){
            String[] temp = get_requests_head_line(messageInfo);
            if (temp != null) {
                host_url =host_url +temp[1];
            }
        }
        return host_url;
    }

    public static Poc_Response Grpc_Send(Poc_Request  requests){
//        Poc_Response response = client.pocscan(requests);
        try {
            return client.pocscan(requests);
        } catch (StatusRuntimeException e) {
            printErr("RPC failed: "+e.getStatus());
            return  null;
        }
    }

}
