package com.example.budgetbee_prog7313_poe_final.ui.expense


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ExpenseViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Expense Fragment"
    }
    val text: LiveData<String> = _text
}
