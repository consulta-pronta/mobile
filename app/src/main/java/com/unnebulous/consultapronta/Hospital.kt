package com.unnebulous.consultapronta

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.unnebulous.consultapronta.databinding.FragmentHospitalBinding
import com.unnebulous.consultapronta.recyclerview.adapter.HospitalAdapter
import de.afarber.openmapview.LatLng
import de.afarber.openmapview.Marker

class Hospital : Fragment() {

	private var _binding: FragmentHospitalBinding? = null
	private val binding get() = _binding!!

	private var isMapView = false
	private var userLocationMarker: Marker? = null

	private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
	private lateinit var settingsLauncher: ActivityResultLauncher<IntentSenderRequest>

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		permissionLauncher = PermissionHelper.createRequestMultiplePermissionsLauncher(this) { permissions ->
			val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
			val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

			if (fineLocationGranted || coarseLocationGranted) {
				// se alguma permissão foi dada, verifica se o GPS de alta precisão está ligado
				checkLocationSettings()
			} else {
				// TODO: handler para permissão negada
			}
		}

		settingsLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { _ ->
			// tenta obter a localização após o fechamento do diálogo de configurações
			onLocationPermissionGranted()
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentHospitalBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		updateHeader {
			changeHeaderType(Utils.HeaderType.COMPACT)
		}

		(requireActivity() as MainActivity).setFragmentPadding(0)

		val adapter = HospitalAdapter().apply {
			onClick = { hospital ->

			}
		}



		binding.recyclerview.adapter = adapter

		binding.mapView.setCenter(LatLng(-20.198054, -40.216428))
		binding.mapView.setZoom(20f)

		binding.buttonMapView.setOnClickListener {
			isMapView = !isMapView

			var buttonDrawable: Drawable?

			if (isMapView) {
				checkAndRequestLocationPermission()

				binding.mapView.visibility = View.VISIBLE
				binding.recyclerview.visibility = View.GONE
				binding.order.visibility = View.GONE
				binding.buttonMapView.text = getString(R.string.list_view)
				buttonDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_list)
			} else {
				binding.mapView.visibility = View.GONE
				binding.recyclerview.visibility = View.VISIBLE
				binding.order.visibility = View.VISIBLE
				binding.buttonMapView.text = getString(R.string.map_view)
				buttonDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_map)
			}

			buttonDrawable?.setTint(ContextCompat.getColor(requireContext(), R.color.textLight))
			binding.buttonMapView.setCompoundDrawablesRelativeWithIntrinsicBounds(buttonDrawable, null, null, null)
		}
	}

	private fun checkAndRequestLocationPermission() {
		val isFineLocationGranted = PermissionHelper.checkPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
		val isCoarseLocationGranted = PermissionHelper.checkPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)

		if (!isFineLocationGranted && !isCoarseLocationGranted) {
			// caso não tenha permissão, solicita
			permissionLauncher.launch(
				arrayOf(
					Manifest.permission.ACCESS_FINE_LOCATION,
					Manifest.permission.ACCESS_COARSE_LOCATION
				)
			)
		} else {
			// se já tiver permissão, verifica se a opção "Precisão de Local" do Google está ativada
			checkLocationSettings()
		}
	}

	private fun checkLocationSettings() {
		// define como será a requisição
		val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build()
		// coloca a requisição num pedido
		val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

		// efetivamente faz a requisição
		val task = LocationServices
			.getSettingsClient(requireActivity())
			.checkLocationSettings(builder.build())

		task.addOnSuccessListener {
			// caso esteja ativada, então prosseguir normalmente
			onLocationPermissionGranted()
		}

		task.addOnFailureListener { exception ->
			if (exception is ResolvableApiException) {
				// se a precisão estiver desligada, tenta solicitar a ativação
				try {
					val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
					settingsLauncher.launch(intentSenderRequest)
				} catch (_: IntentSender.SendIntentException) {
					onLocationPermissionGranted()
				}
			} else {
				onLocationPermissionGranted()
			}
		}
	}

	@SuppressLint("MissingPermission")
	private fun onLocationPermissionGranted() {
		val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

		fusedLocationClient.lastLocation.addOnSuccessListener { location ->
			if (location != null) {
				updateUserLocationOnMap(location.latitude, location.longitude)
			} else {
				// se não conseguir uma última localização conhecida, pega a posição atual
				val priority = Priority.PRIORITY_HIGH_ACCURACY
				val cancellationTokenSource = CancellationTokenSource()

				fusedLocationClient.getCurrentLocation(priority, cancellationTokenSource.token)
					.addOnSuccessListener { currentLocation ->
						if (currentLocation != null) {
							updateUserLocationOnMap(currentLocation.latitude, currentLocation.longitude)
						} else {
							// se o serviço de "Precição de Local" falhar/estiver desativada, tenta a API nativa do Android
							startNativeLocationFallback()
						}
					}
			}
		}
	}

	@SuppressLint("MissingPermission")
	private fun startNativeLocationFallback() {
		val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

		// tenta pegar a última posição registrada via GPS ou rede
		val lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) 
			?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

		if (lastKnown != null) {
			updateUserLocationOnMap(lastKnown.latitude, lastKnown.longitude)
		} else {
			showSnackbar(getString(R.string.location_not_possible), Utils.SnackBarType.DANGER)
		}
	}

	private fun updateUserLocationOnMap(lat: Double, lng: Double) {
		val latLng = LatLng(lat, lng)

		userLocationMarker?.let {
			binding.mapView.removeMarker(it)
		}

		// TODO: alterar o texto e adicionar no strings.xml
		// Marker(OBJETO LatLng, TITULO, DESCRICAO)
		val marker = Marker(latLng, "Sua posição", "Você está aqui!")
		binding.mapView.addMarker(marker)
		userLocationMarker = marker

		binding.mapView.setCenter(latLng)

		binding.mapView.invalidate()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
		(requireActivity() as MainActivity).setFragmentPadding()
	}
}