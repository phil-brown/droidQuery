package self.philbrown.droidQuery;

/**
 * File locations used for {@code write} methods.
 * @see $#write(byte[], FileLocation, String, boolean, boolean)
 * @see $#write(byte[], FileLocation, String, boolean, boolean, Function, Function)
 * @author Phil Brown
 */
public enum FileLocation
{
	/** 
	 * File Location constant. Writes files to the private directory
	 * {@code /data/data/<package>}. Internal files cannot specify a file name with separators.
	 */
	INTERNAL,
	/** 
	 * File Location constant. Writes files to the external directory
	 * {@code <external dir>/<package>}
	 */
	EXTERNAL,
	
	/** 
	 * File Location constant. Writes files to the private directory
	 * {@code /data/data/<package>/cache}. Cache files can be deleted by the system when space is
	 * needed.
	 */
	CACHE,
	
	/** 
	 * File Location constant. Writes files to the external directory
	 * {@code <external dir>Android/data/<package>}
	 */
	DATA,
	
	/** 
	 * File Location constant. Writes files to the path given by {@code fileName}.
	 */
	CUSTOM
};
