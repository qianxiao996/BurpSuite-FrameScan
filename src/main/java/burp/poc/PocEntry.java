package burp.poc;

public class PocEntry
{
    public int id;
    public String type ;

    public String name ;

//    public String url ;
//    public String scope;
//    public String match_method;
//    public String match_value;
//    public int status_code;
    public String group;
    public int dir_count;
    public String plugin_type;
    public String plugins_data;
    public String description;


    public PocEntry(int id, String group,String  type  ,String name,int dir_count,String plugin_type, String plugins_data,String description)
    {
        this.id = id;
        this.group = group;
        this.type = type;
        this.name = name;
        this.dir_count = dir_count;
        this.plugin_type = plugin_type;
        this.plugins_data = plugins_data;
        this.description = description;
    }
}