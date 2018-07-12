package controllers;

import kz.api.json.Context;
import kz.api.json.Result;
import kz.api.json.Room.RoomJson;
import kz.api.json.System.ServiceEvent;
import models.*;
import play.db.jpa.JPA;
import utils.helpers.RedisHelper;

import javax.persistence.NoResultException;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import static utils.providers.ResultFactory.*;

/**
 * Created by abzalsahitov@gmail.com  on 3/23/18.
 */
public class RoomApi extends BaseApi {

    public static Result checkFreeRoomTitle(Context context) {
        String roomTitle = context.getString("title");
        Result result = getOkResult(context);
        long count = Room.count("title=? and deleted=0", roomTitle);

        result.setFree(count == 0 ? true : false);
        return result;
    }

    public static Result createRoomSimple(Context context) {
        User currentUser = getRequiredUser(context);
        String title = context.getString("title");
        if (!(title.length() >= Room.TITLE_MIN_LENGTH && title.length() <= Room.TITLE_MAX_LENGTH)) {
            return getInvalidParameterValueRangeResult(context, "title", User.USERNAME_MIN_LENGTH, User.USERNAME_MAX_LENGTH);
        }
        if (Room.count("title=? and deleted=0", title) != 0) {
            return getAlreadyExistResult(context, "title", title);
        }

        Integer type = context.getInteger("type");
        if (!Room.isValidRoomTypeForCreation(type)) {
            return getInvalidParameterValueResult(context, "type", String.valueOf(type));
        }

        String imageCode = context.getOptionalString("imageCode");
        FileEntity avatarFile = FileEntity.createOptionalAvatar(imageCode);

        Long limit = context.getLong("limit");
        if (limit < 2) {
            return getInvalidParameterValueResult(context, "limit", String.valueOf(limit));
        }

        Room newRoom = new Room();
        newRoom.setCreationDate(Calendar.getInstance());
        newRoom.setRoomType(type);
        newRoom.setTitle(title);
        newRoom.setLanguages(null);
        newRoom.setTags(null);
        newRoom.setCity(null);
        newRoom.setImage(avatarFile);
        newRoom.setRoomLimit(limit);
        newRoom.save();

        Member admin = new Member();
        admin.setAdmin(true);
        admin.setRoom(newRoom);
        admin.setUser(currentUser);
        admin.setPushEnabled(true);
        admin.setCreationDate(Calendar.getInstance());
        admin.setStatus(Member.ACTIVE_STATUS);
        admin.save();

        //store member id to redis cache
        String roomKey = newRoom.getRedisCacheKey();
        RedisHelper.getInstance().lpush(roomKey, String.valueOf(admin.getUser().getCode()));
        Result result = getOkResult(context);

        result.setRoom(newRoom.getOptionalJson(true));
        //generate invite code

        return result;
    }


    public static Result createRoom(Context context) {
        User roomCreator = getRequiredUser(context);

        String title = context.getString("title");
        if (!(title.length() >= Room.TITLE_MIN_LENGTH && title.length() <= Room.TITLE_MAX_LENGTH)) {
            return getInvalidParameterValueRangeResult(context, "title", User.USERNAME_MIN_LENGTH, User.USERNAME_MAX_LENGTH);
        }

        String description = context.getOptionalString("description");//min 1 and max length 2048
        if (description != null) {
            if (description.length() < 1 || description.length() > 2048) {
                return getInvalidParameterValueRangeResult(context, "description", 1, 2048);
            }
        }

        //check if this name is not exist
        if (Room.count("title=? and deleted=0", title) != 0) {
            return getAlreadyExistResult(context, "title", title);
        }


        Integer type = context.getInteger("type");
        if (!Room.isValidRoomTypeForCreation(type)) {
            return getInvalidParameterValueResult(context, "type", String.valueOf(type));
        }

        String cityCode = context.getString("cityCode");
        City city = City.byCode(cityCode);

        List<String> langCodesList = context.getCodeList("languages");
        List<Language> languages = Language.byCodeList(langCodesList);


        String imageCode = context.getOptionalString("imageCode");
        FileEntity avatarFile = FileEntity.createOptionalAvatar(imageCode);


        List<String> tagsList = context.getList("tags");
        List<Tag> tags = Tag.getOrCreateByList(tagsList);


//        FileEntity background = FileEntity.byCode(context.getOptionalString("backgroundCode"));
        Long limit = context.getLong("limit");
        if (limit < 2) {
            return getInvalidParameterValueResult(context, "limit", String.valueOf(limit));
        }

        Result result = getOkResult(context);

        Room newRoom = new Room();
        newRoom.setCreationDate(Calendar.getInstance());
        newRoom.setRoomType(type);
        newRoom.setTitle(title);
        newRoom.setCity(city);
        newRoom.setDescription(description);
        newRoom.setImage(avatarFile);
        newRoom.setTags(tags);
        newRoom.setLanguages(languages);
        newRoom.setRoomLimit(limit);
//        newRoom.setBackground(background);
        newRoom.save();

        Member admin = new Member();
        admin.setAdmin(true);
        admin.setRoom(newRoom);
        admin.setUser(roomCreator);
        admin.setPushEnabled(true);
        admin.setCreationDate(Calendar.getInstance());
        admin.setStatus(Member.ACTIVE_STATUS);
        admin.save();

        //store member id to redis cache
        String roomKey = newRoom.getRedisCacheKey();
        RedisHelper.getInstance().lpush(roomKey, String.valueOf(admin.getUser().getCode()));


        result.setRoom(newRoom.getOptionalJson(false));
        //generate invite code


        return result;
    }

    public static Result editRoom(Context context) {
        User requiredUser = getRequiredUser(context);
        String roomCode = context.getString("code");
        Room room = Room.byCode(roomCode);
        Result result = getOkResult(context);

        if (room == null) {
            result = getNotFoundResult(context);
            result.setMessage("this room not exist, or it was deleted");
            return result;
        }

        if (room.getRoomType() == Room.RANDOM_TYPE) {
            result = getNotModifiableErrorResult(context);
            result.setMessage("You can't edit a room with type Random");
            return result;
        }

        Member member = Member.find("user=:user and room=:room and isAdmin=true and deleted=0 and status=0")
                .setParameter("user", requiredUser)
                .setParameter("room", room)
                .first();

        if (member == null) {
            result = getNotAdminMemberErrorResult(context);
            result.setMessage("You are not an admin of this room and you are not exist in this room");
            return result;
        }


        String title = context.getOptionalString("title");
        if (title != null) {
            if (!(title.length() >= Room.TITLE_MIN_LENGTH && title.length() <= Room.TITLE_MAX_LENGTH)) {
                return getInvalidParameterValueRangeResult(context, "title", User.USERNAME_MIN_LENGTH, User.USERNAME_MAX_LENGTH);
            }
            if (Room.count("title=? and deleted=0", title) != 0) {
                return getAlreadyExistResult(context, "title", title);
            } else {
                room.setTitle(title);
            }
        }

        String description = context.getOptionalString("description");
        if (description != null) {
            if (description.length() < 1 || description.length() > 2048) {
                return getInvalidParameterValueRangeResult(context, "description", 1, 2048);
            } else {
                room.setDescription(description);
            }
        }


        Integer type = context.getOptionalInteger("type");
        if (type != null) {
            if (!Room.isValidRoomTypeForCreation(type)) {
                return getInvalidParameterValueResult(context, "type", String.valueOf(type));
            } else {
                room.setRoomType(type);
            }
        }


        //there we need to check if room type is public
        if(room.getRoomType()==Room.PUBLIC_TYPE){
            String cityCode = context.getOptionalString("cityCode");
            if (cityCode != null) {
                City city = City.find("code=:code and deleted=0").setParameter("code", cityCode).first();
                room.setCity(city);
            }
            List<String> optionalTagList = context.getOptionalList("tags");
            if (optionalTagList != null) {
                List<Tag> tags = Tag.getEmptyOrCreateByList(optionalTagList);
                room.setTags(tags);
            }

            List<String> optionalCodeList = context.getOptionalList("languages");
            if (optionalCodeList != null) {

                List<Language> languages = Language.byList(optionalCodeList);
                room.setLanguages(languages);

            }

        }else{
            room.setCity(null);
            room.setTags(null);
            room.setLanguages(null);
        }


        Long limit = context.getOptionalLongOrNull("limit");
        if (limit != null) {
            //5
            //2
            long roomSize = Member.count("room=? and status=? and deleted=0", room, Member.ACTIVE_STATUS);
            if (limit < roomSize) {
                return getInvalidParameterValueResult(context, "limit", String.valueOf(limit));
            } else {
                room.setRoomLimit(limit);
            }
        }



        String imageCode = context.getOptionalString("imageCode");
        if (imageCode != null) {
            FileEntity fileEntity = FileEntity.byCodeRequired(imageCode);
            room.setImage(fileEntity);
        }



//        FileEntity background = FileEntity.byCode(context.getOptionalString("backgroundCode"));

        causeBroadcastRoomEvent(
                roomCode,
                ServiceEvent.CODE_USER_UPDATED_ROOM,
                String.format(ServiceEvent.MESSAGE_USER_UPDATED_ROOM,
                        requiredUser.getUsername()),
                null
        );

        room.save();
        result.setRoom(room.getOptionalJson(false));
        return result;
    }


    //also delete all members
    public static Result deleteRoom(Context context) {
        User currentUser = getRequiredUser(context); 
        String roomCode = context.getString("code");
        Room room = Room.find("code=:code and deleted=0")
                .setParameter("code", roomCode)
                .first();
        Result result = getOkResult(context);

        if (room == null) {
            result = getNotFoundResult(context);
            result.setMessage("this room not exist, or it was deleted");
            return result;
        }

        Member member = Member.find("user=:user and room=:room and isAdmin=true and deleted=0 and status=0")
                .setParameter("user", currentUser)
                .setParameter("room", room)
                .first();

        if (member == null) {
            result = getForbiddenOperationErrorResult(context, "You are not an admin of this room and you are not exist in this room");
            return result;
        }

        List<Member> members = Member.find("room=:room and deleted=0").setParameter("room", room).fetch();
        for (Member m :
                members) {
            m.setDeleted(1);
            m.save();
        }

        room.setDeleted(1);
        room.save();


        causeBroadcastRoomEvent(
                roomCode,
                ServiceEvent.CODE_USER_DELETED_ROOM,
                String.format(ServiceEvent.MESSAGE_USER_DELETED_ROOM, room.getTitle()),
                null);

        //remove member list from reddis
//        String roomKey = room.getRedisCacheKey();
//        RedisHelper.getInstance().remove(roomKey);


        result.setMessage("Комната с кодом " + room.getCode() + " успешно удалена");
        return result;
    }


    public static Result getRoomInfo(Context context) {
        User user = getRequiredUser(context);
        String roomCode = context.getString("roomCode");
        Result result = getOkResult(context);
        Room room = Room.byCode(roomCode);

        Member member = Member.find("user=:user and room=:room and deleted=0")
                .setParameter("user", user)
                .setParameter("room", room)
                .first();

        if (member == null) {
            result = getNotMemberErrorResult(context);
            result.setMessage("You are not joined to this group");
            result.setRoom(room.getSimpleJson());
            return result;
        }

        if (member.getStatus() == Member.BLOCKED_STATUS) {
            result = getMemberIsBlockedErrorResult(context);
            result.setRoom(room.getSimpleJson());
            result.setMessage("Вы заблокированы в этой комнате");
            return result;
        }


        RoomJson roomJson = room.getOptionalJson(true);
        result.setRoom(roomJson);
        return result;
    }


    //поиск случайного собеседника
    public static Result createRandomRoomWithRandomUser(Context context) {
        User currentUser = getRequiredUser(context);
        long randomUserId;
        Result result = null;
        try {
            BigInteger userId = (BigInteger) JPA.em().
                    createNativeQuery("select u.id from users u WHERE u.deleted=0 and u.isSearchable=true and u.id!=:id  order by rand() limit 1")
                    .setParameter("id", currentUser.getId())
                    .getSingleResult();
            randomUserId = userId.longValue();
        } catch (NoResultException e) {
            result = getCustomResult(context, 404);
            result.setMessage("Не найдено ни одного собеседника");
            return result;
        }

        result = getOkResult(context);
        User randomUser = User.findById(randomUserId);

        Room randomRoom = new Room();
        randomRoom.setCreationDate(Calendar.getInstance());
        randomRoom.setRoomType(Room.RANDOM_TYPE);
        randomRoom.setTitle("Random chat #" + (Room.count("roomType=?", Room.RANDOM_TYPE) + 1));
        randomRoom.setCity(null);
        randomRoom.setDescription("Random room with random user");
        randomRoom.setImage(FileEntity.createOptionalAvatar(null));
        randomRoom.setTags(null);
        randomRoom.setLanguages(null);
        randomRoom.setRoomLimit(2);
        randomRoom.save();

        Member first = new Member();
        first.setAdmin(false);
        first.setRoom(randomRoom);
        first.setUser(currentUser);
        first.setPushEnabled(true);
        first.setCreationDate(Calendar.getInstance());
        first.setStatus(Member.ACTIVE_STATUS);
        first.save();

        Member second = new Member();
        second.setAdmin(false);
        second.setRoom(randomRoom);
        second.setUser(randomUser);
        second.setPushEnabled(true);
        second.setCreationDate(Calendar.getInstance());
        second.setStatus(Member.ACTIVE_STATUS);
        second.save();

        causePersonalEvent(first.getUser().getCode(),
                randomRoom.getCode(),
                ServiceEvent.CODE_CURRENT_USER_WAS_ADDED_TO_RANDOM_ROOM,// old CODE_CURRENT_USER_CREATED_RANDOM_ROOM
                ServiceEvent.MESSAGE_CURRENT_USER_CREATED_RANDOM_ROOM);

        causePersonalEvent(second.getUser().getCode(),
                randomRoom.getCode(),
                ServiceEvent.CODE_CURRENT_USER_WAS_ADDED_TO_RANDOM_ROOM,
                ServiceEvent.MESSAGE_CURRENT_USER_WAS_ADDED_TO_RANDOM_ROOM);

        String roomKey = randomRoom.getRedisCacheKey();
        RedisHelper.getInstance().lrem(roomKey, currentUser.getCode());
        RedisHelper.getInstance().lrem(roomKey, randomUser.getCode());
        RedisHelper.getInstance().lpush(roomKey, currentUser.getCode());
        RedisHelper.getInstance().lpush(roomKey, randomUser.getCode());

        result.setRoom(randomRoom.getOptionalJson(true));
        return result;
    }


    //Тут есть нюанс в имени комнаты, в интересах комнаты....
    public static Result createRoomByUserCodeListAndCurrentUserInterests(Context context) {
        User currentUser = getRequiredUser(context);
        List<String> usersCodeList = context.getCodeList("codeList");

        String title = context.getString("title");
        if (!(title.length() >= Room.TITLE_MIN_LENGTH && title.length() <= Room.TITLE_MAX_LENGTH)) {
            return getInvalidParameterValueRangeResult(context, "title", User.USERNAME_MIN_LENGTH, User.USERNAME_MAX_LENGTH);
        }

        String description = context.getOptionalString("description");//min 1 and max length 2048
        if (description != null) {
            if (description.length() < 1 || description.length() > 2048) {
                return getInvalidParameterValueRangeResult(context, "description", 1, 2048);
            }
        }

        //check if this name is not exist
        if (Room.count("title=? and deleted=0", title) != 0) {
            return getAlreadyExistResult(context, "title", title);
        }


        Integer type = context.getInteger("roomType");
        if (!Room.isValidRoomTypeForCreation(type)) {
            return getInvalidParameterValueResult(context, "type", String.valueOf(type));
        }

        FileEntity avatarFile = FileEntity.createOptionalAvatar(null);


        List<User> users = usersCodeList.stream()
                .distinct()
                .map(code ->
                        User.byCodeRequired(code))
                .filter(u -> u != null && !u.getCode().equals(currentUser.getCode()))
                .collect(Collectors.toList());

        Room room = new Room();
        room.setRoomLimit(users.size() + 1);//we set limit of group by size of users+1
        room.setRoomType(type);
        room.setTitle(title);
        room.setDescription(description);

//        room.setCity(currentUser.getCity());
        room.setCity(null);
//        room.setLanguages(new ArrayList<>(currentUser.getLanguages()));
        room.setLanguages(null);
//        room.setTags(new ArrayList<>(currentUser.getTags()));
        room.setTags(null);
        room.setImage(avatarFile);
        room.setCreationDate(Calendar.getInstance());
        room.save();

        final String roomKey = room.getRedisCacheKey();
        RedisHelper.getInstance().lpush(roomKey, currentUser.getCode());


        Member admin = new Member();
        admin.setRoom(room);
        admin.setPushEnabled(true);
        admin.setCreationDate(Calendar.getInstance());
        admin.setUser(currentUser);
        admin.setAdmin(true);
        admin.setStatus(Member.ACTIVE_STATUS);
        admin.save();

        users.stream().forEach(u -> {
            Member member = new Member();
            member.setRoom(room);
            member.setPushEnabled(true);
            member.setCreationDate(Calendar.getInstance());
            member.setUser(u);
            member.setAdmin(false);
            member.setStatus(Member.ACTIVE_STATUS);
            member.save();
            RedisHelper.getInstance().lpush(roomKey, u.getCode());
            causePersonalEvent(u.getCode(),
                    room.getCode(),
                    ServiceEvent.CODE_CURRENT_USER_WAS_ADDED_TO_GROUP,
                    String.format(ServiceEvent.MESSAGE_CURRENT_USER_WAS_ADDED_TO_GROUP, room.getTitle())
            );
//            causeBroadcastRoomEvent(
//                    room.getCode(),
//                    ServiceEvent.CODE_USER_JOINED_TO_PUBLIC_ROOM,
//                    String.format(ServiceEvent.MESSAGE_USER_JOINED_TO_PUBLIC_ROOM, member.getUser().getUsername())
//            );
        });

        Result result = getOkResult(context);
        result.setRoom(room.getOptionalJson(true));

        return result;
    }


}
