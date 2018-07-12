package utils.helpers;

import com.google.gson.Gson;
import com.mchange.lang.ThrowableUtils;
import kz.api.json.File.Image;
import kz.api.json.User.UserJson;
import models.User;
import net.sf.json.JSONObject;
import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by abzal  on 3/19/18.
 * ElasticSearch 6.2 High-Level Rest Client Helper
 * Implementation is Singleton. Only one instance of ESClientHelper and RestHighLevelClient
 */

public class ESClientHelper {
    private static volatile ESClientHelper instance;
    private RestHighLevelClient client;

    public static final String ES_CLIENT_HOST = "localhost";
    public static final int ES_CLIENT_PORT = 9200;
    public static final String ES_CLIENT_PROTOCOL = "http";

    public static ESClientHelper getInstance() {
        ESClientHelper localInstance = instance;
        if (localInstance == null) {
            synchronized (ESClientHelper.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new ESClientHelper();
                }
            }
        }
        return localInstance;
    }

    private ESClientHelper() {
        client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(ES_CLIENT_HOST, ES_CLIENT_PORT, ES_CLIENT_PROTOCOL)
                ));
    }

    public void close() throws IOException {
        this.client.close();
    }


    public SearchRequest createSearchRequest(String index,
                                             String type,
                                             SearchSourceBuilder sourceBuilder
    ) {
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.types(type);
        searchRequest.source(sourceBuilder);
        return searchRequest;
    }

    public ESResponseFuture<SearchResponse,List<UserJson>> searchAndGetUserJsonListAsync(SearchRequest searchRequest, ESResponseFuture<SearchResponse,List<UserJson>> responeFuture) throws Exception {
        responeFuture.setFunction( response -> {
            List<String> codesList = new ArrayList<>();
            response.getHits().iterator().forEachRemaining(documentFields ->
                    codesList.add(documentFields.getSourceAsString())
            );
            Gson gson = new Gson();
            return codesList.stream()
                    .map(userJsonString -> {
                        try {
                            JSONObject jsonObject = new JSONObject(userJsonString);
                            UserJson userJson = new UserJson();
                            userJson.setUsername(jsonObject.getString("username"));
                            userJson.setCode(jsonObject.getString("code"));
                            userJson.setGender(jsonObject.getString("gender"));
                            Image icon = gson.fromJson(jsonObject.getString("icon"), Image.class);
                            userJson.setIcon(icon);
                            return userJson;
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(u -> u != null)
                    .collect(Collectors.toList());
        } );

        client.searchAsync(searchRequest,responeFuture);
        return responeFuture;
    }

    public List<String> searchAndGetCodesList(SearchRequest searchRequest) {

        List<String> codesList = new ArrayList<>();
        try {
            SearchResponse response = client.search(searchRequest);
            response.getHits().iterator().forEachRemaining(documentFields ->
                    codesList.add(documentFields.getSourceAsString())
            );

        } catch (IOException e) {
            String s = ThrowableUtils.extractStackTrace(e);
            System.out.println(s);
        }
        return codesList;
    }

    //returns string
    public ESResponseFuture<IndexResponse,String> addUserAsync(User user, ESResponseFuture<IndexResponse,String> responseFuture){

        responseFuture.setFunction(i -> i.status().toString());
        client.indexAsync(new IndexRequest(
                User.ES_INDEX_STR,
                User.ES_TYPE_STR,
                user.getCode()).source(user.getESOBject()),responseFuture);
        return responseFuture;
    }

    public String addUser(User user) throws Exception {

        return client.index(new IndexRequest(
                User.ES_INDEX_STR,
                User.ES_TYPE_STR,
                user.getCode())
                .source(user.getESOBject())).status().toString();
    }

    public ESResponseFuture<UpdateResponse,String> updateUserAsync(User user, ESResponseFuture<UpdateResponse,String> responeFuture){
      responeFuture.<UpdateResponse>setFunction(t-> {
          UpdateResponse updateResponse = t;
          DocWriteResponse.Result result = updateResponse.getResult();
          if (result == DocWriteResponse.Result.UPDATED
                  ||
                  result == DocWriteResponse.Result.CREATED) {
              return "OK";
          }
          return "NO";
      });
        client.updateAsync(new UpdateRequest(
                User.ES_INDEX_STR,
                User.ES_TYPE_STR,
                user.getCode()
        )
                .doc(user.getESOBject()),responeFuture);
        return responeFuture;
    }

    public String updateUser(User user) throws Exception {
        DocWriteResponse.Result result = client.update(new UpdateRequest(
                User.ES_INDEX_STR,
                User.ES_TYPE_STR,
                user.getCode()
                )
                .doc(user.getESOBject())
        ).getResult();

        if (result == DocWriteResponse.Result.UPDATED
           ||
            result == DocWriteResponse.Result.CREATED) {
            return "OK";
        }
        return "NO";
    }


    public ESResponseFuture<DeleteResponse,String> deleteUserAsync(User user, ESResponseFuture<DeleteResponse,String> responeFuture){
        responeFuture.setFunction(deleteResponse -> {
            DocWriteResponse.Result result = deleteResponse.getResult();
            if(result == DocWriteResponse.Result.DELETED){
                return "OK";
            }
            return "NO";
        });
        client.deleteAsync(new DeleteRequest(
                User.ES_INDEX_STR,
                User.ES_TYPE_STR,
                user.getCode()
        ),responeFuture);
        return responeFuture;
    }

    public String deleteUser(User user) throws Exception {
        DocWriteResponse.Result result = client.delete(new DeleteRequest(
                User.ES_INDEX_STR,
                User.ES_TYPE_STR,
                user.getCode()
        )).getResult();
        if(result == DocWriteResponse.Result.DELETED){
            return "OK";
        }
        return "NO";
    }

    public String search() throws Exception {
        SearchRequest searchRequest = new SearchRequest("users");
        searchRequest.types("user");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        sourceBuilder.query(QueryBuilders.boolQuery().
                must(QueryBuilders.termsQuery("tags", "java"))
                .should(QueryBuilders.termQuery("isInterestedInMale", true))
        );
//        sourceBuilder.from(0);
//        sourceBuilder.size(5);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        sourceBuilder.fetchSource("code", "");
        searchRequest.source(sourceBuilder);
        final StringBuilder stringBuilder = new StringBuilder();

        SearchResponse search = client.search(searchRequest);
        search.getHits().iterator().forEachRemaining(documentFields -> {
            stringBuilder.append(documentFields.getSourceAsString());
        });
        return stringBuilder.toString();
    }


}
