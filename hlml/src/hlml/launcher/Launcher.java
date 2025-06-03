package hlml.launcher;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import hlml.Values;
import hlml.builder.Builder;
import hlml.checker.Checker;
import hlml.checker.Semantic;
import hlml.reporter.Subject;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
  name = "hlml",
  version = Values.VERSION + "." + Values.TIMESTAMP,
  description = "High Level Mindustry Logic Compiler",
  mixinStandardHelpOptions = true)
final class Launcher implements Callable<Integer> {
  /** Launches after parsing commands. */
  public static void main(String... arguments) {
    System.exit(new CommandLine(new Launcher()).execute(arguments));
  }

  @Option(
    names = "-o",
    description = "File the compiled instructions will be saved to.")
  private Optional<Path> output_path;

  @Option(names = "-I", description = "A directory to look for source files.")
  private List<Path> includes;

  @Parameters(description = "Name of the compiled source.")
  private String name;

  @Override
  public Integer call() {
    try {
      if (includes == null) { includes = new ArrayList<>(); }
      includes.add(0, Path.of("."));
      Subject subject = Subject.of("compiler");
      Semantic.Target target =
        Checker.check(subject, includes, name, Optional.empty());
      if (output_path.isPresent()) {
        Builder.build(subject, output_path.get(), target);
      }
      return 0;
    }
    catch (Throwable cause) {
      while (cause != null) {
        System.err.println(cause.getMessage());
        cause = cause.getCause();
      }
      return -1;
    }
  }
}
