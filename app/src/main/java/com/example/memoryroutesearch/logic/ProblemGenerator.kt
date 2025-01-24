package com.example.memoryroutesearch.logic

import com.example.memoryroutesearch.model.GridItem
import com.example.memoryroutesearch.model.Problem
fun generateInitialItems(boardSize: Int): List<GridItem> {
    val totalCards = boardSize * boardSize
    val numbers = (1..totalCards).map { it.toString() }
    val alphabets = ('A' until 'A' + totalCards).map { it.toString() }
    return numbers.zip(alphabets).map { (front, back) ->
        GridItem(front = front, back = back)
    }.shuffled()
}

fun generateValidProblems(board: List<GridItem>, boardSize: Int): List<Problem> {
    val allSums = mutableSetOf<Pair<Int, List<Int>>>()
    val directions = listOf(-boardSize, boardSize, -1, 1) // 상, 하, 좌, 우

    fun dfs(currentIndex: Int, path: List<Int>, visited: MutableSet<Int>, currentSum: Int) {
        allSums.add(Pair(currentSum, path))
        for (direction in directions) {
            val nextIndex = currentIndex + direction
            if (nextIndex in 0 until board.size && !visited.contains(nextIndex)) {
                if ((direction == -1 && currentIndex % boardSize == 0) || // 왼쪽 경계
                    (direction == 1 && currentIndex % boardSize == boardSize - 1) // 오른쪽 경계
                ) continue
                visited.add(nextIndex)
                dfs(nextIndex, path + nextIndex, visited, currentSum + board[nextIndex].front.toInt())
                visited.remove(nextIndex)
            }
        }
    }

    for (startIndex in board.indices) {
        dfs(startIndex, listOf(startIndex), mutableSetOf(startIndex), board[startIndex].front.toInt())
    }

    return allSums.filter { (sum, path) ->
        path.size > 1 && board[path.first()].back != board[path.last()].back
    }.map { (sum, path) ->
        Problem(
            number = sum,
            start = board[path.first()].back,
            end = board[path.last()].back,
            solutionPath = path
        )
    }.shuffled().take(3)
}


fun isSolutionValid(problem: Problem, selectedPath: List<Int>, board: List<GridItem>): Boolean {
    if (selectedPath.isEmpty() || selectedPath.first() != board.indexOfFirst { it.back == problem.start } ||
        selectedPath.last() != board.indexOfFirst { it.back == problem.end }) {
        return false
    }
    val calculatedSum = selectedPath.sumOf { board[it].front.toInt() }
    return calculatedSum == problem.number
}
