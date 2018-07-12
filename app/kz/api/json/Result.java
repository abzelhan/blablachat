package kz.api.json;


import controllers.Parent;
import kz.api.json.File.Image;
import kz.api.json.Location.CityJson;
import kz.api.json.Location.CitySearchRowJson;
import kz.api.json.Room.MemberJson;
import kz.api.json.Room.RoomJson;
import kz.api.json.User.UserJson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by baha on 6/10/15.
 */
public class Result {
    String command;
    int status;
    String statusES;
    String message;
    List<ResultParameter> params;
    List<Object> list;
    List<String> tags;
    List<UserJson> usersSimpleList;
    Object object;
    Long totalCount;
    Long unreadCount;
    Integer currentPage;
    Integer perPage;
    UserJson user;
    Long balance;
    Long timeout;
    Long usedServiceCatalogs;
    Long partsUnread;
    Long servicesUnread;
    Long sosUnread;
    Long unread;
    Long totalUnread;
    List<RoomJson> publicRooms;
    List<RoomJson> rooms;
    Long postsCount;
    Long requestsCount;
    RoomJson room;
    Boolean promoCodeIsFree;
    MemberJson member;
    String token;
    List<UserJson> possibleInterviewers;
    String redirect;
    Boolean free;
    Boolean same;
    List<CityJson> countries;
    List<CityJson> regions;
    List<CityJson> cities;
    List<CitySearchRowJson> citiesRows;
    List<LanguageJson> languages;
    Image icon;
    List<String> codesList;
    InviteJson invite;

    public InviteJson getInvite() {
        return invite;
    }

    public Result setInvite(InviteJson invite) {
        this.invite = invite;
        return this;
    }

    public List<UserJson> getUsersSimpleList() {
        return usersSimpleList;
    }

    public Result setUsersSimpleList(List<UserJson> usersSimpleList) {
        this.usersSimpleList = usersSimpleList;
        return this;
    }

    public List<String> getCodesList() {
        return codesList;
    }

    public Result setCodesList(List<String> codesList) {
        this.codesList = codesList;
        return this;
    }

    public Boolean getSame() {
        return same;
    }

    public void setSame(Boolean same) {
        this.same = same;
    }

    public Image getIcon() {
        return icon;
    }

    public void setIcon(Image icon) {
        this.icon = icon;
    }

    public Boolean getFree() {
        return free;
    }

    public void setFree(Boolean free) {
        this.free = free;
    }

    public List<CityJson> getCountries() {
        return countries;
    }

    public void setCountries(List<CityJson> countries) {
        this.countries = countries;
    }

    public List<CityJson> getRegions() {
        return regions;
    }

    public void setRegions(List<CityJson> regions) {
        this.regions = regions;
    }

    public List<LanguageJson> getLanguages() {
        return languages;
    }

    public void setLanguages(List<LanguageJson> languages) {
        this.languages = languages;
    }

    public List<CitySearchRowJson> getCitiesRows() {
        return citiesRows;
    }

    public void setCitiesRows(List<CitySearchRowJson> citiesRows) {
        this.citiesRows = citiesRows;
    }

    public String getStatusES() {
        return statusES;
    }

    public Result setStatusES(String statusES) {
        this.statusES = statusES;
        return this;
    }

    public List<CityJson> getCities() {
        return cities;
    }

    public void setCities(List<CityJson> cities) {
        this.cities = cities;
    }

    public Long getPostsCount() {
        return postsCount;
    }

    public void setPostsCount(Long postsCount) {
        this.postsCount = postsCount;
    }

    public Long getRequestsCount() {
        return requestsCount;
    }

    public void setRequestsCount(Long requestsCount) {
        this.requestsCount = requestsCount;
    }

    public Long getTotalUnread() {
        return totalUnread;
    }

    public void setTotalUnread(Long totalUnread) {
        this.totalUnread = totalUnread;
    }

    public Long getUnread() {
        return unread;
    }

    public void setUnread(Long unread) {
        this.unread = unread;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public UserJson getUser() {
        return user;
    }

    public void setUser(UserJson user) {
        this.user = user;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }



    public Boolean getPromoCodeIsFree() {
        return promoCodeIsFree;
    }

    public void setPromoCodeIsFree(Boolean promoCodeIsFree) {
        this.promoCodeIsFree = promoCodeIsFree;
    }



    public Long getPartsUnread() {
        return partsUnread;
    }

    public void setPartsUnread(Long partsUnread) {
        this.partsUnread = partsUnread;
    }

    public Long getServicesUnread() {
        return servicesUnread;
    }

    public void setServicesUnread(Long servicesUnread) {
        this.servicesUnread = servicesUnread;
    }

    public Long getSosUnread() {
        return sosUnread;
    }

    public void setSosUnread(Long sosUnread) {
        this.sosUnread = sosUnread;
    }

    public Long getUsedServiceCatalogs() {
        return usedServiceCatalogs;
    }

    public void setUsedServiceCatalogs(Long usedServiceCatalogs) {
        this.usedServiceCatalogs = usedServiceCatalogs;
    }

    public RoomJson getRoom() {
        return room;
    }

    public void setRoom(RoomJson room) {
        this.room = room;
    }

    public Long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public List<UserJson> getPossibleInterviewers() {
        return possibleInterviewers;
    }

    public void setPossibleInterviewers(List<UserJson> possibleInterviewers) {
        this.possibleInterviewers = possibleInterviewers;
    }

    public void addParam(String name, Object value) {
        ResultParameter rp = new ResultParameter();
        rp.setName(name);
        rp.setValue(value);
        if (params == null) {
            params = new ArrayList<ResultParameter>();
        }
        params.add(rp);
    }



    public ResultParameter getParam(String name) {
        ResultParameter cpp = new ResultParameter();
        if (params != null) {
            for (ResultParameter cp : params) {
                if (cp.getName().equalsIgnoreCase(name)) {
                    cpp = cp;
                    break;
                }
            }
        }
        return cpp;
    }

    public List<ResultParameter> getAllParams(String name) {
        List<ResultParameter> cps = new ArrayList<ResultParameter>();
        if (params != null) {
            for (ResultParameter cp : params) {
                if (cp.getName().equalsIgnoreCase(name)) {
                    cps.add(cp);
                }
            }
        }
        return cps;
    }

    public void clearAllParams() {
        if (params != null) {
            params.clear();
        }
    }


    public List<RoomJson> getPublicRooms() {
        return publicRooms;
    }

    public void setPublicRooms(List<RoomJson> publicRooms) {
        this.publicRooms = publicRooms;
    }

    public List<RoomJson> getRooms() {
        return rooms;
    }

    public void setRooms(List<RoomJson> rooms) {
        this.rooms = rooms;
    }

    public void setStatus(int status) {
        this.status = status;
        if (status== Parent.SERVER_ERROR){
            this.setMessage("Произошла ошибка при отправке. Попробуйте чуть позднее.");
        }else if (status == Parent.NOT_ENOGHT_PARAMS){
            this.setMessage("Недостаточно данных для обработки");
        }
    }

    public List<ResultParameter> getParams() {
        return params;
    }

    public void setParams(List<ResultParameter> params) {
        this.params = params;
    }



    public static Result ok() {
        Result r = new Result();
        r.setStatus(200);
        return r;
    }

    public static Result ok(String message) {
        Result r = new Result();
        r.setStatus(200);
        r.setMessage(message);
        return r;
    }

    public static Result error(int code, String exMessage) {
        Result r = new Result();
        r.setStatus(code);
        r.setMessage(exMessage);
        return r;
    }

    public static Result error(int code, String message, String exMessage) {
        Result r = new Result();
        r.setStatus(code);
        r.setMessage(message);
        return r;
    }


    public void addObject(Object obj) {
        if (list == null) {
            list = new ArrayList<Object>();
        }
        if (!list.contains(obj)) {
            list.add(obj);
        }
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Object> getList() {
        return list;
    }

    public void setList(List<Object> list) {
        this.list = list;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public MemberJson getMember() {
        return member;
    }

    public void setMember(MemberJson member) {
        this.member = member;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


    public Integer getPerPage() {
        return perPage;
    }

    public void setPerPage(Integer perPage) {
        this.perPage = perPage;
    }

    @Override
    public String toString() {
        return "Result{" +
                "command='" + command + '\'' +
                ", status=" + status +
                ", params=" + params +
                '}';
    }
}
