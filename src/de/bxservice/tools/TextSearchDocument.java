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

public class TextSearchDocument extends AbstractOmnisearchDocument {

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
		        System.out.println(entry.getKey());
		        System.out.println(entry.getValue());
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void recreateDocument(String trxName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void insertIntoDocument(String trxName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteFromDocument(String trxName) {
		// TODO Auto-generated method stub
		
	}
	
	
}
