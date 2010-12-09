/**
 * Copyright (C) 2010 Rednus Limited http://www.rednus.co.uk
 * 
 * Project : GNCAndroid Package : rednus.GNCAndroid File : MainView.java
 * Description :
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
public class DomDataParser {
	// TAG for this activity
	private static final String	TAG	= "DomDataParser";
	DataCollection							gncData;
	InputStream inStream;
	private Document						dom; 
	private Element							root;
	// create parser by sending input stream
	public DomDataParser(InputStream inStream, DataCollection data) {
		// copy data object reference
		gncData = data;
		this.inStream = inStream;
	}
	public void parse(){
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
		//write root name
		
		Log.i(TAG, "Got root - " + root.getNodeName());
	}
}
