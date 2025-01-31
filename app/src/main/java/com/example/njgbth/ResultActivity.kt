package com.example.njgbth

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.njgbth.databinding.ActivityResultBinding
import com.example.njgbth.databinding.ActivitySearchBinding
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private val dataString: ArrayList<String> = arrayListOf()
    private val datalink: ArrayList<String> = arrayListOf()
    private val dataInt : ArrayList<Int> = arrayListOf()
    private var getdata : ArrayList<String> =arrayListOf()
    private var getweight : ArrayList<Int> =arrayListOf()
    private lateinit var document : QuerySnapshot
    private val dataRecipe: ArrayList<RecipeData> = arrayListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent: Intent =getIntent()     //데이터 받아옴
        getdata = intent.getSerializableExtra("data") as ArrayList<String>  //데이터 받아옴
        for(i in getdata){      //확인용
            println(i)
        }

        binding.resultRecycle.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        binding.resultRecycle.adapter = RecyclerResultAdapter(dataRecipe)
        binding.resultRecycle.addItemDecoration(
            DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL)
        )
        addRecipe()

        val spinner = binding.spinner
        spinner.adapter = ArrayAdapter.createFromResource(this, R.array.sortList, android.R.layout.simple_spinner_item)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    //가중치 순으로 보여주기
                    0 -> {
                        weight_sort()
                    }
                    //선호도 순으로 보여주기 선택시
                    1 -> {
                        like_sort()
                    }

                }
            }
        }

    }
    fun weight_sort(){
        dataRecipe.sortBy { it.weight }
        dataRecipe.reverse()
        binding.resultRecycle.adapter = RecyclerResultAdapter(dataRecipe)
    }
    fun like_sort(){
        dataRecipe.sortBy { it.numOFHeart }
        dataRecipe.reverse()
        binding.resultRecycle.adapter = RecyclerResultAdapter(dataRecipe)
    }
    fun addRecipe(){
        if(getdata.isNullOrEmpty())
            return
        val db = Firebase.firestore
        db.collection("recipe")
            .get()
            .addOnSuccessListener { documents ->
                for(document in documents){
                    document.reference.collection("재료").document("재료").get()
                        .addOnSuccessListener { result ->
                            if (result != null) {
                                val a = (getdata.toSet()?.subtract(result.data!!.keys.toSet()))
                                if (a.isEmpty()) {
                                    println("이 레시피다 : ${document.id} ${document.get("링크")} ${document.get("선호도")}")
                                    println(result.data)
                                    var sum=0
                                    for(i in getdata.toSet())
                                        sum+=result.data!!.get(i).toString().toInt()
                                    dataRecipe.add(RecipeData(document.id,document.get("링크") as String, sum, document.get("선호도").toString().toInt()))
//                                    getweight.add(sum)
//                                    dataString.add(document.id)
//                                    datalink.add(document.get("링크") as String)
//                                    dataInt.add(document.get("선호도").toString().toInt())
//                                    println(getweight)
                                    weight_sort()
                                }
                            }
                        }
                }

            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)

            }
    }
    suspend fun db(){
        val db = Firebase.firestore
        db.collection("recipe")
            .get()
            .addOnSuccessListener { documents ->
                document = documents
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)

            }.await()
    }

    suspend fun getdocu(document : QueryDocumentSnapshot){
        document.reference.collection("재료").document("재료").get()
            .addOnSuccessListener { result ->
                //Log.d(ContentValues.TAG, "${document.id} : ${result.data}")
                if(result !=null){
                    val a = (getdata.toSet()?.subtract(result.data!!.keys.toSet()))
                    if(a.isEmpty()){
                        println("이 레시피다 : ${document.id}")
                        datalink.add(document.get("링크") as String)
                        dataInt.add(document.get("선호도").toString().toInt())
                    }
                    println(document.id)
                    //println("a"+a)
                    //println(result.data?.keys?.toSet())
                    //println(getdata.toSet())
                }
            }.await()
    }
}