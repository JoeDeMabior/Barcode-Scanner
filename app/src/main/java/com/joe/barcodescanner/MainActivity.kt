package com.joe.barcodescanner

import android.Manifest.permission.CAMERA
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView

class MainActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    private lateinit var scannerView: ZXingScannerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scannerView = ZXingScannerView(this)
        setContentView(scannerView)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (!checkPermission())
                requestCameraPermission()
        }
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(CAMERA), REQUEST_CAMERA)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA) {
            if (grantResults.isNotEmpty()) {
                val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (cameraAccepted) {
                    Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(CAMERA)) {
                            displayAlertMessage("Access to camera required",
                                DialogInterface.OnClickListener { _, _ ->
                                    requestPermissions(arrayOf(CAMERA), REQUEST_CAMERA)
                                }
                            )
                            return
                        }
                    }
                }
            }
        }
    }

    private fun displayAlertMessage(message: String, listener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK", listener)
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission()) {
                scannerView.setResultHandler(this)
                scannerView.startCamera()
            } else {
                checkPermission()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scannerView.stopCamera()
    }

    override fun handleResult(result: Result?) {
        val scanResult = result?.text
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Scan Result")
        builder.setPositiveButton("OK") { _, _ ->
            scannerView.resumeCameraPreview(this)
        }
        builder.setNeutralButton("Visit") { _, _ ->
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(scanResult)))
        }
        builder.setMessage(scanResult)

        val alert = builder.create()
        alert.show()
    }

    companion object {
        const val REQUEST_CAMERA = 100
    }

}
