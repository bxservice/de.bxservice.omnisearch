package org.adempiere.webui.dashboard;

import java.util.HashMap;
import java.util.Map;

import org.adempiere.webui.apps.AEnv;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Div;
import org.zkoss.zul.Html;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Vlayout;

import de.bxservice.tools.TextSearchResult;

public class OmnisearchItemRenderer implements ListitemRenderer<TextSearchResult>, EventListener<Event>  {

	boolean showHeadline = true;
	private Map<Html, TextSearchResult> mapCellColumn = new HashMap<Html, TextSearchResult>();
	
	@Override
	public void render(Listitem item, TextSearchResult result, int index)
			throws Exception {

		Listcell cell = new Listcell();
		Html htmlHeader = new Html();
		if (showHeadline) {

			item.appendChild(cell);
			Vlayout div = new Vlayout();
			StringBuilder divStyle = new StringBuilder();

			divStyle.append("text-align: left; ");
			divStyle.append("overflow:auto");
			div.setStyle(divStyle.toString());

			htmlHeader.setContent("<font color=\"#1a0dab\">" + result.getLabel() + "</font>");
			htmlHeader.addEventListener(Events.ON_CLICK, this);

			div.appendChild(htmlHeader);
			htmlHeader.setSclass("menu-href");

			String htmlText = result.getHtmlHeadline();
			Div content = new Div();
			div.appendChild(content);
			Html htmlHeadline = new Html();
			content.appendChild(htmlHeadline);
			htmlHeadline.setContent(htmlText);
			cell.appendChild(div);

		} else {
			htmlHeader.setContent("<font color=\"#1a0dab\">" + result.getLabel() + "</font>");
			htmlHeader.addEventListener(Events.ON_CLICK, this);

			cell.appendChild(htmlHeader);
			htmlHeader.setSclass("menu-href");
			item.appendChild(cell);
		}
		mapCellColumn.put(htmlHeader, result);

	}

	@Override
	public void onEvent(Event e) throws Exception {
		if (Events.ON_CLICK.equals(e.getName()) && (e.getTarget() instanceof Html)) {
			TextSearchResult row = mapCellColumn.get(e.getTarget());
			zoom(row.getRecord_id(), row.getAd_Table_ID());
		}
	} 

	private void zoom(int recordId, int ad_table_id) {
		AEnv.zoom(ad_table_id, recordId);
	}
}
