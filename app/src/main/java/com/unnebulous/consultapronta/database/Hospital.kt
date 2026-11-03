package com.unnebulous.consultapronta.database

import de.afarber.openmapview.LatLng

data class Hospital(
	val name: String,
	val address: String,
	val latLng: LatLng,
	val occupancyPercentage: Int = 0,
)