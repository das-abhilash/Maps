package in.zollet.abhilash.maps.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.OnCreate;
import net.simonvt.schematic.annotation.OnUpgrade;
import net.simonvt.schematic.annotation.Table;


@Database(version = LocationDatabase.VERSION)
public class LocationDatabase {
    private LocationDatabase() {
    }

    public static final int VERSION = 2;

    @Table(LocationColumns.class)
    public static final String LOCATION = "Location";


}
