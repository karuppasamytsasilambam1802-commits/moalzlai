package com.example.myapplication.location

data class UserLocation(
    val latitude: Double = 13.0827,
    val longitude: Double = 80.2707,
    val city: String = "Chennai",
    val country: String = "India"
)

object LocationManager {

    fun getCurrentLocation(): UserLocation {
        return UserLocation(
            latitude = 13.0827 + ((-50..50).random() / 1000.0),
            longitude = 80.2707 + ((-50..50).random() / 1000.0),
            city = "Chennai",
            country = "India"
        )
    }
}
