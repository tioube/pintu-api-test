package commons;

//import com.google.protobuf.GeneratedMessageV3;
import io.qameta.allure.Allure;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.SneakyThrows;

public class AllureUtil {

//    public static void report(GeneratedMessageV3 msg, String title) {
//        String execTime = LocalDateTime.now().toString();
//        Allure.addAttachment(title, "text/plain", execTime + "\n" + msg.toString());
//    }

    public static void report(String msg) {
        String execTime = LocalDateTime.now().toString();
        StackWalker walker = StackWalker.getInstance();
        Optional<String> invokingMethodName = walker.walk(
                frames -> frames.skip(1).findFirst().map(StackWalker.StackFrame::getMethodName));
        String title = invokingMethodName.orElse("(unknown method)");
        Allure.addAttachment(title, "text/plain", execTime + "\n" + msg);
    }

    public static void report(String title, String msg) {
        Allure.addAttachment(title, "text/plain", msg);
    }

    public static void attachCsv(String csvPath) {
        attachCsv("CSV File", csvPath);
    }

    @SneakyThrows
    public static void attachCsv(String title, String csvPath) {
        byte[] csvBytes = Files.readAllBytes(Path.of(csvPath));
        var attachingCSV = Allure.addByteAttachmentAsync(title, "text/csv", () -> csvBytes);
        attachingCSV.get();
    }

    public static void attachImage(byte[] screenShot) {
        attachImage("Image", screenShot);
    }

    @SneakyThrows
    public static void attachImage(String title, byte[] screenShot) {
        var attachingImage = Allure.addByteAttachmentAsync(title, "image/png", () -> screenShot);
        attachingImage.get();
    }
}
