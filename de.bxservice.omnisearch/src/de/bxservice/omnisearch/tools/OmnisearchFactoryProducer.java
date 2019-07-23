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

public class OmnisearchFactoryProducer {

	public static final String DOCUMENT_FACTORY = "DOCUMENT_FACTORY";
	public static final String INDEX_FACTORY = "INDEX_FACTORY";
	
	public static OmnisearchAbstractFactory getFactory(String choice) {
		   
	      if (DOCUMENT_FACTORY.equalsIgnoreCase(choice)) {
	         return new OmnisearchDocumentFactory();
	         
	      } else if (INDEX_FACTORY.equalsIgnoreCase(choice)) {
	         return new OmnisearchIndexFactory();
	      }
	      
	      return null;
	   }
	
}
