package jp.wasabeef.richeditor;

import java.io.Serializable;

public class RichEditerContent implements Serializable {     
	   private static final long serialVersionUID = 1L; 

	   private String src;
	    private String width; 
	    private String height;
	    private String alt;
		public String getSrc() {
			return src;
		}
		public void setSrc(String src) {
			this.src = src;
		}
		public String getWidth() {
			return width;
		}
		public void setWidth(String width) {
			this.width = width;
		}
		public String getHeight() {
			return height;
		}
		public void setHeight(String height) {
			this.height = height;
		}
		public String getAlt() {
			return alt;
		}
		public void setAlt(String alt) {
			this.alt = alt;
		}
		@Override
		public String toString() {
			return "RichEditerContent [src: "+src+",  width: "+width
					+", height: "+height+",  alt: "+alt+" ]";
		}
	}

