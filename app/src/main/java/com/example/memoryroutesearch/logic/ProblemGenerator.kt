package com.example.memoryroutesearch.logic

import com.example.memoryroutesearch.model.GridItem
import com.example.memoryroutesearch.model.Problem

fun generateInitialItems(): List<GridItem> {
    val numbers = (1..16).map { it.toString() }
    val alphabets = ('A'..'P').map { it.toString() }
    return numbers.zip(alphabets).map { (front, back) ->
        GridItem(front = front, back = back)
    }.shuffled()
}
fun generateValidProblems(board: List<GridItem>): List<Problem> {
    val validPaths = mutableSetOf<Pair<Int, List<Int>>>()
    val directions = listOf(-4, 4, -1, 1) // 상, 하, 좌, 우

    // DFS를 통해 유효한 경로를 탐색
    fun dfs(currentIndex: Int, path: List<Int>, visited: MutableSet<Int>, currentSum: Int) {
        validPaths.add(Pair(currentSum, path))

        for (direction in directions) {
            val nextIndex = currentIndex + direction

            // 범위 확인 및 경계 조건 처리
            if (nextIndex in 0 until 16 && !visited.contains(nextIndex)) {
                // 좌우 경계 처리
                if (direction == -1 && currentIndex % 4 == 0) continue // 왼쪽 경계
                if (direction == 1 && currentIndex % 4 == 3) continue // 오른쪽 경계

                visited.add(nextIndex)
                dfs(nextIndex, path + nextIndex, visited, currentSum + board[nextIndex].front.toInt())
                visited.remove(nextIndex)
            }
        }
    }

    // 보드의 모든 칸을 시작점으로 DFS 탐색
    for (startIndex in board.indices) {
        dfs(
            currentIndex = startIndex,
            path = listOf(startIndex),
            visited = mutableSetOf(startIndex),
            currentSum = board[startIndex].front.toInt()
        )
    }

    // 시작점과 끝점이 서로 다른 유효한 경로만 필터링
    return validPaths.filter { (sum, path) ->
        path.size > 1 &&
                board[path.first()].back != board[path.last()].back // 시작과 끝이 달라야 함
    }.map { (sum, path) ->
        Problem(
            number = sum,
            start = board[path.first()].back, // 시작점 알파벳
            end = board[path.last()].back,   // 끝점 알파벳
            solutionPath = path              // 유효한 경로
        )
    }.shuffled().take(3) // 최대 3개의 문제 반환
}


fun isSolutionValid(problem: Problem, selectedPath: List<Int>, board: List<GridItem>): Boolean {
    if (selectedPath.isEmpty() || selectedPath.first() != board.indexOfFirst { it.back == problem.start } ||
        selectedPath.last() != board.indexOfFirst { it.back == problem.end }) {
        return false
    }
    val calculatedSum = selectedPath.sumOf { board[it].front.toInt() }
    return calculatedSum == problem.number
}
