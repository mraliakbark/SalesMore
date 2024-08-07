package com.example.seller_application

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var tvTotalUsers: TextView
    private lateinit var tvTotalProducts:TextView
    private lateinit var tvTotalOrders: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        tvTotalUsers = findViewById(R.id.tv_TotalUsers)
        tvTotalProducts = findViewById(R.id.tv_TotalProducts)
        tvTotalOrders = findViewById(R.id.tv_TotalOrders)
        fetchTotalUsers()
        fetchTotalProducts()
        fetchTotalOrders()



    }
    private fun fetchTotalUsers() {
        db.collection("user")
            .get()
            .addOnSuccessListener { result ->
                val totalUsers = result.size() // Get the number of documents in the result
                // Now 'totalUsers' contains the total number of users
                // You can do whatever you want with this information, like displaying it in a TextView
                tvTotalUsers.text = "$totalUsers"

            }
            .addOnFailureListener { exception ->
                // Handle errors here
                // For example, display a Toast message or log the error
            }
    }
    private fun fetchTotalProducts() {
        db.collection("Products")
            .get()
            .addOnSuccessListener { result ->
                val totalProducts = result.size() // Get the number of documents in the result
                // Now 'totalUsers' contains the total number of users
                // You can do whatever you want with this information, like displaying it in a TextView
                tvTotalProducts.text = "$totalProducts"

            }
            .addOnFailureListener { exception ->
                // Handle errors here
                // For example, display a Toast message or log the error
            }
    }
    private fun fetchTotalOrders() {
        db.collection("orders")
            .get()
            .addOnSuccessListener { result ->
                val totalOrders = result.size() // Get the number of documents in the result
                // Now 'totalUsers' contains the total number of users
                // You can do whatever you want with this information, like displaying it in a TextView
                tvTotalOrders.text = "$totalOrders"

            }
            .addOnFailureListener { exception ->
                // Handle errors here
                // For example, display a Toast message or log the error
            }
    }


    // Function to navigate to AddProductsActivity
    fun navigateToAddProducts(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
    fun navigateToViewProducts(view: View) {
        val intent = Intent(this, ProductListActivity::class.java)
        startActivity(intent)
    }

}
