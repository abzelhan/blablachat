package controllers;

import kz.api.json.Context;
import kz.api.json.Result;
import kz.api.json.System.ServiceEvent;
import models.Invite;
import models.Member;
import models.Room;
import models.User;
import utils.helpers.RedisHelper;
import utils.providers.ResultFactory;

import java.util.Calendar;
import java.util.UUID;

import static utils.providers.ResultFactory.*;

/**
 * Created by abzalsahitov@gmail.com  on 3/26/18.
 */
public class InviteApi extends BaseApi {

    public static Result deleteInvite(Context context){
        User currentUser = getRequiredUser(context);
        String roomCode = context.getString("code");
        Room room = Room.find("code=:code and deleted=0 and roomType!=2").setParameter("code", roomCode).first();
        if (room == null) {
            Result result = ResultFactory.getNotFoundResult(context);
            result.setMessage("Room with this code not finded, or it not supports invites");
            return result;
        }


        Member admin = Member
                .find("room=:room and user=:user and deleted=0 and status=0 and isAdmin=true")
                .setParameter("room",room)
                .setParameter("user",currentUser)
                .first();
        if(admin==null){
            Result accessDeniedResult = ResultFactory.getNotAdminMemberErrorResult(context);
            accessDeniedResult.setMessage("This operation is only for admins");
            return accessDeniedResult;
        }

        Invite oldInvite = Invite.find("room=:room and deleted=0").setParameter("room", room).first();
        if (oldInvite != null) {
            oldInvite.setDeleted(1);
            oldInvite.save();
        }

        Result result = ResultFactory.getOkResult(context);
        result.setMessage("Invite succesfully deleted");
        return result;
    }

    //только админ может создавать эту ссылку
    public static Result generateInvite(Context context) {
        User currentUser = getRequiredUser(context);
        String roomCode = context.getString("code");
        Room room = Room.find("code=:code and deleted=0 and roomType!=2").setParameter("code", roomCode).first();
        if (room == null) {
            Result result = ResultFactory.getNotFoundResult(context);
            result.setMessage("Room with this code not finded, or it not supports invites");
            return result;
        }


        Member admin = Member
                .find("room=:room and user=:user and deleted=0 and status=0 and isAdmin=true")
                .setParameter("room",room)
                .setParameter("user",currentUser)
                .first();
        if(admin==null){
            Result accessDeniedResult = ResultFactory.getNotAdminMemberErrorResult(context);
            accessDeniedResult.setMessage("This operation is only for admins");
            return accessDeniedResult;
        }

        long roomSize = Member.count("room=? and status=? and deleted=0", room, Member.ACTIVE_STATUS);
        long currentRoomLimit = room.getRoomLimit();
        if (roomSize == currentRoomLimit) {
            Result result = ResultFactory.getOverflowError(context);
            result.setMessage("The room is full. You need to increase your room limit before creating invite link");
            return result;
        }


        Invite invite = new Invite();
        invite.setRoom(room);
        String description = context.getOptionalString("description");
        if (description != null) {
            if (description.length() < 1 || description.length() > 2048) {
                return getInvalidParameterValueRangeResult(context, "description", 1, 2048);
            }
            invite.setDescription(description);
        }
        invite.setInviter(currentUser);
        Integer limit = context.getOptionalIntegerOrNull("limit");
        if (limit != null) {
            if (limit < 1 || limit > (currentRoomLimit-roomSize)) {
                Result result = ResultFactory.getCustomResult(context, 400);
                result.setMessage("Invite limit can't be bigger that room limit. It must be equal or less to room limit. It's can't be less to 0");
                return result;
            }

            invite.setFreeSize(limit);
        }else{
            invite.setFreeSize(1);
        }

//        invite.setUrl(Invite.siteUrl.concat("/invite/").concat(room.getCode()));
        invite.setUrl(Invite.siteUrl.concat("/invite/").concat(UUID.randomUUID().toString().substring(0,10)));
        invite.setCreationDate(Calendar.getInstance());
        //before save delete old invite if it exists
        Invite oldInvite = Invite.find("room=:room and deleted=0").setParameter("room", room).first();
        if (oldInvite != null) {
            oldInvite.setDeleted(1);
            oldInvite.save();
        }

        invite.save();
        Result result = ResultFactory.getOkResult(context);
        result.setInvite(invite.getJson());
        return result;
    }


    public static Result joinToRoomByInviteUrl(Context context) {
        User currentUser = getRequiredUser(context);
        String code = context.getString("code");
        Invite invite = Invite.find("code=:code and deleted=0")
                .setParameter("code", code)
                .first();
        if (invite == null) {
            Result result = ResultFactory.getNotFoundResult(context);
            result.setMessage("This invite is expires or is not exist");
            return result;
        }
//        Room room = Room.find("room=:room and deleted=0").setParameter("room").first();
        Room room = invite.getRoom();
        if (room == null) {
            Result result = ResultFactory.getNotFoundResult(context);
            result.setMessage("room not exist or it was deleted");
            return result;
        }

        long roomSize = Member.count("room=? and status=? and deleted=0", room, Member.ACTIVE_STATUS);
        if (roomSize == room.getRoomLimit()) {
            Result result = getOverflowError(context);
            result.setMessage("Группа переполнена");
            return result;
        }

        Member member = Member.byUserAndRoom(currentUser, room);

        if (member != null) {
//            if (member.getStatus() == Member.EXCLUDED_STATUS) {
//                Result result = getCustomResult(context, 403);
//                result.setMessage("Вы были исключены из группы");
//                return result;
//            }
            if (member.getStatus() == Member.BLOCKED_STATUS) {
                Result result = getMemberIsBlockedErrorResult(context);
                result.setMessage("Вас заблокировали в этой группе");
                return result;
            }
            if (member.getDeleted() == 1) {//восстановление чувака, публичная группа
                member.setDeleted(0);
                member.setLastModificationDate(Calendar.getInstance());
                member.save();
                Result result = getOkResult(context);
                result.setMessage("Ваше участие восстановлено");
                return result;
            } else {
                Result result = getAlreadyMemberErrorResult(context);
                result.setMessage("Вы уже состоите в этой группе");
                return result;
            }
        }

        if (member == null) {
            member = new Member();
            member.setCreationDate(Calendar.getInstance());
            member.setPushEnabled(true);
            member.setAdmin(false);
            member.setRoom(room);
            member.setUser(currentUser);
            member.save();
        }
        int allowInviteSize = invite.getFreeSize() - 1;
        invite.setFreeSize(allowInviteSize);
        if(allowInviteSize==0){
            invite.setDeleted(1);
        }
        invite.save();
        //store member id to redis cache
        String roomKey = room.getRedisCacheKey();
        RedisHelper.getInstance().lrem(roomKey, member.getUser().getCode());
        RedisHelper.getInstance().lpush(roomKey, String.valueOf(member.getUser().getCode()));
        causePersonalEvent(currentUser.getCode(),
                room.getCode(),
                ServiceEvent.CODE_USER_JOINED_TO_PUBLIC_ROOM,//old CODE_CURRENT_USER_WAS_JOINED_TO_ROOM
                String.format(ServiceEvent.MESSAGE_CURRENT_USER_WAS_JOINED_TO_ROOM,room.getTitle()));


        causeBroadcastRoomEvent(
                room.getCode(),
                ServiceEvent.CODE_USER_JOINED_TO_PUBLIC_ROOM,
                String.format(ServiceEvent.MESSAGE_USER_JOINED_TO_PUBLIC_ROOM, currentUser.getUsername()),
                currentUser.getCode()
        );

        Result result = ResultFactory.getOkResult(context);
        result.setMember(member.getJson());
        result.setRoom(room.getOptionalJson(true));
        return result;
    }


    public static Result getInviteInfo(Context context) {
        User currentUser = getRequiredUser(context);
        String inviteUrl = context.getString("url");
        Invite invite = Invite.find("url=:url and deleted=0").setParameter("url", inviteUrl).first();
        if (invite == null) {
            Result result = ResultFactory.getNotFoundResult(context);
            result.setMessage("This invite is expires or is not exist");
            return result;
        }
        Result result = ResultFactory.getOkResult(context);
        result.setInvite(invite.getJson());
        return result;
    }

}
