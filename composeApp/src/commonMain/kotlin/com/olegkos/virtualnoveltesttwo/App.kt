package com.olegkos.virtualnovelapp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.olegkos.vnengine.dsl.scenario
import com.olegkos.vnengine.scene.SceneNode

@Composable
fun App(viewModel: GameViewModel? = null) {
    val scenes = remember {
        scenario {
            scene("intro") {
                text("Ты просыпаешься в темноте.")
                choice("Встать" to "hall", "Лежать" to "sleep")
            }
            scene("hall") {
                text("Ты в коридоре.")
                choice("Идти дальше" to "end")
            }
            scene("sleep") {
                text("Ты снова засыпаешь...")
                jump("end")
            }
            scene("end") {
                text("Конец истории.")
            }
        }
    }

    // Если ViewModel передали, используем её, иначе создаём локально
    val vm = viewModel ?: remember { GameViewModel(scenes) }
    val currentNode by remember { derivedStateOf { vm.currentNode } }

    when (val node = currentNode) {
        is SceneNode.Text -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(node.text)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { vm.next() }) {
                    Text("Далее")
                }
            }
        }
        is SceneNode.Choice -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                node.options.forEach { option ->
                    Button(
                        onClick = { vm.next(option) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text(option.text)
                    }
                }
            }
        }
        is SceneNode.Jump -> {
            LaunchedEffect(Unit) {
                vm.next()
            }
        }
    }
}