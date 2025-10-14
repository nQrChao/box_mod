package com.box.mod.ui.util

import android.app.Application

/**
 * 服务管理者，单例模式
 * 用于持有 app 模块提供的服务实现
 */
object ModManager {
    private var _provider: ModProvider? = null

    val provider: ModProvider
        get() = _provider
            ?: throw IllegalStateException("IAppServiceProvider has not been initialized. Please call ModServiceManager.initialize() in your Application class.")

    /**
     * 在 app 模块的 Application.onCreate() 中调用此方法进行初始化
     */
    fun initialize(app: Application, provider: ModProvider) {
        this._provider = provider
    }
}