package burp;

import java.net.URL;

public class PocEntry
{
    final int id;
    String name ;

    String url ;
    String regex ;
    String scope;
    String match_method;
    String match_value;
    int status_code;

    PocEntry(int id, String name, String url, String scope,String match_method, String match_value,int status_code)
    {
        this.id = id;
        this.name = name;
        this.url = url;
        this.scope = scope;
        this.match_method = match_method;
        this.match_value = match_value;
        this.status_code = status_code;
    }
}