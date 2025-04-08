package com.example.bloodbank

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseAuth


@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()



    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController) }
        composable("landing") { LandingPage(navController) }
        composable("login") { LoginPage(navController) }
        composable("register") { RegisterPage(navController) }
        composable("dashboard") { RequestBloodScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
        composable("addpersonpage") { AddPersonPage(navController)}
        composable("view_people") { ViewPeoplePage(navController) }
        composable("blood_quantity") { BloodQuantityScreen(navController) }
        composable("search_page") { SearchPersonScreen(navController) }




    }
}
