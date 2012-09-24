package com.yammer.dropwizard.jpa;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jetty.util.ConcurrentHashSet;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class PersistenceUnitRepository {

	private Set<String> availablePersistenceUnits = new ConcurrentHashSet<String>();
	
	public PersistenceUnitRepository() {
		this("persistence.xml");
	}

	public PersistenceUnitRepository(String persistenceXml) {
		try {
			loadConfig(persistenceXml);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void loadConfig(String persistenceXml) throws SAXException, IOException, ParserConfigurationException {
		URL url = getClass().getResource("/META-INF/" + persistenceXml);
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openStream());
		Node root = doc.getFirstChild();
		NodeList childNodes = root.getChildNodes();
		for(int i=0; i<childNodes.getLength(); ++i) {
			Node item = childNodes.item(i);
			if(item.hasAttributes()) {
				availablePersistenceUnits.add(item.getAttributes().getNamedItem("name").getNodeValue());
			}
		}
	}
	
	public boolean isPersistenceUnitAvailable(String unitName) {
		return availablePersistenceUnits.contains(unitName);
	}
	
	public boolean hasMultiplePersistenceUnits() {
		return availablePersistenceUnits.size() > 1;
	}

	public String getDefaultPersistenceUnit() {
		if(availablePersistenceUnits.size() == 1) {
			return availablePersistenceUnits.iterator().next();
		}
		return null;
	}
}
