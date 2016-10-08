package in.zollet.abhilash.maps.API;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abhilash on 7/31/2016.
 */
public class Route {

    private String summary;
    private OverviewPolyline overview_polyline;

    public String getSummary() {
        return summary;
    }


    public void setSummary(String summary) {
        this.summary = summary;
    }
    public OverviewPolyline getOverviewPolyline() {
        return overview_polyline;
    }

    public void setOverviewPolyline(OverviewPolyline overviewPolyline) {
        this.overview_polyline = overviewPolyline;
    }

}
