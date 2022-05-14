package net.cyberplanete.demo_sqlite_kotlin

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import net.cyberplanete.demo_sqlite_kotlin.databinding.ActivityMainBinding
import net.cyberplanete.demo_sqlite_kotlin.databinding.DialogUpdateBinding

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
                val employeeList = ArrayList(it)
                setupListOfDataIntoRecyclerView(employeeList, employeeDAO)
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

            /* Adapter class is initialized and list is passed in the param.*/
            val itemAdapter = ItemAdapter(employeeList, { updateID ->
                updateRecordDialog(updateID, employeeDAO)
            }, { deleteID ->
                lifecycleScope.launch {
                    employeeDAO.fetchEmployeeByID(deleteID).collect {
                        if (it != null) {
                            deleteRecordAlertDialog(deleteID, employeeDAO, it)
                        }
                    }
                }


            })
            binding?.rvItemsList?.layoutManager = LinearLayoutManager(this)
            binding?.rvItemsList?.adapter = itemAdapter
            binding?.rvItemsList?.visibility = View.VISIBLE
            binding?.tvNoRecordsAvailable?.visibility = View.GONE
        } else {
            binding?.rvItemsList?.visibility = View.GONE
            binding?.tvNoRecordsAvailable?.visibility = View.VISIBLE
        }

    }

    /*
    *       UI DIALOG updateRecordDialog if button edit is Pressed
    *
    * */
    fun updateRecordDialog(id: Int, employeeDAO: EmployeeDAO) {
        /*
        *  UI Dialog
        * */
        val updateDialog = Dialog(
            this,
            R.style.Theme_Dialog // Correction bug avec creation style de theme.xml
        ) // UI du dialog
        updateDialog.setCancelable(false) // Click outside of the dialog is not permitted
        val dialogUpdateBinding =
            DialogUpdateBinding.inflate(layoutInflater) // Using dialog_update.xml
        updateDialog.setContentView(dialogUpdateBinding.root) // Affichage du dialog
        updateDialog.show() // update dialog
        /* END UI DIALOG */

        /*
        * Populating TextField name and email
        * */
        lifecycleScope.launch {
            employeeDAO.fetchEmployeeByID(id).collect {
                if (it != null)
                {
                    dialogUpdateBinding.etUpdateName.setText(it.name)
                    dialogUpdateBinding.etUpdateEmailId.setText(it.email)
                }

            }
        }
        /*
        * Logique du bouton update
        * */
        dialogUpdateBinding.tvUpdate.setOnClickListener {
            val name = dialogUpdateBinding.etUpdateName.text.toString()
            val email = dialogUpdateBinding.etUpdateEmailId.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty()) // if all TextFields not empty
            {
                lifecycleScope.launch {
                    employeeDAO.update(
                        EmployeeEntity(
                            id,
                            name = name,
                            email = email
                        )
                    ) // Update database with new data
                    Toast.makeText(applicationContext, "Record updated", Toast.LENGTH_LONG)
                        .show() // Informing user
                    updateDialog.dismiss()
                }

            } else {
                Toast.makeText(
                    applicationContext,
                    "Name or email cannot be blank",
                    Toast.LENGTH_LONG
                ).show()
            }

        }
        /*
            * Logique du bouton cancel
            * */
        dialogUpdateBinding.tvCancel.setOnClickListener { updateDialog.dismiss() }

    }
    /*
    *   END    UI DIALOG updateRecordDialog if button pressed
    *
    * */




    /*
    UI Alert Dialog - deleteRecordAlertDialog
     */
    private fun deleteRecordAlertDialog(
        id: Int,
        employeeDAO: EmployeeDAO,
        employeeEntity: EmployeeEntity
    ) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Record")
        //set message for alert dialog
        builder.setMessage("Are you sure you wants to delete ${employeeEntity.name}.")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        /*
        performing positive action
         */
        builder.setPositiveButton("Yes") { monDialog, _ ->
            lifecycleScope.launch {
                employeeDAO.delete(EmployeeEntity(id))
                Toast.makeText(
                    applicationContext,
                    "Record deleted successfully.",
                    Toast.LENGTH_LONG
                ).show()

                monDialog.dismiss() // Dialog will be dismissed
            }
        }
        /*
      END performing positive action
      */

        /*
         performing negative action
        */
        builder.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }

        /*
        *  FINALISATION DE L'ALERTE DIALOG
        * */
        val alertDialogForDeleteRecord: AlertDialog = builder.create()
        alertDialogForDeleteRecord.setCancelable(false)
        alertDialogForDeleteRecord.show()

    }
}