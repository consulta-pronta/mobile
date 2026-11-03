package com.unnebulous.consultapronta

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

object PermissionHelper {
	fun checkPermission(context: Context, permission: String): Boolean {
		return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
	}

	fun createRequestMultiplePermissionsLauncher(fragment: Fragment, callback: (Map<String, Boolean>) -> Unit): ActivityResultLauncher<Array<String>> {
		return fragment.registerForActivityResult(
			ActivityResultContracts.RequestMultiplePermissions()
		) { permissions ->
			callback(permissions)
		}
	}

	fun createRequestPermissionLauncher(fragment: Fragment, callback: (Boolean) -> Unit): ActivityResultLauncher<String> {
		return fragment.registerForActivityResult(
			ActivityResultContracts.RequestPermission()
		) { isGranted ->
			callback(isGranted)
		}
	}
}