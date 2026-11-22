package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.CardVaultDatabase
import com.example.data.CardVaultRepository
import com.example.ui.CardVaultApp
import com.example.ui.CardVaultViewModel
import com.example.ui.CardVaultViewModelFactory
import com.example.ui.theme.CardVaultTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = CardVaultDatabase.getInstance(applicationContext)
        val repository = CardVaultRepository(
            cardDao = database.cardDao(),
            variantDao = database.variantDao()
        )
        val factory = CardVaultViewModelFactory(repository)

        setContent {
            CardVaultTheme {
                val viewModel: CardVaultViewModel = viewModel(factory = factory)
                CardVaultApp(viewModel = viewModel)
            }
        }
    }
}
