package burp;

import java.net.URL;

public class PocEntry
{
    final int id;
    String name ;

    String url ;
    String regex ;
    String scope;


    PocEntry(int id, String name, String url, String scope, String regex)
    {
        this.id = id;
        this.name = name;
        this.url = url;
        this.scope = scope;
        this.regex = regex;

    }



}