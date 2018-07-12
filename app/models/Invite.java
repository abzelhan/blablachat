package models;

import kz.api.json.InviteJson;

import javax.persistence.*;

@Entity
@Table(name = "invites")
public class Invite extends DomainObject{
    @ManyToOne
    private Room room;
    @Column(columnDefinition = "integer default '1'")
    private int freeSize;//декремент при каждом joinbyInvite
    @Transient
    public final static String siteUrl = "http://blablachat.me:9001";
    @Column(columnDefinition = "varchar(2048) default ''")
    String description;
    private String url;


    @ManyToOne
    private User inviter;


    public InviteJson getJson(boolean includeRoom){
        InviteJson inviteJson = new InviteJson();
        inviteJson.setCode(getCode());
        inviteJson.setDescription(getDescription());
        inviteJson.setInviter(inviter.getOptionalJson(false));
        inviteJson.setFreeSize(getFreeSize());
        if(includeRoom) {
            inviteJson.setRoom(getRoom().getOptionalJson(true));
        }
        inviteJson.setUrl(getUrl());
        return inviteJson;
    }

    public InviteJson getJson(){
      return getJson(true);
    }

    public String getDescription() {
        return description;
    }

    public Invite setDescription(String description) {
        this.description = description;
        return this;
    }

    public User getInviter() {
        return inviter;
    }

    public Invite setInviter(User inviter) {
        this.inviter = inviter;
        return this;
    }


    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public int getFreeSize() {
        return freeSize;
    }

    public Invite setFreeSize(int freeSize) {
        this.freeSize = freeSize;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
