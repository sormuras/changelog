/*
 * Copyright (c) 2024 Christian Stein
 * Licensed under the Universal Permissive License v 1.0 -> https://opensource.org/license/upl
 */

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/** Render the change log of this project to the standard output stream. */
class ChangeLog {
  public static void main(String... args) {
    System.out.print(render(log));
  }

  static Release unreleased =
      Release.of("Unreleased", "HEAD")
          .with(Type.Changed, "Improve model", "2")
          .with(Type.Added, "License under UPL-1.0");

  static Release release20240125 =
      Release.of("2024.01.25", "2024.01.25")
          .with(LocalDate.of(2024, 1, 25))
          .with(Type.Added, "Initial release", "1");

  static Log log =
      new Log(
          "Changelog",
          """
          All notable changes to this project will be documented in this file.

          The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
          and this project adheres to [Calendar Versioning](https://calver.org/).
          """,
          unreleased.withPreviousRelease(release20240125),
          release20240125);

  static String HOME = "https://github.com/sormuras/changelog";
  static BinaryOperator<String> COMPARE_LINKER = (HOME + "/compare/%s...%s")::formatted;
  static UnaryOperator<String> ISSUE_LINKER = (HOME + "/issues/%s")::formatted;
  static UnaryOperator<String> TAG_LINKER = (HOME + "/releases/tag/%s")::formatted;

  record Log(String title, String description, Release... releases) {}

  record Release(
      String title,
      Optional<String> description,
      LocalDate date,
      String tag,
      Optional<Release> previous,
      List<Entry> entries) {

    static Release of(String title, String tag) {
      return new Release(title, Optional.empty(), LocalDate.MAX, tag, Optional.empty(), List.of());
    }

    Release with(Type type, String text, String... issues) {
      var entry = new Entry(type, text, List.of(issues));
      var entries = Stream.concat(entries().stream(), Stream.of(entry)).toList();
      return new Release(title, description, date, tag, previous, entries);
    }

    Release with(LocalDate date) {
      return new Release(title, description, date, tag, previous, entries);
    }

    Release withPreviousRelease(Release previous) {
      return new Release(title, description, date, tag, Optional.ofNullable(previous), entries);
    }
  }

  record Entry(Type type, String text, List<String> issues) {}

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
      if (release.description().isPresent()) {
        lines.add("");
        release.description().get().lines().forEach(lines::add);
      }
      if (release.entries().isEmpty()) {
        if (release.description().isEmpty()) {
          lines.add("");
          lines.add("_Nothing noteworthy, yet._");
        }
        continue;
      }
      for (var type : Type.values()) {
        var entries = release.entries().stream().filter(entry -> entry.type() == type).toList();
        if (entries.isEmpty()) continue;
        lines.add("");
        lines.add("### " + type);
        for (var entry : entries) {
          var links =
              entry.issues().stream()
                  .map(issue -> "[#%s](%s)".formatted(issue, ISSUE_LINKER.apply(issue)))
                  .toList();
          var issues = entry.issues().isEmpty() ? "" : " " + String.join(" ", links);
          lines.add("- " + entry.text() + issues);
        }
      }
    }
    if (log.releases().length > 0) {
      lines.add("");
      for (var release : log.releases()) {
        var link =
            release.previous().isEmpty()
                ? TAG_LINKER.apply(release.tag())
                : COMPARE_LINKER.apply(release.previous().get().tag(), release.tag());
        lines.add("[" + release.title() + "]: " + link);
      }
    }
    var string = String.join("\n", lines);
    return string.endsWith("\n") ? string : string + "\n";
  }
}
