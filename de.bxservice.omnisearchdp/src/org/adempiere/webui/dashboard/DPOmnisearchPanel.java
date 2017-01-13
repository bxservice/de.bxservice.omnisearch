package org.adempiere.webui.dashboard;

import java.util.ArrayList;

import org.adempiere.webui.component.Checkbox;
import org.adempiere.webui.component.Textbox;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.Listbox;
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
	private Label   noResultsLabel = null;
		
	public DPOmnisearchPanel()
	{
		super();
		
		this.setSclass("dashboard-widget-max");
		this.setHeight("500px");
		
		initLayout();
		initComponent();

	}
	
	void setModel(ArrayList<TextSearchResult> data) {
		resultListbox.setModel(new ListModelArray<>(data));
	}

	private void initComponent() {
		
		searchTextbox.addEventListener(Events.ON_OK, this);
		searchTextbox.setHflex("1");
		searchTextbox.setSclass("z-textbox");
		
		cbAdvancedSearch.setLabel(Msg.getMsg(Env.getCtx(), "BXS_AdvancedQuery"));
		Label label = new Label();
		
		if (!searchDocument.isValidDocument()) {
			label.setValue(Msg.getMsg(Env.getCtx(), "BXS_NoIndex"));
			searchTextbox.setReadonly(true);
		} else {
			resultListbox = new Listbox();
			resultListbox.setMold("paging");
			resultListbox.setPageSize(10);
			resultListbox.setVflex("1");
			resultListbox.setHflex("1");
			resultListbox.addEventListener("onPaging", this);
			
			noResultsLabel = new Label();
			noResultsLabel.setValue(Msg.getMsg(Env.getCtx(), "FindZeroRecords"));
			noResultsLabel.setVisible(false);
		}

		Vbox box = new Vbox();
		box.setVflex("1");
		box.setHflex("1");
		box.setStyle("margin:5px 5px;");
		box.appendChild(searchTextbox);
		box.appendChild(cbAdvancedSearch);
		if (resultListbox != null) {
			box.appendChild(resultListbox);
			box.appendChild(noResultsLabel);
		}
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
	
	public void showResults(boolean show) {
		if (noResultsLabel != null && resultListbox != null) {
			resultListbox.setVisible(show);
			noResultsLabel.setVisible(!show);
		}
	}

	@Override
	public void onEvent(Event e) throws Exception {
		if (Events.ON_OK.equals(e.getName()) && e.getTarget() instanceof Textbox) {
			Textbox textbox = (Textbox) e.getTarget();
			
			if (resultListbox.getItems() != null) {
				setModel(new ArrayList<TextSearchResult>());
			}
			
			results = searchDocument.performQuery(textbox.getText(), cbAdvancedSearch.isChecked());

			if (results != null && results.size() > 0) {
				
				showResults(true);
				setModel(results);
				renderer = new OmnisearchItemRenderer();
				resultListbox.setItemRenderer(renderer);
		
			} else {
				showResults(false);
			}
		} else if ("onPaging".equals(e.getName()) && (e.getTarget() instanceof Listbox)) {
			PagingEvent ee = (PagingEvent) e;
			int pgno = ee.getActivePage();
			
			if (pgno != 0 && results != null) {
	            int start = pgno * 10;
	            int end = (pgno*10) + 10;
	            
	            if (end > results.size()) 
	            	end = results.size();
				
	            for(int i = start; i < end; i++)
					searchDocument.setHeadline(results.get(i), searchTextbox.getText());
	            
				setModel(results);
			}	
		}
	}

}
