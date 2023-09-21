package configs.listeners;

import commons.Globals;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.ITestContext;

public class AllureTestMasterListener implements TestMasterListener {

    List<String> testResultFlags = List.of("DB_ISSUE", "DUMMY_ISSUE", "VIRGOLAND_ISSUE",
            "depends on not");

    @Override
    public void onSetConfig(ITestContext context) {
        writeEnvironmentFile();
        writeCategoryFile();
    }

    private void writeEnvironmentFile() {
        String urlString = Globals.getUrlCoreLoan();
        String env;
        Matcher m = Pattern.compile(".*api-(.{3}).*").matcher(urlString);
        if (m.matches()) {
            env = m.group(1);
        } else {
            env = "[no config file!]";
        }
        try {
            String allureEnvPath = "allure-results/environment.properties";
            Files.createDirectories(Path.of(allureEnvPath).getParent());
            try (Writer writer = new FileWriter(allureEnvPath)) {
                writer.write(String.format("environment.config=%s", env));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void writeCategoryFile() {
        List<Category> categories = new ArrayList<>();
        categories.add(new Category("Database access issue", r(testResultFlags.get(0)), null));
        categories.add(new Category("DummyUser creation issue", r(testResultFlags.get(1)), null));
        categories.add(new Category("Virgoland login issue", r(testResultFlags.get(2)), null));
        categories.add(new Category("Test dependency skip", r(testResultFlags.get(3)), null));
        String excludePattern = generateExcludeRegexPattern(testResultFlags);
        categories.add(new Category("Uncategorized skipped tests", excludePattern, List.of("skipped")));
        categories.add(new Category("Uncategorized failed tests", excludePattern, List.of("failed")));
        var categoryJson = new JSONArray();
        categories.forEach(category -> {
            var categoryItem = new JSONObject();
            categoryItem.put("name", category.name);
            categoryItem.put("messageRegex", category.messageRegex);
            if (category.matchedStatuses != null) {
                categoryItem.put("matchedStatuses", category.matchedStatuses);
            }
            categoryJson.add(categoryItem);
        });
        try {
            String allureEnvPath = "allure-results/categories.json";
            Files.createDirectories(Path.of(allureEnvPath).getParent());
            try (Writer writer = new FileWriter(allureEnvPath)) {
                writer.write(categoryJson.toJSONString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String r(String s) {
        return String.format(".*%s.*", s);
    }

    private String generateExcludeRegexPattern(List<String> strToExclude) {
        String categoryStr = String.join("|", strToExclude);
        return String.format("^((?!%s).)*$", categoryStr);
    }

    @AllArgsConstructor
    public static class Category {

        String name;
        String messageRegex;
        List<String> matchedStatuses;
    }
}
