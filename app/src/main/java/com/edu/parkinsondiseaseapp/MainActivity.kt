package com.edu.parkinsondiseaseapp

import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log.d
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.edu.parkinsondiseaseapp.databinding.ActivityMainBinding
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel : MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        //check permission/request permission
        viewModel.permissionGranted = ActivityCompat.checkSelfPermission(this, viewModel.permission[0]) == PackageManager.PERMISSION_GRANTED
        viewModel.permissionExternalDirGranted = ActivityCompat.checkSelfPermission( this, viewModel.permissionDir[0]) == PackageManager.PERMISSION_GRANTED

        if (!viewModel.permissionGranted)
            ActivityCompat.requestPermissions(this, viewModel.permission, viewModel.REQUEST_CODE)

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            navController.popBackStack()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        d("bomoh", "PERMISSION: $grantResults, $requestCode")
        when (requestCode) {
            viewModel.REQUEST_CODE -> viewModel.permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
            viewModel.REQUEST_CODE_DIR -> viewModel.permissionExternalDirGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }


}