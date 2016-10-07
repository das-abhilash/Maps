package in.zollet.abhilash.maps.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;


public class LocationColumns {
    @DataType(DataType.Type.INTEGER)
    @AutoIncrement  @PrimaryKey
    public static final String _ID = "_id";

    @DataType(DataType.Type.TEXT)
    public static final String NAME = "name";

    @DataType(DataType.Type.TEXT)  @NotNull
    public static final String LATITUDE = "latitude";

    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String LONGITUDE = "longitude";


}
