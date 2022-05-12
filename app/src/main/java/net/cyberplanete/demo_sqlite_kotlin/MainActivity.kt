package net.cyberplanete.demo_sqlite_kotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import net.cyberplanete.demo_sqlite_kotlin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
//Todo 9: get the employeeDao variable through the application class
        val employeeDAO = (application as EmployeeApp).db.employeeDao()
        binding?.btnAdd?.setOnClickListener { addRecord(employeeDAO) }

        //launch a coroutine block and fetch all employee
        lifecycleScope.launch {
            employeeDAO.fetchAllEmployee().collect {
                val list = ArrayList(it)
                setupListOfDataIntoRecyclerView(list, employeeDAO)
            }
        }

    }

    fun addRecord(employeeDAO: EmployeeDAO) {
        val name = binding?.etName?.text.toString()
        val email = binding?.etEmailId?.text.toString()

        if (email.isNotEmpty() && name.isNotEmpty()) {
            lifecycleScope.launch { employeeDAO.insert(EmployeeEntity(name = name, email = email)) }
            val test = Toast.makeText(applicationContext, "Record saved", Toast.LENGTH_LONG)
            test.show()
            binding?.etName?.text?.clear()
            binding?.etEmailId?.text?.clear()
        } else {
            Toast.makeText(
                applicationContext,
                "Data must be entered either email or name",
                Toast.LENGTH_LONG
            ).show()
        }

    }

    private fun setupListOfDataIntoRecyclerView(
        employeeList: ArrayList<EmployeeEntity>,
        employeeDAO: EmployeeDAO
    ) {
        if (employeeList.isNotEmpty()) {
            val itemAdapter = ItemAdapter(employeeList)
            binding?.rvItemsList?.layoutManager = LinearLayoutManager(this)
            binding?.rvItemsList?.adapter = itemAdapter
            binding?.rvItemsList?.visibility = View.VISIBLE
            binding?.tvNoRecordsAvailable?.visibility = View.GONE
        } else {
            binding?.rvItemsList?.visibility = View.GONE
            binding?.tvNoRecordsAvailable?.visibility = View.VISIBLE
        }

    }


}