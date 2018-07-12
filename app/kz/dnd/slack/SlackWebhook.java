package kz.dnd.slack;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import play.libs.WS;

import java.util.List;

/**
 * Created by bakhyt on 9/12/17.
 */
public class SlackWebhook {

    String url;



    public static SlackWebhook getErrorsInstance(){
        return new SlackWebhook("https://hooks.slack.com/services/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");
    }

    public static SlackWebhook getModerateSosInstance(){
        return new SlackWebhook("https://hooks.slack.com/services/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");
    }

    public static SlackWebhook getDevInstance(){
        return new SlackWebhook("https://hooks.slack.com/services/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");
    }

    public static SlackWebhook getUnrepliedRequests(){
        return new SlackWebhook("https://hooks.slack.com/services/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");
    }

    public static SlackWebhook getFeedbackInstance(){
        return new SlackWebhook("https://hooks.slack.com/services/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");
    }

    public static SlackWebhook getLogsInstance() {
        return new SlackWebhook("https://hooks.slack.com/services/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");
    }

    public static SlackWebhook getModeratorInstance() {
        return new SlackWebhook("https://hooks.slack.com/services/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");
    }

    public static SlackWebhook getModeratorHistory() {
        return new SlackWebhook("https://hooks.slack.com/services/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");
    }

    public static SlackWebhook getPaymentsInstance() {
        return new SlackWebhook("https://hooks.slack.com/services/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");
    }

    public static SlackWebhook getServiceRepliesInstance() {
        return new SlackWebhook("https://hooks.slack.com/services/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");
    }

    //https://slack.com/api/chat.update

    public SlackWebhook(String url) {
        this.url = url;
    }

    public void sendMessage(SlackMessage slackMessage) {
        sendMessage(slackMessage.getText(), slackMessage.getAttachments(), slackMessage.getResponse_url());
    }

    public void sendMessage(String message) {
        sendMessage(message, null);
    }

    public void sendMessage(String message, List<SlackAttachment> attachments) {
        sendMessage(message, attachments, "");
    }

    public void sendMessage(String message, List<SlackAttachment> attachments, String responseUrl) {
        //String url = "https://hooks.slack.com/services/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";
        JsonObject jo = new JsonObject();
        jo.addProperty("text", message);
        jo.addProperty("mrkdwn", true);

        if (responseUrl != null && !responseUrl.isEmpty()) {
            jo.addProperty("response_url", responseUrl);
        }

        if (attachments != null && attachments.size() > 0) {
            JsonArray arr = new JsonArray();
            for (SlackAttachment attachment : attachments) {
                JsonObject j = new JsonObject();
                if (attachment.getText() != null && !attachment.getText().isEmpty()) {
                    j.addProperty("text", attachment.getText());
                }
                if (attachment.getColor() != null && !attachment.getColor().isEmpty()) {
                    j.addProperty("color", attachment.getColor());
                }

                if (attachment.getFallback() != null && !attachment.getFallback().isEmpty()) {
                    j.addProperty("fallback", attachment.getFallback());
                }

                if (attachment.getTitle() != null && !attachment.getTitle().isEmpty()) {
                    j.addProperty("title", attachment.getTitle());
                }

                if (attachment.getImage_url() != null && !attachment.getImage_url().isEmpty()) {
                    j.addProperty("image_url", attachment.getImage_url());
                }

                if (attachment.getAuthor_name() != null && !attachment.getAuthor_name().isEmpty()) {
                    j.addProperty("author_name", attachment.getAuthor_name());
                }

                if (attachment.getAuthor_icon() != null && !attachment.getAuthor_icon().isEmpty()) {
                    j.addProperty("author_icon", attachment.getAuthor_icon());
                }

                if (attachment.getAttachment_type() != null && !attachment.getAttachment_type().isEmpty()) {
                    j.addProperty("attachment_type", attachment.getAttachment_type());
                }

                if (attachment.getCallback_id() != null && !attachment.getCallback_id().isEmpty()) {
                    j.addProperty("callback_id", attachment.getCallback_id());
                }

                if (attachment.getFields() != null && !attachment.getFields().isEmpty()) {
                    JsonArray arrFields = new JsonArray();
                    for (SlackAttachmentField attachmentField : attachment.getFields()) {
                        JsonObject jF = new JsonObject();
                        jF.addProperty("title", attachmentField.getTitle());

                        if (attachmentField.getValue() != null && !attachmentField.getValue().isEmpty()) {
                            jF.addProperty("value", attachmentField.getValue());
                        }

                        jF.addProperty("short", attachmentField.isShort());
                        arrFields.add(jF);
                    }
                    j.add("fields", arrFields);
                }

                if (attachment.getActions() != null && !attachment.getActions().isEmpty()) {
                    JsonArray arrFields = new JsonArray();
                    for (SlackAttachmentAction attachmentField : attachment.getActions()) {
                        try {
                            JsonObject jF = new JsonObject();
                            jF.addProperty("name", attachmentField.getName());
                            jF.addProperty("type", attachmentField.getType());
                            jF.addProperty("text", attachmentField.getText());
                            jF.addProperty("value", attachmentField.getValue());

                            if (attachmentField.getOptions() != null && !attachmentField.getOptions().isEmpty()) {
                                JsonArray arrOptions = new JsonArray();
                                for (SlackAttachmentActionOption attachmentActionOption : attachmentField.getOptions()) {
                                    JsonObject jO = new JsonObject();

                                    jO.addProperty("text", attachmentActionOption.getText());
                                    jO.addProperty("value", attachmentActionOption.getValue());

                                    arrOptions.add(jO);
                                }
                                jF.add("options", arrOptions);
                            }

                            arrFields.add(jF);
                        } catch (Exception e) {
                            e.printStackTrace(System.out);
                        }
                    }
                    j.add("actions", arrFields);
                }
                arr.add(j);
            }
            jo.add("attachments", arr);
        }

        //String result = WS.url(url).body("{\"text\":\""+message+"\",\"mrkdwn\": true}").post().getString();
        String json = jo.toString();
        System.out.println("slack->" + json);
        //String result = WS.url(url).body(json).post().getString();
        String result = "";
        if (responseUrl != null && !responseUrl.isEmpty()) {
            result = WS.url(responseUrl).body(json).post().getString();
            //SlackRequest slackRequest = new SlackRequest(responseUrl, json);
            //slackRequest._save();
        } else {
            //SlackRequest slackRequest = new SlackRequest(url, json);
            //slackRequest._save();
            result = WS.url(url).body(json).post().getString();
        }
        System.out.println("result: " + result);
    }
}
