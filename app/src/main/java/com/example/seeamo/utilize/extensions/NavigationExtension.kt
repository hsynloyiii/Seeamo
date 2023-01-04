package com.example.seeamo.utilize.extensions

import androidx.navigation.NavController
import androidx.navigation.navOptions
import com.example.seeamo.R

fun NavController.navigateFade(
    route: String,
    launchSingleTop: Boolean = false,
    popUpTo: Boolean = false,
    popUpRoute: String = route,
    popInclusive: Boolean = false,
    saveState: Boolean = false,
    restoreState: Boolean = false
) {
    navigate(route, navOptions {
        anim {
            enter = R.anim.fade_in
            exit = R.anim.fade_out
            popEnter = R.anim.fade_in
            popExit = R.anim.fade_out
        }
        if (popUpTo)
            popUpTo(popUpRoute) {
                inclusive = popInclusive
                this.saveState = saveState
            }
        this.launchSingleTop = launchSingleTop
        this.restoreState = restoreState
    })
}

fun NavController.navigateSlide(
    route: String,
    launchSingleTop: Boolean = false,
    popUpTo: Boolean = false,
    popUpRoute: String = route,
    popInclusive: Boolean = false,
    saveState: Boolean = false,
    restoreState: Boolean = false
) {
    navigate(route, navOptions {
        anim {
            enter = R.anim.slide_in_right
            exit = R.anim.scale_out
            popEnter = R.anim.scale_in
            popExit = R.anim.slide_out_right
        }
        if (popUpTo)
            popUpTo(popUpRoute) {
                inclusive = popInclusive
                this.saveState = saveState
            }
        this.launchSingleTop = launchSingleTop
        this.restoreState = restoreState
    })
}


fun NavController.saveBackStack() {
    popBackStack(currentBackStackEntry?.destination?.id ?: 0, inclusive = true, saveState = true)
}