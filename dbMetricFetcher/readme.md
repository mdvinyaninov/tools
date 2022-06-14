# dbMetricFetcher

**dbMetricFetcher** - java application for fetching custom metrics from database using native query language in periodical manner and storing retrieved data in InfluxDB time series.

### Supported database platforms

+ Oracle
+ PosgreSQL

### Supported data storage

+ InfluxDB

## Settings

### application.properties

Fixed scheduled executor thread pool settings:
+ `fetch.pool.size.core`

Dynamic sender pool settings:
+ `async.pool.size.core`
+ `async.pool.size.max`

Example:
```properties
fetch.pool.size=2
async.pool.size.core=1
async.pool.size.max=2
```

### config.xml

XML Configuration file which allows to configure:
+ sql queries to be executed;
+ data source and output;

[XSD Schema](schema1.xsd)

XML Document Structure:

| Element                                          | Element Type | Data Type | Description                                                                                                                                                                                                                                                  |
|--------------------------------------------------|--------------|-----------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `configuration`                                  | Complex      | -         | Корневой элемент                                                                                                                                                                                                                                             |
| `/queries`                                       | Array        | -         | Cписок запросов                                                                                                                                                                                                                                              |
| `/queries/query`                                 | Complex      | -         | Запрос                                                                                                                                                                                                                                                       |
| `/queries/query/@interval`                       | Attribute    | Text      | Интервал опроса. Необязательное значение, если указан глобально для коллектора. Целое число, в миллисекундах.                                                                                                                                                |
| `/queries/query/name`                            | Simple       | Text      | Наименование запроса                                                                                                                                                                                                                                         |
| `/queries/query/text`                            | Simple       | Text      | Текст sql-запроса                                                                                                                                                                                                                                            |
| `/queries/query/columns`                         | Array        | -         | Перечисление столбцов вывода sql-запроса                                                                                                                                                                                                                     |
| `/queries/query/columns/column`                  | Complex      | -         | Столбец вывода sql-запроса. В значении элемента указывается наименование тэга\поля при записи измерения в InfluxDB.                                                                                                                                          |
| `/queries/query/columns/column/@type`            | Attribute    | Text      | Тип данных столбца. `TAG` - столбец соответствует данным тэга измерения InfluxDB, `FIELD` - поля измерения InfluxDB                                                                                                                                          |
| `/queries/query/columns/column/@dataType`        | Attribute    | Text      | Тип даннных значения. Возможные значения: `BOOLEAN`, `INTEGER` (int), `BIGINTEGER` (long), `BIGDECIMAL` (long), `FLOAT` (double), `TEXT`, `TEXT`, `TIMESTAMP`. Используется для преобразования типа данных БД к типу данных Java перед отправкой в InfluxDB. |
| `/collectors`                                    | Array        |           | Список коллекторов                                                                                                                                                                                                                                           |
| `/collectors/collector`                          | Complex      | Text      | Коллектор                                                                                                                                                                                                                                                    |
| `/collectors/collector/@name`                    | Attribute    |           | Наименование коллектора                                                                                                                                                                                                                                      |
| `/collectors/collector/@interval`                | Attribute    | Text      | Интервал опроса. Необязательное значение. Целое число, в миллисекундах.                                                                                                                                                                                      |
| `/collectors/collector/queries`                  | Array        | -         | Cписок указателей запросов                                                                                                                                                                                                                                   |
| `/collectors/collector/queries/queryRef`         | Simple       | Text      | Указатель на имя запроса. Должно соответствовать имени запрос из `/queries/query/name`.                                                                                                                                                                      |
| `/collectors/collector/postgresql`               | Complex      | -         | Параметры соединения с БД PostgreSQL                                                                                                                                                                                                                         |
| `/collectors/collector/postgresql/url`           | Simple       | Text      | JDBC url для соединения с БД PostgreSQL: `jdbc:postgresql://%host_name%:%port%/%database_name%`                                                                                                                                                              |
| `/collectors/collector/postgresql/username`      | Simple       | Text      | Имя пользователя PostgreSQL                                                                                                                                                                                                                                  |
| `/collectors/collector/postgresql/password`      | Simple       | Text      | Пароль                                                                                                                                                                                                                                                       |
| `/collectors/collector/oracle`                   | Complex      | -         | Параметры соединения с БД Oracle                                                                                                                                                                                                                             |
| `/collectors/collector/oracle/url`               | Simple       | Text      | JDBC url для соединения с БД PostgreSQL: `jdbc:postgresql://%host_name%:%port%/%database_name%`                                                                                                                                                              |
| `/collectors/collector/oracle/username`          | Simple       | Text      | Имя пользователя PostgreSQL                                                                                                                                                                                                                                  |
| `/collectors/collector/oracle/password`          | Simple       | Text      | Пароль                                                                                                                                                                                                                                                       |
| `/collectors/collector/influxdb`                 | Complex      | -         | Параметры соединения с PostgreSQL                                                                                                                                                                                                                            |
| `/collectors/collector/influxdb/url`             | Simple       | Text      | URL web-интерфейса InfluxDB: `http(s)://%host_name%:%port%`                                                                                                                                                                                                  |
| `/collectors/collector/influxdb/database`        | Simple       | Text      | База данных InfluxDB                                                                                                                                                                                                                                         |
| `/collectors/collector/influxdb/retentionPolicy` | Simple       | Text      | Retention Policy базы данных InfluxDB                                                                                                                                                                                                                        |
| `/collectors/collector/influxdb/username`        | Simple       | Text      | Имя пользователя InfluxDB. Необязательное значение, если указан в настройках запроса. Допускается пустое значение.                                                                                                                                           |
| `/collectors/collector/influxdb/password`        | Simple       | Text      | Пароль. Необязательное значение. Допускается пустое значение.                                                                                                                                                                                                |

Example:
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<configuration>
    
    <queries>
        
        <query interval="60000">
            <name>pg_stat_activity_by_wait_event</name>
            <text>SELECT datname, wait_event_type, wait_event, count(*) as cnt FROM pg_stat_activity WHERE wait_event IS NOT NULL GROUP BY datname, wait_event_type, wait_event;</text>
            <columns>
                <column type="TAG">datname</column>
                <column type="TAG">wait_event_type</column>
                <column type="TAG">wait_event</column>
                <column type="FIELD" dataType="BIGINTEGER">process_count</column>
            </columns>
        </query>
        
    </queries>

    <collectors>
        <collector name="main" interval="60000">
            <queries>
                <queryRef>pg_stat_activity_by_wait_event</queryRef>
            </queries>
            <postgresql>
                <url>jdbc:postgresql://localhost:5432/postgres</url>
                <username>monitor</username>
                <password>monitor</password>
            </postgresql>
            <influxdb>
                <url>http://localhost:8086</url>
                <database>postgremon</database>
                <retentionPolicy>30d</retentionPolicy>
                <username></username>
                <password></password>
            </influxdb>
        </collector>
    </collectors>
    
</configuration>
```

logback.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration scan="false" scanPeriod="300 seconds" debug="false">

    <property name="LOGS" value="${user.dir}\\logs"/>
    <property name="LOG_NAME" value="postgremon3"/>

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <Pattern>%d %p %C{1.} %M %line [%t] %m%n</Pattern>
        </encoder>
    </appender>

    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOGS}\${LOG_NAME}.log</file>
        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <Pattern>%d %p %C{1.} %M %line [%t] %m%n</Pattern>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${LOGS}/archived/${LOG_NAME}-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
        </rollingPolicy>
    </appender>

    <logger name="org.springframework" level="ERROR"/>
    <logger name="org.hibernate" level="ERROR"/>
    <logger name="ch.qos.logback" level="ERROR"/>
    <logger name="com.zaxxer.hikari" level="ERROR"/>

    <logger name="iteco.lt" level="INFO"/>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>

</configuration>
```

### Running application

Mandatory args:
+ `collector.configuration.file` - XML config file location.

Nonmandatory args:
+ `spring.config.location` - properties file location.
+ `logging.config` - Logback configuration file location.

Example:
```shell
java -jar -Dcollector.configuration.file=file:./config/collector.config.xml dbMetricFetcher-1.0.jar
```

```
┬─ config
│   ├─ application.properties
│   ├─ collector.config.xml
│   └─ logback.xml
└─ dbMetricFetcher-1.0.jar
```

Example:

```shell
java -jar -Dspring.config.location=file:./config/application.properties -Dcollector.configuration.file=file:./config/collector.config.xml -Dlogging.config=file:./config/logback.xml dbMetricFetcher-1.0.jar
```
