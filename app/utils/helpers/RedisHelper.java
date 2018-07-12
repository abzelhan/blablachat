package utils.helpers;

import com.google.gson.Gson;
import kz.api.json.Result;
import kz.dnd.slack.SlackWebhook;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bakhyt on 11/3/17.
 */
public class RedisHelper {
    //private static RedisHelper instance;
    Gson gson = new Gson();

    private static volatile RedisHelper instance;

    public static RedisHelper getInstance() {
        RedisHelper localInstance = instance;
        if (localInstance == null) {
            synchronized (RedisHelper.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new RedisHelper();
                }
            }
        }
        return localInstance;
        //return new RedisHelper();
    }

    String[] datasourceIps = {
            "127.0.0.1"
    };

    List<JedisPool> pools = new ArrayList<JedisPool>();

    //List<Jedis> datasources = new ArrayList<Jedis>();

    //Jedis jedis = new Jedis("88.198.48.246");

    public RedisHelper() {
        init(null);
    }

    public void init(Exception ex) {
        //JedisPool(config, hnp.getHost(), hnp.getPort(), 2000, "foobared");

        JedisPoolConfig jpc = new JedisPoolConfig();
        jpc.setMaxWaitMillis(500);
        jpc.setTestOnBorrow(true);
        jpc.setTestOnCreate(true);
        jpc.setTestOnReturn(true);
        jpc.setTestWhileIdle(true);

        for (String ip:datasourceIps) {
            pools.add(new JedisPool(jpc, ip, 6379, 1000, "redisMe##!"));//nls
        }
    }

    public void put(String key, String value) {
        for (JedisPool pool : pools) {
            try {
                Jedis jedis = null;
                try {
                    jedis = pool.getResource();
                    jedis.set(key, value);
                } finally {
                    if (jedis != null) {
                        jedis.close();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace(System.out);
                SlackWebhook.getLogsInstance().sendMessage("redis datasource down: " + ex.getMessage());
            }
        }
    }

    public String get(String key) {
        String result = null;
        for (JedisPool pool : pools) {
            try {
                Jedis jedis = null;
                try {
                    jedis = pool.getResource();
                    result = jedis.get(key);
                } finally {
                    if (jedis != null) {
                        jedis.close();
                    }
                }
                break;
            } catch (Exception ex) {
                ex.printStackTrace(System.out);
                SlackWebhook.getLogsInstance().sendMessage("redis datasource down: " + ex.getMessage());
                //SlackWebhook.getLogsInstance().sendMessage("redis datasource: " + datasource + " down: " + ex.getMessage());
            }
        }

        return result;
    }

    public void remove(String key) {
        for (JedisPool pool : pools) {
            try {
                Jedis jedis = null;
                try {
                    jedis = pool.getResource();
                    jedis.del(key);
                } finally {
                    if (jedis != null) {
                        jedis.close();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace(System.out);
                SlackWebhook.getLogsInstance().sendMessage("redis datasource down: " + ex.getMessage());
                //SlackWebhook.getLogsInstance().sendMessage("redis datasource: " + datasource + " down: " + ex.getMessage());
            }
        }
    }

    public void lpush(String key, String value) {
        for (JedisPool pool : pools) {
            try {
                Jedis jedis = null;
                try {
                    jedis = pool.getResource();
                    jedis.lpush(key, value);
                } finally {
                    if (jedis != null) {
                        jedis.close();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace(System.out);
                SlackWebhook.getLogsInstance().sendMessage("redis datasource down: " + ex.getMessage());
                //SlackWebhook.getLogsInstance().sendMessage("redis datasource: " + datasource + " down: " + ex.getMessage());
            }
        }
    }


    public void lpop(String key) {
        for (JedisPool pool : pools) {
            try {
                Jedis jedis = null;
                try {
                    jedis = pool.getResource();
                    jedis.lpop(key);
                } finally {
                    if (jedis != null) {
                        jedis.close();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace(System.out);
                SlackWebhook.getLogsInstance().sendMessage("redis datasource down: " + ex.getMessage());
                //SlackWebhook.getLogsInstance().sendMessage("redis datasource: " + datasource + " down: " + ex.getMessage());
            }
        }
    }

    public void lrem(String key, String value) {
        for (JedisPool pool : pools) {
            try {
                Jedis jedis = null;
                try {
                    jedis = pool.getResource();
                    jedis.lrem(key, 100, value);//max value
                } finally {
                    if (jedis != null) {
                        jedis.close();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace(System.out);
                SlackWebhook.getLogsInstance().sendMessage("redis datasource down: " + ex.getMessage());
                //SlackWebhook.getLogsInstance().sendMessage("redis datasource: " + datasource + " down: " + ex.getMessage());
            }
        }
    }


    public List<String> getList(String key, int start, int items) throws Exception {
        for (JedisPool pool : pools) {
            try {
                Jedis jedis = null;
                List<String> list = null;
                try {
                    jedis = pool.getResource();
                    list = jedis.lrange(key, start, items);
                } finally {
                    if (jedis != null) {
                        jedis.close();
                    }
                }
                return list;
            } catch (Exception ex) {
                ex.printStackTrace(System.out);

                //SlackWebhook.getLogsInstance().sendMessage("redis datasource: " + datasource + " down: " + ex.getMessage());
            }
        }

        return null;
    }

    public Result getResult(String key) throws Exception {
        long start = System.currentTimeMillis();
        String data = RedisHelper.getInstance().get(key);
        System.out.println("---- " + key + " got from the redis in " + (System.currentTimeMillis() - start) + "ms");
        if (data != null) {
            try {
                Result result2 = gson.fromJson(data, Result.class);
                if (result2 != null) {
                    System.out.println("---- " + key + " parsed  in " + (System.currentTimeMillis() - start) + "ms");

                    return result2;
                }
            } catch (Exception e) {
                e.printStackTrace(System.out);
                e.printStackTrace();
                throw e;
            }
        }

        return null;
    }

    public void putResult(String key, Result result) {
        put(key, gson.toJson(result));
    }
}
