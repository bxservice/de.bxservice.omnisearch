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
import java.util.List;
import java.util.logging.Level;

import org.compiere.model.MPInstance;
import org.compiere.model.MPInstancePara;
import org.compiere.model.MProcess;
import org.compiere.model.PO;
import org.compiere.process.ProcessInfo;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.compiere.util.Trx;

public class OmnisearchHelper {
	
	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(OmnisearchHelper.class);
	
	public static void recreateIndex(String indexType) {
		
		Thread recreateIndexThread = new Thread(() -> {
			int AD_Process_ID = MProcess.getProcess_ID("CreateIndexProcess", null);
			MProcess process = new MProcess(Env.getCtx(), AD_Process_ID, null);
			ProcessInfo m_pi = new ProcessInfo ("", AD_Process_ID);
			if (indexType != null) {
				MPInstance instance = new MPInstance(Env.getCtx(), AD_Process_ID, 0);
				if (!instance.save()) {
					log.log(Level.SEVERE, Msg.getMsg(Env.getCtx(), "ProcessNoInstance"));
					return;
				}
				m_pi.setAD_PInstance_ID(instance.getAD_PInstance_ID());
				MPInstancePara para = new MPInstancePara(instance, 0);
				para.setParameter("BXS_IndexType", indexType);
			}

			String newTrxName = Trx.createTrxName("OmniIndex");
			Trx trx = Trx.get(newTrxName, true);
			if (!process.processIt(m_pi, trx) && m_pi.getClassName() != null) {
				String msg = Msg.getMsg(Env.getCtx(), "ProcessFailed") + " : (" + m_pi.getClassName() + ") " + m_pi.getSummary();
				log.log(Level.SEVERE, msg);
			}
		});
		recreateIndexThread.setDaemon(true);
		recreateIndexThread.start();
	}
	
	public static List<String> getIndexedTables(String indexColumnName, String trxName) {
		List<String> tableNames = new ArrayList<>();

		if (indexExist(indexColumnName, trxName)) {
			StringBuilder sql = new StringBuilder("SELECT AD_TABLE.tablename FROM AD_TABLE")
					.append(" WHERE EXISTS (SELECT 1 FROM AD_COLUMN WHERE AD_COLUMN.AD_TABLE_ID = AD_TABLE.AD_TABLE_ID AND AD_COLUMN.")
					.append(indexColumnName)
					.append(" = 'Y' AND AD_COLUMN.IsActive='Y' AND ColumnSQL IS NULL)")
					.append(" AND AD_TABLE.IsActive='Y' AND AD_TABLE.AD_Client_ID IN (0,?)");

			//Bring the table ids that are indexed
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try {
				pstmt = DB.prepareStatement(sql.toString(), trxName);
				pstmt.setInt(1, Env.getAD_Client_ID(Env.getCtx()));
				rs = pstmt.executeQuery();

				while (!Thread.currentThread().isInterrupted() && rs.next()) {
					tableNames.add(rs.getString(1));
				}
			} catch (Exception e) {
				log.log(Level.SEVERE, sql.toString(), e);
			} finally {
				DB.close(rs, pstmt);
				rs = null;
				pstmt = null;
			}			
		}

		return tableNames;
	}
	
	public static void recreateDocument(String documentType, String trxName) {
		if (documentType != null)
			getDocument(documentType).recreateDocument(trxName);
	}

	//Check if the index column exists to avoid NPE in the validator the first time the plug-in runs
	public static boolean indexExist(String indexType, String trxName) {
		return DB.getSQLValue(trxName, "SELECT 1 FROM ad_column WHERE columnname =?", indexType) > 0;
	}
	
	public static void updateDocument(String documentType, PO po, boolean isNew) {
		Thread recreateDocumentThread = new Thread(() -> {
			getDocument(documentType).updateDocument(po, isNew, null);
		});
		recreateDocumentThread.setDaemon(true);
		recreateDocumentThread.start();
	}
	
	public static void deleteFromDocument(String documentType, PO po) {
		getDocument(documentType).deleteFromDocument(po);
	}
	
	public static OmnisearchDocument getDocument(String documentType) {
		OmnisearchAbstractFactory omnisearchFactory = OmnisearchFactoryProducer.getFactory(OmnisearchFactoryProducer.DOCUMENT_FACTORY);
		return omnisearchFactory.getDocument(documentType);
	}
	
	public static OmnisearchIndex getIndex(String indexType) {
		OmnisearchAbstractFactory omnisearchFactory = OmnisearchFactoryProducer.getFactory(OmnisearchFactoryProducer.INDEX_FACTORY);
		return omnisearchFactory.getIndex(indexType);
	}
}