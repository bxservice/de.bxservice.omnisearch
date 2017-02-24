package de.bxservice.tools;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;

public abstract class AbstractOmnisearchDocument implements OmnisearchDocument {

	/**	Logger							*/
	protected static CLogger log = CLogger.getCLogger (AbstractOmnisearchDocument.class);
	
	protected HashMap<Integer, ArrayList<Integer>> indexedTables;
	
	public HashMap<Integer, ArrayList<Integer>> getIndexedTables(boolean reQuery, String trxName, String indexColumnName) {
		if(indexedTables == null || reQuery)
			getIndexedTables(trxName, indexColumnName);
		
		return indexedTables;
	}
	
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

		ArrayList<Integer> columnIds = null;

		StringBuilder sql = new StringBuilder("SELECT AD_COLUMN.AD_COLUMN_ID FROM AD_TABLE")
		.append(" JOIN AD_COLUMN ON AD_COLUMN.AD_TABLE_ID = AD_TABLE.AD_TABLE_ID AND AD_COLUMN."+ indexColumnName + " = 'Y'")
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
	
}
