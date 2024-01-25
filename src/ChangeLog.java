import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

/** Render the change log of this project to the standard output stream. */
class ChangeLog {
  public static void main(String... args) {
    System.out.print(render(log));
  }

  static Log log =
      new Log(
          "Changelog",
          """
          All notable changes to this project will be documented in this file.

          The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
          and this project adheres to [Calendar Versioning](https://calver.org/).
          """,
          new Release(
              "Unreleased",
              "_Nothing noteworthy, yet._",
              LocalDate.MAX,
              Tag.HEAD,
              Tag.NAME_2024_01_25),
          new Release(
              Tag.NAME_2024_01_25,
              "",
              LocalDate.of(2024, 1, 25),
              Tag.NAME_2024_01_25,
              "",
              new Entry(Type.Added, "Initial release", "1")));

  interface Tag {
    String HEAD = "HEAD";
    String NAME_2024_01_25 = "2024.01.25";
  }

  static String HOME = "https://github.com/sormuras/changelog";
  static BinaryOperator<String> COMPARE_LINKER = (HOME + "/compare/%s...%s")::formatted;
  static UnaryOperator<String> ISSUE_LINKER = (HOME + "/issues/%s")::formatted;
  static UnaryOperator<String> TAG_LINKER = (HOME + "/releases/tag/%s")::formatted;

  record Log(String title, String description, Release... releases) {}

  record Release(
      String title,
      String description,
      LocalDate date,
      String releaseTag,
      String previousTag,
      Entry... entries) {}

  record Entry(Type type, String text, String... issues) {}

  enum Type {
    Added, // for new features
    Changed, // for changes in existing functionality
    Deprecated, // for soon-to-be removed features
    Fixed, // for any bug fixes
    Removed, // for now removed features
    Security, // in case of vulnerabilities
  }

  static String render(Log log) {
    var lines = new ArrayList<String>();
    lines.add("# " + log.title());
    if (!log.description().isBlank()) {
      lines.add("");
      log.description().lines().forEach(lines::add);
    }
    for (var release : log.releases()) {
      lines.add("");
      if (release.date() == LocalDate.MAX) {
        lines.add("## [" + release.title() + "]");
      } else {
        lines.add("## [" + release.title() + "] - " + release.date());
      }
      if (!release.description().isBlank()) {
        lines.add("");
        release.description().lines().forEach(lines::add);
      }
      if (release.entries().length == 0) {
        if (release.description().isBlank()) {
          lines.add("");
          lines.add("_Nothing noteworthy, yet._");
        }
        continue;
      }
      for (var type : Type.values()) {
        var entries =
            Arrays.stream(release.entries()).filter(entry -> entry.type() == type).toList();
        if (entries.isEmpty()) continue;
        lines.add("");
        lines.add("### " + type);
        for (var entry : entries) {
          var links =
              Arrays.stream(entry.issues())
                  .map(issue -> "[%s](%s)".formatted(issue, ISSUE_LINKER.apply(issue)))
                  .toList();
          var issues = entry.issues().length == 0 ? "" : " " + String.join(" ", links);
          lines.add("- " + entry.text() + issues);
        }
      }
    }
    if (log.releases().length > 0) {
      lines.add("");
      for (var release : log.releases()) {
        var link =
            release.previousTag().isBlank()
                ? TAG_LINKER.apply(release.releaseTag())
                : COMPARE_LINKER.apply(release.previousTag(), release.releaseTag());
        lines.add("[" + release.title() + "]: " + link);
      }
    }
    var string = String.join("\n", lines);
    return string.endsWith("\n") ? string : string + "\n";
  }
}
