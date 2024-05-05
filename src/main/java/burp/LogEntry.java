package burp;

import java.net.URL;

public class LogEntry
{
    final int id;
    final int tool;
    final IHttpRequestResponsePersisted requestResponse;
    final URL url;
    final String parameter;
    final String value;
    final String data_md5;
    final int times;
    final int response_code;

    final String scan_result;


    LogEntry(int id, int tool, IHttpRequestResponsePersisted requestResponse, URL url, String parameter, String value, String data_md5, int times, Integer response_code, String scan_result)
    {
        this.id = id;
        this.tool = tool;
        this.requestResponse = requestResponse;
        this.url = url;
        this.parameter = parameter;
        this.value = value;
        this.data_md5 = data_md5;
        this.times = times;
        this.response_code = response_code;
        this.scan_result = scan_result;
    }
}