package controllers;

import kz.api.json.Context;
import kz.api.json.Result;
import models.User;
import utils.helpers.RedisHelper;
import utils.providers.ResultFactory;

/**
 * Created by abzalsahitov@gmail.com  on 3/23/18.
 */
public class AuthApi extends BaseApi {


    public static Result login(Context context) throws Exception {
        String username = context.getString("username");
        String password = context.getString("password");
        User user = User.find("username=:username and password=:password and deleted=0").
                setParameter("username", username).
                setParameter("password", password).
                first();
        if (user == null) {
            return ResultFactory.getAuthErrorResult(context);
        }
        Result result = ResultFactory.getOkResult(context);
        String token = generateToken();
        RedisHelper.getInstance().put(token, "" + user.getId());
        result.setUser(user.getOptionalJson(true));
        result.setToken(token);
        return result;
    }

    public static Result logout(Context context) throws Exception {
        Result r = ResultFactory.getOkResult(context);

        if (RedisHelper.getInstance().get(context.getToken()) != null) {
            RedisHelper.getInstance().remove(context.getToken());
            r.setMessage(context.getMessage(Context.KEY_MESSAGE_SUCCESS_LOGOUT));
        } else {
            r = ResultFactory.getNotFoundResult(context);
        }

        return r;

    }


}
