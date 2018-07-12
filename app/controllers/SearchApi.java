package controllers;

import kz.api.json.Context;
import kz.api.json.Location.CitySearchRowJson;
import kz.api.json.Result;
import kz.api.json.Room.RoomJson;
import kz.api.json.User.UserJson;
import models.City;
import models.Room;
import models.User;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import play.db.jpa.JPA;
import utils.helpers.ESClientHelper;
import utils.helpers.ESResponseFuture;
import utils.providers.ResultFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static utils.providers.ResultFactory.getOkResult;

/**
 * Created by abzalsahitov@gmail.com  on 3/25/18.
 */
public class SearchApi extends BaseApi {

    //returns the list of city that 50% same as the written name
    public static Result searchCityByName(Context context)  {
        String cityName = context.getString("name");
        List<CitySearchRowJson> cityJsons = new ArrayList<>();
        if (!cityName.isEmpty()) {
            List<City> cities = JPA.em().createQuery("select c from City c where lower(c.title) like concat(:name,'%')")
                    .setParameter("name", cityName)
                    .getResultList();
            for (City city :
                    cities) {
                cityJsons.add(city.getJsonSearchRow());
            }
        }
        Result result = getOkResult(context);
        result.setCitiesRows(cityJsons);
        result.setTotalCount(cityJsons.size());
        return result;
    }

    public static Result searchTagByValue(Context context)  {
        String value = context.getString("value");
        List<String> tags = null;
        if (!value.isEmpty()) {
            value = value.toLowerCase();
            tags = JPA.em().createQuery("select t.value from Tag t where lower(t.value) like concat(:value,'%')")
                    .setParameter("value", value)
                    .getResultList();
        } else {
            tags = new ArrayList<>();
        }

        Result result = getOkResult(context);
        result.setTags(tags);
        result.setTotalCount(tags.size());
        return result;
    }

    public static Result findUserByUsername(Context context){
        User currentUser = getRequiredUser(context);
        String username = context.getString("username");
        User searchedUser = User.find("username=:username and searchable=true").setParameter("username", username).first();
        if(searchedUser==null){
            Result notFoundResult = ResultFactory.getNotFoundResult(context);
            notFoundResult.setMessage("user with this username not founded, or is not viewed for internet");
            return notFoundResult;
        }
        Result result = ResultFactory.getOkResult(context);
        result.setUser(searchedUser.getJson());
        return result;
    }

    //Выдаёт только публичные группы
    public static Result findRoomsByName(Context context)  {
        User currentUser = getRequiredUser(context);
        String roomName = context.getString("name");
        List<Room> publicRooms;
        if (!roomName.isEmpty()) {
            publicRooms = JPA.em().createQuery("select r from Room r where r.roomType=0 and r.deleted=0 and lower(r.title) like concat(:name,'%')")
                    .setParameter("name", roomName)
                    .getResultList();
        } else {
            publicRooms = new ArrayList<>();
        }
        Result result = getOkResult(context);
        List<RoomJson> rooms = new ArrayList<>();
        for (Room room : publicRooms) {
            rooms.add(room.getOptionalJson(false));
        }
        result.setPublicRooms(rooms);

        return result;
    }


    //Elastic search Zone
    public static Result searchUserByCriteria(Context context) throws InterruptedException {
        User currentUser = getRequiredUser(context);
        boolean hasSearchRows = false;
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.mustNot(QueryBuilders.termsQuery("code",currentUser.getCode()));//check to prevent find user that make this search
        String gender = context.getOptionalString("gender");
        if (gender != null) {
            boolQueryBuilder.must(QueryBuilders.termQuery("gender", gender));//by value
            hasSearchRows = true;
        }
        String cityCode = context.getOptionalString("cityCode");
        if (cityCode != null) {
            boolQueryBuilder.must(QueryBuilders.termQuery("city", cityCode));//by code
            hasSearchRows = true;
        }
        List<String> languagesCodesList = context.getOptionalCodeList("languages");
        if (languagesCodesList != null) {
            boolQueryBuilder.must(QueryBuilders.termsQuery("languages", languagesCodesList));//by codes
            hasSearchRows = true;
        }
        List<String> tagsList = context.getOptionalList("tags");
        if (tagsList != null) {
            boolQueryBuilder.must(QueryBuilders.termsQuery("tags", tagsList));//by values
            hasSearchRows = true;
        }

        if (!hasSearchRows) {
            Result customResult = ResultFactory.getCustomResult(context, 400);
            customResult.setMessage("You need to provide at least one criteria for search");
            return customResult;
        }

        //get users that set searchable to true
        boolQueryBuilder.must(QueryBuilders.termsQuery("searchable", "true"));


        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.from(0);
        sourceBuilder.size(100);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        sourceBuilder.fetchSource(new String[]{"code", "username", "icon", "gender"}, new String[]{});

        sourceBuilder.query(boolQueryBuilder);
        List<UserJson> usersSimpleListJson;

        try {
            ESClientHelper instance = ESClientHelper.getInstance();
            ESResponseFuture<SearchResponse, List<UserJson>> responseFuture = instance
                    .searchAndGetUserJsonListAsync(
                            instance.createSearchRequest(User.ES_INDEX_STR, User.ES_TYPE_STR,
                                    sourceBuilder),
                            new ESResponseFuture<>());
            usersSimpleListJson = responseFuture.getFetched();
        } catch (Exception e) {
            e.printStackTrace();
            usersSimpleListJson = new ArrayList<>();
        }

        Result result = getOkResult(context);
        result.setTotalCount(usersSimpleListJson.size());
        result.setUsersSimpleList(usersSimpleListJson);
        return result;
    }


}
