package de.bxservice.omnisearch;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import org.compiere.model.MColumn;
import org.compiere.model.MTable;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;

import de.bxservice.omniimpl.TextSearchResult;

public class TSSearch {
	
	protected static CLogger log = CLogger.getCLogger (TSSearch.class);
	private HashMap<Integer, String> indexQuery = new HashMap<>();
	
	public ArrayList<TextSearchResult> performQuery(String query, boolean isAdvanced) {
		
		ArrayList<TextSearchResult> results = new ArrayList<>();
		indexQuery.clear();
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ad_table_id, record_id ");
		sql.append("FROM BXS_omnSearch");
		sql.append(" WHERE bxs_omntsvector @@ ");
		
		if(isAdvanced)
			sql.append("to_tsquery('" + query + "') ");
		else
			sql.append("plainto_tsquery('" + query + "') ");

		sql.append("AND AD_CLIENT_ID IN (0,?) ");

		
		//Check if the table exists and if it's populated
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql.toString(), null);
			pstmt.setInt(1, Env.getAD_Client_ID(Env.getCtx()));
			rs = pstmt.executeQuery();

			TextSearchResult result = null;
			int i = 0;
			while (rs.next())
			{
				result = new TextSearchResult();
				result.setAd_Table_ID(rs.getInt(1));
				result.setRecord_id(rs.getInt(2));
				results.add(result);
				
				if (i < 10) {
					setHeadline(result, query);
				}
				
				i++;
			}
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql.toString(), e);
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null;
			pstmt = null;
		}
		
		return results;
	}
	
	private void setHeadline(TextSearchResult result, String query) {
		
		StringBuilder sql = new StringBuilder();
		
		if (indexQuery.get(result.getAd_Table_ID()) != null) {
			sql.append(indexQuery.get(result.getAd_Table_ID()));
		} else {
			
			ArrayList<Integer> columnIds = getIndexedColumns(result.getAd_Table_ID());
			
			if(columnIds == null || columnIds.isEmpty()) {
				result.setHtmlHeadline("");
				return;
			}
			
			sql.append("SELECT ts_headline(body, q) FROM (");			
			sql.append(getIndexSql(columnIds, result.getAd_Table_ID()));
			
			indexQuery.put(result.getAd_Table_ID(), sql.toString());
		}
		
		//Bring the table ids that are indexed
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql.toString(), null);
			pstmt.setString(1, query);
			pstmt.setInt(2, Env.getAD_Client_ID(Env.getCtx()));
			pstmt.setInt(3, result.getRecord_id());
			rs = pstmt.executeQuery();

			while (!Thread.currentThread().isInterrupted() && rs.next())
			{
				result.setHtmlHeadline(rs.getString(1));
				System.out.println(result.getHtmlHeadline());
			}
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql.toString(), e);
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null;
			pstmt = null;
		}

	}

	private ArrayList<Integer> getIndexedColumns(int AD_Table_ID) {
		
		ArrayList<Integer> columnIds = null;
		//TODO: Cambiar 
		StringBuilder sql = new StringBuilder("SELECT AD_COLUMN.AD_COLUMN_ID FROM AD_TABLE")
		.append(" JOIN AD_COLUMN ON AD_COLUMN.AD_TABLE_ID = AD_TABLE.AD_TABLE_ID AND AD_COLUMN.BXS_IsTSIndex = 'Y'")
		.append(" WHERE AD_TABLE.IsActive='Y' AND AD_COLUMN.IsActive='Y' AND AD_TABLE.AD_Table_ID = ?");
		
        //Bring the table ids that are indexed
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql.toString(), null);
			pstmt.setInt(1, AD_Table_ID);
			rs = pstmt.executeQuery();
			
			columnIds = new ArrayList<>();
			while (!Thread.currentThread().isInterrupted() && rs.next())
			{
				columnIds.add(rs.getInt(1));
			}
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql.toString(), e);
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null;
			pstmt = null;
		}
		
		return columnIds;
	}
	
	
	private String getIndexSql(ArrayList<Integer> columnIds, int AD_Table_ID) {
		return getSelectQuery(AD_Table_ID, columnIds);
	}
	
	private String getSelectQuery(int AD_Table_ID, ArrayList<Integer> columns) {
		
		MTable table = MTable.get(Env.getCtx(), AD_Table_ID);
		String mainTableAlias = "a";
		StringBuilder selectQuery = new StringBuilder();
		selectQuery.append("SELECT ");

		//Columns that want to be indexed
		MColumn column = null;
		//TableName, List of validations after the ON clause
		ArrayList<String> joinClauses = null;
    	for (int i = 0; i < columns.size(); i++) {
    		int AD_Column_ID = columns.get(i);
    		column = MColumn.get(Env.getCtx(), AD_Column_ID);
    		String foreignTableName = column.getReferenceTableName();
    		
    		if (foreignTableName != null) {
    			String foreignAlias = "a" + i;
    			MTable foreignTable = MTable.get(Env.getCtx(), foreignTableName);
    			
    			if (joinClauses == null)
    				joinClauses = new ArrayList<>();
    			
    			joinClauses.add(getJoinClause(foreignTable, mainTableAlias, foreignAlias, column));
    			selectQuery.append(getForeignValues(foreignTable, foreignAlias));

    		} else {
        		selectQuery.append("COALESCE(");
        		selectQuery.append(mainTableAlias);
        		selectQuery.append(".");
        		selectQuery.append(column.getColumnName());
    		}

    		if (i < columns.size() -1)
    			selectQuery.append(",'') || ' ' || "); //space between words
    		else
    			selectQuery.append(",'') ");
    	}
		selectQuery.append(" AS body, q ");

    	selectQuery.append(" FROM ");
    	selectQuery.append(table.getTableName());
    	selectQuery.append(" " + mainTableAlias);

    	if (joinClauses != null && joinClauses.size() > 0) {
    		for (String joinClause : joinClauses)
    	    	selectQuery.append(joinClause);
    	}
    	
    	selectQuery.append(",");
    	selectQuery.append("to_tsquery(?) q");
    	
    	selectQuery.append(" WHERE " + mainTableAlias + ".AD_Client_ID IN (0, ?) AND " + mainTableAlias + ".IsActive='Y'");
    	selectQuery.append(" AND ");
    	selectQuery.append(mainTableAlias + ".");
		selectQuery.append(table.getKeyColumns()[0]); //Record_ID
		selectQuery.append(" = ? ) AS foo WHERE body @@ q");
    	
		return selectQuery.toString();
	}
	
	/**
	 * Gets the values of the identifiers columns when a FK is selected as Index
	 */
	private String getForeignValues(MTable table, String tableAlias) {
		
		String[] identifierColumns = table.getIdentifierColumns(); 
		
		if (identifierColumns != null && identifierColumns.length > 0) {
			StringBuilder foreingColumns = new StringBuilder();
			
			for (int i = 0; i < identifierColumns.length; i++) {
				foreingColumns.append("COALESCE(");
				foreingColumns.append(tableAlias);
				foreingColumns.append(".");
				foreingColumns.append(identifierColumns[i]);
				
				if (i < identifierColumns.length -1)
					foreingColumns.append(",'') || ' ' || "); //space between words
			}
			
			return foreingColumns.toString();
		}
		else
			return null;
	}
	
	private String getJoinClause(MTable foreignTable, String tableAlias, String foreignTableAlias, MColumn currentColumn) {
		
		if (foreignTable == null || currentColumn == null)
			return null;
		
		StringBuilder joinClause = new StringBuilder();
		joinClause.append(" LEFT JOIN ");
		joinClause.append(foreignTable.getTableName());
		joinClause.append(" " + foreignTableAlias);
		joinClause.append(" ON ");
		joinClause.append(tableAlias);
		joinClause.append(".");
		joinClause.append(currentColumn.getColumnName());
		joinClause.append(" = ");
		joinClause.append(foreignTableAlias);
		joinClause.append(".");
		joinClause.append(foreignTable.getKeyColumns()[0]);
		
		return joinClause.toString();
	}
	
}
