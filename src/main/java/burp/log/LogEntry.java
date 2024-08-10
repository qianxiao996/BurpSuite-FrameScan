package burp.log;
import burp.IHttpRequestResponse;

import java.net.URL;

public class LogEntry
{
    public final int id;
    final String tool;
//    public final IHttpRequestResponsePersisted  requestResponse;
    public final IHttpRequestResponse  requestResponse;
    final URL url;
    final String parameter;
    final String value;
    final String data_md5;
    final int times;
    final int response_code;
    public String finger_scan_result;
    public String vuln_scan_result;

    public int vuln_poc_id;

    public LogEntry(int id, String tool, IHttpRequestResponse requestResponse, URL url, String parameter, String value, String data_md5, int times, Integer response_code, String finger_scan_result, String vuln_scan_result,int vuln_poc_id)
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
        this.finger_scan_result = finger_scan_result;
        this.vuln_scan_result = vuln_scan_result;
        this.vuln_poc_id = vuln_poc_id;
    }
}