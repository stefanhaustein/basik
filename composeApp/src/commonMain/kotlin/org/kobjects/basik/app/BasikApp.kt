package org.kobjects.basik.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.jetbrains.compose.ui.tooling.preview.Preview

import kotlinx.coroutines.launch
import org.kobjects.konsole.compose.ComposeKonsole
import org.kobjects.basik.Interpreter
import org.kobjects.konsole.compose.RenderKonsole

@Composable
@Preview
fun BasikApp() {
    val konsole = remember { ComposeKonsole() }
    val interpreter = remember { Interpreter( { konsole.println(it) }, { konsole.readln(it) }) }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Parsek Basic") },
                    actions = {
                        IconButton(
                            onClick = { interpreter.stop() }
                        ) {
                            Icon(Icons.Filled.Stop, "Stop" )
                        }
                    }
                )
            }
        ) {
            val launched = mutableStateOf(false)
            if (!launched.value) {
                launched.value = true
                CoroutineScope(Dispatchers.Default).launch {
                    interpreter.runShell()
                }
            }

            RenderKonsole(
                Modifier.padding(it).fillMaxSize().background(Color(0xffeeeeee)),
                konsole)
        }
    }
}
