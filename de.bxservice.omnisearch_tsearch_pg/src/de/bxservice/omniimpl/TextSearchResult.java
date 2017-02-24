package de.bxservice.omniimpl;

import org.compiere.model.MTable;
import org.compiere.model.MUserDefWin;
import org.compiere.model.MWindow;
import org.compiere.model.PO;
import org.compiere.util.Env;
import org.compiere.util.Util;

public class TextSearchResult {
	
	private int AD_Table_ID;
	private int record_id;
	private String htmlHeadline;
	
	public int getAd_Table_ID() {
		return AD_Table_ID;
	}
	
	public void setAd_Table_ID(int ad_table_id) {
		this.AD_Table_ID = ad_table_id;
	}
	
	public int getRecord_id() {
		return record_id;
	}
	
	public void setRecord_id(int record_id) {
		this.record_id = record_id;
	}
	
	public String getHtmlHeadline() {
		return htmlHeadline;
	}
	
	public void setHtmlHeadline(String htmlHeadline) {
		this.htmlHeadline = htmlHeadline;
	}
	
	public String getLabel() {
		String windowName;
		int windowID = Env.getZoomWindowID(getAd_Table_ID(), getRecord_id());
		
		MWindow win = MWindow.get(Env.getCtx(), windowID);
		MUserDefWin userDef = MUserDefWin.getBestMatch(Env.getCtx(), windowID);
		if (userDef != null && !Util.isEmpty(userDef.getName())) {
			windowName = userDef.getName();
		} else {
			windowName = win.get_Translation("Name");
		}
		
		MTable table = MTable.get(Env.getCtx(), getAd_Table_ID());
		PO po = table.getPO(getRecord_id(), null);

		String titleLogic = win.getTitleLogic();
		StringBuilder recordIdentifier = new StringBuilder("");

		if (! Util.isEmpty(titleLogic)) { // default way
			titleLogic = Env.parseVariable(titleLogic, po, null, false);
			if (! Util.isEmpty(titleLogic))
				recordIdentifier.append(" ").append(titleLogic);
		}
		
		if (recordIdentifier.length() == 0) {
			if (po.get_ColumnIndex("DocumentNo") > 0)
				recordIdentifier.append(" ").append(po.get_ValueAsString("DocumentNo"));
			if (po.get_ColumnIndex("Value") > 0)
				recordIdentifier.append(" ").append(po.get_ValueAsString("Value"));
			if (po.get_ColumnIndex("Name") > 0)
				recordIdentifier.append(" ").append(po.get_ValueAsString("Name"));
			if (recordIdentifier.length() == 0)
				recordIdentifier.append(" ").append(po.toString());
			if (recordIdentifier.length() == 0)
				recordIdentifier.append(" [").append(po.get_ID()).append("]");
			if (recordIdentifier.length() == 0)
				recordIdentifier.append(" [no identifier]");
		}
		return windowName + ": " + recordIdentifier.substring(1);
	}
}
