package com.tlc.domain.model.firebase

import android.os.Parcel
import android.os.Parcelable

data class Place (
    var name: String = "",
    var capacity: Int = 0,
    val id: String = "",
    val placeImageUrl: String = "",
    val reservationTimes: List<String> = emptyList()
    ): Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readInt(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.createStringArrayList() ?: emptyList()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(capacity)
        parcel.writeString(id)
        parcel.writeString(placeImageUrl)
        parcel.writeStringList(reservationTimes)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Place> {
        override fun createFromParcel(parcel: Parcel): Place {
            return Place(parcel)
        }

        override fun newArray(size: Int): Array<Place?> {
            return arrayOfNulls(size)
        }
    }
}