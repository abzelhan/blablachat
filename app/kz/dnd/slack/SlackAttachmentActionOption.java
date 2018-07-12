package kz.dnd.slack;

/**
 * Created by bakhyt on 9/13/17.
 */
public class SlackAttachmentActionOption {
    String text;
    String value;

    public SlackAttachmentActionOption(String text, String value) {
        this.text = text;
        this.value = value;
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
