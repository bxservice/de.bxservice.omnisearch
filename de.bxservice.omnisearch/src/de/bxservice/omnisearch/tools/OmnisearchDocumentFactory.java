package de.bxservice.omnisearch.tools;

import org.adempiere.base.Service;
import org.adempiere.base.ServiceQuery;
import org.adempiere.exceptions.AdempiereException;

public class OmnisearchDocumentFactory extends OmnisearchAbstractFactory {

	@Override
	public OmnisearchIndex getIndex(String indexType) {
		return null;
	}

	@Override
	public OmnisearchDocument getDocument(String documentType) {
		if (documentType == null)
			return null;
		
		ServiceQuery query = new ServiceQuery();
		query.put("documentType", documentType);
		OmnisearchDocument custom = Service.locator().locate(OmnisearchDocument.class, query).getService();			
		if (custom == null)
			throw new AdempiereException("No OmnisearchDocument provider found for documentType " + documentType);
		try {
			return custom.getClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {}
		
		return null;
	}

}
