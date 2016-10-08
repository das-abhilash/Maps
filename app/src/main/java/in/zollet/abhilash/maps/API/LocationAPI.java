package in.zollet.abhilash.maps.API;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Abhilash on 7/30/2016.
 */
public interface LocationAPI {

    @GET("maps/api/directions/json")
    Call<LocationData> getLocation(
            @Query("origin") String Origin, @Query("destination") String Destination
            , @Query("key") String Key);


}
