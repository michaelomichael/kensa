package dev.kensa.output;

import dev.kensa.Kensa;
import dev.kensa.context.TestContainer;

import java.util.List;
import java.util.function.BiConsumer;

import static dev.kensa.output.template.Template.Mode.MultiFile;
import static dev.kensa.output.template.Template.Mode.TestFile;
import static dev.kensa.output.template.Template.asIndex;
import static dev.kensa.output.template.Template.asJsonScript;

public class MultiFileWriter implements BiConsumer<List<TestContainer>, Kensa.Configuration> {
    @Override
    public void accept(List<TestContainer> containers, Kensa.Configuration configuration) {
        var indexTemplate = configuration.createTemplate("index.html", MultiFile);

        for (var container : containers) {
            var scriptTemplate = configuration.createTemplate(container.testClass().getName() + ".html", TestFile);
            scriptTemplate.addJsonScript(container, asJsonScript(configuration.renderers()));
            scriptTemplate.write();
            indexTemplate.addIndex(container, asIndex());
        }

        indexTemplate.write();
    }
}
