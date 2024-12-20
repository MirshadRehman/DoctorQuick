package com.example.doctorquick

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import com.example.doctorquick.databinding.ActivityMainBinding
import com.example.doctorquick.auth.WelcomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private var userType: String = "Patient"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setSupportActionBar(binding.toolbar)

        drawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        userType = intent.getStringExtra("userType") ?: "Patient"
        val graphId = if (userType == "Doctor") R.navigation.nav_doctor else R.navigation.nav_patient
        navController.setGraph(graphId)

        // Set the correct menu for the drawer based on the userType
        val menuId = if (userType == "Doctor") R.menu.drawer_menu_doctor else R.menu.drawer_menu_patient
        navView.menu.clear()  // Clear existing items
        navView.inflateMenu(menuId)  // Inflate the correct menu

        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        NavigationUI.setupWithNavController(navView, navController)

        setUpNavigationDrawer(userType)

        // Setup navigation listener based on userType
        navView.setNavigationItemSelectedListener { menuItem ->
            handleNavigation(menuItem)
        }
    }

    private fun handleNavigation(menuItem: MenuItem): Boolean {
        when (userType) {
            "Doctor" -> return handleDoctorNavigation(menuItem)
            "Patient" -> return handlePatientNavigation(menuItem)
        }
        return false
    }

    private fun handleDoctorNavigation(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.profileDoctor, R.id.dashboardAppointmentDoctor -> {
                navigateTo(menuItem.itemId)
                true
            }
            R.id.logout_doctor -> {
                confirmLogout()
                true
            }
            else -> false
        }
    }

    private fun handlePatientNavigation(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.profilePatient, R.id.dashboardAppointmentPatient, R.id.viewAllDoctors, R.id.dashboardAmbulance -> {
                navigateTo(menuItem.itemId)
                true
            }
            R.id.logout_patient -> {
                confirmLogout()
                true
            }
            else -> false
        }
    }

    private fun navigateTo(destinationId: Int) {
        val bundle = Bundle().apply { putString("userId", auth.currentUser?.uid) }
        if (navController.currentDestination?.id != destinationId) {
            navController.navigate(destinationId, bundle)
        }
        drawerLayout.closeDrawers()
    }

    private fun setUpNavigationDrawer(userType: String) {
        val headerView = binding.navView.getHeaderView(0)
        val userNameTextView = headerView.findViewById<TextView>(R.id.tvDoctorPatientName)
        auth.currentUser?.uid?.let { userId ->
            firestore.collection("users").document(userId).get().addOnSuccessListener { documentSnapshot ->
                userNameTextView.text = "Hello, ${documentSnapshot.getString("userName")}"
            }.addOnFailureListener {
                userNameTextView.text = "Hello, User"
            }
        }
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this).apply {
            setTitle("Confirm Logout")
            setMessage("Are you sure you want to log out?")
            setPositiveButton("Log Out") { _, _ -> logout() }
            setNegativeButton("Cancel", null)
            show()
        }
    }

    private fun logout() {
        auth.signOut()
        startActivity(Intent(this, WelcomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }
}
