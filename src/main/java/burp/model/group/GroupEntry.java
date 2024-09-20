package burp.model.group;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GroupEntry {
    public int id;
    public String name;
    public String type;
    public boolean is_finger_poc;
    public boolean is_finger_jump_poc;

    // 无参构造函数
    public GroupEntry() {}

    // 有参构造函数
    public GroupEntry(int id, String name, String type, boolean is_finger_poc, boolean is_finger_jump_poc) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.is_finger_poc = is_finger_poc;
        this.is_finger_jump_poc = is_finger_jump_poc;
    }

    // Getter and Setter methods
    @JsonProperty("id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("is_finger_poc")
    public boolean getIsFingerPoc() {
        return is_finger_poc;
    }

    public void setIsFingerPoc(boolean is_finger_poc) {
        this.is_finger_poc = is_finger_poc;
    }

    @JsonProperty("is_finger_jump_poc")
    public boolean getIsFingerJumpPoc() {
        return is_finger_jump_poc;
    }

    public void setIsFingerJumpPoc(boolean is_finger_jump_poc) {
        this.is_finger_jump_poc = is_finger_jump_poc;
    }

    @Override
    public String toString() {
        return "GroupEntry{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", is_finger_poc=" + is_finger_poc +
                ", is_finger_jump_poc=" + is_finger_jump_poc +
                '}';
    }
}