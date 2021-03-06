package us.dot.its.jpo.ode.plugin.j2735.timstorage;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import us.dot.its.jpo.ode.plugin.asn1.Asn1Object;

@JsonPropertyOrder({ "lat", "long", "elevation" })
public class Anchor extends Asn1Object {
   private static final long serialVersionUID = 1L;

   @JsonProperty("lat")
   private String lat;

   @JsonProperty("long")
   private String llong;

   @JsonProperty("elevation")
   private String elevation;

   public String getLat() {
      return lat;
   }

   public void setLat(String lat) {
      this.lat = lat;
   }

   public String getLlong() {
      return llong;
   }

   public void setLlong(String llong) {
      this.llong = llong;
   }

   public String getElevation() {
      return elevation;
   }

   public void setElevation(String elevation) {
      this.elevation = elevation;
   }

}