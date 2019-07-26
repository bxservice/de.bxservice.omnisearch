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

import org.osgi.service.component.annotations.Component;

import de.bxservice.omnisearch.tools.OmnisearchIndex;

@Component(
		service = OmnisearchIndex.class,
		property= {"indexType:String=TS"}
)
/**
 * The methods in this class are empty because the index is created
 * within a 2pack and to avoid future problems the postgreSQL index
 * should not be deleted.
 * 
 * This methods might be more appropriate for other tools that require to 
 * create/modify the indexes in special ways
 */
public class TextSearchIndex implements OmnisearchIndex {

	@Override
	public void createIndex(String trxName) {
	}

	@Override
	public void updateIndex(String trxName) {
	}

	@Override
	public void dropIndex(String trxName) {
	}

	@Override
	public void recreateIndex(String trxName) {
		dropIndex(trxName);
		createIndex(trxName);
	}

}
