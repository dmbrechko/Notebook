package com.example.retrofitweather.model

import com.google.gson.annotations.SerializedName


data class Rain (

    @SerializedName("1h" ) var h : Double? = null

)