package com.projects.mytaxi

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.projects.mytaxi.databinding.FragmentSplashBinding
import com.projects.mytaxi.service.LocationService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {
    private var binding: FragmentSplashBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSplashBinding.inflate(layoutInflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            delay(2000)
            checkGps()
        }
    }

    fun checkGps() {
        val locationManager =
            context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGPSEnabled =
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (isGPSEnabled) {
            // GPS is enabled, do something
            Intent(
                requireContext().applicationContext,
                LocationService::class.java
            ).apply {
                action = LocationService.ACTION_START
                requireContext().startService(this)
            }
            findNavController().navigate(R.id.action_splashFragment_to_mapFragment)

        } else {
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage("To continue, turn on device\nlocation, which uses Google's\nlocation service")
            builder.setPositiveButton("OK") { dialog, which ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                findNavController().navigate(R.id.action_splashFragment_to_mapFragment)
            }
            builder.setNegativeButton("No,thanks") { dialog, which ->
                findNavController().navigate(R.id.action_splashFragment_to_mapFragment)
            }
            builder.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

