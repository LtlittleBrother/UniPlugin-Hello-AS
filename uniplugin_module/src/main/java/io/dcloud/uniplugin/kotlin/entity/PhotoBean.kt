package io.dcloud.uniplugin.kotlin.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * @Package: io.dcloud.uniplugin.kotlin.entity
 * @ClassName: PhotoBean
 * @Author: LiuTao
 * @CreateDate: 2022/4/14 22:38
 */
@Parcelize
data class PhotoBean (
    val type: Int = -1,
    var path: String = "",
    var drawableId: Int = 0): Parcelable