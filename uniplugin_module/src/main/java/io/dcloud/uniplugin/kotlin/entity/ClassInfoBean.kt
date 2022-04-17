package io.dcloud.uniplugin.kotlin.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * @Package: io.dcloud.uniplugin.kotlin.entity
 * @ClassName: ClassInfoBean
 * @Author: LiuTao
 * @CreateDate: 2022/4/15 22:51
 */
@Parcelize
data class ClassInfoBean(
    var id: Int = 0,
    var address: String = ""
): Parcelable