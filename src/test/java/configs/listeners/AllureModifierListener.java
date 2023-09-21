package configs.listeners;

import io.qameta.allure.listener.TestLifecycleListener;
import io.qameta.allure.model.Label;
import io.qameta.allure.model.TestResult;
import io.qameta.allure.util.ResultsUtils;
import java.util.List;

// this is used following this: https://stackoverflow.com/a/45693082
public class AllureModifierListener implements TestLifecycleListener {

    @Override
    public void beforeTestSchedule(TestResult result) {
        var epic = result.getLabels().stream().filter(label -> label.getName().equals("epic"))
                .findAny();
        if (epic.isEmpty()) {
            List<Label> labels = result.getLabels();
            labels.add(ResultsUtils.createEpicLabel("Uncategorized"));
            result.setLabels(labels);
        }
    }

    @Override
    public void afterTestStop(TestResult result) {
        var param = result.getParameters().stream().filter(p -> p.getName().equals("title"))
                .findFirst();
        if (param.isPresent()) {
            var name = result.getName();
            result.setName(String.format("[%s] %s", name, param.get().getValue()));
        }
    }
}
