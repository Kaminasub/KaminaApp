package com.kamina.app.api

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

fun fetchEntities(onResult: (List<EntityResponse>?) -> Unit) {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.kaminajp.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api = retrofit.create(ApiService::class.java)

    api.getEntities().enqueue(object : Callback<List<EntityResponse>> {
        override fun onResponse(call: Call<List<EntityResponse>>, response: Response<List<EntityResponse>>) {
            if (response.isSuccessful) {
                onResult(response.body())
            } else {
                onResult(null)
            }
        }

        override fun onFailure(call: Call<List<EntityResponse>>, t: Throwable) {
            onResult(null)
        }
    })
}
