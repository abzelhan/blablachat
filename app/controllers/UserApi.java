package controllers;

import com.mchange.lang.ThrowableUtils;
import kz.api.json.Context;
import kz.api.json.Result;
import models.*;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateResponse;
import utils.helpers.ESClientHelper;
import utils.helpers.ESResponseFuture;
import utils.helpers.RedisHelper;
import utils.providers.ResultFactory;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static utils.providers.ResultFactory.getOkResult;

/**
 * Created by abzalsahitov@gmail.com  on 3/23/18.
 */
public class UserApi extends BaseApi {


    //returns total count of users with this name
    public static Result checkFreeUsername(Context context){
        String username = context.getString("username");
        Result result = getOkResult(context);
        long count = User.count("username=?", username);
        result.setFree(count == 0 ? true : false);
        return result;
    }

    public static Result checkOldPassword(Context context)  {
        User requiredUser = getRequiredUser(context);
        String password = context.getString("password");
        Result result = getOkResult(context);
        result.setSame(requiredUser.getPassword().equals(password));
        return result;
    }


    public static Result simpleRegister(Context context){

        String username = context.getString("username");//min length 4 max length 255


        if (!(username.length() >= User.USERNAME_MIN_LENGTH && username.length() <= User.USERNAME_MAX_LENGTH)) {
            return ResultFactory.getInvalidParameterValueRangeResult(context, "username", User.USERNAME_MIN_LENGTH, User.USERNAME_MAX_LENGTH);
        }

        //check for cyrrilic
        for (int i = 0; i < username.length(); i++) {
            if(Character.UnicodeBlock.of(username.charAt(i)).equals(Character.UnicodeBlock.CYRILLIC)) {
                Result customResult = ResultFactory.getCustomResult(context, 400);
                customResult.setMessage("Field username can't contain a CYRILLIC characters!");
                return customResult;
            }
        }


        User user = User.byUsername(username);

        if (user != null) {
            return ResultFactory.getAlreadyExistResult(context, "username", username);
        }

        String password = context.getString("password");//min length 4 max length 255

        if (!(password.length() >= User.PASSWORD_MIN_LENGTH && password.length() <= User.PASSWORD_MAX_LENGTH)) {
            return ResultFactory.getInvalidParameterValueRangeResult(context, "password", User.PASSWORD_MIN_LENGTH, User.PASSWORD_MAX_LENGTH);
        }

        for (int i = 0; i < password.length(); i++) {
            if(Character.UnicodeBlock.of(password.charAt(i)).equals(Character.UnicodeBlock.CYRILLIC)) {
                Result customResult = ResultFactory.getCustomResult(context, 400);
                customResult.setMessage("Field password can't contain a CYRILLIC characters!");
                return customResult;
            }
        }

        String email = context.getOptionalString("email");//min length 10 max length 255

        if (email != null) {
            Pattern pattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(email);
            if (!matcher.find()) {
                return ResultFactory.getInvalidParameterValueResult(context, "email", email);
            }
        }

        String imageCode = context.getOptionalString("imageCode");
        FileEntity avatarFile = FileEntity.createOptionalAvatar(imageCode);


        user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setCreationDate(Calendar.getInstance());
        user.setImage(avatarFile);
        user.setInterestedInFemale(true);
        user.setInterestedInMale(true);
        user.save();


        Result result = ResultFactory.getOkResult(context);

        result.setMessage(context.getMessage(Context.KEY_MESSAGE_SUCCESS_REGISTERED));
        String token = context.generateToken();

        RedisHelper.getInstance().put(token, "" + user.getId());


        //add to ElasticSearch

        String esResult;//check for null
        try {
            ESResponseFuture<IndexResponse,String> responeFuture = ESClientHelper
                    .getInstance()
                    .addUserAsync(user, new ESResponseFuture<>());
            esResult = responeFuture.getFetched();
        } catch (Exception e) {
            System.out.println(ThrowableUtils.extractStackTrace(e));
            esResult = ESResponseFuture.SERVER_ERROR;
        }

        result.setStatusES(esResult);
        result.setUser(user.getOptionalJson(true));
        result.setToken(token);
        return result;
    }

    public static Result register(Context context) {
        String username = context.getString("username");//min length 4 max length 255

        if (!(username.length() >= User.USERNAME_MIN_LENGTH && username.length() <= User.USERNAME_MAX_LENGTH)) {
            return ResultFactory.getInvalidParameterValueRangeResult(context, "username", User.USERNAME_MIN_LENGTH, User.USERNAME_MAX_LENGTH);
        }

        User user = User.byUsername(username);

        if (user != null) {
            return ResultFactory.getAlreadyExistResult(context, "username", username);
        }

        String password = context.getString("password");//min length 4 max length 255

        if (!(password.length() >= User.PASSWORD_MIN_LENGTH && password.length() <= User.PASSWORD_MAX_LENGTH)) {
            return ResultFactory.getInvalidParameterValueRangeResult(context, "password", User.PASSWORD_MIN_LENGTH, User.PASSWORD_MAX_LENGTH);
        }


        String gender = context.getString("gender");//male or female
        if (!User.isValidGender(gender)) {
            return ResultFactory.getInvalidParameterValueResult(context, "gender", gender);
        }


        String email = context.getOptionalString("email");//min length 10 max length 255

        if (email != null) {
            Pattern pattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(email);
            if (!matcher.find()) {
                return ResultFactory.getInvalidParameterValueResult(context, "email", email);
            }
        }


        String cityCode = context.getString("cityCode");
        City city = City.byCode(cityCode);

        List<String> langCodesList = context.getCodeList("languages");
        List<Language> languages = Language.byCodeList(langCodesList);


        String imageCode = context.getOptionalString("imageCode");
        FileEntity avatarFile = FileEntity.createOptionalAvatar(imageCode);


        List<String> tagsList = context.getList("tags");
        List<Tag> tags = Tag.getOrCreateByList(tagsList);


        Boolean isInterestedInMale = context.getBoolean("interestedInMale");
        Boolean isInterestedInFemale = context.getBoolean("interestedInFemale");
        Boolean searchable = context.getBoolean("searchable");

        Calendar birthdate = context.getCalendar("birthdate", User.BIRTH_DATE_FORMAT);

        Result result;


        result = getOkResult(context);

        user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setGender(gender);
        user.setLanguages(languages);
        user.setPassword(password);
        user.setInterestedInFemale(isInterestedInFemale);
        user.setInterestedInMale(isInterestedInMale);
        user.setCreationDate(Calendar.getInstance());
        user.setCity(city);
        user.setImage(avatarFile);
        user.setTags(tags);
        user.setBirthdate(birthdate);
        user.setSearchable(searchable);
        user.save();

        result.setMessage(context.getMessage(Context.KEY_MESSAGE_SUCCESS_REGISTERED));
        String token = context.generateToken();

        RedisHelper.getInstance().put(token, "" + user.getId());


        //add to ElasticSearch

        String esResult;
        try {
            ESResponseFuture<IndexResponse,String> responeFuture = ESClientHelper
                    .getInstance()
                    .addUserAsync(user, new ESResponseFuture<>());
            esResult = responeFuture.getFetched();
        } catch (Exception e) {
            e.printStackTrace();
            esResult = ESResponseFuture.SERVER_ERROR;

        }

        result.setStatusES(esResult);
        result.setUser(user.getJson());
        result.setToken(token);

        return result;
    }



    public static Result updateUserOptional(Context context){
        User currentUser = getRequiredUser(context);
        String password = context.getOptionalString("password");

        if (password != null) {
            if (!(password.length() >= User.PASSWORD_MIN_LENGTH && password.length() <= User.PASSWORD_MAX_LENGTH)) {
                return ResultFactory.getInvalidParameterValueRangeResult(context, "password", User.PASSWORD_MIN_LENGTH, User.PASSWORD_MAX_LENGTH);
            } else {
                for (int i = 0; i < password.length(); i++) {
                    if(Character.UnicodeBlock.of(password.charAt(i)).equals(Character.UnicodeBlock.CYRILLIC)) {
                        Result customResult = ResultFactory.getCustomResult(context, 400);
                        customResult.setMessage("Field password can't contain a CYRILLIC characters!");
                        return customResult;
                    }
                }
                currentUser.setPassword(password);
            }
        }

        String email = context.getOptionalString("email");
        if (email != null) {
            Pattern pattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(email);
            if (!matcher.find()) {
                return ResultFactory.getInvalidParameterValueResult(context, "email", email);
            } else {
                currentUser.setEmail(email);
            }
        }

        String imageCode = context.getOptionalString("imageCode");
        if (imageCode != null) {
            FileEntity fileEntity = FileEntity.byCodeRequired(imageCode);
            currentUser.setImage(fileEntity);

        }


        Boolean searchable = context.getOptionalBooleanOrNull("searchable");
        //here we need to check, if searchable true, we can accept other variables,
        // but if searchable is false, we need to set null to other fields
        if (searchable != null) {
            currentUser.setSearchable(searchable);
        }

        if(currentUser.isSearchable()){

            String gender = context.getOptionalString("gender");
            if (gender != null) {
                if (!User.isValidGender(gender)) {
                    return ResultFactory.getInvalidParameterValueResult(context, "gender", gender);
                } else {
                    currentUser.setGender(gender);
                }
            }

            String cityCode = context.getOptionalString("cityCode");
            if (cityCode != null) {
                City city = City.find("code=:code and deleted=0").setParameter("code", cityCode).first();
                currentUser.setCity(city);
            }

            List<String> optionalCodeList = context.getOptionalList("languages");
            if (optionalCodeList != null) {
                List<Language> languages = Language.byList(optionalCodeList);
                currentUser.setLanguages(languages);

            }

            Boolean isInterestedInMale = context.getOptionalBooleanOrNull("interestedInMale");
            if (isInterestedInMale != null) {
                currentUser.setInterestedInMale(isInterestedInMale);
            }
            Boolean isInterestedInFemale = context.getOptionalBooleanOrNull("interestedInFemale");
            if (isInterestedInFemale != null) {
                currentUser.setInterestedInFemale(isInterestedInFemale);
            }

            Calendar birthdate = context.getOptionalCalendar("birthdate", User.BIRTH_DATE_FORMAT);
            if (birthdate != null) {
                currentUser.setBirthdate(birthdate);
            }

            List<String> optionalTagList = context.getOptionalList("tags");
            if (optionalTagList != null) {
                List<Tag> tags = Tag.getEmptyOrCreateByList(optionalTagList);
                currentUser.setTags(tags);
            }

        }
        else{
            currentUser.setGender(null);
            currentUser.setBirthdate(null);
            currentUser.setCity(null);
            currentUser.setInterestedInMale(false);
            currentUser.setInterestedInFemale(false);
            currentUser.setLanguages(null);
            currentUser.setTags(null);
        }

        currentUser.save();
        String esResult;
        try {
            ESClientHelper instance = ESClientHelper.getInstance();
            ESResponseFuture<UpdateResponse, String> responseFuture = instance
                    .updateUserAsync(currentUser, new ESResponseFuture<>());
            esResult = responseFuture.getFetched();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(ThrowableUtils.extractStackTrace(e));
            esResult = ESResponseFuture.SERVER_ERROR;
        }

        Result result = getOkResult(context);
        result.setStatusES(esResult);
        result.setUser(currentUser.getOptionalJson(password!=null));
        return result;

    }

    public static Result updateUser(Context context) {
        User user = getRequiredUser(context);
        String password = context.getOptionalString("password");

        if (password != null) {
            if (!(password.length() >= User.PASSWORD_MIN_LENGTH && password.length() <= User.PASSWORD_MAX_LENGTH)) {
                return ResultFactory.getInvalidParameterValueRangeResult(context, "password", User.PASSWORD_MIN_LENGTH, User.PASSWORD_MAX_LENGTH);
            } else {
                user.setPassword(password);
            }
        }


        String gender = context.getOptionalString("gender");
        if (gender != null) {
            if (!User.isValidGender(gender)) {
                return ResultFactory.getInvalidParameterValueResult(context, "gender", gender);
            } else {
                user.setGender(gender);
            }
        }


        String cityCode = context.getOptionalString("cityCode");
        if (cityCode != null) {
            City city = City.byCode(cityCode);//только меняет, нельзя удалить
            user.setCity(city);
        }

        List<String> optionalCodeList = context.getOptionalCodeList("languages");
        if (optionalCodeList != null) {
            List<Language> languages = Language.byCodeList(optionalCodeList);
            user.setLanguages(languages);

        }

        String email = context.getOptionalString("email");
        if (email != null) {
            Pattern pattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(email);
            if (!matcher.find()) {
                return ResultFactory.getInvalidParameterValueResult(context, "email", email);
            } else {
                user.setEmail(email);
            }
        }

        String imageCode = context.getOptionalString("imageCode");
        if (imageCode != null) {
            FileEntity fileEntity = FileEntity.byCodeRequired(imageCode);
            user.setImage(fileEntity);

        }


        Boolean searchable = context.getOptionalBooleanOrNull("searchable");
        if (searchable != null) {
            user.setSearchable(searchable);
        }

        Boolean isInterestedInMale = context.getOptionalBooleanOrNull("interestedInMale");
        if (isInterestedInMale != null) {
            user.setInterestedInMale(isInterestedInMale);
        }
        Boolean isInterestedInFemale = context.getOptionalBooleanOrNull("interestedInFemale");
        if (isInterestedInFemale != null) {
            user.setInterestedInFemale(isInterestedInFemale);
        }

        Calendar birthdate = context.getOptionalCalendar("birthdate", User.BIRTH_DATE_FORMAT);
        if (birthdate != null) {
            user.setBirthdate(birthdate);
        }

        List<String> optionalTagList = context.getOptionalList("tags");
        if (optionalTagList != null) {
            List<Tag> tags = Tag.getEmptyOrCreateByList(optionalTagList);
            user.setTags(tags);
        }

        user.save();

        //Es client

        String esResult;
        try {
            ESClientHelper instance = ESClientHelper.getInstance();
            ESResponseFuture<UpdateResponse, String> responseFuture = instance
                    .updateUserAsync(user, new ESResponseFuture<>());
            esResult = responseFuture.getFetched();
        } catch (Exception e) {
            e.printStackTrace();
            esResult = ESResponseFuture.SERVER_ERROR;

        }



        Result result = getOkResult(context);
        result.setStatusES(esResult);
        result.setUser(user.getOptionalJson(true));
        return result;
    }

    public static Result deleteUser(Context context)  {
        User user = getRequiredUser(context);

        if (RedisHelper.getInstance().get(context.getToken()) != null) {
            RedisHelper.getInstance().remove(context.getToken());
        }

        List<Member> members = Member.find("user=:user and deleted=0").setParameter("user", user).fetch();
        for (Member m :
                members) {
           MemberApi.leaveFromRoom(user,m.getRoom().getCode());


//            //delete member from redis list
//            String roomKey = m.getRoom().getRedisCacheKey();
//            RedisHelper.getInstance().lrem(roomKey, m.getUser().getCode());
//            causeDefaultPersonalEventThenBroadcast(
//                    user.getCode(),
//                    m.getRoom().getCode(),
//                    ServiceEvent.CODE_USER_LEAVED_FROM_ROOM,
//                    String.format(ServiceEvent.MESSAGE_USER_LEAVED_FROM_ROOM, user.getUsername()
//                    ));
        }
        user.setDeleted(1);
        user.save();


        String esResult;
        try {
            ESClientHelper instance = ESClientHelper.getInstance();
            ESResponseFuture<DeleteResponse, String> responseFuture = instance
                    .deleteUserAsync(user, new ESResponseFuture<>());
            esResult = responseFuture.getFetched();
        } catch (Exception e) {
            e.printStackTrace();
            esResult = ESResponseFuture.SERVER_ERROR;

        }


        Result result = getOkResult(context);
        result.setStatusES(esResult);
        result.setMessage("Ваш аккаунт успешно удалён");

        return result;
    }






}
