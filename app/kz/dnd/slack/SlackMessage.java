package kz.dnd.slack;

import java.util.List;

/**
 * Created by bakhyt on 11/24/17.
 */
public class SlackMessage {
    String text;
    List<SlackAttachment> attachments;
    String response_url;

    public String getResponse_url() {
        return response_url;
    }

    public void setResponse_url(String response_url) {
        this.response_url = response_url;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<SlackAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<SlackAttachment> attachments) {
        this.attachments = attachments;
    }
}
