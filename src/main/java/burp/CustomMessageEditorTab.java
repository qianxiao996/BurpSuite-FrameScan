package burp;


import burp.model.log.LogEntry;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static burp.Main_Vuln.model;


public class CustomMessageEditorTab implements IMessageEditorTab {

    private IBurpExtenderCallbacks callbacks;
    private IMessageEditorController controller;
    private ITextEditor iTextEditor;

    public CustomMessageEditorTab(IBurpExtenderCallbacks callbacks, IMessageEditorController controller) {
        this.callbacks = callbacks;
        this.controller = controller;
        iTextEditor = callbacks.createTextEditor();
        // 启用自动换行
    }


    @Override
    public String getTabCaption() {
        return "FS POC";
    }

    @Override
    public Component getUiComponent() {
        return iTextEditor.getComponent();
    }

    @Override
    public boolean isEnabled(byte[] bytes, boolean b) {
        return true;
    }

    @Override
    public void setMessage(byte[] content, boolean isRequest) {
        // 检查控制器是否是自定义插件的实例
//        if (controller instanceof BurpExtender) {
            // 处理消息内容
            if (content != null) {
//                List<LogEntry>  all_table_Data= model.getAllValue();
                int row = Main_Vuln.logTable.getSelectedRow();
                row =Main_Vuln.logTable.convertRowIndexToModel(row);
                if(!(row >= 0 && row < model.getRowCount())){
                    return;
                }
                int id = (int)Main_Vuln.logTable.getValueAt(row,0);
                LogEntry current_log  = model.getValueByid(id);
                // 解析HTTP请求或响应
                if (isRequest) {
                    if (current_log != null) {
                        iTextEditor.setText(current_log.poc_requestResponse.getRequest());
                    }
                } else {
                    if (current_log != null) {
                        iTextEditor.setText(current_log.poc_requestResponse.getResponse());
                    }
                }
            } else {
                iTextEditor.setText("".getBytes(StandardCharsets.UTF_8));
            }
//        } else {
//            iTextEditor.setText("".getBytes(StandardCharsets.UTF_8));
//        }
    }

    @Override
    public byte[] getMessage() {
        // 返回当前显示的消息内容
        return iTextEditor.getText();
    }

    @Override
    public boolean isModified() {
        // 判断消息是否被修改
        return iTextEditor.isTextModified();
    }

    @Override
    public byte[] getSelectedData() {
        return iTextEditor.getSelectedText();
        // 返回当前选中的数据
    }
}