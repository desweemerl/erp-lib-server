package com.fw.server.json.response;

import java.util.ArrayList;
import java.util.List;

public class ListResponse<E> {
	
	private Integer page = 1;
	private Integer maxPages = 0;
	private Long count = 0L;
	private Integer size = 10;
	private String order = null;
	private Boolean orderAsc = true;
	private List<E> list = new ArrayList<E>();
	
	
	public Integer getPage() {
		return page;
	}
	
	public void setPage(Integer page) {
		this.page = page;
	}
	
	public Integer getMaxPages() {
		return maxPages;
	}
	
	public void setMaxPages(Integer maxPages) {
		this.maxPages = maxPages;
	}
	
	public Long getCount() {
		return count;
	}
	
	public void setCount(Long count) {
		this.count = count;
	}
	
	public Integer getSize() {
		return size;
	}
	
	public void setSize(Integer size) {
		this.size = size;
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
	
	public List<E> getList() {
		return list;
	}
	
	public void setList(List<E> list) {
		this.list = list;
	}
	
}
