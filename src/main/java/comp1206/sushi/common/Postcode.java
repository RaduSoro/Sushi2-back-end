package comp1206.sushi.common;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Postcode extends Model implements Serializable {

	private String name;
	private Map<String,Double> latLong;
	private Number distance;


	public Postcode(String code) {
		this.name = code;
		calculateLatLong(code);
		this.distance = Integer.valueOf(0);
	}

	
	@Override
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Number getDistance() {
		return this.distance;
	}

	public Map<String,Double> getLatLong() {
		return this.latLong;
	}

	public void calculateDistance(Restaurant restaurant) {
		if (restaurant == null) this.distance = 0;
		else {
			//This function needs implementing
			Postcode origin = restaurant.getLocation();
			Double postcodeLat = this.latLong.get("lat");
			Double postcodeLong = this.latLong.get("long");
			Map<String, Double> restaurantMap = readFromUrl(origin.toString());
			Double restaurantLat = restaurantMap.get("lat");
			Double resturantLong = restaurantMap.get("long");
			this.distance = distance(postcodeLat, restaurantLat, postcodeLong, resturantLong);
		}
	}

	protected void calculateLatLong(String string) {
		this.latLong = readFromUrl(string);
	}

	public Map<String, Double> readFromUrl(String postcode) {
		Map<String, Double> latLong = new HashMap<>();
		String lg = "";
		String lt = "";
		try {
			String url = "https://www.southampton.ac.uk/~ob1a12/postcode/postcode.php?postcode=";
			postcode = postcode.replaceAll("\\s+", "");
			URL oracle = new URL(url + postcode);
			BufferedReader in = new BufferedReader(new InputStreamReader(oracle.openStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				lt = inputLine.substring(30, 48);
				lg = inputLine.substring(58, 76);
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		latLong.put("lat", Double.valueOf(lt));
		latLong.put("long", Double.valueOf(lg));
		return latLong;
	}

	/**
	 * @SOURCE https://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude
	 * Calculate distance between two points in latitude and longitude taking
	 * into account height difference. If you are not interested in height
	 * difference pass 0.0. Uses Haversine method as its base.
	 * <p>
	 * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
	 * el2 End altitude in meters
	 * @returns Distance in Meters
	 */
	public double distance(double lat1, double lat2, double lon1,
						   double lon2) {

		final int R = 6371; // Radius of the earth

		double latDistance = Math.toRadians(lat2 - lat1);
		double lonDistance = Math.toRadians(lon2 - lon1);
		double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
				+ Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
				* Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distance = R * c * 1000; // convert to meters


		distance = Math.pow(distance, 2);

		return Math.sqrt(distance);
	}
}

