package kz.api.json.System;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.List;

/**
 * Created by abzal  on 3/12/18.
 */
public class ServiceEvent {

    public static final int CODE_USER_JOINED_TO_PUBLIC_ROOM = 1;//когда пользователь вступил в группу(прилетает всем участникам)
    public static final String MESSAGE_USER_JOINED_TO_PUBLIC_ROOM = "Пользователь %s вступил в комнату";
    public static final int CODE_USER_LEAVED_FROM_ROOM = 2;//когда пользователь покинул группу(прилетает всем участникам)
    public static final String MESSAGE_USER_LEAVED_FROM_ROOM = "Пользователь %s покинул комнату";
    public static final int CODE_USER_UPDATED_ROOM = 3;// когда комната была обновления(прилетает всем участникам)
    public static final String MESSAGE_USER_UPDATED_ROOM = "Пользователь %s обновил комнату";
    public static final int CODE_USER_DELETED_ROOM = 4;// когда комната была удалена(прилетает всем участникам)
    public static final String MESSAGE_USER_DELETED_ROOM = "Комната \"%s\" была удалена";
    public static final int CODE_USER_WAS_BLOCKED = 5;// когда пользователя заблокировали(прилетает всем участникам)
    public static final String MESSAGE_USER_WAS_BLOCKED = "Пользователь %s заблокировал %s";
    public static final int CODE_USER_WAS_UNBLOCKED = 6;// когда пользователя разблокировали(прилетает всем участникам)
    public static final String MESSAGE_USER_WAS_UNBLOCKED = "Пользователь %s разблокировал %s";
    public static final int CODE_USER_WAS_EXCLUDED = 9;// когда пользователя исключили(прилетает всем участникам)
    public static final String MESSAGE_USER_WAS_EXCLUDED = "Пользователь %s исключил %s";
    public static final int CODE_ADMIN_WAS_ASSIGNED = 7;// когда пользователя сделали админом(прилетает всем участникам)
    public static final String MESSAGE_ADMIN_WAS_ASSIGNED = "Пользователь %s назначил администратором %s";
    public static final int CODE_ADMIN_WAS_DEBAR_ = 8;// когда у пользователя забрали админку(прилетает всем участникам)
    public static final String MESSAGE_ADMIN_WAS_DEBAR = "Пользователь %s убрал из администраторов %s";
    public static final int CODE_CURRENT_USER_WAS_ADDED_TO_GROUP = 10;//Когда пользователя добавили в группу(прилетает персонально)
    public static final String MESSAGE_CURRENT_USER_WAS_ADDED_TO_GROUP = "Вас добавили в комнату %s";
    public static final int CODE_ADMIN_ADDED_NEW_USER_TO_GROUP = 11;//Когда пользователя добавили в группу(прилетает всем участникам)
    public static final String MESSAGE_ADMIN_ADDED_NEW_USER_TO_GROUP = "Пользователь %s добавил в комнату пользователя %s";
    public static final int CODE_CURRENT_USER_WAS_BLOCKED_IN_ROOM = 12; //когда текущего пользователя заблокировали(прилетает персонально)
    public static final String MESSAGE_USER_WAS_BLOCKED_IN_ROOM = "Пользователь %s заблокировал вас в комнате %s";
    public static final int CODE_CURRENT_USER_WAS_UNBLOCKED_IN_ROOM = 19; //когда текущего пользователя заблокировали(прилетает персонально)
    public static final String MESSAGE_CURRENT_USER_WAS_UNBLOCKED_IN_ROOM = "Пользователь %s разблокировал вас в комнате %s";
    public static final int CODE_CURRENT_USER_CREATED_RANDOM_ROOM = 14; //когда текущий пользователь создал рандомную комнату(прилетает персонально)
    public static final String MESSAGE_CURRENT_USER_CREATED_RANDOM_ROOM = "Вы создали случайную комнату";
    public static final int CODE_CURRENT_USER_WAS_ADDED_TO_RANDOM_ROOM = 15; //Когда текущего пользователя добавили в рандомную комнату(прилетает персонально)
    public static final String MESSAGE_CURRENT_USER_WAS_ADDED_TO_RANDOM_ROOM = "Вы были добавлены в случайную комнату";
    public static final int CODE_CURRENT_USER_WAS_JOINED_TO_ROOM = 16;
    public static final String MESSAGE_CURRENT_USER_WAS_JOINED_TO_ROOM = "Вы вступили в комнату %s";
    public static final int CODE_CURRENT_USER_WAS_LEAVED_FROM_ROOM = 19;
    public static final String MESSAGE_CURRENT_USER_WAS_LEAVED_FROM_ROOM = "Вы вышли из комнаты";
    public static final int CODE_CURRENT_USER_WAS_ASSIGNED_TO_ADMIN = 17;
    public static final String MESSAGE_CURRENT_USER_WAS_ASSIGNED_TO_ADMIN = "Пользователь %s назначил вас администратором в комнате %s";
    public static final int CODE_CURRENT_USER_WAS_DEBARED_FROM_ADMIN = 18;
    public static final String MESSAGE_CURRENT_USER_WAS_DEBARED_FROM_ADMIN = "Пользователь %s убрал вас из администраторов в комнате %s";
    public static final int CODE_CURRENT_USER_WAS_EXCLUDED_FROM_ROOM = 20;
    public static final String MESSAGE_CURRENT_USER_WAS_EXCLUDED_FROM_ROOM = "Пользователь %s исключил вас из комнаты %s";

    @JsonIgnore
    public static final Integer SERVICE_TYPE_BROADCAST = 1;

    @JsonIgnore
    public static final Integer SERVICE_TYPE_PERSONAL_BROADCAST = 3;

    @JsonIgnore
    public static final Integer SERVICE_TYPE_PERSONAL = 2;


    public static final String REDIS_KEY = "SERVICE_EVENT_LIST";

    private int serviceCode;

    private String body;//message

    private String to;//default room code

    private String from;//default from system

    private String timestamp;//ex: 2018.04.14 20:11:11

    private long timestampLong;

    private String event;//default 'service'

    private String room;//room code

    private Integer serviceType;

    private String excludeCode;

    public Integer getServiceType() {
        return serviceType;
    }

    public String getExcludeCode() {
        return excludeCode;
    }

    public ServiceEvent setExcludeCode(String excludeCode) {
        this.excludeCode = excludeCode;
        return this;
    }

    public ServiceEvent setServiceType(Integer serviceType) {
        this.serviceType = serviceType;
        return this;
    }

    public static ServiceEvent createRoomEvent(final int serviceCode, final String roomCode, final String body) {
        final ServiceEvent serviceEvent = new ServiceEvent();
        serviceEvent.setFrom("system");
        serviceEvent.setBody(body);
        serviceEvent.setEvent("service");
        serviceEvent.setRoom(roomCode);
        serviceEvent.setServiceCode(serviceCode);
        return serviceEvent;
    }

    public int getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(int serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }
}
