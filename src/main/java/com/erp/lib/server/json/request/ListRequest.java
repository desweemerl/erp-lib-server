package com.erp.lib.server.json.request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.codehaus.jackson.annotate.JsonIgnore;

public class ListRequest {

    private Map<String, Object> params;
    private Integer page = 1;
    private Integer size = 10;
    private String order = null;
    private List<String> orders = new ArrayList();
    private Boolean orderAsc = true;
    private List<Boolean> ordersAsc = new ArrayList();

    public Map<String, Object> getParams() {
        return params;
    }

    @JsonIgnore
    public Object getParam(String parameter) {
        return params.get(parameter);
    }

    @JsonIgnore
    public void setParamValue(String parameter, Object value) {
        params.put(parameter, value);
    }

    @JsonIgnore
    public boolean paramExists(String parameter) {
        return params != null
                ? params.containsKey(parameter)
                : false;
    }

    @JsonIgnore
    public boolean paramIsNotNull(String parameter) {
        return params != null
                ? params.get(parameter) != null
                : false;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        if (page > 0) {
            this.page = page;
        }
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        if (size <= 100) {
            this.size = size;
        }
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public Boolean getOrderAsc() {
        return orderAsc;
    }

    public void setOrderAsc(Boolean orderAsc) {
        this.orderAsc = orderAsc;
    }

    public List<String> getOrders() {
        return orders;
    }

    public void setOrders(List<String> orders) {
        this.orders = orders;
    }

    @JsonIgnore
    public void setOrders(String[] orders) {
        this.orders = Arrays.asList(orders);
    }

    public List<Boolean> getOrdersAsc() {
        return ordersAsc;
    }

    public void setOrdersAsc(List<Boolean> ordersAsc) {
        this.ordersAsc = ordersAsc;
    }

    @JsonIgnore
    public void setOrdersAsc(Boolean[] ordersAsc) {
        this.ordersAsc = Arrays.asList(ordersAsc);
    }
}
