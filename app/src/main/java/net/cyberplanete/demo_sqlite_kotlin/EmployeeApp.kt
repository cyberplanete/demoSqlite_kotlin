package net.cyberplanete.demo_sqlite_kotlin

import android.app.Application
/*
* application class and initialize the database
* */
class EmployeeApp :Application() {

    val db by lazy { EmployeeDatabase.getInstance(this) }
}