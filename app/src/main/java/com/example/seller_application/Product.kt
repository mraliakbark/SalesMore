package com.example.seller_application

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val id: String,
    val name: String,
    val category: String,
    val price: Float,
    val offerPercentage: Float? = null,
    val description: String? = null,
    val colors: List<Int>? = null,
    val sizes: List<String>? = null,
    val images: List<String>, // Assuming this list contains image URLs
    val imageUrl: String="" // Add this field to hold the image URL
)
 : Parcelable{
    constructor(): this("0","","",0f,images = emptyList())

}