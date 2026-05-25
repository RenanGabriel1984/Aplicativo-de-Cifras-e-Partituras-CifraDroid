package com.example

import android.os.Bundle
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.ManuscriptRepository
import com.example.navigation.DigitalManuscriptApp
import com.example.ui.MainViewModel
import com.example.ui.MainViewModelFactory
import com.example.ui.theme.MyApplicationTheme
import com.example.util.PedalManager
import androidx.lifecycle.ViewModelStoreOwner

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        val database = AppDatabase.getDatabase(this)
        val repository = ManuscriptRepository(database.manuscriptDao())
        val pedalManager = PedalManager(applicationContext)
        
        setContent {
            MyApplicationTheme {
                viewModel = viewModel(
                    factory = MainViewModelFactory(repository, pedalManager)
                )
                DigitalManuscriptApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val uri: Uri? = when (intent.action) {
            Intent.ACTION_SEND -> intent.getParcelableExtra(Intent.EXTRA_STREAM) as? Uri
            Intent.ACTION_VIEW -> intent.data
            else -> null
        }
        if (uri != null) {
            if (::viewModel.isInitialized) {
                viewModel.importDocument(applicationContext, uri)
                // Clear the intent action so it doesn't import again
                intent.action = null
            }
        }
    }
}
