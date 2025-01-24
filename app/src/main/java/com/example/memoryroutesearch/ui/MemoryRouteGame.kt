package com.example.memoryroutesearch.ui

import android.os.CountDownTimer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.memoryroutesearch.logic.generateInitialItems
import com.example.memoryroutesearch.logic.generateValidProblems
import com.example.memoryroutesearch.model.GridItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryRouteGame() {
    val board = remember { mutableStateListOf(*generateInitialItems().toTypedArray()) }
    var isGameOver by remember { mutableStateOf(false) }
    var isFlipped by remember { mutableStateOf(false) }
    var timerText by remember { mutableStateOf("60") }
    var problems by remember { mutableStateOf(generateValidProblems(board)) }
    var currentProblemIndex by remember { mutableStateOf(0) }
    val selectedIndices = remember { mutableStateListOf<Int>() }
    var showSolution by remember { mutableStateOf(false) }

    val currentProblem = if (currentProblemIndex < problems.size) problems[currentProblemIndex] else null

    LaunchedEffect(Unit) {
        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerText = (millisUntilFinished / 1000).toString()
            }

            override fun onFinish() {
                isFlipped = true
                board.forEachIndexed { index, item ->
                    board[index] = item.copy(isFlipped = true)
                }
            }
        }.start()
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(title = { Text("Memory Route Search") })
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isGameOver) {
                    Text("Game Over!", style = MaterialTheme.typography.headlineMedium)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Time left: $timerText seconds",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                        currentProblem?.let { problem ->
                            Text(
                                text = "Problem: Sum = ${problem.number}, Start = ${problem.start}, End = ${problem.end}",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(16.dp)
                            )
                            if (showSolution) {
                                val solutionAlphabets = problem.solutionPath.joinToString(" -> ") { index ->
                                    board[index].back
                                }
                                Text(
                                    text = "Solution: $solutionAlphabets",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Green,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                        Board(board = board, onCardClick = { index ->
                            if (isFlipped) {
                                board[index] = board[index].copy(isFlipped = !board[index].isFlipped)
                                selectedIndices.add(index)
                                if (currentProblem != null &&
                                    selectedIndices.contains(currentProblem.solutionPath.first()) &&
                                    selectedIndices.contains(currentProblem.solutionPath.last())
                                ) {
                                    val selectedSum = selectedIndices.sumOf { board[it].front.toInt() }
                                    if (selectedSum == currentProblem.number) {
                                        currentProblemIndex++
                                        if (currentProblemIndex >= problems.size) {
                                            isGameOver = true
                                        }
                                    }
                                    selectedIndices.forEach { idx ->
                                        board[idx] = board[idx].copy(isFlipped = true)
                                    }
                                    selectedIndices.clear()
                                }
                            }
                        })
                    }

                    // 버튼 추가
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = {
                            isFlipped = !isFlipped
                            board.forEachIndexed { index, item ->
                                board[index] = item.copy(isFlipped = isFlipped)
                            }
                        }) {
                            Text(if (isFlipped) "Flip to Numbers" else "Flip to Alphabets")
                        }

                        Button(onClick = { showSolution = !showSolution }) {
                            Text(if (showSolution) "Hide Solution" else "Show Solution")
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun Board(board: List<GridItem>, onCardClick: (Int) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(board.size) { index ->
            Card(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clickable { onCardClick(index) },
                colors = CardDefaults.cardColors(containerColor = if (board[index].isFlipped) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.elevatedCardElevation(8.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = if (board[index].isFlipped) board[index].back else board[index].front,
                        fontSize = 20.sp,
                        color = if (board[index].isFlipped) Color.Black else Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
