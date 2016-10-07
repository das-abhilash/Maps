package in.zollet.abhilash.maps.data;



import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;


@ContentProvider(authority = LocationProvider.AUTHORITY, database = LocationDatabase.class)
public class LocationProvider {

    public static final String AUTHORITY = "in.zollet.abhilash.maps";

    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    interface Path {
        String Location= "location";
    }

    private static Uri buildUri(String... paths) {
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for (String path : paths) {
            builder.appendPath(path);
        }
        return builder.build();
    }

    @TableEndpoint(table = LocationDatabase.LOCATION)
    public static class Location {
        @ContentUri(
                path = Path.Location,
                type = "vnd.android.cursor.dir/news"
        )
        public static final Uri CONTENT_URI = buildUri(Path.Location);

        @InexactContentUri(
                name = "NEWS_ID",
                path = Path.Location + "/*",
                type = "vnd.android.cursor.item/news",
                whereColumn = LocationColumns._ID,
                pathSegment = 1
        )
        public static Uri ID(String ID) {
            return buildUri(Path.Location, ID);
        }
    }
}

