/**
 * Copyright (C) 2010 Rednus Limited http://www.rednus.co.uk
 * 
 * #TODO License
 */
package rednus.gncandroid;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import rednus.gncandroid.GNCDataHandler.DataCollection;
import android.util.Log;

/**
 * This class implements DOM to read data file and fill in the data collection
 * in data handler
 * 
 * @author shyam.avvari
 * 
 */
public class DomDataParser extends AbstractParser {
	private static final String	TAG	= "DomDataParser";	// TAG for this activity
	private Document						dom;
	private Element							root;
	/**
	 * Capture stream and data references
	 * 
	 * @param inStream
	 * @param data
	 */
	public DomDataParser(InputStream inStream, DataCollection data) {
		// call super constructor
		super(inStream, data);
	}
	/**
	 * Create DOM Parser and read data file. Once document is received - #TODO
	 * 
	 * @see rednus.gncandroid.AbstractParser#parse()
	 */
	public void parse() {
		// start building document
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			dom = builder.parse(inStream);
			root = dom.getDocumentElement();
		} catch (Exception e) {
			Log.i(TAG, "Error Parsing data...");
			throw new RuntimeException(e);
		}
		Log.i(TAG, "Got root - " + root.getNodeName());
	}
}
