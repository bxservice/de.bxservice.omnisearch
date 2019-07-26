/**********************************************************************
 * Copyright (C) Contributors                                          *
 *                                                                     *
 * This program is free software; you can redistribute it and/or       *
 * modify it under the terms of the GNU General Public License         *
 * as published by the Free Software Foundation; either version 2      *
 * of the License, or (at your option) any later version.              *
 *                                                                     *
 * This program is distributed in the hope that it will be useful,     *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of      *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the        *
 * GNU General Public License for more details.                        *
 *                                                                     *
 * You should have received a copy of the GNU General Public License   *
 * along with this program; if not, write to the Free Software         *
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,          *
 * MA 02110-1301, USA.                                                 *
 *                                                                     *
 * Contributors:                                                       *
 * - Diego Ruiz - BX Service GmbH                                      *
 **********************************************************************/
package de.bxservice.omniimpl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.compiere.model.MClient;
import org.compiere.model.MColumn;
import org.compiere.model.MTable;
import org.compiere.model.PO;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.osgi.service.component.annotations.Component;

import de.bxservice.omnisearch.tools.AbstractOmnisearchDocument;
import de.bxservice.omnisearch.tools.OmnisearchDocument;
import de.bxservice.omnisearch.tools.TextSearchResult;

@Component(
		service = OmnisearchDocument.class,
		property= {"documentType:String=TS"}
)
public class TextSearchDocument extends AbstractOmnisearchDocument {

	private HashMap<Integer, String> indexQuery = new HashMap<>();

	@Override
	/**
	 * fills the vector column in the TS table
	 */
	public void buildDocument(String trxName) {
		if (indexedTables == null)
			getIndexedTables(true, trxName, TextSearchValues.TS_INDEX_NAME);

		if (indexedTables != null && indexedTables.size() > 0) {
			log.log(Level.INFO, "Indexing...");
			for(Entry<Integer, ArrayList<Integer>> entry: indexedTables.entrySet()) {
				insertIntoDocument(trxName, entry.getKey(), entry.getValue());
			}
		} else {
			log.log(Level.WARNING, "There's nothing to index");
		}

	}

	@Override
	public void updateDocument(PO po, boolean isNew, String trxName) {
		if (po == null)
			return;

		ArrayList<Integer> columnIds = getIndexedColumns(po.get_Table_ID(), TextSearchValues.TS_INDEX_NAME);

		//Insert 
		if (isNew) {
			insertIntoDocument(trxName, po.get_Table_ID(), po.get_ID(), columnIds);
		} else { //Update
			ArrayList<String> columnNames = getIndexedColumnNames(po.get_Table_ID(), TextSearchValues.TS_INDEX_NAME);
			//Check if one of the indexed columns was modified
			boolean indexChanged = false;
			for (String columnName : columnNames) {
				if (po.is_ValueChanged(columnName)) {
					indexChanged = true;
					break;
				}
			}

			if (indexChanged)
				updateRecord(po, columnIds, trxName);
		}
	}

	@Override
	public void deleteDocument(String trxName) {
		String sql = "DELETE FROM " + TextSearchValues.TS_TABLE_NAME + " WHERE AD_Client_ID = ?" ;
		DB.executeUpdateEx(sql, new Object[] {Env.getAD_Client_ID(Env.getCtx())}, trxName);
	}

	@Override
	public void recreateDocument(String trxName) {
		deleteDocument(trxName);
		buildDocument(trxName);
	}
	
	@Override
	public void insertIntoDocument(String trxName, int AD_Table_ID, int Record_ID, ArrayList<Integer> columns) {
		if (columns == null || columns.isEmpty())
			return;

		log.log(Level.INFO, "Indexing " + AD_Table_ID + " " + columns);

		StringBuilder insertQuery = new StringBuilder();
		insertQuery.append("INSERT INTO ");
		insertQuery.append(TextSearchValues.TS_TABLE_NAME);
		insertQuery.append(" (");
		for (String columnName : TextSearchValues.TS_COLUMNS) {
			insertQuery.append(columnName);
			insertQuery.append(",");
		}
		insertQuery.deleteCharAt(insertQuery.length() -1); //remove last comma
		insertQuery.append(") ");

		String selectQuery = getSelectQuery(AD_Table_ID, columns, false, Record_ID > 0);

		if (selectQuery == null) {
			log.log(Level.WARNING, "A table with more than one key column cannot be indexed");
		} else {
			insertQuery.append(selectQuery);
			log.log(Level.FINEST, insertQuery.toString());

			Object[] params;
			if (Record_ID > 0)
				params = new Object[]{Env.getAD_Client_ID(Env.getCtx()), Record_ID};
			else 
				params = new Object[]{Env.getAD_Client_ID(Env.getCtx())};

			DB.executeUpdateEx(insertQuery.toString(), params, trxName);
		}
	}

	@Override
	public void insertIntoDocument(String trxName, int AD_Table_ID, ArrayList<Integer> columns) {
		insertIntoDocument(trxName, AD_Table_ID, -1, columns);
	}
	
	private String getSelectQuery(int AD_Table_ID, ArrayList<Integer> columns, boolean isSearch) {
		return getSelectQuery(AD_Table_ID, columns, isSearch, false);
	}
	
	private String getSelectQuery(int AD_Table_ID, ArrayList<Integer> columns, boolean isSearch, boolean isSingleRecord) {

		MTable table = MTable.get(Env.getCtx(), AD_Table_ID);
		String mainTableAlias = "a";
		StringBuilder selectQuery = new StringBuilder();
		selectQuery.append("SELECT ");
		
		if (!isSearch) {
			selectQuery.append(mainTableAlias + ".AD_Client_ID"); //AD_Client_ID
			selectQuery.append(", ");
			selectQuery.append(AD_Table_ID); //AD_Table_ID
			selectQuery.append(", ");

			if (table.getKeyColumns() != null && table.getKeyColumns().length == 1) 
				selectQuery.append(table.getKeyColumns()[0]); //Record_ID
			else
				return null;

			selectQuery.append(", ");

			selectQuery.append("to_tsvector(");
			selectQuery.append("'" + getTSConfig() + "', "); //Language Parameter config		
		}

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

		if (isSearch)
			selectQuery.append(" AS body, q ");
		else
			selectQuery.append(") ");

		selectQuery.append(" FROM ");
		selectQuery.append(table.getTableName());
		selectQuery.append(" " + mainTableAlias);

		if (joinClauses != null && joinClauses.size() > 0) {
			for (String joinClause : joinClauses)
				selectQuery.append(joinClause);
		}

		if (isSearch) {
	    	selectQuery.append(",");
	    	selectQuery.append("to_tsquery(?) q");
	    	
	    	selectQuery.append(" WHERE " + mainTableAlias + ".AD_Client_ID = ?");
	    	selectQuery.append(" AND ");
	    	selectQuery.append(mainTableAlias + ".");
			selectQuery.append(table.getKeyColumns()[0]); //Record_ID
			selectQuery.append(" = ? ) AS foo WHERE body @@ q");
		} else {
			selectQuery.append(" WHERE " + mainTableAlias + ".AD_Client_ID = ?");
			if (isSingleRecord) {
		    	selectQuery.append(" AND ");
		    	selectQuery.append(mainTableAlias + ".");
				selectQuery.append(table.getKeyColumns()[0]); //Record_ID
				selectQuery.append(" = ? ");
			}
		}

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
	
	

	private String getTSConfig() {
		return MClient.get(Env.getCtx()).getLanguage().getLocale().getDisplayLanguage(Locale.ENGLISH);
	}

	@Override
	public boolean isValidDocument() {
		String sql = "SELECT COUNT(record_id) FROM " + TextSearchValues.TS_TABLE_NAME + " WHERE AD_CLIENT_ID IN (0,?)";

		//Check if the table exists and if it's populated
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql, null);
			pstmt.setInt(1, Env.getAD_Client_ID(Env.getCtx()));
			rs = pstmt.executeQuery();

			if (rs.next())
			{
				int noRecords = rs.getInt(1);

				if (noRecords > 0)
					return true;
				else
					return false;
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

		return false;
	}

	@Override
	public void deleteFromDocument(PO po) {
		String sql = "DELETE FROM " + TextSearchValues.TS_TABLE_NAME + " WHERE AD_Client_ID = ? "
				+ " AND AD_Table_ID = ? AND Record_ID = ?";
		DB.executeUpdateEx(sql, new Object[] {Env.getAD_Client_ID(Env.getCtx()), po.get_Table_ID(), po.get_IDOld()}, null);
	}

	@Override
	public ArrayList<TextSearchResult> performQuery(String query, boolean isAdvanced) {

		ArrayList<TextSearchResult> results = new ArrayList<>();
		indexQuery.clear();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT DISTINCT ad_table_id, record_id ");
		sql.append("FROM " + TextSearchValues.TS_TABLE_NAME);
		sql.append(" WHERE bxs_omntsvector @@ ");

		if(isAdvanced)
			sql.append("to_tsquery('" + query + "') ");
		else
			sql.append("plainto_tsquery('" + query + "') ");

		sql.append("AND AD_CLIENT_ID IN (0,?) ");

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
				result.setAD_Table_ID(rs.getInt(1));
				result.setRecord_ID(rs.getInt(2));
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

	@Override
	public void setHeadline(TextSearchResult result, String query) {
		
		if (result.getHtmlHeadline() != null && !result.getHtmlHeadline().isEmpty())
			return;

		StringBuilder sql = new StringBuilder();

		if (indexQuery.get(result.getAD_Table_ID()) != null) {
			sql.append(indexQuery.get(result.getAD_Table_ID()));
		} else {

			ArrayList<Integer> columnIds = getIndexedColumns(result.getAD_Table_ID(), TextSearchValues.TS_INDEX_NAME);

			if(columnIds == null || columnIds.isEmpty()) {
				result.setHtmlHeadline("");
				return;
			}

			sql.append("SELECT ts_headline(body, q) FROM (");			
			sql.append(getIndexSql(columnIds, result.getAD_Table_ID()));

			indexQuery.put(result.getAD_Table_ID(), sql.toString());
		}

		//Bring the table ids that are indexed
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql.toString(), null);
			pstmt.setString(1, query);
			pstmt.setInt(2, Env.getAD_Client_ID(Env.getCtx()));
			pstmt.setInt(3, result.getRecord_ID());
			rs = pstmt.executeQuery();

			while (!Thread.currentThread().isInterrupted() && rs.next())
			{
				result.setHtmlHeadline(rs.getString(1));
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
	
	private String getIndexSql(ArrayList<Integer> columnIds, int AD_Table_ID) {
		return getSelectQuery(AD_Table_ID, columnIds, true);
	}
	
	/**
	 * 
	 * @param po 
	 * @param columnIDs -> columns that are indexed
	 * @return A String with the value to be written in the BXS_omnTSVector column
	 */
	private void updateRecord(PO po, ArrayList<Integer> columnIDs, String trxName) {

		StringBuilder updateQuery = new StringBuilder();
		updateQuery.append("UPDATE ");
		updateQuery.append(TextSearchValues.TS_TABLE_NAME);
		updateQuery.append(" SET ");
		updateQuery.append(TextSearchValues.INDEX_COLUMN);
		
		updateQuery.append(" = (");
		MTable table = MTable.get(Env.getCtx(), po.get_Table_ID());
		String mainTableAlias = "a";
		
		updateQuery.append("SELECT ");
		updateQuery.append("to_tsvector(");
		updateQuery.append("'" + getTSConfig() + "', "); //Language Parameter config

		//Columns that want to be indexed
		MColumn column = null;
		//TableName, List of validations after the ON clause
		ArrayList<String> joinClauses = null;
		for (int i = 0; i < columnIDs.size(); i++) {
			column = MColumn.get(Env.getCtx(), columnIDs.get(i));
			String foreignTableName = column.getReferenceTableName();

			if (foreignTableName != null) {
				String foreignAlias = "a" + i;
				MTable foreignTable = MTable.get(Env.getCtx(), foreignTableName);

				if (joinClauses == null)
					joinClauses = new ArrayList<>();

				joinClauses.add(getJoinClause(foreignTable, mainTableAlias, foreignAlias, column));
				updateQuery.append(getForeignValues(foreignTable, foreignAlias));

			} else {
				updateQuery.append("COALESCE(");
				updateQuery.append(mainTableAlias);
				updateQuery.append(".");
				updateQuery.append(column.getColumnName());
			}

			if (i < columnIDs.size() -1)
				updateQuery.append(",'') || ' ' || "); //space between words
			else
				updateQuery.append(",'') ");
		}
		updateQuery.append(") ");

		updateQuery.append(" FROM ");
		updateQuery.append(table.getTableName());
		updateQuery.append(" " + mainTableAlias);

		if (joinClauses != null && joinClauses.size() > 0) {
			for (String joinClause : joinClauses)
				updateQuery.append(joinClause);
		}

		updateQuery.append(" WHERE " + mainTableAlias + ".AD_Client_ID = ?");
		updateQuery.append(" AND ");
		updateQuery.append(mainTableAlias + ".");
		updateQuery.append(table.getKeyColumns()[0]); //Record_ID
		updateQuery.append(" = ? )");
		
		updateQuery.append(" WHERE AD_Table_ID = ? AND Record_ID = ?");
		
		DB.executeUpdateEx(updateQuery.toString(), 
				new Object[] {Env.getAD_Client_ID(Env.getCtx()), po.get_ID(), po.get_Table_ID(), po.get_ID()}, 
				trxName);
	}
}
