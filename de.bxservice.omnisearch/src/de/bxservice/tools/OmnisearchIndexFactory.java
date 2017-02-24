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

import org.adempiere.base.Service;
import org.adempiere.base.ServiceQuery;
import org.adempiere.exceptions.AdempiereException;

public class OmnisearchIndexFactory extends OmnisearchAbstractFactory {
	
	@Override
	public OmnisearchIndex getIndex(String indexType) {
		
		if (indexType == null)
			return null;
		
		ServiceQuery query = new ServiceQuery();
		query.put("indexType", indexType);
		OmnisearchIndex custom = Service.locator().locate(OmnisearchIndex.class, query).getService();			
		if (custom == null)
			throw new AdempiereException("No OmnisearchIndex provider found for indexType " + indexType);
		try {
			return custom.getClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {}

		return null;
	}

	@Override
	public OmnisearchDocument getDocument(String documentType) {
		return null;
	}
	
}
