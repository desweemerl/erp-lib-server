package com.erp.lib.server.json.response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ChartResponse {

    private class Limit {

        private String name = null;
        private BigDecimal value = null;

        public Limit(String name, BigDecimal value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public BigDecimal getValue() {
            return value;
        }
    }

    private class Serie {

        private String name = null;
        private List list = null;
        private boolean xTimestamp = false;

        public Serie(String name, List list) {
            this.name = name;
            this.list = list;
        }

        public Serie(String name, List list, boolean xTimestamp) {
            this.name = name;
            this.list = list;
            this.xTimestamp = xTimestamp;
        }

        public String getName() {
            return name;
        }

        public List getList() {
            return list;
        }

        public boolean isxTimestamp() {
            return xTimestamp;
        }

    }

    private List<Limit> limits = new ArrayList();
    private List<Serie> series = new ArrayList();

    public void addLimit(String name, BigDecimal value) {
        this.limits.add(new Limit(name, value));
    }

    public void addSerie(String name, List list, boolean xTimestamp) {
        this.series.add(new Serie(name, list, xTimestamp));
    }

    public List getLimits() {
        return limits;
    }

    public List getSeries() {
        return series;
    }
}
