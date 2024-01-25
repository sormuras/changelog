# changelog

## Installation

- Download a copy of the [ChangeLog.java](src/ChangeLog.java) file to your project.
- In your copy, edit operator constants to match your project's configuration.
- Remove also all existing change log entries to start with a blank log.

## Usage

- Add change log entries directly into your copy of the `ChangeLog.java` file.
- Run `java ChangeLog.java > CHANGELOG.md` to (re-)create or update the `CHANGELOG.md` file.

For example, the following command recreates the `CHANGELOG.md` file of this project.

```shell
java src/ChangeLog.java > CHANGELOG.md
```
