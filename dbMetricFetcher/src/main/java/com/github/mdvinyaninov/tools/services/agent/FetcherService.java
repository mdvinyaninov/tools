package com.github.mdvinyaninov.tools.services.agent;

import com.github.mdvinyaninov.tools.config.QueryConfig;
import com.github.mdvinyaninov.tools.data.FetchResult;
import com.github.mdvinyaninov.tools.data.MeasurementData;
import com.github.mdvinyaninov.tools.config.Column;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.*;

@Service
@Transactional
public class FetcherService {
    private static Logger LOGGER = LoggerFactory.getLogger(FetcherService.class.getName());

    public List<MeasurementData> fetch(Collector collector, QueryConfig config) {
        LOGGER.debug("Fetching '{}' query... ", config.getName());
        final FetchResult result = execute(collector, config);
        if (result != null) {
            final int rows = result.getData().size();
            LOGGER.debug("Query '{}' has fetched {} rows in {}ms.", config.getName(), rows, result.getElapsedTime());
            final ZonedDateTime timestamp = ZonedDateTime.now();
            final String source = collector.getName();
            return process(source, config, timestamp, result.getData());
        }
        else return null;
    }

    private FetchResult execute(Collector collector, QueryConfig config) {
        FetchResult result = null;
        try {
            long start = System.currentTimeMillis();
            LOGGER.trace("Query sql: " + config.getText());

            EntityManager entityManager = collector.getEntityManager();

            Query query = collector.getPreparedQueries().get(config.getName());
            List<Object[]> rs = null;
            LOGGER.trace("Executing '{}' query...", config.getName());
            if (entityManager.isOpen()) {
                rs = query.getResultList();
            }
            final long elapsed = System.currentTimeMillis() - start;
            final long size = rs.size();
            LOGGER.trace("Query '{}' completed. Fetched {} rows. Execution took {} ms.", config.getName(), size, elapsed);
            result = new FetchResult(elapsed, rs);
        }
        catch (Exception e) {
            LOGGER.error("Error while fetching data from database", e);
        }
        finally {
        }
        return result;
    }

    private static MeasurementData processSingle(String source, QueryConfig config, ZonedDateTime timestamp, Object[] objects) {
        LOGGER.trace("RAW DATA: {}", Arrays.toString(objects));
        MeasurementData data = null;
        try {
            List<Column> columns = config.getColumns();
            Map<String, String> tags = new LinkedHashMap<>();
            Map<String, Object> fields = new LinkedHashMap<>();
            int i = 0;
            for (Object object : objects) {
                final Column column = columns.get(i);
                switch (column.getType()) {
                    case TAG:
                        if (object == null) {
                            tags.put(column.getName(), "null");
                        }else {
                            String tagValue = object.toString();
                            tags.put(
                                    column.getName(),
                                    tagValue == null || tagValue.isEmpty() ? "null)" : tagValue
                            );
                        }
                        break;
                    case FIELD:
                        if (object != null) {
                            switch (column.getDataType()) {
                                case BOOLEAN:
                                    final Boolean boolValue = Boolean.valueOf(object.toString());
                                    fields.put(column.getName(), boolValue);
                                    break;
                                case INTEGER:
                                    final Integer intValue = (Integer) object;
                                    fields.put(column.getName(), intValue);
                                    break;
                                case BIGINTEGER:
                                    final Long longValue = ((BigInteger) object).longValue();
                                    fields.put(column.getName(), longValue);
                                    break;
                                case BIGDECIMAL:
                                    final Long longValue2 = ((BigDecimal) object).longValue();
                                    fields.put(column.getName(), longValue2);
                                    break;
                                case FLOAT:
                                    final Double doubleValue = (Double) object;
                                    fields.put(column.getName(), doubleValue);
                                    break;
                                case TEXT:
                                    final String textValue = object.toString();
                                    fields.put(
                                            column.getName(),
                                            textValue == null || textValue.isEmpty() ? "null)" : textValue
                                    );
                                    break;
                                case TIMESTAMP:
                                    fields.put(column.getName(), timestamp);
                                    break;
                            }
                        }
                        break;
                }
                i++;
            }
            data = new MeasurementData();
            data.setMeasurement(config.getName());
            data.setSource(source);
            data.setTimestamp(timestamp);
            data.setTags(tags);
            data.setFields(fields);
            LOGGER.trace("MAPPED DATA: {}", data);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    private static List<MeasurementData> process(String source, QueryConfig config, ZonedDateTime timestamp, List<Object[]> result) {
        final Boolean send = config.getSend();
        if (send != null && !send) {
            LOGGER.trace("not processing");
            return null;
        }
        List<MeasurementData> list = null;
        try {
            list = new ArrayList<>();
            for (Object[] objects : result) {
                MeasurementData item = processSingle(source, config, timestamp, objects);
                list.add(item);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Error while mapping data", e);
        }
        return list;
    }
}
