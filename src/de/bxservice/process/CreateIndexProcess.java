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

import org.compiere.process.SvrProcess;

import de.bxservice.tools.OmnisearchAbstractFactory;
import de.bxservice.tools.OmnisearchDocument;
import de.bxservice.tools.OmnisearchFactoryProducer;
import de.bxservice.tools.OmnisearchIndex;


/**
 * Read if it is TS or Lucene or whatever
 * @author diego
 *
 */
public class CreateIndexProcess extends SvrProcess {
	

	@Override
	protected void prepare() {
	}	

	@Override
	protected String doIt() throws Exception {

		//First populate the vector -> create the index
		//Creates the document
		log.log(Level.INFO, "Creating the document");
		OmnisearchAbstractFactory omnisearchFactory = OmnisearchFactoryProducer.getFactory(OmnisearchFactoryProducer.DOCUMENT_FACTORY);
		OmnisearchDocument document = omnisearchFactory.getDocument("TextSearch");
		document.buildDocument(get_TrxName());
		
		
		
		//Creates the index
		log.log(Level.INFO, "Creating the index");
		omnisearchFactory = OmnisearchFactoryProducer.getFactory(OmnisearchFactoryProducer.INDEX_FACTORY);
		OmnisearchIndex index = omnisearchFactory.getIndex("TextSearch");
		index.createIndex(get_TrxName());
		
        return null;
	}
}
