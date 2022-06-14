# GrafanaExporter

Утилита для сохранения изображений графиков с панелей дашборда Grafana. 

Использование интерфейса командной строки:

    java -jar GrafanaExporter.jar [options]
      Options:
        --config
          XML Configuration file
        --endtime, -e
          Test end time
        --help
          Help
          Default: false
        --starttime, -s
          Test start time

Пример запуска:

    java -jar GrafanaExporter.jar --config F:\grafanaExportConfig.xml -s 2019-08-21T13:00:00 -e 2019-08-21T14:00:00

Пример конфигурации (config.xml)

    <Configuration>
        <Host>http://grafana</Host>
        <Dashboard>fintech-api</Dashboard>
	    <DashboardUID>SSVf6YRWk</DashboardUID>
        <ApiKey>...==</ApiKey>
        <Destination>\\sbt-st2ball01\share\</Destination>
        <Timezone>Europe/Moscow</Timezone>
        <Timeout>120</Timeout>
        <Panel>
            <Id>4</Id>
            <Name>VUsers</Name>
            <Width>1000</Width>
            <Height>500</Height>
            <Var>var-timeAggregation=1m</Var>
            <Var>var-wm=All</Var>
        </Panel>
        <Panel>
            <Id>6</Id>
            <Name>Scripts_pass</Name>
            <Width>1000</Width>
            <Height>500</Height>
            <Var>var-timeAggregation=1m</Var>
            <Var>var-wm=All</Var>
        </Panel>
        ...
        <Panel>
            <Id>52</Id>
            <Name>TA_B</Name>
            <Width>1000</Width>
            <Height>500</Height>
            <Var>var-timeAggregation=1m</Var>
            <Var>var-wm=All</Var>
        </Panel>
    </Configuration>

