package com.contentwatch.Ektorp;

import org.codehaus.jackson.annotate.*;
import org.ektorp.support.CouchDbDocument;

@JsonWriteNullProperties(false)
public class Sofa extends CouchDbDocument {
//        private String id;
//        private String revision;
        private String color;

/*        
        @JsonProperty("_id")
        public String getId() {
                return id;
        }

        @JsonProperty("_id")
        public void setId(String s) {
                id = s;
        }

        @JsonProperty("_rev")
        public String getRevision() {
                return revision;
        }

        @JsonProperty("_rev")
        public void setRevision(String s) {
                revision = s;
        }
*/
        
        public void setColor(String s) {
                color = s;
        }
        
        public String getColor() {
                return color;
        }
}