package io.dcloud.uniplugin.kotlin


import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.AssetManager
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.alibaba.fastjson.JSON
import com.baidu.mapapi.SDKInitializer
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.MultiPoint
import com.baidu.mapapi.map.MultiPointItem
import com.baidu.mapapi.map.MultiPointOption
import com.baidu.mapapi.model.LatLng
import com.blankj.utilcode.util.GsonUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.huantansheng.easyphotos.EasyPhotos
import com.huantansheng.easyphotos.callback.SelectCallback
import com.huantansheng.easyphotos.models.album.entity.Photo
import io.dcloud.uniplugin.kotlin.activity.SelectedClassActivity
import io.dcloud.uniplugin.kotlin.adapter.MainPhotoAdapter
import io.dcloud.uniplugin.kotlin.entity.ClassInfoBean
import io.dcloud.uniplugin.kotlin.entity.MapDataBean
import io.dcloud.uniplugin.kotlin.entity.PhotoBean
import io.dcloud.uniplugin.kotlin.entity.TressTypeBean
import io.dcloud.uniplugin.kotlin.popup.SelectedTressPopup
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import uni.dcloud.io.uniplugin_module.R
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.CopyOnWriteArrayList


const val SELECTED_CLASS_INFO_CODE = 1

class MainActivity : FragmentActivity(), View.OnClickListener,
    SelectedTressPopup.OnSelectedTressClickListener, OnItemClickListener,
    OnItemChildClickListener {


    private lateinit var launchActivity: ActivityResultLauncher<Intent>

    private val mAdapter by lazy {
        MainPhotoAdapter()
    }

    val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
    val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5
    val TAG = "MainActivity"
    val nameList = mutableListOf(
        "210423109001.json",
        "210423109002.json",
        "210423109003.json",
        "210423109004.json",
        "210423109005.json",
        "210423109006.json",
        "210423109007.json",
        "210423109008.json",
        "210423109009.json",
        "210423109011.json",
        "210423109012.json",
        "210423109013.json",
        "210423109014.json",
        "210423109015.json",
        "210423109016.json",
        "210423109017.json",
        "210423109018.json",
        "210423109019.json",
        "210423109021.json",
        "210423109022.json",
        "210423109023.json",
        "210423109024.json",
        "210423109025.json",
        "210423109026.json",
        "210423109027.json",
        "210423109028.json"
    )
    var jsonList = mutableListOf<JsonBean>()
    var layerList = mutableListOf<String>()

    private var markerSelected = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        // todo 插件时需打开此行代码 不然报错
//        SDKInitializer.setAgreePrivacy(this.applicationContext,true)
        SDKInitializer.initialize(this.applicationContext)
        //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
//        Log.d("liutao","jsonString == ${intent?.getStringExtra("json_string")}")
        setContentView(R.layout.activity_main)
        jsonData()

//        val bitmapA = BitmapDescriptorFactory.fromResource(R.drawable.main_selected_class_icon)
//        val multiPointItems = ArrayList<MultiPointItem>()
//        multiPointItems.add(MultiPointItem(LatLng(42.28327092692213,124.68326451815378)))
//        multiPointItems.add(MultiPointItem(LatLng(39.925,116.454)))
//        multiPointItems.add(MultiPointItem(LatLng(39.955,116.494)))
//        multiPointItems.add(MultiPointItem(LatLng(39.905,116.554)))
//        multiPointItems.add(MultiPointItem(LatLng(39.965,116.604)))
//        val multiPointOption = MultiPointOption()
//        multiPointOption?.multiPointItems = multiPointItems
//        multiPointOption?.icon = bitmapA
//        mMapView?.map?.addOverlay(multiPointOption)
        launchActivity =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == SELECTED_CLASS_INFO_CODE) {
                    val data = it.data?.extras?.getParcelable<ClassInfoBean>("class_info")
                    Log.d("liutao", "address == ${data?.address}")
                }
            }
        mFindSuspicionTv.setOnClickListener(this)
        mSelectedTypeTv.setOnClickListener(this)
        mNormalTv.setOnClickListener(this)
        mPieceDistributionTv.setOnClickListener(this)
        mPointDistributionTv.setOnClickListener(this)
        DeadTreeHasTv.setOnClickListener(this)
        DeadTreeNoTv.setOnClickListener(this)
        mNearbyHasTv.setOnClickListener(this)
        mNearbyNoTv.setOnClickListener(this)
        mClassInfo.setOnClickListener(this)

        setSpanString(mDeadTreeTitleTv)
        setSpanString(mResultTitleTv)
        setSpanString(mTreesTypeTitleTv)
        setSpanString(mDistributionTitleTv)
        setSpanString(mNearbyTitleTv)

        mAdapter.setOnItemChildClickListener(this)
        mAdapter.setOnItemClickListener(this)
        mRecyclerView.adapter = mAdapter
        addEmptyData()
        mMapView.setOnTouchListener { v, event ->
            if (v.id == R.id.mMapView) {
                mMapView.parent.requestDisallowInterceptTouchEvent(true)
                return@setOnTouchListener false
            }
            return@setOnTouchListener super.onTouchEvent(event)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.mFindSuspicionTv -> {
                mFindSuspicionTv.isSelected = true
                mNormalTv.isSelected = false
            }
            R.id.mNormalTv -> {
                mFindSuspicionTv.isSelected = false
                mNormalTv.isSelected = true
            }
            R.id.mNearbyHasTv -> {
                mNearbyHasTv.isSelected = true
                mNearbyNoTv.isSelected = false
            }
            R.id.mNearbyNoTv -> {
                mNearbyHasTv.isSelected = false
                mNearbyNoTv.isSelected = true
            }
            R.id.DeadTreeHasTv -> {
                DeadTreeHasTv.isSelected = true
                DeadTreeNoTv.isSelected = false
            }
            R.id.DeadTreeNoTv -> {
                DeadTreeHasTv.isSelected = false
                DeadTreeNoTv.isSelected = true
            }
            R.id.mSelectedTypeTv -> {
                val popup = SelectedTressPopup(this)
                popup.popupGravity = Gravity.BOTTOM
                popup.setWidthAsAnchorView(true)
                popup.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent))
                popup.setOnSelectedTressClickListener(this)
                popup.showPopupWindow(mSelectedTypeTv)
            }
            R.id.mPieceDistributionTv -> {
                mPieceDistributionTv.isSelected = true
                mPointDistributionTv.isSelected = false
            }
            R.id.mPointDistributionTv -> {
                mPieceDistributionTv.isSelected = false
                mPointDistributionTv.isSelected = true
            }
//            R.id.mSelectedTimeTv -> {
//                selectedTime()
//            }
            R.id.mClassInfo -> {
                launchActivity.launch(Intent(this, SelectedClassActivity::class.java))
            }
        }
    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        if (mAdapter.itemCount >= 9) {
            Toast.makeText(this, "最多只能选择9张图片", LENGTH_SHORT).show()
            return
        }
        selectedImage()
    }

    override fun onItemChildClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        if (view.id == R.id.mDelIv) {
            mAdapter.getItemOrNull(position)?.apply {
                mAdapter.remove(this)
            }
        }
    }

    private fun addEmptyData() {
        mAdapter.addData(PhotoBean(drawableId = R.drawable.common_add_photo_icon))
    }

    override fun selectedItem(item: TressTypeBean) {
        mSelectedTypeTv.text = item.title
    }

    private fun jsonData() {
//        for (i in nameList.indices) {
//            val jsonBean = JsonBean()
//            jsonBean.backgroundSourceId = "backgroundSourceId$i"
//            jsonBean.backgroundLayerId = "backgroundLayerId$i"
//            jsonBean.layerId = "layerId$i"
//            jsonBean.sourceId = "sourceId$i"
//            jsonBean.path = "asset://json/${nameList[i]}"
//            jsonList.add(jsonBean)
//            layerList.add(jsonBean.layerId)
//        }
        val dataList = arrayListOf<MapDataBean>()
        val startTime = System.currentTimeMillis()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                var data: MapDataBean?
                val multiPointItems = CopyOnWriteArrayList<MultiPointItem>()
                var pointItem: MultiPointItem? = null
                var latLng: LatLng? = null
                var multiPointOption: MultiPointOption?
                val bitmapA = BitmapDescriptorFactory.fromResource(R.drawable.main_selected_class_icon)
                nameList.forEach { name ->
                    val stringJson = getJsonString(name) ?: ""
                    data = GsonUtils.fromJson(stringJson, MapDataBean::class.java)
                    data?.features?.forEach { features ->
                        features.geometry?.coordinates?.forEach { coordinates ->
                            coordinates.forEach { child ->
                                Log.d("liutao", "child[0] == ${child[1]}   \n " +
                                        "child[1] == ${child[0]}")
                                latLng = LatLng(child[1],child[0])
                                pointItem = MultiPointItem(latLng)
                                multiPointItems.add(pointItem)
                                latLng = null
                                pointItem = null
                            }
                        }
                    }
                    multiPointOption = MultiPointOption()
                    multiPointOption?.multiPointItems = multiPointItems
                    multiPointOption?.icon = bitmapA
                    withContext(Dispatchers.Main){
                        Log.d("liutao","map == null ${mMapView == null} \n " +
                                "mMapView.map == null == ${mMapView.map == null}")
                        mMapView.map?.addOverlay(multiPointOption)
                    }
                    multiPointItems.clear()
                    multiPointOption = null
                    // 添加海量点覆盖物
                    data = null
                    Log.d("liutao", "用时${getTime(startTime)}秒")
                }
                Log.d("liutao", "总用时 == ${getTime(startTime)} 秒")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun test(multiPointItems: CopyOnWriteArrayList<MultiPointItem>) {
        // 设置海量点数据

    }

    private fun getTime(startTime: Long): Long {
        return (System.currentTimeMillis() - startTime) / 1000
    }

    private fun getJsonString(fileName: String): String? {
        val newstringBuilder = StringBuilder()
        var inputStream: InputStream? = null
        try {
            inputStream = resources.assets.open(fileName)
            val isr = InputStreamReader(inputStream)
            val reader = BufferedReader(isr)
            var jsonLine: String?
            while (reader.readLine().also { jsonLine = it } != null) {
                newstringBuilder.append(jsonLine)
            }
            reader.close()
            isr.close()
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return newstringBuilder.toString()
    }

    private fun parseFile(fileName: String): String {
        val assets: AssetManager = assets
        var stream: InputStream? = null
        var br: BufferedReader? = null
        val builder = StringBuilder()
        try {
            Log.d("liutao", "fileName == $fileName")
            stream = assets.open(fileName)
            br = BufferedReader(InputStreamReader(stream))
            var line = br.readLine()
            while (line != null) {
                builder.append(line)
                line = br.readLine()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                stream?.close()
                br?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return builder.toString()
    }


    private fun setSpanString(textView: TextView) {
        val spanString = SpannableString(textView.text)
        val span = ForegroundColorSpan(ContextCompat.getColor(this, R.color.color_c30525))
        spanString.setSpan(span, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView.text = spanString
    }

    private fun selectedImage() {
        EasyPhotos.createAlbum(
            this, false, true,
            GlideEngine.getInstance()
        )
            .setCount(9)
            .start(object : SelectCallback() {
                override fun onResult(result: ArrayList<Photo>?, p1: Boolean) {
                    result?.forEach {
                        mAdapter.addData(PhotoBean(1, it.path ?: ""))
                    }
                }

                override fun onCancel() {

                }
            })
    }
}