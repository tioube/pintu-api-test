package commons;

import static io.restassured.config.EncoderConfig.encoderConfig;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.config.SSLConfig;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.navimatrix.commons.data.DataObject;
import org.navimatrix.commons.data.XmlCoder;

public class Util {

    public static Optional<String> getSystemProp(String key) {
        return Optional.ofNullable(System.getProperty(key));
    }

    public static void prepareConfig() {
        if (getSystemProp("config").isEmpty()) {
            getSystemProp("testEnv").map(String::toLowerCase).ifPresentOrElse(
                    testEnv -> {
                        String config;
                        switch (testEnv) {
                            case "dev":
                                config = getResourcePath("configs/dev.yaml");
                                break;
                            case "stg":
                                config = getResourcePath("configs/stg.yaml");
                                break;
                            default:
                                throw new IllegalArgumentException("wrong -DtestEnv value!");
                        }
                        Globals.setEnvironment(config);
                        DBUtil.setUpDatabaseAccess(config);
                    },
                    () -> {
                        throw new IllegalArgumentException("missing -Dconfig or -DtestEnv flag!");
                    }
            );
        } else {
            prepareConfig("config");
        }
        setTimeoutRestAssured(10000);
    }

    public static void prepareConfig(String propName) {
        String configYamlPath = System.getProperty(propName);
        Globals.setEnvironment(configYamlPath);
        DBUtil.setUpDatabaseAccess(configYamlPath);
    }

    public static void setTimeoutRestAssured(Integer timeout) {
        RestAssured.config = RestAssuredConfig.config()
                .sslConfig(SSLConfig.sslConfig().relaxedHTTPSValidation()).httpClient(
                        HttpClientConfig.httpClientConfig().setParam("http.connection.timeout", timeout)
                                .setParam("http.socket.timeout", timeout)
                                .setParam("http.connection-manager.timeout", timeout))
                .encoderConfig(encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false));
    }

    @SneakyThrows
    public static String encodeFileToBase64Binary(String fileName) {
        File file = new File(fileName);
        byte[] encoded = Base64.encodeBase64(FileUtils.readFileToByteArray(file));
        return new String(encoded, StandardCharsets.US_ASCII);
    }

    @SneakyThrows
    public static String encodeToBase64Binary(String str) {
        byte[] encoded = Base64.encodeBase64(str.getBytes());
        return new String(encoded, StandardCharsets.US_ASCII);
    }

    @SneakyThrows
    public static String decodeFromBase64Binary(String string) {
        byte[] decodedBytes = Base64.decodeBase64(string);
        return new String(decodedBytes);
    }

    /**
     * @return timestamp
     */
    public static String getEpoch() {
        return Long.toString(Instant.now().getEpochSecond());
    }

    public static String hashSHA512(String keyword) {
        byte[] bytesOfMessage;
        MessageDigest md;
        try {
            bytesOfMessage = keyword.getBytes(StandardCharsets.UTF_8);
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new PreconditionException(e.getMessage());
        }
        byte[] theDigest = md != null ? md.digest(bytesOfMessage) : new byte[0];
        BigInteger bigInt = new BigInteger(1, theDigest);
        StringBuilder hashtext = new StringBuilder(bigInt.toString(16));
        while (hashtext.length() < 128) {
            hashtext.insert(0, "0");
        }
        return hashtext.toString();
    }

    public static String hashSHA256(String keyword) {
        byte[] bytesOfMessage;
        MessageDigest md;
        try {
            bytesOfMessage = keyword.getBytes(StandardCharsets.UTF_8);
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new PreconditionException(e.getMessage());
        }
        byte[] theDigest = md != null ? md.digest(bytesOfMessage) : new byte[0];
        BigInteger bigInt = new BigInteger(1, theDigest);
        StringBuilder hashtext = new StringBuilder(bigInt.toString(16));
        while (hashtext.length() < 64) {
            hashtext.insert(0, "0");
        }
        return hashtext.toString();
    }

    @SneakyThrows
    public static String hashMd5(String input) {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(input.getBytes());
        BigInteger no = new BigInteger(1, messageDigest);
        StringBuilder hashtext = new StringBuilder(no.toString(16));
        hashtext.reverse();
        while (hashtext.length() < 32) {
            hashtext.append("0");
        }
        return hashtext.reverse().toString();
    }

    /**
     * @param csvFilePath csv file path
     * @param separator   separator
     * @param hasHeader   true / false
     * @return list of array of string for each line
     */
    @Step("Read CSV")
    public static List<String[]> readCSV(String csvFilePath, Character separator, Boolean hasHeader) {
        try (Reader reader = new FileReader(csvFilePath)) {
            CSVParser csvParser = new CSVParserBuilder().withSeparator(separator).build();
            CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(csvParser)
                    .withSkipLines(hasHeader ? 1 : 0).build();
            return csvReader.readAll();
        } catch (Exception e) {
            System.out.println("Invalid CSV");
            throw new PreconditionException("[CSV_ISSUE]" + e.getMessage());
        }
    }

    /**
     * @param csvFilePath csv file path
     * @param separator   separator
     * @param hasHeader   true / false
     * @return list of array of string for each line
     */
    @Step("Read CSV")
    public static List<String[]> readCSVWithQuote(String csvFilePath, Character separator,
                                                  Character quote, Boolean hasHeader) {
        try (Reader reader = new FileReader(csvFilePath)) {
            CSVParser csvParser = new CSVParserBuilder().withSeparator(separator).withQuoteChar(quote)
                    .build();
            CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(csvParser)
                    .withSkipLines(hasHeader ? 1 : 0).build();
            return csvReader.readAll();
        } catch (Exception e) {
            System.out.println("Invalid CSV");
            throw new PreconditionException("[CSV_ISSUE]" + e.getMessage());
        }
    }

    @Step("Write to CSV")
    public static void writeBeanToCSVCustom(String csvFilePath, List<BaseBean> beanList,
                                            Character separator) {
        try (Writer writer = new FileWriter(csvFilePath, Boolean.TRUE)) {
            CustomMappingStrategy<BaseBean> customMappingStrategy = new CustomMappingStrategy<>();
            customMappingStrategy.setType(beanList.get(0).getClass());
            StatefulBeanToCsv<BaseBean> btcsv = new StatefulBeanToCsvBuilder<BaseBean>(
                    writer).withSeparator(separator).withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .withMappingStrategy(customMappingStrategy).build();
            btcsv.write(beanList);
        } catch (Exception e) {
            e.printStackTrace();
            throw new PreconditionException("[CSV_ISSUE]" + e.getMessage());
        }
    }

    @SneakyThrows
    public static void writeCSV(String csvFilePath, List<String[]> data, Character separator,
                                boolean append) {
        Files.createDirectories(Path.of(csvFilePath).getParent());
        try (Writer writer = new FileWriter(csvFilePath, append)) {
            CSVParser csvParser = new CSVParserBuilder().withSeparator(separator).build();
            ICSVWriter icsvWriter = new CSVWriterBuilder(writer).withParser(csvParser).build();
            icsvWriter.writeAll(data);
        } catch (Exception e) {
            e.printStackTrace();
            throw new PreconditionException("[CSV_ISSUE]" + e.getMessage());
        }
    }

    public static String generateRandomAlphanumericString(int length) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();
        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97)).limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    /**
     * The random strategy is as follow - generate a sequence of random integer for a randomized
     * length of (digit/2 - random(0,2)) - and then append a timestamp string for the remaining length
     * needed
     * <p>
     * the A in the name is to signify there could be another random strategy added
     *
     * @param prefix the prefix of the  numeric string, the length doesn't count in the number of
     *               digit provided
     * @param digit  the number of digit that will be randomly generated after the prefix
     * @return the randomly generated numeric string
     */
    public static String generateRandomNumericStringA(String prefix, int digit) {
        StringBuilder numberBuilder = new StringBuilder(prefix);
        Random random = new Random();
        int leftPartLength = (digit / 2) - random.nextInt(2);
        int rightPartLength = digit - leftPartLength;
        String epoch = Long.toString(Instant.now().toEpochMilli());
        if (rightPartLength > epoch.length()) {
            var remainder = rightPartLength - epoch.length();
            leftPartLength += remainder;
            rightPartLength = epoch.length();
        }
        for (int i = 0; i < leftPartLength; i++) {
            numberBuilder.append(random.nextInt(10));
        }
        String partOfTimeStamp = epoch.substring(epoch.length() - rightPartLength);
        numberBuilder.append(partOfTimeStamp);
        return numberBuilder.toString();
    }

    /**
     * the random strategy is similar to strategy A but flipped the timestamp and the random integers
     *
     * @param prefix the prefix of the  numeric string, the length doesn't count in the number of
     *               digit provided
     * @param digit  the number of digit that will be randomly generated after the prefix
     * @return the randomly generated numeric string
     */
    // FIXME digit more than epoch length problem
    public static String generateRandomNumericStringB(String prefix, int digit) {
        StringBuilder numberBuilder = new StringBuilder(prefix);
        Random random = new Random();
        int randomLength = (digit / 2) - random.nextInt(2);
        String epoch = Long.toString(Instant.now().toEpochMilli());
        String partOfTimeStamp = epoch.substring(epoch.length() - randomLength);
        numberBuilder.append(partOfTimeStamp);
        for (int i = 0; i < (digit - randomLength); i++) {
            numberBuilder.append(random.nextInt(10));
        }
        return numberBuilder.toString();
    }

    @SneakyThrows
    public static void saveImageBase64(String imageBase64, String filePath) {
        byte[] imageByte = new Base64().decode(imageBase64.getBytes());
        @Cleanup ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
        BufferedImage image = ImageIO.read(bis);
        File file = new File(filePath);
        ImageIO.write(image, "png", file);
    }

    @Deprecated // as a reminder that we need to be more descriptive when checking this
    public static void makeSure(boolean whatShouldBeTrue) {
        doMakeSure(whatShouldBeTrue, "");
    }

    private static void doMakeSure(boolean whatShouldBeTrue, String message) {
        if (!whatShouldBeTrue) {
            throw new PreconditionException(message, 2);
        }
    }

    public static void makeSure(boolean whatShouldBeTrue, String message) {
        doMakeSure(whatShouldBeTrue, message);
    }

    @Deprecated // as a reminder that we need to be more descriptive when checking this
    public static void makeSureEqual(String actual, String expected) {
        doMakeSure(actual.equals(expected), "");
    }

    public static void makeSureEqual(String actual, String expected, String message) {
        doMakeSure(actual.equals(expected), message);
    }

    @Deprecated // as a reminder that we need to be more descriptive when checking this
    public static void makeSureEqual(int actual, int expected) {
        doMakeSure(actual == expected, "");
    }

    public static void makeSureEqual(int actual, int expected, String message) {
        doMakeSure(actual == expected, message);
    }

    @Deprecated
    public static void makeSureUnequal(String actual, String expectedFalse) {
        doMakeSure(!actual.equals(expectedFalse), "");
    }

    public static void makeSureUnequal(String actual, String expectedFalse, String message) {
        doMakeSure(!actual.equals(expectedFalse), message);
    }

    public static void makeSureUnequal(int actual, int expectedFalse, String message) {
        doMakeSure(actual != expectedFalse, message);
    }

    public static <Ret> Ret retryUntilSucceedOrNAttempts(Supplier<Ret> process, int numOfAttempt) {
        return retryUntilSucceedOrNAttempts(process, numOfAttempt, Duration.ZERO, "");
    }

    public static <Ret> Ret retryUntilSucceedOrNAttempts(Supplier<Ret> process, int numOfAttempt,
                                                         String desc) {
        return retryUntilSucceedOrNAttempts(process, numOfAttempt, Duration.ZERO, desc);
    }

    @SneakyThrows
    public static <Ret> Ret retryUntilSucceedOrNAttempts(Supplier<Ret> process, int numOfAttempt,
                                                         Duration duration, String desc) {
        RuntimeException lastException = null;
        Ret retVal = null;
        for (int i = 0; i < numOfAttempt; i++) {
            try {
                System.out.printf("[%s] Attempt %s out of %s%n", desc, i + 1, numOfAttempt);
                retVal = process.get();
                if (retVal != null) {
                    lastException = null;
                    break;
                }
            } catch (RuntimeException re) {
                System.err.println(re.getMessage());
                lastException = re;
                if (!duration.isZero()) {
                    System.err.printf("... waiting for %s minute until next attempt%n",
                            duration.toMinutes());
                    Thread.sleep(duration.toMillis());
                }
            }
        }
        if (lastException == null) {
            return retVal;
        } else {
            var lastExceptionMsg = String.format("[%s] last exception(%s) thrown by %s, message: %s",
                    desc, lastException.getClass().getName(), lastException.getStackTrace()[0].toString(),
                    lastException.getMessage());
            throw new PreconditionException(
                    String.format("[RETRY_ISSUE] + %s retry(s) attempted, %s", numOfAttempt,
                            lastExceptionMsg));
        }
    }

    @SneakyThrows
    public static String getURLQueryParamValue(String link, String paramName) {
        List<NameValuePair> queryParams = new URIBuilder(link).getQueryParams();
        return queryParams.stream().filter(param -> param.getName().equalsIgnoreCase(paramName))
                .map(NameValuePair::getValue).findFirst().orElse("");
    }

    @SneakyThrows
    public static String getXMLText(String xml, String xpath) {
        InputStream targetStream = new ByteArrayInputStream(xml.getBytes());
        DataObject data = XmlCoder.decode(new InputStreamReader(targetStream));
        return data.getString(xpath);
    }

//    public static String getEnv() {
//        if (Globals.getUrlCore().contains("api-dev")) {
//            return "dev";
//        } else if (Globals.getUrlCore().contains("api-stg")) {
//            return "stg";
//        } else {
//            return null;
//        }
//    }

    // get a file from the resources folder
    // works everywhere, IDEA, unit test and JAR file.
    @SneakyThrows
    public static String getResourcePath(String fileName) {

        // The class loader that loaded the class
        ClassLoader classLoader = Util.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);
        File file = File.createTempFile("tempConfig", ".yaml");
        OutputStream out = new FileOutputStream(file);
        int read;
        byte[] bytes = new byte[1024];

        while ((read = inputStream.read(bytes)) != -1) {
            out.write(bytes, 0, read);
        }
        out.close();
        file.deleteOnExit();

        return file.getPath();
    }

    @SneakyThrows
    public static String mapToJson(Map<String, Object> elements) {
        return new ObjectMapper().writeValueAsString(elements);
    }

    /**
     * Calculates the last digits for the card number received as parameter
     *
     * @param card {@link String} number
     * @return {@link String} the check digit
     */
    public static String calculateCheckDigit(String card) {
        if (card == null) {
            return null;
        }
        String digit;
        /* convert to array of int for simplicity */
        int[] digits = new int[card.length()];
        for (int i = 0; i < card.length(); i++) {
            digits[i] = Character.getNumericValue(card.charAt(i));
        }

        /* double every other starting from right - jumping from 2 in 2 */
        for (int i = digits.length - 1; i >= 0; i -= 2) {
            digits[i] += digits[i];

            /* taking the sum of digits grater than 10 - simple trick by substract 9 */
            if (digits[i] >= 10) {
                digits[i] = digits[i] - 9;
            }
        }
        int sum = 0;
        for (int j : digits) {
            sum += j;
        }
        /* multiply by 9 step */
        sum = sum * 9;

        /* convert to string to be easier to take the last digit */
        digit = sum + "";
        return digit.substring(digit.length() - 1);
    }

    @SneakyThrows
    public static Map<String, Object> stringJsonToMap(String jsonString) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(jsonString);
        return mapper.convertValue(jsonNode, new TypeReference<>() {
        });
    }

    public static String getKeyFromJWT(String token, String key) {
        String[] chunks = token.split("\\.");
        for (String chunk : chunks) {
            String body = new String(Base64.decodeBase64(chunk));
            System.out.println(body);
            Map<String, Object> map = Util.stringJsonToMap(body);
            if (map.containsKey(key)) {
                return map.get(key).toString();
            }
        }
        return null;
    }
}
