package kz.api.json;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import play.i18n.Messages;
import play.mvc.Http;
import play.mvc.Scope;
import utils.helpers.RedisHelper;

import java.security.InvalidParameterException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by bakhyt on 10/4/17.
 */
public class Context {
    String command;
    JSONObject params;
    Long user_id;
    String source;
    String token;
    String locale = "en";
    Scope.Session session;
    Http.Request request;

    public static final String KEY_MESSAGE_ERROR_JSON_PARAMETER_NOT_EXIST = "error.json_parameter.not_exist";
    public static final String KEY_MESSAGE_ERROR_JSON_PARAMETER_NOT_EXIST_OR_EMPTY = "error.json_parameter.not_exist_or_empty";
    public static final String KEY_MESSAGE_ERROR_JSON_OUT_OF_RANGE = "error.json_parameter.out_of_range";
    public static final String KEY_MESSAGE_ERROR_JSON_INVALID_VALUE = "error.json_parameter.value_invalid";
    public static final String KEY_MESSAGE_ERROR_JSON_INVALID_FORMAT = "error.json_parameter.unknown_format";
    public static final String KEY_MESSAGE_ERROR_JSON_PARAMETER_INVALIT_TYPE = "error.json_parameter.invalid_type";
    public static final String KEY_MESSAGE_ERROR_DATABASE_ELEMENT_NOT_FOUND = "error.database.element.not_found";
    public static final String KEY_MESSAGE_ERROR_DATABASE_ELEMENT_ALREADY_EXIST = "error.database.element.already_exist";
    public static final String KEY_MESSAGE_ERROR_AUTH = "error.auth";
    public static final String KEY_MESSAGE_ERROR_AUTH_INCORRECT = "error.auth.not_found";

    public static final String KEY_MESSAGE_SUCCESS_LOGIN = "success.login";
    public static final String KEY_MESSAGE_SUCCESS_REGISTERED = "success.registration";
    public static final String KEY_MESSAGE_SUCCESS_DELETED = "success.deleted";
    public static final String KEY_MESSAGE_SUCCESS_LOGOUT = "success.logout";


    //room constants
    public static final String KEY_MESSAGE_ERROR_ROOM_EXCLUDE_SELF = "error.room.exclude.self";
    public static final String KEY_MESSAGE_ERROR_ROOM_BLOCK_SELF = "error.room.block.self";


    public String getMessage(String key, Object... params) {
        return Messages.getMessage(locale, key, params);
    }


    public Context() {
    }

    public String getOptionalCode(String name) {
        if (params.has(name)) {

            return params.getJSONObject(name).getString("code");

        } else {
            return null;
        }
    }


    public String getCode(String name) throws InvalidParameterException {
        if (params.has(name)) {
            return params.getJSONObject(name).getString("code");
        } else {
            throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_PARAMETER_NOT_EXIST, name));
        }
    }

    public String getOptionalString(String name, int minLength, int maxLength) {
        if (params.has(name) && (params.getString(name).length() >= minLength && params.getString(name).length() <= maxLength)) {
            return params.getString(name);
        } else {
            return null;
        }
    }

    public String getOptionalString(String name) {
        if (params.has(name)) {
            try {
                return params.getString(name);
            } catch (Exception e) {
                throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_PARAMETER_INVALIT_TYPE, name, "String"));
            }
        } else {
            return null;
        }
    }

    public String getStringNotEmpty(String name) throws InvalidParameterException {
        if (params.has(name) && !params.getString(name).isEmpty()) {
            return params.getString(name);
        } else {
            throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_PARAMETER_NOT_EXIST_OR_EMPTY, name));
        }
    }

    public String getStringNotEmptyInRange(String name, int minLength, int maxLength) throws InvalidParameterException {
        if (params.has(name) && !params.getString(name).isEmpty()) {
            if (params.getString(name).length() >= minLength && params.getString(name).length() <= maxLength) {
                return params.getString(name);
            } else {
                throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_OUT_OF_RANGE, name, minLength, maxLength));
            }

        } else {
            throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_PARAMETER_NOT_EXIST_OR_EMPTY, name));
        }
    }

    public String getOptionalStringNotEmpty(String name, int minLength, int maxLength) throws InvalidParameterException {
        if (params.has(name) && !params.getString(name).isEmpty() && (params.getString(name).length() >= minLength && params.getString(name).length() <= maxLength)) {
            return params.getString(name);
        } else {
            return null;
        }
    }

    public String getOptionalStringNotEmpty(String name) throws InvalidParameterException {
        if (params.has(name) && !params.getString(name).isEmpty()) {
            return params.getString(name);
        } else {
            return null;
        }
    }

    public String getString(String name) throws InvalidParameterException {
        if (params.has(name)) {
            try {
                return params.getString(name);
            } catch (Exception e) {
                throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_PARAMETER_INVALIT_TYPE, name, "String"));
            }
        } else {
            throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_PARAMETER_NOT_EXIST, name));
        }
    }


    public Calendar getCalendar(String name) throws InvalidParameterException {
        if (params.has(name)) {
            String format = "dd.MM.yyyy";
            return getCalendar(name, format);
        } else {
            throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_PARAMETER_NOT_EXIST, name));
        }
    }


    public Calendar getCalendar(String name, String format) throws InvalidParameterException {
        if (params.has(name)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            String date = params.getString(name);
            Calendar instance = null;
            try {
                Date parse = dateFormat.parse(date);
                instance = Calendar.getInstance();
                instance.setTime(parse);
            } catch (ParseException e) {
                throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_INVALID_FORMAT, name, format));
            }
            return instance;
        } else {
            throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_PARAMETER_NOT_EXIST, name));
        }
    }

    public Calendar getOptionalCalendar(String name) throws InvalidParameterException {
        if (params.has(name)) {
            String format = "dd.MM.yyyy";
            return getOptionalCalendar(name, format);
        } else {
            return null;
        }
    }

    public Calendar getOptionalCalendar(String name, String format) throws InvalidParameterException {
        if (params.has(name)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            String date = params.getString(name);
            Calendar instance = null;
            try {
                Date parse = dateFormat.parse(date);
                instance = Calendar.getInstance();
                instance.setTime(parse);
            } catch (ParseException e) {
                throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_INVALID_FORMAT, name, format));
            }
            return instance;
        } else {
            return null;
        }
    }

    public Long getLong(String name) throws InvalidParameterException {
        if (params.has(name)) {
            return params.getLong(name);
        } else {
            throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_PARAMETER_NOT_EXIST, name));
        }
    }

    public Long getOptionalLong(String name) {
        if (params.has(name)) {
            return params.getLong(name);
        } else {
            return 0l;
        }
    }

    public Long getOptionalLongOrNull(String name) {
        if (params.has(name)) {
            try {
                return params.getLong(name);
            } catch (Exception e) {
                throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_PARAMETER_INVALIT_TYPE, name, "long"));
            }
        } else {
            return null;
        }
    }

    public Integer getInteger(String name) throws InvalidParameterException {
        if (params.has(name)) {
            try {
                return params.getInt(name);
            } catch (Exception e) {
                throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_PARAMETER_INVALIT_TYPE, name, "integer"));
            }
        } else {
            throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_PARAMETER_NOT_EXIST, name));
        }
    }

    public boolean getBoolean(String name) throws InvalidParameterException {
        if (params.has(name)) {

            try {
                return params.getBoolean(name);
            } catch (Exception e) {
                throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_PARAMETER_INVALIT_TYPE, name, "boolean"));
            }
        } else {
            throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_PARAMETER_NOT_EXIST, name));
        }
    }

    public boolean getOptionalBoolean(String name) throws InvalidParameterException {
        if (params.has(name)) {
            return params.getBoolean(name);
        } else {
            return false;
        }
    }

    public Boolean getOptionalBooleanOrNull(String name) throws InvalidParameterException {
        if (params.has(name)) {
            return params.getBoolean(name);
        } else {
            return null;
        }
    }

    public JSONArray getOptionalJSONArray(String name) {
        JSONArray arr = new JSONArray("[]");
        if (params.has(name)) {
            arr = params.getJSONArray(name);
        }
        return arr;
    }

    public JSONArray getJSONArray(String name) throws InvalidParameterException {
        if (params.has(name)) {
            JSONArray arr = params.getJSONArray(name);
            return arr;
        } else {
            throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_PARAMETER_NOT_EXIST, name));
        }
    }

    public JSONObject getJSONObject(String name) throws InvalidParameterException {
        if (params.has(name)) {
            JSONObject arr = params.getJSONObject(name);
            return arr;
        } else {
            throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_PARAMETER_NOT_EXIST, name));
        }
    }

    public Integer getOptionalInteger(String name) {
        if (params.has(name)) {
            try {
                return params.getInt(name);
            } catch (Exception e) {
                throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_PARAMETER_INVALIT_TYPE, name, "integer"));
            }
        } else {
            return 0;
        }
    }

    public Integer getOptionalIntegerOrNull(String name) {
        if (params.has(name)) {
            return params.getInt(name);
        } else {
            return null;
        }
    }

    public Double getDouble(String name) throws InvalidParameterException {
        if (params.has(name)) {
            return params.getDouble(name);
        } else {
            throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_PARAMETER_NOT_EXIST, name));
        }
    }

    public Double getOptionalDouble(String name) {
        if (params.has(name)) {
            return params.getDouble(name);
        } else {
            return 0.0;
        }
    }


    public List<String> getOptionalCodeList(String name) {
        if (params.has(name)) {
            try {
                List<String> list = new ArrayList<>();
                JSONArray arr = null;
                arr = params.getJSONArray(name);
                for (int i = 0; i < arr.length(); i++) {
                    if (arr.getJSONObject(i).has("code")) {
                        String obj = (String) arr.getJSONObject(i).getString("code");
                        if (obj != null && !obj.isEmpty())
                            list.add(obj);
                    } else {
                        throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_PARAMETER_NOT_EXIST, "code"));
                    }
                }
                if (list.size() == 0) {
                    throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_PARAMETER_NOT_EXIST_OR_EMPTY, name));
                }
                return list;
            } catch (Exception e) {
                throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_PARAMETER_NOT_EXIST, "code"));

            }

        } else {
            return null;
        }
    }


    public List<String> getCodeList(String name) throws InvalidParameterException {
        if (params.has(name)) {
            List<String> list = new ArrayList<>();
            JSONArray arr = params.getJSONArray(name);
            for (int i = 0; i < arr.length(); i++) {
                if (arr.getJSONObject(i).has("code")) {
                    String obj = (String) arr.getJSONObject(i).getString("code");
                    if (obj != null && !obj.isEmpty())
                        list.add(obj);
                } else {
                    throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_PARAMETER_NOT_EXIST, "code"));
                }

            }
            if (list.size() == 0) {
                throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_PARAMETER_NOT_EXIST_OR_EMPTY, name));
            }
            return list;
        } else {
            throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_PARAMETER_NOT_EXIST, name));
        }
    }

    public List<String> getCodeListNotEmpty(String name) throws InvalidParameterException {
        if (params.has(name)) {

            List<String> list = new ArrayList<>();
            JSONArray arr = params.getJSONArray(name);

            for (int i = 0; i < arr.length(); i++) {
                String obj = arr.getJSONObject(i).getString("code");
                if (!obj.isEmpty())
                    list.add(obj);
            }

            if (list.size() == 0) {
                throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_PARAMETER_NOT_EXIST_OR_EMPTY, name));
            }

            return list;

        } else {
            throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_PARAMETER_NOT_EXIST, name));
        }
    }

    public List<String> getOptionalCodeListNotEmpty(String name) throws InvalidParameterException {
        if (params.has(name)) {

            List<String> list = new ArrayList<>();
            JSONArray arr = params.getJSONArray(name);
            if (arr.length() != 0) {
                for (int i = 0; i < arr.length(); i++) {
                    String obj = (String) arr.getJSONObject(i).getString("code");
                    list.add(obj);
                }
                return list;
            } else {
                throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_PARAMETER_NOT_EXIST_OR_EMPTY, name));
            }

        } else {
            return null;
        }
    }


    public List<String> getOptionalList(String name) {
        List<String> list;
        if (params.has(name)) {
            list = new ArrayList<>();
            JSONArray arr = params.getJSONArray(name);
            for (int i = 0; i < arr.length(); i++) {
                String obj = (String) arr.get(i);
                if (!obj.isEmpty())
                    list.add(obj);
            }
            return list;
        } else {
            return null;
        }

    }

    public List<String> getOptionalListNotEmpty(String name) {
        List<String> list;
        if (params.has(name)) {
            list = new ArrayList<>();
            JSONArray arr = params.getJSONArray(name);
            for (int i = 0; i < arr.length(); i++) {
                String obj = (String) arr.get(i);
                list.add(obj);
            }
        } else {
            return null;
        }
        return list;
    }

    public List<String> getListNotEmpty(String name) throws InvalidParameterException {
        if (params.has(name)) {
            List<String> list = new ArrayList<>();
            JSONArray arr = params.getJSONArray(name);

            for (int i = 0; i < arr.length(); i++) {
                String obj = (String) arr.get(i);
                if (!obj.isEmpty())
                    list.add(obj);
            }

            if (list.size() == 0) {
                throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_PARAMETER_NOT_EXIST_OR_EMPTY, name));
            }

            return list;

        } else {
            throw new InvalidParameterException(name);
        }
    }

    public List<String> getList(String name) throws InvalidParameterException {
        if (params.has(name)) {
            List<String> list = new ArrayList<>();
            JSONArray arr = params.getJSONArray(name);
            for (int i = 0; i < arr.length(); i++) {
                String obj = (String) arr.get(i);
                if (obj != null && !obj.isEmpty())
                    list.add(obj);
            }
            if (list.size() == 0) {
                throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_PARAMETER_NOT_EXIST_OR_EMPTY, name));
            }
            return list;
        } else {
            throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_PARAMETER_NOT_EXIST, name));
        }
    }

    public List<Integer> getOptionalIntList(String name) {
        List<Integer> list = new ArrayList<>();
        if (params.has(name)) {
            JSONArray arr = params.getJSONArray(name);
            for (int i = 0; i < arr.length(); i++) {
                Integer obj = (Integer) arr.get(i);
                list.add(obj);
            }
        } else {
            return null;
        }
        return list;
    }

    public List<Integer> getIntList(String name) throws InvalidParameterException {
        if (params.has(name)) {
            List<Integer> list = new ArrayList<>();
            JSONArray arr = params.getJSONArray(name);
            for (int i = 0; i < arr.length(); i++) {
                Integer obj = (Integer) arr.get(i);
                list.add(obj);
            }
            return list;
        } else {
            throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_PARAMETER_NOT_EXIST, name));
        }
    }

    public List<Long> getOptionalLongList(String name) {
        List<Long> list = new ArrayList<>();
        if (params.has(name)) {
            JSONArray arr = params.getJSONArray(name);
            for (int i = 0; i < arr.length(); i++) {
                Long obj = new Long((Integer) arr.get(i));
                list.add(obj);
            }
        } else {
            return null;
        }
        return list;
    }

    public List<Long> getLongList(String name) throws InvalidParameterException {
        if (params.has(name)) {
            List<Long> list = new ArrayList<>();
            JSONArray arr = params.getJSONArray(name);
            for (int i = 0; i < arr.length(); i++) {
                Long obj = new Long((Integer) arr.get(i));
                list.add(obj);
            }
            return list;
        } else {
            throw new InvalidParameterException(getMessage(Context.KEY_MESSAGE_ERROR_JSON_PARAMETER_NOT_EXIST, name));
        }
    }

    public Context(String command, JSONObject params, Long user_id, String source) {
        this.command = command;
        this.params = params;
        this.user_id = user_id;
        this.source = source;
    }

    public Context(String command, JSONObject params, Long user_id,
                   String source, Scope.Session session, Http.Request request) {
        this.command = command;
        this.params = params;
        this.user_id = user_id;
        this.source = source;
        this.session = session;
        this.request = request;
    }

    public Http.Request getRequest() {
        return request;
    }

    public Context setRequest(Http.Request request) {
        this.request = request;
        return this;
    }

    public Scope.Session getSession() {
        return session;
    }

    public Context setSession(Scope.Session session) {
        this.session = session;
        return this;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public JSONObject getParams() {
        return params;
    }

    public void setParams(JSONObject params) {
        this.params = params;
    }

    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String generateToken() {
        String token = "token-" + java.util.UUID.randomUUID().toString();
        int count = 0;
        while (RedisHelper.getInstance().get(token) != null) {
            token = token + "-" + count;
            count++;
        }
        return token;
    }


    @Override
    public String toString() {
        return "Context{" +
                "command='" + command + '\'' +
                ", user_id=" + user_id +
                ", source='" + source + '\'' +
                '}';
    }
}
