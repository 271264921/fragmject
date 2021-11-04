package com.example.fragment.library.common.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.fragment.library.base.activity.BaseActivity
import com.example.fragment.library.base.utils.ActivityResultHelper.requestStoragePermissions
import com.example.fragment.library.base.utils.FragmentHelper
import com.example.fragment.library.base.utils.PermissionsCallback
import com.example.fragment.library.common.constant.NavMode
import com.example.fragment.library.common.constant.Router
import com.example.fragment.library.common.utils.WanHelper
import com.tencent.smtt.export.external.TbsCoreSettings
import com.tencent.smtt.sdk.QbSdk

/**
 * 路由类，方便模块之间调用
 */
abstract class RouterActivity : BaseActivity() {

    private var currFragment: Class<out Fragment>? = null

    abstract fun frameLayoutId(): Int

    /**
     * 导航方法，根据路由名跳转
     */
    abstract fun navigation(
        name: Router,
        bundle: Bundle? = null,
        navMode: NavMode = NavMode.SWITCH,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUIMode()
        initQbSdk()
    }

    /**
     * 切换Fragment
     */
    fun switcher(
        clazz: Class<out Fragment>,
        bundle: Bundle? = null,
        navMode: NavMode,
        addToBackStack: Boolean = true
    ) {
        if (navMode == NavMode.SWITCH) {
            currFragment = FragmentHelper.switcher(
                supportFragmentManager,
                frameLayoutId(),
                currFragment,
                clazz,
                bundle,
                addToBackStack
            )
        } else if (navMode == NavMode.POP_BACK_STACK) {
            currFragment = FragmentHelper.pop(supportFragmentManager, clazz)
        }
    }

    /**
     * 设置夜间模式方法
     */
    private fun initUIMode() {
        WanHelper.getUIMode().observe(this, {
            if (it != AppCompatDelegate.getDefaultNightMode()) {
                when (it) {
                    1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }
        })
    }

    private fun initQbSdk() {
        requestStoragePermissions(object : PermissionsCallback {
            override fun allow() {
                //x5内核初始化接口
                QbSdk.initX5Environment(applicationContext, object : QbSdk.PreInitCallback {
                    override fun onViewInitFinished(arg0: Boolean) {
                    }

                    override fun onCoreInitFinished() {
                    }
                })
                val map = HashMap<String, Any>()
                map[TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER] = true
                map[TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE] = true
                QbSdk.initTbsSettings(map)
            }

            override fun deny() {
            }

        })
    }

}