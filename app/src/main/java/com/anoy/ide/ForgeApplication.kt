package com.anoy.ide

import android.app.Application

class ForgeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        // TODO(M0): initialize Hilt, Room, Supabase and the toolchain integrity
        // verifier once the dependency graph lands.
    }

    companion object {
        lateinit var instance: ForgeApplication
            private set
    }
}
