package org.adempiere.webui.dashboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.component.ListItem;
import org.adempiere.webui.component.Listbox;
import org.adempiere.webui.component.Textbox;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Div;
import org.zkoss.zul.Html;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.Vlayout;

import de.bxservice.omnisearch.TSSearch;
import de.bxservice.omnisearch.TextsearchResult;

public class DPOmnisearchPanel extends DashboardPanel implements EventListener<Event> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8116512057982561129L;
	
	TSSearch ts = new TSSearch();
	
	private Vlayout layout = new Vlayout();
	private Div div = new Div();
	private Textbox tb = new Textbox();
	private Listbox resultListbox = null;
	
	private Map<Html, TextsearchResult> mapCellColumn = new HashMap<Html, TextsearchResult>();
	private boolean showHeadline = true;
	
	public DPOmnisearchPanel()
	{
		super();
		
		this.setSclass("omnisearchpanel-box");
		this.setHeight("220px");
		
		initLayout();
		initComponent();

	}

	private void initComponent() {
		
		tb.addEventListener(Events.ON_OK, this);
		tb.setHflex("1");
		tb.setSclass("z-bandbox-input");
		
		Label label = new Label();
		
		if (!ts.validIndex()) {
			label.setValue("The index does not exist or it is empty");
			tb.setReadonly(true);
		} else {
			resultListbox = new Listbox();
			resultListbox.setMold("paging");
			resultListbox.setPageSize(10);
			resultListbox.setVflex("1");
			resultListbox.setHflex("1");
		}
				
		Vbox box = new Vbox();
		box.setVflex("1");
		box.setHflex("1");
		box.setStyle("margin:5px 5px;");
		box.appendChild(tb);
		if (resultListbox != null)
			box.appendChild(resultListbox);
		else
			box.appendChild(label);
		
		div.appendChild(box);

	}

	private void initLayout() {
		layout.setParent(this);
		layout.setSclass("omnisearchpanel-layout");
		layout.setSpacing("0px");
		layout.setStyle("height: 100%; width: 100%;");
		
		div.setParent(layout);
		div.setVflex("1");
		div.setHflex("1");
		div.setStyle("margin:5px 5px; overflow:auto;");
				
	}

	@Override
	public void onEvent(Event e) throws Exception {
		if (Events.ON_OK.equals(e.getName()) && e.getTarget() instanceof Textbox) {
			Textbox textbox = (Textbox) e.getTarget();
			
			if (resultListbox.getItems() != null)
				resultListbox.getItems().clear();
			mapCellColumn.clear();
			
			ArrayList<TextsearchResult> results = ts.performQuery(textbox.getText());
			
			if (results != null && results.size() > 0) {
				
				ListItem item;
				for (TextsearchResult result : results) {
					item = new ListItem();
					resultListbox.appendChild(item);
					
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
					    
						cell.addEventListener(Events.ON_CLICK, this);
					    cell.appendChild(div);
					    
						mapCellColumn.put(htmlHeader, result);						
					} else {
						htmlHeader.setContent("<font color=\"#1a0dab\">" + result.getLabel() + "</font>");
						htmlHeader.addEventListener(Events.ON_CLICK, this);

						cell.appendChild(htmlHeader);
						htmlHeader.setSclass("menu-href");
						item.appendChild(cell);
						mapCellColumn.put(htmlHeader, result);						
					}
					//mapCellColumn.put(cell, result);						

				}
				
			}
		} else if (Events.ON_CLICK.equals(e.getName()) && (e.getTarget() instanceof Html)) {
			TextsearchResult row = mapCellColumn.get(e.getTarget());
			zoom(row.getRecord_id(), row.getAd_Table_ID());
		}
	}
	
	private void zoom(int recordId, int ad_table_id) {
		AEnv.zoom(ad_table_id, recordId);
	}

}
