package io.dcloud.uniplugin.kotlin.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import io.dcloud.uniplugin.kotlin.entity.TressTypeBean
import uni.dcloud.io.uniplugin_module.R

class SelectedTressPopupAdapter:
    BaseQuickAdapter<TressTypeBean,BaseViewHolder>(R.layout.main_selected_tress_popup_item) {

    override fun convert(holder: BaseViewHolder, item: TressTypeBean) {
        holder.setText(R.id.mItemTv,item.title)
    }
}