package burp.model.poc;

public class PocEntry {
    public int id;
    public String type;
    public String name;
    public String group;
    public int dir_count;
    public String plugin_type;
    public String plugins_data;
    public String description;

    // 无参构造函数
    public PocEntry() {}

    // 有参构造函数
    public PocEntry(int id, String group, String type, String name, int dir_count, String plugin_type, String plugins_data, String description) {
        this.id = id;
        this.group = group;
        this.type = type;
        this.name = name;
        this.dir_count = dir_count;
        this.plugin_type = plugin_type;
        this.plugins_data = plugins_data;
        this.description = description;
    }

    // Getter and Setter methods
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getDirCount() {
        return dir_count;
    }

    public void setDirCount(int dir_count) {
        this.dir_count = dir_count;
    }

    public String getPluginType() {
        return plugin_type;
    }

    public void setPluginType(String plugin_type) {
        this.plugin_type = plugin_type;
    }

    public String getPluginsData() {
        return plugins_data;
    }

    public void setPluginsData(String plugins_data) {
        this.plugins_data = plugins_data;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "PocEntry{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", group='" + group + '\'' +
                ", dir_count=" + dir_count +
                ", plugin_type='" + plugin_type + '\'' +
                ", plugins_data='" + plugins_data + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}