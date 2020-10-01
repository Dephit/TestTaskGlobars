package com.sergeenko.alexey.globars.activities

import android.os.Bundle
import android.os.PersistableBundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.maps.android.ui.IconGenerator
import com.sergeenko.alexey.globars.*
import com.sergeenko.alexey.globars.api.GlobarsApiService
import com.sergeenko.alexey.globars.dataClasses.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.map_layout.*
import kotlinx.android.synthetic.main.marker_view.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.Callback as Callback1


class MapActivity : BaseActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private var menu: Menu? = null
    private val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"
    private var gmap: GoogleMap? = null
    private lateinit var model: MapViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.map_layout)
        setMap(savedInstanceState)
        setMenu()
        setModel()
    }

    private fun setMenu() {
        setSupportActionBar(findViewById(R.id.my_toolbar))
    }

    private fun setModel() {
        model = ViewModelProvider(this).get(MapViewModel::class.java)
        model.retrofit = retrofit
        model.loadSession()
        model.sessionData.observe(this, { it ->
            it?.let { result ->
                showTransport(result)
            } ?: run {
                Toast.makeText(this, "Нет данных", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showTransport(result: TransportObjects) {
        result.data.forEach {objectData->
            if(objectData.checked) {
                menu?.add(objectData.name)?.setOnMenuItemClickListener { _ ->
                    moveCameraToPosition(objectData.position)
                    return@setOnMenuItemClickListener true
                }
                setMarker(objectData)
            }
        }
        moveCameraToPosition(result.data.firstOrNull()?.position)
    }

    private fun moveCameraToPosition(position: Position?) {
        position?.let { pos->
            val ny = LatLng(pos.lt, pos.ln)
            moveCameraToPosition(ny)
        }
    }

    private fun setMarker(it: ObjectData) {
        it.position?.let { pos ->
            val target = LatLng(pos.lt, pos.ln)
            gmap?.addMarker(
                    MarkerOptions()
                        .alpha(if(it.eye) 1f else 0.5f)
                        .position(target)
                        .icon(makersMaker(it.name))
            ).also { marker ->
                setMarker(marker, it)
            }
        }
    }

    private fun setMap(savedInstanceState: Bundle?) {
        mapView.onCreate(savedInstanceState?.getBundle(MAP_VIEW_BUNDLE_KEY))
        mapView.getMapAsync(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        this.menu = menu
        return super.onCreateOptionsMenu(menu)
    }

    fun minusZoom(view: View){
        gmap?.animateCamera(CameraUpdateFactory.zoomOut())
    }

    fun plusZoom(view: View){
        gmap?.animateCamera(CameraUpdateFactory.zoomIn())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY)
        mapViewBundle ?: run{
            mapViewBundle = Bundle()
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle)
        }
        mapView.onSaveInstanceState(mapViewBundle)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        model.sessionData.removeObservers(this)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onMapReady(map: GoogleMap?) {
        gmap = map
        gmap?.setOnMarkerClickListener(this)
    }

    private  fun makersMaker(title: String?): BitmapDescriptor {
        val iGen = IconGenerator(this)
        val icon = layoutInflater.inflate(R.layout.marker_view, null) as ConstraintLayout
        icon.name.text = title
        iGen.setContentView(icon)
        return BitmapDescriptorFactory.fromBitmap(iGen.makeIcon())
    }

    private fun setMarker(marker: Marker?, objectData: ObjectData) = CoroutineScope(IO).launch {
        marker?.let {
            val iGen = IconGenerator(this@MapActivity)
            val icon = layoutInflater.inflate(R.layout.marker_view, null) as ConstraintLayout
            icon.name.text = objectData.name
            val image = Picasso.with(this@MapActivity)
                .load("https://test.globars.ru/assets/images/unit/${objectData.icon}")
                .get()
            icon.imageView.setImageBitmap(image)
            iGen.setContentView(icon)
            withContext(Main){
                it.setIcon(BitmapDescriptorFactory.fromBitmap(iGen.makeIcon()))
            }
        }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        moveCameraToPosition(marker?.position)
        return true
    }

    private fun moveCameraToPosition(latLng: LatLng?) {
        gmap?.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        gmap?.animateCamera(CameraUpdateFactory.zoomTo( 14.0f ))
    }
}

class MapViewModel : ViewModel() {

    lateinit var retrofit: Retrofit

    val sessionData = MutableLiveData<TransportObjects?>()

    fun loadSession() = CoroutineScope(IO).launch {
        val trackingSessionsCallback = object : Callback1<TrackingSessionsResult> {
            override fun onResponse(call: Call<TrackingSessionsResult>, response: Response<TrackingSessionsResult>) {
                if (response.code() == 200) {
                    response.body()?.let {
                        it.data.map { data ->
                            getObjectsFromId(data)
                        }
                    }
                } else sessionData.postValue(null) }

            override fun onFailure(call: Call<TrackingSessionsResult>, t: Throwable) {
                sessionData.postValue(null)
            }

        }

        retrofit.create(GlobarsApiService::class.java)
            .getTrackingSessions()
            .enqueue(trackingSessionsCallback)
    }

    private fun getObjectsFromId(data: Data) {
        data.id?.let { sessionId ->
            val objectDataCallback = object : Callback1<TransportObjects> {
            override fun onResponse(call: Call<TransportObjects>, response: Response<TransportObjects>) {
                if(response.code() == 200){
                    response.body()?.let{
                        sessionData.postValue(it)
                    }
                }else sessionData.postValue(null)
            }

            override fun onFailure(call: Call<TransportObjects>, t: Throwable) {
                sessionData.postValue(null)
            }
        }
            retrofit.create(GlobarsApiService::class.java)
                .getObjects(sessionId)
                .enqueue(objectDataCallback)
        }
    }
}

