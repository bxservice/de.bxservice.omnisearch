/******************************************************************************
 * Product: iDempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2012 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
/** Generated Model - DO NOT CHANGE */
package de.bxservice.model;

import java.sql.ResultSet;
import java.util.Properties;
import org.compiere.model.*;

/** Generated Model for BXS_omnSearch
 *  @author iDempiere (generated) 
 *  @version Release 3.1 - $Id$ */
public class X_BXS_omnSearch extends PO implements I_BXS_omnSearch, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20160810L;

    /** Standard Constructor */
    public X_BXS_omnSearch (Properties ctx, int BXS_omnSearch_ID, String trxName)
    {
      super (ctx, BXS_omnSearch_ID, trxName);
      /** if (BXS_omnSearch_ID == 0)
        {
			setAD_Table_ID (0);
			setBXS_omnSearch_ID (0);
			setRecord_ID (0);
        } */
    }

    /** Load Constructor */
    public X_BXS_omnSearch (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 4 - System 
      */
    protected int get_AccessLevel()
    {
      return accessLevel.intValue();
    }

    /** Load Meta Data */
    protected POInfo initPO (Properties ctx)
    {
      POInfo poi = POInfo.getPOInfo (ctx, Table_ID, get_TrxName());
      return poi;
    }

    public String toString()
    {
      StringBuffer sb = new StringBuffer ("X_BXS_omnSearch[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	public org.compiere.model.I_AD_Table getAD_Table() throws RuntimeException
    {
		return (org.compiere.model.I_AD_Table)MTable.get(getCtx(), org.compiere.model.I_AD_Table.Table_Name)
			.getPO(getAD_Table_ID(), get_TrxName());	}

	/** Set Table.
		@param AD_Table_ID 
		Database Table information
	  */
	public void setAD_Table_ID (int AD_Table_ID)
	{
		if (AD_Table_ID < 1) 
			set_Value (COLUMNNAME_AD_Table_ID, null);
		else 
			set_Value (COLUMNNAME_AD_Table_ID, Integer.valueOf(AD_Table_ID));
	}

	/** Get Table.
		@return Database Table information
	  */
	public int getAD_Table_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_Table_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set OmniSearch Table.
		@param BXS_omnSearch_ID OmniSearch Table	  */
	public void setBXS_omnSearch_ID (int BXS_omnSearch_ID)
	{
		if (BXS_omnSearch_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_BXS_omnSearch_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_BXS_omnSearch_ID, Integer.valueOf(BXS_omnSearch_ID));
	}

	/** Get OmniSearch Table.
		@return OmniSearch Table	  */
	public int getBXS_omnSearch_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_BXS_omnSearch_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set BXS_omnSearch_UU.
		@param BXS_omnSearch_UU BXS_omnSearch_UU	  */
	public void setBXS_omnSearch_UU (String BXS_omnSearch_UU)
	{
		set_Value (COLUMNNAME_BXS_omnSearch_UU, BXS_omnSearch_UU);
	}

	/** Get BXS_omnSearch_UU.
		@return BXS_omnSearch_UU	  */
	public String getBXS_omnSearch_UU () 
	{
		return (String)get_Value(COLUMNNAME_BXS_omnSearch_UU);
	}

	/** Set TextSearch Vector.
		@param BXS_omnTSVector TextSearch Vector	  */
	public void setBXS_omnTSVector (String BXS_omnTSVector)
	{
		set_Value (COLUMNNAME_BXS_omnTSVector, BXS_omnTSVector);
	}

	/** Get TextSearch Vector.
		@return TextSearch Vector	  */
	public String getBXS_omnTSVector () 
	{
		return (String)get_Value(COLUMNNAME_BXS_omnTSVector);
	}

	/** Set Record ID.
		@param Record_ID 
		Direct internal record ID
	  */
	public void setRecord_ID (int Record_ID)
	{
		if (Record_ID < 0) 
			set_ValueNoCheck (COLUMNNAME_Record_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_Record_ID, Integer.valueOf(Record_ID));
	}

	/** Get Record ID.
		@return Direct internal record ID
	  */
	public int getRecord_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Record_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}
}