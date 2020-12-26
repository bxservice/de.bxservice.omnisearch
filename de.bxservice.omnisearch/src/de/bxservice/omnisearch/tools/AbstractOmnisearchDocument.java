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
package de.bxservice.omnisearch.tools;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import org.compiere.model.MColumn;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;

public abstract class AbstractOmnisearchDocument implements OmnisearchDocument {

	/**	Logger							*/
	protected static CLogger log = CLogger.getCLogger (AbstractOmnisearchDocument.class);
	
	protected HashMap<Integer, ArrayList<Integer>> indexedTables;
	protected HashMap<String, ArrayList<MColumn>> foreignTables;
	
	public HashMap<Integer, ArrayList<Integer>> getIndexedTables(boolean reQuery, String trxName, String indexColumnName) {
		if(indexedTables == null || reQuery)
			getIndexedTables(trxName, indexColumnName);
		
		return indexedTables;
	}
	
	/**
	 * Remove a hashmap with tables and its indexed columns
	 * @param trxName
	 * @param indexColumnName
	 */
	private void getIndexedTables(String trxName, String indexColumnName) {
		StringBuilder sql = new StringBuilder("SELECT AD_TABLE.AD_TABLE_ID, AD_COLUMN.AD_COLUMN_ID FROM AD_TABLE")
		.append(" JOIN AD_COLUMN ON AD_COLUMN.AD_TABLE_ID = AD_TABLE.AD_TABLE_ID AND AD_COLUMN."+ indexColumnName +" = 'Y'")
		.append(" WHERE AD_TABLE.IsActive='Y' AND AD_COLUMN.IsActive='Y' AND AD_TABLE.AD_Client_ID IN (0,?) AND ColumnSQL IS NULL")
		.append(" GROUP BY AD_TABLE.AD_TABLE_ID, AD_COLUMN_ID ");
		
        //Bring the table ids that are indexed
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql.toString(), trxName);
			pstmt.setInt(1, Env.getAD_Client_ID(Env.getCtx()));
			rs = pstmt.executeQuery();
			
			indexedTables = new HashMap<>();
			int tableID = -1;
			int columnID = -1;
			while (!Thread.currentThread().isInterrupted() && rs.next())
			{
				tableID = rs.getInt(1);
				columnID = rs.getInt(2);
				
				if (!indexedTables.containsKey(tableID)) {
					indexedTables.put(tableID, new ArrayList<Integer>());
				}
				indexedTables.get(tableID).add(columnID);
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

	public ArrayList<Integer> getIndexedColumns(int AD_Table_ID, String indexColumnName) {
		if(indexedTables == null)
			getIndexedTables(null, indexColumnName);

		return indexedTables.get(AD_Table_ID);
	}
	
	public ArrayList<String> getIndexedColumnNames(int AD_Table_ID, String indexColumnName) {

		ArrayList<String> columnNames = null;

		String sql = "SELECT AD_COLUMN.columnname FROM AD_TABLE "
				+ " JOIN AD_COLUMN ON AD_COLUMN.AD_TABLE_ID = AD_TABLE.AD_TABLE_ID AND AD_COLUMN." 
				+ indexColumnName + " = 'Y' "
				+ " WHERE AD_TABLE.IsActive='Y' AND AD_COLUMN.IsActive='Y' AND AD_TABLE.AD_Table_ID = ?";

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = DB.prepareStatement(sql, null);
			pstmt.setInt(1, AD_Table_ID);
			rs = pstmt.executeQuery();

			columnNames = new ArrayList<>();
			while (!Thread.currentThread().isInterrupted() && rs.next()) {
				columnNames.add(rs.getString(1));
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, sql.toString(), e);
		} finally {
			DB.close(rs, pstmt);
			rs = null;
			pstmt = null;
		}

		return columnNames;
	}
	
	protected HashMap<String, ArrayList<MColumn>> getFKParentTables(String indexColumnName) {
		foreignTables = new HashMap<>();

		String sql = "SELECT AD_COLUMN_ID FROM AD_COLUMN" + 
				" WHERE AD_COLUMN." + indexColumnName + 
				" = 'Y' AND AD_COLUMN.IsActive='Y' AND ColumnSQL IS NULL" + 
				" AND AD_REFERENCE_ID IN (?,?,?,?,?,?,?,?,?,?,?)";

		//Bring the column ids from the FK
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = DB.prepareStatement(sql.toString(), null);
			pstmt.setInt(1, DisplayType.TableDir);
			pstmt.setInt(2, DisplayType.Search);
			pstmt.setInt(3, DisplayType.Table);
			pstmt.setInt(4, DisplayType.List);
			pstmt.setInt(5, DisplayType.Payment);
			pstmt.setInt(6, DisplayType.Location);
			pstmt.setInt(7, DisplayType.Account);
			pstmt.setInt(8, DisplayType.Locator);
			pstmt.setInt(9, DisplayType.PAttribute);
			pstmt.setInt(10, DisplayType.Assignment);
			pstmt.setInt(11, DisplayType.RadiogroupList);
			rs = pstmt.executeQuery();

			MColumn column = null;
			while (!Thread.currentThread().isInterrupted() && rs.next()) {
				column = MColumn.get(Env.getCtx(), rs.getInt(1));

				if (column != null) {
					String tableName = column.getReferenceTableName();
					if (tableName != null) {
						if (!foreignTables.containsKey(tableName))
							foreignTables.put(tableName, new ArrayList<MColumn>());

						foreignTables.get(tableName).add(column);
					}
				}
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, sql.toString(), e);
		} finally {
			DB.close(rs, pstmt);
			rs = null;
			pstmt = null;
		}			

		return foreignTables;
	}
	
	protected ArrayList<MColumn> getReferencedColumns(String indexColumnName, String tableName) {
		if(foreignTables == null)
			getFKParentTables(indexColumnName);
		
		return foreignTables.get(tableName);
	}
	
}
