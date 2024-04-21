package com.example.smakegame

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : Activity() {

    private lateinit var highestScoreTextView: TextView
    private var highestScore = 0
    private lateinit var sharedPreferences: SharedPreferences
    private var isGameEnded = false // Declare boolean flag to track game end

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
        highestScore = sharedPreferences.getInt("highestScore", 0)

        val resetButton = findViewById<Button>(R.id.reset_highest_score)
        highestScoreTextView = findViewById(R.id.highest_score)
        highestScoreTextView.text = "Highest Score: $highestScore"

        val board = findViewById<RelativeLayout>(R.id.board)
        val upButton = findViewById<Button>(R.id.up)
        val downButton = findViewById<Button>(R.id.down)
        val leftButton = findViewById<Button>(R.id.left)
        val rightButton = findViewById<Button>(R.id.right)
        val pauseButton = findViewById<Button>(R.id.pause)
        val newgame = findViewById<Button>(R.id.new_game)
        val resume = findViewById<Button>(R.id.resume)
        val playagain = findViewById<Button>(R.id.playagain)
        val score2 = findViewById<Button>(R.id.score2)
        val endGameButton = findViewById<Button>(R.id.end_game)
        highestScoreTextView = findViewById(R.id.highest_score)
        highestScoreTextView.text = " Highest --- Score: $highestScore"

        val meat = ImageView(this)
        val snake = ImageView(this)
        val snakeSegments = mutableListOf(snake)
        val handler = Handler()
        var delayMillis = 30L
        var currentDirection = "right"
        var scorex = 0

        board.visibility = View.INVISIBLE
        playagain.visibility = View.INVISIBLE
        score2.visibility = View.INVISIBLE

        resetButton.setOnClickListener {
            // Reset the highest score to 0
            highestScore = 0
            highestScoreTextView.text = " Highest --- Score: $highestScore"
            saveHighestScore()
        }

        newgame.setOnClickListener {
            board.visibility = View.VISIBLE
            newgame.visibility = View.INVISIBLE
            resume.visibility = View.INVISIBLE
            score2.visibility = View.VISIBLE
            resetButton.visibility = View.INVISIBLE
            endGameButton.visibility = View.VISIBLE

            val snakeWidth = 100 // Snake width in pixels
            val snakeHeight = 100 // Snake height in pixels
            val meatWidth = 200 // Meat width in pixels
            val meatHeight = 80 // Meat height in pixels

            snake.setImageResource(R.drawable.snake)
            snake.setPadding(10, 10, 10, 10) // Add padding to increase touch-sensitive area
            snake.layoutParams = ViewGroup.LayoutParams(snakeWidth, snakeHeight)
            board.addView(snake)
            snakeSegments.add(snake)

            var snakeX = snake.x
            var snakeY = snake.y

            meat.setImageResource(R.drawable.meat)
            meat.setPadding(-10, -100, -10, -100) // Add padding to increase touch-sensitive area
            meat.layoutParams = ViewGroup.LayoutParams(meatWidth, meatHeight)
            board.addView(meat)

            val random = Random()
            val randomX = random.nextInt(701) - 400
            val randomY = random.nextInt(701) - 400

            meat.x = randomX.toFloat()
            meat.y = randomY.toFloat()

            fun checkFoodCollision() {
                val meatBounds = Rect()
                meat.getHitRect(meatBounds)

                for (segment in snakeSegments) {
                    val segmentBounds = Rect()
                    segment.getHitRect(segmentBounds)

                    if (Rect.intersects(meatBounds, segmentBounds)) {
                        val newSnakeSegment = ImageView(this)
                        newSnakeSegment.setImageResource(R.drawable.snake)
                        newSnakeSegment.setPadding(20, 20, 20, 20) // Add padding to increase touch-sensitive area
                        newSnakeSegment.layoutParams = ViewGroup.LayoutParams(snakeWidth, snakeHeight)
                        board.addView(newSnakeSegment)
                        snakeSegments.add(newSnakeSegment)

                        val randomX = random.nextInt(board.width - 600)
                        val randomY = random.nextInt(board.height - 600)

                        meat.x = randomX.toFloat()
                        meat.y = randomY.toFloat()

                        delayMillis--
                        scorex++
                        score2.text = "score : " + scorex.toString()

                        if (!isGameEnded) { // Check the flag before updating the highest score
                            if (scorex > highestScore) {
                                highestScore = scorex
                                highestScoreTextView.text = "Highest Score: $highestScore"
                                saveHighestScore()
                            }
                        }

                        break // Exit the loop once collision is detected
                    }
                }
            }

            val runnable = object : Runnable {
                override fun run() {
                    for (i in snakeSegments.size - 1 downTo 1) {
                        snakeSegments[i].x = snakeSegments[i - 1].x
                        snakeSegments[i].y = snakeSegments[i - 1].y
                    }

                    when (currentDirection) {
                        "up" -> {
                            snakeY -= 3
                            if (snakeY < -600) {
                                snakeY = board.height / 2.toFloat()
                            }
                            snake.translationY = snakeY
                        }
                        "down" -> {
                            snakeY += 3
                            if (snakeY > 670 - snake.height) {
                                snakeY = -600f
                            }
                            snake.translationY = snakeY
                        }
                        "left" -> {
                            snakeX -= 3
                            if (snakeX < -500) {
                                snakeX = 560f
                            }
                            snake.translationX = snakeX
                        }
                        "right" -> {
                            snakeX += 3
                            if (snakeX > 600 - snake.width) {
                                snakeX = -500f
                            }
                            snake.translationX = snakeX
                        }
                        "pause" -> {
                            // No need to update position when paused
                        }
                    }

                    checkFoodCollision()
                    handler.postDelayed(this, delayMillis)
                }
            }

            handler.postDelayed(runnable, delayMillis)

            upButton.setOnClickListener {
                currentDirection = "up"
            }
            downButton.setOnClickListener {
                currentDirection = "down"
            }
            leftButton.setOnClickListener {
                currentDirection = "left"
            }
            rightButton.setOnClickListener {
                currentDirection = "right"
            }
            pauseButton.setOnClickListener {
                currentDirection = "pause"
                board.visibility = View.INVISIBLE
                resume.visibility = View.VISIBLE
                endGameButton.visibility = View.VISIBLE
            }
            resume.setOnClickListener {
                currentDirection = "right"
                board.visibility = View.VISIBLE
                resume.visibility = View.INVISIBLE
            }
            endGameButton.setOnClickListener {
                isGameEnded = true // Update the flag when the game ends
                finish()
            }
        }

        hideSystemUI()
    }

    private fun saveHighestScore() {
        val editor = sharedPreferences.edit()
        editor.putInt("highestScore", highestScore)
        editor.apply()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView.findViewById(android.R.id.content)).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
