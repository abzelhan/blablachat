package kz.api.json;

import kz.api.json.Room.RoomJson;
import kz.api.json.User.UserJson;

/**
 * Created by abzalsahitov@gmail.com  on 3/26/18.
 */
public class InviteJson {

    private String code;
    private String url;
    private String description;
    private UserJson inviter;
    private RoomJson room;
    private int freeSize;

    public String getCode() {
        return code;
    }

    public InviteJson setCode(String code) {
        this.code = code;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public InviteJson setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public InviteJson setDescription(String description) {
        this.description = description;
        return this;
    }

    public UserJson getInviter() {
        return inviter;
    }

    public InviteJson setInviter(UserJson inviter) {
        this.inviter = inviter;
        return this;
    }

    public RoomJson getRoom() {
        return room;
    }

    public InviteJson setRoom(RoomJson room) {
        this.room = room;
        return this;
    }

    public int getFreeSize() {
        return freeSize;
    }

    public InviteJson setFreeSize(int freeSize) {
        this.freeSize = freeSize;
        return this;
    }
}
