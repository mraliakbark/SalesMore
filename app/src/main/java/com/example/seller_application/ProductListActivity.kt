package com.example.seller_application

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.seller_application.databinding.ActivityProductListBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProductListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductListBinding
    private lateinit var adapter: ProductAdapter
    private val firestore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up RecyclerView
        adapter = ProductAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Fetch products from Firestore
        fetchProducts()
    }

    private fun fetchProducts() {
        // Query Firestore for all products
        firestore.collection("Products")
            .get()
            .addOnSuccessListener { result ->
                val products = mutableListOf<Product>()
                for (document in result) {
                    val product = document.toObject(Product::class.java)
                    products.add(product)
                }
                // Update RecyclerView with fetched products
                adapter.submitList(products)
            }
            .addOnFailureListener { exception ->
                // Handle failures
                // You can show an error message or retry fetching products
            }
    }
}
