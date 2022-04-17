package io.dcloud.uniplugin.kotlin.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import io.dcloud.uniplugin.kotlin.entity.ClassInfoBean
import uni.dcloud.io.uniplugin_module.R

/**
 * @Package: io.dcloud.uniplugin.kotlin.adapter
 * @ClassName: SelectedClassAdapter
 * @Author: LiuTao
 * @CreateDate: 2022/4/15 22:51
 */
class SelectedClassAdapter :
    BaseQuickAdapter<ClassInfoBean, BaseViewHolder>(R.layout.selected_class_item) {

    init {
        addChildClickViewIds(R.id.mSelectedItem)
    }

    override fun convert(holder: BaseViewHolder, item: ClassInfoBean) {
        holder.setText(R.id.mAddressTv,item.address)
        holder.setText(R.id.mNumTv,item.id.toString())
    }
}