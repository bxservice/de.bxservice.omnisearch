package de.bxservice.tools;

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
