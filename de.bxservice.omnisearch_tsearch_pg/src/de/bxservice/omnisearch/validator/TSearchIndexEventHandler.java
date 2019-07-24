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
package de.bxservice.omnisearch.validator;

import java.util.List;
import java.util.logging.Level;

import org.adempiere.base.event.AbstractEventHandler;
import org.adempiere.base.event.IEventManager;
import org.adempiere.base.event.IEventTopics;
import org.compiere.model.MColumn;
import org.compiere.model.MSysConfig;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.osgi.service.event.Event;

import de.bxservice.omniimpl.TextSearchValues;
import de.bxservice.omnisearch.tools.OmnisearchAbstractFactory;
import de.bxservice.omnisearch.tools.OmnisearchHelper;

public class TSearchIndexEventHandler extends AbstractEventHandler {

	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(TSearchIndexEventHandler.class);
	
	private String trxName = null;

	@Override
	protected void initialize() {
		log.warning("");

		List<String> indexedTables = OmnisearchHelper.getIndexedTables(TextSearchValues.TS_INDEX_NAME, trxName);

		for (String tableName : indexedTables) {
			registerTableEvent(IEventTopics.PO_AFTER_NEW, tableName);
			registerTableEvent(IEventTopics.PO_AFTER_CHANGE, tableName);
			registerTableEvent(IEventTopics.PO_AFTER_DELETE, tableName);
		}
		
		registerTableEvent(IEventTopics.PO_AFTER_NEW, MColumn.Table_Name);
		registerTableEvent(IEventTopics.PO_AFTER_CHANGE, MColumn.Table_Name);
		registerTableEvent(IEventTopics.PO_AFTER_DELETE, MColumn.Table_Name);
	}

	@Override
	protected void doHandleEvent(Event event) {
		String type = event.getTopic();

		if (type.equals(IEventTopics.PO_AFTER_NEW)
				|| type.equals(IEventTopics.PO_AFTER_CHANGE)
				|| type.equals(IEventTopics.PO_AFTER_DELETE)) {

			PO po = getPO(event);
			if (po instanceof MColumn) {
				if ((type.equals(IEventTopics.PO_AFTER_CHANGE) &&
						po.is_ValueChanged(TextSearchValues.TS_INDEX_NAME)) || 
						(po.get_ValueAsBoolean(TextSearchValues.TS_INDEX_NAME))) {
					//If the Text search index flag is changed -> register/unregister the modified table
					trxName = po.get_TrxName();
					IEventManager tempManager = eventManager;
					unbindEventManager(eventManager);
					bindEventManager(tempManager);
				}
			}

			if (MSysConfig.getBooleanValue(OmnisearchHelper.MSYSCONFIG_AUTOMATIC_RECREATE, false)) {
				if (log.isLoggable(Level.INFO))
					log.info("Recreating index po : " + po + "type: "+type);

				OmnisearchHelper.recreateIndex(OmnisearchAbstractFactory.TEXTSEARCH_INDEX);				
			}
		}
	}

}
