package com.raibaz.lupus.game;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.repackaged.org.json.JSONString;

public class AbsJsonable implements JSONString {
	
	private static final Logger log = Logger.getLogger("AbsJsonable");

	@Override
	public String toJSONString() {
		
		log.log(Level.FINE, "jsoning object from class " + this.getClass().getCanonicalName());
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		boolean first = true;
		for(Method m : this.getClass().getDeclaredMethods()) {			
			if(isPropertyGetter(m)) {
				
				log.log(Level.FINE, "Method " + m.getName());
				JsonVisibility anno = m.getAnnotation(JsonVisibility.class);
				if(anno != null && anno.hide()) {
					continue;
				}
				
										
				try {
					Object value = m.invoke(this);
					if(value == null) {
						continue;
					}
					if(first) {
						first = false;
					} else {
						sb.append(",");
					}
					
					String propertyName = m.getName().substring(3);
					propertyName = propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);
					sb.append("\"" + propertyName + "\":");
					if(AbsJsonable.class.isAssignableFrom(m.getReturnType())) {						
						sb.append(((AbsJsonable)m.invoke(this)).toJSONString());
					} else if(Collection.class.isAssignableFrom(m.getReturnType())) {
						sb.append("[");
						boolean collectionFirst = true;
						for(Object o : (Collection)m.invoke(this)) {
							if(collectionFirst) {
								collectionFirst = false;
							} else {
								sb.append(",");
							}
							if(AbsJsonable.class.isAssignableFrom(o.getClass())) {
								sb.append(((AbsJsonable)o).toJSONString());
							} else {
								sb.append("\"" + o + "\"");
							}
						}
						sb.append("]");
					} else {
						sb.append("\"" + m.invoke(this) + "\"");
					}
				} catch(Exception e) {}
				
				log.log(Level.FINE, "currently json = " + sb.toString());
			}
		}
		
		sb.append("}");
		return sb.toString();
	}
	
	public static boolean isPropertyGetter(Method m) {
		return !void.class.equals(m.getReturnType()) &&
		m.getParameterTypes().length == 0 &&
		m.getName().startsWith("get") && 
		!m.getName().equals("getClass");
	}

}
