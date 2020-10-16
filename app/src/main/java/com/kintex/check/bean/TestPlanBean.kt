package com.kintex.check.bean

import android.os.Parcel
import android.os.Parcelable
import com.kintex.check.R


class TestPlanBean() : Parcelable {

     var planName = ""
     var planDescription=""
     var planPic = R.mipmap.wifi
     var planResult = 0
     var clickState = false
    var caseId = 0
    var resultItemList : ArrayList<TestCase> = ArrayList()

    constructor(parcel: Parcel) : this() {
        planName = parcel.readString()!!
        planDescription = parcel.readString()!!
        planPic = parcel.readInt()
        planResult = parcel.readInt()
        clickState = parcel.readByte() != 0.toByte()
        caseId = parcel.readInt()
    }

    constructor(
        planName: String,
        planDescription: String,
        planPic: Int,
        planResult: Int,
        clickState: Boolean ?=false,
        caseId: Int ?=0
    ) : this() {
        this.planName = planName
        this.planDescription = planDescription
        this.planPic = planPic
        this.planResult = planResult
        this.clickState = clickState!!
        this.caseId = caseId!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(planName)
        parcel.writeString(planDescription)
        parcel.writeInt(planPic)
        parcel.writeInt(planResult)
        parcel.writeByte(if (clickState) 1 else 0)
        parcel.writeInt(caseId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TestPlanBean> {
        override fun createFromParcel(parcel: Parcel): TestPlanBean {
            return TestPlanBean(parcel)
        }

        override fun newArray(size: Int): Array<TestPlanBean?> {
            return arrayOfNulls(size)
        }
    }
}