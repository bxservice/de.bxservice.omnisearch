package org.adempiere.webui.dashboard;

import org.zkoss.zul.Div;
import org.zkoss.zul.Html;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Vlayout;

import de.bxservice.tools.TextSearchResult;

public class OmnisearchItemRenderer implements ListitemRenderer<TextSearchResult> {

	boolean showHeadline = true;

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
			//htmlHeader.addEventListener(Events.ON_CLICK, this);

			div.appendChild(htmlHeader);
			htmlHeader.setSclass("menu-href");

			String htmlText = result.getHtmlHeadline();
			Div content = new Div();
			div.appendChild(content);
			Html htmlHeadline = new Html();
			content.appendChild(htmlHeadline);
			htmlHeadline.setContent(htmlText);
			cell.appendChild(div);

			//mapCellColumn.put(htmlHeader, result);						
		} else {
			htmlHeader.setContent("<font color=\"#1a0dab\">" + result.getLabel() + "</font>");
			//htmlHeader.addEventListener(Events.ON_CLICK, this);

			cell.appendChild(htmlHeader);
			htmlHeader.setSclass("menu-href");
			item.appendChild(cell);
			//mapCellColumn.put(htmlHeader, result);						
		}

	} 

}
