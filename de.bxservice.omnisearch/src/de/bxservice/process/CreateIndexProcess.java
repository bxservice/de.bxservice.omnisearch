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
package de.bxservice.process;

import java.util.logging.Level;

import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;

import de.bxservice.tools.OmnisearchAbstractFactory;
import de.bxservice.tools.OmnisearchDocument;
import de.bxservice.tools.OmnisearchFactoryProducer;
import de.bxservice.tools.OmnisearchIndex;

public class CreateIndexProcess extends SvrProcess {
	
	String indexType = null;
	
	@Override
	protected void prepare() {
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("BXS_IndexType"))
				indexType = (String)para[i].getParameter();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
	}

	@Override
	protected String doIt() throws Exception {
		
		//Set default in case of null to avoid NPE
		if (indexType == null)
			indexType = OmnisearchAbstractFactory.TEXTSEARCH_INDEX;

		//First populate the vector then create the index for faster performance
		//Creates the document
		log.log(Level.INFO, "Creating the document");
		OmnisearchAbstractFactory omnisearchFactory = OmnisearchFactoryProducer.getFactory(OmnisearchFactoryProducer.DOCUMENT_FACTORY);
		OmnisearchDocument document = omnisearchFactory.getDocument(indexType);
		document.buildDocument(get_TrxName());
		
		//Creates the index
		log.log(Level.INFO, "Creating the index");
		omnisearchFactory = OmnisearchFactoryProducer.getFactory(OmnisearchFactoryProducer.INDEX_FACTORY);
		OmnisearchIndex index = omnisearchFactory.getIndex(indexType);
		index.recreateIndex(get_TrxName());
		
        return "";
	}
}
