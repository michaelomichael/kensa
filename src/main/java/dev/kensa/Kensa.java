package dev.kensa;

import dev.kensa.output.OutputStyle;
import dev.kensa.output.template.Template;
import dev.kensa.render.Renderer;
import dev.kensa.render.Renderers;
import dev.kensa.render.diagram.directive.UmlDirective;
import dev.kensa.sentence.Acronym;
import dev.kensa.sentence.Dictionary;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class Kensa {

    private static final String KENSA_OUTPUT_ROOT = "kensa.output.root";
    private static final String KENSA_OUTPUT_DIR = "kensa-output";
    private static Kensa KENSA = new Kensa();

    public static Kensa configure() {
        return KENSA = new Kensa();
    }

    public static Supplier<Configuration> configuration() {
        return () -> KENSA.configuration;
    }

    private final Configuration configuration = new Configuration();

    private Kensa() {
    }

    public Kensa withIssueTrackerUrl(String url) {
        try {
            return withIssueTrackerUrl(new URL(url));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid Issue Tracker URL specified.", e);
        }
    }

    public Kensa withIssueTrackerUrl(URL url) {
        configuration.issueTrackerUrl = url;

        return this;
    }

    public Kensa withOutputDir(String dir) {
        return withOutputDir(Paths.get(dir));
    }

    public Kensa withOutputDir(Path dir) {
        if (!dir.isAbsolute()) {
            throw new IllegalArgumentException("OutputDir must be absolute.");
        }
        configuration.outputDir = dir.endsWith(KENSA_OUTPUT_DIR) ? dir : dir.resolve(KENSA_OUTPUT_DIR);

        return this;
    }

    public <T> Kensa withRenderer(Class<T> klass, Renderer<? extends T> renderer) {
        configuration.renderers.add(klass, renderer);

        return this;
    }

    public Kensa withOutputStyle(OutputStyle outputStyle) {
        configuration.outputStyle = outputStyle;

        return this;
    }

    public Kensa withAcronyms(Acronym... acronyms) {
        configuration.dictionary.putAcronyms(acronyms);
        return this;
    }

    public Kensa withKeywords(String... keywords) {
        configuration.dictionary.putKeywords(keywords);

        return this;
    }

    public class Configuration {
        private final Dictionary dictionary;
        private Path outputDir;
        private Renderers renderers;
        private List<UmlDirective> umlDirectives;
        private OutputStyle outputStyle;
        private URL issueTrackerUrl;

        private Configuration() {
            this.outputDir = Paths.get(System.getProperty(KENSA_OUTPUT_ROOT, System.getProperty("java.io.tmpdir")), KENSA_OUTPUT_DIR);
            this.renderers = new Renderers();
            this.umlDirectives = new ArrayList<>();
            this.outputStyle = OutputStyle.MultiFile;
            this.dictionary = new Dictionary();
        }

        public Dictionary dictionary() {
            return dictionary;
        }

        public Path outputDir() {
            return outputDir;
        }

        public Renderers renderers() {
            return renderers;
        }

        public List<UmlDirective> umlDirectives() {
            return umlDirectives;
        }

        public OutputStyle outputStyle() {
            return outputStyle;
        }

        public Template createTemplate(String path, Template.Mode mode) {
            return new Template(outputDir.resolve(path), mode, issueTrackerUrl);
        }
    }
}
