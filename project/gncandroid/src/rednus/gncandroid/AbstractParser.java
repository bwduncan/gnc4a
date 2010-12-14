/**
 * Copyright (C) 2010 Rednus Limited http://www.rednus.co.uk
 * 
 * #TODO License
 */
package rednus.gncandroid;
import java.io.InputStream;
import rednus.gncandroid.GNCDataHandler.DataCollection;

/**
 * This class defines methods for parser to read data file and fill in the data
 * collection in data handler
 * 
 * @author shyam.avvari
 * 
 */
public abstract class AbstractParser {
	DataCollection	gncData;
	InputStream			inStream;
	/**
	 * Constructor for parser
	 * 
	 * @param gncData
	 * @param inStream
	 */
	public AbstractParser(InputStream inStream, DataCollection gncData) {
		this.gncData = gncData;
		this.inStream = inStream;
	}
	/**
	 * Parse method
	 * 
	 * This method should be implemented by sub classes
	 */
	public abstract void parse();
}
