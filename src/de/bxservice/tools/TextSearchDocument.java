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
* - Diego Ruiz - Bx Service GmbH                                      *
**********************************************************************/
package de.bxservice.tools;

import java.util.ArrayList;
import java.util.Map.Entry;

import org.compiere.model.MColumn;
import org.compiere.model.MTable;
import org.compiere.util.DB;
import org.compiere.util.Env;

public class TextSearchDocument extends AbstractOmnisearchDocument {
	
	private static final String   TS_TABLE_NAME = "BXS_omnSearch";
	private static final String[] TS_COLUMNS = {"ad_client_id","ad_table_id","record_id","BXS_omnTSVector"};

	@Override
	/**
	 * fills the vector column in the TS table
	 */
	public void buildDocument(String trxName) {
		if (indexedTables == null)
			getIndexedTables(true, trxName, "BXS_IsTSIndex");
		
		if (indexedTables != null && indexedTables.size() > 0) {
			System.out.println("Indexando ...");
		    for(Entry<Integer, ArrayList<Integer>> entry: indexedTables.entrySet()) {
	    		insertIntoDocument(trxName, entry.getKey(), entry.getValue());
		    }
		} else {
			System.out.println("No hay nada para indexar");
		}
		
	}

	@Override
	public void updateDocument(String trxName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteDocument(String trxName) {
		String sql = "TRUNCATE " + TS_TABLE_NAME;
		DB.executeUpdateEx(sql, trxName);
	}

	@Override
	public void recreateDocument(String trxName) {
		deleteDocument(trxName);
		buildDocument(trxName);		
	}

	@Override
	public void insertIntoDocument(String trxName, int AD_Table_ID, ArrayList<Integer> columns) {
    	if (columns == null || columns.isEmpty())
    		return;
    	
    	System.out.println("Indexing " + AD_Table_ID + " " + columns);
    	
    	StringBuilder insertQuery = new StringBuilder();
    	insertQuery.append("INSERT INTO ");
    	insertQuery.append(TS_TABLE_NAME);
    	insertQuery.append(" (");
    	for (String columnName : TS_COLUMNS) {
    		insertQuery.append(columnName);
    		insertQuery.append(",");
    	}
    	insertQuery.deleteCharAt(insertQuery.length() -1); //remove last comma
    	insertQuery.append(") ");
    	
    	String selectQuery = getSelectQuery(AD_Table_ID, columns);
    	
    	if (selectQuery != null)
    		insertQuery.append(selectQuery);
    	else
    		System.out.println("No se puede indexar una tabal con mas de una columna");

    	System.out.println("QWUETR " + insertQuery.toString());
    	
    	if (Env.getAD_Client_ID(Env.getCtx())  == 0)
    		DB.executeUpdateEx(insertQuery.toString(), trxName);
    	else
    		DB.executeUpdateEx(insertQuery.toString(), new Object[]{Env.getAD_Client_ID(Env.getCtx())},trxName);
    	

	}
	
	private String getSelectQuery(int AD_Table_ID, ArrayList<Integer> columns) {
		
		MTable table = MTable.get(Env.getCtx(), AD_Table_ID);
		
		StringBuilder selectQuery = new StringBuilder();
		selectQuery.append("SELECT ");
		selectQuery.append(Env.getAD_Client_ID(Env.getCtx())); //AD_Client_ID
		selectQuery.append(", ");
		selectQuery.append(AD_Table_ID); //AD_Table_ID
		selectQuery.append(", ");
				
		if (table.getKeyColumns() != null && table.getKeyColumns().length == 1) 
			selectQuery.append(table.getKeyColumns()[0]); //Record_ID
		else
			return null;
		
		selectQuery.append(", ");
		
		selectQuery.append("to_tsvector(");
		//Columns that want to be indexed
    	for (int i = 0; i < columns.size(); i++) {
    		int AD_Column_ID = columns.get(i);
    		selectQuery.append("COALESCE(");
    		selectQuery.append(MColumn.getColumnName(Env.getCtx(), AD_Column_ID));
    		
    		if (i < columns.size() -1)
    			selectQuery.append(",'') || ' ' || "); //space between words
    		else
    			selectQuery.append(") ");
    	}
		selectQuery.append(") ");

    	selectQuery.append(" FROM ");
    	selectQuery.append(table.getTableName());

    	//If System -> all clients
    	if (Env.getAD_Client_ID(Env.getCtx())  == 0)
    		selectQuery.append(" WHERE IsActive='Y'");
    	else
    		selectQuery.append(" WHERE AD_Client_ID IN (0, ?) AND IsActive='Y'");
    			
		return selectQuery.toString();
	}

	@Override
	public void deleteFromDocument(String trxName) {
		
	}
	
	
}
