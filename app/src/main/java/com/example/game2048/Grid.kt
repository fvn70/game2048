package com.example.game2048

import android.content.Context
import android.widget.TextView

fun blankGrid(): Array<Array<Int>> {
    return Array(4) { Array(4) { 0 } }
}

fun copyGrid(grid: Array<Array<Int>>): Array<Array<Int>> {
    val extra = blankGrid()
    for (i in 0..3)
        for (j in 0..3)
            extra[i][j] = grid[i][j]
    return extra;
}

fun fillGrid(arr: Array<Array<Int>>) {
    for (i in 0..3)
        for (j in 0..3)
            arr[i][j] = i*4 + j
}

fun addNumber() {
    val options = arrayListOf<Pos>()
    for (i in 0..3)
        for (j in 0..3)
            if (grid[i][j] == 0) {
                val pos = Pos(i, j)
                options.add(pos)
            }
    if (options.size > 0) {
        val spot = options.shuffled().first()
        val r = (1..100).shuffled().first()
        grid[spot.x][spot.y] = if (r > 10) 2 else 4
        grid_new[spot.x][spot.y] = 1
    }
}

fun flipGrid() {
    for (i in 0..3) {
        grid[i].reverse();
    }
}

fun transposeGrid(grid: Array<Array<Int>>, direction: Int): Array<Array<Int>> {
    val newGrid = blankGrid();
    for (i in 0..3)
        for (j in 0..3)
            if (direction == 1) {
                newGrid[i][j] = grid[j][i]
            } else {
                newGrid[j][i] = grid[i][j]
            }

    return newGrid
}


data class Pos(
    val x: Int,
    val y: Int
)