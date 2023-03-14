package com.projects.mytaxi

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.common.MapboxSDKCommon
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.projects.mytaxi.dao.LocationInfo
import com.projects.mytaxi.dao.LocationViewModel
import com.projects.mytaxi.databinding.FragmentMapBinding
import com.projects.mytaxi.service.LocationService
import java.lang.ref.WeakReference

class MapFragment : Fragment() {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var locationPermissionHelper: LocationPermissionHelper
    private lateinit var viewModel: LocationViewModel

    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        //binding.mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        binding.mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
        // binding.mapView.gestures.focalPoint = binding.mapView.getMapboxMap().pixelForCoordinate(it)
    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDismissed()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }

    val currentNightMode: Int = MapboxSDKCommon.getContext().getResources()
        .getConfiguration().uiMode and Configuration.UI_MODE_NIGHT_MASK


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapBinding.inflate(layoutInflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            0
        )

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val data = intent.getParcelableExtra("data_key") as? LocationInfo
                data?.let { viewModel.insertLocation(it) }
            }
        }

        val filter = IntentFilter("my_custom_action")
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(receiver, filter)


        val mapboxMap: MapboxMap = binding.mapView.getMapboxMap()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fetchLocation()

        val intentFilter = IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED)
        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_CONFIGURATION_CHANGED) {
                    val isDarkMode =
                        resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
                    if (isDarkMode) {
                        // Dark mode is now enabled
                        Log.d(TAG, "---------light")
                        binding.mapView.getMapboxMap().loadStyleUri(
                            Style.MAPBOX_STREETS
                        )
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

                    } else {
                        // Light mode is now enabled
                        Log.d(TAG, "---------night")
                        binding.mapView.getMapboxMap().loadStyleUri(
                            Style.TRAFFIC_NIGHT
                        )
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)


                    }
                }
            }
        }
        requireContext().registerReceiver(broadcastReceiver, intentFilter)

        binding.drawerMenuBtn.setOnClickListener {
            val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.drawerLayout)
            drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.menuBtn.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }


        binding.locationBtn.setOnClickListener {
            fetchLocation()
        }
        binding.plusBtn.setOnClickListener {
            val currentZoomLevel = mapboxMap.cameraState.zoom + 0.5

            binding.mapView.getMapboxMap().setCamera(
                CameraOptions.Builder()
                    .zoom(currentZoomLevel)
                    .build()
            )
        }
        binding.minusBtn.setOnClickListener {
            val currentZoomLevel = mapboxMap.cameraState.zoom - 0.5

            binding.mapView.getMapboxMap().setCamera(
                CameraOptions.Builder()
                    .zoom(currentZoomLevel)
                    .build()
            )
        }

    }

    private fun fetchLocation() {
        val task = mFusedLocationClient.lastLocation

        if (ActivityCompat.checkSelfPermission(
                requireActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 101
            )
            return
        }
        task.addOnSuccessListener { loc ->
            if (loc != null) {
                Intent(requireContext().applicationContext, LocationService::class.java).apply {
                    action = LocationService.ACTION_START
                    requireContext().startService(this)
                }
                locationPermissionHelper =
                    LocationPermissionHelper(WeakReference(requireActivity()))
                locationPermissionHelper.checkPermissions {
                    onMapReady()
                }

            } else {
                val builder = AlertDialog.Builder(requireContext())
                builder.setMessage("To continue, turn on device\nlocation, which uses Google's\nlocation service")
                builder.setPositiveButton("OK") { dialog, which ->
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))

                }
                builder.setNegativeButton("No,thanks") { dialog, which ->
                }
                builder.show()
            }
        }
    }

    private fun onMapReady() {
        binding.mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .zoom(14.0)
                .build()
        )
        binding.mapView.getMapboxMap().loadStyleUri(
            when (currentNightMode) {
                Configuration.UI_MODE_NIGHT_NO -> {
                    Style.MAPBOX_STREETS
                }
                Configuration.UI_MODE_NIGHT_YES -> {
                    Style.TRAFFIC_NIGHT
                }
                Configuration.UI_MODE_NIGHT_MASK -> {
                    Style.TRAFFIC_NIGHT
                }
                else -> {
                    Style.MAPBOX_STREETS
                }
            }
        )
        {
            initLocationComponent()
            setupGesturesListener()
        }
    }

    private fun setupGesturesListener() {
        binding.mapView.gestures.addOnMoveListener(onMoveListener)
    }

    private fun initLocationComponent() {
        val locationComponentPlugin = binding.mapView.location
        locationComponentPlugin.updateSettings {
            this.enabled = true
            this.locationPuck = LocationPuck2D(
                bearingImage = AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.car_icon_2,
                ),
                shadowImage = AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.car_icon_2,
                ),
                scaleExpression = interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(0.0)
                        literal(0.6)
                    }
                    stop {
                        literal(20.0)
                        literal(1.0)
                    }
                }.toJson()
            )
        }
        locationComponentPlugin.addOnIndicatorPositionChangedListener(
            onIndicatorPositionChangedListener
        )
        locationComponentPlugin.addOnIndicatorBearingChangedListener(
            onIndicatorBearingChangedListener
        )
    }

    private fun onCameraTrackingDismissed() {
        //Toast.makeText(this, "onCameraTrackingDismissed", Toast.LENGTH_SHORT).show()
        binding.mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        binding.mapView.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        binding.mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        binding.mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        binding.mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}