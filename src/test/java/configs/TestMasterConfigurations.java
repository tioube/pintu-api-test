package configs;

import com.github.dzieciou.testing.curl.CurlRestAssuredConfigFactory;
import commons.GlobalFlags;
import commons.Util;
import configs.listeners.AllureTestMasterListener;
import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.config.SSLConfig;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import configs.listeners.TestMasterListener;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;

import static io.restassured.config.EncoderConfig.encoderConfig;

public abstract class TestMasterConfigurations implements IHookable {

    private static final List<TestMasterListener> listeners = new ArrayList<>(
            Arrays.asList(new AllureTestMasterListener()));

    @Step
    @BeforeSuite
    protected void setConfig(ITestContext context) {
        Util.prepareConfig();
        GlobalFlags.setDebug(Boolean.parseBoolean(System.getProperty("debug")));
        RestAssured.config = RestAssuredConfig.config()
                .sslConfig(SSLConfig.sslConfig().relaxedHTTPSValidation()).httpClient(
                        HttpClientConfig.httpClientConfig().setParam("http.connection.timeout", 60000)
                                .setParam("http.socket.timeout", 60000)
                                .setParam("http.connection-manager.timeout", 60000))
                .encoderConfig(encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false));
        RestAssured.filters(new AllureRestAssured());
//        GrpcGlobal.INSTANCE.getListenersSupplier()
//                .add(AllureRPCListener::new);
        if (GlobalFlags.isDebug()) {
            RestAssured.config = CurlRestAssuredConfigFactory.updateConfig(RestAssured.config);
            RestAssured.filters(new ResponseLoggingFilter());
            RestAssured.filters(new RequestLoggingFilter());
//            GrpcGlobal.INSTANCE.withDefaultLogToConsole();
        }
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType(ContentType.JSON)
                .build();
        var testMasterListenerServiceLoader = ServiceLoader.load(TestMasterListener.class);
        testMasterListenerServiceLoader.iterator().forEachRemaining(listeners::add);
        listeners.forEach(listener -> listener.onSetConfig(context));
    }

    public void run(final IHookCallBack icb, ITestResult testResult) {
        boolean intercepted = false;
        var listenersIterator = listeners.iterator();
        while (!intercepted && listenersIterator.hasNext()) {
            var listener = listenersIterator.next();
            intercepted = listener.onRunResolveInterception(testResult);
        }
        if (!intercepted) {
            icb.runTestMethod(testResult);
        }
    }

    @AfterSuite(alwaysRun = true)
    protected void afterSuite(ITestContext context) {
        listeners.forEach(listener -> listener.onAfterSuite(context));
    }

    protected String repo() {
        return Paths.get("build", "test-data") + File.separator + getClass().getCanonicalName()
                .replace(".", File.separator);
    }
}