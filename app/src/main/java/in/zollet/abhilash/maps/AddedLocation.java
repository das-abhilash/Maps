package in.zollet.abhilash.maps;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Abhilash on 10/7/2016.
 */

public class AddedLocation implements Parcelable {
    private String name;
    private Double lat;
    private Double lon;

    public AddedLocation(String name,Double lat,Double lon){
        this.name = name;
        this.lat = lat;
        this.lon = lon;

    }
    public AddedLocation(Double lat,Double lon){
        this.lat = lat;
        this.lon = lon;

    }

    protected AddedLocation(Parcel in) {
        name = in.readString();
    }

    public static final Creator<AddedLocation> CREATOR = new Creator<AddedLocation>() {
        @Override
        public AddedLocation createFromParcel(Parcel in) {
            return new AddedLocation(in);
        }

        @Override
        public AddedLocation[] newArray(int size) {
            return new AddedLocation[size];
        }
    };

    public Double getLat() {
        return lat;
    }

    public Double getLon() {
        return lon;
    }

    public String getName() {
        return name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
    }
}
