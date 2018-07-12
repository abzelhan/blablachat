package controllers;

import com.google.gson.Gson;
import javassist.NotFoundException;
import kz.api.json.Command;
import kz.api.json.Context;
import kz.api.json.Result;
import kz.wg.utils.Achtung;
import models.User;
import play.cache.Cache;
import play.mvc.Before;
import play.mvc.Controller;

import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by baha on 6/10/15.
 */
public class Parent extends Controller {
    public static int OK = 200;
    public static int NOT_FOUND = 404;
    public static int INCORRECT_DATA = 400;
    //Custom params
    public static int ALREADY_EXIST = 201;
    public static int NOT_ENOGHT_PARAMS = 202;

    public static int SERVER_ERROR = 500;
    public static int ACCESS_DENIED = 403;
    public static int AUTHORIZATION_REQUIRED = 401;


    protected static Result getForbiddenOperationErrorResult(Context context,String message) {
        Result result = new Result();
        result.setCommand(context.getCommand());
        result.setStatus(ACCESS_DENIED);
        result.setMessage(message);
        return result;
    }

    protected static void notNull(Object object) throws NotFoundException {
        if (object == null) {
            throw new NotFoundException("");
        }
    }

    protected static void notNull(Object object, String message) throws NotFoundException {
        if (object == null) {
            throw new NotFoundException(message);
        }
    }

    protected static User getRequiredUser(Context context) throws AccessControlException {
        User user = getOptionalUser(context);

        if (user == null) {
            throw new AccessControlException("You need call adminLogin");
        } else {
            return user;
        }
    }

    protected static User getOptionalUser(Context context) {
        return User.find("id=:id and deleted=0").setParameter("id", context.getUser_id()).first();
    }

    protected static Result getOkResult(Context context) {
        Result result = new Result();
        result.setCommand(context.getCommand());
        result.setStatus(OK);
        return result;
    }

    protected static Result getAuthErrorResult(Context context){
        Result result = new Result();
        result.setCommand(context.getCommand());
        result.setStatus(AUTHORIZATION_REQUIRED);
        result.setMessage(context.getMessage(Context.KEY_MESSAGE_ERROR_AUTH_INCORRECT));
        return result;
    }

    protected static Result getOkResult(Command command) {
        Result result = new Result();
        result.setCommand(command.getCommand());
        result.setStatus(OK);
        return result;
    }

    protected static Result getNotFoundResult(Command command) {
        Result result = new Result();
        result.setCommand(command.getCommand());
        result.setStatus(NOT_FOUND);
        return result;
    }

    protected static Result getNotFoundResult(Context context) {
        Result result = new Result();
        result.setCommand(context.getCommand());
        result.setStatus(NOT_FOUND);
        return result;
    }

    protected static Result getWTFResult(Context context,String methodName){
        Result result = new Result();
        result.setCommand(context.getCommand());
        result.setMessage("WTF Result in method - "+methodName);
        result.setStatus(500);
        return result;
    }

    protected static Result getIncorrectResult() {
        Result result = new Result();
        result.setStatus(INCORRECT_DATA);
        return result;
    }

    protected static Result getCustomResult(Command command, int code) {
        Result result = new Result();
        result.setCommand(command.getCommand());
        result.setStatus(code);
        return result;
    }

    protected static Result getCustomResult(Context context, int code) {
        Result result = new Result();
        result.setCommand(context.getCommand());
        result.setStatus(code);
        return result;
    }

    protected static Object getCommand(String data, Class c) {
        Gson gson = new Gson();
        return gson.fromJson(data, c);
    }

    protected static Result getAlreadyExistResult(Context context,String field,String value){
        Result result = new Result();
        result.setCommand(context.getCommand());
        result.setStatus(ALREADY_EXIST);
        result.setMessage(context.getMessage(Context.KEY_MESSAGE_ERROR_DATABASE_ELEMENT_ALREADY_EXIST,field,value));
        return result;
    }

    protected static Result getInvalidParameterValueRangeResult(Context context,String field,int min,int max){
        Result result = new Result();
        result.setCommand(context.getCommand());
        result.setStatus(INCORRECT_DATA);
        result.setMessage(context.getMessage(Context.KEY_MESSAGE_ERROR_JSON_OUT_OF_RANGE, field, min, max));
        return result;
    }

    protected static Result getInvalidParameterValueResult(Context context,String field,String value){
        Result result = new Result();
        result.setCommand(context.getCommand());
        result.setStatus(INCORRECT_DATA);
        result.setMessage(context.getMessage(Context.KEY_MESSAGE_ERROR_JSON_INVALID_VALUE,field,value));
        return result;
    }

    public static boolean addAchtung(String text, String type, int delay) {
        Achtung a = new Achtung(text, type, delay);
        List<Achtung> messages = Cache.get(session.getId() + "-achtung", List.class);
        if (messages == null) {
            messages = new ArrayList<Achtung>();
        }
        messages.add(a);
        Cache.set(session.getId() + "-achtung", messages, "30mn");
        return true;
    }

    public static User getCurrentUser() {
        String userId = session.get("user");
        System.out.println("userID: " + userId);

        if (request.headers.containsKey("token")) {
            System.out.println("token: " + request.headers.get("token").value());

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

    @Before
    static void addDefaults() {
        //request.secure = true;
        renderArgs.put("user", getCurrentUser());
        response.accessControl("*");
        response.setHeader("Access-Control-Allow-Headers", "*");
    }

    public static boolean nullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }
}
