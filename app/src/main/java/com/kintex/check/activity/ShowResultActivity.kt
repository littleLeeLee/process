package com.kintex.check.activity

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ToastUtils
import com.elvishew.xlog.XLog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kintex.check.R
import com.kintex.check.adapter.SummaryListAdapter
import com.kintex.check.bean.Action
import com.kintex.check.bean.TestCase
import com.kintex.check.bean.TestPlanBean
import com.kintex.check.bean.TestSummaryBean
import com.kintex.check.view.SmoothLinearLayoutManager
import com.kongzue.dialog.interfaces.OnDialogButtonClickListener
import com.kongzue.dialog.util.BaseDialog
import com.kongzue.dialog.v3.MessageDialog
import kotlinx.android.synthetic.main.activity_showresult.*


class ShowResultActivity : BaseActivity() {

    private val TESTPLAN = "testPlan"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_showresult)
        if(testResultList == null|| testResultList!!.size == 0){

            XLog.d("？？？？")
        }
        setView()
    }

    private fun setView() {

        tv_recovery.setOnClickListener {

          //  showChooseDialog()
            AccessibilityActivity.start(this)

        }


        setList()

    }
    private var adapter : SummaryListAdapter ?=null
    private fun setList() {


        adapter = SummaryListAdapter(this, testResultList!!)

        val smoothLinearLayoutManager =
            SmoothLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        ry_summary.layoutManager = smoothLinearLayoutManager
        ry_summary.adapter = adapter
        parseJson()
    }

    private fun parseJson(){
        val testCaseList = ArrayList<TestCase>()
        for (testPlanBean in testResultList!!) {
            if(testPlanBean.resultItemList != null){
                testCaseList.addAll(testPlanBean.resultItemList)
            }

        }
        if(testCaseList.size == 0){
            XLog.d("没有测试结果")
            return
        }
        val testSummaryBean = TestSummaryBean(Action("test_result",testCaseList))

        val toJson = Gson().toJson(
            testSummaryBean,
            object : TypeToken<TestSummaryBean>() {
            }.type
        )

        /*val toJson = Gson().toJson(
            testSummaryBean,
            object : TypeToken<ArrayList<TestCase?>?>() {
            }.type
        )*/
        XLog.d("json:$toJson")
    }

    private fun showChooseDialog() {

        val dialog = MessageDialog.build(this).setTitle("提示")
            .setMessage("此操作会删除设置上的所有数据，确认擦除数据吗？")
            .setOkButton("是", object : OnDialogButtonClickListener {
                override fun onClick(baseDialog: BaseDialog?, v: View?): Boolean {
                    dialog!!.dismiss()

                    wipeData()

                    return false
                }
            }).setCancelButton("否", object : OnDialogButtonClickListener {
                override fun onClick(baseDialog: BaseDialog?, v: View?): Boolean {
                    dialog!!.dismiss()
                    return false
                }
            })


        dialog.cancelable = false
        dialog.show()


    }
    private var devicePolicyManager :DevicePolicyManager ?= null
    private fun wipeData() {
        devicePolicyManager =  getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        var  adminComponent = ComponentName(packageName,
            "com.kintex.check.recevier.DeviceAdministrator"
        )

        // Request device admin activation if not enabled.
        if (!devicePolicyManager!!.isAdminActive(adminComponent)) {
            ToastUtils.showShort("请确认权限")
            val activateDeviceAdmin = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            activateDeviceAdmin.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            startActivityForResult(activateDeviceAdmin, 1024)
        }else{
            devicePolicyManager!!.wipeData(0)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1024){
            if(resultCode == RESULT_OK){
                ToastUtils.showShort("获取权限成功")
                devicePolicyManager!!.wipeData(0)
            }else{
                ToastUtils.showShort("获取权限失败")
            }
        }
    }

    companion object{
        private var testResultList : ArrayList<TestPlanBean> ?=null
        fun start(context: Context,arrayList: ArrayList<TestPlanBean>){
            testResultList = arrayList
            val intent = Intent(context, ShowResultActivity::class.java)
            context.startActivity(intent)
        }
    }

}