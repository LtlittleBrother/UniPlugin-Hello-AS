package io.dcloud.uniplugin.kotlin.adapter

import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.android.material.imageview.ShapeableImageView
import io.dcloud.uniplugin.kotlin.entity.PhotoBean
import uni.dcloud.io.uniplugin_module.R
import java.io.File

/**
 * @Package: io.dcloud.uniplugin.kotlin.adapter
 * @ClassName: MainPhotoAdapter
 * @Author: LiuTao
 * @CreateDate: 2022/4/14 22:06
 */
class MainPhotoAdapter :
    BaseQuickAdapter<PhotoBean, BaseViewHolder>(R.layout.main_photos_item) {

    init {
        addChildClickViewIds(R.id.mDelIv)
    }

    override fun convert(holder: BaseViewHolder, item: PhotoBean) {
        val imageView = holder.getView<ShapeableImageView>(R.id.mPhotoIv)
        if (item.type == -1){
            imageView.setImageResource(item.drawableId)
            holder.setVisible(R.id.mDelIv,false)
        }else{
            holder.setVisible(R.id.mDelIv,true)
            Glide.with(context).load(File(item.path)).into(imageView)
        }
    }
}