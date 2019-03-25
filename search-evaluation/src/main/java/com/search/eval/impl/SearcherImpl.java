package com.search.eval.impl;

import java.net.MalformedURLException;
import java.util.List;

import org.apache.log4j.Logger;

import com.caucho.hessian.client.HessianProxyFactory;
import com.search.eval.SearchDoc;
import com.search.eval.SearchQuery;
import com.search.eval.SearchResult;
import com.search.eval.Searcher;
import com.yihaodian.mandy.model.MandyException;
import com.yihaodian.mandy.model.Product;
import com.yihaodian.mandy.service.MandyRequest;
import com.yihaodian.mandy.service.MandyRespone;
import com.yihaodian.mandy.service.MandyService;
import com.yihaodian.mandy.service.ProductSearchRequest;

/**
 * use mandy api to search
 * 
 */
public class SearcherImpl implements Searcher {
	int abt = 1;
	int sortType = ProductSearchRequest.ST_RELATED;
	static Logger logger = Logger.getLogger(SearcherImpl.class);

	MandyService mandyService = null;

	/**
	 * abt & sortType is related to ranking.
	 * 
	 * @param serviceUrl
	 * @param abt
	 * @param sortType
	 */
	public SearcherImpl(String serviceUrl, int abt, int sortType) {
		HessianProxyFactory factory = new HessianProxyFactory();
		try {
			mandyService = (MandyService) factory.create(MandyService.class,
					serviceUrl);
		} catch (MalformedURLException e) {
			throw new MandyException("failed to generate new mandy instance.",
					e);
		}

		this.abt = abt;
		this.sortType = sortType;
	}

	public SearcherImpl(String serviceUrl) {
		this(serviceUrl, 1, ProductSearchRequest.ST_RELATED);
	}

	/**
	 * call mandy service
	 */
	@Override
	public SearchResult search(SearchQuery query) {
		MandyRequest req = toMandyRequest(query);
		MandyRespone response = mandyService.search(req);
		SearchResult result = toSearchResult(response);

		logger.debug(response.getLuceneCondition());
		return result;
	}

	/**
	 * minimal query.
	 * 
	 * @param query
	 * @return
	 */
	public MandyRequest toMandyRequest(SearchQuery query) {
		MandyRequest request = new MandyRequest();

		request.setPageOffset(0);
		request.setPageSize(query.getTopNumber());
		request.setKeyword(query.getQuery());
		request.setSortType(ProductSearchRequest.ST_RELATED);
		request.setRequestType(ProductSearchRequest.RQT_COMMON);
		request.setSearch_abt(abt);
		request.setVipMerchantId(1);

		if (query.getCategoryId() > 0)
			request.setCategoryId(query.getCategoryId());
		request.setProvinceId(query.getProvinceId());
		request.setMcsiteid(query.getMcSiteId());
		request.setSiteType(1);
		request.setSiteFlag(1);

		return request;
	}

	/**
	 * @param response
	 * @return
	 */
	public SearchResult toSearchResult(MandyRespone response) {
		SearchResult result = new SearchResult();
		result.setTotalHit(response.getProductCount());

		List<Product> products = response.getProducts();
		if (products != null) {
			SearchDoc[] topDocs = new SearchDoc[products.size()];
			result.setTopDocs(topDocs);

			for (int i = 0; i < products.size(); i++) {
				Product product = products.get(i);
				topDocs[i] = new ProductDoc(product);
			}
		}

		result.setLuceneCondition(response.getLuceneCondition());
		return result;
	}

}
