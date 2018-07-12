package models;

import kz.api.json.LanguageJson;
import kz.api.json.Location.CityJson;
import kz.api.json.Room.MemberJson;
import kz.api.json.Room.RoomJson;
import kz.api.json.TagJson;
import kz.api.json.User.UserJson;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Entity
@Table(name = "rooms")
public class Room extends DomainObject {
    @Transient
    public static final int PUBLIC_TYPE = 0;
    @Transient
    public static final int PRIVATE_TYPE = 1;
    @Transient
    public static final int RANDOM_TYPE = 2;
    @Transient
    public static final String REDIS_CACHE_KEY = "members_";
    @Transient
    public static final int TITLE_MIN_LENGTH = 4;
    @Transient
    public static final int TITLE_MAX_LENGTH = 100;

    String title;

    @Column(columnDefinition = "varchar(2048) default ''")
    String description;


    @Column(columnDefinition = "integer default '0'")
    int roomType;

    @ManyToOne
    FileEntity image;

    @ManyToOne
    FileEntity background;

    @ManyToMany
    List<Tag> tags;
    @ManyToMany
    List<Language> languages;

    @ManyToOne
    City city;
    @Column(columnDefinition = "integer default '1'")
    long roomLimit;


    public String getRedisCacheKey() {
        return Room.REDIS_CACHE_KEY + getCode();
    }

    public static boolean isValidRoomType(int type) {
        return (type == Room.PUBLIC_TYPE || type == RANDOM_TYPE || type == PRIVATE_TYPE);
    }

    public static boolean isValidRoomTypeForCreation(int type) {
        return (type == Room.PUBLIC_TYPE || type == PRIVATE_TYPE);

    }


    public Room() {
        tags = new ArrayList<>();
        languages = new ArrayList<>();
    }

    public static Room byCode(String code) {
        if (code != null && !code.isEmpty()) {
            Room room = Room.find("code=:code and deleted=0")
                    .setParameter("code", code)
                    .first();
            if (room == null) {
                throw new NoSuchElementException("Room with this code - " + code + " not finded");
            }
            return room;
        } else {
            return null;
        }
    }


    public static Room byCode(String code, int type) {
        if (code != null && !code.isEmpty()) {
            Room room = Room.find("code=:code and deleted=0 and roomType=:type")
                    .setParameter("code", code)
                    .setParameter("type", type)
                    .first();
            if (room == null) {
                throw new NoSuchElementException(code + " and type: " + type);
            }
            return room;
        } else {
            return null;
        }
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getRoomType() {
        return roomType;
    }

    public void setRoomType(int roomType) {
        this.roomType = roomType;
    }

    public long getRoomLimit() {
        return roomLimit;
    }

    public void setRoomLimit(long roomLimit) {
        this.roomLimit = roomLimit;
    }

    public FileEntity getBackground() {
        return background;
    }

    public void setBackground(FileEntity background) {
        this.background = background;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public List<Language> getLanguages() {
        return languages;
    }

    public void setLanguages(List<Language> languages) {
        this.languages = languages;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public FileEntity getImage() {
        return image;
    }

    public void setImage(FileEntity image) {
        this.image = image;
    }

    public RoomJson getSimpleJson() {
        RoomJson roomJson = new RoomJson();
        roomJson.setTitle(getTitle());
        roomJson.setSize(Member.count("room=? and status=? and deleted=0", this, Member.ACTIVE_STATUS));
        roomJson.setIcon(getImage().getJson());
        roomJson.setDescription(getDescription());
        roomJson.setCode(getCode());
        return roomJson;
    }

    public RoomJson getOptionalJson(boolean includeMembers) {
        RoomJson roomJson = new RoomJson();
        roomJson.setCode(getCode());
        roomJson.setTitle(title);
        roomJson.setType(roomType);
        roomJson.setLimit(roomLimit);
        roomJson.setDescription(description == null ? "" : description);
        roomJson.setSize(Member.count("room=? and status=? and deleted=0", this, Member.ACTIVE_STATUS));
        roomJson.setCreationDate(creationDate);
        Optional.ofNullable(image).ifPresent(i -> roomJson.setIcon(i.getJson()));

        Optional.<Invite>ofNullable(
                Invite.find("room=:room and deleted=0")
                        .setParameter("room", this)
                        .first())
                .ifPresent(i -> roomJson
                        .setInvite(i.getJson(false)));

        if (includeMembers) {
            roomJson.setMembers(Member.find("room=:room and status=:open and deleted=0")
                    .setParameter("room", this)
                    .setParameter("open", Member.ACTIVE_STATUS)
                    .<Member>fetch().stream().map(member -> member.getJson())
                    .collect(Collectors.toList()));

            roomJson.setBlackList(Member.find("room=:room and status=:blocked")
                    .setParameter("room", this)
                    .setParameter("blocked", Member.BLOCKED_STATUS)
                    .<Member>fetch().stream().map(member -> member.getJson()).collect(Collectors.toList()));
        }


        if (getRoomType() == Room.PUBLIC_TYPE) {
            Optional.ofNullable(languages).
                    ifPresent(
                            lang -> roomJson.
                                    setLanguages(lang.stream()
                                            .map(l -> l.getJson())
                                            .collect(Collectors.toList()))
                    );

            Optional.ofNullable(city).
                    ifPresent(c -> roomJson.
                            setCity(c.getJson()));


            Optional.ofNullable(tags).
                    ifPresent(
                            tagList -> roomJson.
                                    setTags(tagList.stream()
                                            .map(t -> t.getJson())
                                            .collect(Collectors.toList()))
                    );
        }

        return roomJson;
    }





}
