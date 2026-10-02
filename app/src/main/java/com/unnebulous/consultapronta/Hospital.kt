package com.unnebulous.consultapronta

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.unnebulous.consultapronta.databinding.FragmentHospitalBinding
import com.unnebulous.consultapronta.recyclerview.adapter.HospitalAdapter
import de.afarber.openmapview.LatLng
import de.afarber.openmapview.Marker
import de.westnordost.osmapi.OsmConnection
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Node
import de.westnordost.osmapi.map.data.Relation
import de.westnordost.osmapi.map.data.Way
import de.westnordost.osmapi.map.handler.MapDataHandler
import de.westnordost.osmapi.overpass.OverpassMapDataApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import kotlin.concurrent.thread
import com.unnebulous.consultapronta.database.Hospital as HospitalModel

//const val ENDPOINT = "https://overpass.private.coffee/api/interpreter"
const val ENDPOINT = "https://overpass-api.de/api/interpreter"
const val QUERY_RADIUS = 7000

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
				TODO()
			}
		}

		binding.recyclerview.adapter = adapter

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

		val marker = Marker(latLng,
			getString(R.string.user_marker_title),
			getString(R.string.user_marker_snippet)
		)
		setNavbarEntryActive(Utils.NavbarButton.FOURTH)

		binding.mapView.addMarker(marker)
		userLocationMarker = marker

		binding.mapView.invalidate()
		binding.mapView.setCenter(latLng)

		lifecycleScope.launch {
			val hospitals = getHospitals(getQuery(latLng.latitude, latLng.longitude))
			(binding.recyclerview.adapter as HospitalAdapter).userLatLng = marker.position

			hospitals.forEach { hospital ->
				binding.mapView.addMarker(Marker(
					hospital.latLng,
					hospital.name,
					hospital.address
				))
			}

			(binding.recyclerview.adapter as HospitalAdapter).submitList(hospitals)

			binding.mapView.invalidate()
		}
	}

	private fun getQuery(latitude: Double, longitude: Double): String = """
			[out:csv(::lat, ::lon, name, "addr:street", "addr:housenumber", "addr:suburb"; false; "|")][timeout:25];
			nwr["amenity"="hospital"](around:$QUERY_RADIUS, $latitude, $longitude);
			out center;
		""".trimIndent()

	private suspend fun getHospitals(query: String): List<HospitalModel> = withContext(Dispatchers.IO){
		val connection = OsmConnection(ENDPOINT, "ConsultaPronta/1.0 (${requireContext().packageName})")
		val overpass = OverpassMapDataApi(connection)
		val hospitals = mutableListOf<HospitalModel>()

		try {
			overpass.queryTable(query) { row ->
				val lat = row.getOrNull(0)?.toDoubleOrNull() ?: return@queryTable
				val lon = row.getOrNull(1)?.toDoubleOrNull() ?: return@queryTable
				val name = row.getOrNull(2)?.takeIf { it.isNotEmpty() } ?: getString(R.string.not_available)

				val street = row.getOrNull(3) ?: ""
				val number = row.getOrNull(4) ?: ""
				val suburb = row.getOrNull(5) ?: ""

				val address = if (street.isNotEmpty()) {
					listOf(street, number, suburb).filter { it.isNotEmpty() }.joinToString(", ")
				} else {
					getString(R.string.address_not_found)
				}

				Log.i("InfoPronto", name)

				hospitals.add(HospitalModel(
					name,
					address,
					LatLng(lat, lon)
				))
			}
		} catch (e: Exception) {
			showSnackbar(getString(R.string.not_possible_get_hospital), Utils.SnackBarType.DANGER)
			e.printStackTrace()
		}


		return@withContext hospitals.filter { it.name != getString(R.string.not_available) }
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
		(requireActivity() as MainActivity).setFragmentPadding()
	}
}