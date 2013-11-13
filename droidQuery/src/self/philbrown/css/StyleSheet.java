package self.philbrown.css;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;
import org.w3c.css.sac.InputSource;

import android.content.Context;

import com.steadystate.css.parser.CSSOMParser;

public class StyleSheet {

//	/**
//	 * keeps track of styles by type
//	 */
//	private Map<Style.Type, Style> styles;
//	
//	private StyleSheet(){}
//	
//	public static StyleSheet fromAsset(Context context, String assetPath) throws IOException
//	{
//		InputStream in = context.getAssets().open(assetPath);
//		InputSource source = new InputSource();
//		source.setByteStream(in);
//		CSSOMParser parser = new CSSOMParser();
//		parser.setErrorHandler(new ErrorHandler() {
//
//			@Override
//			public void error(CSSParseException arg0) throws CSSException {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void fatalError(CSSParseException arg0) throws CSSException {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void warning(CSSParseException arg0) throws CSSException {
//				// TODO Auto-generated method stub
//				
//			}
//
//			
//		});
//		CSSStyleSheet css = parser.parseStyleSheet(source, null, null);
//		
//		InputStreamReader stream = new InputStreamReader(in);
//		BufferedReader inputReader = new BufferedReader(stream);
//		String line = null;
//		boolean inComment = false;
//		Style.Type currentType = null;
//		
//		
//		
//        while ((line = inputReader.readLine()) != null) 
//        {
//        	line = line.trim();
//        	if (line == null || line.equals("") || line.length() == 0)
//        		continue;
//        	
//        	//handle next char '{' - same line or other...
//        	char firstChar = line.charAt(0);
//        	if (firstChar == '/')
//        	{
//        		//check if it is a comment block.
//        		if (line.charAt(1) == '*')
//        		{
//        			inComment = true;
//        			continue;
//        		}
//        	}
//        	
//        	Style.Type type = Style.getTypeForPrefix(firstChar);
//        	switch(type)//TODO
//        	{
//	        	case CLASS : {
//	        		break;
//	        	}
//	        	case VIEW : {
//	        		break;
//	        	}
//	        	case LABEL : {
//	        		break;
//	        	}
//	        	case SPECIAL : {
//	        		break;
//	        	}
//	        	default : {
//	        		continue;
//	        	}
//        	}
//        }
//		//TODO
//        inputReader.close();
//        stream.close();
//        in.close();
//        
//		return null;
//	}
//	
//	public void keyframes()
//	{
//		
//	}

}
