package com.example.memoryroutesearch.model

data class Problem(
    val number: Int,  // Target sum
    val start: String, // Starting alphabet
    val end: String,   // Ending alphabet
    val solutionPath: List<Int> // Indices of the solution path

)
