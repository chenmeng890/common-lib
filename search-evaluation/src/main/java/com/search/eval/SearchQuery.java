package com.search.eval;

import java.util.Comparator;

/**
 * a simple version of {@link com.yihaodian.mandy.service.MandyRequest}
 * 
 */
public class SearchQuery implements Comparable<SearchQuery>{
	int id = 0;

	/**
	 * query keywords
	 */
	String query;

	/**
	 */
	long provinceId = 1;

	/**
	 */
	long mcSiteId = 1;

	/**
	 * the number of top docs should return. Default is the first page.
	 */
	int topNumber = EvalConfig.getEvalTop();

	/**
	 * 0 means all
	 */
	long categoryId = 0;

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public long getProvinceId() {
		return provinceId;
	}

	public void setProvinceId(long provinceId) {
		this.provinceId = provinceId;
	}

	public long getMcSiteId() {
		return mcSiteId;
	}

	public void setMcSiteId(long mcSiteId) {
		this.mcSiteId = mcSiteId;
	}

	public int getTopNumber() {
		return topNumber;
	}

	public void setTopNumber(int topNumber) {
		this.topNumber = topNumber;
	}

	public long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(long categoryId) {
		this.categoryId = categoryId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SearchQuery other = (SearchQuery) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SearchQuery [query=" + query + ", provinceId=" + provinceId
				+ ", mcSiteId=" + mcSiteId + ", topNumber=" + topNumber
				+ ", categoryId=" + categoryId + "]";
	}

	public static Comparator<SearchQuery> weightComarator = new Comparator<SearchQuery>() {
		@Override
		public int compare(SearchQuery o1, SearchQuery o2) {
			return o1.id - o2.id;
		}

	};

	@Override
	public int compareTo(SearchQuery o) {
		return id - o.id;
	}
}
