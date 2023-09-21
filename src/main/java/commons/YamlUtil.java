package commons;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.yaml.snakeyaml.Yaml;

public class YamlUtil {

    private static final Pattern EXPRESSION_PATTERN = Pattern.compile(
            "#\\{([a-z0-9A-Z_.]+)\\s?(?:'([^']+)')?(?:,'([^']+)')*}");
    private final List<Map<String, Object>> valuesMaps;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public YamlUtil(InputStream stream) {
        final List<Map<String, Object>> all = new ArrayList();
        all.add(new Yaml().loadAs(stream, Map.class));
        this.valuesMaps = Collections.unmodifiableList(all);
    }

    /**
     * Safely fetches a key.
     *
     * @param key   the key to fetch from the YML structure.
     * @param index the index value to return if the fetched value is array
     */
    @SuppressWarnings("unchecked")
    public String fetchObject(String key, int index) {
        Object o = fetchObject(key);
        if (o == null) {
            return null;
        }
        if (o instanceof List) {
            List<String> values = (List<String>) o;
            if (values.size() == 0) {
                return null;
            }
            return values.get(index);
        } else if (isSlashDelimitedRegex(o.toString())) {
            return String.format("#{regexify '%s'}", trimRegexSlashes(o.toString()));
        } else {
            return (String) o;
        }
    }

    /**
     * Return the object selected by the key from yaml file.
     *
     * @param key key contains path to an object. Path segment is separated by dot. E.g. TC01.mobile
     */
    @SuppressWarnings("unchecked")
    public Object fetchObject(String key) {
        String[] path = key.split("\\.");
        Object result = null;
        for (Map<String, Object> valuesMap : valuesMaps) {
            Object currentValue = valuesMap;
            for (int p = 0; currentValue != null && p < path.length; p++) {
                currentValue = ((Map<String, Object>) currentValue).get(path[p]);
            }
            result = currentValue;
            if (result != null) {
                break;
            }
        }
        return result;
    }

    /**
     * @param expression input expression
     * @return true if s is non null and is a slash delimited regex (ex. {@code /[ab]/})
     */
    private boolean isSlashDelimitedRegex(String expression) {
        return expression != null && expression.startsWith("/") && expression.endsWith("/");
    }

    /**
     * Given a {@code slashDelimitedRegex} such as {@code /[ab]/}, removes the slashes and returns
     * only {@code [ab]}
     *
     * @param slashDelimitedRegex a non null slash delimited regex (ex. {@code /[ab]/})
     * @return the regex without the slashes (ex. {@code [ab]})
     */
    private String trimRegexSlashes(String slashDelimitedRegex) {
        return slashDelimitedRegex.substring(1, slashDelimitedRegex.length() - 1);
    }
}
