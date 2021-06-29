package com.example.game2048

import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.VolumeShaper
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import java.util.prefs.Preferences

const val TAG = "Game"
private val arr4 = Array(4) { 0 }
var grid = Array(4) { Array(4) { 0 } }
val grid_new = Array(4) { Array(4) { 0 } }
var past = Array(4) { Array(4) { 0 } }
var score = 0
var best = 100
var beginGame = true
var currentOrientation = 1

class MainActivity : AppCompatActivity() {
    val scoreView: TextView by lazy { findViewById(R.id.score_view) }
    val bestView: TextView by lazy { findViewById(R.id.best_view) }
    val resetBtn: Button by lazy { findViewById(R.id.reset_btn) }
    val allView: ViewGroup by lazy { findViewById(R.id.view_group) }
    val savedState: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        resetBtn.setOnClickListener {
            saveGame()
            beginGame = true
            startGame()
        }

        allView.setOnTouchListener(
            object : OnSwipeTouchListener(this@MainActivity) {
                override fun onSwipeTop() {
                    super.onSwipeTop()
                    run(Dir.UP)
                }

                override fun onSwipeBottom() {
                    super.onSwipeBottom()
                    run(Dir.DOWN)
                }

                override fun onSwipeLeft() {
                    super.onSwipeLeft()
                    run(Dir.LEFT)
                }

                override fun onSwipeRight() {
                    super.onSwipeRight()
                    run(Dir.RIGHT)
                }
            })

        setClicks()
        startGame()
    }

    override fun onDestroy() {
        super.onDestroy()
        saveGame()
    }

    fun startGame() {
        best = savedState.getInt("BEST", 0)
        bestView.text = best.toString()
        score = 0

        val json = savedState.getString("GRID", "") ?: ""
        if (beginGame || json.length == 0) {
            grid = blankGrid()
            addNumber()
            addNumber()
            beginGame = false
        } else {
            val gson = Gson()
            val type = object : TypeToken<Array<Array<Int>>>()  {}.type
            grid = gson.fromJson(json, type)
        }
        showGrid()
    }

    fun showGrid() {
//        currentOrientation = Configuration().orientation
        currentOrientation = getResources().getConfiguration().orientation
        for (j in 0..3)
            for (i in 0..3) {
                val id = "tile" + j + i
                val cell = findViewById<TextView>(
                    resources.getIdentifier(id, "id", packageName)
                )
                val x = grid[i][j]
                if (grid_new[i][j] == 1) {
                    // add number
                    cell.setBackgroundResource(R.drawable.textview_bg)
                    grid_new[i][j] = 0
                    cell.text = x.toString()
                    cell.textSize = 64f
                    if (currentOrientation === Configuration.ORIENTATION_LANDSCAPE) {
                        cell.textSize = 48f
                    }
                } else {
                    if (x > 0) {
                        var textSize = colorsSizes[x]?.first?.toFloat() ?: 8.0f
                        if (currentOrientation === Configuration.ORIENTATION_LANDSCAPE) {
                            textSize *= 0.8f
                        }
                        cell.textSize = textSize
                        val color = colorsSizes[x]?.second ?: "#000000"
                        cell.setBackgroundColor(Color.parseColor(color))
                        cell.text = x.toString()
                    } else {
                        cell.setBackgroundColor(Color.parseColor("#AAAAAA"))
                        cell.text = ""
                    }
                }
            }
        scoreView.text = score.toString()
    }

    fun setClicks() {
        tile01.setOnClickListener { getClicks(tile01) }
        tile02.setOnClickListener { getClicks(tile02) }
        tile10.setOnClickListener { getClicks(tile10) }
        tile20.setOnClickListener { getClicks(tile20) }
        tile31.setOnClickListener { getClicks(tile31) }
        tile32.setOnClickListener { getClicks(tile32) }
        tile13.setOnClickListener { getClicks(tile13) }
        tile23.setOnClickListener { getClicks(tile23) }
        btn_up.setOnClickListener { getClicks(btn_up) }
        btn_left.setOnClickListener { getClicks(btn_left) }
        btn_right.setOnClickListener { getClicks(btn_right) }
        btn_down.setOnClickListener { getClicks(btn_down) }
    }

    fun getClicks(v: View) {
        var dir = Dir.DOWN
        when (v) {
            tile01, tile02, btn_up -> {
                dir = Dir.UP
            }
            tile10, tile20, btn_left -> {
                dir = Dir.LEFT
            }
            tile31, tile32, btn_down -> {
                dir = Dir.DOWN
            }
            tile13, tile23, btn_right -> {
                dir = Dir.RIGHT
            }
        }

        run(dir)
    }

    fun run(dir: Dir) {
        var flipped = false
        var rotated = false
        var played = true
        when (dir) {
            Dir.UP -> {
                flipGrid()
                flipped = true
            }
            Dir.RIGHT -> {
                grid = transposeGrid(grid, 1)
                rotated = true
            }
            Dir.LEFT -> {
                grid = transposeGrid(grid, 1)
                flipGrid()
                rotated = true
                flipped = true
            }
            Dir.DOWN -> {
            }
            else -> {
                played = false
            }
        }

        if (played) {
            past = copyGrid(grid)
            for (i in 0..3) {
                grid[i] = operate(grid[i])
            }
        }
        val changed = !past.contentDeepEquals(grid)
        if (flipped) {
            flipGrid()
        }
        if (rotated) {
            grid = transposeGrid(grid, -1);
        }
        if (changed) {
            addNumber()
        }

        showGrid()

        if (isGameOver()) {
//            savedState.edit().putInt("Score", score)
            resetBtn.text = "GAME OVER!!!"
            saveGame()
        }
        if (isGameWon()) {
//            savedState.edit().putInt("Score", score)
            resetBtn.text = "GAME WON!!!"
            saveGame()
        }

    }

    fun saveGame() {
        val edit = savedState.edit()
        val gson = Gson()
        val json = gson.toJson(grid)
        edit.putString("GRID", json)
        Log.d("GSON", json)
        if (score > best) {
            edit.putInt("BEST", score)
        }
        edit.apply()
    }

    enum class Dir {
        UP, RIGHT, DOWN, LEFT
    }
}