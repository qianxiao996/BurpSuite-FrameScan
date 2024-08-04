package burp.utils;

import burp.Main_Vuln;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static burp.BurpExtender.*;

public class Config {
    public static Map<String, Object> read_config(String Config_PATH) throws IOException {
//        BufferedReader in = new BufferedReader(new FileReader(Config_PATH));
        BufferedReader in = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(Config_PATH)), StandardCharsets.UTF_8));
        String str;
        StringBuilder str_data= new StringBuilder();
        while ((str = in.readLine()) != null) {
            str_data.append(str).append("\n");
        }
        Yaml yaml = new Yaml();
        return yaml.load(str_data.toString());
    }
}
