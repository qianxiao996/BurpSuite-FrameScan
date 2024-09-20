package burp.utils;

import static burp.Main_Vuln.*;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class Config {
    public static Map<String, Object> read_config(String Config_PATH) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(Config_PATH)), StandardCharsets.UTF_8));
        String str;
        StringBuilder str_data= new StringBuilder();
        while ((str = in.readLine()) != null) {
            str_data.append(str).append("\n");
        }
        Yaml yaml = new Yaml();
        return yaml.load(str_data.toString());
    }
    public static void save_config() {
        if(Objects.equals(btn3_white.getText(), "当前白名单状态:启用")){
            Global_Config.put("WhiteEnable", true) ;
        }else{
            Global_Config.put("WhiteEnable",false) ;
        }
        Global_Config.put("Enable_Poc",chkbox_poc.isSelected()) ;
        Global_Config.put("Enable_Finger",chkbox_finger.isSelected()) ;
        Global_Config.put("Is_Proxy", chkbox_proxy.isSelected()) ;
        Global_Config.put("Is_Repeater", chkbox_repeater.isSelected()) ;
        Global_Config.put("Is_Debug",chkbox_is_debug.isSelected()) ;
        Global_Config.put("WhiteList", textField_white.getText()) ;
        Global_Config.put("BlackList", textField_black.getText()) ;
//        Global_Config.put("EnablePocList", vuln_poc_combox.getSelectedItem()) ;
//        Global_Config.put("DisenableFingerPocList", vuln_disenable_finger_poc_combox.getSelectedItem()) ;

        Poc.reload_poc();
        Yaml yaml = new Yaml(options);
        String yamlString = yaml.dump(Global_Config);
        try (Writer out = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(Paths.get(Config_PATH)), StandardCharsets.UTF_8))) {
            out.write(yamlString);
        } catch (IOException e) {
            printErr(Arrays.toString(e.getStackTrace()));
        }
        JOptionPane.showMessageDialog(null, "配置保存成功！", "提示", JOptionPane.INFORMATION_MESSAGE);
    }


}
