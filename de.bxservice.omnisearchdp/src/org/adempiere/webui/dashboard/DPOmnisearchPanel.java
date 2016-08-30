package org.adempiere.webui.dashboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.component.Checkbox;
import org.adempiere.webui.component.Textbox;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Div;
import org.zkoss.zul.Html;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.event.PagingEvent;

import de.bxservice.tools.TextSearchDocument;
import de.bxservice.tools.TextSearchResult;

public class DPOmnisearchPanel extends DashboardPanel implements EventListener<Event> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8116512057982561129L;
	
	
	private TextSearchDocument searchDocument = new TextSearchDocument();
	private ArrayList<TextSearchResult> results;
	
	private OmnisearchItemRenderer renderer;
	private Vlayout layout = new Vlayout();
	private Div div = new Div();
	private Textbox searchTextbox = new Textbox();
	private Checkbox cbAdvancedSearch = new Checkbox();
	private Listbox resultListbox = null;
	
	private Map<Html, TextSearchResult> mapCellColumn = new HashMap<Html, TextSearchResult>();
	private boolean showHeadline = true;
	
	public DPOmnisearchPanel()
	{
		super();
		
		this.setSclass("omnisearchpanel-box");
		this.setHeight("220px");
		
		initLayout();
		initComponent();

	}
	
	void setModel(ArrayList<TextSearchResult> data) {
		resultListbox.setModel(new ListModelArray<>(data));
	}

	private void initComponent() {
		
		searchTextbox.addEventListener(Events.ON_OK, this);
		searchTextbox.setHflex("1");
		searchTextbox.setSclass("z-bandbox-input");
		
		//cbClient.setLabel(Msg.translate(m_ctx, "AD_Client_ID"));
		cbAdvancedSearch.setLabel("Advanced");
		Label label = new Label();
		
		if (!searchDocument.isValidDocument()) {
			label.setValue("The index does not exist or it is empty");
			searchTextbox.setReadonly(true);
		} else {
			resultListbox = new Listbox();
			resultListbox.setMold("paging");
			resultListbox.setPageSize(10);
			resultListbox.setVflex("1");
			resultListbox.setHflex("1");
			resultListbox.addEventListener("onPaging", this);
		}

		Vbox box = new Vbox();
		box.setVflex("1");
		box.setHflex("1");
		box.setStyle("margin:5px 5px;");
		box.appendChild(searchTextbox);
		box.appendChild(cbAdvancedSearch);
		if (resultListbox != null)
			box.appendChild(resultListbox);
		else
			box.appendChild(label);

		div.appendChild(box);

		//  ActionListener
		cbAdvancedSearch.setChecked(false);
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
			
			results = searchDocument.performQuery(textbox.getText(), cbAdvancedSearch.isChecked());

			if (results != null && results.size() > 0) {
				
				setModel(results);
				renderer = new OmnisearchItemRenderer();
				resultListbox.setItemRenderer(renderer);
				
				
				//TODO: Add listeners in renderer
				Listitem item;
				for (TextSearchResult result : results) {
					item = new Listitem();
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
					    cell.appendChild(div);
					    
						//mapCellColumn.put(htmlHeader, result);						
					} else {
						htmlHeader.setContent("<font color=\"#1a0dab\">" + result.getLabel() + "</font>");
						htmlHeader.addEventListener(Events.ON_CLICK, this);

						cell.appendChild(htmlHeader);
						htmlHeader.setSclass("menu-href");
						item.appendChild(cell);
						//mapCellColumn.put(htmlHeader, result);						
					}
					mapCellColumn.put(htmlHeader, result);

				}
				
			}
		} else if (Events.ON_CLICK.equals(e.getName()) && (e.getTarget() instanceof Html)) {
			TextSearchResult row = mapCellColumn.get(e.getTarget());
			zoom(row.getRecord_id(), row.getAd_Table_ID());
		} else if ("onPaging".equals(e.getName()) && (e.getTarget() instanceof Listbox)) {
			PagingEvent ee = (PagingEvent) e;
			int pgno = ee.getActivePage();
			
			if (pgno != 0 && results != null) {
	            int start = pgno * 10;
	            int end = (pgno*10) + 10;
	            
	            if (end > results.size()) 
	            	end = results.size();
				
	            for(int i = start; i < end; i++) {
					searchDocument.setHeadline(results.get(i), searchTextbox.getText());
				}
	            
				setModel(results);

			}	
		}
	}
	
	private void zoom(int recordId, int ad_table_id) {
		AEnv.zoom(ad_table_id, recordId);
	}

}
