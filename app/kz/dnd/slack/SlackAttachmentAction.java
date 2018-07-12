package kz.dnd.slack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bakhyt on 9/13/17.
 */
public class SlackAttachmentAction {
    String name;
    String type;
    String text;
    String value;
    List<SlackAttachmentActionOption> options;

    public SlackAttachmentAction(String name, String type, String text, String value) {
        this.name = name;
        this.type = type;
        this.text = text;
        this.value = value;
    }

    public void addOption(String text, String value) {
        if (options == null) {
            options = new ArrayList<>();
        }
        options.add(new SlackAttachmentActionOption(text, value));
    }

    public void addOption(String text) {
        addOption(text, text);
    }

    public List<SlackAttachmentActionOption> getOptions() {
        return options;
    }

    public void setOptions(List<SlackAttachmentActionOption> options) {
        this.options = options;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
