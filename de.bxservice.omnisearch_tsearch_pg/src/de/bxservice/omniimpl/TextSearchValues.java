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

public interface TextSearchValues {

	static final String   TS_TABLE_NAME = "BXS_omnSearch";
	static final String   TS_INDEX_NAME = "BXS_IsTSIndex";
	static final String   INDEX_COLUMN = "BXS_omnTSVector";
	static final String   INDEX_NAME = "textsearch_idx";
	static final String[] TS_COLUMNS = {"ad_client_id","ad_table_id","record_id","BXS_omnTSVector"};
	
}
