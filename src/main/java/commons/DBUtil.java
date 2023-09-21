package commons;

import com.github.javafaker.Faker;
import io.qameta.allure.Step;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;
import lombok.Cleanup;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;

public class DBUtil {

    public static final String DB_CONFIG_KEY = "db_config";
    public static final String GLOBAL_CONFIG_KEY = "global";
    public static final String CLUSTERS_KEY = "clusters";
    @Deprecated
    public static boolean accessAvailable = false;
    private static Map<String, DBConfig> listOfClusters;
    private static Integer numOfAttempt = 1;
    private static String lastInvokingMethod = "";

    public static YamlUtil yaml;

    @SuppressWarnings("unchecked")
    public static void setUpDatabaseAccess(String configYaml) {
        //retrieve all config
        try {
            yaml = new YamlUtil(new FileInputStream(configYaml));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new PreconditionException("[CONFIG_FILE_ISSUE]" + e.getMessage());
        }
        listOfClusters = new HashMap<>();
        Map<String, Object> allClusters = (Map<String, Object>) yaml.fetchObject(
                String.format("%s.%s", DB_CONFIG_KEY, CLUSTERS_KEY));
        for (String clusterName : allClusters.keySet()) {
            DBConfig clusterConfig = getCluster(clusterName);
            listOfClusters.put(clusterName, clusterConfig);
        }
    }

    @SuppressWarnings("unchecked")
    private static DBConfig getCluster(String clusterName) {
        String host = "", port = "", username = "", password = "";
        Integer timeoutInSeconds = 0;
        // get global config first
        Map<String, Object> globalConfig = (Map<String, Object>) yaml.fetchObject(
                String.format("%s.%s", DB_CONFIG_KEY, GLOBAL_CONFIG_KEY));
        host = (String) globalConfig.getOrDefault("host", host);
        port = (String) globalConfig.getOrDefault("port", port);
        username = (String) globalConfig.getOrDefault("username", username);
        password = (String) globalConfig.getOrDefault("password", password);
        timeoutInSeconds = (Integer) globalConfig.getOrDefault("timeout", timeoutInSeconds);
        numOfAttempt = (Integer) globalConfig.getOrDefault("num_of_attempt", timeoutInSeconds);
        // get specific cluster info and this will override global config
        Map<String, Object> clusterConfig = (Map<String, Object>) yaml.fetchObject(
                String.format("%s.%s.%s", DB_CONFIG_KEY, CLUSTERS_KEY, clusterName));
        host = Util.decodeFromBase64Binary((String) clusterConfig.getOrDefault("host", host));
        port = (String) clusterConfig.getOrDefault("port", port);
        username = Util.decodeFromBase64Binary(
                (String) clusterConfig.getOrDefault("username", username));
        password = Util.decodeFromBase64Binary(
                (String) clusterConfig.getOrDefault("password", password));
        timeoutInSeconds = (Integer) clusterConfig.getOrDefault("timeout", timeoutInSeconds);
        Properties props = new Properties();
        props.put("user", username);
        props.put("password", password);
        String timeoutStr = String.valueOf(timeoutInSeconds * 1000);
        props.put("timeoutInSecs", timeoutInSeconds);
        props.put("connectTimeout", timeoutStr);
        props.put("socketTimeout", timeoutStr);
        DBConfig dbConfig = new DBConfig();
        dbConfig.url = String.format("jdbc:mysql://%s:%s", host, port);
        dbConfig.props = props;
        dbConfig.clusterName = clusterName;
        return dbConfig;
    }

    @Step
    public static List<Map<String, Object>> query(String dbName, String sql) {
        saveLastCaller();
        return query(dbName, dbName, sql);
    }

    private static void saveLastCaller() {
        StackWalker walker = StackWalker.getInstance();
        Optional<String> invokingMethodName = walker.walk(
                frames -> frames.skip(3).findFirst().map(StackWalker.StackFrame::getMethodName));
        lastInvokingMethod = invokingMethodName.orElse("(unknown method)");
    }

    @Step
    public static List<Map<String, Object>> query(String clusterName, String dbName, String sql) {
        saveLastCaller();
        try {
            return queryCluster(clusterName, dbName, sql);
        } catch (PreconditionException p) {
            throw new PreconditionException(String.format("[DB_ISSUE]:%s", p.getMessage()));
        }
    }

    private static List<Map<String, Object>> queryCluster(String clusterName, String dbName,
                                                          String sql) {
        System.out.println("sql: " + sql);
        DBConfig config = listOfClusters.get(clusterName);
        return runWithAHealthyConfig(config, () -> {
            try {
                @Cleanup Connection connection = DriverManager.getConnection(config.urlTo(dbName),
                        config.props);
                return new QueryRunner().query(connection, sql, new MapListHandler());
            } catch (SQLException throwable) {
                throwable.printStackTrace();
                throw new RuntimeException(throwable);
            }
        });
    }

    private static <Ret> Ret runWithAHealthyConfig(DBConfig config, Supplier<Ret> process) {
        if (config.isHealthy) {
            try {
                return Util.retryUntilSucceedOrNAttempts(process, numOfAttempt);
            } catch (PreconditionException e) {
                config.isHealthy = false;
                config.lastIncident = String.format(
                        "method (%s) tried to connect to (%s) last time and failed after %s attempts",
                        lastInvokingMethod, config.clusterName, numOfAttempt);
                throw e;
            }
        } else {
            throw new PreconditionException(
                    String.format("%s is not healthy!\nlast incident:\n\t%s", config.clusterName,
                            config.lastIncident));
        }
    }

    @Step
    public static Integer update(String dbname, String sql) {
        saveLastCaller();
        return update(dbname, dbname, sql);
    }

    @Step
    public static Integer update(String clusterName, String dbName, String sql) {
        saveLastCaller();
        try {
            return updateCluster(clusterName, dbName, sql);
        } catch (PreconditionException p) {
            throw new PreconditionException(String.format("[DB_ISSUE]:%s", p.getMessage()));
        }
    }

    private static Integer updateCluster(String clusterName, String dbName, String sql) {
        System.out.println("sql: " + sql);
        DBConfig config = listOfClusters.get(clusterName);
        return runWithAHealthyConfig(config, () -> {
            try {
                @Cleanup Connection connection = DriverManager.getConnection(config.urlTo(dbName),
                        config.props);
                return connection.prepareStatement(sql).executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                throw new PreconditionException(e.getMessage());
            }
        });
    }

    private static class DBConfig {

        public String clusterName;
        public String url;
        public Properties props;
        public boolean isHealthy = true;
        public String lastIncident = "";

        public String urlTo(String dbName) {
            return String.format("%s/%s", url, dbName);
        }
    }
}
