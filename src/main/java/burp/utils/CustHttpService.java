package burp.utils;

import burp.IHttpRequestResponse;
import burp.IHttpService;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static burp.Main_Vuln.printErr;

public class CustHttpService {
    public static IHttpRequestResponse GetHttpRequestResponse(String url_str,List<String> req_headers, String req_body) {
        try {
            List<String> all_headers = new ArrayList<>();
            if(req_body==null){
                req_body="";
            }
            String requests_raw;
            String req_method="GET";
            String head_str = req_headers.get(0);
            String[] heads_list=new String[3];
            if(head_str.contains("HTTP")){
                String[]  temp_head_list = head_str.split(" ");
                if(temp_head_list.length>=3){
                    heads_list[0] = temp_head_list[0];
                    heads_list[1] = temp_head_list[1];
                    heads_list[2] = temp_head_list[2];
                }else  if(temp_head_list.length==2){
                    heads_list[0] = temp_head_list[0];
                    heads_list[1] = temp_head_list[1];
                    heads_list[2] = "HTTP/1.1";
                } else if (temp_head_list.length==1) {
                    heads_list[1] ="/";
                    heads_list[2]="HTTP/1.1";
                }
                try{
                    all_headers =  req_headers.subList(1, req_headers.size());
                }catch (Exception ex){
                    printErr(ex.getMessage());
                    printErr(Arrays.toString(ex.getStackTrace()));
                }
                req_method=heads_list[0];
            }else{
                heads_list[0] ="GET";
                heads_list[1] ="/";
                heads_list[2]="HTTP/1.1";
            }
            if(all_headers.isEmpty()){
                requests_raw = req_method+" "+heads_list[1]+" "+heads_list[2]+"\r\n\r\n"+req_body;
            }else{
                requests_raw = req_method+" "+heads_list[1]+" "+heads_list[2]+"\r\n"+String.join("\r\n", all_headers)+"\r\n\r\n"+req_body;
            }

            // 初始化一个信任所有证书的 SSLContext
//            SSLContext sslContext = createUnsafeSSLContext();
//            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            // 创建 URL 对象
            URL url = new URL(url_str);

            // 打开连接
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // 设置请求方法为 POST
            connection.setRequestMethod(req_method);
            // 设置允许输出
            connection.setDoOutput(true);
            // 设置请求头
            for (String single_heade :all_headers ){
                String[] temp_header = single_heade.split(":");
                if(temp_header.length==2){
                    connection.setRequestProperty(temp_header[0],temp_header[1]);
                } else if (temp_header.length==1) {
                    connection.setRequestProperty(temp_header[0],"");
                }
            }
//            String requests_raw = req_method+" "+url.getPath()+" HTTP/1.1\r\n"+String.join("\r\n", req_headers);;
            // 写入请求体
            try (OutputStream out = connection.getOutputStream()) {
                out.write(req_body.getBytes(StandardCharsets.UTF_8));
            }
            // 获取响应状态码
//            int responseCode = connection.getResponseCode();
            // 读取响应
            List<String> response_header = new ArrayList<>();
            for (String key : connection.getHeaderFields().keySet()) {
//                if(!Objects.equals(key, "Accept-Ranges")) {
                    if (key == null) {
                        if(connection.getHeaderField(null).startsWith("HTTP/")){
                            response_header.add(0, connection.getHeaderField(null));
                        }else{
                            response_header.add(connection.getHeaderField(null));
                        }
                    } else {
                        response_header.add(key + ": " + connection.getHeaderField(key));
                    }
//                }
            }
            String  response_header_str = String.join("\r\n",response_header);
            StringBuilder response_body = new StringBuilder();
            try (InputStream in = connection.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String bodyLine;
                while ((bodyLine = reader.readLine()) != null) {
                    response_body.append(bodyLine);
                }
            }catch (Exception  e)
            {
                if(connection.getResponseCode()==HttpURLConnection.HTTP_NOT_FOUND){

                }else{
                    printErr(e.getMessage());
                    printErr(Arrays.toString(e.getStackTrace()));
                }

            }
            // 关闭连接
            connection.disconnect();
            return new IHttpRequestResponse() {
                @Override
                public byte[] getRequest() {
                    return requests_raw.getBytes();
                }

                @Override
                public void setRequest(byte[] bytes) {

                }

                @Override
                public byte[] getResponse() {
                    return (response_header_str+"\r\n\r\n"+response_body).getBytes();
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
                    return new IHttpService() {
                        @Override
                        public String getHost() {
                            return url.getHost();
                        }

                        @Override
                        public int getPort() {
                            //                            if (port == -1) {
//                                if(Objects.equals(url.getProtocol(), "https")){
//                                    port=443;
//                                }else{
//                                    port=80;
//                                }
//                            }
                            return url.getPort();
                        }

                        @Override
                        public String getProtocol() {
                            return url.getProtocol();
                        }
                    };
                }

                @Override
                public void setHttpService(IHttpService iHttpService) {

                }
            };
        } catch (IOException e) {
            printErr(Arrays.toString(e.getStackTrace()));
            printErr(e.getMessage());
        }
        return null;
    }
    /**
     * 创建一个信任所有证书的 SSLContext。
     *
     * @return SSLContext
     * @throws NoSuchAlgorithmException 如果算法不存在
     * @throws KeyManagementException 如果密钥管理器初始化失败
     */
    private static SSLContext createUnsafeSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
        // 创建一个信任所有证书的信任管理器
        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
                // 信任所有客户端证书
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
                // 信任所有服务器证书
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        }};

        // 使用信任所有证书的信任管理器初始化 SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        return sslContext;
    }
    public static String get_host_ico_http_requests(String urlpath) throws IOException, NoSuchAlgorithmException, KeyManagementException {


        // 初始化一个信任所有证书的 SSLContext
        SSLContext sslContext = createUnsafeSSLContext();
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        // 创建URL对象
        URL url = new URL(urlpath);
        // 打开连接
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // 设置请求方法为GET
        connection.setRequestMethod("GET");
        // 设置是否允许输入输出流
        connection.setDoInput(true);
        connection.setDoOutput(false);
//        int responseCode ;
//        try{
//            responseCode = connection.getResponseCode();
//        }catch (Exception ex) {
//            printErr("send_http_get Error:"+urlpath+" "+ex.getMessage());
//            return  "";
//        }
        StringBuilder content = new StringBuilder();
        if(connection.getResponseCode()==HttpURLConnection.HTTP_OK){
            // 读取响应
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            connection.disconnect();
        }else{
            return "";
        }
        return content.toString();
    }
}