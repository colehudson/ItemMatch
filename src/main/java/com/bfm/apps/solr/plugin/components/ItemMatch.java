package com.bfm.apps.solr.plugin.components;

import java.io.IOException;
import java.lang.ArrayIndexOutOfBoundsException;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.io.File;
import java.util.logging.Level;
import java.util.List;
import java.util.ArrayList;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.ScoreDoc;

import org.apache.solr.common.SolrException;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocList;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemMatch extends SearchComponent {

	private static Logger LOGGER = LoggerFactory.getLogger(ItemMatch.class);
	volatile long numRequests;
	volatile long numErrors;
	volatile long totalRequestsTime;
	volatile String lastnewSearcher;
	volatile String lastOptimizeEvent;
	protected String defaultField;
	protected String defaultPath;
	protected Field mfield;
	
	@Override
	public void init(NamedList args) {
		super.init(args);
		defaultField = (String)args.get("field");
		if(defaultField == null) {
			throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "You need to set a default field for item Match to work");
		}
		defaultPath = (String)args.get("path");
		if(defaultPath == null) {
			throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "You need to set a default index path for item Match to work");
		}
	}
	
	
	@Override
	public void prepare(ResponseBuilder res) throws IOException {
		// empty
		
	}
	
	@Override
	public void process(ResponseBuilder res) throws IOException {
		numRequests++;

		long lstartTime = System.currentTimeMillis();
		SolrIndexSearcher searcher = res.req.getSearcher();
		
		NamedList<Object> response = new SimpleOrderedMap<Object>();
		String field = null;
		
		if (defaultField != null) {
			field = defaultField;
		}
		
		if (field == null) {
			LOGGER.error("Didn't define your default field. Item Match won't do anything.");
			return;
		}
			
		// Grab document list
		try {
			DocList docs = res.getResults().docList;

		if(docs == null || docs.size() == 0) {
			LOGGER.debug("No results");
		}
		LOGGER.debug("Working with the following amount of documents:\t" + docs.size());
		
		// Look through the documents and grab the value from the specified 'field' (i.e. value from dc_title)
		DocIterator iterator = docs.iterator();
			for(int i = 0; i < docs.size(); i++) {
				try {
					int currentDoc = iterator.nextDoc();
					// Set docresults variable for later
					List<String> docresults = new ArrayList<String>();
					
					// Load Document searcher
					Document doc = searcher.doc(currentDoc);
					// Now we need 2 field values out of this doc: the specified field from solrconfig (called field) and the id field value
					// Grab 'field' and id
					IndexableField[] docField = doc.getFields(field);
					IndexableField[] idField = doc.getFields("id");
					// Set id value to string variable
					String docidField = idField[0].stringValue();
					// Now try to set value for the field from solrconfig. Using try and catch, just in case not all documents have that field.
					try {
						String secondQueryContent = docField[0].stringValue();


						/////////// Now switch to your second query and query the Lucene Index //////////

						// Load Indexer and setup new index searcher
						String indexLocation = defaultPath;
						Directory index = FSDirectory.open(Paths.get(indexLocation));
						IndexReader reader = DirectoryReader.open(index);
						IndexSearcher lsearcher = new IndexSearcher(reader);
						Term t = new Term(field, secondQueryContent);

						// Construct query
						Query query = new TermQuery(t);
			
						// Run query
						TopDocs tops= lsearcher.search(query, 10);
						ScoreDoc[] matches = tops.scoreDocs;
						
						// Look through each document and get wayne:id; then make a named list
						for (int li=0; li<matches.length; li++) {
							int ldocId = matches[li].doc;
							Document d = lsearcher.doc(ldocId);
							// Get field value
							IndexableField[] ldocField = d.getFields("id");
							// Grab first value from first key/value pair; there should only be one key/value pair
							String lstringField = ldocField[0].stringValue();
							// Make sure we're not adding the doc as a match to itself
							if (lstringField.equals(docidField) == false) {
								try {
									docresults.add(lstringField);								
								} catch (ArrayIndexOutOfBoundsException exception) {
									// break
								}

							}
						}						
					} catch (ArrayIndexOutOfBoundsException exc) {
						// break
						
					}

					// final check here; make sure the results aren't empty.
					if (!docresults.isEmpty() && docresults != null) {
						 response.add(docidField, docresults);						
					}

				} catch(IOException ex) {
					java.util.logging.Logger.getLogger(ItemMatch.class.getName()).log(Level.SEVERE,null,ex);
				}
			}	
		res.rsp.add("matches", response);
		} catch (NullPointerException npe) {
			// empty
		}
		totalRequestsTime += System.currentTimeMillis() - lstartTime;
	}
	
	// Metadata
	
	@Override
	public String getDescription() {
		return "item Match via specified field";
	}

	@Override
	public String getSource() {
		return "https://github.com/colehudson/itemMatch";
	}
	
	@Override
	public String getVersion() {
		return "1";
	}
	
	@Override
	public NamedList<Object> getStatistics() {
		NamedList<Object> all = new SimpleOrderedMap<Object>();
		all.add("requests", "" + numRequests);
		all.add("errors", "" + numErrors);
		all.add("totalTime(ms)", "" + totalRequestsTime);
		return all;
	}
	
}