# PaperGraves

Paper **26.1.2** plugin: on death, stores inventory in a player-head grave block with owner-only collection and configurable lifetime.

## Build

Requires JDK 25 (project links to `.jdk25` from `paper-nickname-whitelist` via junction).

```bat
cd C:\Users\HAILOS\paper-graves
gradlew.bat build
```

Output JAR: `build/libs/PaperGraves-1.0.0.jar`

## Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/graves help` | — | Help |
| `/graves list` | `graves.list` | Your active graves + remaining time |
| `/graves toggle` | `graves.admin` | Enable/disable graves globally |

## Config

`plugins/PaperGraves/config.yml` — `enabled`, `lifetime-seconds` (default 3600), `nether-roof-y` (127).

## Tests

```bat
gradlew.bat test
```

Pure unit tests cover placement rules, item slot planning, lifetime, and collection permissions.
