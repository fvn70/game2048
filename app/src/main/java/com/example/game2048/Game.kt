package com.example.game2048

// сдвиг элементов вправо
fun slide(row: Array<Int>): Array<Int> {
    val arr = (row.filter {it > 0})
    val missing = 4 - arr.size
    val zeros = ArrayList<Int>()
    for (i in 0..missing-1) {
        zeros.add(0)
    }
    val new = (zeros + arr).toTypedArray()
    return new
}

fun operate(row: Array<Int>): Array<Int> {
    var arr = slide(row)
    arr = combine(arr);
    arr = slide(arr)
    return arr
}

// operating on array itself
fun combine(row: Array<Int>): Array<Int> {
    for (i in 3 downTo 1) {
        val a = row[i]
        val b = row[i - 1]
        if (a == b) {
            row[i] = a + b
            score += row[i]
            row[i - 1] = 0
        }
    }
    return row;
}

fun isGameOver(): Boolean {
    for (i in 0..3) {
        for (j in 0..3) {
        if (grid[i][j] == 0) {
            return false
        }
        if (j < 3 && grid[i][j] == grid[i][j+1]) {
            return false
        }
        if (i < 3 && grid[i][j] == grid[i+1][j]) {
            return false
        }
    }
    }
    return true
}

fun isGameWon(): Boolean {
    for (i in 0..3) {
        for (j in 0..3) {
            if (grid[i][j] == 2048) {
                return true
            }
        }
    }
    return false
}

