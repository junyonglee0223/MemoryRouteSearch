package com.example.memoryroutesearch.ui

import android.os.CountDownTimer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import com.example.memoryroutesearch.model.Problem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryRouteGame() {
    var boardSize by remember { mutableStateOf(4) } // Default to 4x4 board
    val drawerState = rememberDrawerState(DrawerValue.Closed) // Drawer state
    val coroutineScope = rememberCoroutineScope()
    val board = remember { mutableStateListOf<GridItem>() } // Board data
    var isGameOver by remember { mutableStateOf(false) }
    var isFlipped by remember { mutableStateOf(false) } // Alphabet/Numeric flip state
    var timerText by remember { mutableStateOf("60") } // Default timer: 60 seconds
    var problems by remember { mutableStateOf(emptyList<Problem>()) }
    var currentProblemIndex by remember { mutableStateOf(0) }
    var showSolution by remember { mutableStateOf(false) } // Show solution toggle
    val selectedIndices = remember { mutableStateListOf<Int>() } // Selected indices for cards

    var timer: CountDownTimer? by remember { mutableStateOf(null) } // 타이머 관리

    // 게임 초기화 함수
    fun initializeGame(newBoardSize: Int) {
        boardSize = newBoardSize
        board.clear()
        board.addAll(generateInitialItems(boardSize))
        problems = generateValidProblems(board, boardSize)
        isGameOver = false
        isFlipped = false
        currentProblemIndex = 0
        showSolution = false
        timerText = if (boardSize == 3) "30" else "60" // 타이머 초기화
        selectedIndices.clear()

        // 기존 타이머 취소 후 새로 설정
        timer?.cancel()
        timer = object : CountDownTimer(timerText.toLong() * 1000, 1000) {
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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // 사이드바 메뉴
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Board Size", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    initializeGame(3) // 3x3 보드
                    coroutineScope.launch { drawerState.close() } // 사이드바 닫기
                }, modifier = Modifier.padding(8.dp)) {
                    Text("3x3 Board")
                }
                Button(onClick = {
                    initializeGame(4) // 4x4 보드
                    coroutineScope.launch { drawerState.close() } // 사이드바 닫기
                }, modifier = Modifier.padding(8.dp)) {
                    Text("4x4 Board")
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                SmallTopAppBar(
                    title = { Text("Memory Route Search") },
                    navigationIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch { drawerState.open() } // 사이드바 열기
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
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
                        // 게임 종료 화면
                        Text("Game Over!", style = MaterialTheme.typography.headlineMedium)
                        Button(onClick = { initializeGame(boardSize) }) {
                            Text("Restart Game")
                        }
                    } else {
                        // 게임 진행 화면
                        Text(
                            text = "Time left: $timerText seconds",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                        val currentProblem = if (currentProblemIndex < problems.size) problems[currentProblemIndex] else null
                        currentProblem?.let { problem ->
                            Text(
                                text = "Problem: Sum = ${problem.number}, Start = ${problem.start}, End = ${problem.end}",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(16.dp)
                            )
                            if (showSolution) {
                                Text(
                                    text = "Solution: ${problem.solutionPath.joinToString(" -> ") { index -> board[index].back }}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Green,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                        Board(
                            board = board,
                            boardSize = boardSize,
                            onCardClick = { index ->
                                if (isFlipped) {
                                    board[index] = board[index].copy(isFlipped = !board[index].isFlipped)

                                    // 선택한 카드 인덱스 저장
                                    selectedIndices.add(index)

                                    // 정답 확인
                                    val currentProblem = problems[currentProblemIndex]
                                    if (
                                        selectedIndices.contains(currentProblem.solutionPath.first()) && // 시작점 포함
                                        selectedIndices.contains(currentProblem.solutionPath.last()) && // 끝점 포함
                                        selectedIndices.sumOf { board[it].front.toInt() } == currentProblem.number // 합 체크
                                    ) {
                                        // 정답일 경우
                                        currentProblemIndex++ // 다음 문제로 이동
                                        if (currentProblemIndex >= problems.size) {
                                            isGameOver = true // 마지막 문제라면 게임 종료
                                        }

                                        // 선택한 카드 초기화
                                        selectedIndices.forEach { idx ->
                                            board[idx] = board[idx].copy(isFlipped = true)
                                        }
                                        selectedIndices.clear()
                                    }
                                }
                            }
                        )

                        // 버튼 추가
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // 뒤집기 버튼
                            Button(onClick = {
                                isFlipped = !isFlipped
                                board.forEachIndexed { index, item ->
                                    board[index] = item.copy(isFlipped = isFlipped)
                                }
                            }) {
                                Text(if (isFlipped) "Flip to Numbers" else "Flip to Alphabets")
                            }

                            // 솔루션 버튼
                            Button(onClick = { showSolution = !showSolution }) {
                                Text(if (showSolution) "Hide Solution" else "Show Solution")
                            }
                        }
                    }
                }
            }
        )
    }
}


@Composable
fun Board(board: List<GridItem>, boardSize: Int, onCardClick: (Int) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(boardSize),
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
