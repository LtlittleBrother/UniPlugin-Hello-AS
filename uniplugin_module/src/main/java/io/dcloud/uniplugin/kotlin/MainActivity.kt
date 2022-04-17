package io.dcloud.uniplugin.kotlin


import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.blankj.utilcode.constant.TimeConstants
import com.blankj.utilcode.util.TimeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.github.gzuliyujiang.wheelpicker.DatePicker
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.utils.ToastUtils
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
import java.lang.ref.WeakReference
import java.net.URISyntaxException
import java.util.*
import uni.dcloud.io.uniplugin_module.R
const val SELECTED_CLASS_INFO_CODE = 1
class MainActivity : FragmentActivity(), View.OnClickListener,
    SelectedTressPopup.OnSelectedTressClickListener, OnItemClickListener,
    OnItemChildClickListener {


    private lateinit var launchActivity: ActivityResultLauncher<Intent>

    private val mAdapter by lazy {
        MainPhotoAdapter()
    }

    var mapboxMap: MapboxMap? = null
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        launchActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if (it.resultCode == SELECTED_CLASS_INFO_CODE){
                val data = it.data?.extras?.getParcelable<ClassInfoBean>("class_info")
                Log.d("liutao","address == ${data?.address}")
            }
        }
        mFindSuspicionTv.setOnClickListener(this)
        mSelectedTypeTv.setOnClickListener(this)
        mNormalTv.setOnClickListener(this)
        mPieceDistributionTv.setOnClickListener(this)
        mPointDistributionTv.setOnClickListener(this)
        mSelectedTimeTv.setOnClickListener(this)
        mClassInfo.setOnClickListener(this)
        mDeadTreeSwitch.setOnCheckedChangeListener { _, isChecked ->
            mDeadTreeSwitch.text = if (isChecked) {
                "是"
            } else {
                "否"
            }
        }
        mNearbySwitch.setOnCheckedChangeListener { _, isChecked ->
            mNearbySwitch.text = if (isChecked) {
                "是"
            } else {
                "否"
            }
        }
        mAdapter.setOnItemChildClickListener(this)
        mAdapter.setOnItemClickListener(this)
        mRecyclerView.adapter = mAdapter
        addEmptyData()
        initMap()
    }

    var backgroundFillLayerLayer: FillLayer? = null

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
            R.id.mSelectedTimeTv -> {
                selectedTime()
            }
            R.id.mClassInfo -> {
                launchActivity.launch(Intent(this, SelectedClassActivity::class.java))
            }
        }
    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        if (mAdapter.itemCount >= 9) {
            ToastUtils.showToast(this, "最多只能选择9张图片")
            return
        }
        selectedImage()
    }

    override fun onItemChildClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        if (view.id == R.id.mDelIv) {
            mAdapter.getItemOrNull(position)?.apply {
                mAdapter.remove(this)
                if (mAdapter.itemCount == 0) {
                    addEmptyData()
                }
            }
        }

    }

    private fun addEmptyData() {
        mAdapter.addData(PhotoBean(drawableId = R.drawable.common_add_photo_icon))
    }

    override fun selectedItem(item: TressTypeBean) {
        mSelectedTypeTv.text = item.title
    }

    private fun selectedTime() {
        val datePicker = DatePicker(this)
        datePicker.wheelLayout.setResetWhenLinkage(false)
        datePicker.setOnDatePickedListener { year, month, day ->
            val time = year.toString().plus("-").plus(month).plus("-").plus(day)
            mSelectedTimeTv.text = time
            val diffDay = TimeUtils.getTimeSpanByNow(
                time,
                TimeUtils.getSafeDateFormat("yyyy-MM-dd"),
                TimeConstants.DAY
            )
            mDayTv.text = diffDay.toString().plus("天")
        }
        datePicker.show()
    }

    private fun selectedImage() {
        XXPermissions.with(this)
            .permission(Permission.WRITE_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE)
            .request { _, all ->
                val maxSelectedNum = if (mAdapter.getItem(0).type == -1) {
                    9
                } else {
                    9 - mAdapter.itemCount
                }
                if (all) {
                    PictureSelector
                        .create(this)
                        .openGallery(SelectMimeType.ofImage())
                        .setImageEngine(GlideEngine.createGlideEngine())
                        .setMaxSelectNum(maxSelectedNum)
                        .forResult(object : OnResultCallbackListener<LocalMedia?> {
                            override fun onResult(result: ArrayList<LocalMedia?>) {
                                if (mAdapter.getItem(0).type == -1) {
                                    mAdapter.removeAt(0)
                                }
                                result.forEach {
                                    mAdapter.addData(PhotoBean(1, it?.path ?: ""))
                                }
                            }

                            override fun onCancel() {

                            }
                        })
                }
            }
    }

    private fun initMap() {

        mapView.getMapboxMap()?.loadStyleUri(Style.MAPBOX_STREETS)
        mapboxMap = mapView!!.getMapboxMap()
        mapboxMap?.loadStyleUri(
            Style.MAPBOX_STREETS,
            // After the style is loaded, initialize the Location component.
            object : Style.OnStyleLoaded {
                override fun onStyleLoaded(style: Style) {
                    mapView?.location?.updateSettings {
                    }
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
            }
        )

        mapboxMap?.getStyle() {
            it.localizeLabels(Locale.CHINESE)
        }
        mapboxMap?.setCamera(
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
        mapboxMap?.addOnMapClickListener(object : OnMapClickListener {
            override fun onMapClick(point: Point): Boolean {
                mapboxMap?.getStyle() {
                    val pixel = mapboxMap?.pixelForCoordinate(point)
                    mapboxMap?.queryRenderedFeatures(
                        RenderedQueryGeometry(screenBoxFromPixel(pixel!!)),
                        RenderedQueryOptions(layerList, null)
                    ) { expected: Expected<String, MutableList<QueriedFeature>> ->
                        if (expected.value!!.isNotEmpty() && markerSelected) {
                            return@queryRenderedFeatures
                        }
                        val queriedFeatures = expected.value!!
                        for (jsonBean in jsonList) {
                            it.getSourceAs<GeoJsonSource>(jsonBean.backgroundSourceId)!!.apply {
                                if (queriedFeatures.size > 0) {
                                    feature(queriedFeatures[0].feature!!)
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

        var fillLayer = FillLayer(jsonBean.layerId, jsonBean.sourceId)
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
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }


    fun drowView(result: LocationEngineResult) {
        var latitude = result.lastLocation?.latitude
        var longitude = result.lastLocation?.longitude
        // Create an instance of the Annotation API and get the polygon manager.
        val annotationApi = mapView?.annotations
        val polygonAnnotationManager = annotationApi?.createPolygonAnnotationManager(mapView!!)
// Define a list of geographic coordinates to be connected.
        val points = listOf(
            listOf(
                Point.fromLngLat(longitude?.minus(0.0005)!!, latitude?.minus(0.0005)!!),
                Point.fromLngLat(longitude?.minus(0.0005)!!, latitude?.plus(0.0005)!!),
                Point.fromLngLat(longitude?.plus(0.0005)!!, latitude?.plus(0.0005)!!),
                Point.fromLngLat(longitude?.plus(0.0015)!!, latitude?.plus(0.0015)!!),
                Point.fromLngLat(longitude?.plus(0.0005)!!, latitude?.minus(0.0005)!!),
                Point.fromLngLat(longitude?.minus(0.0005)!!, latitude?.minus(0.0005)!!)
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
        val overviewViewportState: OverviewViewportState =
            viewportPlugin!!.makeOverviewViewportState(
                OverviewViewportStateOptions.Builder()
                    .geometry(Point.fromLngLat(124.51698126, 42.23875025))
                    .padding(EdgeInsets(200.0, 200.0, 200.0, 200.0))
                    .build()
            )
        val immediateTransition = viewportPlugin?.makeImmediateViewportTransition()
        viewportPlugin?.transitionTo(overviewViewportState, immediateTransition) { success ->
            // the transition has been completed with a flag indicating whether the transition succeeded
        }
    }

    var isLocation = true

    inner class LocationListeningCallback internal constructor(activity: MainActivity) :
        LocationEngineCallback<LocationEngineResult> {

        private val activityWeakReference: WeakReference<MainActivity>

        init {
            this.activityWeakReference = WeakReference(activity)
        }

        override fun onSuccess(result: LocationEngineResult) {

// The LocationEngineCallback interface's method which fires when the device's location has changed.
            if (isLocation) {
                isLocation = false
                result.getLastLocation()
                drowView(result)
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