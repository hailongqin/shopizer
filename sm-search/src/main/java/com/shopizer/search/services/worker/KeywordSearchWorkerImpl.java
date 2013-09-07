package com.shopizer.search.services.worker;

import java.util.Collection;

import com.shopizer.search.services.SearchRequest;
import com.shopizer.search.services.SearchResponse;
import com.shopizer.search.utils.SearchClient;
import com.shopizer.services.search.impl.SearchServiceImpl;

public class KeywordSearchWorkerImpl implements KeywordSearchWorker {

	public SearchResponse execute(SearchClient client, String collection,String json,int size, ExecutionContext context) throws Exception{

		
		
		SearchServiceImpl service = new SearchServiceImpl(client);
		Collection<String> hits = service.searchAutocomplete(collection, json, size);
		SearchResponse resp = new SearchResponse();

		String[] array = (String[])hits.toArray(new String[hits.size()]);
		
		
		resp.setInlineSearchList(array);
		
	    return resp; 

	}

}