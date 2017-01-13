package de.bxservice.utilities;

public interface TextSearchValues {

	static final String   TS_TABLE_NAME = "BXS_omnSearch";
	static final String   TS_INDEX_NAME = "BXS_IsTSIndex";
	static final String   INDEX_COLUMN = "BXS_omnTSVector";
	static final String   INDEX_NAME = "textsearch_idx";
	static final String[] TS_COLUMNS = {"ad_client_id","ad_table_id","record_id","BXS_omnTSVector"};
	
}
