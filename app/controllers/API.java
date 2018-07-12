package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kz.api.json.Context;
import kz.api.json.Result;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.commons.lang.exception.ExceptionUtils;
import play.Logger;
import play.libs.F;
import utils.helpers.RedisHelper;

import java.security.AccessControlException;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by bakhyt on 10/4/17.
 */
public class API extends Parent {

    public static final String REQUEST_DELIMETER = "________________________________";

    public static void rest(String data, String checksum, String token, String source) {
        StringBuilder requestInfoStr = new StringBuilder();
        requestInfoStr.append("\n\n").append(REQUEST_DELIMETER);
        Calendar requestTime = Calendar.getInstance();
        requestInfoStr.append("\n")
                .append(requestTime.getTime())
                .append("\ngot rest command: ")
                .append(data);


        if (token == null) {
            if (request.headers.get("token") != null) {
                token = request.headers.get("token").value();
            }
            System.out.println(token);
            if (token == null) {
                token = "";
            }
        }
        requestInfoStr.append("token: ").append(token);

//        String value = RedisHelper.getInstance().get(token);

        //String value = (String) Cache.get(token);
        String value = RedisHelper.getInstance().get(token);

//        System.out.println(token + " ===> " + value);

        Long userid = 0l;

        try {
            userid = value != null ? Long.parseLong(value) : 0l;
        } catch (Exception e) {
        }


        JSONObject command = null;

        String commandName = null;
        JSONObject params = null;

        try {
            command = (JSONObject) JSONSerializer.toJSON(data);
            commandName = command.getString("command");

        } catch (Exception e) {
            Result r = new Result();
            r.setMessage("Incorrect data: " + e.getMessage());
            r.setStatus(INCORRECT_DATA);
            renderJSON(r);
        }
        if (command.has("params")) {
            params = command.getJSONObject("params");
        }
        long start = System.currentTimeMillis();

        final Context context = new Context(commandName, params, userid, source);

        if (request.headers.get("locale") != null) {
            context.setLocale(request.headers.get("locale").value());
        }

        if (token != null) {
            context.setToken(token);
        }

        final F.Promise promise = new F.Promise<>();
        promise.invoke(execute(context));

        await(promise);

        Result result = getCustomResult(context, 500);
        try {
            result = (Result) promise.get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        requestInfoStr.append("--->request: ")
                .append(context.getCommand())
                .append(" processed in ")
                .append((System.currentTimeMillis() - start))
                .append("ms\n");

//        System.out.println("--->request: " + context.getCommand() + " processed in " + (System.currentTimeMillis() - start) + "ms\n");

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(result);
        requestInfoStr.append("\nResult body: ").append(json).append("\n");
//        System.out.println("\nResult body: " + json + " \n");

        //  writeLog(data, context, result);
//        String s = new Gson().toJson(result);
//        System.out.println("Result->> "+s+"\n");
        requestInfoStr.append("\n\n").append(REQUEST_DELIMETER);
//        System.out.println("\n\n" + REQUEST_DELIMETER);
        Logger.info(requestInfoStr.toString());
        renderJSON(result);
    }

    public static Result execute(Context context) {
        System.out.println("executing command: " + context.getCommand() + " from " + context.getUser_id());

        Result result = getNotFoundResult(context);
        try {

            switch (context.getCommand()) {
                case "registration":
                    result = UserApi.simpleRegister(context);
                    break;
                case "login":
                    result = AuthApi.login(context);
                    break;
                case "logout":
                    result = AuthApi.logout(context);
                    break;
                case "edit_user":
                    result = UserApi.updateUserOptional(context);
                    break;
                case "delete_user":
                    result = UserApi.deleteUser(context);
                    break;
                case "get_languages":
                    result = ListApi.getLanguages(context);
                    break;
                case "search_city_by_name":
                    result = SearchApi.searchCityByName(context);
                    break;
                case "search_tag_by_value":
                    result = SearchApi.searchTagByValue(context);
                    break;
                case "check_username":
                    result = UserApi.checkFreeUsername(context);
                    break;
                case "check_password":
                    result = UserApi.checkOldPassword(context);
                    break;
                case "check_room_title":
                    result = RoomApi.checkFreeRoomTitle(context);
                    break;
                case "create_room":
                    result = RoomApi.createRoomSimple(context);
                    break;
                case "edit_room":
                    result = RoomApi.editRoom(context);
                    break;
                case "delete_room":
                    result = RoomApi.deleteRoom(context);
                    break;
                case "join_room":
                    result = MemberApi.joinToPublicRoom(context);
                    break;
                case "get_room_info":
                    result = RoomApi.getRoomInfo(context);
                    break;
                case "leave_from_room":
                    result = MemberApi.leaveFromRoom(context);
                    break;
                case "exclude_member_from_room":
                    result = MemberApi.excludeMemberFromRoom(context);
                    break;
                case "block_member":
                    result = MemberApi.blockMember(context);
                    break;
                case "unblock_member":
                    result = MemberApi.unblockMember(context);
                    break;
                case "public_room_list":
                    result = ListApi.getPublicRoomsList(context);
                    break;
                case "search_room_by_name":
                    result = SearchApi.findRoomsByName(context);
                    break;
                case "get_user_room_list":
                    result = ListApi.getRoomsListOfUser(context);
                    break;
                case "admin_assign":
                    result = MemberApi.assignAdmin(context);
                    break;
                case "admin_debar":
                    result = MemberApi.debarAdmin(context);
                    break;
                case "create_random_room":
                    result = RoomApi.createRandomRoomWithRandomUser(context);
                    break;
                case "search_user_by_criteria_list":
                    result = SearchApi.searchUserByCriteria(context);
                    break;
                case "create_room_by_user_code_list":
                    result = RoomApi.createRoomByUserCodeListAndCurrentUserInterests(context);
                    break;
                case "create_invite_to_room":
                    result = InviteApi.generateInvite(context);
                    break;
                case "delete_invite_to_room":
                    result = InviteApi.deleteInvite(context);
                    break;
                case "get_invite_info_by_url":
                    result = InviteApi.getInviteInfo(context);
                    break;
                case "join_to_room_by_invite_code":
                    result = InviteApi.joinToRoomByInviteUrl(context);
                    break;
                case "search_user_by_username":
                    result = SearchApi.findUserByUsername(context);
                    break;
            }


            if (context.getCommand().startsWith("crudList")) {
                String[] arr = {"GasolineType", "CarState", "Brand"
                        , "TransmissionType", "CarBody"
                        , "WheelType", "CarExteriorFeatures",
                        "CarInteriorFeatures", "CarOpticFeatures",
                        "CarMediaFeatures", "CarAdditionalFeatures",
                        "CarFeatures", "Country"
                        , "Car", "Model", "Color", "PartsCategory", "ServicesCategory",
                        "SosCategory", "AuctionPartsCategory", "ServiceType", "PaymentType", "ColorScheme"};
                List<String> list = Arrays.asList(arr);

                String className = context.getCommand().replaceFirst("crudList", "");
                if (list.contains(className)) {
                    //  result = CRUDRest.list(context);
                }
            }
        } catch (InvalidParameterException ipe) {
            ipe.printStackTrace(System.out);
            result = getCustomResult(context, 400);
            result.setMessage(ipe.getMessage());

        } catch (AccessControlException ace) {
            result = getCustomResult(context, 401);
            result.setMessage(context.getMessage(Context.KEY_MESSAGE_ERROR_AUTH));
        } catch (NoSuchElementException nse) {
            nse.printStackTrace(System.out);
            result = getCustomResult(context, 404);
            result.setMessage(context.getMessage(Context.KEY_MESSAGE_ERROR_DATABASE_ELEMENT_NOT_FOUND, nse.getMessage()));
        } catch (Exception e) {
            String fullStackTrace = ExceptionUtils.getFullStackTrace(e);
            String stackTrace = ExceptionUtils.getStackTrace(e);
            e.printStackTrace(System.out);
            e.printStackTrace();
            result = getCustomResult(context, 500);
            result.setMessage(e.getMessage() + "\n" + fullStackTrace + "\n\n\n\n" + stackTrace);
        }

        System.out.println("->result status: " + result.getStatus());


        return result;
    }


}
