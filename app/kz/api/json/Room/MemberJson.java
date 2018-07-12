package kz.api.json.Room;

import kz.api.json.User.UserJson;

import java.util.Calendar;

public class MemberJson {

    private String code;
    private Calendar joiningDate;
    private UserJson user;
    private UserJson inviterUser;
    private RoomJson room;
    private Boolean isPushEnabled;
    private Boolean isAdmin;
    private int deleted;
    private int status;

    public int getDeleted() {
        return deleted;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Calendar getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(Calendar joiningDate) {
        this.joiningDate = joiningDate;
    }

    public UserJson getUser() {
        return user;
    }

    public void setUser(UserJson user) {
        this.user = user;
    }

    public UserJson getInviterUser() {
        return inviterUser;
    }

    public void setInviterUser(UserJson inviterUser) {
        this.inviterUser = inviterUser;
    }

    public Boolean getPushEnabled() {
        return isPushEnabled;
    }

    public void setPushEnabled(Boolean pushEnabled) {
        isPushEnabled = pushEnabled;
    }

    public Boolean getAdmin() {
        return isAdmin;
    }

    public void setAdmin(Boolean admin) {
        isAdmin = admin;
    }

    public RoomJson getRoom() {
        return room;
    }

    public void setRoom(RoomJson room) {
        this.room = room;
    }
}
