package iteco.devops;

import java.net.URLEncoder;
import java.util.List;

public class GrafanaUrlBuilder {
    private static String host;
    private static String dashboard;
    private static String dashboardUID;
    private static final String CONTEXT = "/render/dashboard-solo/db";
    private static final String CONTEXT_UNQ = "/render/d-solo";
    private static String uri;
    private static long from;
    private static long to;
    private static long panelId;
    private static List<String> vars;
    private static long width = 500;
    private static long height = 300;
    private static String tz = "Europe Moscow";
    private static long timeout = 180;

    public GrafanaUrlBuilder host(String host) { this.host = host; return this; }
    public GrafanaUrlBuilder dashboard(String dashboard) { this.dashboard = dashboard; return this; }
    public GrafanaUrlBuilder dashboardUID(String dashboardUID) { this.dashboardUID = dashboardUID; return this; }
    public GrafanaUrlBuilder from(long from) { this.from = from; return this; }
    public GrafanaUrlBuilder to(long to) { this.to = to; return this; }
    public GrafanaUrlBuilder width(long width) { this.width = width; return this; }
    public GrafanaUrlBuilder height(long height) { this.height = height; return this; }
    public GrafanaUrlBuilder panelId(long panelId) { this.panelId = panelId; return this; }
    public GrafanaUrlBuilder vars(List<String> vars) { this.vars = vars; return this; }
    public GrafanaUrlBuilder tz(String tz) { this.tz = tz; return this; }
    public GrafanaUrlBuilder timeout(long timeout) { this.timeout = timeout; return this; }

    public String getURI() {return uri; }

    // http://grafana/render/d-solo/SSVf6YRWk/fintech-api-lt1-1-general
    // ?orgId=99
    // &refresh=10s
    // &from=1580205982123
    // &to=1580206282123
    // &var-timeAggregation=1m
    // &var-wm=All
    // &panelId=4
    // &width=1000
    // &height=500
    // &tz=Europe%2FMoscow
    public GrafanaUrlBuilder build() throws Exception {

        StringBuilder builder = new StringBuilder();
        builder.append(host);

        if (dashboardUID != null && !dashboardUID.isEmpty()) {
            builder.append(CONTEXT_UNQ + "/" + dashboardUID + "/" + dashboard);
        }
        else {
            builder.append(CONTEXT + "/" + dashboard);
        }
        builder.append("?panelId=" + panelId);
        builder.append("&from=" + from);
        builder.append("&to=" + to);
        if (vars != null) {
            for (String var : vars) {
                builder.append("&" + var);
            }
        }
        builder.append("&width=" + width);
        builder.append("&height=" + height);
        builder.append("&tz=" + URLEncoder.encode(tz, "UTF-8"));
        builder.append("&timeout=" + timeout);
        uri = builder.toString();
        return this;
    }

    @Override
    public String toString() {
        return uri;
    }
}
