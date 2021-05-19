package com.kintex.check.activity

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.elvishew.xlog.XLog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kintex.check.R
import com.kintex.check.adapter.SummaryListAdapter
import com.kintex.check.bean.*
import com.kintex.check.utils.ResultCode.SENDSUMMARY
import com.kintex.check.view.SmoothLinearLayoutManager
import com.kongzue.dialog.interfaces.OnDialogButtonClickListener
import com.kongzue.dialog.util.BaseDialog
import com.kongzue.dialog.v3.MessageDialog
import kotlinx.android.synthetic.main.activity_showresult.*
import org.greenrobot.eventbus.EventBus


class ShowResultActivity : BaseActivity() {

    private val TESTPLAN = "testPlan"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        setContentView(R.layout.activity_showresult)
        if(testResultList == null|| testResultList!!.size == 0){
            XLog.d("？？？？")
        }
        setView()
    }


    private fun setView() {

        tv_recovery.setOnClickListener {

            showChooseDialog()
         //   AccessibilityActivity.start(this)

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
            testCaseList.addAll(testPlanBean.resultItemList)

        }
        if(testCaseList.size == 0){
            XLog.d("没有测试结果")
            return
        }

        val testSummaryBean = TestSummaryBean(Action("test_inprogress",SPUtils.getInstance().getString("UUID")),testCaseList)

        val toJson = Gson().toJson(
            testSummaryBean,
            object : TypeToken<TestSummaryBean>() {
            }.type
        )
        val testResultBean = TestResultBean(SENDSUMMARY, 0, null)
        testResultBean.description = toJson
        EventBus.getDefault().post(testResultBean)
        XLog.d("json:$toJson")
    }

    private fun showChooseDialog() {

        val dialog = MessageDialog.build(this).setTitle("提示")
            .setMessage("此操作会删除设置上的所有数据，确认擦除数据吗？")
            dialog.setOkButton("是", object : OnDialogButtonClickListener {
                override fun onClick(baseDialog: BaseDialog?, v: View?): Boolean {
                    dialog!!.doDismiss()

                    wipeData()

                    return false
                }
            })
            dialog.setCancelButton("否", object : OnDialogButtonClickListener {
                override fun onClick(baseDialog: BaseDialog?, v: View?): Boolean {
                    dialog!!.doDismiss()
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
         var instance : ShowResultActivity?=null
        private var testResultList : ArrayList<TestPlanBean> ?=null
        fun start(context: Context,arrayList: ArrayList<TestPlanBean>){
            testResultList = arrayList
            val intent = Intent(context, ShowResultActivity::class.java)
            context.startActivity(intent)
        }
    }

}