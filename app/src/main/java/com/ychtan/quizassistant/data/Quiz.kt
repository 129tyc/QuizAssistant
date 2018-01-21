package com.ychtan.quizassistant.data

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by 谈永成 on 2018/1/14.
 */
data class Quiz(val questionText: String,
                val separateText: MutableList<String>,
                val options: LinkedHashMap<String, Double>) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            mutableListOf(),
            linkedMapOf<String, Double>()) {
        parcel.readStringList(separateText)
        parcel.readMap(options, this::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(questionText)
        parcel.writeStringList(separateText)
        parcel.writeMap(options)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Quiz> {
        override fun createFromParcel(parcel: Parcel): Quiz {
            return Quiz(parcel)
        }

        override fun newArray(size: Int): Array<Quiz?> {
            return arrayOfNulls(size)
        }
    }
}