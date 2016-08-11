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

import org.compiere.util.DB;

public class TextSearchIndex implements OmnisearchIndex {
	
	private static final String INDEX_TABLE = "BXS_omnSearch";
	private static final String INDEX_COLUMN = "BXS_omnTSVector";
	private static final String INDEX_NAME = "textsearch_idx";

	@Override
	public void createIndex(String trxName) {
		StringBuilder createStatement = new StringBuilder("CREATE INDEX ");
		createStatement.append(INDEX_NAME);
		createStatement.append(" ON ");
		createStatement.append(INDEX_TABLE);
		createStatement.append(" USING gin(");
		createStatement.append(INDEX_COLUMN);
		createStatement.append(")");
		
		System.out.println(createStatement.toString());
		
		int no = DB.executeUpdateEx(createStatement.toString(), trxName);
		
		System.out.println(no);
		if (no == 1)
			System.out.println("success");
	}

	@Override
	public void updateIndex(String trxName) {
		
	}

	@Override
	public void dropIndex(String trxName) {
		String dropStatement = "DROP INDEX IF EXISTS " + INDEX_NAME;
		DB.executeUpdateEx(dropStatement, trxName);
	}

	@Override
	public void recreateIndex(String trxName) {
		dropIndex(trxName);
		createIndex(trxName);
	}

}
