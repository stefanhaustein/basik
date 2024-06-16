package org.kobjects.basik.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview

import kotlinx.coroutines.launch
import org.kobjects.konsole.compose.ComposeKonsole
import org.kobjects.basik.Interpreter
import org.kobjects.konsole.compose.RenderKonsole

@Composable
@Preview
fun App() {
    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar {
                    Text("  KMP Basic Interpreter")
                }
            }
        ) {
            val konsole = remember { ComposeKonsole() }

            // Most of this should probably go into a view model...
            val coroutineScope = rememberCoroutineScope()
            val launched = mutableStateOf(false)
            if (!launched.value) {
                launched.value = true
                coroutineScope.launch {
                    Interpreter(
                        { konsole.println(it) },
                        { konsole.readln(it) }
                    ).runShell()
                }
            }

            RenderKonsole(Modifier.padding(it).fillMaxSize(), konsole)
        }
    }
}
