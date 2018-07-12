package models;

import kz.api.json.Room.MemberJson;

import javax.persistence.*;
@Entity
@Table(name = "members")
public class Member extends DomainObject {

    @Transient
    public static final int ACTIVE_STATUS = 0;

    @Transient
    public static final int BLOCKED_STATUS = 1;

    @Transient
    public static final int EXCLUDED_STATUS = 2;

    @ManyToOne
    private User user;
    @ManyToOne
    private User invitedBy;
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;
    @Column(columnDefinition = "integer default '0'")
    private boolean isPushEnabled;

    @Column(columnDefinition = "integer default '0'")
    private boolean isAdmin;


    @Column(columnDefinition = "integer default '0'")
    int status;


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        setCreator(user);
        this.user = user;
    }

    public static Member byUserAndRoom(User user,Room room){
        if(user!=null && room!=null){
           return  Member.find("user=:user and room=:room").setParameter("user", user).setParameter("room", room).first();
        }else{
            return null;
        }

    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public boolean isPushEnabled() {
        return isPushEnabled;
    }

    public void setPushEnabled(boolean pushEnabled) {
        isPushEnabled = pushEnabled;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public User getInvitedBy() {
        return invitedBy;
    }



    public void setInvitedBy(User invitedBy) {
        this.invitedBy = invitedBy;
    }



    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public MemberJson getJson(){
        return getJson(false);
    }

    public MemberJson getJson(boolean includeRoom){
        MemberJson memberJson = new MemberJson();
        memberJson.setAdmin(isAdmin);
        if(invitedBy!=null) {
            memberJson.setInviterUser(invitedBy.getOptionalJson(false));
        }
        memberJson.setDeleted(getDeleted());
        memberJson.setStatus(getStatus());
        memberJson.setCode(getCode());
        memberJson.setPushEnabled(isPushEnabled);
        memberJson.setUser(user.getOptionalJson(false));
        memberJson.setJoiningDate(creationDate);
        if(includeRoom){
            memberJson.setRoom(room.getOptionalJson(false));
        }
        return memberJson;
    }

}
