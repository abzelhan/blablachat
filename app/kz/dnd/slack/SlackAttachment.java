package kz.dnd.slack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bakhyt on 9/13/17.
 */
public class SlackAttachment {
    String title;
    String fallback;
    String color;
    String text;
    String callback_id;
    String attachment_type;

    String image_url;
    String author_name;
    String author_icon;

    List<SlackAttachmentField> fields;
    List<SlackAttachmentAction> actions;



    public SlackAttachment(String title, String color, String text) {
        this.title = title;
        this.color = color;
        this.text = text;
    }

    public SlackAttachment(String title, String image_url) {
        this.title = title;
        this.image_url = image_url;
    }

    public void addAttachmentField(SlackAttachmentField field){
        if (fields==null){
            fields = new ArrayList<SlackAttachmentField>();
        }
        fields.add(field);
    }

    public void addAttackmentAction(SlackAttachmentAction action){
        if (actions==null){
            actions = new ArrayList<>();
        }
        actions.add(action);
    }

    public String getCallback_id() {
        return callback_id;
    }

    public void setCallback_id(String callback_id) {
        this.callback_id = callback_id;
    }

    public String getAttachment_type() {
        return attachment_type;
    }

    public void setAttachment_type(String attachment_type) {
        this.attachment_type = attachment_type;
    }

    public List<SlackAttachmentAction> getActions() {
        return actions;
    }

    public void setActions(List<SlackAttachmentAction> actions) {
        this.actions = actions;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public List<SlackAttachmentField> getFields() {
        return fields;
    }

    public void setFields(List<SlackAttachmentField> fields) {
        this.fields = fields;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFallback() {
        return fallback;
    }

    public void setFallback(String fallback) {
        this.fallback = fallback;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAuthor_name() {
        return author_name;
    }

    public void setAuthor_name(String author_name) {
        this.author_name = author_name;
    }

    public String getAuthor_icon() {
        return author_icon;
    }

    public void setAuthor_icon(String author_icon) {
        this.author_icon = author_icon;
    }
}
