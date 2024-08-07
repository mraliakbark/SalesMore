package com.example.seller_application

import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.contextaware.withContextAvailable
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.seller_application.databinding.ActivityMainBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private var selectImages = mutableListOf<Uri>()
    private val selectedColors = mutableListOf<Int>()
    private val productStorage = Firebase.storage.reference
    private val firestore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.buttonSave.setOnClickListener {
            onSaveButtonClick(it) // Call the onSaveButtonClick function
        }
        binding.buttonColorPicker.setOnClickListener {
            ColorPickerDialog.Builder(this)
                .setTitle("Product color")
                .setPositiveButton("select", object : ColorEnvelopeListener {
                    override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {
                        envelope?.let {
                            selectedColors.add(it.color)
                            updateColors()
                        }
                    }

                })
                .setNegativeButton("Cancel") { colorPicker, _ ->
                    colorPicker.dismiss()

                }.show()
        }
            val selectImagesActivityResult =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
                    if (result.resultCode == RESULT_OK){
                        val intent = result.data

                        //Multiple images selected
                        if (intent?.clipData !=null){
                            val count = intent.clipData?.itemCount ?:0
                            (0 until count).forEach {
                                val imagesUri = intent.clipData?.getItemAt(it)?.uri
                                imagesUri?.let {
                                    selectImages.add(it)
                                }
                            }

                            }else{
                                val imageUri = intent?.data
                                imageUri?.let { selectImages.add(it) }
                        }
                        updateImages()
                        }

                    }


        binding.buttonImagesPicker.setOnClickListener {
            val intent = Intent(ACTION_GET_CONTENT)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
            intent.type = "image/*"
            selectImagesActivityResult.launch(intent)


        }

    }

    private fun updateImages() {
        binding.tvSelectedImages.text = selectImages.size.toString()
    }

    private fun updateColors() {
        var colors = ""
        selectedColors.forEach{
            colors="$colors ${Integer.toHexString(it)}"
        }
        binding.tvSelectedColors.text = colors
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }
    fun onSaveButtonClick(view: android.view.View) {
        val productValidation = validationInformation()
        if (!productValidation) {
            Toast.makeText(this, "Check your inputs", Toast.LENGTH_LONG).show()
        } else {
            saveProducts()
        }
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.itemId == R.id.saveProduct){
//            val productValidation = validationInformation()
//            if (!productValidation){
//                Toast.makeText(this,"Check your inputs",Toast.LENGTH_LONG).show()
//                return false
//            }
//            saveProducts()
//        }
//        return super.onOptionsItemSelected(item)
//    }

    private fun saveProducts() {
        val name = binding.edName.text.toString().trim()
        val category = binding.edCategory.text.toString().trim()
        val price = binding.edPrice.text.toString().trim()
        val offerPercentage = binding.offerPercentage.text.toString().trim()
        val description = binding.edDescription.text.toString().trim()
        val sizes = getSizesList(binding.edSizes.text.toString().trim())
        val imagesByteArrays = getImagesByteArrays()
        val images = mutableListOf<String>()

        lifecycleScope.launch  (Dispatchers.IO){
            withContext(Dispatchers.Main) {
                showLoading()
            }



            try {
                async {
                    imagesByteArrays.forEach {
                        val id = UUID.randomUUID().toString()
                        launch {
                            val imageStorage = productStorage.child("product/images/$id")
                            val result = imageStorage.putBytes(it).await()
                            val downloadUrl = result.storage.downloadUrl.await().toString()
                            images.add(downloadUrl)
                        }
                    }
                }.await()

            }catch (e: java.lang.Exception){
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    showLoading()
                }
            }
            val product = Product(
                UUID.randomUUID().toString(),
                name,
                category,
                price.toFloat(),
                if (offerPercentage.isEmpty()) null else offerPercentage.toFloat(),
                if (description.isEmpty()) null else description,
                if (selectedColors.isEmpty()) null else selectedColors,
                sizes,
                images
            )

            firestore.collection("Products").add(product).addOnSuccessListener {
                hideLoading()
            }.addOnFailureListener {
                hideLoading()
                Log.e("Error",it.message.toString())
            }
        }
    }

    private fun hideLoading() {
        binding.progressbar.visibility = View.INVISIBLE
    }

    private fun showLoading() {
        binding.progressbar.visibility = View.VISIBLE
    }

    private fun getImagesByteArrays(): List<ByteArray> {
        val imageByteArray = mutableListOf<ByteArray>()
        selectImages.forEach {
            val stream = ByteArrayOutputStream()
            val imageBmp = MediaStore.Images.Media.getBitmap(contentResolver,it)
            if (imageBmp.compress(Bitmap.CompressFormat.JPEG,100,stream)){
                imageByteArray.add(stream.toByteArray())
            }
        }
        return imageByteArray
    }

    private fun getSizesList(sizesStr: String): List<String>? {
        if (sizesStr.isEmpty())
            return null
        val sizesList = sizesStr.split(",")
        return sizesList
    }

    private fun validationInformation(): Boolean {
        if (binding.edPrice.text.toString().toString().isEmpty())
            return false
        if (binding.edName.text.toString().toString().isEmpty())
            return false
        if (binding.edCategory.text.toString().toString().isEmpty())
            return false

        if(selectImages.isEmpty())
            return false

        return true

    }
}



