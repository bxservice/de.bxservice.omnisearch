package de.bxservice.tools;

public class OmnisearchDocumentFactory extends OmnisearchAbstractFactory {

	@Override
	public OmnisearchIndex getIndex(String indexType) {
		return null;
	}

	@Override
	public OmnisearchDocument getDocument(String documentType) {
		if (documentType == null)
			return null;
		
		if (documentType.equals(TEXTSEARCH_INDEX)) {
			return new TextSearchDocument();
		}
		//Lucene and others go below
		
		return null;
	}

}
