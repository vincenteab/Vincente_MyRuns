package com.example.vincente_buenaventura_myruns2
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey



@Entity(tableName = "history_table")
data class HistoryEntry (
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = "input_type_column")
    var inputType: Int = 0,

    @ColumnInfo(name = "activity_type_column")
    var activityType: Int = 0,

    @ColumnInfo(name = "date_time_column")
    var dateTime: String = "",

    @ColumnInfo(name = "duration_column")
    var duration: Double = 0.0,

    @ColumnInfo(name = "distance_column")
    var distance: Double = 0.0,

    @ColumnInfo(name = "calories_column")
    var calories: Double = 0.0,

    @ColumnInfo(name = "heartrate_column")
    var heartRate: Double = 0.0,

    @ColumnInfo(name = "comment_column")
    var comment: String = "",

    @ColumnInfo(name = "units_column")
    var units: String = "",

    @ColumnInfo(name = "avg_pace_column")
    var avgPace: Double = 0.0,

    @ColumnInfo(name = "avg_speed_column")
    var avgSpeed: Double = 0.0,

    @ColumnInfo(name = "climb_column")
    var climb: Double = 0.0,

    @ColumnInfo(name = "coordinates_column", typeAffinity = ColumnInfo.BLOB)
    var coordinates: ByteArray? = null

)