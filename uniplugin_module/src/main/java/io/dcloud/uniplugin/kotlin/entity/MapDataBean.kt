package io.dcloud.uniplugin.kotlin.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * @Package: io.dcloud.uniplugin.kotlin.entity
 * @ClassName: MapDataBean
 * @Author: LiuTao
 * @CreateDate: 2022/4/20 21:45
 */
@Parcelize
data class MapDataBean(
//    var type: String = "",
    var features: ArrayList<DataFeatures> = arrayListOf()
): Parcelable

@Parcelize
data class DataFeatures(
    var geometry: Geometry? = null,
//    var id: String = "",
//    var geometry_name: String = "",
//    var type: String = "",
//    var properties: Properties? = null
): Parcelable

@Parcelize
data class Geometry(
    var coordinates : ArrayList<ArrayList<ArrayList<Double>>> = arrayListOf(),
//    var id: Int = 0
): Parcelable

@Parcelize
data class Properties(
    var xb_code: String = "",
    var id: String = "",
    var division_code: String = ""
): Parcelable
