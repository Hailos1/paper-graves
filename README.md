# PaperGraves

Плагин для Paper **26.1.2**: после смерти сохраняет инвентарь игрока в могиле-голове, которую может забрать только владелец. Время жизни могилы настраивается.

## Build

Требуется JDK 25.

```bat
cd C:\Users\HAILOS\paper-graves
gradlew.bat build
```

Готовый JAR: `build/libs/PaperGraves-1.0.0.jar`

## Commands

| Команда | Право | Описание |
|---------|-------|----------|
| `/graves help` | — | Помощь |
| `/graves list` | `graves.list` | Ваши активные могилы и оставшееся время |
| `/graves toggle` | `graves.admin` | Включить или отключить могилы |

## Config

`plugins/PaperGraves/config.yml` — `enabled`, `lifetime-seconds` (по умолчанию 3600), `nether-roof-y` (127), блок `messages` с русскими MiniMessage-строками.

## Tests

```bat
gradlew.bat test
```

Юнит-тесты покрывают правила размещения, восстановление слотов, время жизни и права на сбор могилы.
