package burp.model.finger;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FingerEntry {
    public int id;
    public String cms;
    public String method;
    public String location;
    public String keyword;
    public int isImportant;
    public String type;

    // 无参构造函数
    public FingerEntry() {}

    // 有参构造函数
    public FingerEntry(int id, String cms, String method, String location, String keyword, int isImportant, String type) {
        this.id = id;
        this.cms = cms;
        this.method = method;
        this.location = location;
        this.keyword = keyword;
        this.isImportant = isImportant;
        this.type = type;
    }

    // Getter and Setter methods
    @JsonProperty("id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @JsonProperty("cms")
    public String getCms() {
        return cms;
    }

    public void setCms(String cms) {
        this.cms = cms;
    }

    @JsonProperty("method")
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @JsonProperty("location")
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @JsonProperty("keyword")
    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    @JsonProperty("isImportant")
    public int getIsImportant() {
        return isImportant;
    }

    public void setIsImportant(int isImportant) {
        this.isImportant = isImportant;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "FingerEntry{" +
                "id=" + id +
                ", cms='" + cms + '\'' +
                ", method='" + method + '\'' +
                ", location='" + location + '\'' +
                ", keyword='" + keyword + '\'' +
                ", isImportant=" + isImportant +
                ", type='" + type + '\'' +
                '}';
    }
}