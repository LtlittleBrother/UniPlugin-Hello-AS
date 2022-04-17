package io.dcloud.uniplugin.kotlin.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener
import com.scwang.smart.refresh.layout.listener.OnRefreshListener
import io.dcloud.uniplugin.kotlin.SELECTED_CLASS_INFO_CODE
import io.dcloud.uniplugin.kotlin.adapter.SelectedClassAdapter
import io.dcloud.uniplugin.kotlin.entity.ClassInfoBean
import kotlinx.android.synthetic.main.activity_selected_class_layout.*
import uni.dcloud.io.uniplugin_module.R

class SelectedClassActivity : FragmentActivity(), OnRefreshListener,
    OnLoadMoreListener,OnItemClickListener {

    private val mAdapter by lazy {
        SelectedClassAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selected_class_layout)
        mRefresh.setRefreshHeader(ClassicsHeader(this))
        mRefresh.setRefreshFooter(ClassicsFooter(this))
        mRefresh.setOnRefreshListener(this)
        mRefresh.setOnLoadMoreListener(this)
        mCloseIv.setOnClickListener {
            finish()
        }
        mAdapter.setOnItemClickListener(this)
        mRecyclerView.adapter = mAdapter
        addDate()
    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        mAdapter.getItemOrNull(position)?.apply {
            val intent = Intent()
            val bundle = Bundle()
            bundle.putParcelable("class_info",this)
            intent.putExtras(bundle)
            setResult(SELECTED_CLASS_INFO_CODE,intent)
            finish()
        }
    }

    private fun addDate() {
        val list = arrayListOf<ClassInfoBean>()
        for (i in 0..20) {
            list.add(ClassInfoBean(i, "辽宁省-抚顺市-清苑县-红透山镇-红透山村$i"))
        }
        mAdapter.setList(list)
        mRefresh.finishRefresh()
    }

    private fun loadMoreData() {
        val startIndex = mAdapter.itemCount
        for (index in startIndex..(startIndex + 20)) {
            mAdapter.addData(ClassInfoBean(index, "辽宁省-抚顺市-清苑县-红透山镇-红透山村$index"))
        }
        mRefresh.finishLoadMore()
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        addDate()
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        loadMoreData()
    }

}