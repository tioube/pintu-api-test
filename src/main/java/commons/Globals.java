package commons;

//import com.byorange.qa.mesineskrim.GrpcGlobal;
//import io.appium.java_client.MobileElement;
//import io.appium.java_client.android.AndroidDriver;
//import io.grpc.ManagedChannelBuilder;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.UnaryOperator;
import lombok.Getter;
import lombok.Setter;
//import org.openqa.selenium.WebDriver;

public class Globals {

    @Getter
    private static boolean headless;
    @Getter
    private static String urlCoreLoan;
    @Getter
    private static String jsonSchema;
    @Getter
    private static String redisHost;
    @Getter
    private static String redisPassword;
    @Getter
    private static Integer redisPort;
    @Getter
    private static String urlBrowserlessChrome;
    @Setter
    @Getter
    private static String jiraUsername;
    @Setter
    @Getter
    private static String jiraUrl;
    @Setter
    @Getter
    private static String jiraAPIToken;


    public static void setEnvironment(String configYaml) {
        try {
            YamlUtil yaml = new YamlUtil(new FileInputStream(configYaml));
            // file paths
            jsonSchema = String.valueOf(yaml.fetchObject("paths.jsonSchema"));
            //set selenium options
//            browser = String.valueOf(yaml.fetchObject("selenium.browser"));
            headless = Boolean.parseBoolean(String.valueOf(yaml.fetchObject("selenium.headless")));
            // redis
            redisHost = String.valueOf(yaml.fetchObject("redis.host"));
            redisPort = (Integer) yaml.fetchObject("redis.port");
            redisPassword = String.valueOf(yaml.fetchObject("redis.password"));
            // urls
            urlBrowserlessChrome = String.valueOf(yaml.fetchObject("urls.browserlessChrome"));
            urlCoreLoan = String.valueOf(yaml.fetchObject("urls.virgo"));
            //jira
            jiraUrl = String.valueOf(yaml.fetchObject("jira.url"));
            jiraUsername = String.valueOf(yaml.fetchObject("jira.username"));
            jiraAPIToken = String.valueOf(yaml.fetchObject("jira.token"));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new PreconditionException("[CONFIG_FILE_ISSUE]" + e.getMessage());
        }
    }



}
