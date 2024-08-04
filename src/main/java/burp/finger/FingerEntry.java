package burp.finger;

public class FingerEntry
{
    public final int id;
    public String cms ;

    public String method ;

    public String location ;
    public String keyword;
    public int isImportant;
    public String type;

    public FingerEntry(int id, String cms, String method, String location, String keyword, int isImportant, String type)
    {
        this.id = id;
        this.cms = cms;
        this.method = method;
        this.location = location;
        this.keyword =  keyword;
        this.isImportant = isImportant;
        this.type = type;
    }
}