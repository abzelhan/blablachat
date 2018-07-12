package utils.providers;

import com.google.gson.Gson;
import javassist.NotFoundException;
import kz.api.json.Command;
import kz.api.json.Context;
import kz.api.json.Result;
import kz.wg.utils.Achtung;
import models.User;
import play.cache.Cache;
import play.mvc.Before;

import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by abzalsahitov@gmail.com  on 3/23/18.
 */
public class ResultFactory {


    public static int OK = 200;
    public static int NOT_FOUND = 404;
    public static int INCORRECT_DATA = 400;
    //Custom params
    public static int ALREADY_EXIST = 201;
    public static int NOT_ENOGHT_PARAMS = 202;

    public static int SERVER_ERROR = 500;
    public static int ACCESS_DENIED = 403;
    public static int AUTHORIZATION_REQUIRED = 401;

    public static int OVERFLOW = 405;
    public static int USER_NOT_MEMBER_IN_ROOM = 406;
    public static int USER_IS_BLOCKED_IN_ROOM = 407;

    public static int USER_NOT_ADMIN_TO_DO_THIS_OPERATION = 408;

    public static int UNMODIFIABLE_ROOM = 409;
    public static int USER_IS_ALREADY_MEMBER_OF_ROOM = 410;



    public static Result getForbiddenOperationErrorResult(Context context, String message) {
        Result result = new Result();
        result.setCommand(context.getCommand());
        result.setStatus(USER_NOT_ADMIN_TO_DO_THIS_OPERATION);
        result.setMessage(message);
        return result;
    }

    public static void notNull(Object object) throws NotFoundException {
        if (object == null) {
            throw new NotFoundException("");
        }
    }

    public static void notNull(Object object, String message) throws NotFoundException {
        if (object == null) {
            throw new NotFoundException(message);
        }
    }

    public static User getRequiredUser(Context context) throws AccessControlException {
        User user = getOptionalUser(context);

        if (user == null) {
            throw new AccessControlException("You need call adminLogin");
        } else {
            return user;
        }
    }

    public static User getOptionalUser(Context context) {
        return User.find("id=:id and deleted=0").setParameter("id", context.getUser_id()).first();
    }

    public static Result getOkResult(Context context) {
        Result result = new Result();
        result.setCommand(context.getCommand());
        result.setStatus(OK);
        return result;
    }

    public static Result getAlreadyMemberErrorResult(Context context) {
        Result result = new Result();
        result.setCommand(context.getCommand());
        result.setStatus(USER_IS_ALREADY_MEMBER_OF_ROOM);
        return result;
    }

    public static Result getOverflowError(Context context) {
        Result result = new Result();
        result.setCommand(context.getCommand());
        result.setStatus(OVERFLOW);
        return result;
    }

    public static Result getAuthErrorResult(Context context){
        Result result = new Result();
        result.setCommand(context.getCommand());
        result.setStatus(AUTHORIZATION_REQUIRED);
        result.setMessage(context.getMessage(Context.KEY_MESSAGE_ERROR_AUTH_INCORRECT));
        return result;
    }

    public static Result getOkResult(Command command) {
        Result result = new Result();
        result.setCommand(command.getCommand());
        result.setStatus(OK);
        return result;
    }

    public static Result getNotFoundResult(Command command) {
        Result result = new Result();
        result.setCommand(command.getCommand());
        result.setStatus(NOT_FOUND);
        return result;
    }

    public static Result getNotFoundResult(Context context) {
        Result result = new Result();
        result.setCommand(context.getCommand());
        result.setStatus(NOT_FOUND);
        return result;
    }

    public static Result getNotMemberErrorResult(Context context){
        Result result = new Result();
        result.setCommand(context.getCommand());
        result.setStatus(USER_NOT_MEMBER_IN_ROOM);
        return result;
    }

    public static Result getMemberIsBlockedErrorResult(Context context){
        Result result = new Result();
        result.setCommand(context.getCommand());
        result.setStatus(USER_IS_BLOCKED_IN_ROOM);
        return result;
    }

    public static Result getNotAdminMemberErrorResult(Context context){
        Result result = new Result();
        result.setCommand(context.getCommand());
        result.setStatus(USER_NOT_ADMIN_TO_DO_THIS_OPERATION);
        return result;
    }

    public static Result getNotModifiableErrorResult(Context context){
        Result result = new Result();
        result.setCommand(context.getCommand());
        result.setStatus(UNMODIFIABLE_ROOM);
        return result;
    }

    public static Result getWTFResult(Context context,String methodName){
        Result result = new Result();
        result.setCommand(context.getCommand());
        result.setMessage("WTF Result in method - "+methodName);
        result.setStatus(500);
        return result;
    }

    public static Result getIncorrectResult() {
        Result result = new Result();
        result.setStatus(INCORRECT_DATA);
        return result;
    }

    public static Result getCustomResult(Command command, int code) {
        Result result = new Result();
        result.setCommand(command.getCommand());
        result.setStatus(code);
        return result;
    }

    public static Result getCustomResult(Context context, int code) {
        Result result = new Result();
        result.setCommand(context.getCommand());
        result.setStatus(code);
        return result;
    }

    public static Object getCommand(String data, Class c) {
        Gson gson = new Gson();
        return gson.fromJson(data, c);
    }

    public static Result getAlreadyExistResult(Context context,String field,String value){
        Result result = new Result();
        result.setCommand(context.getCommand());
        result.setStatus(ALREADY_EXIST);
        result.setMessage(context.getMessage(Context.KEY_MESSAGE_ERROR_DATABASE_ELEMENT_ALREADY_EXIST,field,value));
        return result;
    }

    public static Result getInvalidParameterValueRangeResult(Context context,String field,int min,int max){
        Result result = new Result();
        result.setCommand(context.getCommand());
        result.setStatus(INCORRECT_DATA);
        result.setMessage(context.getMessage(Context.KEY_MESSAGE_ERROR_JSON_OUT_OF_RANGE, field, min, max));
        return result;
    }

    public static Result getInvalidParameterValueResult(Context context,String field,String value){
        Result result = new Result();
        result.setCommand(context.getCommand());
        result.setStatus(INCORRECT_DATA);
        result.setMessage(context.getMessage(Context.KEY_MESSAGE_ERROR_JSON_INVALID_VALUE,field,value));
        return result;
    }
//
//    public static boolean addAchtung(String text, String type, int delay) {
//        Achtung a = new Achtung(text, type, delay);
//        List<Achtung> messages = Cache.get(session.getId() + "-achtung", List.class);
//        if (messages == null) {
//            messages = new ArrayList<Achtung>();
//        }
//        messages.add(a);
//        Cache.set(session.getId() + "-achtung", messages, "30mn");
//        return true;
//    }

    public static User getCurrentUser(Context context) {
        String userId = context.getSession().get("user");
        System.out.println("userID: " + userId);

        if (context.getRequest().headers.containsKey("token")) {
            System.out.println("token: " + context.getRequest().headers.get("token").value());

            //   userId = (String) RedisHelper.getInstance().get(request.headers.get("token").value());

            System.out.println("got userid: " + userId);
        }

        if (userId != null) {
            try {
                User user = User.find("id=?", Long.parseLong(userId)).first();//Cache.get(session.getId() + "-user", User.class);
                return user;
                //return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {

            return null;
        }
    }

//    @Before
//    static void addDefaults() {
//        //request.secure = true;
//        renderArgs.put("user", getCurrentUser());
//        response.accessControl("*");
//        response.setHeader("Access-Control-Allow-Headers", "*");
//    }


}
