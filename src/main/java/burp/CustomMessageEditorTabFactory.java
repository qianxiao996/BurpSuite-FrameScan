package burp;

public class CustomMessageEditorTabFactory implements IMessageEditorTabFactory {

    private IBurpExtenderCallbacks callbacks;
    private IMessageEditorController controller;
    public CustomMessageEditorTabFactory(IBurpExtenderCallbacks callbacks, IMessageEditorController controller) {
        this.callbacks = callbacks;
        this.controller = controller;
    }

    @Override
    public IMessageEditorTab createNewInstance(IMessageEditorController controller, boolean editable) {
        // 创建新的自定义标签页实例
        return new CustomMessageEditorTab(callbacks, controller);
    }

}