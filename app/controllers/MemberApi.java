package controllers;

import kz.api.json.Context;
import kz.api.json.Result;
import kz.api.json.System.ServiceEvent;
import models.Invite;
import models.Member;
import models.Room;
import models.User;
import utils.helpers.RedisHelper;

import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

import static utils.providers.ResultFactory.*;

/**
 * Created by abzalsahitov@gmail.com  on 3/25/18.
 */
public class MemberApi extends BaseApi {

    public static Result joinToPublicRoom(Context context) throws Exception {
        User currentUser = getRequiredUser(context);
        String roomCode = context.getString("code");
        Room room = Room.byCode(roomCode, Room.PUBLIC_TYPE);
        long roomSize = Member.count("room=? and status=? and deleted=0", room, Member.ACTIVE_STATUS);
        Result result = getOkResult(context);
        if (roomSize == room.getRoomLimit()) {
            result = getOverflowError(context);
            result.setMessage("Группа переполнена");
            return result;
        }

        Member member = Member.byUserAndRoom(currentUser, room);

        if (member != null) {
//            if (member.getStatus() == Member.EXCLUDED_STATUS) {
//                result = getCustomResult(context, 403);
//                result.setMessage("Вы были исключены из группы");
//                return result;
//            }
            if (member.getStatus() == Member.BLOCKED_STATUS) {
                result = getMemberIsBlockedErrorResult(context);
                result.setMessage("Вас заблокировали в этой группе");
                return result;
            }
            if (member.getDeleted() == 1) {//восстановление чувака, публичная группа
                member.setDeleted(0);
                member.setLastModificationDate(Calendar.getInstance());
                member.save();
                result.setMessage("Ваше участие восстановлено");
            } else {
                result = getAlreadyMemberErrorResult(context);
                result.setMessage("Вы уже состоите в этой группе");
                return result;
            }
        }

        if (member == null) {
            member = new Member();
            member.setCreationDate(Calendar.getInstance());
            member.setPushEnabled(true);
            if (room.getCreator() != null && room.getCreator().equals(currentUser)) {
                member.setAdmin(true);
            } else {
                member.setAdmin(false);
            }
            member.setRoom(room);
            member.setUser(currentUser);
            member.save();
        }
        //store member id to redis cache
        String roomKey = room.getRedisCacheKey();
        RedisHelper.getInstance().lrem(roomKey, member.getUser().getCode());
        RedisHelper.getInstance().lpush(roomKey, String.valueOf(member.getUser().getCode()));

        //delete invite link if size of room is equal to room limit
        roomSize++;
        if (roomSize == room.getRoomLimit()) {
            Invite oldInvite = Invite.find("room=:room and deleted=0").setParameter("room", room).first();
            if (oldInvite != null) {
                oldInvite.setDeleted(1);
                oldInvite.save();
            }
        }


        //store event to redis cache

        causePersonalEvent(currentUser.getCode(),
                room.getCode(),
                ServiceEvent.CODE_USER_JOINED_TO_PUBLIC_ROOM,// old CODE_CURRENT_USER_WAS_JOINED_TO_ROOM
                String.format(ServiceEvent.MESSAGE_CURRENT_USER_WAS_JOINED_TO_ROOM, room.getTitle()));


        causeBroadcastRoomEvent(
                room.getCode(),
                ServiceEvent.CODE_USER_JOINED_TO_PUBLIC_ROOM,
                String.format(ServiceEvent.MESSAGE_USER_JOINED_TO_PUBLIC_ROOM, currentUser.getUsername()),
                currentUser.getCode()
        );


        result.setMember(member.getJson());
        result.setRoom(room.getOptionalJson(true));
        return result;
    }

    public static void leaveFromRoom(User user, String roomCode) {
        Room room = Room.find("code=:code and deleted=0").setParameter("code", roomCode).first();
        if (room.getRoomType() == Room.RANDOM_TYPE) {
            Member.find("status=:status and deleted=0 and room=:room")
                    .setParameter("status", Member.ACTIVE_STATUS)
                    .setParameter("room", room)
                    .fetch()
                    .stream()
                    .forEach(m -> {
                                ((Member) m).setDeleted(1);
                                ((Member) m).save();
                            }
                    );
            room.setDeleted(1);
            room.save();

            causeBroadcastRoomEvent(
                    roomCode,
                    ServiceEvent.CODE_USER_DELETED_ROOM,
                    String.format(ServiceEvent.MESSAGE_USER_DELETED_ROOM, room.getTitle()),
                    null);
        }

        Member member = Member.find("room=:room and user=:user and deleted=0 and status=:status")
                .setParameter("room", room)
                .setParameter("user", user)
                .setParameter("status", Member.ACTIVE_STATUS)
                .first();

        if (user.getId().compareTo(member.getUser().getId()) == 0) {
            String redisCacheKey = room.getRedisCacheKey();
            if (member.isAdmin()) {
                long count = Member.count("room=? and deleted=0 and status=? and code!=?", room, Member.ACTIVE_STATUS, member.getCode());
                if (count == 0) {
                    member.setDeleted(1);
                    member.setAdmin(false);
                    member.save();
                    RedisHelper.getInstance().remove(redisCacheKey);
                    room.setDeleted(1);
                    room.save();
                } else {
                    long amountOfAdmins = Member.count("room=? and deleted=0 and status=? and code!=? and isAdmin=true", room, Member.ACTIVE_STATUS, member.getCode());
                    if (amountOfAdmins == 0) {
                        List<Member> members = Member.find("room=:room and deleted=0 and status=:status and code!=:code and isAdmin=false")//get users that not admins
                                .setParameter("room", room)
                                .setParameter("status", Member.ACTIVE_STATUS)
                                .setParameter("code", member.getCode())
                                .fetch();


                        Member newAdmin = members.stream().max(Comparator.comparing(Member::getCreationDate).reversed()).get();//get very old member(by joining date)
                        newAdmin.setAdmin(true);
                        newAdmin.save();
                        causePersonalEvent(newAdmin.getUser().getCode(),
                                roomCode,
                                ServiceEvent.CODE_ADMIN_WAS_ASSIGNED, // old CODE_CURRENT_USER_WAS_ASSIGNED_TO_ADMIN
                                String.format(ServiceEvent.MESSAGE_CURRENT_USER_WAS_ASSIGNED_TO_ADMIN, user.getUsername(),newAdmin.getRoom().getTitle()));

                        causeBroadcastRoomEvent(
                                roomCode,
                                ServiceEvent.CODE_ADMIN_WAS_ASSIGNED,
                                String.format(ServiceEvent.MESSAGE_ADMIN_WAS_ASSIGNED,
                                        user.getUsername(),
                                        newAdmin.getUser().getUsername()
                                ),
                                newAdmin.getUser().getCode()
                        );
                    }
                    member.setDeleted(1);
                    member.setAdmin(false);
                    member.save();

                    causePersonalEvent(user.getCode(),
                            roomCode,
                            ServiceEvent.CODE_USER_LEAVED_FROM_ROOM,// old CODE_CURRENT_USER_WAS_LEAVED_FROM_ROOM
                            ServiceEvent.MESSAGE_CURRENT_USER_WAS_LEAVED_FROM_ROOM);

                    causeBroadcastRoomEvent(
                            roomCode,
                            ServiceEvent.CODE_USER_LEAVED_FROM_ROOM,
                            String.format(ServiceEvent.MESSAGE_USER_LEAVED_FROM_ROOM, user.getUsername()
                            ),
                            user.getCode());
                }
            } else {
                member.setDeleted(1);
                member.save();
                causePersonalEvent(user.getCode(),
                        roomCode,
                        ServiceEvent.CODE_USER_LEAVED_FROM_ROOM,// old CODE_CURRENT_USER_WAS_LEAVED_FROM_ROOM
                        ServiceEvent.MESSAGE_CURRENT_USER_WAS_LEAVED_FROM_ROOM);

                causeBroadcastRoomEvent(
                        roomCode,
                        ServiceEvent.CODE_USER_LEAVED_FROM_ROOM,
                        String.format(ServiceEvent.MESSAGE_USER_LEAVED_FROM_ROOM, user.getUsername()
                        ),
                        user.getCode());
            }
        }
    }

    //доработать логику с админами
    public static Result leaveFromRoom(Context context) throws Exception {
        User user = getRequiredUser(context);
        String roomCode = context.getString("code");

        Room room = Room.find("code=:code and deleted=0").setParameter("code", roomCode).first();
        Result result = getOkResult(context);
        if (room == null) {
            result = getNotFoundResult(context);
            result.setMessage("This room not exist or it was deleted");
            return result;
        }

        if (room.getRoomType() == Room.RANDOM_TYPE) {
            return leaveFromRandomRoom(context, room);
        }


        Member member = Member.find("room=:room and user=:user and deleted=0 and status=:status")
                .setParameter("room", room)
                .setParameter("user", user)
                .setParameter("status", Member.ACTIVE_STATUS)
                .first();

        if (member == null) {
            result = getNotFoundResult(context);
            result.setMessage("This member deleted or is already leaved");
            return result;
        }
        if (user.getId().compareTo(member.getUser().getId()) == 0) {

            String redisCacheKey = room.getRedisCacheKey();
            //check if it is admin
            if (member.isAdmin()) {
                //get amount of members in this room
                long count = Member.count("room=? and deleted=0 and status=? and code!=?", room, Member.ACTIVE_STATUS, member.getCode());
                if (count == 0) {//only one member in room and it's admin
                    member.setDeleted(1);
                    member.setAdmin(false);
                    member.save();
                    RedisHelper.getInstance().remove(redisCacheKey);
                    room.setDeleted(1);
                    room.save();
                } else {
                    long amountOfAdmins = Member.count("room=? and deleted=0 and status=? and code!=? and isAdmin=true", room, Member.ACTIVE_STATUS, member.getCode());

                    if (amountOfAdmins == 0) {//find and assign new admin
                        List<Member> members = Member.find("room=:room and deleted=0 and status=:status and code!=:code and isAdmin=false")//get users that not admins
                                .setParameter("room", room)
                                .setParameter("status", Member.ACTIVE_STATUS)
                                .setParameter("code", member.getCode())
                                .fetch();


                        Member newAdmin = members.stream().max(Comparator.comparing(Member::getCreationDate).reversed()).get();//get very old member(by joining date)
                        if (newAdmin.getCode().compareTo(member.getCode()) == 0) {
                            return getWTFResult(context, "leaveFromRoom 546");
                        }
                        newAdmin.setAdmin(true);
                        newAdmin.save();
                        causeBroadcastRoomEvent(
                                roomCode,
                                ServiceEvent.CODE_ADMIN_WAS_ASSIGNED,
                                String.format(ServiceEvent.MESSAGE_ADMIN_WAS_ASSIGNED,
                                        user.getUsername(),
                                        newAdmin.getUser().getUsername()
                                ),
                                newAdmin.getUser().getCode()
                        );
                    }
                    member.setDeleted(1);
                    member.setAdmin(false);
                    member.save();
                    causePersonalEvent(user.getCode(),
                            room.getCode(),
                            ServiceEvent.CODE_USER_LEAVED_FROM_ROOM,// old CODE_CURRENT_USER_WAS_LEAVED_FROM_ROOM
                            ServiceEvent.MESSAGE_CURRENT_USER_WAS_LEAVED_FROM_ROOM);

                    causeBroadcastRoomEvent(
                            roomCode,
                            ServiceEvent.CODE_USER_LEAVED_FROM_ROOM,
                            String.format(ServiceEvent.MESSAGE_USER_LEAVED_FROM_ROOM, user.getUsername()
                            ),
                            user.getCode());
                }
            } else {
                member.setDeleted(1);
                member.save();
                causePersonalEvent(user.getCode(),
                        roomCode,
                        ServiceEvent.CODE_USER_LEAVED_FROM_ROOM,// old CODE_CURRENT_USER_WAS_LEAVED_FROM_ROOM
                        ServiceEvent.MESSAGE_CURRENT_USER_WAS_LEAVED_FROM_ROOM);

                causeBroadcastRoomEvent(
                        roomCode,
                        ServiceEvent.CODE_USER_LEAVED_FROM_ROOM,
                        String.format(ServiceEvent.MESSAGE_USER_LEAVED_FROM_ROOM, user.getUsername()
                        ),
                        user.getCode());
            }
        } else {
            return getWTFResult(context, "leaveFromRoom");
        }
        return result;
    }

    public static Result leaveFromRandomRoom(Context context, Room room) {
        Result result = getOkResult(context);
        Member.find("status=:status and deleted=0 and room=:room")
                .setParameter("status", Member.ACTIVE_STATUS)
                .setParameter("room", room)
                .fetch()
                .stream()
                .forEach(m -> {
                            ((Member) m).setDeleted(1);
                            ((Member) m).save();
                        }
                );
        room.setDeleted(1);
        room.save();
        causeBroadcastRoomEvent(
                room.getCode(),
                ServiceEvent.CODE_USER_DELETED_ROOM,
                String.format(ServiceEvent.MESSAGE_USER_DELETED_ROOM, room.getTitle()),
                null);
        return result;
    }


    //this operation is valid if user is admin
    public static Result excludeMemberFromRoom(Context context) throws Exception {
        User admin = getRequiredUser(context);
        String memberCode = context.getString("code");
        Member member = Member.find("code=:code and deleted=0 and status=:status")
                .setParameter("code", memberCode)
                .setParameter("status", Member.ACTIVE_STATUS)
                .first();//find member that will be excluded

        Result result = null;

        if (member == null) {
            result = getNotFoundResult(context);
            result.setMessage("This member deleted or is already excluded");
            return result;
        }

        Room room = member.getRoom();

        Member adminMember = Member.find("user=:user and room=:room and isAdmin=true and deleted=0 and status=:status")
                .setParameter("user", admin)
                .setParameter("room", room)
                .setParameter("status", Member.ACTIVE_STATUS)
                .first();

        if (adminMember == null) {//check for admin existance
            result = getForbiddenOperationErrorResult(context, "You are not an admin of this room and you are not exist in this room");
            return result;
        }

        if (adminMember.getId().compareTo(member.getId()) == 0) {//check for self exclusion
            return getForbiddenOperationErrorResult(context, context.getMessage(Context.KEY_MESSAGE_ERROR_ROOM_EXCLUDE_SELF));
        }

        result = getOkResult(context);
        member.setDeleted(1);
        member.setAdmin(false);
        member.save();
        result.setMessage("Member with code " + member.getCode() + " was excluded from room");

        causePersonalEvent(member.getUser().getCode(),
                room.getCode(),
                ServiceEvent.CODE_USER_WAS_EXCLUDED,// old CODE_CURRENT_USER_WAS_EXCLUDED_FROM_ROOM
                String.format(ServiceEvent.MESSAGE_CURRENT_USER_WAS_EXCLUDED_FROM_ROOM, admin.getUsername(), member.getRoom().getTitle()));

        causeBroadcastRoomEvent(
                room.getCode(),
                ServiceEvent.CODE_USER_WAS_EXCLUDED,
                String.format(ServiceEvent.MESSAGE_USER_WAS_EXCLUDED,
                        admin.getUsername(),
                        member.getUser().getUsername()),
                member.getUser().getCode());

        //delete this member from member list in Redis
//        String roomKey = member.getRoom().getRedisCacheKey();
//        RedisHelper.getInstance().lrem(roomKey, String.valueOf(member.getUser().getCode()));

        return result;
    }


    public static Result blockMember(Context context) throws Exception {
        User admin = getRequiredUser(context);
        String memberCode = context.getString("code");
        Member member = Member.find("code=:code and deleted=0 and status=:status")
                .setParameter("code", memberCode)
                .setParameter("status", Member.ACTIVE_STATUS)
                .first();//find member that will be excluded

        Result result = null;

        if (member == null) {
            result = getNotFoundResult(context);
            result.setMessage("This member deleted or is already blocked");
            return result;
        }

        Room room = member.getRoom();

        Member adminMember = Member.find("user=:user and room=:room and isAdmin=true and deleted=0 and status=:status")
                .setParameter("user", admin)
                .setParameter("room", room)
                .setParameter("status", Member.ACTIVE_STATUS)
                .first();

        if (adminMember == null){//check for admin existance
            result = getForbiddenOperationErrorResult(context, "You are not an admin of this room and you are not exist in this room");
            return result;
        }

        if (adminMember.getId().compareTo(member.getId()) == 0) {//check for self exclusion
            return getForbiddenOperationErrorResult(context, context.getMessage(Context.KEY_MESSAGE_ERROR_ROOM_BLOCK_SELF));
        }

        result = getOkResult(context);
        member.setDeleted(1);
        member.setAdmin(false);
        member.setStatus(Member.BLOCKED_STATUS);
        member.save();
        result.setMessage("Member with code " + member.getCode() + " was added to black list");
        //delete this member from member list in Redis
        causePersonalEvent(member.getUser().getCode(),
                room.getCode(),
                ServiceEvent.CODE_USER_WAS_BLOCKED,// old CODE_CURRENT_USER_WAS_BLOCKED_IN_ROOM
                String.format(ServiceEvent.MESSAGE_USER_WAS_BLOCKED_IN_ROOM, admin.getUsername(), member.getRoom().getTitle()));

        causeBroadcastRoomEvent(
                room.getCode(),
                ServiceEvent.CODE_USER_WAS_BLOCKED,
                String.format(ServiceEvent.MESSAGE_USER_WAS_BLOCKED,
                        admin.getUsername(),
                        member.getUser().getUsername()),
                member.getUser().getCode());


//        String roomKey = member.getRoom().getRedisCacheKey();
//        RedisHelper.getInstance().lrem(roomKey, String.valueOf(member.getUser().getCode()));

        return result;
    }

    public static Result unblockMember(Context context) throws Exception {
        User admin = getRequiredUser(context);
        String memberCode = context.getString("code");
        Member member = Member.find("code=:code")
                .setParameter("code", memberCode)
                .first();//find member that will be excluded

        Result result = null;

        if (member == null) {
            result = getNotFoundResult(context);
            result.setMessage("This member is not deleted and not blocked");
            return result;
        }

        Room room = member.getRoom();

        Member adminMember = Member.find("user=:user and room=:room and isAdmin=true and deleted=0 and status=:status")
                .setParameter("user", admin)
                .setParameter("room", room)
                .setParameter("status", Member.ACTIVE_STATUS)
                .first();

        if (adminMember == null) {//check for admin existance
            result = getForbiddenOperationErrorResult(context, "You are not an admin of this room and you are not exist in this room");
            return result;
        }

        if (adminMember.getId().compareTo(member.getId()) == 0) {//check for self exclusion
            return getForbiddenOperationErrorResult(context, context.getMessage("You are not blocked"));
        }

        result = getOkResult(context);
//        member.setDeleted(0);
        member.setAdmin(false);
        member.setStatus(Member.ACTIVE_STATUS);
        member.save();
        result.setMessage("Member with code " + member.getCode() + " was unblocked");
        //delete this member from member list in Redis
//        String roomKey = member.getRoom().getRedisCacheKey();

        causePersonalEvent(member.getUser().getCode(),
                room.getCode(),
                ServiceEvent.CODE_USER_WAS_UNBLOCKED,// old CODE_CURRENT_USER_WAS_UNBLOCKED_IN_ROOM
                String.format(ServiceEvent.MESSAGE_CURRENT_USER_WAS_UNBLOCKED_IN_ROOM, admin.getUsername(), member.getRoom().getTitle()));

        causeBroadcastRoomEvent(
                room.getCode(),
                ServiceEvent.CODE_USER_WAS_UNBLOCKED,
                String.format(ServiceEvent.MESSAGE_USER_WAS_UNBLOCKED,
                        admin.getUsername(),
                        member.getUser().getUsername()),
                member.getUser().getCode());
//        RedisHelper.getInstance().lpush(roomKey, String.valueOf(member.getUser().getCode()));
        return result;
    }

    public static Result assignAdmin(Context context) throws Exception {
        User admin = getRequiredUser(context);
        String memberCode = context.getString("code");
        Member member = Member.find("code=:code and deleted=0 and status=:status")
                .setParameter("code", memberCode)
                .setParameter("status", Member.ACTIVE_STATUS)
                .first();//find member that will be excluded

        Result result = null;
        if (member == null) {
            result = getNotFoundResult(context);
            result.setMessage("This member deleted or is already excluded");
            return result;
        }

        Room room = member.getRoom();
        Member adminMember = Member.find("user=:user and room=:room and isAdmin=true and deleted=0 and status=:status")
                .setParameter("user", admin)
                .setParameter("room", room)
                .setParameter("status", Member.ACTIVE_STATUS)
                .first();

        if (adminMember == null) {//check for admin existance
            result = getForbiddenOperationErrorResult(context, "You are not an admin of this room and you are not exist in this room");
            return result;
        }

        if (adminMember.getId().compareTo(member.getId()) == 0) {//check for self exclusion
            return getForbiddenOperationErrorResult(context, context.getMessage(Context.KEY_MESSAGE_ERROR_ROOM_EXCLUDE_SELF));
        }
        result = getOkResult(context);
        member.setAdmin(true);
        member.save();

        causePersonalEvent(member.getUser().getCode(),
                room.getCode(),
                ServiceEvent.CODE_ADMIN_WAS_ASSIGNED,// old CODE_CURRENT_USER_WAS_ASSIGNED_TO_ADMIN
                String.format(ServiceEvent.MESSAGE_CURRENT_USER_WAS_ASSIGNED_TO_ADMIN, admin.getUsername(),member.getRoom().getTitle()));

        causeBroadcastRoomEvent(
                room.getCode(),
                ServiceEvent.CODE_ADMIN_WAS_ASSIGNED,
                String.format(ServiceEvent.MESSAGE_ADMIN_WAS_ASSIGNED, admin.getUsername(), member.getUser().getUsername()
                ),
                member.getUser().getCode());

        result.setMessage("Member with code " + member.getCode() + " now admin");
        return result;
    }

    public static Result debarAdmin(Context context) throws Exception {
        User admin = getRequiredUser(context);
        String memberCode = context.getString("code");
        Member member = Member.find("code=:code and deleted=0 and status=:status")
                .setParameter("code", memberCode)
                .setParameter("status", Member.ACTIVE_STATUS)
                .first();//find member that will be excluded

        Result result = null;
        if (member == null) {
            result = getNotFoundResult(context);
            result.setMessage("This member deleted or is already excluded");
            return result;
        }
        Room room = member.getRoom();
        Member adminMember = Member.find("user=:user and room=:room and isAdmin=true and deleted=0 and status=:status")
                .setParameter("user", admin)
                .setParameter("room", room)
                .setParameter("status", Member.ACTIVE_STATUS)
                .first();
        if (adminMember == null) {//check for admin existance
            result = getForbiddenOperationErrorResult(context, "You are not an admin of this room and you are not exist in this room");
            return result;
        }
        if (adminMember.getId().compareTo(member.getId()) == 0) {//check for self exclusion
            return getForbiddenOperationErrorResult(context, context.getMessage(Context.KEY_MESSAGE_ERROR_ROOM_EXCLUDE_SELF));
        }
        result = getOkResult(context);
        member.setAdmin(false);
        member.save();

        causePersonalEvent(member.getUser().getCode(),
                room.getCode(),
                ServiceEvent.CODE_ADMIN_WAS_DEBAR_,// CODE_CURRENT_USER_WAS_DEBARED_FROM_ADMIN
                String.format(ServiceEvent.MESSAGE_CURRENT_USER_WAS_DEBARED_FROM_ADMIN, admin.getUsername(),member.getRoom().getTitle()));

        causeBroadcastRoomEvent(
                room.getCode(),
                ServiceEvent.CODE_ADMIN_WAS_DEBAR_,
                String.format(ServiceEvent.MESSAGE_ADMIN_WAS_DEBAR, admin.getUsername(), member.getUser().getUsername()
                ),
                member.getUser().getCode());

        result.setMessage("Member with code " + member.getCode() + " now not admin");
        return result;
    }

}
