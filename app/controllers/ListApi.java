package controllers;

import kz.api.json.Context;
import kz.api.json.LanguageJson;
import kz.api.json.Result;
import kz.api.json.Room.RoomJson;
import models.Language;
import models.Member;
import models.Room;
import models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static utils.providers.ResultFactory.getOkResult;

/**
 * Created by abzalsahitov@gmail.com  on 3/23/18.
 */
public class ListApi extends BaseApi {

    public static Result getLanguages(Context context) {
        List<Language> languages = Language.findAll();
        List<LanguageJson> languageJsons = new ArrayList<>();
        for (Language lang :
                languages) {
            languageJsons.add(lang.getJson());
        }
        Result result = getOkResult(context);
        result.setLanguages(languageJsons);
        return result;
    }

    //сортировать по количеству участников
    public static Result getPublicRoomsList(Context context) throws Exception {
        User currentUser = getRequiredUser(context);
        int page = context.getInteger("page");
        int perpage = context.getInteger("perPage");
        if (perpage == 0) {
            perpage = 10;
        }
        List<Room> publicRooms = Room.find("roomType=0 and deleted=0").fetch(page, perpage);
        long roomCount = Room.count("roomType=0 and deleted=0");
        Result result = getOkResult(context);
        List<RoomJson> rooms = new ArrayList<>();
        for (Room room : publicRooms) {
            rooms.add(room.getOptionalJson(false));
        }
        result.setPublicRooms(rooms);
        result.setTotalCount(roomCount);
        return result;
    }

    public static Result getRoomsListOfUser(Context context) throws Exception {
        User currentUser = getRequiredUser(context);
        int page = context.getInteger("page");
        int perpage = context.getInteger("perPage");
        if (perpage == 0) {
            perpage = 10;
        }
        Result result = getOkResult(context);
        result.setRooms(
                Member.find("user=:user and status=0 and deleted=0 order by creationDate desc")
                        .setParameter("user", currentUser)
                        .fetch(page, perpage)
                        .stream().
                        map(m -> ((Member) m).getRoom().getOptionalJson(false))
                        .collect(Collectors.toList())
        );
        result.setTotalCount(Member.count("user=? and status=0 and deleted=0", currentUser));
        return result;
    }



}
