package io.dcloud.uniplugin.kotlin


import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
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
import com.blankj.utilcode.util.LogUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.huantansheng.easyphotos.EasyPhotos
import com.huantansheng.easyphotos.callback.SelectCallback
import com.huantansheng.easyphotos.models.album.entity.Photo
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.bindgen.Expected
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.extension.localization.localizeLabels
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.addLayerBelow
import com.mapbox.maps.extension.style.layers.generated.FillLayer
import com.mapbox.maps.extension.style.layers.properties.generated.Visibility
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPolygonAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.data.OverviewViewportStateOptions
import com.mapbox.maps.plugin.viewport.state.OverviewViewportState
import com.mapbox.maps.plugin.viewport.viewport
import io.dcloud.uniplugin.kotlin.activity.SelectedClassActivity
import io.dcloud.uniplugin.kotlin.adapter.MainPhotoAdapter
import io.dcloud.uniplugin.kotlin.entity.ClassInfoBean
import io.dcloud.uniplugin.kotlin.entity.PhotoBean
import io.dcloud.uniplugin.kotlin.entity.TressTypeBean
import io.dcloud.uniplugin.kotlin.popup.SelectedTressPopup
import kotlinx.android.synthetic.main.activity_main.*
import uni.dcloud.io.uniplugin_module.R
import java.lang.ref.WeakReference
import java.net.URISyntaxException
import java.util.*
import kotlin.collections.ArrayList


const val SELECTED_CLASS_INFO_CODE = 1*11

class MainActivity : FragmentActivity(), View.OnClickListener,
    SelectedTressPopup.OnSelectedTressClickListener, OnItemClickListener,
    OnItemChildClickListener {


    private lateinit var launchActivity: ActivityResultLauncher<Intent>

    private val mAdapter by lazy {
        MainPhotoAdapter()
    }

    lateinit var mapboxMap: MapboxMap
    lateinit var permissionsManager: PermissionsManager
    var locationEngine: LocationEngine? = null
    private val callback = LocationListeningCallback(this)
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

    var backgroundFillLayerLayer: FillLayer? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
        mapView.setOnTouchListener { v, event ->
            LogUtils.d("liutao","setOnTouchListener")
            if (v.id == R.id.mapView) {
                mapView.parent.requestDisallowInterceptTouchEvent(true)
                return@setOnTouchListener true
            }
            return@setOnTouchListener super.onTouchEvent(event)
        }
        initMap()
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
                try {
                    launchActivity.launch(Intent(this, SelectedClassActivity::class.java))
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        if (mAdapter.itemCount >= 9) {
            Toast.makeText(this, "??????????????????9?????????", LENGTH_SHORT).show()
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

    private fun getTime(startTime: Long): Long {
        return (System.currentTimeMillis() - startTime) / 1000
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


    private fun initMap() {

        mapboxMap = mapView.getMapboxMap()
        mapboxMap.loadStyleUri(Style.MAPBOX_STREETS)
        mapboxMap.loadStyleUri(Style.MAPBOX_STREETS){ style ->
            mapView?.location?.updateSettings {}
            for (i in nameList.indices) {
                val jsonBean = JsonBean()
                jsonBean.backgroundSourceId = "backgroundSourceId$i"
                jsonBean.backgroundLayerId = "backgroundLayerId$i"
                jsonBean.layerId = "layerId$i"
                jsonBean.sourceId = "sourceId$i"
                jsonBean.path = "asset://json/${nameList[i]}"
                jsonList.add(jsonBean)
                layerList.add(jsonBean.layerId)
            }
            for (jsonBean in jsonList) {
                initSource(style, jsonBean)
                initLayers(style, jsonBean)
            }
        }

        mapboxMap.getStyle() {
            it.localizeLabels(Locale.CHINESE)
        }
        mapboxMap.setCamera(
            CameraOptions.Builder()
//                .center(Point.fromLngLat(124.86345178, 42.21473639))
//                .center(Point.fromLngLat(-121.37881303595779, 38.39168793390614))
//                .center(Point.fromLngLat(124.59603993976417,41.90442556733382))//100
//                .center(Point.fromLngLat(124.32302361247464,40.71431407251384))//70
                .center(Point.fromLngLat(124.71620446581129, 42.23236689082264))//50
//                .center(Point.fromLngLat(120.44147073170312,41.47509322174478))//28
//                .center(Point.fromLngLat(120.44147073170312,41.47509322174478))
                .zoom(15.0)
                .build()
        )
        mapboxMap.addOnMapClickListener(object : OnMapClickListener {
            override fun onMapClick(point: Point): Boolean {
                mapboxMap.getStyle() {
                    val pixel = mapboxMap.pixelForCoordinate(point)
                    mapboxMap.queryRenderedFeatures(
                        RenderedQueryGeometry(screenBoxFromPixel(pixel)),
                        RenderedQueryOptions(layerList, null)
                    ) { expected: Expected<String, MutableList<QueriedFeature>> ->
                        if (expected.value!!.isNotEmpty() && markerSelected) {
                            return@queryRenderedFeatures
                        }
                        val queriedFeatures = expected.value!!
                        for (jsonBean in jsonList) {
                            it.getSourceAs<GeoJsonSource>(jsonBean.backgroundSourceId)?.apply {
                                if (queriedFeatures.size > 0) {
                                    feature(queriedFeatures[0].feature)
                        //                                Toast.makeText(this@MainActivity, queriedFeatures[0].feature.properties()?.get("xb_code").toString(), Toast.LENGTH_SHORT).show()
                                    backgroundFillLayerLayer?.visibility(Visibility.VISIBLE)
                                } else {
                        //                                backgroundFillLayerLayer?.visibility(Visibility.NONE)
                                }
                            }
                        }


                    }
                }
                return true
            }
        })
    }

    /**
     * Set up the line layer source
     */
    fun initSource(loadedMapStyle: Style, jsonBean: JsonBean) {
        try {
//            loadedMapStyle.addSource(GeoJsonSource.Builder("source-id").url("asset://210423105(1).geojson").build())//100
//            loadedMapStyle.addSource(GeoJsonSource.Builder("source-id").url("asset://210682111(1).geojson").build())//71
            loadedMapStyle.addSource(
                GeoJsonSource.Builder(jsonBean.sourceId).url(jsonBean.path).build()
            )//51
//            loadedMapStyle.addSource(GeoJsonSource.Builder("source-id2").url("asset://211321109(1).geojson").build())//28
        } catch (exception: URISyntaxException) {
            exception.printStackTrace()
        }
        loadedMapStyle.addSource(GeoJsonSource.Builder(jsonBean.backgroundSourceId).build())
//        loadedMapStyle.addSource(GeoJsonSource.Builder("background-geojson-source-id2").build())
    }


    fun initLayers(loadedMapStyle: Style, jsonBean: JsonBean) {

        backgroundFillLayerLayer =
            FillLayer(jsonBean.backgroundLayerId, jsonBean.backgroundSourceId)
        backgroundFillLayerLayer?.fillColor(Color.RED)
        backgroundFillLayerLayer?.fillOpacity(0.4)
        backgroundFillLayerLayer?.visibility(Visibility.NONE)
        loadedMapStyle.addLayer(backgroundFillLayerLayer!!)

        val fillLayer = FillLayer(jsonBean.layerId, jsonBean.sourceId)
        fillLayer.fillColor(Color.BLACK)
        fillLayer.fillOpacity(0.4)
        loadedMapStyle.addLayerBelow(fillLayer, jsonBean.backgroundLayerId)

    }

    private fun screenBoxFromPixel(pixel: ScreenCoordinate) = ScreenBox(
        ScreenCoordinate(pixel.x - 25.0, pixel.y - 25.0),
        ScreenCoordinate(pixel.x + 25.0, pixel.y + 25.0)
    )

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }


    @OptIn(MapboxExperimental::class)
    fun drawView(result: LocationEngineResult) {
        val latitude = result.lastLocation?.latitude
        val longitude = result.lastLocation?.longitude
        // Create an instance of the Annotation API and get the polygon manager.
        val annotationApi = mapView?.annotations
        val polygonAnnotationManager = annotationApi?.createPolygonAnnotationManager(mapView!!)
        longitude?.also {
            // Define a list of geographic coordinates to be connected.
            val points = listOf(
                listOf(
                    Point.fromLngLat(it.minus(0.0005), it.minus(0.0005)),
                    Point.fromLngLat(it.minus(0.0005), it.plus(0.0005)),
                    Point.fromLngLat(it.plus(0.0005), it.plus(0.0005)),
                    Point.fromLngLat(it.plus(0.0015), it.plus(0.0015)),
                    Point.fromLngLat(it.plus(0.0005), it.minus(0.0005)),
                    Point.fromLngLat(it.minus(0.0005), it.minus(0.0005))
                )
            )
// Set options for the resulting fill layer.
            val polygonAnnotationOptions: PolygonAnnotationOptions = PolygonAnnotationOptions()
                .withPoints(points)
                // Style the polygon that will be added to the map.
                .withFillColor("#ee4e8b")
                .withFillOpacity(0.4)
// Add the resulting polygon to the map.
            polygonAnnotationManager?.create(polygonAnnotationOptions)

            val viewportPlugin = mapView?.viewport
            viewportPlugin?.also { plugin ->
                val overviewViewportState: OverviewViewportState =
                    plugin.makeOverviewViewportState(
                        OverviewViewportStateOptions.Builder()
                            .geometry(Point.fromLngLat(124.51698126, 42.23875025))
                            .padding(EdgeInsets(200.0, 200.0, 200.0, 200.0))
                            .build()
                    )
                val immediateTransition = plugin.makeImmediateViewportTransition()
                plugin.transitionTo(overviewViewportState, immediateTransition) { success ->
                    // the transition has been completed with a flag indicating whether the transition succeeded
                }
            }
        }
    }

    var isLocation = true

    inner class LocationListeningCallback internal constructor(activity: MainActivity) :
        LocationEngineCallback<LocationEngineResult> {

        private val activityWeakReference = WeakReference(activity)

        override fun onSuccess(result: LocationEngineResult) {

// The LocationEngineCallback interface's method which fires when the device's location has changed.
            if (isLocation) {
                isLocation = false
                result.lastLocation
                drawView(result)
            }
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location can not be captured
         *
         * @param exception the exception message
         */
        override fun onFailure(exception: Exception) {
            // The LocationEngineCallback interface's method which fires   when the device's location can not be captured
        }
    }

}