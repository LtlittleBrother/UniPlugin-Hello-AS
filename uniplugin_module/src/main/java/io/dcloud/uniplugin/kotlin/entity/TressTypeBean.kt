package io.dcloud.uniplugin.kotlin.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TressTypeBean(
    val type: Int = 0,
    val title: String
): Parcelable