package io.dcloud.uniplugin.kotlin.popup

import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import io.dcloud.uniplugin.kotlin.adapter.SelectedTressPopupAdapter
import io.dcloud.uniplugin.kotlin.entity.TressTypeBean
import io.dcloud.uniplugin.razerdp.basepopup.BasePopupWindow
import uni.dcloud.io.uniplugin_module.R

class SelectedTressPopup(private val activity: FragmentActivity) : BasePopupWindow(activity),
    LifecycleOwner,OnItemClickListener {

    private var mListener: OnSelectedTressClickListener? = null

    fun setOnSelectedTressClickListener(l: OnSelectedTressClickListener){
        this.mListener = l
    }

    private val mAdapter by lazy {
        SelectedTressPopupAdapter()
    }

    init {
        setContentView(R.layout.main_selected_tress_popup_layout)
    }

    override fun getLifecycle(): Lifecycle {
        return activity.lifecycle
    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        bindLifecycleOwner(this)
        mAdapter.setOnItemClickListener(this)
        val recyclerView = contentView.findViewById<RecyclerView>(R.id.mRecyclerView)
        recyclerView.adapter = mAdapter
        initData()
    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        mAdapter.getItemOrNull(position)?.apply {
            mListener?.selectedItem(item = this)
            dismiss()
        }
    }

    private fun initData() {
        mAdapter.data.clear()
        context.resources.getStringArray(R.array.tress_type_title_array)
            .forEachIndexed { index, title ->
                mAdapter.addData(TressTypeBean(index, title))
            }
    }

    interface OnSelectedTressClickListener{
        fun selectedItem(item: TressTypeBean)
    }

}