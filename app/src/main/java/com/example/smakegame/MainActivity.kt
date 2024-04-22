package com.example.smakegame

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
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
        val mainMenu = findViewById<Button>(R.id.MainMenu)
        val newgame = findViewById<Button>(R.id.new_game)
        val resume = findViewById<Button>(R.id.resume)
        val playagain = findViewById<RelativeLayout>(R.id.board1)
        val score2 = findViewById<Button>(R.id.score2)
        val endGameButton = findViewById<Button>(R.id.end_game)
        highestScoreTextView = findViewById(R.id.highest_score)
        highestScoreTextView.text = " Highest --- Score: $highestScore"

        val meat = ImageView(this)
        val snake = ImageView(this)
        val badman = ImageView(this) // Add badman ImageView
        val snakeSegments = mutableListOf(snake)
        val handler = Handler()
        var delayMillis = 40L
        var currentDirection = "right"
        var scorex = 0

        board.visibility = View.INVISIBLE
        playagain.visibility = View.INVISIBLE
        score2.visibility = View.VISIBLE

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

            val snakeWidth = 172 // Snake width in pixels
            val snakeHeight = 300 // Snake height in pixels
            val meatWidth = 112 // Meat width in pixels
            val meatHeight = 294 // Meat height in pixels
            val badmanWidth = 130 // Badman width in pixels
            val badmanHeight = 220 // Badman height in pixels

            snake.setImageResource(R.drawable.snake)
            snake.setPadding(10, 10, 10, 10) // Add padding to increase touch-sensitive area
            snake.layoutParams = ViewGroup.LayoutParams(snakeWidth, snakeHeight)
            board.addView(snake)
            snakeSegments.add(snake)

            var snakeX = snake.x
            var snakeY = snake.y

            meat.setImageResource(R.drawable.meat)
            meat.setPadding(-10, -80, -10, -60) // Add padding to increase touch-sensitive area
            meat.layoutParams = ViewGroup.LayoutParams(meatWidth, meatHeight)
            board.addView(meat)

            badman.setImageResource(R.drawable.badman) // Assuming "badman" is the name of your vector drawable
            badman.layoutParams = ViewGroup.LayoutParams(badmanWidth, badmanHeight)
            board.addView(badman)

            // Function to generate random coordinates for badman within the board bounds
            fun generateRandomPosition(): Pair<Float, Float> {
//                val randomX = Random().nextInt(500 - badmanWidth)
//                val randomY = Random().nextInt(500 - badmanHeight)
                return Pair(400f, 550f)
            }

            // Position the badman at a random location initially
            var (badmanX, badmanY) = generateRandomPosition()
            badman.x = badmanX
            badman.y = badmanY

            // Add logic to position the badman within the board layout

            fun checkFoodCollision() {
                val meatBounds = Rect()
                meat.getHitRect(meatBounds)

                for (segment in snakeSegments) {
                    val segmentBounds = Rect()
                    segment.getHitRect(segmentBounds)

                    if (Rect.intersects(meatBounds, segmentBounds)) {
                        val randomX = Random().nextInt(board.width - 200)
                        val randomY = Random().nextInt(board.height - 200)

                        meat.x = randomX.toFloat()
                        meat.y = randomY.toFloat()

                        delayMillis--
                        scorex++
                        score2.text = "score : $scorex"

                        if (!isGameEnded) { // Check the flag before updating the highest score
                            if (scorex > highestScore) {
                                highestScore = scorex
                                highestScoreTextView.text = " Highest --- Score: $highestScore"
                                saveHighestScore()
                            }
                        }

                        break // Exit the loop once collision is detected
                    }
                }
            }

            fun checkBadmanCollision() {
                val badmanBounds = Rect()
                badman.getHitRect(badmanBounds)

                for (segment in snakeSegments) {
                    val segmentBounds = Rect()
                    segment.getHitRect(segmentBounds)

                    if (Rect.intersects(badmanBounds, segmentBounds)) {
                        isGameEnded = true // End the game if badman collision detected
                        playagain.visibility = View.VISIBLE
                        board.visibility = View.INVISIBLE
                        newgame.visibility = View.INVISIBLE
                        mainMenu.visibility = View.VISIBLE

                        return // Exit the function once collision is detected
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
                                snakeY = 760f
                            }
                            snake.translationY = snakeY
                        }
                        "down" -> {
                            snakeY += 3
                            if (snakeY > 1020 - snake.height) {
                                snakeY = -650f
                            }
                            snake.translationY = snakeY
                        }
                        "left" -> {
                            snakeX -= 3
                            if (snakeX < -500) {
                                snakeX = 560f
                            }
                            snake.scaleX = -1f // Flip the snake horizontally
                            snake.translationX = snakeX
                        }
                        "right" -> {
                            snakeX += 3
                            if (snakeX > 600 - snake.width) {
                                snakeX = -500f
                            }
                            snake.scaleX = 1f
                            snake.translationX = snakeX
                        }
                        "pause" -> {
                            // No need to update position when paused
                        }
                    }

                    checkFoodCollision()
                    checkBadmanCollision() // Check for badman collision
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

        mainMenu.setOnClickListener {
            val Intent = Intent(this, newGame::class.java)
            startActivity(Intent)
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
